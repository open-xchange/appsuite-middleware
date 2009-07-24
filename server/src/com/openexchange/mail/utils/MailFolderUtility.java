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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import com.openexchange.mail.FullnameArgument;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mailaccount.MailAccount;

/**
 * {@link MailFolderUtility} - Provides utility methods for mail folders.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailFolderUtility {

    /**
     * Initializes a new {@link MailFolderUtility}.
     */
    private MailFolderUtility() {
        super();
    }

    private static final int LEN = MailFolder.DEFAULT_FOLDER_ID.length();

    /**
     * Parses specified fullname argument to an appropriate instance of {@link FullnameArgument}.
     * <p>
     * Cuts off starting {@link MailFolder#DEFAULT_FOLDER_ID} plus the static separator character <code>'/'</code> from specified folder
     * fullname argument only if fullname argument is not <code>null</code> and is not equal to {@link MailFolder#DEFAULT_FOLDER_ID}.<br>
     * Example:
     * 
     * <pre>
     * &quot;default/INBOX&quot; -&gt; &quot;INBOX&quot;
     * </pre>
     * 
     * @param fullnameArgument The groupware's mail folder fullname
     * @return The stripped mail folder fullname argument
     */
    public static FullnameArgument prepareMailFolderParam(final String fullnameArgument) {
        if (fullnameArgument == null) {
            return null;
        }
        if (!fullnameArgument.startsWith(MailFolder.DEFAULT_FOLDER_ID)) {
            return new FullnameArgument(fullnameArgument);
        }
        final int len = fullnameArgument.length();
        final char separator = '/';
        int index = LEN;
        while (index < len && fullnameArgument.charAt(index) != separator) {
            index++;
        }
        // Parse account ID
        final int accountId;
        try {
            accountId = (index == LEN ? MailAccount.DEFAULT_ID : Integer.parseInt(fullnameArgument.substring(LEN, index)));
        } catch (final NumberFormatException e) {
            final IllegalArgumentException err = new IllegalArgumentException("Mail account is not a number: " + fullnameArgument);
            err.initCause(e);
            throw err;
        }
        if (index >= len) {
            return new FullnameArgument(accountId, MailFolder.DEFAULT_FOLDER_ID);
        }
        return new FullnameArgument(accountId, fullnameArgument.substring(index + 1));
    }

    /**
     * Checks if specified fullname argument's real fullname equals given fullname.
     * 
     * @param fullnameArgument The fullname argument
     * @param fullname The fullname to compare with
     * @return <code>true</code> if specified fullname argument's real fullname equals given fullname; otherwise <code>false</code>
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
     * Prepends {@link MailFolder#DEFAULT_FOLDER_ID} plus the static separator character <code>'/'</code> to given folder fullname. <br>
     * Example:
     * 
     * <pre>
     * &quot;INBOX&quot; -&gt; &quot;default2/INBOX&quot;
     * </pre>
     * 
     * @param accountId The account ID
     * @param fullname The folder fullname
     * @return The groupware's mail folder fullname
     */
    public static String prepareFullname(final int accountId, final String fullname) {
        if (fullname == null) {
            return null;
        }
        final int length = fullname.length();
        if (MailFolder.DEFAULT_FOLDER_ID.equals(fullname) || (0 == length)) {
            return new StringBuilder(length + 4).append(fullname).append(accountId).toString();
        }
        if (fullname.startsWith(MailFolder.DEFAULT_FOLDER_ID)) {
            return fullname;
        }
        return new StringBuilder(LEN + length + 4).append(MailFolder.DEFAULT_FOLDER_ID).append(accountId).append('/').append(fullname).toString();
    }

    /**
     * Tests if specified string is empty; either <code>null</code>, zero length, or only consists of white space characters.
     * 
     * @param str The string to test
     * @return <code>true</code> if specified string is empty; otherwise <code>false</code>.
     */
    public static boolean isEmpty(final String str) {
        if (null == str || str.length() == 0) {
            return true;
        }
        final char[] chars = str.toCharArray();
        for (final char c : chars) {
            if (!Character.isWhitespace(c)) {
                return false;
            }
        }
        return true;
    }
}
