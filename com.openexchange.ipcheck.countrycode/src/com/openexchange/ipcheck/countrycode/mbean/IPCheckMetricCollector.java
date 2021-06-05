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

package com.openexchange.ipcheck.countrycode.mbean;

import java.util.Collections;
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
        Map<AcceptReason, Counter> acceptCounters = new EnumMap<>(AcceptReason.class);
        for (AcceptReason reason : AcceptReason.values()) {
            acceptCounters.put(reason, getCounter("accepted", reason.name()));
        }
        this.acceptCounters = Collections.unmodifiableMap(acceptCounters);

        Map<DenyReason, Counter> denyCounters = new EnumMap<>(DenyReason.class);
        for (DenyReason reason : DenyReason.values()) {
            denyCounters.put(reason, getCounter("denied", reason.name()));
        }
        this.denyCounters = Collections.unmodifiableMap(denyCounters);
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
                return acceptCounters.values().stream().mapToDouble(c -> c.count()).sum();
            case deniedCountryChanged:
                return denyCounters.get(DenyReason.COUNTRY_CHANGE).count();
            case deniedException:
                return denyCounters.get(DenyReason.EXCEPTION).count();
            case deniedIPChanges:
                return denyCounters.values().stream().mapToDouble(c -> c.count()).sum();
            case totalIPChanges:
                return Double.sum(getCount(IPCheckMetric.acceptedIPChanges), getCount(IPCheckMetric.deniedIPChanges));
            default:
                return -1d;
        }
    }
}
