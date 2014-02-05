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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.ms.internal.portable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
        writer.writeUTF("s", senderID);
        if (null == messagePayload) {
            writer.writeInt("l", 0);
        } else {
            int length = messagePayload.size();
            writer.writeInt("l", length);
            if (0 < length) {
                Portable[] pa = new Portable[length];
                for (int i = 0; i < pa.length; i++) {
                    pa[i] = messagePayload.get(i);
                }
                writer.writePortableArray("p", pa);
            }
//            for (P data : messagePayload) {
//                data.writePortable(writer);
//            }
//            for (int i = 0; i < length; i++) {
//                writer.writePortable("l" + i, messagePayload.get(i));
//            }
        }
    }

    @Override
    public void readPortable(PortableReader reader) throws IOException {
        senderID = reader.readUTF("s");
        int length = reader.readInt("l");
        if (0 < length) {
            messagePayload = new ArrayList<P>(length);
            Portable[] portables = reader.readPortableArray("p");
            for (int i = 0; i < portables.length; i++) {
                messagePayload.add((P) portables[i]);
//                messagePayload.add(reader.<P>readPortable("l" + i));
            }
        }
    }

    public List<P> getMessagePayload() {
        return messagePayload;
    }

    public String getSenderID() {
        return senderID;
    }

}
