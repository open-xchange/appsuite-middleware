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

package com.openexchange.imap.command;

import com.openexchange.java.Strings;
import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;

/**
 * {@link COPYUIDResponse}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
final class COPYUIDResponse {

    private final org.slf4j.Logger logger;

    protected String src;

    protected String dest;

    public COPYUIDResponse(final org.slf4j.Logger logger) {
        super();
        this.logger = logger;
    }

    /**
     * Fills given <code>retval</code> with UIDs from destination folder while keeping order of source UIDs in <code>uids</code>
     *
     * @param uids The source UIDs
     * @param retval The destination UIDs to fill
     */
    public void fillResponse(final long[] uids, final long[] retval) {
        final long[] srcArr = toLongArray(src);
        final long[] destArr = toLongArray(dest);
        for (int in = 0; in < srcArr.length; in++) {
            int index = 0;
            /*
             * Determine index position in given UIDs...
             */
            while ((index < uids.length) && (uids[index] != srcArr[in])) {
                index++;
            }
            /*
             * ... and apply copied UID to corresponding index position in return value
             */
            try {
                retval[index] = destArr[in];
            } catch (final ArrayIndexOutOfBoundsException e) {
                logger.error("A COPYUID's source UID could not be found in given source UIDs", e);
            }
        }
    }

    /**
     * Turns a sequence of UIDs to a corresponding array of <code>long</code>.
     *
     * <pre>
     * 7,32,44:49
     * </pre>
     *
     * would be
     *
     * <pre>
     * [7,32,44,46,47,48,49]
     * </pre>
     *
     * @param uidSet The sequence of UIDs
     * @return The corresponding array of <code>long</code>.
     */
    private static long[] toLongArray(final String uidSet) {
        final TLongList arr = new TLongArrayList(32);
        final String[] sa = Strings.splitByComma(uidSet);
        Next: for (int i = 0; i < sa.length; i++) {
            final int pos = sa[i].indexOf(':');
            if (pos == -1) {
                arr.add(Long.parseLong(sa[i]));
                continue Next;
            }
            final long endUID = Long.parseLong(sa[i].substring(pos + 1));
            for (long j = Long.parseLong(sa[i].substring(0, pos)); j <= endUID; j++) {
                arr.add(j);
            }
        }
        return arr.toArray();
    }
}
