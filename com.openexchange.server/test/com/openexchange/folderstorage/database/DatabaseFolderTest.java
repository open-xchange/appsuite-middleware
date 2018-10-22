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

package com.openexchange.folderstorage.database;

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
import com.openexchange.folderstorage.database.DatabaseFolder;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.ldap.User;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.tools.session.ServerSession;

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
        PowerMockito.when(serverSession.getUserId()).thenReturn(1);
        Mockito.when(folderObject.isNonSystemVisible(1)).thenReturn(true);
        assertTrue("Folder should not be hidden because of non system visibility", folder.isHidden() == false);
    }
    
    @Test
    public void isHiddenTest_falseNonSystemVisibleForGroup() throws Exception {
        FolderObject folderObject = PowerMockito.spy(createFolder());
        folderObject.setType(FolderObject.PUBLIC);
        folderObject.setDefaultFolder(false);
        DatabaseFolder folder = PowerMockito.spy(new DatabaseFolder(folderObject));
        PowerMockito.doReturn(serverSession).when(folder, "getSession");
        PowerMockito.when(serverSession.getUserId()).thenReturn(1);
        Mockito.when(folderObject.isNonSystemVisible(1)).thenReturn(false);
        PowerMockito.when(serverSession.getUser()).thenReturn(mockedUser);
        PowerMockito.when(mockedUser.getGroups()).thenReturn(new int[]{2});
        Mockito.when(folderObject.isNonSystemVisible(2)).thenReturn(true);
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
        PowerMockito.when(serverSession.getUserId()).thenReturn(1);
        Mockito.when(folderObject.isNonSystemVisible(1)).thenReturn(false);
        PowerMockito.when(serverSession.getUser()).thenReturn(mockedUser);
        PowerMockito.when(mockedUser.getGroups()).thenReturn(new int[]{2});
        Mockito.when(folderObject.isNonSystemVisible(2)).thenReturn(true);
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
