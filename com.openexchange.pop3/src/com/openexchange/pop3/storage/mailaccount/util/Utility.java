/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
     * Prepends specified path to given virtual full name.<br>
     * <code>
     * &quot;<b>Trash</b>&quot;&nbsp;=&gt;&nbsp;&quot;INBOX/path/to/pop3account/<b>Trash</b>&quot;
     * </code>
     *
     * @param path The path to prepend; e.g. "INBOX/My POP3 account"
     * @param separator The separator character
     * @param virtualFullname The virtual full name; e.g. "INBOX"
     * @return The real full name
     */
    public static String prependPath2Fullname(final String path, final char separator, final String virtualFullname) {
        if (MailFolder.ROOT_FOLDER_ID.equals(virtualFullname)) {
            return path;
        }
        return new StringBuilder(path.length() + virtualFullname.length() + 1).append(path).append(separator).append(virtualFullname).toString();
    }

    /**
     * Strips possibly prepended path from specified real full name.<br>
     * <code>
     * &quot;INBOX/path/to/pop3account/<b>Trash</b>&quot;&nbsp;=&gt;&nbsp;&quot;<b>Trash</b>&quot;
     * </code>
     *
     * @param path The path to strip
     * @param realFullname The real full name
     * @return The virtual full name
     */
    public static String stripPathFromFullname(final String path, final String realFullname) {
        if (null == realFullname) {
            return realFullname;
        } else if (path.equals(realFullname)) {
            return MailFolder.ROOT_FOLDER_ID;
        } else if (!realFullname.startsWith(path)) {
            return realFullname;
        }
        return realFullname.substring(path.length() + 1);
    }

}
