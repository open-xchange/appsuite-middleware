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

package com.openexchange.ajax.folder;

import org.junit.Test;
import com.openexchange.ajax.infostore.apiclient.FileMovePermissionWarningTest.FolderType;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.MoveFolderPermissionMode;
import com.openexchange.test.tryagain.TryAgain;
import com.openexchange.testing.httpclient.models.FolderUpdateResponse;

/**
 *
 * Tests the warning behavior of {@link com.openexchange.folder.json.actions.UpdateAction} with {@link MoveFolderPermissionMode} INHERIT when moving a folder.
 *
 * Tests with (expected = AssertionError.class) test if a MOVE_TO_SHARED_WARNING occurs when a unshared file/folder is moved into a folder with permissions.
 * Warnings for this case are disabled, so the test must fail.
 *
 * @author <a href="mailto:anna.ottersbach@open-xchange.com">Anna Ottersbach</a>
 * @since v7.10.5
 */
public class InheritWarningPermissionOnMoveTest extends AbstractFolderMoveWarningTest {

    public InheritWarningPermissionOnMoveTest() {
        super("inherit");
    }

    /**
     *
     * Tests whether the warning
     * {@link com.openexchange.folderstorage.FolderExceptionErrorMessage#MOVE_TO_ANOTHER_SHARED_WARNING}
     * is returned, when a folder is moved from a public folder to a shared folder.
     *
     * @throws Exception
     */
    @Test
    @TryAgain
    public void testMoveFromPublicFolderToSharedFolder() throws Exception {
        String sourceFolder = publicFolderId;
        String destinationFolder = sharedFolderId;
        FolderExceptionErrorMessage expectedWarning = FolderExceptionErrorMessage.MOVE_TO_ANOTHER_SHARED_WARNING;
        checkFolderMove(sourceFolder, destinationFolder, expectedWarning, FolderType.PUBLIC, FolderType.SHARED);
    }


    /**
     *
     * Tests whether the warning
     * {@link com.openexchange.folderstorage.FolderExceptionErrorMessage#MOVE_TO_NOT_SHARED_WARNING}
     * is returned, when a folder is moved from a public folder to a private unshared folder.
     *
     * @throws Exception
     */
    @Test
    @TryAgain
    public void testMoveFromPublicFolderToUnsharedPrivate() throws Exception {
        String sourceFolder = publicFolderId;
        String destinationFolder = createPrivateFolder(null);
        FolderExceptionErrorMessage expectedWarning = FolderExceptionErrorMessage.MOVE_TO_NOT_SHARED_WARNING;
        checkFolderMove(sourceFolder, destinationFolder, expectedWarning, FolderType.PUBLIC, FolderType.PRIVATE);
    }

    /**
     *
     * Tests whether the warning
     * {@link com.openexchange.folderstorage.FolderExceptionErrorMessage#MOVE_TO_ANOTHER_SHARED_WARNING}
     * is returned, when a folder is moved from a shared folder to a public folder.
     *
     * @throws Exception
     */
    @Test
    @TryAgain
    public void testMoveFromSharedFolderToPublicFolder() throws Exception {
        String sourceFolder = sharedFolderId;
        String destinationFolder = publicFolderId;
        FolderExceptionErrorMessage expectedWarning = FolderExceptionErrorMessage.MOVE_TO_ANOTHER_SHARED_WARNING;
        checkFolderMove(sourceFolder, destinationFolder, expectedWarning, FolderType.SHARED, FolderType.PUBLIC);
    }

    /**
     *
     * Tests whether the warning
     * {@link com.openexchange.folderstorage.FolderExceptionErrorMessage#MOVE_TO_NOT_SHARED_WARNING}
     * is returned, when a folder is moved from a shared folder to a private unshared folder.
     *
     * @throws Exception
     */
    @Test
    @TryAgain
    public void testMoveFromSharedFolderToUnsharedPrivate() throws Exception {
        String sourceFolder = sharedFolderId;
        String destinationFolder = createPrivateFolder(null);
        FolderExceptionErrorMessage expectedWarning = FolderExceptionErrorMessage.MOVE_TO_NOT_SHARED_WARNING;
        checkFolderMove(sourceFolder, destinationFolder, expectedWarning, FolderType.SHARED, FolderType.PRIVATE);
    }

    /**
     *
     * Tests whether no warning is returned,
     * when a folder is moved from a private folder shared with user 2 to a private folder shared with user 2.
     *
     * @throws Exception
     */
    @Test
    @TryAgain
    public void testMoveFromSharedPrivateToSameSharedPrivate() throws Exception {
        String sourceFolder = createPrivateFolder(userId2);
        String destinationFolder = createPrivateFolder(userId2);

        String folderToMove = createChildFolder(sourceFolder);
        FolderUpdateResponse responseIgnored = moveFolder(folderToMove, destinationFolder, Boolean.FALSE);
        checkResponse(responseIgnored.getError(), responseIgnored.getErrorDesc(), responseIgnored.getData());
        checkParentFolder(folderToMove, destinationFolder);
    }

    /**
     *
     * Tests whether the warning
     * {@link com.openexchange.folderstorage.FolderExceptionErrorMessage#MOVE_TO_ANOTHER_SHARED_WARNING}
     * is returned, when a folder is moved from a private folder shared with user 2 to a private folder shared with user 3.
     *
     * @throws Exception
     */
    @Test
    @TryAgain
    public void testMoveFromSharedPrivateToSharedPrivate() throws Exception {
        String sourceFolder = createPrivateFolder(userId2);
        String destinationFolder = createPrivateFolder(userId3);
        FolderExceptionErrorMessage expectedWarning = FolderExceptionErrorMessage.MOVE_TO_ANOTHER_SHARED_WARNING;
        checkFolderMove(sourceFolder, destinationFolder, expectedWarning, FolderType.PRIVATE, FolderType.PRIVATE);
    }

