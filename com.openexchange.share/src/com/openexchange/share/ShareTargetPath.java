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
 *     Copyright (C) 2004-2015 Open-Xchange, Inc.
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

package com.openexchange.share;

import java.util.Iterator;
import java.util.List;
import com.google.common.io.BaseEncoding;
import com.openexchange.java.Charsets;
import com.openexchange.java.Strings;

/**
 * Encapsulates the path to a shared folder or item that is used to generate
 * share links for guests. All contained IDs are valid from a global perspective,
 * i.e. are physical IDs that can be addressed without impersonating a certain user.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class ShareTargetPath {

    private final int module;

    private final String folder;

    private final String item;

    /**
     * Initializes a new {@link ShareTargetPath}.
     * @param module The module ID.
     * @param folder The folder ID. Must be a full-qualified global identifier.
     * @param item The item ID or <code>null</code>, if a folder is addressed. Must be a full-qualified global identifier.
     */
    public ShareTargetPath(int module, String folder, String item) {
        super();
        this.module = module;
        this.folder = folder;
        this.item = item;
    }

    /**
     * Gets the module ID.
     *
     * @return The module
     */
    public int getModule() {
        return module;
    }

    /**
     * Gets the folder ID.
     *
     * @return The folder
     */
    public String getFolder() {
        return folder;
    }

    /**
     * Gets the item ID.
     *
     * @return The item
     */
    public String getItem() {
        return item;
    }

    /**
     * Gets whether this path denotes a folder or item.
     *
     * @return <code>true</code> for a folder
     */
    public boolean isFolder() {
        return item == null;
    }

    /**
     * Gets the path representation with a leading slash.
     * Example: /1/8/NzYxMjE.
     *
     * @return The path
     */
    public String get() {
        StringBuilder sb = new StringBuilder(64).append("/");
        String version = "1";
        sb.append(version).append('/');
        sb.append(module).append('/');
        sb.append(encodeFolder(version, folder));
        if (item != null) {
            sb.append('/').append(encodeItem(version, item));
        }
        return sb.toString();
    }

    /**
     * Parses the path of a share URL. The path must start at the version
     * segment. Input example: /1/8/NzYxMjE.
     *
     * @param path The path
     * @return A {@link ShareTargetPath} instance or <code>null</code>, if
     * the passed input is no valid path.
     */
    public static ShareTargetPath parse(String path) {
        List<String> segments = Strings.splitAndTrim(path, "/");
        Iterator<String> iterator = segments.iterator();
        while (iterator.hasNext()) {
            if (Strings.isEmpty(iterator.next())) {
                iterator.remove();
            }
        }
        return parse(segments);
    }

    /**
     * Parses a list of path segments. The first segment must be the version
     * segment and the last one the folder or item segment.
     *
     * @param segments The segments
     * @return A {@link ShareTargetPath} instance or <code>null</code>, if
     * the passed input is no valid path.
     */
    public static ShareTargetPath parse(List<String> segments) {
        try {
            Iterator<String> it = segments.iterator();
            String version = it.next();
            int module = Integer.parseInt(it.next());
            String folder = decodeFolder(version, it.next());
            String item = null;
            if (it.hasNext()) {
                item = decodeItem(version, it.next());
            }
            if (!it.hasNext()) {
                return new ShareTargetPath(module, folder, item);
            }
        } catch (Exception e) {

        }

        return null;
    }

    private static String encodeFolder(String version, String folder) {
        if ("1".equals(version)) {
            return base64(folder, true);
        }

        throw new IllegalArgumentException("Unknown encoding version: " + version);
    }

    private static String decodeFolder(String version, String folder) {
        if ("1".equals(version)) {
            return base64(folder, false);
        }

        throw new IllegalArgumentException("Unknown encoding version: " + version);
    }

    private static String encodeItem(String version, String item) {
        if ("1".equals(version)) {
            return base64(item, true);
        }

        throw new IllegalArgumentException("Unknown encoding version: " + version);
    }

    private static String decodeItem(String version, String item) {
        if ("1".equals(version)) {
            return base64(item, false);
        }

        throw new IllegalArgumentException("Unknown encoding version: " + version);
    }

    private static String base64(String input, boolean encode) {
        if (input == null) {
            return null;
        }

        if (encode) {
            return BaseEncoding.base64Url().omitPadding().encode(input.getBytes(Charsets.UTF_8));
        }

        return new String(BaseEncoding.base64Url().omitPadding().decode(input), Charsets.UTF_8);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((folder == null) ? 0 : folder.hashCode());
        result = prime * result + ((item == null) ? 0 : item.hashCode());
        result = prime * result + module;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ShareTargetPath other = (ShareTargetPath) obj;
        if (folder == null) {
            if (other.folder != null)
                return false;
        } else if (!folder.equals(other.folder))
            return false;
        if (item == null) {
            if (other.item != null)
                return false;
        } else if (!item.equals(other.item))
            return false;
        if (module != other.module)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return get();
    }

}
