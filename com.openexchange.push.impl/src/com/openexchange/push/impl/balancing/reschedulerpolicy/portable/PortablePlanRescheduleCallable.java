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
import com.openexchange.push.impl.balancing.reschedulerpolicy.PermanentListenerRescheduler;


/**
 * {@link PortablePlanRescheduleCallable}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.2
 */
public class PortablePlanRescheduleCallable extends AbstractCustomPortable implements Callable<Boolean> {

    private static final String FIELD_ID = "id";

    private String id;

    /**
     * Initializes a new {@link PortablePlanRescheduleCallable}.
     */
    public PortablePlanRescheduleCallable() {
        super();
    }

    /**
     * Initializes a new {@link PortablePlanRescheduleCallable}.
     *
     * @param source The push user to drop
     */
    public PortablePlanRescheduleCallable(UUID id) {
        super();
        this.id = UUIDs.getUnformattedString(id);
    }

    @Override
    public Boolean call() throws Exception {
        PermanentListenerRescheduler rescheduler = PushManagerRegistry.getInstance().getRescheduler();
        if (null != rescheduler) {
            rescheduler.planReschedule(false, "PortablePlanRescheduleCallable called");
        }
        return Boolean.TRUE;
    }

    @Override
    public int getClassId() {
        return 105;
    }

    @Override
    public void writePortable(PortableWriter writer) throws IOException {
        writer.writeUTF(FIELD_ID, id);
    }

    @Override
    public void readPortable(PortableReader reader) throws IOException {
        this.id = reader.readUTF(FIELD_ID);
    }

}
