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
import org.json.JSONObject;
import com.hazelcast.nio.serialization.ClassDefinition;
import com.hazelcast.nio.serialization.ClassDefinitionBuilder;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.openexchange.hazelcast.serialization.AbstractCustomPortable;
import com.openexchange.java.util.UUIDs;
import com.openexchange.push.impl.PushManagerRegistry;


/**
 * {@link PortableCheckForExtendedServiceCallable}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.2
 */
public class PortableCheckForExtendedServiceCallable extends AbstractCustomPortable implements Callable<String> {

    public static final String NODE_INFO_ALL_USERS_STARTED = "allStarted";

    public static final String NODE_INFO_PERMANENT_PUSH_ALLOWED = "allowed";

    /** The unique portable class ID of the {@link PortableCheckForExtendedServiceCallable} */
    public static final int CLASS_ID = 103;

    /**
     * The class version for {@link PortableCheckForExtendedServiceCallable}
     * <p>
     * This number should be incremented whenever fields are added;
     * see <a href="http://docs.hazelcast.org/docs/latest-development/manual/html/Serialization/Implementing_Portable_Serialization/Versioning_for_Portable_Serialization.html">here</a> for reference.
     */
    public static final int CLASS_VERSION = 2;

    private static final String FIELD_ID = "id";
    private static final String FIELD_VERSION = "version";

    /** The class definition for PortableSession */
    public static ClassDefinition CLASS_DEFINITION = new ClassDefinitionBuilder(FACTORY_ID, CLASS_ID, CLASS_VERSION)
        .addUTFField(FIELD_ID)
        .addUTFField(FIELD_VERSION)
        .build();

    // -------------------------------------------------------------------------------------------------------------------------------------

    private String id;
    private String version;

    /**
     * Initializes a new {@link PortableCheckForExtendedServiceCallable}.
     */
    public PortableCheckForExtendedServiceCallable() {
        super();
    }

    /**
     * Initializes a new {@link PortableCheckForExtendedServiceCallable}.
     *
     * @param id The associated UUID
     * @param version The version identifier
     */
    public PortableCheckForExtendedServiceCallable(UUID id, String version) {
        super();
        this.id = UUIDs.getUnformattedString(id);
        this.version = version;
    }

    @Override
    public String call() throws Exception {
        PushManagerRegistry registry = PushManagerRegistry.getInstance();
        boolean permanentPushAllowed = registry.isPermanentPushAllowed();
        boolean allUsersStarted = registry.wereAllUsersStarted();
        return new JSONObject(2).put(NODE_INFO_PERMANENT_PUSH_ALLOWED, permanentPushAllowed).put(NODE_INFO_ALL_USERS_STARTED, allUsersStarted).toString();
    }

    @Override
    public int getClassId() {
        return CLASS_ID;
    }

    @Override
    public void writePortable(PortableWriter writer) throws IOException {
        writer.writeUTF(FIELD_ID, id);
        writer.writeUTF(FIELD_VERSION, version);
    }

    @Override
    public void readPortable(PortableReader reader) throws IOException {
        this.id = reader.readUTF(FIELD_ID);
        this.version = reader.readUTF(FIELD_VERSION);
    }

}
