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
