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

package com.openexchange.pop3.storage.mailaccount.util;

import com.openexchange.mail.dataobjects.MailFolder;

/**
 * {@link Utility} - TODO Short description of this class' purpose.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Utility {

    /**
     * Initializes a new {@link Utility}.
     */
    private Utility() {
        super();
    }

    /**
     * Prepends specified path to given virtual fullname.<br>
     * <code>
     * &quot;<b>Trash</b>&quot;&nbsp;=&gt;&nbsp;&quot;INBOX/path/to/pop3account/<b>Trash</b>&quot;
     * </code>
     *
     * @param path The path to prepend; e.g. "INBOX/My POP3 account"
     * @param separator The separator character
     * @param virtualFullname The virtual fullname; e.g. "INBOX"
     * @return The real fullname
     */
    public static String prependPath2Fullname(final String path, final char separator, final String virtualFullname) {
        if (MailFolder.DEFAULT_FOLDER_ID.equals(virtualFullname)) {
            return path;
        }
        return new StringBuilder(path.length() + virtualFullname.length() + 1).append(path).append(separator).append(virtualFullname).toString();
    }

    /**
     * Strips possibly prepended path from specified real fullname.<br>
     * <code>
     * &quot;INBOX/path/to/pop3account/<b>Trash</b>&quot;&nbsp;=&gt;&nbsp;&quot;<b>Trash</b>&quot;
     * </code>
     *
     * @param path The path to strip
     * @param realFullname The real fullname
     * @return The virtual fullname
     */
    public static String stripPathFromFullname(final String path, final String realFullname) {
        if (null == realFullname) {
            return realFullname;
        } else if (path.equals(realFullname)) {
            return MailFolder.DEFAULT_FOLDER_ID;
        } else if (!realFullname.startsWith(path)) {
            return realFullname;
        }
        return realFullname.substring(path.length() + 1);
    }

}
