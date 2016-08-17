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

package com.openexchange.push.dovecot.registration;

import com.openexchange.exception.OXException;

/**
 * {@link RegistrationResult}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class RegistrationResult {

    private static final RegistrationResult SUCCESS = new RegistrationResult();

    /**
     * The result for successful registration
     *
     * @return The successful registration result
     */
    public static RegistrationResult successRegistrationResult() {
        return SUCCESS;
    }

    /**
     * The result for failed registration
     *
     * @param exception The cause why registration failed
     * @param scheduleRetry <code>true</code> to re-try registration; otherwise <code>false</code>
     * @param logInfo The optional log info
     * @return The failed registration result
     */
    public static RegistrationResult failedRegistrationResult(OXException exception, boolean scheduleRetry, String logInfo) {
        return new RegistrationResult(exception, scheduleRetry, logInfo);
    }

    /**
     * The result for denied registration
     *
     * @param reason The reason in case registration is denied due to missing back-end capabilities
     * @return The denied registration result
     */
    public static RegistrationResult deniedRegistrationResult(String reason) {
        return new RegistrationResult(reason);
    }

    // ------------------------------------------------------------------------------------------------------------------------

    private final String reason;
    private final OXException exception;
    private final boolean scheduleRetry;
    private final String logInfo;

    /**
     * Initializes a new {@link RegistrationResult}.
     */
    private RegistrationResult(String reason) {
        super();
        this.reason = reason;
        exception = null;
        scheduleRetry = false;
        logInfo = null;
    }

    /**
     * Initializes a new {@link RegistrationResult}.
     */
    private RegistrationResult(OXException exception, boolean scheduleRetry, String logInfo) {
        super();
        reason = null;
        this.exception = exception;
        this.scheduleRetry = scheduleRetry;
        this.logInfo = logInfo;
    }

    /**
     * Initializes a new {@link RegistrationResult}.
     */
    private RegistrationResult() {
        super();
        reason = null;
        exception = null;
        scheduleRetry = false;
        logInfo = null;
    }

    /**
     * Checks if this result signals success.
     *
     * @return <code>true</code> on success; otherwise <code>false</code>
     */
    public boolean isSuccess() {
        return null == reason && null == exception;
    }

    /**
     * Checks if this result signals failure.
     *
     * @return <code>true</code> on failure; otherwise <code>false</code>
     */
    public boolean isFailed() {
        return null == reason && null != exception;
    }

    /**
     * Checks if this result signals denial.
     *
     * @return <code>true</code> on denial; otherwise <code>false</code>
     */
    public boolean isDenied() {
        return null != reason && null == exception;
    }

    /**
     * Gets the reason in case registration is denied due to missing back-end capabilities.
     *
     * @return The reason
     */
    public String getReason() {
        return reason;
    }

    /**
     * Gets the exception in case registration attempt failed.
     * <p>
     * Please check {@link #scheduleRetry()} whether it is supposed to re-try registration.
     *
     * @return The exception
     */
    public OXException getException() {
        return exception;
    }

    /**
     * Signals whether it is supposed to re-try registration provided that previous attempt failed;
     * meaning {@link #getException()} does not return <code>null</code>
     *
     * @return <code>true</code> to re-try registration; otherwise <code>false</code>
     */
    public boolean scheduleRetry() {
        return scheduleRetry;
    }

    /**
     * Gets the optional log info (used in case this result signals failure).
     *
     * @return The optional log info or <code>null</code>
     */
    public String getLogInfo() {
        return logInfo;
    }

}
