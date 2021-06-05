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

package com.openexchange.mailfilter.internal;

import com.openexchange.metrics.micrometer.binders.CircuitBreakerMetrics;
import net.jodah.failsafe.CircuitBreaker;

/**
 * {@link CircuitBreakerInfo} . Circuit breaker information.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class CircuitBreakerInfo {

    private final CircuitBreaker circuitBreaker;
    private final CircuitBreakerMetrics metrics;

    /**
     * Initializes a new {@link CircuitBreakerInfo}.
     *
     * @param circuitBreaker The circuit breaker
     * @param metrics The metrics reference
     */
    public CircuitBreakerInfo(CircuitBreaker circuitBreaker, CircuitBreakerMetrics metrics) {
        super();
        this.circuitBreaker = circuitBreaker;
        this.metrics = metrics;
    }

    /**
     * Gets the circuit breaker
     *
     * @return The circuit breaker
     */
    public CircuitBreaker getCircuitBreaker() {
        return circuitBreaker;
    }

    /**
     * Increments the number of denials due to a closed circuit breaker. The current value
     * is reported as a monitoring metric.
     */
    public void incrementDenials() {
        metrics.getDenialsCounter().ifPresent(c -> c.increment());
    }

    /**
     * Increments the number of circuit breaker trips. The current value
     * is reported as a monitoring metric.
     *
     */
    public void incrementOpens() {
        metrics.getOpensCounter().ifPresent(c -> c.increment());
    }

}
