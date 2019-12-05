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

package com.openexchange.session.oauth;

/**
 * {@link RefreshResult}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.10.3
 */
public class RefreshResult {

    public static enum SuccessReason {
        /**
         * Access token is not expired yet
         */
        NON_EXPIRED,
        /**
         * Access token was successfully refreshed
         */
        REFRESHED,
        /**
         * Access token was refreshed by another thread in the meantime
         */
        CONCURRENT_REFRESH;
    }

    public static enum FailReason {
        /**
         * Session contains an invalid refresh token
         */
        INVALID_REFRESH_TOKEN,
        /**
         * Lock timeout was exceeded
         */
        LOCK_TIMEOUT,
        /**
         * A temporary error occurred, retry later
         */
        TEMPORARY_ERROR,
        /**
         * A permanent error occurred, retry will not help to resolve this
         */
        PERMANENT_ERROR;
    }

    public static RefreshResult success(SuccessReason reason) {
        RefreshResult result = new RefreshResult();
        result.successReason = reason;
        return result;
    }

    public static RefreshResult fail(FailReason reason, String description) {
        return fail(reason, description, null);
    }

    public static RefreshResult fail(FailReason reason, String description, Throwable t) {
        RefreshResult result = new RefreshResult();
        result.failReason = reason;
        if (description == null) {
            result.errorDesc = "Unknown";
        } else {
            result.errorDesc = description;
        }
        result.exception = t;
        return result;
    }

    private SuccessReason successReason;
    private FailReason failReason;
    private String errorDesc;
    private Throwable exception;

    private RefreshResult() {
        super();
    }

    public boolean isSuccess() {
        return successReason != null;
    }

    public boolean isFail() {
        return failReason != null;
    }

    public SuccessReason getSuccessReason() {
        return successReason;
    }

    public FailReason getFailReason() {
        return failReason;
    }

    public String getErrorDesc() {
        return errorDesc;
    }

    public boolean hasException() {
        return exception != null;
    }

    public Throwable getException() {
        return exception;
    }

}
