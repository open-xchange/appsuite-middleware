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
