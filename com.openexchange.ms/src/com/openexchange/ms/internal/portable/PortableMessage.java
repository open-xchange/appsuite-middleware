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

package com.openexchange.ms.internal.portable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.openexchange.hazelcast.serialization.AbstractCustomPortable;

/**
 * {@link PortableMessage}
 *
 * @author <a href="mailto:tobias.friedrihc@open-xchange.com">Tobias Friedrich</a>
 */
public class PortableMessage<P extends Portable> extends AbstractCustomPortable {

    /** The unique portable class ID of the {@link PortableMessage} */
    public static final int CLASS_ID = 3;

    private List<P> messagePayload;
    private String senderID;

    /**
     * Initializes a new {@link PortableMessage}.
     *
     * @param senderID The identifier of the sender
     * @param messagePayload The message payload to carry
     */
    public PortableMessage(String senderID, List<P> messagePayload) {
        super();
        this.senderID = senderID;
        this.messagePayload = messagePayload;
    }

    /**
     * Initializes a new {@link PortableMessage}.
     *
     * @param senderID The identifier of the sender
     * @param messagePayload The message payload to carry
     */
    public PortableMessage(String senderID, P messagePayload) {
        this(senderID, Collections.singletonList(messagePayload));
    }

    /**
     * Initializes a new, empty {@link PortableMessage}.
     */
    public PortableMessage() {
        super();
    }

    @Override
    public int getClassId() {
        return CLASS_ID;
    }

    @Override
    public void writePortable(PortableWriter writer) throws IOException {
        ObjectDataOutput out = writer.getRawDataOutput();
        out.writeUTF(senderID);
        boolean hasPayload = null != messagePayload;
        out.writeBoolean(hasPayload);
        if (hasPayload) {
            out.writeInt(messagePayload.size());
            for (int i = 0; i < messagePayload.size(); i++) {
                out.writeObject(messagePayload.get(i));
            }
        }
    }

    @Override
    public void readPortable(PortableReader reader) throws IOException {
        ObjectDataInput in = reader.getRawDataInput();
        senderID = in.readUTF();
        boolean hasPayload = in.readBoolean();
        if (hasPayload) {
            int size = in.readInt();
            messagePayload = new ArrayList<P>(size);
            for (int i = 0; i < size; i++) {
                messagePayload.add(in.<P>readObject());
            }
        }
    }

    /**
     * Gets the payload of the message.
     *
     * @return The message payload
     */
    public List<P> getMessagePayload() {
        return messagePayload;
    }

    /**
     * Gets the sender ID.
     *
     * @return The sender ID
     */
    public String getSenderID() {
        return senderID;
    }

}
