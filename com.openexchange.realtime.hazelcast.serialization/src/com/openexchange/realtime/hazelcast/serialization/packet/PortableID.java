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

package com.openexchange.realtime.hazelcast.serialization.packet;

import java.io.IOException;
import com.hazelcast.nio.serialization.ClassDefinition;
import com.hazelcast.nio.serialization.ClassDefinitionBuilder;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.openexchange.hazelcast.serialization.CustomPortable;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.IDComponentsParser;
import com.openexchange.realtime.packet.IDComponentsParser.IDComponents;


/**
 * {@link PortableID} A {@link ID} implementation that can efficiently be serialized via Hazelcast's Portable mechanism.
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 * @since 7.6.0
 */
public class PortableID extends ID implements CustomPortable {


    private static final long serialVersionUID = -6140097121581373922L;

    public static final int CLASS_ID = 5;

    private static final String FIELD_ID = "id";

    public static ClassDefinition CLASS_DEFINITION = null;

    static {
        CLASS_DEFINITION = new ClassDefinitionBuilder(FACTORY_ID, CLASS_ID)
            .addUTFField(FIELD_ID)
            .build();
    }

    /**
     * Initializes a new {@link PortableID}.
     */
    public PortableID() {
        super();
    }

    /**
     * Initializes a new {@link PortableID} based on an existing non portable ID.
     * @param id The non portable ID to use as base for this PortableID.
     */
    public PortableID(ID id) {
        //call id.toString() instead of id.getCachedStringRepresentation to make sure we cache in the original and this instance
        super(id.getProtocol(), id.getComponent(), id.getUser(), id.getContext(), id.getResource(), id.toString());
    }

    /**
     * Initializes a new {@link PortableID}.
     * @param id
     */
    public PortableID(String id) {
        super(id);
    }

    /**
     * Initializes a new {@link PortableID}.
     * @param id
     * @param defaultContext
     */
    public PortableID(String id, String defaultContext) {
        super(id, defaultContext);
    }

    /**
     * Initializes a new {@link PortableID}.
     * @param protocol
     * @param user
     * @param context
     * @param resource
     */
    public PortableID(String protocol, String user, String context, String resource) {
        super(protocol, user, context, resource);
    }

    /**
     * Initializes a new {@link PortableID}.
     * @param protocol
     * @param component
     * @param user
     * @param context
     * @param resource
     */
    public PortableID(String protocol, String component, String user, String context, String resource) {
        super(protocol, component, user, context, resource);
    }

    @Override
    public PortableID toGeneralForm() {
        return new PortableID(null, component, user, context, null);
    }

    @Override
    public int getFactoryId() {
        return FACTORY_ID;
    }

    @Override
    public int getClassId() {
        return CLASS_ID;
    }

    @Override
    public void writePortable(PortableWriter writer) throws IOException {
        writer.writeUTF(FIELD_ID, this.toString());
    }

    @Override
    public void readPortable(PortableReader reader) throws IOException {
        String idString = reader.readUTF(FIELD_ID);
        IDComponents idComponents = IDComponentsParser.parse(idString);
        protocol = idComponents.protocol;
        component = idComponents.component;
        user = idComponents.user;
        context = idComponents.context;
        resource = idComponents.resource;
        cachedStringRepresentation = idString;
        sanitize();
        validate();
    }

}
