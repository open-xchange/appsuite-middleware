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

package com.openexchange.folderstorage.database;

import static com.openexchange.java.Autoboxing.B;
import static com.openexchange.java.Autoboxing.I;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.User;

/**
 * {@link DatabaseFolderTest}
 *
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 * @since v7.10.1
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({DatabaseFolder.class})
public class DatabaseFolderTest {


    @Mock
    ServerSession serverSession;

    @Mock
    User mockedUser;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void isHiddenTest_falseSystemType() {
        FolderObject folderObject = createFolder();
        folderObject.setType(FolderObject.SYSTEM_TYPE);
        DatabaseFolder folder = new DatabaseFolder(folderObject);
        assertTrue("Folder should not be hidden because it is a System folder", folder.isHidden() == false);
    }

    @Test
    public void isHiddenTest_falsePublicDefaultType() {
        FolderObject folderObject = createFolder();
        folderObject.setType(FolderObject.PUBLIC);
        folderObject.setDefaultFolder(true);
        DatabaseFolder folder = new DatabaseFolder(folderObject);
        assertTrue("Folder should not be hidden because it is a Public-default folder", folder.isHidden() == false);
    }

    @Test
    public void isHiddenTest_trueNoSession() throws Exception {
        FolderObject folderObject = createFolder();
        folderObject.setType(FolderObject.PUBLIC);
        folderObject.setDefaultFolder(false);
        DatabaseFolder folder = PowerMockito.spy(new DatabaseFolder(folderObject));
        PowerMockito.doReturn(null).when(folder, "getSession");
        assertTrue("Folder should be hidden because the session is null", folder.isHidden());
    }

    @Test
    public void isHiddenTest_falseNonSystemVisible() throws Exception {
        FolderObject folderObject = PowerMockito.spy(createFolder());
        folderObject.setType(FolderObject.PUBLIC);
        folderObject.setDefaultFolder(false);
        DatabaseFolder folder = PowerMockito.spy(new DatabaseFolder(folderObject));
        PowerMockito.doReturn(serverSession).when(folder, "getSession");
        PowerMockito.when(I(serverSession.getUserId())).thenReturn(I(1));
        Mockito.when(B(folderObject.isNonSystemVisible(1))).thenReturn(Boolean.TRUE);
        assertTrue("Folder should not be hidden because of non system visibility", folder.isHidden() == false);
    }

    @Test
    public void isHiddenTest_falseNonSystemVisibleForGroup() throws Exception {
        FolderObject folderObject = PowerMockito.spy(createFolder());
        folderObject.setType(FolderObject.PUBLIC);
        folderObject.setDefaultFolder(false);
        DatabaseFolder folder = PowerMockito.spy(new DatabaseFolder(folderObject));
        PowerMockito.doReturn(serverSession).when(folder, "getSession");
        PowerMockito.when(I(serverSession.getUserId())).thenReturn(I(1));
        Mockito.when(B(folderObject.isNonSystemVisible(1))).thenReturn(Boolean.FALSE);
        PowerMockito.when(serverSession.getUser()).thenReturn(mockedUser);
        PowerMockito.when(mockedUser.getGroups()).thenReturn(new int[]{2});
        Mockito.when(B(folderObject.isNonSystemVisible(2))).thenReturn(Boolean.TRUE);
        assertTrue("Folder should not be hidden because of non system visibility for a user group", folder.isHidden() == false);
    }

    @Test
    public void isVisibleThroughSystemPermissions_falseNoSession() throws Exception {
        FolderObject folderObject = createFolder();
        DatabaseFolder folder = PowerMockito.spy(new DatabaseFolder(folderObject));
        PowerMockito.doReturn(null).when(folder, "getSession");
        assertTrue("Folder should not be visible because the session is null", folder.isVisibleThroughSystemPermissions() == false);
    }

    @Test
    public void isVisibleThroughSystemPermissions_true() throws Exception {
        FolderObject folderObject = PowerMockito.spy(createFolder());
        folderObject.setType(FolderObject.PUBLIC);
        folderObject.setDefaultFolder(false);
        DatabaseFolder folder = PowerMockito.spy(new DatabaseFolder(folderObject));
        PowerMockito.doReturn(serverSession).when(folder, "getSession");
        PowerMockito.when(I(serverSession.getUserId())).thenReturn(I(1));
        Mockito.when(B(folderObject.isNonSystemVisible(1))).thenReturn(Boolean.FALSE);
        PowerMockito.when(serverSession.getUser()).thenReturn(mockedUser);
        PowerMockito.when(mockedUser.getGroups()).thenReturn(new int[]{2});
        Mockito.when(B(folderObject.isNonSystemVisible(2))).thenReturn(Boolean.TRUE);
        assertTrue("Folder should be visible because of user permissions", folder.isVisibleThroughSystemPermissions());
    }

    private static FolderObject createFolder() {
        final FolderObject folder = new FolderObject();
        folder.setFolderName("DatabaseFolder");
        folder.setModule(FolderObject.INFOSTORE);
        final OCLPermission perm1 = new OCLPermission();
        perm1.setEntity(1);
        perm1.setGroupPermission(false);
        perm1.setFolderAdmin(true);
        perm1.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
        perm1.setSystem(1);
        folder.setPermissionsAsArray(new OCLPermission[] { perm1 });
        return folder;
    }
}
