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

package com.openexchange.oauth.impl.internal.hazelcast;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.openexchange.hazelcast.serialization.AbstractCustomPortable;
import com.openexchange.oauth.impl.internal.CallbackRegistryImpl;

/**
 * {@link PortableMultipleCallbackRegistryFetch}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class PortableMultipleCallbackRegistryFetch extends AbstractCustomPortable implements Callable<String> {

    private static final AtomicReference<CallbackRegistryImpl> REGISTRY_REFERENCE = new AtomicReference<CallbackRegistryImpl>();

    /**
     * Sets the registry reference
     *
     * @param registry The registry reference or <code>null</code>
     */
    public static void setCallbackRegistry(CallbackRegistryImpl registry) {
        REGISTRY_REFERENCE.set(registry);
    }

    // ---------------------------------------------------------------------------------------------------------------------

    /** The unique portable class ID of the {@link PortableMultipleCallbackRegistryFetch} */
    public static final int CLASS_ID = 701;

    private static final String FIELD_TOKENS = "tokens";

    private String[] tokens;

    /**
     * Initializes a new {@link PortableCheckForExtendedServiceCallable}.
     */
    public PortableMultipleCallbackRegistryFetch() {
        super();
    }

    /**
     * Initializes a new {@link PortableCheckForExtendedServiceCallable}.
     *
     * @param tokens The tokens
     */
    public PortableMultipleCallbackRegistryFetch(String[] tokens) {
        super();
        this.tokens= tokens;
    }

    @Override
    public String call() throws Exception {
        CallbackRegistryImpl registry = REGISTRY_REFERENCE.get();
        if (null == registry) {
            return null;
        }

        for (String token : tokens) {
            String result = registry.getLocalUrlByToken(token);
            if (null != result) {
                return result;
            }
        }
        return null;
    }

    @Override
    public int getClassId() {
        return CLASS_ID;
    }

    @Override
    public void writePortable(PortableWriter writer) throws IOException {
        writer.writeUTFArray(FIELD_TOKENS, tokens);
    }

    @Override
    public void readPortable(PortableReader reader) throws IOException {
        this.tokens = reader.readUTFArray(FIELD_TOKENS);
    }

}
