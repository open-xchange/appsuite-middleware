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

package com.openexchange.groupware.upload.impl;

import java.util.HashMap;
import java.util.Map;

/**
 * {@link UploadUtility} - Utility class for uploads.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UploadUtility {

    private static final Map<Integer, String> M = new HashMap<Integer, String>(13);

    static {
        int pos = 0;
        M.put(Integer.valueOf(pos++), "");
        M.put(Integer.valueOf(pos++), "Kilo");
        M.put(Integer.valueOf(pos++), "Mega");
        M.put(Integer.valueOf(pos++), "Giga");
        M.put(Integer.valueOf(pos++), "Tera");
        M.put(Integer.valueOf(pos++), "Peta");
        M.put(Integer.valueOf(pos++), "Exa");
        M.put(Integer.valueOf(pos++), "Zetta");
        M.put(Integer.valueOf(pos++), "Yotta");
        M.put(Integer.valueOf(pos++), "Xenna");
        M.put(Integer.valueOf(pos++), "W-");
        M.put(Integer.valueOf(pos++), "Vendeka");
        M.put(Integer.valueOf(pos++), "U-");
    }

    /**
     * Initializes a new {@link UploadUtility}
     */
    private UploadUtility() {
        super();
    }

    /**
     * Converts given number of bytes to a human readable format
     * 
     * @param size The number of bytes
     * @param precision The number of digits allowed after dot
     * @param longName <code>true</code> to use unit's long name (e.g. <code>Megabytes</code>) or short name (e.g. <code>MB</code>)
     * @param realSize <code>true</code> to bytes' real size of <code>1024</code> used for detecting proper unit; otherwise
     *            <code>false</code> to narrow unit with <code>1000</code>.
     * @return The number of bytes in a human readable format
     */
    public static String getSize(final long size, final int precision, final boolean longName, final boolean realSize) {
        int pos = 0;
        double decSize = size;
        final int base = realSize ? 1024 : 1000;
        while (decSize > base) {
            decSize /= base;
            pos++;
        }
        final int num = (int) Math.pow(10, precision);
        final StringBuilder sb = new StringBuilder(8).append(((Math.round(decSize * num)) / (double) num)).append(' ');
        if (longName) {
            sb.append(getSizePrefix(pos)).append("bytes");
        } else {
            final String prefix = getSizePrefix(pos);
            sb.append(prefix.length() == 0 ? "" : String.valueOf(prefix.charAt(0))).append('B');
        }
        return sb.toString();
    }

    private static String getSizePrefix(final int pos) {
        final String prefix = M.get(Integer.valueOf(pos));
        if (prefix != null) {
            return prefix;
        }
        return "?-";
    }

}
