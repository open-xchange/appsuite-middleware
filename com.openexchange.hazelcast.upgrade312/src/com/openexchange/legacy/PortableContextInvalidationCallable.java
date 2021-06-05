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

package com.openexchange.legacy;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;

/**
 * {@link PortableContextInvalidationCallable}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class PortableContextInvalidationCallable extends AbstractCustomPortable implements Callable<Boolean> {

    public static final int CLASS_ID = 26;

    public static final String PARAMETER_POOL_IDS = "poolIds";
    public static final String PARAMETER_SCHEMAS = "schemas";

    private int[] poolIds;
    private String[] schemas;

    /**
     * Initializes a new {@link PortableContextInvalidationCallable}.
     */
    public PortableContextInvalidationCallable() {
        super();
    }

    /**
     * Initializes a new {@link PortableContextInvalidationCallable}.
     *
     * @param list The schemas, which shall be invalidated
     */
    public PortableContextInvalidationCallable(List<PoolAndSchema> list) {
        super();
        int size = list.size();
        poolIds = new int[size];
        schemas = new String[size];
        int i = 0;
        for (PoolAndSchema poolAndSchema : list) {
            poolIds[i] = poolAndSchema.poolId;
            schemas[i] = poolAndSchema.schema;
            i++;
        }
    }

    @Override
    public Boolean call() throws Exception {
        /*-
         * This is just a placeholder for calling the remote 'c.o.ms.internal.portable.PortableContextInvalidationCallable'
         *
         * This class is never supposed to be called!
         */
        return Boolean.TRUE;
    }

    @Override
    public int getClassId() {
        return CLASS_ID;
    }

    @Override
    public void writePortable(PortableWriter writer) throws IOException {
        writer.writeIntArray(PARAMETER_POOL_IDS, poolIds);
        writer.writeUTFArray(PARAMETER_SCHEMAS, schemas);
    }

    @Override
    public void readPortable(PortableReader reader) throws IOException {
        poolIds = reader.readIntArray(PARAMETER_POOL_IDS);
        schemas = reader.readUTFArray(PARAMETER_SCHEMAS);
    }

}
