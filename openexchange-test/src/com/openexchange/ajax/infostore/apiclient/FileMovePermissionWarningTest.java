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

package com.openexchange.ajax.infostore.apiclient;

import static com.openexchange.java.Autoboxing.I;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.folder.manager.FolderManager;
import com.openexchange.ajax.parser.ResponseParser;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.test.pool.TestUser;
import com.openexchange.testing.httpclient.invoker.ApiClient;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.FolderPermission;
import com.openexchange.testing.httpclient.models.InfoItemMovedResponse;
import com.openexchange.testing.httpclient.models.InfoItemPermission;
import com.openexchange.testing.httpclient.models.InfoItemsMovedResponse;
import com.openexchange.testing.httpclient.modules.FoldersApi;
import com.openexchange.tools.io.IOTools;

/**
 *
 * {@link FileMovePermissionWarningTest}
 *
 * Tests the warning behavior of {@link com.openexchange.file.storage.json.actions.files.MoveAction}.
 * The action can handle single files, files as pairs and folders.
 * The folder move is only tested with the MoveFolderPermissionMode INHERIT to test moving a folder via the files move action.
 * The generation of warnings of the other mode is tested in {@link com.openexchange.ajax.folder.MergeWarningPermissionOnMoveTest}.
 *
 * Tests with (expected = AssertionError.class) test if a MOVE_TO_SHARED_WARNING occurs when a unshared file/folder is moved into a folder with permissions.
 * Warnings for this case are disabled, so the test must fail.
 *
 * @author <a href="mailto:anna.ottersbach@open-xchange.com">Anna Ottersbach</a>
 * @since v7.10.5
 */
@RunWith(Parameterized.class)
public class FileMovePermissionWarningTest extends InfostoreApiClientTest {

    public enum MoveItem {
        FILE_PAIR,
        SINGLE_FILE;
    }

    private static final Integer BITS_ADMIN = new Integer(403710016);
    private static final Integer BITS_REVIEWER = new Integer(33025);
    private static final String SHARED_FOLDER_ID = "15";

    @Parameters(name = "itemToMove={0}")
    public static Iterable<Object[]> params() {
        List<Object[]> settings = new ArrayList<>(3);
        settings.add(new Object[] { MoveItem.FILE_PAIR });
        settings.add(new Object[] { MoveItem.SINGLE_FILE });
        return settings;
    }

    @Parameter(value = 0)
    public MoveItem itemToMove;
    private ApiClient apiClient3;
    private File file;
    private FolderManager folderManager;
    private FolderManager folderManager2;
    private TestUser testUser3;
    private Integer userId1;
    private Integer userId2;
    private Integer userId3;
    private List<String> foldersToDelete;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        userId1 = apiClient.getUserId();
        userId2 = apiClient2.getUserId();
        testUser3 = testContext.acquireUser();
        apiClient3 = generateApiClient(testUser3);
        rememberClient(apiClient3);
        userId3 = apiClient3.getUserId();

        folderManager = new FolderManager(new FoldersApi(getApiClient()), "1");
        folderManager2 = new FolderManager(new FoldersApi(getApiClient2()), "1");
        foldersToDelete = new ArrayList<String>();

