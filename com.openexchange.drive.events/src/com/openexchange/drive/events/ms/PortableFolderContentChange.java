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
import java.util.ArrayList;
import java.util.List;
import com.hazelcast.nio.serialization.ClassDefinition;
import com.hazelcast.nio.serialization.ClassDefinitionBuilder;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.hazelcast.nio.serialization.VersionedPortable;
import com.openexchange.drive.events.DriveContentChange;
import com.openexchange.drive.events.internal.DriveContentChangeImpl;
import com.openexchange.file.storage.IdAndName;
import com.openexchange.hazelcast.serialization.CustomPortable;


/**
 * {@link PortableFolderContentChange}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since 7.10.3
 */
public class PortableFolderContentChange implements CustomPortable, VersionedPortable {

    /** The unique portable class ID of the {@link PortableFolderContentChange} */
    static final int CLASS_ID = 27;

    /**
     * The class version for {@link PortableFolderContentChange}.
     * <p>
     * This number should be incremented whenever fields are added;
     * see <a href="http://docs.hazelcast.org/docs/latest-development/manual/html/Serialization/Implementing_Portable_Serialization/Versioning_for_Portable_Serialization.html">here</a> for reference.
     */
    static final int CLASS_VERSION = 1;

    private static final String PARAMETER_FOLDER_ID = "f";
    private static final String PARAMETER_PATH_TO_ROOT = "p";

    /** The class definition for PortableFolderContentChange */
    static ClassDefinition CLASS_DEFINITION = new ClassDefinitionBuilder(FACTORY_ID, CLASS_ID, CLASS_VERSION)
        .addUTFField(PARAMETER_FOLDER_ID)
        .addUTFArrayField(PARAMETER_PATH_TO_ROOT)
    .build();
    
    private String folderId;
    private List<IdAndName> pathToRoot;

    /**
     * Initializes a new {@link PortableFolderContentChange}.
     */
    public PortableFolderContentChange() {
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
        writer.writeUTF(PARAMETER_FOLDER_ID, folderId);
        writer.writeUTFArray(PARAMETER_PATH_TO_ROOT, encode(pathToRoot));
    }

    @Override
    public void readPortable(PortableReader reader) throws IOException {
        folderId = reader.readUTF(PARAMETER_FOLDER_ID);
        pathToRoot = decode(reader.readUTFArray(PARAMETER_PATH_TO_ROOT));
    }

    private static String[] encode(List<IdAndName> pathToRoot) {
        if (null == pathToRoot) {
            return null;
        }
        String[] strings = new String[pathToRoot.size() * 2];
        int i = 0;
        for (IdAndName idAndName : pathToRoot) {
            strings[i++] = idAndName.getId();
            strings[i++] = idAndName.getName();
        }
        return strings;
    }

    private static List<IdAndName> decode(String[] strings) {
        if (null == strings) {
            return null;
        }
        if (0 != strings.length % 2) {
            throw new IllegalArgumentException("uneven array size");
        }
        List<IdAndName> pathToRoot = new ArrayList<IdAndName>(strings.length / 2);
        for (int i = 0; i < strings.length; i += 2) {
            pathToRoot.add(new IdAndName(strings[i], strings[i + 1]));
        }
        return pathToRoot;
    }

    public static Portable[] wrap(List<DriveContentChange> contentChanges) {
        if (null == contentChanges) {
            return null;
        }
        Portable[] portableContentChanges = new Portable[contentChanges.size()];
        for (int i = 0; i < contentChanges.size(); i++) {
            PortableFolderContentChange portableContentChange = new PortableFolderContentChange();
            portableContentChange.folderId = contentChanges.get(i).getFolderId();
            portableContentChange.pathToRoot = contentChanges.get(i).getPathToRoot();
            portableContentChanges[i] = portableContentChange;
        }
        return portableContentChanges;
    }

    public static List<DriveContentChange> unwrap(Portable[] portableContentChanges) {
        if (null == portableContentChanges) {
            return null;
        }
        List<DriveContentChange> folderContentChanges = new ArrayList<DriveContentChange>(portableContentChanges.length);
        for (Portable portable : portableContentChanges) {
            PortableFolderContentChange portableContentChange = (PortableFolderContentChange) portable;
            folderContentChanges.add(new DriveContentChangeImpl(portableContentChange.folderId, portableContentChange.pathToRoot));
        }
        return folderContentChanges;
    }

}
