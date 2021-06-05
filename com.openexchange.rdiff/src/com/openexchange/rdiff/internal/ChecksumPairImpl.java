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

package com.openexchange.rdiff.internal;

import org.metastatic.rsync.ChecksumPair;


/**
 * {@link ChecksumPairImpl} - The checksum pair implementation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ChecksumPairImpl implements com.openexchange.rdiff.ChecksumPair {

    private final ChecksumPair checksumPair;

    /**
     * Initializes a new {@link ChecksumPairImpl}.
     */
    public ChecksumPairImpl(final ChecksumPair checksumPair) {
        super();
        this.checksumPair = checksumPair;
    }

    @Override
    public int getWeak() {
        return checksumPair.getWeak();
    }

    @Override
    public byte[] getStrong() {
        return checksumPair.getStrong();
    }

    @Override
    public long getOffset() {
        return checksumPair.getOffset();
    }

    @Override
    public int getLength() {
        return checksumPair.getLength();
    }

    @Override
    public int getSequence() {
        return checksumPair.getSequence();
    }

    @Override
    public int hashCode() {
        return checksumPair.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ChecksumPairImpl)) {
            return false;
        }
        final ChecksumPairImpl other = (ChecksumPairImpl) obj;
        if (null == checksumPair) {
            if (null != other.checksumPair) {
                return false;
            }
        } else if (!checksumPair.equals(other.checksumPair)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return checksumPair.toString();
    }
}
