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
    public static final String METRICS_GROUP = "appsuite.circuit.breakers.";

    /** The name for circuit breaker status */
    public static final String METRICS_STATUS_NAME = "status";
    /** The description for circuit breaker status */
    public static final String METRICS_STATUS_DESC = "The current status of the circuit breaker";


    /** The name for circuit breaker failure threshold */
    public static final String METRICS_FAILURE_THRESHOLD_NAME = "failureThreshold";
    /** The description for circuit breaker failure threshold */
    public static final String METRICS_FAILURE_THRESHOLD_DESC = "The number of successive failures that must occur in order to open the circuit";


    /** The name for circuit breaker success threshold */
    public static final String METRICS_SUCCESS_THRESHOLD_NAME = "successThreshold";
    /** The description for circuit breaker success threshold */
    public static final String METRICS_SUCCESS_THRESHOLD_DESC = "The number of successive successful executions that must occur when in a half-open state in order to close the circuit";


    /** The name for circuit breaker delay in milliseconds */
    public static final String METRICS_DELAY_MILLIS_NAME = "delayMillis";
    /** The description for circuit breaker delay in milliseconds */
    public static final String METRICS_DELAY_MILLIS_DESC = "The number of milliseconds to wait in open state before transitioning to half-open";


    /** The name for circuit breaker trip count */
    public static final String METRICS_TRIP_COUNT_NAME = "tripCount";
    /** The description for circuit breaker trip count */
    public static final String METRICS_TRIP_COUNT_DESC = "The number representing how often the circuit breaker tripped";
    /** The units' name for circuit breaker trip count */
    public static final String METRICS_TRIP_COUNT_UNITS = "trips";


    /** The name for circuit breaker denials meter */
    public static final String METRICS_DENIALS_NAME = "denialsMeter";
    /** The description for circuit breaker denials meter */
    public static final String METRICS_DENIALS_DESC = "The occurrences when an access attempt has been denied because circuit breaker is currently open and thus not allowing executions to occur";
    /** The units' name for circuit breaker denials meter */
    public static final String METRICS_DENIALS_UNITS = "denials";

}
