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

import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.dataobjects.MailFolder;

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

    /**
     * Cuts off starting {@link MailFolder#DEFAULT_FOLDER_ID} plus the default separator from specified folder fullname argument only if
     * fullname argument is not <code>null</code> and is not equal to {@link MailFolder#DEFAULT_FOLDER_ID}.<br>
     * Example:
     * 
     * <pre>
     * &quot;default/INBOX&quot; -&gt; &quot;INBOX&quot;
     * </pre>
     * 
     * @param folderStringArg The groupware's mail folder fullname
     * @return The stripped mail folder fullname argument
     */
    public static String prepareMailFolderParam(final String folderStringArg) {
        if (folderStringArg == null) {
            return null;
        } else if (MailFolder.DEFAULT_FOLDER_ID.equals(folderStringArg)) {
            return folderStringArg;
        } else if (folderStringArg.startsWith(MailFolder.DEFAULT_FOLDER_ID)) {
            /*
             * Cut off prefix "default" plus separator character
             */
            return folderStringArg.substring(8);
        }
        return folderStringArg;
    }

    /**
     * Prepends {@link MailFolder#DEFAULT_FOLDER_ID} plus the default separator (obtained by {@link MailConfig#getDefaultSeparator()}) to
     * given folder fullname. <br>
     * Example:
     * 
     * <pre>
     * &quot;INBOX&quot; -&gt; &quot;default/INBOX&quot;
     * </pre>
     * 
     * @param fullname The folder fullname
     * @return The groupware's mail folder fullname
     */
    public static String prepareFullname(final String fullname) {
        if (fullname == null) {
            return null;
        }
        if (MailFolder.DEFAULT_FOLDER_ID.equals(fullname) || (fullname.length() == 0)) {
            return fullname;
        } else if (fullname.startsWith(MailFolder.DEFAULT_FOLDER_ID)) {
            return fullname;
        }
        return new StringBuilder(32).append(MailFolder.DEFAULT_FOLDER_ID).append(MailProperties.getInstance().getDefaultSeparator()).append(fullname).toString();
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
