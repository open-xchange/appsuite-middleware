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

import static com.openexchange.java.Autoboxing.L;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import com.openexchange.testing.httpclient.models.FolderCheckLimitsFiles;
import com.openexchange.testing.httpclient.models.FolderCheckLimitsResponse;

/**
 * {@link CheckLimitsTest}
 *
 * @author <a href="mailto:jan-oliver.huhn@open-xchange.com">Jan-Oliver Huhn</a>
 * @since v7.10.1
 */
public class CheckLimitsTest extends AbstractFolderCheckLimitTest {

    private static final Long FILE_SIZE_SMALL = L(9437184); // 9MB
    private static final Long FILE_SIZE_MEDIUM = L(31457280); // 30MB
    private static final Long FILE_SIZE_LARGE = L(115343360);

    @Test
    public void testFileQuotaCheckSingleFile_noLimitExceeded() throws Exception {
        List<FolderCheckLimitsFiles> infoItemQuotaCheckFiles = Collections.singletonList(createQuotaCheckFiles(FILE_SIZE_SMALL));
        FolderCheckLimitsResponse checkLimits = checkLimits(createQuotaCheckData(infoItemQuotaCheckFiles), quotaTestFolderId, "filestorage");

        assertEquals(0, checkLimits.getData().getErrors().size());
    }

    @Test
    public void testFileQuotaCheckSingleFile_maxUploadExceeded() throws Exception {
        List<FolderCheckLimitsFiles> infoItemQuotaCheckFiles = Collections.singletonList(createQuotaCheckFiles(FILE_SIZE_MEDIUM));
        FolderCheckLimitsResponse checkLimits = checkLimits(createQuotaCheckData(infoItemQuotaCheckFiles), quotaTestFolderId, "filestorage");

        assertEquals(1, checkLimits.getData().getErrors().size());
    }

    @Test
    public void testFileQuotaCheckMultipleFiles_maxUploadSizeForMediumExceeded() throws Exception {
        List<FolderCheckLimitsFiles> infoItemQuotaCheckFiles = new ArrayList<>();
        infoItemQuotaCheckFiles.add(createQuotaCheckFiles(FILE_SIZE_MEDIUM));
        infoItemQuotaCheckFiles.add(createQuotaCheckFiles(FILE_SIZE_SMALL));

        FolderCheckLimitsResponse checkLimits = checkLimits(createQuotaCheckData(infoItemQuotaCheckFiles), quotaTestFolderId, "filestorage");

        assertEquals(1, checkLimits.getData().getErrors().size());
    }

    @Test
    public void testFileQuotaCheckMultipleFiles_bothExceedMaxUploadOf10() throws Exception {
        List<FolderCheckLimitsFiles> infoItemQuotaCheckFiles = new ArrayList<>();
        infoItemQuotaCheckFiles.add(createQuotaCheckFiles(FILE_SIZE_MEDIUM));
        infoItemQuotaCheckFiles.add(createQuotaCheckFiles(FILE_SIZE_LARGE));

        FolderCheckLimitsResponse checkLimits = checkLimits(createQuotaCheckData(infoItemQuotaCheckFiles), quotaTestFolderId, "filestorage");

        assertEquals(3, checkLimits.getData().getErrors().size());
    }

    @Test
    public void testStorageQuotaMultipleFile() throws Exception {
        List<FolderCheckLimitsFiles> infoItemQuotaCheckFiles = new ArrayList<>();
        infoItemQuotaCheckFiles.add(createQuotaCheckFiles(FILE_SIZE_SMALL));
        infoItemQuotaCheckFiles.add(createQuotaCheckFiles(FILE_SIZE_MEDIUM));
        checkLimits(createQuotaCheckData(infoItemQuotaCheckFiles), quotaTestFolderId, "filestorage");

        infoItemQuotaCheckFiles.add(createQuotaCheckFiles(FILE_SIZE_LARGE));
        checkLimits(createQuotaCheckData(infoItemQuotaCheckFiles), quotaTestFolderId, "filestorage");
    }

    @Test
    public void testFileQuotaCheckSingleFile_unknownType_returnException() throws Exception {
        List<FolderCheckLimitsFiles> infoItemQuotaCheckFiles = Collections.singletonList(createQuotaCheckFiles(FILE_SIZE_MEDIUM));
        FolderCheckLimitsResponse checkLimits = checkLimits(createQuotaCheckData(infoItemQuotaCheckFiles), quotaTestFolderId, "bla");

        assertEquals("FILE-LIMIT-4001", checkLimits.getCode());
        assertNull(checkLimits.getData());
    }

    @Test
    public void testFileQuotaCheckSingleFile_unknownFolder_returnException() throws Exception {
        List<FolderCheckLimitsFiles> infoItemQuotaCheckFiles = Collections.singletonList(createQuotaCheckFiles(FILE_SIZE_MEDIUM));
        FolderCheckLimitsResponse checkLimits = checkLimits(createQuotaCheckData(infoItemQuotaCheckFiles), "19191911", "filestorage");

        assertEquals("FILE_STORAGE-0029", checkLimits.getCode());
        assertNull(checkLimits.getData());
    }

    @Test
    public void testFileQuota_bug622059_fileQuotaSetWithoutLimit_doNotReturnError() throws Exception {
        user.setUserAttribute("config", "com.openexchange.quota.infostore", "0");
        iface.change(context, user, credentials);
        List<FolderCheckLimitsFiles> infoItemQuotaCheckFiles = Collections.singletonList(createQuotaCheckFiles(FILE_SIZE_SMALL));
        FolderCheckLimitsResponse checkLimits = checkLimits(createQuotaCheckData(infoItemQuotaCheckFiles), quotaTestFolderId, "filestorage");

        assertEquals(0, checkLimits.getData().getErrors().size());
    }

}
