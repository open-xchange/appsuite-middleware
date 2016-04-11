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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.ajax.share.tests;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.infostore.actions.InfostoreTestManager;
import com.openexchange.ajax.share.ShareTest;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFileStorageGuestObjectPermission;
import com.openexchange.file.storage.DefaultFileStorageObjectPermission;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageObjectPermission;
import com.openexchange.file.storage.composition.FileID;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.ObjectPermission;
import com.openexchange.share.recipient.ShareRecipient;

/**
 * {@link AbstractSharedFilesTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.2
 */
public abstract class AbstractSharedFilesTest extends ShareTest {

    protected static final String SHARED_FOLDER = "10";

    protected InfostoreTestManager infoMgr;
    protected FolderObject userSourceFolder;
    protected FolderObject userDestFolder;
    protected File file;
    
    @Override
    public void setUp() throws Exception {
        super.setUp();
        userSourceFolder = insertPrivateFolder(EnumAPI.OX_NEW, FolderObject.INFOSTORE, getClient().getValues().getPrivateInfostoreFolder());

        file = insertFile(userSourceFolder.getObjectID(), randomUID());
        infoMgr = new InfostoreTestManager(getClient());
    }
    
    protected AbstractSharedFilesTest(String name) {
        super(name);
    }

    protected void addUserPermission(int userId) throws OXException, IOException, JSONException {
        List<FileStorageObjectPermission> permissions = new ArrayList<FileStorageObjectPermission>(2);
        permissions.add(new DefaultFileStorageObjectPermission(userId, false, FileStorageObjectPermission.WRITE)); //shared to internal user
        file.getObjectPermissions().addAll(permissions);
    }

    protected void addGuestPermission(ShareRecipient shareRecipient) throws OXException, IOException, JSONException {
        List<FileStorageObjectPermission> permissions = new ArrayList<FileStorageObjectPermission>(2);

        DefaultFileStorageGuestObjectPermission guestPermission = new DefaultFileStorageGuestObjectPermission();
        guestPermission.setPermissions(ObjectPermission.WRITE);
        guestPermission.setRecipient(shareRecipient);
        permissions.add(guestPermission);

        file.getObjectPermissions().addAll(permissions);
    }

    protected static String sharedFileId(String fileId) {
        FileID tmp = new FileID(fileId);
        tmp.setFolderId(SHARED_FOLDER);
        return tmp.toUniqueID();
    }
}
