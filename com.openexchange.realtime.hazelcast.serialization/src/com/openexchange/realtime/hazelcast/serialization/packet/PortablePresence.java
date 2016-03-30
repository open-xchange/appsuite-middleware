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
import org.apache.commons.lang.Validate;
import com.hazelcast.nio.serialization.ClassDefinition;
import com.hazelcast.nio.serialization.ClassDefinitionBuilder;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.openexchange.hazelcast.serialization.CustomPortable;
import com.openexchange.realtime.packet.Presence;
import com.openexchange.realtime.packet.PresenceState;

/**
 * {@link PortablePresence} - Efficient serialization of default {@link Presence} fields via {@link Portable} mechanism. Use 
 * {@link PortablePresence#getPresence()} to reconstruct the serialized Presence via the {@link Presence.Builder}.
 * 
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 * @since 7.6.1
 */
public class PortablePresence implements CustomPortable {

    public static final int CLASS_ID = 11;

    private PortableID from;
    private static final String FIELD_FROM = "from";

    private Presence.Type type;
    private static final String FIELD_TYPE = "type";

    private PresenceState state;
    private static final String FIELD_STATE = "state";

    private String message;
    private static final String FIELD_MESSAGE = "message";

    private byte priority;
    private static final String FIELD_PRIORITY = "priority";

    public static ClassDefinition CLASS_DEFINITION = null;

    static {
        CLASS_DEFINITION = new ClassDefinitionBuilder(FACTORY_ID, CLASS_ID)
        .addPortableField(FIELD_FROM, PortableID.CLASS_DEFINITION)
        .addUTFField(FIELD_TYPE)
        .addUTFField(FIELD_STATE)
        .addUTFField(FIELD_MESSAGE)
        .addByteField(FIELD_PRIORITY)
        .build();
    }

    public PortablePresence() {
        super();
    }

    public PortablePresence(Presence presence) {
        Validate.notNull(presence, "Mandatory argument missing: presence");
        this.from = new PortableID(presence.getFrom());
        this.type = presence.getType();
        this.state = presence.getState();
        this.message = presence.getMessage();
        this.priority = presence.getPriority();
    }

    @Override
    public void writePortable(PortableWriter writer) throws IOException {
        writer.writePortable(FIELD_FROM, from);
        writer.writeUTF(FIELD_TYPE, type.name());
        writer.writeUTF(FIELD_STATE, state.name());
        writer.writeUTF(FIELD_MESSAGE, message);
        writer.writeByte(FIELD_PRIORITY, priority);
    }

    @Override
    public void readPortable(PortableReader reader) throws IOException {
        from = reader.readPortable(FIELD_FROM);
        type = getType(reader.readUTF(FIELD_TYPE));
        state = getState(reader.readUTF(FIELD_STATE));
        message = reader.readUTF(FIELD_MESSAGE);
        priority = reader.readByte(FIELD_PRIORITY);
    }

    @Override
    public int getFactoryId() {
        return FACTORY_ID;
    }

    @Override
    public int getClassId() {
        return CLASS_ID;
    }

    /**
     * Gets the from
     *
     * @return The from
     */
    public PortableID getFrom() {
        return from;
    }

    /**
     * Sets the from
     *
     * @param from The from to set
     */
    public void setFrom(PortableID from) {
        this.from = from;
    }

    /**
     * Gets the type
     *
     * @return The type
     */
    public Presence.Type getType() {
        return type;
    }

    /**
     * Sets the type
     *
     * @param type The type to set
     */
    public void setType(Presence.Type type) {
        this.type = type;
    }

    /**
     * Gets the state
     *
     * @return The state
     */
    public PresenceState getState() {
        return state;
    }

    /**
     * Sets the state
     *
     * @param state The state to set
     */
    public void setState(PresenceState state) {
        this.state = state;
    }

    /**
     * Gets the message
     *
     * @return The message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the message
     *
     * @param message The message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Gets the priority
     *
     * @return The priority
     */
    public byte getPriority() {
        return priority;
    }

    /**
     * Sets the priority
     *
     * @param priority The priority to set
     */
    public void setPriority(byte priority) {
        this.priority = priority;
    }

    /**
     * Build and return a {@link Presence} Stanza from the infos stored in this {@link Portable}
     * 
     * @return a {@link Presence} Stanza from the infos stored in this {@link Portable}
     */
    public Presence getPresence() {
        return Presence.builder()
            .from(from)
            .type(type)
            .state(state)
            .message(message)
            .priority(priority)
            .build();
    }

    private static Presence.Type getType(String type) {
        return Enum.valueOf(Presence.Type.class, type);
    }

    private static PresenceState getState(String state) {
        return Enum.valueOf(PresenceState.class, state);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((from == null) ? 0 : from.hashCode());
        result = prime * result + ((message == null) ? 0 : message.hashCode());
        result = prime * result + priority;
        result = prime * result + ((state == null) ? 0 : state.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof PortablePresence))
            return false;
        PortablePresence other = (PortablePresence) obj;
        if (from == null) {
            if (other.from != null)
                return false;
        } else if (!from.equals(other.from))
            return false;
        if (message == null) {
            if (other.message != null)
                return false;
        } else if (!message.equals(other.message))
            return false;
        if (priority != other.priority)
            return false;
        if (state != other.state)
            return false;
        if (type != other.type)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "PortablePresence [from=" + from + ", type=" + type + ", state=" + state + ", message=" + message + ", priority=" + priority + "]";
    }

}
