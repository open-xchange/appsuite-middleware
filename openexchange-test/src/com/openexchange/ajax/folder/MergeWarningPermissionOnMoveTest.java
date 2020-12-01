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

package com.openexchange.ajax.folder;

import org.junit.Test;
import com.openexchange.ajax.infostore.apiclient.FileMovePermissionWarningTest.FolderType;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.MoveFolderPermissionMode;
import com.openexchange.testing.httpclient.models.FolderUpdateResponse;

/**
 * 
 * Tests the warning behavior of {@link com.openexchange.folder.json.actions.UpdateAction} with {@link MoveFolderPermissionMode} MERGE when moving a folder.
 * 
 * Tests with (expected = AssertionError.class) test if a MOVE_TO_SHARED_WARNING occurs when a unshared file/folder is moved into a folder with permissions.
 * Warnings for this case are disabled, so the test must fail.
 *
 * @author <a href="mailto:anna.ottersbach@open-xchange.com">Anna Ottersbach</a>
 * @since v7.10.5
 */
public class MergeWarningPermissionOnMoveTest extends AbstractFolderMoveWarningTest {

    public MergeWarningPermissionOnMoveTest() {
        super("merge");
    }

    /**
     * 
     * Tests whether the warning
     * {@link com.openexchange.folderstorage.FolderExceptionErrorMessage#MOVE_TO_SHARED_WARNING}
     * is returned, when a folder is moved from a public folder to a shared folder.
     *
     * @throws Exception
     */
    @Test(expected = AssertionError.class)
    public void testMoveFromPublicFolderToSharedFolder() throws Exception {
        String sourceFolder = publicFolderId;
        String destinationFolder = sharedFolderId;
        FolderExceptionErrorMessage expectedWarning = FolderExceptionErrorMessage.MOVE_TO_SHARED_WARNING;
        checkFolderMove(sourceFolder, destinationFolder, expectedWarning, FolderType.PUBLIC, FolderType.SHARED);
    }

    /**
     * 
     * Tests whether no warning is returned, when a folder is moved from a public folder to a private unshared folder.
     *
     * @throws Exception
     */
    @Test
    public void testMoveFromPublicFolderToUnsharedPrivate() throws Exception {
        String sourceFolder = publicFolderId;
        String destinationFolder = createPrivateFolder(null);
        String folderToMove = createChildFolder(sourceFolder);
        FolderUpdateResponse response = moveFolder(folderToMove, destinationFolder, null);
        checkResponse(response.getError(), response.getErrorDesc(), response.getData());
        checkParentFolder(folderToMove, destinationFolder);
    }

    /**
     * 
     * Tests whether the warning
     * {@link com.openexchange.folderstorage.FolderExceptionErrorMessage#MOVE_TO_SHARED_WARNING}
     * is returned, when a folder is moved from a shared folder to a public folder.
     *
     * @throws Exception
     */
    @Test(expected = AssertionError.class)
    public void testMoveFromSharedFolderToPublicFolder() throws Exception {
        String sourceFolder = sharedFolderId;
        String destinationFolder = publicFolderId;
        FolderExceptionErrorMessage expectedWarning = FolderExceptionErrorMessage.MOVE_TO_SHARED_WARNING;
        checkFolderMove(sourceFolder, destinationFolder, expectedWarning, FolderType.SHARED, FolderType.PUBLIC);
    }

    /**
     * 
     * Tests whether no warning is returned, when a folder is moved from a shared folder to a private unshared folder.
     *
     * @throws Exception
     */
    @Test
    public void testMoveFromSharedFolderToUnsharedPrivate() throws Exception {
        String sourceFolder = sharedFolderId;
        String destinationFolder = createPrivateFolder(null);
        String folderToMove = createChildFolder(sourceFolder);
        FolderUpdateResponse response = moveFolder(folderToMove, destinationFolder, null);
        checkResponse(response.getError(), response.getErrorDesc(), response.getData());
        checkParentFolder(folderToMove, destinationFolder);
    }

