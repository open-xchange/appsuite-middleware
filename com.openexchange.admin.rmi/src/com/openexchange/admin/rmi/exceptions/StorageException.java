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
     */
    public static StorageException storageExceotionFor(RuntimeException cause) {
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
        Stack<Throwable> causeHierarchy = new Stack<Throwable>();
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
