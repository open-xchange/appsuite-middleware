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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.hazelcast.nio.serialization.ClassDefinition;
import com.hazelcast.nio.serialization.ClassDefinitionBuilder;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.hazelcast.nio.serialization.VersionedPortable;
import com.openexchange.drive.events.DriveEvent;
import com.openexchange.drive.events.DriveContentChange;
import com.openexchange.drive.events.internal.DriveEventImpl;
import com.openexchange.hazelcast.serialization.CustomPortable;


/**
 * {@link PortableDriveEvent}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class PortableDriveEvent implements CustomPortable, VersionedPortable {

    /** The unique portable class ID of the {@link PortableDriveEvent} */
    static final int CLASS_ID = 2;

    /**
     * The class version for {@link PortableDriveEvent}.
     * <p>
     * This number should be incremented whenever fields are added;
     * see <a href="http://docs.hazelcast.org/docs/latest-development/manual/html/Serialization/Implementing_Portable_Serialization/Versioning_for_Portable_Serialization.html">here</a> for reference.
     */
    static final int CLASS_VERSION = 2;

    private static final String PARAMETER_CONTEXT_ID = "c";
    private static final String PARAMETER_PUSH_TOKEN = "p";
    private static final String PARAMETER_FOLDER_IDS = "f";
    private static final String PARAMETER_CONTENT_CHANGES = "cc";
    private static final String PARAMETER_CONTENT_CHANGES_ONLY = "cco";

    /** The class definition for PortableDriveEvent */
    static ClassDefinition CLASS_DEFINITION = new ClassDefinitionBuilder(FACTORY_ID, CLASS_ID, CLASS_VERSION)
        .addIntField(PARAMETER_CONTEXT_ID)
        .addUTFField(PARAMETER_PUSH_TOKEN)
        .addUTFArrayField(PARAMETER_FOLDER_IDS)
        .addPortableArrayField(PARAMETER_CONTENT_CHANGES, PortableFolderContentChange.CLASS_DEFINITION)
        .addBooleanField(PARAMETER_CONTENT_CHANGES_ONLY)
    .build();
    
    private Set<String> folderIDs;
    private int contextID;
    private String pushToken;
    private List<DriveContentChange> contentChanges;
    private boolean contentChangesOnly;

    /**
     * Initializes a new {@link PortableDriveEvent}.
     */
    public PortableDriveEvent() {
        super();
    }

    @Override
    public int getClassId() {
        return CLASS_ID;
    }

    @Override
    public int getClassVersion() {
        return CLASS_VERSION;
    }

    @Override
    public void writePortable(PortableWriter writer) throws IOException {
        writer.writeInt(PARAMETER_CONTEXT_ID, contextID);
        writer.writeUTF(PARAMETER_PUSH_TOKEN, pushToken);
        String[] value = null != folderIDs ? folderIDs.toArray(new String[folderIDs.size()]) : null;
        writer.writeUTFArray(PARAMETER_FOLDER_IDS, value);
        writer.writePortableArray(PARAMETER_CONTENT_CHANGES, PortableFolderContentChange.wrap(contentChanges));
        writer.writeBoolean(PARAMETER_CONTENT_CHANGES_ONLY, contentChangesOnly);
    }

    @Override
    public void readPortable(PortableReader reader) throws IOException {
        contextID = reader.readInt(PARAMETER_CONTEXT_ID);
        pushToken = reader.readUTF(PARAMETER_PUSH_TOKEN);
        String[] value = reader.readUTFArray(PARAMETER_FOLDER_IDS);
        folderIDs = null != value ? new HashSet<String>(Arrays.asList(value)) : null;
        contentChanges = PortableFolderContentChange.unwrap(reader.readPortableArray(PARAMETER_CONTENT_CHANGES));
        contentChangesOnly = reader.readBoolean(PARAMETER_CONTENT_CHANGES_ONLY);
    }

    public static PortableDriveEvent wrap(DriveEvent driveEvent) {
        if (null == driveEvent) {
            return null;
        }
        PortableDriveEvent portableEvent = new PortableDriveEvent();
        portableEvent.contextID = driveEvent.getContextID();
        portableEvent.pushToken = driveEvent.getPushTokenReference();
        portableEvent.folderIDs = driveEvent.getFolderIDs();
        portableEvent.contentChanges = driveEvent.getContentChanges();
        portableEvent.contentChangesOnly = driveEvent.isContentChangesOnly();
        return portableEvent;
    }

    public static DriveEvent unwrap(PortableDriveEvent portableEvent) {
        return new DriveEventImpl(portableEvent.contextID, portableEvent.folderIDs, portableEvent.contentChanges, portableEvent.contentChangesOnly, true, portableEvent.pushToken);
    }

}
