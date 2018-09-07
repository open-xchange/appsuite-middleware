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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.health.NodeHealthCheck;
import com.openexchange.health.NodeHealthCheckProperty;
import com.openexchange.health.NodeHealthCheckResponse;
import com.openexchange.health.NodeHealthCheckService;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.threadpool.ThreadPoolService;


/**
 * {@link NodeHealthCheckServiceImpl}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.1
 */
public class NodeHealthCheckServiceImpl implements NodeHealthCheckService {

    private static final Logger LOG = LoggerFactory.getLogger(NodeHealthCheckServiceImpl.class);

    private final Map<String, NodeHealthCheck> checks;
    private final ServiceLookup services;

    public NodeHealthCheckServiceImpl(ServiceLookup services) {
        super();
        this.services = services;
        this.checks = new ConcurrentHashMap<>();
    }

    @Override
    public Map<String, NodeHealthCheck> getAllChecks() {
        return Collections.unmodifiableMap(checks);
    }

    @Override
    public NodeHealthCheck getCheck(String name) {
        return checks.get(name);
    }

    @Override
    public NodeHealthCheck addCheck(NodeHealthCheck check) {
        return checks.put(check.getName(), check);
    }

    @Override
    public NodeHealthCheck removeCheck(String name) {
        return checks.remove(name);
    }

    @Override
    public Map<String, NodeHealthCheckResponse> check() throws OXException {
        ThreadPoolService threadPoolService = services.getService(ThreadPoolService.class);
        if (null == threadPoolService) {
            throw ServiceExceptionCode.absentService(ThreadPoolService.class);
        }
        Map<String, NodeHealthCheckTask> tasks = applySkipBlacklist();
        Map<Future<NodeHealthCheckResponse>, NodeHealthCheckTask> futures = new LinkedHashMap<>(tasks.size());
        for (NodeHealthCheckTask task : tasks.values()) {
            futures.put(threadPoolService.submit(task), task);
        }

        Map<String, NodeHealthCheckResponse> result = new HashMap<>(tasks.size());
        for (Map.Entry<Future<NodeHealthCheckResponse>, NodeHealthCheckTask> futureAndTask : futures.entrySet()) {
            try {
                NodeHealthCheckResponse response = futureAndTask.getKey().get(1000, TimeUnit.MILLISECONDS);
                result.put(response.getName(), response);
            } catch (InterruptedException e) {
                // Keep interrupted status
                Thread.currentThread().interrupt();
                NodeHealthCheckTask task = futureAndTask.getValue();
                LOG.warn("Interrupted while obtaining health check response from task {}: {}", task.getName(), task.getClass().getName(), e);
            } catch (ExecutionException e) {
                NodeHealthCheckTask task = futureAndTask.getValue();
                LOG.warn("Timed out while obtaining health check response from task {}: {}", task.getName(), task.getClass().getName(), e);
            } catch (TimeoutException e) {
                NodeHealthCheckTask task = futureAndTask.getValue();
                Throwable cause = e.getCause();
                if (null == cause) {
                    // Huh...? ExecutionException w/o a cause
                    LOG.error("Failed to obtain health check response from task {}: {}", task.getName(), task.getClass().getName(), e);
                } else {
                    LOG.error("Failed to obtain health check response from task {}: {}", task.getName(), task.getClass().getName(), cause);
                }
            }
        }
        return result;
    }

    @Override
    public List<String> getIgnoreList() {
        LeanConfigurationService configService = services.getService(LeanConfigurationService.class);
        String ignoreProperty = configService.getProperty(NodeHealthCheckProperty.ignore);
        List<String> ignoreList = Collections.emptyList();
        if (Strings.isNotEmpty(ignoreProperty)) {
            ignoreList = Arrays.asList(Strings.splitByComma(ignoreProperty));
        }
        return ignoreList;
    }

    private Map<String, NodeHealthCheckTask> applySkipBlacklist() throws OXException {
        Map<String, NodeHealthCheckTask> result = new ConcurrentHashMap<>();
        LeanConfigurationService configService = services.getService(LeanConfigurationService.class);
        if (null == configService) {
            throw ServiceExceptionCode.absentService(LeanConfigurationService.class);
        }
        String skipProperty = configService.getProperty(NodeHealthCheckProperty.skip);
        List<String> skipList = Collections.emptyList();
        if (Strings.isNotEmpty(skipProperty)) {
            skipList = Arrays.asList(Strings.splitByComma(skipProperty));
        }

        if (null != checks && checks.size() > 0) {
            for (String healthCheckName : checks.keySet()) {
                if (!skipList.contains(healthCheckName)) {
                    result.put(healthCheckName, new NodeHealthCheckTask(checks.get(healthCheckName)));
                }
            }
        }
        return result;
    }

}
