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

package com.openexchange.drive.events.ms;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.openexchange.drive.events.DriveEvent;
import com.openexchange.drive.events.internal.DriveEventImpl;
import com.openexchange.hazelcast.serialization.AbstractCustomPortable;


/**
 * {@link PortableDriveEvent}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class PortableDriveEvent extends AbstractCustomPortable {

    /** The unique portable class ID of the {@link PortableDriveEvent} */
    public static final int CLASS_ID = 2;

    private Set<String> folderIDs;
    private int contextID;
    private String pushToken;

    public PortableDriveEvent() {
        super();
    }

    @Override
    public int getClassId() {
        return CLASS_ID;
    }

    @Override
    public void writePortable(PortableWriter writer) throws IOException {
        writer.writeInt("c", contextID);
        writer.writeUTF("p", pushToken);
        if (null == folderIDs || 0 == folderIDs.size()) {
            writer.writeInt("f", 0);
        } else {
            writer.writeInt("f", folderIDs.size());
            ObjectDataOutput out = writer.getRawDataOutput();
            for (String folderID : folderIDs) {
                out.writeUTF(folderID);
            }
        }

    }

    @Override
    public void readPortable(PortableReader reader) throws IOException {
        contextID = reader.readInt("c");
        pushToken = reader.readUTF("p");
        int size = reader.readInt("f");
        folderIDs = new HashSet<String>(size);
        ObjectDataInput in = reader.getRawDataInput();
        for (int i = 0; i < size; i++) {
            folderIDs.add(in.readUTF());
        }
    }


    public static PortableDriveEvent wrap(DriveEvent driveEvent) {
        if (null == driveEvent) {
            return null;
        }
        PortableDriveEvent portableEvent = new PortableDriveEvent();
        portableEvent.contextID = driveEvent.getContextID();
        portableEvent.folderIDs = driveEvent.getFolderIDs();
        portableEvent.pushToken = driveEvent.getPushTokenReference();
        return portableEvent;
    }

    public static DriveEvent unwrap(PortableDriveEvent portableEvent) {
        return new DriveEventImpl(portableEvent.contextID, portableEvent.folderIDs, true, portableEvent.pushToken);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + contextID;
        result = prime * result + ((folderIDs == null) ? 0 : folderIDs.hashCode());
        result = prime * result + ((pushToken == null) ? 0 : pushToken.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof PortableDriveEvent)) {
            return false;
        }
        PortableDriveEvent other = (PortableDriveEvent) obj;
        if (contextID != other.contextID) {
            return false;
        }
        if (folderIDs == null) {
            if (other.folderIDs != null) {
                return false;
            }
        } else if (!folderIDs.equals(other.folderIDs)) {
            return false;
        }
        if (pushToken == null) {
            if (other.pushToken != null) {
                return false;
            }
        } else if (!pushToken.equals(other.pushToken)) {
            return false;
        }
        return true;
    }

}
