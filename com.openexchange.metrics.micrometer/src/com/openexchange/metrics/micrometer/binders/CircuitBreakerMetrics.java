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

package com.openexchange.metrics.micrometer.binders;

import java.util.Optional;
import com.openexchange.metrics.micrometer.Micrometer;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.MeterBinder;
import net.jodah.failsafe.CircuitBreaker;
import net.jodah.failsafe.CircuitBreaker.State;
import net.jodah.failsafe.util.Duration;
import net.jodah.failsafe.util.Ratio;


/**
 * {@link CircuitBreakerMetrics}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.10.4
 */
public class CircuitBreakerMetrics implements MeterBinder {

    private static final String BASE = "appsuite.circuitbreaker.";

    private static final String STATE_NAME = BASE+"state";
    private static final String STATE_DESCRIPTION = "The current state of the circuit. 1.0 if so, otherwise 0.0.";

    private static final String TIMEOUT_NAME = BASE+"timeout.seconds";
    private static final String TIMEOUT_DESCRIPTION = "The timeout for executions or negative if none has been configured.";

    private static final String DELAY_NAME = BASE+"delay.seconds";
    private static final String DELAY_DESCRIPTION = "The delay before allowing another execution on the circuit.";

    private static final String SUCCESS_THRESHOLD_NAME = BASE+"success.threshold.ratio";
    private static final String SUCCESS_THRESHOLD_DESCRIPTION = "The ratio of successive successful executions that must occur when in a half-open state in order to close the circuit.";

    private static final String FAILURE_THRESHOLD_NAME = BASE+"failure.threshold.ratio";
    private static final String FAILURE_THRESHOLD_DESCRIPTION = "The ratio of successive failures that must occur when in a closed state in order to open the circuit.";

    private final CircuitBreaker circuitBreaker;
    private final String name;
    private final Optional<String> targetHost;
    private Counter opensCounter;
    private Counter denialsCounter;

    /**
     * Initializes a new {@link CircuitBreakerMetrics}.
     *
     * @param circuitBreaker The circuit breaker instance to monitor
     * @param name The circuit breaker name to distinguish it from others
     * @param targetHost If this circuit breaker has one instance per host, the according unique host identifier (e.g. IP and port combination)
     */
    public CircuitBreakerMetrics(CircuitBreaker circuitBreaker, String name, Optional<String> targetHost) {
        super();
        this.circuitBreaker = circuitBreaker;
        this.name = name;
        this.targetHost = targetHost;
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        Tags commonTags = Tags.of("name", name);
        if (targetHost.isPresent()) {
            commonTags = commonTags.and("host", targetHost.get());
        } else {
            commonTags = commonTags.and("host", "all");
        }


        // @formatter:off
        for (State state : State.values()) {
            Micrometer.registerOrUpdateGauge(registry, STATE_NAME, commonTags.and("state", state.name()), STATE_DESCRIPTION, null, circuitBreaker,
                (cb) -> cb.getState() == state ? 1.0 : 0.0);
        }

        Micrometer.registerOrUpdateGauge(registry, TIMEOUT_NAME, commonTags, TIMEOUT_DESCRIPTION, null, circuitBreaker,
            (cb) -> {
                Duration timeout = cb.getTimeout();
                if (timeout == null) {
                    return -1.0;
                }
                return (double) timeout.toSeconds();
            });

        Micrometer.registerOrUpdateGauge(registry, DELAY_NAME, commonTags, DELAY_DESCRIPTION, null, circuitBreaker,
            (cb) -> cb.getDelay().toSeconds());

        Micrometer.registerOrUpdateGauge(registry, SUCCESS_THRESHOLD_NAME, commonTags, SUCCESS_THRESHOLD_DESCRIPTION, null, circuitBreaker,
            (cb) -> {
                Ratio threshold = cb.getSuccessThreshold();
                if (threshold == null) {
                    return 0.0;
                }
                return threshold.ratio;
            });

        Micrometer.registerOrUpdateGauge(registry, FAILURE_THRESHOLD_NAME, commonTags, FAILURE_THRESHOLD_DESCRIPTION, null, circuitBreaker,
            (cb) -> {
                Ratio threshold = cb.getFailureThreshold();
                if (threshold == null) {
                    return 0.0;
                }
                return threshold.ratio;
            });

        opensCounter = Counter.builder("appsuite.circuitbreaker.opens.total")
            .description("The number of times the circuit was opened.")
            .tags(commonTags)
            .register(registry);

        denialsCounter = Counter.builder("appsuite.circuitbreaker.denials.total")
            .description("The number of times an execution was denied because the circuit was open.")
            .tags(commonTags)
            .register(registry);
        // @formatter:on
    }

    public Optional<Counter> getOpensCounter() {
        return Optional.ofNullable(opensCounter);
    }

    public Optional<Counter> getDenialsCounter() {
        return Optional.ofNullable(denialsCounter);
    }

}