    /**
     *
     * Tests whether the warning
     * {@link com.openexchange.folderstorage.FolderExceptionErrorMessage#MOVE_TO_NOT_SHARED_WARNING}
     * is returned, when a folder is moved from a private folder shared with user 2 to a private unshared folder.
     *
     * @throws Exception
     */
    @Test
    @TryAgain
    public void testMoveFromSharedPrivateToUnsharedPrivate() throws Exception {
        String sourceFolder = createPrivateFolder(userId2);
        String destinationFolder = createPrivateFolder(null);
        FolderExceptionErrorMessage expectedWarning = FolderExceptionErrorMessage.MOVE_TO_NOT_SHARED_WARNING;
        checkFolderMove(sourceFolder, destinationFolder, expectedWarning, FolderType.PRIVATE, FolderType.PRIVATE);
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
    @TryAgain
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
    @TryAgain
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
    @TryAgain
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
     * Tests whether the warning
     * {@link com.openexchange.folderstorage.FolderExceptionErrorMessage#MOVE_SHARED_SUBFOLDERS_TO_NOT_SHARED_WARNING}
     * is returned, when a folder with subfolders is moved from a private folder to a private folder.
     *
     * @throws Exception
     */
    @Test
    @TryAgain
    public void testMoveSubfoldersFromPrivateToPrivate() throws Exception {
        String sourceFolder = createPrivateFolder(null);
        String folderToMove = createChildFolder(sourceFolder);
        String subfolder1 = createChildFolder(folderToMove, userId2);
        String subfolder2 = createChildFolder(subfolder1, null);
        createChildFolder(subfolder2, userId3);
        String destinationFolder = createPrivateFolder(null);
        OXException expectedWarning = FolderExceptionErrorMessage.MOVE_SHARED_SUBFOLDERS_TO_NOT_SHARED_WARNING.create(getWarningParameters(folderToMove, sourceFolder, destinationFolder, FolderType.PRIVATE, FolderType.PRIVATE));
        checkFolderMove(folderToMove, sourceFolder, destinationFolder, expectedWarning);
    }

    /**
     *
     * Tests whether the warning
     * {@link com.openexchange.folderstorage.FolderExceptionErrorMessage#MOVE_SHARED_SUBFOLDERS_TO_SHARED_WARNING}
     * is returned, when a folder with subfolders is moved from a private folder to a private folder shared with user 3.
     *
     * @throws Exception
     */
    @Test
    public void testMoveSubfoldersFromPrivateToSharedPrivate() throws Exception {
        String sourceFolder = createPrivateFolder(null);
        String folderToMove = createChildFolder(sourceFolder);
        String subfolder1 = createChildFolder(folderToMove, userId2);
        String subfolder2 = createChildFolder(subfolder1, null);
        createChildFolder(subfolder2, userId3);
        String destinationFolder = createPrivateFolder(userId3);
        OXException expectedWarning = FolderExceptionErrorMessage.MOVE_SHARED_SUBFOLDERS_TO_SHARED_WARNING.create(getWarningParameters(folderToMove, sourceFolder, destinationFolder, FolderType.PRIVATE, FolderType.PRIVATE));
        checkFolderMove(folderToMove, sourceFolder, destinationFolder, expectedWarning);
    }

    /**
     *
     * Tests whether the warning
     * {@link com.openexchange.folderstorage.FolderExceptionErrorMessage#MOVE_TO_NOT_SHARED_WARNING}
     * is returned, when a folder with subfolders is moved from a private folder shared with user 2 to a private folder.
     *
     * @throws Exception
     */
    @Test
    @TryAgain
    public void testMoveSubfoldersFromSharedPrivateToPrivate() throws Exception {
        String sourceFolder = createPrivateFolder(userId2);
        String folderToMove = createChildFolder(sourceFolder);
        String subfolder1 = createChildFolder(folderToMove, userId2);
        String subfolder2 = createChildFolder(subfolder1, null);
        createChildFolder(subfolder2, userId3);
        String destinationFolder = createPrivateFolder(null);
        OXException expectedWarning = FolderExceptionErrorMessage.MOVE_TO_NOT_SHARED_WARNING.create(getWarningParameters(folderToMove, sourceFolder, destinationFolder, FolderType.PRIVATE, FolderType.PRIVATE));
        checkFolderMove(folderToMove, sourceFolder, destinationFolder, expectedWarning);
    }

    /**
     *
     * Tests whether the warning
     * {@link com.openexchange.folderstorage.FolderExceptionErrorMessage#MOVE_TO_ANOTHER_SHARED_WARNING}
     * is returned, when a folder with subfolders is moved from a private folder shared with user 2 to a private folder shared with user 3.
     *
     * @throws Exception
     */
    @Test
    @TryAgain
    public void testMoveSubfoldersFromSharedPrivateToSharedPrivate() throws Exception {
        String sourceFolder = createPrivateFolder(userId2);
        String folderToMove = createChildFolder(sourceFolder);
        String subfolder1 = createChildFolder(folderToMove, userId2);
        String subfolder2 = createChildFolder(subfolder1, null);
        createChildFolder(subfolder2, userId3);
        String destinationFolder = createPrivateFolder(userId3);
        OXException expectedWarning = FolderExceptionErrorMessage.MOVE_TO_ANOTHER_SHARED_WARNING.create(getWarningParameters(folderToMove, sourceFolder, destinationFolder, FolderType.PRIVATE, FolderType.PRIVATE));
        checkFolderMove(folderToMove, sourceFolder, destinationFolder, expectedWarning);
    }

}
