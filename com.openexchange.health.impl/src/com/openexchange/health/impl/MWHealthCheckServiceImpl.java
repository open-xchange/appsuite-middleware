/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.health.impl;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.config.lean.Property;
import com.openexchange.health.MWHealthCheck;
import com.openexchange.health.MWHealthCheckProperty;
import com.openexchange.health.MWHealthCheckResponse;
import com.openexchange.health.MWHealthCheckResult;
import com.openexchange.health.MWHealthCheckService;
import com.openexchange.health.MWHealthState;
import com.openexchange.java.Collators;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.threadpool.ThreadPoolService;


/**
 * {@link MWHealthCheckServiceImpl}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.1
 */
public class MWHealthCheckServiceImpl implements MWHealthCheckService {

    private final ConcurrentMap<String, MWHealthCheck> checks;
    private final ServiceLookup services;
    private final Comparator<MWHealthCheckResponse> responseComparator;

    /**
     * Initializes a new {@link MWHealthCheckServiceImpl}.
     *
     * @param services The service look-up
     */
    public MWHealthCheckServiceImpl(ServiceLookup services) {
        super();
        this.services = services;
        this.checks = new ConcurrentHashMap<String, MWHealthCheck>(16, 0.9F, 1);
        responseComparator = new Comparator<MWHealthCheckResponse>() {

            private final Collator collator = Collators.getSecondaryInstance(Locale.US);

            @Override
            public int compare(MWHealthCheckResponse r1, MWHealthCheckResponse r2) {
                String s1 = r1.getName();
                String s2 = r2.getName();
                return collator.compare(s1, s2);
            }
        };
    }

    @Override
    public Collection<MWHealthCheck> getAllChecks() {
        return Collections.unmodifiableCollection(checks.values());
    }

    @Override
    public MWHealthCheck getCheck(String name) {
        return null == name ? null : checks.get(name);
    }

    /**
     * Adds specified health check.
     *
     * @param check The health check to add
     * @return <code>true</code> if given health check could be successfully added; otherwise <code>false</code>
     */
    public boolean addCheck(MWHealthCheck check) {
        return null == check ? false : (null == checks.putIfAbsent(check.getName(), check));
    }

    /**
     * Removes specified health check.
     *
     * @param checkName The name of the health check to remove
     * @return <code>true</code> if given health check could be successfully removed; otherwise <code>false</code>
     */
    public boolean removeCheck(String checkName) {
        return null == checkName ? false : (null != checks.remove(checkName));
    }

    @Override
    public MWHealthCheckResult check() {
        ThreadPoolService threadPoolService = services.getService(ThreadPoolService.class);
        if (null == threadPoolService) {
            throw new IllegalStateException("ThreadPoolService is unavailable");
        }

        // Obtain black-list and ignore-list
        Set<String> blacklist = getSkipBlacklist();
        Set<String> ignorelist = getIgnoreList();

        List<String> resultBlacklist = new ArrayList<>(blacklist.size());
        List<String> resultIgnorelist = new ArrayList<>(ignorelist.size());

        // Filter tasks by black-list
        List<MWHealthCheckTask> tasks = new LinkedList<>();
        int numOfTasks = 0;
        for (MWHealthCheck check : checks.values()) {
            if (!blacklist.contains(check.getName())) {
                tasks.add(new MWHealthCheckTask(check));
                numOfTasks++;
            } else {
                resultBlacklist.add(check.getName());
            }
        }
        if (numOfTasks == 0) {
            // No tasks left for execution
            return new MWHealthCheckResultImpl(MWHealthState.UP, Collections.emptyList(), Collections.emptyList(), resultBlacklist);
        }

        // Schedule tasks for concurrent execution
        Map<Future<MWHealthCheckResponse>, MWHealthCheckTask> futures = new LinkedHashMap<>(numOfTasks);
        for (MWHealthCheckTask task : tasks) {
            futures.put(threadPoolService.submit(task), task);
        }

        // Await tasks' responses to determine overall health status (and optionally ignore individual health state)
        List<MWHealthCheckResponse> responses = new ArrayList<>(numOfTasks);
        boolean overallState = true;
        for (Map.Entry<Future<MWHealthCheckResponse>, MWHealthCheckTask> futureAndTask : futures.entrySet()) {
            MWHealthCheckTask task = futureAndTask.getValue();

            MWHealthCheckResponse response = null;
            try {
                long timeout = task.getTimeout();
                response = timeout > 0 ? futureAndTask.getKey().get(timeout, TimeUnit.MILLISECONDS) : futureAndTask.getKey().get();
            } catch (InterruptedException e) {
                // Fail, but keep interrupted status
                Thread.currentThread().interrupt();
                LOG.warn("Interrupted while obtaining health check response from task {}", task.getName(), e);
                return new MWHealthCheckResultImpl(MWHealthState.DOWN, Collections.emptyList(), Collections.emptyList(), resultBlacklist);
            } catch (TimeoutException e) {
                LOG.warn("Timed out while obtaining health check response from task {}", task.getName(), e);
                response = createDownResponse(task, true, e);
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                if (null == cause) {
                    // Huh...? ExecutionException w/o a cause
                    response = createDownResponse(task, false, e);
                    LOG.error("Failed to obtain health check response from task {}", task.getName(), e);
                } else {
                    response = createDownResponse(task, false, cause);
                    LOG.error("Failed to obtain health check response from task {}", task.getName(), cause);
                }
            }

            // Adapt overall state (if task shall not be ignored) & add response
            if (!ignorelist.contains(response.getName())) {
                overallState &= MWHealthState.UP.equals(response.getState());
            } else {
                resultIgnorelist.add(response.getName());
            }
            responses.add(response);
        }

        LOG.debug("Health Status: {} (Checks: {})", (overallState ? "UP" : "DOWN"), formatChecksForDebug(responses));

        Collections.sort(responses, responseComparator);
        return new MWHealthCheckResultImpl(overallState ? MWHealthState.UP : MWHealthState.DOWN, responses, resultIgnorelist, resultBlacklist);
    }

    private Set<String> getIgnoreList() {
        return getSetForProperty(MWHealthCheckProperty.ignore);
    }

    private Set<String> getSkipBlacklist() {
        return getSetForProperty(MWHealthCheckProperty.skip);
    }

    private Set<String> getSetForProperty(Property property) {
        LeanConfigurationService configService = services.getOptionalService(LeanConfigurationService.class);
        if (null == configService) {
            throw new IllegalStateException("LeanConfigurationService is unavailable");
        }

        String value = configService.getProperty(property);
        return Strings.isNotEmpty(value) ? new LinkedHashSet<String>(Arrays.asList(Strings.splitByComma(value))) : Collections.emptySet();
    }

    private MWHealthCheckResponse createDownResponse(MWHealthCheckTask task, boolean timeout, Throwable e) {
        Map<String, Object> data;
        if (timeout) {
            data = Collections.singletonMap("error", "Timeout after " + task.getTimeout() / 1000L + " seconds");
        } else {
            String message = null == e ? "Unknown reason" : e.getMessage();
            data = Collections.singletonMap("error", null == message ? "Unknown reason" : message);
        }
        return new MWHealthCheckResponseImpl(task.getName(), data, MWHealthState.DOWN);
    }

    private String formatChecksForDebug(List<MWHealthCheckResponse> responses) {
        StringBuilder sb = new StringBuilder();
        for (MWHealthCheckResponse response : responses) {
            sb.append(response.toString()).append(", ");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

}