    /**
     * 
     * Tests whether the warning
     * {@link com.openexchange.folderstorage.FolderExceptionErrorMessage#MOVE_TO_SHARED_WARNING}
     * is returned, when a folder is moved from a private folder shared with user 2 to a private folder shared with user 3.
     *
     * @throws Exception
     */
    @Test(expected = AssertionError.class)
    public void testMoveFromSharedPrivateToSharedPrivate() throws Exception {
        String sourceFolder = createPrivateFolder(userId2);
        String destinationFolder = createPrivateFolder(userId3);
        FolderExceptionErrorMessage expectedWarning = FolderExceptionErrorMessage.MOVE_TO_SHARED_WARNING;
        checkFolderMove(sourceFolder, destinationFolder, expectedWarning, FolderType.PRIVATE, FolderType.PRIVATE);
    }

    /**
     * 
     * Tests whether no warning is returned,
     * when a folder is moved from a private folder shared with user 2 to a private unshared folder.
     *
     * @throws Exception
     */
    @Test
    public void testMoveFromSharedPrivateToUnsharedPrivate() throws Exception {
        String sourceFolder = createPrivateFolder(userId2);
        String destinationFolder = createPrivateFolder(null);
        String folderToMove = createChildFolder(sourceFolder);
        FolderUpdateResponse response = moveFolder(folderToMove, destinationFolder, null);
        checkResponse(response.getError(), response.getErrorDesc(), response.getData());
        checkParentFolder(folderToMove, destinationFolder);
    }

    /**
     * 
     * Tests whether the warning
     * {@link com.openexchange.folderstorage.FolderExceptionErrorMessage#MOVE_TO_SHARED_WARNING}
     * is returned, when a folder is moved from a private unshared folder to a public folder.
     *
     * @throws Exception
     */
    @Test(expected = AssertionError.class)
    public void testMoveFromUnsharedPrivateToPublicFolder() throws Exception {
        String sourceFolder = createPrivateFolder(null);
        String destinationFolder = publicFolderId;
        FolderExceptionErrorMessage expectedWarning = FolderExceptionErrorMessage.MOVE_TO_SHARED_WARNING;
        checkFolderMove(sourceFolder, destinationFolder, expectedWarning, FolderType.PRIVATE, FolderType.PUBLIC);
    }

    /**
     * 
     * Tests whether the warning
     * {@link com.openexchange.folderstorage.FolderExceptionErrorMessage#MOVE_TO_SHARED_WARNING}
     * is returned, when a folder is moved from a private unshared folder to a shared folder.
     *
     * @throws Exception
     */
    @Test(expected = AssertionError.class)
    public void testMoveFromUnsharedPrivateToSharedFolder() throws Exception {
        String sourceFolder = createPrivateFolder(null);
        String destinationFolder = sharedFolderId;
        FolderExceptionErrorMessage expectedWarning = FolderExceptionErrorMessage.MOVE_TO_SHARED_WARNING;
        checkFolderMove(sourceFolder, destinationFolder, expectedWarning, FolderType.PRIVATE, FolderType.SHARED);
    }

    /**
     * 
     * Tests whether the warning
     * {@link com.openexchange.folderstorage.FolderExceptionErrorMessage#MOVE_TO_SHARED_WARNING}
     * is returned, when a folder is moved from a private unshared folder to a private folder shared with user 2.
     *
     * 
     * @throws Exception
     */
    @Test(expected = AssertionError.class)
    public void testMoveFromUnsharedPrivateToSharedPrivate() throws Exception {
        String sourceFolder = createPrivateFolder(null);
        String destinationFolder = createPrivateFolder(userId2);
        FolderExceptionErrorMessage expectedWarning = FolderExceptionErrorMessage.MOVE_TO_SHARED_WARNING;
        checkFolderMove(sourceFolder, destinationFolder, expectedWarning, FolderType.PRIVATE, FolderType.PRIVATE);
    }

    /**
     * 
     * Tests whether no warning is returned,
     * when a folder is moved from a private unshared folder to a private unshared folder.
     *
     * @throws Exception
     */
    @Test
    public void testMoveFromUnsharedPrivateToUnsharedPrivate() throws Exception {
        String sourceFolder = createPrivateFolder(null);
        String destinationFolder = createPrivateFolder(null);
        String folderToMove = createChildFolder(sourceFolder);
        FolderUpdateResponse response = moveFolder(folderToMove, destinationFolder, null);
        checkResponse(response.getError(), response.getErrorDesc(), response.getData());
        checkParentFolder(folderToMove, destinationFolder);
    }

