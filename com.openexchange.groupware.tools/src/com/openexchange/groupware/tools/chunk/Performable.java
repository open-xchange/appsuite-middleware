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

import com.openexchange.exception.OXException;


/**
 * A {@link Performable} defines parameters and the operation that will be used / executed by {@link ChunkPerformer}.
 * See {@link ChunkPerformerTest} for examples.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public interface Performable {

    /**
     * Will be called for every chunk.
     * With every call, <code>off</code> will be incremented by the last return value of {@link #perform(int, int)}.
     * During the first call <code>off</code> is equal to {@link #getInitialOffset()}.<br>
     * <br>
     * Example:<br>
     *  <code>
     *  List<String> subList = clistToUseChunkWise.subList(off, len);<br>
     *  // Your code using the subList here<br>
     *  return subList.size();
     *  </code>
     *
     * @param off The current offset.
     * @param len The length based on the chunk size.
     * @return The value by which <code>off</code> should be increased.
     * @throws OXException in cases of errors.
     */
    int perform(int off, int len) throws OXException;

    /**
     * @return The chunk size.
     */
    int getChunkSize();

    /**
     * @return The length.
     */
    int getLength();

    /**
     * @return The initial offset.
     */
    int getInitialOffset();

}
