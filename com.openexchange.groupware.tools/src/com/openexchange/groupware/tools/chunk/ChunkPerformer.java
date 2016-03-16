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

package com.openexchange.groupware.tools.chunk;

import java.util.Arrays;
import java.util.List;
import com.openexchange.exception.OXException;


/**
 * A {@link ChunkPerformer} helps you to perform some work chunk-wise.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class ChunkPerformer {

    /**
     * Performs an operation in chunks. This method might be helpful when dealing with custom data structures or
     * uncommon situations where chunking is needed.
     *
     * @param performable The {@link Performable}.
     * @throws OXException The rethrown {@link OXException} thrown by {@link Performable#perform(int, int)}.
     * Note that the whole operation fails, if one chunk throws an {@link OXException}.
     */
    public static void perform(Performable performable) throws OXException {
        int sum = performable.getLength();
        int off = performable.getInitialOffset();
        int chunkSize = performable.getChunkSize();
        int len = chunkSize == 0 ? sum : chunkSize;
        do {
            if ((off + len) > sum) {
                len = sum - off;
            }

            off += performable.perform(off, (len + off));
        } while (off < sum);
    }

    /**
     * Splits a given {@link List} into chunks and executes the performable for every chunk.
     * The chunks are sublists of the original list, based on the given offset and chunk size.
     *
     * @param list The original list.
     * @param initialOffset The initial offset that will be used to split the original list into chunks.
     * @param chunkSize The chunk size.
     * @param performable The {@link ListPerformable}.
     * @throws OXException The rethrown {@link OXException} thrown by {@link ListPerformable#perform(List)}.
     * Note that the whole operation fails, if one chunk throws an {@link OXException}.
     */
    public static <T> void perform(final List<T> list, final int initialOffset, final int chunkSize, final ListPerformable<T> performable) throws OXException {
        int off = initialOffset;
        int totalLength = list.size() - initialOffset;
        int len = chunkSize == 0 ? totalLength : chunkSize;
        do {
            if ((off + len) > totalLength) {
                len = totalLength - off;
            }

            List<T> subList = list.subList(off, (len + off));
            performable.perform(subList);
            off += subList.size();
        } while (off < totalLength);
    }

    /**
     * Splits a given array into chunks and executes the performable for every chunk.
     * The chunks are copied sub arrays of the original one, based on the given offset and chunk size.
     *
     * @param list The original array.
     * @param initialOffset The initial offset that will be used to split the original array into chunks.
     * @param chunkSize The chunk size.
     * @param performable The {@link ArrayPerformable}.
     * @throws OXException The rethrown {@link OXException} thrown by {@link ArrayPerformable#perform(Object[])}.
     * Note that the whole operation fails, if one chunk throws an {@link OXException}.
     */
    public static <T> void perform(final T[] array, final int initialOffset, final int chunkSize, final ArrayPerformable<T> performable) throws OXException {
        int off = initialOffset;
        int totalLength = array.length - initialOffset;
        int len = chunkSize == 0 ? totalLength : chunkSize;
        do {
            if ((off + len) > totalLength) {
                len = totalLength - off;
            }

            T[] subArray = Arrays.copyOfRange(array, off, (len + off));
            performable.perform(subArray);
            off += subArray.length;
        } while (off < totalLength);
    }

}