        file = File.createTempFile("FileMovePermissionWarningTest", ".txt");
    }

    @Override
    public void tearDown() throws Exception {
        for (String folder : foldersToDelete) {
            deleteFolder(folder, Boolean.TRUE);
        }
        super.tearDown();
    }

    /**
     *
     * Tests whether the warning MOVE_TO_ANOTHER_SHARED_WARNING
     * is returned, when a file (as pair), a single file or a folder (with permission mode inherit) is moved
     * from a private folder shared with user 2 to a private folder shared with user 3.
     *
     * @throws Exception
     */
    @Test
    public void testMoveFromPrivateSharedToSharedFolder() throws Exception {
        String sourceFolderId = createPrivateFolder(userId2);
        String destinationFolderId = createPrivateFolder(userId3);
        OXException expectedFileWarning = FileStorageExceptionCodes.MOVE_TO_ANOTHER_SHARED_WARNING.create(getFileWarningArguments(sourceFolderId, destinationFolderId, FolderType.PRIVATE, FolderType.PRIVATE));
        checkInfoItemMove(sourceFolderId, destinationFolderId, expectedFileWarning);
    }

    /**
     *
     * Tests whether the warning MOVE_TO_NOT_SHARED_WARNING
     * is returned, when a file (as pair), a single file or a folder (with permission mode inherit) is moved
     * from a private folder shared with user 2 to a private unshared folder.
     *
     * @throws Exception
     */
    @Test
    public void testMoveFromPrivateSharedToUnsharedFolder() throws Exception {
        String sourceFolderId = createPrivateFolder(userId2);
        String destinationFolderId = createPrivateFolder(null);
        OXException expectedFileWarning = FileStorageExceptionCodes.MOVE_TO_NOT_SHARED_WARNING.create(getFileWarningArguments(sourceFolderId, destinationFolderId, FolderType.PRIVATE, FolderType.PRIVATE));
        checkInfoItemMove(sourceFolderId, destinationFolderId, expectedFileWarning);
    }

    /**
     *
     * Tests whether the warning MOVE_TO_SHARED_WARNING
     * is returned, when a file (as pair), a single file or a folder (with permission mode inherit) is moved
     * from a private unshared folder to a public folder.
     *
     * @throws Exception
     */
    @Test(expected = AssertionError.class)
    public void testMoveFromPrivateToPublicFolder() throws Exception {
        String sourceFolderId = createPrivateFolder(null);
        String destinationFolderId = createPublicFolder();
        OXException expectedFileWarning = FileStorageExceptionCodes.MOVE_TO_SHARED_WARNING.create(getFileWarningArguments(sourceFolderId, destinationFolderId, FolderType.PRIVATE, FolderType.PUBLIC));
        checkInfoItemMove(sourceFolderId, destinationFolderId, expectedFileWarning);
    }

    /**
     *
     * Tests whether the warning MOVE_TO_SHARED_WARNING
     * is returned, when a file (as pair), a single file or a folder (with permission mode inherit) is moved
     * from a private unshared folder to a shared folder.
     *
     * @throws Exception
     */
    @Test(expected = AssertionError.class)
    public void testMoveFromPrivateToSharedFolder() throws Exception {
        String sourceFolderId = createPrivateFolder(null);
        String destinationFolderId = createSharedFolder();
        OXException expectedFileWarning = FileStorageExceptionCodes.MOVE_TO_SHARED_WARNING.create(getFileWarningArguments(sourceFolderId, destinationFolderId, FolderType.PRIVATE, FolderType.SHARED));
        checkInfoItemMove(sourceFolderId, destinationFolderId, expectedFileWarning);
    }

    /**
     *
     * Tests whether the warning MOVE_TO_SHARED_WARNING
     * is returned, when a file (as pair), a single file or a folder (with permission mode inherit) is moved
     * from a private unshared folder to a private folder shared with user 2.
     *
     * @throws Exception
     */
    @Test(expected = AssertionError.class)
    public void testMoveFromPrivateUnsharedToSharedFolder() throws Exception {
        String sourceFolderId = createPrivateFolder(null);
        String destinationFolderId = createPrivateFolder(userId2);
        OXException expectedFileWarning = FileStorageExceptionCodes.MOVE_TO_SHARED_WARNING.create(getFileWarningArguments(sourceFolderId, destinationFolderId, FolderType.PRIVATE, FolderType.PRIVATE));
        checkInfoItemMove(sourceFolderId, destinationFolderId, expectedFileWarning);
    }

    /**
     *
     * Tests whether the warning MOVE_TO_NOT_SHARED_WARNING
     * is returned, when a file (as pair), a single file or a folder (with permission mode inherit) is moved from a public folder to a private unshared folder.
     *
     * @throws Exception
     */
    @Test
    public void testMoveFromPublicToPrivateUnsharedFolder() throws Exception {
        String sourceFolderId = createPublicFolder();
        String destinationFolderId = createPrivateFolder(null);
        OXException expectedFileWarning = FileStorageExceptionCodes.MOVE_TO_NOT_SHARED_WARNING.create(getFileWarningArguments(sourceFolderId, destinationFolderId, FolderType.PUBLIC, FolderType.PRIVATE));
        checkInfoItemMove(sourceFolderId, destinationFolderId, expectedFileWarning);
    }

    /**
     *
     * Tests whether the warning MOVE_TO_ANOTHER_SHARED_WARNING
     * is returned, when a file (as pair), a single file or a folder (with permission mode inherit) is moved from a public folder to a shared folder.
     *
     * @throws Exception
     */
    @Test
    public void testMoveFromPublicToSharedFolder() throws Exception {
        String sourceFolderId = createPublicFolder();
        String destinationFolderId = createSharedFolder();
        OXException expectedFileWarning = FileStorageExceptionCodes.MOVE_TO_ANOTHER_SHARED_WARNING.create(getFileWarningArguments(sourceFolderId, destinationFolderId, FolderType.PUBLIC, FolderType.SHARED));
        checkInfoItemMove(sourceFolderId, destinationFolderId, expectedFileWarning);
    }

    /**
     *
     * Tests whether the warning MOVE_TO_NOT_SHARED_WARNING
     * is returned, when a file (as pair), a single file or a folder (with permission mode inherit) is moved from a shared folder to a private unshared folder.
     *
     * @throws Exception
     */
    @Test
    public void testMoveFromSharedToPrivateUnsharedFolder() throws Exception {
        String sourceFolderId = createSharedFolder();
        String destinationFolderId = createPrivateFolder(null);
        OXException expectedFileWarning = FileStorageExceptionCodes.MOVE_TO_NOT_SHARED_WARNING.create(getFileWarningArguments(sourceFolderId, destinationFolderId, FolderType.SHARED, FolderType.PRIVATE));
        checkInfoItemMove(sourceFolderId, destinationFolderId, expectedFileWarning);
    }

    /**
     *
     * Tests whether the warning MOVE_TO_ANOTHER_SHARED_WARNING
     * is returned, when a file (as pair), a single file or a folder (with permission mode inherit) is moved from a shared folder to a public folder.
     *
     * @throws Exception
     */
    @Test
    public void testMoveFromSharedToPublicFolder() throws Exception {
        String sourceFolderId = createSharedFolder();
        String destinationFolderId = createPublicFolder();
        OXException expectedFileWarning = FileStorageExceptionCodes.MOVE_TO_ANOTHER_SHARED_WARNING.create(getFileWarningArguments(sourceFolderId, destinationFolderId, FolderType.SHARED, FolderType.PUBLIC));
        checkInfoItemMove(sourceFolderId, destinationFolderId, expectedFileWarning);
    }

    /**
     *
     * Tests whether the warnings
     * {@link com.openexchange.file.storage.FileStorageExceptionCodes#MOVE_SHARED_FILE_WARNING}
     * and {@link com.openexchange.file.storage.FileStorageExceptionCodes#MOVE_TO_SHARED_WARNING}
     * is returned, when a file shared with user 2 is moved from a private unshared to another private shared folder.
     *
     * @throws Exception
     */
    @Test(expected = AssertionError.class)
    public void testMoveSharedFileFromPrivateUnsharedToPrivateSharedFolder() throws Exception {
        String sourceFolderId = createPrivateFolder(null);
        String destinationFolderId = createPrivateFolder(userId3);
        String newFileId = uploadInfoItemToFolder(null, file, sourceFolderId, "text/plain", null, null, null, null, null);
        shareFileToUser(newFileId, userId2);
        OXException expectedWarning1 = FileStorageExceptionCodes.MOVE_SHARED_FILE_WARNING.create(getFileWarningArguments(newFileId, sourceFolderId, destinationFolderId, FolderType.PRIVATE, FolderType.PRIVATE));
        OXException expectedWarning2 = FileStorageExceptionCodes.MOVE_TO_SHARED_WARNING.create(getFileWarningArguments(newFileId, sourceFolderId, destinationFolderId, FolderType.PRIVATE, FolderType.PRIVATE));
        switch (itemToMove) {
            case FILE_PAIR:
                InfoItemsMovedResponse response = moveInfoItem(newFileId, sourceFolderId, destinationFolderId, false);
                checkResponseForWarning(response, expectedWarning1);
                checkResponseForWarning(response, expectedWarning2);
                assertTrue(fileExistsInFolder(newFileId, sourceFolderId));
                checkMoveInfoItemRequestWithIgnoreWarnings(newFileId, sourceFolderId, destinationFolderId);
                assertFalse(fileExistsInFolder(newFileId, destinationFolderId));
                break;
            case SINGLE_FILE:
                InfoItemMovedResponse infoItemMovedResponse = moveFile(newFileId, sourceFolderId, destinationFolderId, false);
                checkResponseForWarning(infoItemMovedResponse, expectedWarning1);
                checkResponseForWarning(infoItemMovedResponse, expectedWarning2);
                assertTrue(fileExistsInFolder(newFileId, sourceFolderId));
                checkMoveFileRequestWithIgnoreWarnings(newFileId, sourceFolderId, destinationFolderId);
                assertFalse(fileExistsInFolder(newFileId, sourceFolderId));
                break;
            default:
                break;
        }
    }

    /**
     *
     * Tests whether the warning
     * {@link com.openexchange.file.storage.FileStorageExceptionCodes#MOVE_SHARED_FILE_WARNING}
     * is returned, when files shared with user 2 is moved from a private unshared to another private unshared folder.
     *
     * @throws Exception
     */
    @Test
    public void testMoveMultiSharedFilesFromPrivateUnsharedToUnsharedFolder() throws Exception {
        String sourceFolderId = createPrivateFolder(null);
        String destinationFolderId = createPrivateFolder(null);
        String newFileId1 = uploadInfoItemToFolder(null, file, sourceFolderId, "text/plain", null, null, null, null, null);
        shareFileToUser(newFileId1, userId2);
        String newFileId2 = uploadInfoItemToFolder(null, file, sourceFolderId, "text/plain", null, null, null, null, null);
        shareFileToUser(newFileId2, userId2);
        OXException expectedWarning1 = FileStorageExceptionCodes.MOVE_SHARED_FILE_WARNING.create(getFileWarningArguments(newFileId1, sourceFolderId, destinationFolderId, FolderType.PRIVATE, FolderType.PRIVATE));
        OXException expectedWarning2 = FileStorageExceptionCodes.MOVE_SHARED_FILE_WARNING.create(getFileWarningArguments(newFileId2, sourceFolderId, destinationFolderId, FolderType.PRIVATE, FolderType.PRIVATE));

        InfoItemsMovedResponse response = moveInfoItems(Arrays.asList(newFileId1, newFileId2), sourceFolderId, destinationFolderId, false);
        checkResponseForWarning(response, expectedWarning2);
        checkResponseForWarning(response, expectedWarning1);
        assertTrue(fileExistsInFolder(newFileId1, sourceFolderId));
        assertTrue(fileExistsInFolder(newFileId2, sourceFolderId));
        InfoItemsMovedResponse responseIgnored = moveInfoItems(Arrays.asList(newFileId1, newFileId2), sourceFolderId, destinationFolderId, true);
        assertNotNull("Response expected", responseIgnored);
        assertEquals("Warning expected, but no error.", Category.CATEGORY_WARNING.toString(), responseIgnored.getCategories());
        assertFalse(fileExistsInFolder(newFileId1, destinationFolderId));
        assertFalse(fileExistsInFolder(newFileId2, destinationFolderId));

    }

    /**
     *
     * Tests whether the warning
     * {@link com.openexchange.file.storage.FileStorageExceptionCodes#MOVE_SHARED_FILE_WARNING}
     * is returned, when a file shared with user 2 is moved from a private unshared to another private unshared folder.
     *
     * @throws Exception
     */
    @Test
    public void testMoveSharedFileFromPrivateUnsharedToUnsharedFolder() throws Exception {
        String sourceFolderId = createPrivateFolder(null);
        String destinationFolderId = createPrivateFolder(null);
        String newFileId = uploadInfoItemToFolder(null, file, sourceFolderId, "text/plain", null, null, null, null, null);
        shareFileToUser(newFileId, userId2);
        OXException expectedWarning = FileStorageExceptionCodes.MOVE_SHARED_FILE_WARNING.create(getFileWarningArguments(newFileId, sourceFolderId, destinationFolderId, FolderType.PRIVATE, FolderType.PRIVATE));
        switch (itemToMove) {
            case FILE_PAIR:
                InfoItemsMovedResponse response = moveInfoItem(newFileId, sourceFolderId, destinationFolderId, false);
                checkResponseForWarning(response, expectedWarning);
                assertTrue(fileExistsInFolder(newFileId, sourceFolderId));
                checkMoveInfoItemRequestWithIgnoreWarnings(newFileId, sourceFolderId, destinationFolderId);
                assertFalse(fileExistsInFolder(newFileId, destinationFolderId));
                break;
            case SINGLE_FILE:
                InfoItemMovedResponse infoItemMovedResponse = moveFile(newFileId, sourceFolderId, destinationFolderId, false);
                checkResponseForWarning(infoItemMovedResponse, expectedWarning);
                assertTrue(fileExistsInFolder(newFileId, sourceFolderId));
                checkMoveFileRequestWithIgnoreWarnings(newFileId, sourceFolderId, destinationFolderId);
                assertFalse(fileExistsInFolder(newFileId, sourceFolderId));
                break;
            default:
                break;
        }
    }

    /**
     *
     * Tests whether no warning is returned,
     * when a file (as pair), a single file or a folder (with permission mode inherit) is moved
     * from a private folder shared with user 2 to a private folder shared with user 2.
     *
     * @throws Exception
     */
    @Test
    public void testMoveToPrivateSharedSamePermissions() throws Exception {
        String sourceFolderId = createPrivateFolder(userId2);
        String destinationFolderId = createPrivateFolder(userId2);
        checkInfoItemMove(sourceFolderId, destinationFolderId);
    }

    /**
     *
     * Tests whether no warning is returned,
     * when a file (as pair), a single file or a folder (with permission mode inherit) is moved
     * from a private unshared folder to a private unshared folder.
     *
     * @throws Exception
     */
    @Test
    public void testMoveToPrivateUnsharedSamePermission() throws Exception {
        String sourceFolderId = createPrivateFolder(null);
        String destinationFolderId = createPrivateFolder(null);
        checkInfoItemMove(sourceFolderId, destinationFolderId);
    }

    @Override
    protected Map<String, String> getNeededConfigurations() {
        Map<String, String> configs = new HashMap<>();
        configs.put("com.openexchange.folderstorage.permissions.moveToPublic", "inherit");
        configs.put("com.openexchange.folderstorage.permissions.moveToShared", "inherit");
        configs.put("com.openexchange.folderstorage.permissions.moveToPrivate", "inherit");
        return configs;
    }

    @Override
    protected String getScope() {
        return "user";
    }

    private void addFileIdToException(OXException expectedFileWarning, String fileID) {
        Object[] displayArgs = expectedFileWarning.getDisplayArgs();
        displayArgs[3] = fileID;
        expectedFileWarning.setDisplayMessage(expectedFileWarning.getDisplayMessage(null), displayArgs);
    }

    private void checkInfoItemMove(String sourceFolderId, String destinationFolderId) throws ApiException, FileNotFoundException, IOException {
        String newItemToMove;
        switch (itemToMove) {
            case FILE_PAIR:
                newItemToMove = createFile(sourceFolderId);
                InfoItemsMovedResponse response = moveInfoItem(newItemToMove, sourceFolderId, destinationFolderId, false);
                assertNotNull("Response expected", response);
                assertNull("No errors and warnings expected.", response.getError());
                assertFalse(fileExistsInFolder(newItemToMove, destinationFolderId));
                break;
            case SINGLE_FILE:
                newItemToMove = createFile(sourceFolderId);
                InfoItemMovedResponse infoItemMovedResponse = moveFile(newItemToMove, sourceFolderId, destinationFolderId, false);
                assertNotNull("Response expected", infoItemMovedResponse);
                assertNull("No errors and warnings expected.", infoItemMovedResponse.getError());
                assertFalse(fileExistsInFolder(newItemToMove, sourceFolderId));
                break;
            default:
                break;
        }
    }

    private void checkInfoItemMove(String sourceFolderId, String destinationFolderId, OXException expectedFileWarning) throws ApiException, FileNotFoundException, IOException, JSONException {
        String newItemToMove;
        switch (itemToMove) {
            case FILE_PAIR:
                newItemToMove = createFile(sourceFolderId);
                addFileIdToException(expectedFileWarning, newItemToMove);
                InfoItemsMovedResponse response = moveInfoItem(newItemToMove, sourceFolderId, destinationFolderId, false);
                checkResponseForWarning(response, expectedFileWarning);
                assertTrue(fileExistsInFolder(newItemToMove, sourceFolderId));
                checkMoveInfoItemRequestWithIgnoreWarnings(newItemToMove, sourceFolderId, destinationFolderId);
                assertFalse(fileExistsInFolder(newItemToMove, sourceFolderId));
                break;
            case SINGLE_FILE:
                newItemToMove = createFile(sourceFolderId);
                addFileIdToException(expectedFileWarning, newItemToMove);
                InfoItemMovedResponse infoItemMovedResponse = moveFile(newItemToMove, sourceFolderId, destinationFolderId, false);
                checkResponseForWarning(infoItemMovedResponse, expectedFileWarning);
                assertTrue(fileExistsInFolder(newItemToMove, sourceFolderId));
                checkMoveFileRequestWithIgnoreWarnings(newItemToMove, sourceFolderId, destinationFolderId);
                assertFalse(fileExistsInFolder(newItemToMove, sourceFolderId));
                break;
            default:
                break;
        }
    }

    private void checkMoveFileRequestWithIgnoreWarnings(String fileId, String sourceFolderId, String destinationFolderId) throws ApiException {
        InfoItemMovedResponse ignoreWarningsResponse = moveFile(fileId, sourceFolderId, destinationFolderId, true);
        assertNotNull("Response expected", ignoreWarningsResponse);
        String[] splittedID = fileId.split("/");
        String newObjectID = destinationFolderId + "/" + splittedID[1];
        assertEquals("Expected the new object id.", newObjectID, ignoreWarningsResponse.getData());
        assertEquals("Warning expected, but no error.", Category.CATEGORY_WARNING.toString(), ignoreWarningsResponse.getCategories());
    }

    private void checkMoveInfoItemRequestWithIgnoreWarnings(String fileId, String sourceFolderId, String destinationFolderId) throws ApiException {
        InfoItemsMovedResponse ignoreWarningsResponse = moveInfoItem(fileId, sourceFolderId, destinationFolderId, true);
        assertNotNull("Response expected", ignoreWarningsResponse);
        assertTrue("Empty list of conflicting items expected.", ignoreWarningsResponse.getData().isEmpty());
        assertEquals("Warning expected, but no error.", Category.CATEGORY_WARNING.toString(), ignoreWarningsResponse.getCategories());
    }

    private void checkParsedResponseForWarning(Response parsedResponse, OXException expectedWarning) {
        List<OXException> warnings = parsedResponse.getWarnings();
        boolean result = false;
        for (OXException warning : warnings) {
            if (warning.getErrorCode().contentEquals(expectedWarning.getErrorCode()) && // equality in error code
                warning.getPlainLogMessage().equals(expectedWarning.getPlainLogMessage())) { // equality in plain log message
                for (int i = 0; i < warning.getDisplayArgs().length; i++) { // equality in display args
                    if (warning.getDisplayArgs()[i].equals(expectedWarning.getDisplayArgs()[i])) {
                        result = true;
                    } else {
                        result = false;
                        continue;
                    }
                }
            }
        }
        assertTrue("Excepected: \"" + expectedWarning.getMessage() + "\"\n, but warnings only contains : \"" + warnings.toString() + "\"", result);
    }

    private void checkResponseForWarning(InfoItemMovedResponse response, OXException expectedWarning) throws JSONException {
        assertNotNull("Response expected", response);
        assertNotNull("Warning expected", response.getError());
        assertNull("Expected null for new infoitem id.", response.getData());
        Response parsedResponse = ResponseParser.parse(response.toJson());
        assertTrue(response.getErrorDesc(), parsedResponse.hasWarnings());
        checkParsedResponseForWarning(parsedResponse, expectedWarning);
    }

    private void checkResponseForWarning(InfoItemsMovedResponse response, OXException expectedWarning) throws JSONException {
        assertNotNull("Response expected", response);
        assertNotNull("Warning expected", response.getError());
        assertNotNull(response.getData());
        assertFalse("Expected an array with the conflicting infoitems", response.getData().isEmpty());
        Response parsedResponse = ResponseParser.parse(response.toJson());
        assertTrue(response.getErrorDesc(), parsedResponse.hasWarnings());
        checkParsedResponseForWarning(parsedResponse, expectedWarning);
    }

    private String createFile(String parentFolderId) throws ApiException, FileNotFoundException, IOException {
        return uploadInfoItemToFolder(null, file, parentFolderId, "text/plain", null, IOTools.getBytes(new FileInputStream(file)), null, null, null);
    }

    private FolderPermission createPermissionFor(Integer entity, Integer bits, Boolean isGroup) {
        FolderPermission p = new FolderPermission();
        p.setEntity(entity);
        p.setGroup(isGroup);
        p.setBits(bits);
        return p;
    }

    private String createPrivateFolder(Integer sharedToUser) throws ApiException {
        return createPrivateFolder(folderId, sharedToUser);
    }

    private String createPrivateFolder(String parentFolderId, Integer sharedToUser) throws ApiException {
        List<FolderPermission> perm = new ArrayList<>();
        FolderPermission p1 = createPermissionFor(userId1, BITS_ADMIN, Boolean.FALSE);
        perm.add(p1);
        if (sharedToUser != null && sharedToUser.intValue() > 0) {
            FolderPermission p = createPermissionFor(sharedToUser, BITS_REVIEWER, Boolean.FALSE);
            perm.add(p);
        }
        return folderManager.createFolder(parentFolderId, "FileMovePermissionWarningTest" + UUID.randomUUID().toString(), perm);
    }

    private String createPublicFolder() throws ApiException {
        List<FolderPermission> perm = new ArrayList<>();
        FolderPermission p1 = createPermissionFor(userId1, BITS_ADMIN, Boolean.FALSE);
        perm.add(p1);
        FolderPermission p = createPermissionFor(I(0), BITS_REVIEWER, Boolean.TRUE);
        perm.add(p);
        String id = folderManager.createFolder(SHARED_FOLDER_ID, "FileMovePermissionWarningTest" + UUID.randomUUID().toString(), perm);
        foldersToDelete.add(id);
        return id;
    }

    private String createSharedFolder() throws ApiException {
        List<FolderPermission> perm = new ArrayList<>();
        FolderPermission p1 = createPermissionFor(userId2, BITS_ADMIN, Boolean.FALSE);
        perm.add(p1);
        FolderPermission p2 = createPermissionFor(userId1, BITS_ADMIN, Boolean.FALSE);
        perm.add(p2);
        String id = folderManager2.createFolder(getPrivateInfostoreFolder(getApiClient2()), "FolderPermissionTest_" + UUID.randomUUID().toString(), perm);
        foldersToDelete.add(id);
        return id;
    }

    private Object[] getFileWarningArguments(String sourceFolderId, String destinationFolderId, FolderType sourceType, FolderType destinationType) throws ApiException {
        return getFileWarningArguments(null, sourceFolderId, destinationFolderId, sourceType, destinationType);
    }

    private Object[] getFileWarningArguments(String fileId, String sourceFolderId, String destinationFolderId, FolderType sourceType, FolderType destinationType) throws ApiException {
        String sourceFolderPath = sourceType.getRootPath() + (sourceType.equals(FolderType.SHARED) ? folderManager2.getFolderName(getPrivateInfostoreFolder(getApiClient2())) + "/" : "") + (sourceType.equals(FolderType.PRIVATE) ? folderTitle + "/" : "") + folderManager.getFolderName(sourceFolderId);
        String destinationFolderPath = destinationType.getRootPath() + (destinationType.equals(FolderType.SHARED) ? folderManager2.getFolderName(getPrivateInfostoreFolder(getApiClient2())) + "/" : "") + (destinationType.equals(FolderType.PRIVATE) ? folderTitle + "/" : "") + folderManager.getFolderName(destinationFolderId);
        Object[] args = { file.getName(), sourceFolderPath, destinationFolderPath, fileId, destinationFolderId };
        return args;
    }

    private void shareFileToUser(String fileId, Integer userId) throws ApiException {
        InfoItemPermission permission = new InfoItemPermission();
        permission.entity(userId);
        permission.setGroup(Boolean.FALSE);
        permission.setBits(InfoItemPermission.BitsEnum.NUMBER_1);
        List<InfoItemPermission> permissions = Collections.singletonList(permission);
        updatePermissions(fileId, permissions);
    }

    public enum FolderType {

        PRIVATE("/Drive/My files/"),
        SHARED("/Drive/Shared files/"),
        PUBLIC("/Drive/Public files/");

        private final String rootPath;

        FolderType(String rootPath) {
            this.rootPath = rootPath;
        }

        /**
         * Gets the rootPath
         *
         * @return The rootPath
         */
        public String getRootPath() {
            return rootPath;
        }
    }


}
