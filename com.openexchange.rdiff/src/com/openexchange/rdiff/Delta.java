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

package com.openexchange.rdiff;

/**
 * {@link Delta} - A Delta is, in the Rsync algorithm, one of two things: (1) a block of bytes and an offset, or (2) a pair of offsets, one
 * old and one new.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface Delta {

    /**
     * Gets the size of the block of data this class represents.
     *
     * @return The size of the block of data this class represents.
     */
    public int getBlockLength();

    /**
     * Gets the offset at which this delta should be written.
     *
     * @return The write offset.
     */
    public long getWriteOffset();

}
