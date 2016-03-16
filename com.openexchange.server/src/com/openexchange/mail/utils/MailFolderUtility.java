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

package com.openexchange.mail.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.openexchange.java.Strings;
import com.openexchange.mail.FullnameArgument;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.UnifiedInboxManagement;
import com.sun.mail.imap.protocol.BASE64MailboxDecoder;
import com.sun.mail.imap.protocol.BASE64MailboxEncoder;

/**
 * {@link MailFolderUtility} - Provides utility methods for mail folders.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailFolderUtility {

    /**
     * Virtual full name of mailbox's root folder
     *
     * @value "default"
     */
    private static final String DEFAULT_FOLDER_ID = MailFolder.DEFAULT_FOLDER_ID;

    /**
     * Initializes a new {@link MailFolderUtility}.
     */
    private MailFolderUtility() {
        super();
    }

    /**
     * Checks for an empty string.
     *
     * @param string The string
     * @return <code>true</code> if input is null or empty; else <code>false</code>
     */
    public static boolean isEmpty(final String string) {
        return Strings.isEmpty(string);
    }

    /**
     * Performs mailbox encoding.
     *
     * @param fullName The full name
     * @return The encoded full name
     */
    public static String encode(final String fullName) {
        return BASE64MailboxEncoder.encode(fullName);
    }

    /**
     * Performs mailbox decoding.
     *
     * @param encoded The encoded full name
     * @return The decoded full name
     */
    public static String decode(final String encoded) {
        return BASE64MailboxDecoder.decode(encoded);
    }

    private static final int LEN = DEFAULT_FOLDER_ID.length();

    /**
     * Parses specified full name argument to an appropriate instance of {@link FullnameArgument}.
     * <p>
     * Cuts off starting {@link MailFolder#DEFAULT_FOLDER_ID} plus the default separator from specified folder full name argument only if
     * full name argument is not <code>null</code> and is not equal to {@link MailFolder#DEFAULT_FOLDER_ID}.<br>
     * Example:
     *
     * <pre>
     * &quot;default/INBOX&quot; -&gt; &quot;INBOX&quot;
     * </pre>
     *
     * @param fullnameArgument The groupware's mail folder full name
     * @return The stripped mail folder full name argument
     */
    public static FullnameArgument prepareMailFolderParam(final String fullnameArgument) {
        if (fullnameArgument == null) {
            return null;
        }
        if (!fullnameArgument.startsWith(DEFAULT_FOLDER_ID)) {
            return new FullnameArgument(fullnameArgument);
        }
        final int len = fullnameArgument.length();
        final char separator = MailProperties.getInstance().getDefaultSeparator();
        int index = LEN;
        {
            char c = 0;
            while (index < len && Character.isDigit(c = fullnameArgument.charAt(index)) && c != separator) {
                index++;
            }
        }
        // Parse account ID
        final int accountId;
        try {
            accountId = (index == LEN ? MailAccount.DEFAULT_ID : Integer.parseInt(fullnameArgument.substring(LEN, index)));
        } catch (final NumberFormatException e) {
            final IllegalArgumentException err = new IllegalArgumentException(
                "Mail account identifier is not a number: " + fullnameArgument);
            err.initCause(e);
            throw err;
        }
        if (index >= len) {
            return new FullnameArgument(accountId, DEFAULT_FOLDER_ID);
        }
        return new FullnameArgument(accountId, fullnameArgument.substring(index + 1));
    }

    /**
     * Checks if specified full name argument's real full name equals given full name.
     *
     * @param fullnameArgument The full name argument
     * @param fullname The full name to compare with
     * @return <code>true</code> if specified full name argument's real full name equals given full name; otherwise <code>false</code>
     */
    public static boolean equalsFullname(final String fullnameArgument, final String fullname) {
        if (fullnameArgument == null) {
            if (fullname == null) {
                return true;
            }
            return false;
        }
        return prepareMailFolderParam(fullnameArgument).getFullname().equals(fullname);
    }

    /**
     * Prepends {@link MailFolder#DEFAULT_FOLDER_ID} plus the default separator (obtained by {@link MailConfig#getDefaultSeparator()}) to
     * given folder full name. <br>
     * Example:
     *
     * <pre>
     * &quot;INBOX&quot; -&gt; &quot;default2/INBOX&quot;
     * </pre>
     *
     * @param accountId The account ID
     * @param fullname The folder full name
     * @return The groupware's mail folder full name
     */
    public static String prepareFullname(final int accountId, final String fullname) {
        if (fullname == null) {
            return null;
        }
        final int length = fullname.length();
        if (DEFAULT_FOLDER_ID.equals(fullname) || (0 == length)) {
            return new StringBuilder(length + 4).append(fullname).append(accountId).toString();
        }
        if (fullname.startsWith(DEFAULT_FOLDER_ID)) {
            /*
             * Ensure given account ID is contained
             */
            final String tmpFullname = prepareMailFolderParam(fullname).getFullname();
            if (DEFAULT_FOLDER_ID.equals(tmpFullname)) {
                return new StringBuilder(length + 4).append(fullname).append(tmpFullname).toString();
            }
            return new StringBuilder(LEN + length + 4).append(DEFAULT_FOLDER_ID).append(accountId).append(
                MailProperties.getInstance().getDefaultSeparator()).append(tmpFullname).toString();
        }
        return new StringBuilder(LEN + length + 4).append(DEFAULT_FOLDER_ID).append(accountId).append(
            MailProperties.getInstance().getDefaultSeparator()).append(fullname).toString();
    }

    private static final Pattern P_FOLDER;
    static {
        final StringBuilder sb = new StringBuilder(64);
        // List of known folders
        sb.append("(?:");
        boolean first = true;
        for (final String name : UnifiedInboxManagement.KNOWN_FOLDERS) {
            if (first) {
                first = false;
            } else {
                sb.append('|');
            }
            sb.append(name);
        }
        sb.append(')');
        // Appendix: <sep> + "default" + <number> + <sep> + <rest>
        sb.append("[./]").append(DEFAULT_FOLDER_ID).append("[0-9]+[./](.+)");
        P_FOLDER = Pattern.compile(sb.toString());
    }

    /**
     * Sanitizes given folder full name.
     * <p>
     * Common problem are messed-up folder full names like:
     * <pre>
     *   "INBOX/default0/actual/folder/path"
     *     ==&gt;
     *   "actual/folder/path"
     * </pre>
     *
     * @param fullName The full name
     * @return The possibly sanitized full name
     */
    public static String sanitizeFullName(final String fullName) {
        if (null == fullName || fullName.indexOf(DEFAULT_FOLDER_ID) < 0) {
            return fullName;
        }
        final Matcher m = P_FOLDER.matcher(fullName);
        return m.matches() ? m.group(1) : fullName;
    }

}
