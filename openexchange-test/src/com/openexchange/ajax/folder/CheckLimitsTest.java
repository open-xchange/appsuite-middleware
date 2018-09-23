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

package com.openexchange.ajax.folder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import com.openexchange.testing.httpclient.models.FolderCheckLimitsFiles;
import com.openexchange.testing.httpclient.models.FolderCheckLimitsResponse;
import edu.emory.mathcs.backport.java.util.Collections;

/**
 * {@link CheckLimitsTest}
 *
 * @author <a href="mailto:jan-oliver.huhn@open-xchange.com">Jan-Oliver Huhn</a>
 * @since v7.10.1
 */
public class CheckLimitsTest extends AbstractFolderCheckLimitTest {

    private static final long FILE_SIZE_SMALL = 9437184L; // 9MB
    private static final long FILE_SIZE_MEDIUM = 31457280L; // 30MB
    private static final long FILE_SIZE_LARGE = 115343360L;

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
        FolderCheckLimitsResponse checkLimits = checkLimits(createQuotaCheckData(infoItemQuotaCheckFiles), quotaTestFolderId, "filestorage");

        infoItemQuotaCheckFiles.add(createQuotaCheckFiles(FILE_SIZE_LARGE));
        checkLimits = checkLimits(createQuotaCheckData(infoItemQuotaCheckFiles), quotaTestFolderId, "filestorage");
        System.out.println();
    }

    @Test
    public void testFileQuotaCheckSingleFile_unknownType_returnException() throws Exception {
        List<FolderCheckLimitsFiles> infoItemQuotaCheckFiles = Collections.singletonList(createQuotaCheckFiles(FILE_SIZE_MEDIUM));
        FolderCheckLimitsResponse checkLimits = checkLimits(createQuotaCheckData(infoItemQuotaCheckFiles), quotaTestFolderId, "bla");

        assertEquals("UPL-LIM-4001", checkLimits.getCode());
        assertNull(checkLimits.getData());
    }

    @Test
    public void testFileQuotaCheckSingleFile_unknownFolder_returnException() throws Exception {
        List<FolderCheckLimitsFiles> infoItemQuotaCheckFiles = Collections.singletonList(createQuotaCheckFiles(FILE_SIZE_MEDIUM));
        FolderCheckLimitsResponse checkLimits = checkLimits(createQuotaCheckData(infoItemQuotaCheckFiles), "19191911", "filestorage");

        assertEquals("FILE_STORAGE-0029", checkLimits.getCode());
        assertNull(checkLimits.getData());
    }

}
