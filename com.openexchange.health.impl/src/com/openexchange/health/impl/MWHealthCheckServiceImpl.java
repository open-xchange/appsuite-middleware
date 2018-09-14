/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.health.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.health.MWHealthCheckResult;
import com.openexchange.health.MWHealthCheck;
import com.openexchange.health.MWHealthCheckProperty;
import com.openexchange.health.MWHealthCheckResponse;
import com.openexchange.health.MWHealthCheckService;
import com.openexchange.health.MWHealthState;
import com.openexchange.java.ConcurrentList;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.threadpool.ThreadPoolService;


/**
 * {@link MWHealthCheckServiceImpl}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.1
 */
public class MWHealthCheckServiceImpl implements MWHealthCheckService {

    private static final Logger LOG = LoggerFactory.getLogger(MWHealthCheckServiceImpl.class);

    private final List<MWHealthCheck> checks;
    private final ServiceLookup services;

    public MWHealthCheckServiceImpl(ServiceLookup services) {
        super();
        this.services = services;
        this.checks = new ConcurrentList<>();
    }

    @Override
    public List<MWHealthCheck> getAllChecks() {
        return Collections.unmodifiableList(checks);
    }

    @Override
    public MWHealthCheck getCheck(String name) {
        for (MWHealthCheck check : checks) {
            if (check.getName().equals(name)) {
                return check;
            }
        }
        return null;
    }

    public boolean addCheck(MWHealthCheck check) {
        return checks.add(check);
    }

    public boolean removeCheck(MWHealthCheck check) {
        return checks.remove(check);
    }

    @Override
    public MWHealthCheckResult check() throws OXException {
        ThreadPoolService threadPoolService = services.getService(ThreadPoolService.class);
        if (null == threadPoolService) {
            throw ServiceExceptionCode.absentService(ThreadPoolService.class);
        }

        Set<String> blacklist = getSkipBlacklist();
        Set<String> ignorelist = getIgnoreList();
        boolean overallState = true;
        List<MWHealthCheckTask> tasks = new ArrayList<>(checks.size());
        for (MWHealthCheck check : checks) {
            if (!blacklist.contains(check.getName())) {
                tasks.add(new MWHealthCheckTask(check));
            }
        }
        List<MWHealthCheckResponse> responses = new ArrayList<>(tasks.size());
        Map<Future<MWHealthCheckResponse>, MWHealthCheckTask> futures = new LinkedHashMap<>(tasks.size());
        for (MWHealthCheckTask task : tasks) {
            futures.put(threadPoolService.submit(task), task);
        }

        for (Map.Entry<Future<MWHealthCheckResponse>, MWHealthCheckTask> futureAndTask : futures.entrySet()) {
            MWHealthCheckTask task = futureAndTask.getValue();

            MWHealthCheckResponse response = null;
            try {
                long timeout = task.getTimeout();
                response = timeout > 0 ? futureAndTask.getKey().get(timeout, TimeUnit.MILLISECONDS) : futureAndTask.getKey().get();
                if (!ignorelist.contains(response.getName())) {
                    overallState &= MWHealthState.UP.equals(response.getState());
                }
            } catch (InterruptedException e) {
                // Keep interrupted status
                Thread.currentThread().interrupt();
                LOG.warn("Interrupted while obtaining health check response from task {}: {}", task.getName(), task.getClass().getName(), e);
            } catch (TimeoutException e) {
                LOG.warn("Timed out while obtaining health check response from task {}: {}", task.getName(), task.getClass().getName(), e);
                response = createDownResponse(task, true, e);
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                if (null == cause) {
                    // Huh...? ExecutionException w/o a cause
                    response = createDownResponse(task, false, e);
                    LOG.error("Failed to obtain health check response from task {}: {}", task.getName(), task.getClass().getName(), e);
                } else {
                    response = createDownResponse(task, false, cause);
                    LOG.error("Failed to obtain health check response from task {}: {}", task.getName(), task.getClass().getName(), cause);
                }
            }
            if (null != response) {
                if (!ignorelist.contains(response.getName())) {
                    overallState &= MWHealthState.UP.equals(response.getState());
                }
                responses.add(response);
            }

        }
        return new MWHealthCheckResultImpl(overallState ? MWHealthState.UP : MWHealthState.DOWN, responses, asList(ignorelist), asList(blacklist));
    }

    private List<String> asList(Set<String> set) {
        return set.isEmpty() ? Collections.emptyList() : new ArrayList<String>(set);
    }

    private Set<String> getIgnoreList() {
        LeanConfigurationService configService = services.getService(LeanConfigurationService.class);
        String ignoreProperty = configService.getProperty(MWHealthCheckProperty.ignore);
        if (Strings.isNotEmpty(ignoreProperty)) {
            return new LinkedHashSet<String>(Arrays.asList(Strings.splitByComma(ignoreProperty)));
        }
        return Collections.emptySet();
    }

    private Set<String> getSkipBlacklist() throws OXException {
        LeanConfigurationService configService = services.getService(LeanConfigurationService.class);
        if (null == configService) {
            throw ServiceExceptionCode.absentService(LeanConfigurationService.class);
        }
        String skipProperty = configService.getProperty(MWHealthCheckProperty.skip);
        if (Strings.isNotEmpty(skipProperty)) {
            return new LinkedHashSet<String>(Arrays.asList(Strings.splitByComma(skipProperty)));
        }
        return Collections.emptySet();
    }

    private MWHealthCheckResponse createDownResponse(MWHealthCheckTask task, boolean timeout, Throwable e) {
        Map<String, Object> data = new HashMap<>();
        if (timeout) {
            data.put("error", "Timeout after " + task.getTimeout() / 1000L + " seconds");
        } else {
            data.put("error", e.getMessage());
        }
        return new MWHealthCheckResponseImpl(task.getName(), data, MWHealthState.DOWN);
    }

}
