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

package com.openexchange.ipcheck.countrycode.mbean;

import java.util.EnumMap;
import java.util.Map;
import com.openexchange.ipcheck.countrycode.AcceptReason;
import com.openexchange.ipcheck.countrycode.DenyReason;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;

/**
 * {@link IPCheckMetricCollector}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class IPCheckMetricCollector {

    public static final String COMPONENT_NAME = "ipcheck";

    private final Map<AcceptReason, Counter> acceptCounters;
    private final Map<DenyReason, Counter> denyCounters;

    /**
     * Initialises a new {@link IPCheckMetricCollector}.
     *
     * @param componentName
     */
    public IPCheckMetricCollector() {
        super();
        acceptCounters = new EnumMap<>(AcceptReason.class);
        for (AcceptReason reason : AcceptReason.values()) {
            acceptCounters.put(reason, getCounter("accepted", reason.name()));
        }

        denyCounters = new EnumMap<>(DenyReason.class);
        for (DenyReason reason : DenyReason.values()) {
            denyCounters.put(reason, getCounter("denied", reason.name()));
        }
    }

    public void incrementAccepted(AcceptReason reason) {
        acceptCounters.get(reason).increment();
    }

    public void incrementDenied(DenyReason reason) {
        denyCounters.get(reason).increment();
    }

    private Counter getCounter(String status, String reason) {
        return Counter.builder("appsuite.ipchanges").description("Total number of detected user session IP changes.").tags("status", status, "reason", reason).register(Metrics.globalRegistry);
    }

    public double getCount(IPCheckMetric metric) {
        switch (metric) {
            case acceptedEligibleIPChanges:
                return acceptCounters.get(AcceptReason.ELIGIBLE).count();
            case acceptedPrivateIP:
                return acceptCounters.get(AcceptReason.PRIVATE_IPV4).count();
            case acceptedWhiteListed:
                return acceptCounters.get(AcceptReason.WHITE_LISTED).count();
            case acceptedIPChanges:
                return acceptCounters.values().stream().map(c -> Double.valueOf(c.count())).reduce(Double.valueOf(0d), Double::sum).doubleValue();
            case deniedCountryChanged:
                return denyCounters.get(DenyReason.COUNTRY_CHANGE).count();
            case deniedException:
                return denyCounters.get(DenyReason.EXCEPTION).count();
            case deniedIPChanges:
                return denyCounters.values().stream().map(c -> Double.valueOf(c.count())).reduce(Double.valueOf(0d), Double::sum).doubleValue();
            case totalIPChanges:
                return Double.sum(getCount(IPCheckMetric.acceptedIPChanges), getCount(IPCheckMetric.deniedIPChanges));
            default:
                return -1d;
        }
    }
}
