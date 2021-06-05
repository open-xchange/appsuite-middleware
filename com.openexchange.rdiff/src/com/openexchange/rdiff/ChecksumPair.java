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
 * {@link ChecksumPair} - A pair of weak and strong checksums for use with the Rsync algorithm.
 * <p>
 * The weak "rolling" checksum is typically a 32-bit sum derived from the Adler32 algorithm; the strong checksum is usually a 128-bit MD4
 * checksum.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface ChecksumPair {

    /**
     * Get the weak checksum.
     *
     * @return The weak checksum
     */
    public int getWeak();

    /**
     * Get the strong checksum.
     *
     * @return The strong checksum
     */
    public byte[] getStrong();

    /**
     * Gets the offset from where this checksum pair was generated.
     *
     * @return The offset or <code>-1</code>
     */
    public long getOffset();

    /**
     * Gets the length of the data for which this checksum pair was generated.
     *
     * @return The length or <code>0</code>
     */
    public int getLength();

    /**
     * Gets the sequence number of this checksum pair, if any.
     *
     * @return The sequence number or <code>0</code>
     */
    public int getSequence();

}
