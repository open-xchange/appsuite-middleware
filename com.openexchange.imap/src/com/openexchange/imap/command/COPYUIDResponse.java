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

    public COPYUIDResponse(org.slf4j.Logger logger) {
        super();
        this.logger = logger;
    }

    /**
     * Fills given <code>retval</code> with UIDs from destination folder while keeping order of source UIDs in <code>uids</code>
     *
     * @param uids The source UIDs
     * @param retval The destination UIDs to fill
     */
    public void fillResponse(long[] uids, long[] retval) {
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
            } catch (ArrayIndexOutOfBoundsException e) {
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
    private static long[] toLongArray(String uidSet) {
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
