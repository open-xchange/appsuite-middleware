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

package com.openexchange.legacy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;

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
