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

package com.openexchange.sessiond.impl;

import static com.openexchange.java.Autoboxing.I;
import java.util.ArrayList;
import java.util.List;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Metrics;

/**
 *
 * {@link SessionMetricHandler} - initializes metrics
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.2
 */
public final class SessionMetricHandler {

    private static final String GROUP = "appsuite.sessions.";

    private static final String COUNT_TOTAL = "total";
    private static final String COUNT_TOTAL_DESC = "The total number of sessions in local short- and long-term containers.";

    private static final String COUNT_LONG = "long.term.total";
    private static final String COUNT_LONG_DESC = "The total number of sessions in local long-term containers.";

    private static final String COUNT_SHORT = "short.term.total";
    private static final String COUNT_SHORT_DESC = "The total number of sessions in short-term containers";

    private static final String COUNT_ACTIVE = "active.total";
    private static final String COUNT_ACTIVE_DESC = "The number of active sessions, i.e. the ones within the first two short-term containers.";

    private static final String COUNT_MAX = "max";
    private static final String COUNT_MAX_DESC = "The maximum number of sessions possible on this node.";


    private static final List<Gauge> METERS = new ArrayList<>(4);

    private static final String CLIENT_DIMENSION_KEY = "client";
    private static final String CLIENT_DIMENSION_VALUE = "all";

    /**
     * Initializes the metrics
     */
    public static void init() {
        // @formatter:off
        METERS.add(Gauge.builder(GROUP+COUNT_TOTAL, () -> I(SessionHandler.getMetricTotalSessions()))
                          .description(COUNT_TOTAL_DESC)
                          .tags(CLIENT_DIMENSION_KEY, CLIENT_DIMENSION_VALUE)
                          .register(Metrics.globalRegistry));

        METERS.add(Gauge.builder(GROUP+COUNT_LONG, () -> I(SessionHandler.getMetricLongSessions()))
            .description(COUNT_LONG_DESC)
            .tags(CLIENT_DIMENSION_KEY, CLIENT_DIMENSION_VALUE)
            .register(Metrics.globalRegistry));

        METERS.add(Gauge.builder(GROUP+COUNT_SHORT, () -> I(SessionHandler.getMetricShortSessions()))
            .description(COUNT_SHORT_DESC)
            .tags(CLIENT_DIMENSION_KEY, CLIENT_DIMENSION_VALUE)
            .register(Metrics.globalRegistry));

        METERS.add(Gauge.builder(GROUP+COUNT_ACTIVE, () -> I(SessionHandler.getMetricActiveSessions()))
            .description(COUNT_ACTIVE_DESC)
            .tags(CLIENT_DIMENSION_KEY, CLIENT_DIMENSION_VALUE)
            .register(Metrics.globalRegistry));

        METERS.add(Gauge.builder(GROUP+COUNT_MAX, () -> I(SessionHandler.getMaxNumberOfSessions()))
            .description(COUNT_MAX_DESC)
            .tags(CLIENT_DIMENSION_KEY, CLIENT_DIMENSION_VALUE)
            .register(Metrics.globalRegistry));

        // @formatter:on
    }

    /**
     * Removes the metrics from the metric registry
     */
    public static void stop() {
        METERS.forEach((m) -> Metrics.globalRegistry.remove(m));
    }

    /**
     * Prevents initialization
     */
    private SessionMetricHandler() {
        super();
    }

}
