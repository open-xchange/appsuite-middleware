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

package com.openexchange.admin.rmi.exceptions;

import java.security.SecureRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * {@link AbstractAdminRmiException} - Super class for all administrative RMI exceptions.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.4
 */
public abstract class AbstractAdminRmiException extends Exception {

    private static final long serialVersionUID = 8462133304948138631L;

    private final static int EXCEPTION_ID = new SecureRandom().nextInt();
    private final static AtomicInteger COUNTER = new AtomicInteger(0);

    /**
     * Generates a unique exception identifier.
     *
     * @return The generated exception identifier
     */
    public static String generateExceptionId() {
        int count = COUNTER.incrementAndGet();
        while ((count = COUNTER.incrementAndGet()) <= 0) {
            if (COUNTER.compareAndSet(count, 1)) {
                count = 1;
                break;
            }
        }

        return new StringBuilder().append(EXCEPTION_ID).append("-").append(count).toString();
    }

    /**
     * Adds exception identifier to given message, helping to trace errors across client and server
     *
     * @param message The message that should be enhanced
     * @param exceptionId The exception identifier
     * @return The exception message plus exception identifier
     */
    public static String enhanceExceptionMessage(String message, String exceptionId) {
        return new StringBuilder(message == null ? "" : message).append("; exceptionId ").append(exceptionId).toString();
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    /** The unique exception identifier */
    protected final String exceptionId;

    /**
     * Initializes a new {@link AbstractAdminRmiException}.
     */
    protected AbstractAdminRmiException() {
        super();
        exceptionId = generateExceptionId();
    }

    /**
     * Initializes a new {@link AbstractAdminRmiException}.
     *
     * @param message The detail message
     * @param cause The root cause
     */
    protected AbstractAdminRmiException(String message, Throwable cause) {
        super(message, cause);
        exceptionId = generateExceptionId();
    }

    /**
     * Initializes a new {@link AbstractAdminRmiException}.
     *
     * @param message The detail message
     */
    protected AbstractAdminRmiException(String message) {
        super(message);
        exceptionId = generateExceptionId();
    }

    /**
     * Initializes a new {@link AbstractAdminRmiException}.
     *
     * @param cause The root cause
     */
    protected AbstractAdminRmiException(Throwable cause) {
        super(cause);
        exceptionId = generateExceptionId();
    }

    /**
     * Gets the exception identifier.
     *
     * @return The exception identifier
     */
    public String getExceptionId() {
        return exceptionId;
    }

    @Override
    public final String getMessage() {
        return enhanceExceptionMessage(super.getMessage(), exceptionId);
    }

    /**
     * Gets the base message w/o exception identifier.
     *
     * @return The base message
     */
    public String getBaseMessage() {
        return super.getMessage();
    }

}