    /**
     * 
     * Tests whether the no warning
     * is returned, when a folder with subfolders is moved from a private folder to a private folder.
     *
     * @throws Exception
     */
    @Test
    public void testMoveSubfoldersFromPrivateToPrivate() throws Exception {
        String sourceFolder = createPrivateFolder(null);
        String folderToMove = createChildFolder(sourceFolder);
        String subfolder1 = createChildFolder(folderToMove, userId2);
        String subfolder2 = createChildFolder(subfolder1, null);
        createChildFolder(subfolder2, userId3);
        String destinationFolder = createPrivateFolder(null);
        FolderUpdateResponse response = moveFolder(folderToMove, destinationFolder, null);
        checkResponse(response.getError(), response.getErrorDesc(), response.getData());
        checkParentFolder(folderToMove, destinationFolder);
    }

    /**
     * 
     * Tests whether the warning
     * {@link com.openexchange.folderstorage.FolderExceptionErrorMessage#MOVE_TO_SHARED_WARNING}
     * is returned, when a folder with subfolders is moved from a private folder to a private folder shared with user 3.
     *
     * @throws Exception
     */
    @Test(expected = AssertionError.class)
    public void testMoveSubfoldersFromPrivateToSharedPrivate() throws Exception {
        String sourceFolder = createPrivateFolder(null);
        String folderToMove = createChildFolder(sourceFolder);
        String subfolder1 = createChildFolder(folderToMove, userId2);
        String subfolder2 = createChildFolder(subfolder1, null);
        createChildFolder(subfolder2, userId3);
        String destinationFolder = createPrivateFolder(userId3);
        OXException expectedWarning = FolderExceptionErrorMessage.MOVE_TO_SHARED_WARNING.create(getWarningParameters(folderToMove, sourceFolder, destinationFolder, FolderType.PRIVATE, FolderType.PRIVATE));
        checkFolderMove(folderToMove, sourceFolder, destinationFolder, expectedWarning);
    }

    /**
     * 
     * Tests whether the no warning
     * is returned, when a folder with subfolders is moved from a private folder shared with user 2 to a private folder.
     *
     * @throws Exception
     */
    @Test
    public void testMoveSubfoldersFromSharedPrivateToPrivate() throws Exception {
        String sourceFolder = createPrivateFolder(userId2);
        String folderToMove = createChildFolder(sourceFolder);
        String subfolder1 = createChildFolder(folderToMove, userId2);
        String subfolder2 = createChildFolder(subfolder1, null);
        createChildFolder(subfolder2, userId3);
        String destinationFolder = createPrivateFolder(null);
        FolderUpdateResponse response = moveFolder(folderToMove, destinationFolder, null);
        checkResponse(response.getError(), response.getErrorDesc(), response.getData());
        checkParentFolder(folderToMove, destinationFolder);
    }

    /**
     * 
     * Tests whether the warning
     * {@link com.openexchange.folderstorage.FolderExceptionErrorMessage#MOVE_TO_SHARED_WARNING}
     * is returned, when a folder with subfolders is moved from a private folder shared with user 2 to a private folder shared with user 3.
     *
     * @throws Exception
     */
    @Test(expected = AssertionError.class)
    public void testMoveSubfoldersFromSharedPrivateToSharedPrivate() throws Exception {
        String sourceFolder = createPrivateFolder(userId2);
        String folderToMove = createChildFolder(sourceFolder);
        String subfolder1 = createChildFolder(folderToMove, userId2);
        String subfolder2 = createChildFolder(subfolder1, null);
        createChildFolder(subfolder2, userId3);
        String destinationFolder = createPrivateFolder(userId3);
        OXException expectedWarning = FolderExceptionErrorMessage.MOVE_TO_SHARED_WARNING.create(getWarningParameters(folderToMove, sourceFolder, destinationFolder, FolderType.PRIVATE, FolderType.PRIVATE));
        checkFolderMove(folderToMove, sourceFolder, destinationFolder, expectedWarning);
    }

}
