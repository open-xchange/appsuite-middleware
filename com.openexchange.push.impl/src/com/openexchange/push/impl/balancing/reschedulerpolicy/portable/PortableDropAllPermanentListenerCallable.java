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

package com.openexchange.push.impl.balancing.reschedulerpolicy.portable;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.Callable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.openexchange.hazelcast.serialization.AbstractCustomPortable;
import com.openexchange.java.util.UUIDs;
import com.openexchange.push.impl.PushManagerRegistry;


/**
 * {@link PortableDropAllPermanentListenerCallable}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.2
 */
public class PortableDropAllPermanentListenerCallable extends AbstractCustomPortable implements Callable<Boolean> {

    public static final String PARAMETER_CALLER = "caller";

    private String caller;

    /**
     * Initializes a new {@link PortableDropAllPermanentListenerCallable}.
     */
    public PortableDropAllPermanentListenerCallable() {
        super();
    }

    /**
     * Initializes a new {@link PortableDropAllPermanentListenerCallable}.
     *
     * @param source The push user to drop
     */
    public PortableDropAllPermanentListenerCallable(UUID caller) {
        super();
        this.caller = UUIDs.getUnformattedString(caller);
    }

    @Override
    public Boolean call() throws Exception {
        PushManagerRegistry.getInstance().stopAllPermanentListenerForReschedule(); // No reconnect since we are going to restart them in cluster
        return Boolean.TRUE;
    }

    @Override
    public int getClassId() {
        return 109;
    }

    @Override
    public void writePortable(PortableWriter writer) throws IOException {
        writer.writeUTF(PARAMETER_CALLER, caller);
    }

    @Override
    public void readPortable(PortableReader reader) throws IOException {
        caller = reader.readUTF(PARAMETER_CALLER);
    }

}
