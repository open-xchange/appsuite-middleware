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

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.json.JSONException;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.infostore.apiclient.FileMovePermissionWarningTest.FolderType;
import com.openexchange.ajax.parser.ResponseParser;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.test.TestClassConfig;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.FolderBody;
import com.openexchange.testing.httpclient.models.FolderData;
import com.openexchange.testing.httpclient.models.FolderResponse;
import com.openexchange.testing.httpclient.models.FolderUpdateResponse;

/**
 *
 * {@link AbstractFolderMoveWarningTest}
 *
 * @author <a href="mailto:anna.ottersbach@open-xchange.com">Anna Ottersbach</a>
 * @since v7.10.5
 */
public abstract class AbstractFolderMoveWarningTest extends AbstractFolderMovePermissionsTest {

    protected Integer userId3;

    protected AbstractFolderMoveWarningTest(String type) {
        super(type);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        userId3 = I(testContext.acquireUser().getUserId());
    }

    @Override
    public TestClassConfig getTestConfig() {
        return TestClassConfig.builder().createApiClient().withUserPerContext(3).build();
    }

    protected void checkFolderMove(String sourceFolder, String destinationFolder, FolderExceptionErrorMessage warning, FolderType sourceType, FolderType targetType) throws ApiException, JSONException {
        String folderToMove = createChildFolder(sourceFolder);
        checkFolderMove(folderToMove, sourceFolder, destinationFolder, warning.create(getWarningParameters(folderToMove, sourceFolder, destinationFolder, sourceType, targetType)));
    }

    protected void checkFolderMove(String folderToMove, String sourceFolder, String destinationFolder, OXException expectedWarning) throws ApiException, JSONException {
        FolderUpdateResponse response = moveFolder(folderToMove, destinationFolder, null);

        checkResponseForWarning(response, expectedWarning);

        checkParentFolder(folderToMove, sourceFolder);

        FolderUpdateResponse responseIgnored = moveFolder(folderToMove, destinationFolder, Boolean.TRUE);
        checkResponseForWarning(responseIgnored);

        checkParentFolder(folderToMove, destinationFolder);
    }

    protected void checkParentFolder(String folderToCheck, String expectedParentFolder) throws ApiException {
        FolderResponse folderReponse = api.getFolder(folderToCheck, TREE, null, null);
        assertNotNull(folderReponse);
        FolderData data = folderReponse.getData();
        assertNotNull(data);
        assertEquals("Not the expected parent folder.", expectedParentFolder, data.getFolderId());
    }

    protected void checkRequestWithIgnoreWarnings(String toMoveFolderId, String destinationFolder) throws ApiException {
        FolderUpdateResponse responseIgnored = moveFolder(toMoveFolderId, destinationFolder, Boolean.TRUE);
        assertNotNull(responseIgnored);
        assertNull(responseIgnored.getError());
        checkParentFolder(toMoveFolderId, destinationFolder);
    }

    protected void checkResponseForWarning(FolderUpdateResponse response, OXException expectedWarning) throws JSONException {
        assertNotNull(response);
        assertNotNull(response.getError());
        Response parsedResponse = ResponseParser.parse(response.toJson());
        assertTrue(parsedResponse.hasWarnings());
        boolean result = false;
        for (OXException warning : parsedResponse.getWarnings()) {
            if (warning.getErrorCode().contentEquals(expectedWarning.getErrorCode()) && warning.getPlainLogMessage().equals(expectedWarning.getPlainLogMessage())) {
                result = true;
            }
        }
        assertTrue("Excepected: \"" + expectedWarning.getMessage() + "\"\n, but warnings only contains : \"" + parsedResponse.getWarnings().toString() + "\"", result);
    }

    protected void checkResponseForWarning(FolderUpdateResponse response) {
        assertNotNull(response);
        assertNotNull(response.getError());
        assertEquals("Warning expected, but no error.", Category.CATEGORY_WARNING.toString(), response.getCategories());

    }

    protected String createPrivateFolder(Integer sharedToUserId) throws Exception {
        return createNewFolder(sharedToUserId, BITS_REVIEWER, false, true);
    }

    protected String getFolderName(String folderId) throws ApiException {
        FolderResponse folder = api.getFolder(folderId, TREE, null, null);
        return folder.getData().getTitle();
    }

    protected FolderUpdateResponse moveFolder(String folderToMove, String destinationFolder, Boolean ignoreWarnings) throws ApiException {
        FolderBody folderBody = new FolderBody();
        FolderData folderData = new FolderData();
        folderData.setFolderId(destinationFolder);
        folderData.setPermissions(null);
        folderBody.setFolder(folderData);

        FolderUpdateResponse response = api.updateFolder(folderToMove, folderBody, Boolean.FALSE, L(System.currentTimeMillis()), TREE, null, Boolean.FALSE, null, Boolean.FALSE, ignoreWarnings);
        return response;
    }

    protected Object[] getWarningParameters(String sourceFolder, String targetFolder, FolderType sourceType, FolderType targetType) throws ApiException {
         return getWarningParameters(null, sourceFolder, targetFolder, sourceType, targetType);
    }

    protected Object[] getWarningParameters(String folderToMove, String sourceFolder, String targetFolder, FolderType sourceType, FolderType targetType) throws ApiException {
        String folderPath = folderToMove != null ? getPath(folderToMove, sourceFolder, sourceType) : "";
        String sourceFolderPath = getPath(sourceFolder, sourceType);
        String targetFolderPath = getPath(targetFolder, targetType);
        Object folderId = folderToMove;
        Object targetFolderId = targetFolder;
        return new Object[] { folderPath, sourceFolderPath, targetFolderPath, folderId, targetFolderId };
    }

    private String getPath(String sourceFolder, FolderType sourceType) throws ApiException {
        return getPath(null, sourceFolder, sourceType);
    }

    private String getPath(String folderToMove, String sourceFolder, FolderType sourceType) throws ApiException {
        return sourceType.getRootPath() + (sourceType.equals(FolderType.SHARED) ? getFolderName(getPrivateInfostoreFolder(testUser2.getApiClient())) + "/" : "") + getFolderName(sourceFolder) + (folderToMove != null ? "/" + getFolderName(folderToMove) : "");
    }
}
