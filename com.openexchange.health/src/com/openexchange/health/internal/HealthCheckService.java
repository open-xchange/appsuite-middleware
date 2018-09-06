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

package com.openexchange.health.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
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
import com.openexchange.health.DefaultHealthCheck;
import com.openexchange.health.DefaultHealthCheckResponse;
import com.openexchange.health.DefaultHealthCheckTask;
import com.openexchange.java.Strings;
import com.openexchange.osgi.annotation.SingletonService;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.threadpool.ThreadPoolService;

/**
 * {@link HealthCheckService}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.1
 */
@SingletonService
public class HealthCheckService {

    private static final Logger LOG = LoggerFactory.getLogger(HealthCheckService.class);

    private final ServiceLookup services;
    private final HealthCheckRegistry registry;

    public HealthCheckService(ServiceLookup services, HealthCheckRegistry registry) {
        super();
        this.services = services;
        this.registry = registry;
    }

    public Map<String, DefaultHealthCheckResponse> check() throws OXException {
        ThreadPoolService threadPoolService = services.getService(ThreadPoolService.class);
        if (null == threadPoolService) {
            throw ServiceExceptionCode.absentService(ThreadPoolService.class);
        }

        Map<String, DefaultHealthCheckTask> tasks = applySkipBlacklist();
        List<Future<DefaultHealthCheckResponse>> futures = new ArrayList<>();
        for (DefaultHealthCheckTask task : tasks.values()) {
            futures.add(threadPoolService.submit(task));
        }

        Map<String, DefaultHealthCheckResponse> result = new HashMap<>();
        for (Future<DefaultHealthCheckResponse> future : futures) {
            try {
                DefaultHealthCheckResponse response = future.get(1000, TimeUnit.MILLISECONDS);
                result.put(response.getName(), response);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                LOG.error(e.getMessage());
            }
        }

        return result;
    }

    public List<String> getIgnoreList() {
        LeanConfigurationService configService = services.getService(LeanConfigurationService.class);
        String ignoreProperty = configService.getProperty(HealthCheckProperty.ignore);
        List<String> ignoreList = Collections.emptyList();
        if (Strings.isNotEmpty(ignoreProperty)) {
            ignoreList = Arrays.asList(Strings.splitByComma(ignoreProperty));
        }
        return ignoreList;
    }

    private Map<String, DefaultHealthCheckTask> applySkipBlacklist() {
        Map<String, DefaultHealthCheckTask> result = new ConcurrentHashMap<>();
        LeanConfigurationService configService = services.getService(LeanConfigurationService.class);
        Map<String, DefaultHealthCheck> healthChecks = registry.getAll();
        String skipProperty = configService.getProperty(HealthCheckProperty.skip);
        List<String> skipList = Collections.emptyList();
        if (Strings.isNotEmpty(skipProperty)) {
            skipList = Arrays.asList(Strings.splitByComma(skipProperty));
        }

        if (null != healthChecks && healthChecks.size() > 0) {
            for (String healthCheckName : healthChecks.keySet()) {
                if (!skipList.contains(healthCheckName)) {
                    result.put(healthCheckName, new DefaultHealthCheckTask(healthChecks.get(healthCheckName), healthCheckName));
                }
            }
        }
        return result;
    }

}
