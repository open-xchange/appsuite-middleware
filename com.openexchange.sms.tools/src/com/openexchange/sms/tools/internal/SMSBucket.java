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
public class SMSBucket extends AbstractCustomPortable {

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
