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

package com.openexchange.imap.util;

import java.util.Locale;
import javax.mail.MessagingException;
import javax.mail.Store;
import com.sun.mail.iap.Argument;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.IMAPFolder;

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
     * Appends command information to given information string.
     *
     * @param info The information
     * @param imapFolder The optional IMAP folder
     * @return The command with optional information appended
     */
    public static String appendCommandInfo(final String info, final IMAPFolder imapFolder) {
        if (null == imapFolder) {
            return info;
        }
        final StringBuilder sb = new StringBuilder(info);
        sb.append(" (folder=\"").append(imapFolder.getFullName()).append('"');
        final Store store = imapFolder.getStore();
        if (null != store) {
            sb.append(", store=\"").append(store.toString()).append('"');
        }
        sb.append(')');
        return sb.toString();
    }

    /**
     * Appends command information to given information string.
     *
     * @param info The information
     * @param fullName The optional full name of associated folder
     * @param store The optional description of connected IMAP store
     * @return The command with optional information appended
     */
    public static String appendCommandInfo(final String info, final String fullName, final String store) {
        final StringBuilder sb = new StringBuilder(info);
        boolean parenthesis = true;
        if (!com.openexchange.java.Strings.isEmpty(fullName)) {
            sb.append(" (folder=\"").append(fullName).append('"');
            parenthesis = false;
        }
        if (!com.openexchange.java.Strings.isEmpty(store)) {
            if (parenthesis) {
                sb.append(" (");
                parenthesis = false;
            } else {
                sb.append(", ");
            }
            sb.append("store=\"").append(store).append('"');
        }
        if (!parenthesis) {
            sb.append(')');
        }
        return sb.toString();
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

    /**
     * Prepares the IMAP command for logging purpose.
     *
     * @param imapCommand The IMAP command to prepare
     * @param args The command arguments
     * @return The prepared IMAP command
     */
    public static String prepareImapCommandForLogging(String imapCommand, Argument args) {
        if (null == args) {
            return prepareImapCommandForLogging(imapCommand);
        }

        return (null == imapCommand ? null : prepareImapCommandForLogging(new StringBuilder(imapCommand).append(' ').append(args).toString()));
    }

    /**
     * Prepares the IMAP command for logging purpose.
     *
     * @param imapCommand The IMAP command to prepare
     * @return The prepared IMAP command
     */
    public static String prepareImapCommandForLogging(String imapCommand) {
        if (null == imapCommand) {
            return "NIL";
        }
        if (imapCommand.startsWith("FETCH ")) {
            int openParenthesis = imapCommand.indexOf('(', 6);
            if (openParenthesis <= 32) {
                return surroundWithSingleQuotes(imapCommand);
            }
            return new StringBuilder(imapCommand.length()).append('\'').append("FETCH ... ").append(imapCommand.substring(openParenthesis)).append('\'').toString();
        } else if (imapCommand.startsWith("UID FETCH ")) {
            int openParenthesis = imapCommand.indexOf('(', 6);
            if (openParenthesis <= 36) {
                return surroundWithSingleQuotes(imapCommand);
            }
            return new StringBuilder(imapCommand.length()).append('\'').append("UID FETCH ... ").append(imapCommand.substring(openParenthesis)).append('\'').toString();
        } else if (imapCommand.startsWith("UID EXPUNGE ")) {
            return imapCommand.length() > 32 ? "'UID EXPUNGE ...'" : surroundWithSingleQuotes(imapCommand);
        } else {
            return surroundWithSingleQuotes(imapCommand);
        }
    }

    private static String surroundWithSingleQuotes(String imapCommand) {
        return new StringBuilder(imapCommand.length() + 2).append('\'').append(imapCommand).append('\'').toString();
    }

}
