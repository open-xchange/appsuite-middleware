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

package com.openexchange.realtime.hazelcast.serialization.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import com.hazelcast.nio.serialization.ClassDefinition;
import com.hazelcast.nio.serialization.ClassDefinitionBuilder;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.openexchange.exception.OXException;
import com.openexchange.hazelcast.serialization.CustomPortable;
import com.openexchange.realtime.hazelcast.serialization.packet.PortableID;
import com.openexchange.realtime.packet.ID;

/**
 * {@link PortableIDToOXExceptionMapEntry} - Makes entries from IDMap portable by serializing them as pairs.
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 * @since 7.6.2
 */
public class PortableIDToOXExceptionMapEntry implements CustomPortable {

    public static final int CLASS_ID = 21;

    private static final String FIELD_ID = "id";

    private static final String FIELD_EXCEPTION = "exception";

    private PortableID id;
    private OXException exception;

    public static ClassDefinition CLASS_DEFINITION = null;

    static {
        CLASS_DEFINITION = new ClassDefinitionBuilder(FACTORY_ID, CLASS_ID)
        .addPortableField(FIELD_ID, PortableID.CLASS_DEFINITION)
        .addByteArrayField(FIELD_EXCEPTION)
        .build();
    }

    /**
     * Initializes a new {@link PortableIDToOXExceptionMapEntry}.
     */
    public PortableIDToOXExceptionMapEntry() {
        super();
    }

    /**
     * Initializes a new {@link PortableIDToOXExceptionMapEntry}. This will automatically convert ID instances to PortableIDs if neccessary.
     *
     * @param entry The Map.Entry containing the ID -> OXException pair
     */
    public PortableIDToOXExceptionMapEntry(java.util.Map.Entry<ID, OXException> entry) {
        ID keyParam = entry.getKey();
        if (keyParam instanceof PortableID) {
            this.id = PortableID.class.cast(keyParam);
        } else {
            this.id = new PortableID(keyParam);
        }
        this.exception = entry.getValue();
    }

    @Override
    public void writePortable(PortableWriter writer) throws IOException {
        writer.writePortable(FIELD_ID, id);
        writer.writeByteArray(FIELD_EXCEPTION, getBytes(exception));
    }

    @Override
    public void readPortable(PortableReader reader) throws IOException {
        id = reader.readPortable(FIELD_ID);
        byte[] exceptionBytes = reader.readByteArray(FIELD_EXCEPTION);
        try {
            exception = getOXException(exceptionBytes);
        } catch (ClassNotFoundException cnfe) {
            throw new IOException(cnfe);
        }
    }

    @Override
    public int getFactoryId() {
        return FACTORY_ID;
    }

    @Override
    public int getClassId() {
        return CLASS_ID;
    }

    public PortableID getKey() {
        return id;
    }

    public OXException getValue() {
        return exception;
    }

    /**
     * Serialize a {@link OXException} into a byte array
     *
     * @param oxe The {@link OXException} to be serialized
     * @return The serialized {@link OXException} as byte array
     * @throws IOException If the {@link OXException} can't be serialized
     */
    private static byte[] getBytes(OXException oxe) throws IOException {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(oxe);
        return byteArrayOutputStream.toByteArray();
    }

    /**
     * Deserialize a {@link OXException} from a byte array representation. Needs access to all the classes that make up a OXException via
     * proper OSGI imports.
     *
     * @param exceptionBytes The byte array representation of the OXException
     * @return The deserialzed {@link OXException}
     * @throws IOException If reading the byte array fails
     * @throws ClassNotFoundException If the OSGI imports are too restrictive and not all classes that make up a {@link OXException}
     *         subclass are accessible
     */
    private static OXException getOXException(byte[] exceptionBytes) throws IOException, ClassNotFoundException {
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(exceptionBytes);
        final ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
        return OXException.class.cast(objectInputStream.readObject());
    }

}
