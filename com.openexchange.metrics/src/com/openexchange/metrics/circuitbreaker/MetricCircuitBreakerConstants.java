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

package com.openexchange.metrics.circuitbreaker;


/**
 * {@link MetricCircuitBreakerConstants} - Provides some useful constants for circuit breaker metrics.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class MetricCircuitBreakerConstants {

    /**
     * Initializes a new {@link MetricCircuitBreakerConstants}.
     */
    private MetricCircuitBreakerConstants() {
        super();
    }

    /** The key for protocol dimension */
    public static final String METRICS_DIMENSION_PROTOCOL_KEY = "protocol";

    /** The key for account dimension */
    public static final String METRICS_DIMENSION_ACCOUNT_KEY = "account";

    /** The group name for circuit breaker metrics */
    public static final String METRICS_GROUP = "appsuite.circuit.breaker.";

    /** The name for circuit breaker status */
    public static final String METRICS_STATUS_NAME = "status";
    /** The description for circuit breaker status */
    public static final String METRICS_STATUS_DESC = "The current status of the circuit breaker (0: closed, 1: open, 2: half-open)";


    /** The name for circuit breaker failure threshold */
    public static final String METRICS_FAILURE_THRESHOLD_NAME = "failure.threshold";
    /** The description for circuit breaker failure threshold */
    public static final String METRICS_FAILURE_THRESHOLD_DESC = "The number of successive failures that must occur in order to open the circuit";


    /** The name for circuit breaker success threshold */
    public static final String METRICS_SUCCESS_THRESHOLD_NAME = "success.threshold";
    /** The description for circuit breaker success threshold */
    public static final String METRICS_SUCCESS_THRESHOLD_DESC = "The number of successive successful executions that must occur when in a half-open state in order to close the circuit";


    /** The name for circuit breaker delay in milliseconds */
    public static final String METRICS_DELAY_MILLIS_NAME = "delay.millis";
    /** The description for circuit breaker delay in milliseconds */
    public static final String METRICS_DELAY_MILLIS_DESC = "The number of milliseconds to wait in open state before transitioning to half-open";


    /** The name for circuit breaker trip count */
    public static final String METRICS_TRIP_COUNT_NAME = "trips";
    /** The description for circuit breaker trip count */
    public static final String METRICS_TRIP_COUNT_DESC = "The number representing how often the circuit breaker tripped";
    /** The units' name for circuit breaker trip count */
    public static final String METRICS_TRIP_COUNT_UNITS = "trips";


    /** The name for circuit breaker denials meter */
    public static final String METRICS_DENIALS_NAME = "denials";
    /** The description for circuit breaker denials meter */
    public static final String METRICS_DENIALS_DESC = "The occurrences when an access attempt has been denied because circuit breaker is currently open and thus not allowing executions to occur";
    /** The units' name for circuit breaker denials meter */
    public static final String METRICS_DENIALS_UNITS = "denials";

}
