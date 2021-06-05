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

package com.openexchange.sms.tools.internal;

import java.io.IOException;
import java.util.Arrays;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.openexchange.hazelcast.serialization.AbstractCustomPortable;

/**
 * {@link SMSBucket}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.1
 */
public class SMSBucket extends AbstractCustomPortable implements Cloneable {

    private static final int SMS_BUCKET_ID = 500;

    private long[] timestamps;
    private int counter;

    /**
     * Initializes a new {@link SMSBucket}.
     */
    public SMSBucket() {
        super();
    }

    /**
     * Initializes a new {@link SMSBucket}.
     */
    public SMSBucket(int size) {
        super();
        timestamps = new long[size];
        counter = size;
    }

    /**
     * Tries to remove a token from the bucket and retrieves the number of remaining tokens
     *
     * @param refreshInterval The refresh interval
     * @return The number of remaining tokens, or -1 if no token is available
     */
    public int removeToken(int refreshInterval) {
        int pos = refreshTokens(refreshInterval);
        if (pos == -1) {
            return -1;
        }
        timestamps[pos] = System.currentTimeMillis();
        int result = counter;
        counter--;
        return result;
    }

    @Override
    public int getClassId() {
        return SMS_BUCKET_ID;
    }

    @Override
    public void writePortable(PortableWriter writer) throws IOException {
        writer.writeInt("counter", counter);
        writer.writeLongArray("timestamps", timestamps);
    }

    @Override
    public void readPortable(PortableReader reader) throws IOException {
        counter = reader.readInt("counter");
        timestamps = reader.readLongArray("timestamps");
    }

    private int refreshTokens(int refreshInterval) {
        long currentMillis = System.currentTimeMillis();
        int result = -1;
        int tmpCounter = 0;
        for (int x = 0; x < timestamps.length; x++) {
            long value = timestamps[x];
            if (value == 0 || currentMillis - value >= refreshInterval * 60000) {
                tmpCounter++;
                if (result == -1) {
                    result = x;
                }
            }
        }
        counter = tmpCounter;
        return result;
    }

    @Override
    public int hashCode() {
        int prime = 53;
        int result = 1;
        result = prime * result + Arrays.hashCode(timestamps);
        result = prime * result + counter * prime;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SMSBucket)) {
            return false;
        }

        SMSBucket other = (SMSBucket) obj;
        if (other.counter != this.counter) {
            return false;
        }

        if (other.timestamps.length != this.timestamps.length) {
            return false;
        }

        for (int x = 0; x < this.timestamps.length; x++) {
            if (this.timestamps[x] != other.timestamps[x]) {
                return false;
            }
        }

        return true;
    }

    @Override
    public SMSBucket clone() {
        SMSBucket clone = new SMSBucket(timestamps.length);
        clone.timestamps = Arrays.copyOf(this.timestamps, timestamps.length);
        clone.counter = this.counter;
        return clone;
    }

    /**
     * Retrieves the maximum number of tokens in this bucket
     *
     * @return The size
     */
    public int getBucketSize() {
        return timestamps.length;
    }
}
