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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.imap.util;

import java.util.Locale;
import javax.mail.MessagingException;
import com.sun.mail.iap.Response;

/**
 * {@link ImapUtility} - IMAP utility class.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ImapUtility {

    /**
     * Initializes a new {@link ImapUtility}.
     */
    private ImapUtility() {
        super();
    }

    /**
     * Checks if given <code>MessagingException</code> indicates "Invalid messageset" or "Invalid uidset" error.
     * 
     * @param response The IMAP response to check
     * @return <code>true</code> if given <code>MessagingException</code> indicates "Invalid messageset" or "Invalid uidset" error; otherwise <code>false</code>
     */
    public static boolean isInvalidMessageset(final MessagingException error) {
        if (null == error) {
            return false;
        }
        final Exception exception = error.getNextException();
        if (!(exception instanceof com.sun.mail.iap.BadCommandException)) {
            return false;
        }
        return isInvalidMessageset((com.sun.mail.iap.BadCommandException) exception);
    }

    /**
     * Checks if given <code>BadCommandException</code> indicates "Invalid messageset" or "Invalid uidset" error.
     * 
     * @param response The IMAP response to check
     * @return <code>true</code> if given <code>BadCommandException</code> indicates "Invalid messageset" or "Invalid uidset" error; otherwise <code>false</code>
     */
    public static boolean isInvalidMessageset(final com.sun.mail.iap.BadCommandException error) {
        // A90 BAD Error in IMAP command FETCH: Invalid messageset
        if (null == error) {
            return false;
        }
        String sResponse = error.getMessage();
        if (null == sResponse) {
            return false;
        }
        sResponse = sResponse.toLowerCase(Locale.US);
        return sResponse.indexOf("invalid messageset") >= 0 || sResponse.indexOf("invalid uidset") >= 0;
    }

    /**
     * Checks if given response indicates "Invalid messageset" or "Invalid uidset" IMAP error.
     * 
     * @param response The IMAP response to check
     * @return <code>true</code> if given response indicates "Invalid messageset" or "Invalid uidset" IMAP error; otherwise <code>false</code>
     */
    public static boolean isInvalidMessageset(final Response response) {
        // A90 BAD Error in IMAP command FETCH: Invalid messageset
        if (null == response || !response.isBAD()) {
            return false;
        }
        String sResponse = response.toString();
        if (null == sResponse) {
            return false;
        }
        sResponse = sResponse.toLowerCase(Locale.US);
        return sResponse.indexOf("invalid messageset") >= 0 || sResponse.indexOf("invalid uidset") >= 0;
    }

}
