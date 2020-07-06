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
