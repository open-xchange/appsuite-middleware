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
