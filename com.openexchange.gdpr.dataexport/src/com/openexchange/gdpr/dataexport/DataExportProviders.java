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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.gdpr.dataexport;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import com.google.common.collect.ImmutableSet;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.java.IOs;

/**
 * {@link DataExportProviders} - Utility class offering common helper methods.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class DataExportProviders {

    /**
     * Initializes a new {@link DataExportProviders}.
     */
    private DataExportProviders() {
        super();
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Gets the given boolean option from given module.
     *
     * @param propName The name of the boolean property
     * @param def The default value
     * @param module The module
     * @return The boolean value
     */
    public static boolean getBoolOption(String propName, boolean def, Module module) {
        Optional<Map<String, Object>> optionalProps = module.getProperties();
        if (!optionalProps.isPresent()) {
            return def;
        }

        Boolean b = (Boolean) optionalProps.get().get(propName);
        return b == null ? def : b.booleanValue();
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Checks whether a retry is supposed to be performed based on a specified exception.
     */
    @FunctionalInterface
    public static interface Condition {

        /**
         * Checks whether a retry is supposed to be performed on given exception.
         *
         * @param e The exception to examine
         * @return <code>true</code> to retry; otherwise <code>false</code>
         */
        boolean retryOn(Exception e);
    }

    /** The retry condition in case a retry-able error occurs */
    public static final Condition RETRY_ON_CATEGORY_MATCH_OR_TIMEOUTCONNECT_EXCEPTION = new Condition() {

        private final ImmutableSet<Category.EnumType> categories = ImmutableSet.of(Category.EnumType.CONNECTIVITY, Category.EnumType.TRY_AGAIN);

        @Override
        public boolean retryOn(Exception e) {
            if (OXException.class.isInstance(e)) {
                Category category = ((OXException) e).getCategory();
                if (null != category && categories.contains(category.getType())) {
                    return true;
                }
            }

            return IOs.isTimeoutOrConnectException(e);
        }
    };

    /**
     * Checks if specified exception is considered as retry-able and sink-associated work item is allowed to fail.
     *
     * @param e The exception to examine
     * @param sink The sink to use
     * @return <code>true</code> if retry-able and failure allowed; otherwise <code>false</code>
     * @throws OXException If fail count cannot be incremented
     */
    public static boolean isRetryableExceptionAndMayFail(Exception e, DataExportSink sink) throws OXException {
        return RETRY_ON_CATEGORY_MATCH_OR_TIMEOUTCONNECT_EXCEPTION.retryOn(e) && sink.incrementFailCount();
    }

    /**
     * Tries to cast exception instance to given type.
     *
     * @param <E> The desired exception type
     * @param e The exception to cast
     * @param clazz The class of the desired exception type
     * @return The desired exception or <code>null</code>
     */
    public static <E extends Exception> E castElseNull(Exception e, Class<E> clazz) {
        return clazz.isInstance(e) ? (E) e : null;
    }

    private static final int DEFAULT_MAX_TRIES = 5;

    /**
     * Executes given callable instance and retries in case a retry-able error occurs.
     *
     * @param <V> The return type
     * @param <E> The desired exception type
     * @param callable The callable to execute
     * @param maxTries The number of execution attempts
     * @param errorType The desired exception type
     * @return The callable's result
     * @throws E If execution yields an exception
     * @throws IllegalArgumentException If passed arguments are invalid
     * @throws RuntimeException If an unexpected exception occurs
     */
    public static <V, E extends Exception> V executeWithRetryOnRetryableException(Callable<V> callable, Class<E> errorType) throws E {
        return executeWithRetryOnCondition(callable, DEFAULT_MAX_TRIES, RETRY_ON_CATEGORY_MATCH_OR_TIMEOUTCONNECT_EXCEPTION, errorType);
    }

    /**
     * Executes given callable instance and retries in case a retry-able error occurs.
     *
     * @param <V> The return type
     * @param <E> The desired exception type
     * @param callable The callable to execute
     * @param maxTries The number of execution attempts
     * @param errorType The desired exception type
     * @return The callable's result
     * @throws E If execution yields an exception
     * @throws IllegalArgumentException If passed arguments are invalid
     * @throws RuntimeException If an unexpected exception occurs
     */
    public static <V, E extends Exception> V executeWithRetryOnRetryableException(Callable<V> callable, int maxTries, Class<E> errorType) throws E {
        return executeWithRetryOnCondition(callable, maxTries, RETRY_ON_CATEGORY_MATCH_OR_TIMEOUTCONNECT_EXCEPTION, errorType);
    }

    /**
     * Executes given callable instance and retries in case condition is fulfilled.
     *
     * @param <V> The return type
     * @param <E> The desired exception type
     * @param callable The callable to execute
     * @param maxTries The number of execution attempts
     * @param condition The condition to check
     * @param errorType The desired exception type
     * @return The callable's result
     * @throws E If execution yields an exception
     * @throws IllegalArgumentException If passed arguments are invalid
     * @throws RuntimeException If an unexpected exception occurs
     */
    public static <V, E extends Exception> V executeWithRetryOnCondition(Callable<V> callable, int maxTries, Condition condition, Class<E> errorType) throws E {
        if (callable == null) {
            throw new IllegalArgumentException("Callable must not be null.");
        }
        if (condition == null) {
            throw new IllegalArgumentException("Condition must not be null.");
        }
        if (maxTries <= 0) {
            throw new IllegalArgumentException("maxRetries must not be less than or equal to 0 (zero).");
        }

        int count = 0;
        while (count++ < maxTries) {
            try {
                return callable.call();
            } catch (Exception e) {
                if (count < maxTries && condition.retryOn(e)) {
                    long nanosToWait = TimeUnit.NANOSECONDS.convert((count * 1000) + ((long) (Math.random() * 1000)), TimeUnit.MILLISECONDS);
                    LockSupport.parkNanos(nanosToWait);
                } else {
                    E exc = castElseNull(e, errorType);
                    if (exc != null) {
                        throw exc;
                    }
                    throw e instanceof RuntimeException ? ((RuntimeException) e) : new RuntimeException(e);
                }
            }
        }
        return null;
    }

    /**
     * Executes given runnable instance and retries in case a timeout or connect I/O error occurs.
     *
     * @param runnable The runnable to execute
     * @param maxTries The number of execution attempts
     * @throws RuntimeException If execution yields an exception
     */
    public static void executeWithRetryOnRetryableException(Runnable runnable, int maxTries) {
        executeWithRetryOnCondition(runnable, maxTries, RETRY_ON_CATEGORY_MATCH_OR_TIMEOUTCONNECT_EXCEPTION);
    }

    /**
     * Executes given runnable instance and retries in case specified condition is fulfilled.
     *
     * @param runnable The runnable to execute
     * @param maxTries The number of execution attempts
     * @throws RuntimeException If execution yields an exception
     */
    public static void executeWithRetryOnCondition(Runnable runnable, int maxTries, Condition condition) {
        if (runnable == null) {
            throw new IllegalArgumentException("Runnable must not be null.");
        }
        if (condition == null) {
            throw new IllegalArgumentException("Condition must not be null.");
        }
        if (maxTries <= 0) {
            throw new IllegalArgumentException("maxRetries must not be less than or equal to 0 (zero).");
        }

        int count = 0;
        while (count++ < maxTries) {
            try {
                runnable.run();
            } catch (Exception e) {
                if (count < maxTries && condition.retryOn(e)) {
                    long nanosToWait = TimeUnit.NANOSECONDS.convert((count * 1000) + ((long) (Math.random() * 1000)), TimeUnit.MILLISECONDS);
                    LockSupport.parkNanos(nanosToWait);
                } else {
                    throw e;
                }
            }
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Checks if given exception holds a "permission denied" category.
     *
     * @param e The exception to check
     * @return <code>true</code> if "permission denied"; otherwise <code>false</code>
     */
    public static boolean isPermissionDenied(Exception e) {
        if (e instanceof OXException) {
            OXException oxe = (OXException) e;
            Category category = oxe.getCategory();
            if (category != null && category.getType() == Category.EnumType.PERMISSION_DENIED) {
                return true;
            }
        }
        return false;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

}
