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

import java.util.Stack;

/**
 * @author d7
 *
 */
public class StorageException extends AbstractAdminRmiException {

    private static final long serialVersionUID = -7054584373955744724L;

    /**
     * Compiles a {@code StorageException} for given {@code RuntimeException} instance.
     *
     * @param cause The {@code RuntimeException} instance
     * @return The {@code StorageException} carrying given cause's message and stack trace
     * @deprecated Use {@link #storageExceptionFor(RuntimeException)}
     */
    @Deprecated
    public static StorageException storageExceotionFor(RuntimeException cause) {
        return storageExceptionFor(cause);
    }

    /**
     * Compiles a {@code StorageException} for given {@code RuntimeException} instance.
     *
     * @param cause The {@code RuntimeException} instance
     * @return The {@code StorageException} carrying given cause's message and stack trace
     */
    public static StorageException storageExceptionFor(RuntimeException cause) {
        return wrapForRMI(cause);
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    public StorageException(String message) {
        super(message);
    }

    public StorageException(Throwable cause) {
        super(cause);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a surrounding instance of <code>StorageException</code> wrapping trace from given <code>Throwable</code> instance.
     *
     * @param t The code>Throwable</code> instance to wrap
     * @return The wrapping instance of <code>StorageException</code>
     */
    public static StorageException wrapForRMI(Throwable t) {
        return wrapForRMI(null, t);
    }

    /**
     * Creates a surrounding instance of <code>StorageException</code> wrapping trace from given <code>Throwable</code> instance.
     *
     * @param message The optional message to set; if set to <code>null</code>, then <code>Throwable</code>'s message is used
     * @param t The code>Throwable</code> instance to wrap
     * @return The wrapping instance of <code>StorageException</code>
     */
    public static StorageException wrapForRMI(String message, Throwable t) {
        Stack<Throwable> causeHierarchy = new Stack<>();
        Throwable cause = t.getCause();
        while (cause != null) {
            causeHierarchy.push(cause);
            cause = cause.getCause();
        }

        Exception finalCause = null;
        while (!causeHierarchy.isEmpty()) {
            cause = causeHierarchy.pop();
            Exception transformedCause;
            if (cause.getClass().getName().startsWith("java.")) {
                transformedCause = (Exception) cause;
                transformedCause.initCause(finalCause);
            } else {
                transformedCause = new Exception(cause.getMessage(), finalCause);
                StackTraceElement[] stackTrace = cause.getStackTrace();
                if (stackTrace != null) {
                    transformedCause.setStackTrace(stackTrace);
                }
            }
            finalCause = transformedCause;
        }

        StorageException storageException = new StorageException(message == null ? t.getMessage() : message, finalCause);
        StackTraceElement[] stackTrace = t.getStackTrace();
        if (stackTrace != null && stackTrace.length > 0) {
            storageException.setStackTrace(stackTrace);
        }

        return storageException;
    }

}
