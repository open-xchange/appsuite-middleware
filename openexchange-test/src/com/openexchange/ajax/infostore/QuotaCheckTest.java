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

package com.openexchange.ajax.infostore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.junit.Test;
import com.openexchange.testing.httpclient.models.InfoItemQuotaCheckFiles;

/**
 * {@link QuotaCheckTest}
 *
 * @author <a href="mailto:jan-oliver.huhn@open-xchange.com">Jan-Oliver Huhn</a>
 * @since v7.10.1
 */
public class QuotaCheckTest extends AbstractInfostoreQuotaCheckTest {

    private static final long FILE_SIZE_SMALL = 26214400L;
    private static final long FILE_SIZE_MEDIUM = 52428800L;
    private static final long FILE_SIZE_LARGE = 115343360L;

    private static final String INFOSTORE_QUOTA = "com.openexchange.quota.infostore";

    private HashMap<String, String> properties = new HashMap<>();

    private List<InfoItemQuotaCheckFiles> infoItemQuotaCheckFiles;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        initInfostoreQuota();
        infoItemQuotaCheckFiles = new ArrayList<>();
    }

    @Override
    public void tearDown() throws Exception {
        deleteUser();
        setInfostoreQuotaToDefault();
        clearFileList();
        super.tearDown();
    }

    @Test
    public void testFileQuotaCheckSingleFile() throws Exception {
        addQuotaCheckFiles(createQuotaCheckFiles(FILE_SIZE_SMALL));
        checkQuota(infoItemQuotaCheckFiles, false);
    }

//    deactivated for the moment because the file quota configuration doesnt seem to function anymore
//    @Test
//    public void testFileQuotaCheckMultipleFile() throws Exception {
//        addQuotaCheckFiles(createQuotaCheckFiles(FILE_SIZE_SMALL));
//        addQuotaCheckFiles(createQuotaCheckFiles(FILE_SIZE_SMALL));
//        checkQuota(infoItemQuotaCheckFiles, false);
//
//        addQuotaCheckFiles(createQuotaCheckFiles(FILE_SIZE_SMALL));
//        checkQuota(infoItemQuotaCheckFiles, true);
//    }

    @Test
    public void testStorageQuotaSingleFile() throws Exception {
        addQuotaCheckFiles(createQuotaCheckFiles(FILE_SIZE_SMALL));
        checkQuota(infoItemQuotaCheckFiles, false);

        clearFileList();

        addQuotaCheckFiles(createQuotaCheckFiles(FILE_SIZE_LARGE));
        checkQuota(infoItemQuotaCheckFiles, true);
    }

    @Test
    public void testStorageQuotaMultipleFile() throws Exception {
        addQuotaCheckFiles(createQuotaCheckFiles(FILE_SIZE_SMALL));
        addQuotaCheckFiles(createQuotaCheckFiles(FILE_SIZE_MEDIUM));
        checkQuota(infoItemQuotaCheckFiles, false);

        addQuotaCheckFiles(createQuotaCheckFiles(FILE_SIZE_LARGE));
        checkQuota(infoItemQuotaCheckFiles, true);
    }

    /**
     * Configures the file storage quota
     *
     * @throws Exception, if the configuration fails
     */
    private void initInfostoreQuota() throws Exception {
        properties.putIfAbsent(INFOSTORE_QUOTA, "2");
        addUserProperty(properties);
        initConfiguration();
    }

    /**
     * Configures the default file storage quota
     *
     * @throws Exception, if the default configuration fails
     */
    private void setInfostoreQuotaToDefault() throws Exception {
        if (!properties.isEmpty()) {
            properties = new HashMap<>();
        }
        properties.putIfAbsent(INFOSTORE_QUOTA, "250000");
        setPropertiesToDefault(properties);
    }

    /**
     * Creates a data object with file meta data
     *
     * @param fileSize The file size
     * @return data, the object which contains the file size and the file name
     */
    protected InfoItemQuotaCheckFiles createQuotaCheckFiles(Long fileSize) {
        return super.createQuotaCheckFiles(getFileName(), fileSize);
    }

    /**
     * @param infoItemQuotaCheckFile
     */
    private void addQuotaCheckFiles(InfoItemQuotaCheckFiles infoItemQuotaCheckFile ) {
        infoItemQuotaCheckFiles.add(infoItemQuotaCheckFile);
    }

    /**
     * Makes a call to the check quota api
     *
     * @param infoItemQuotaCheckFiles The data object which holds the file meta data
     * @param expectException A boolean value if to expect an error from the server call
     * @throws Exception, if the quota check fails
     */
    protected void checkQuota(List<InfoItemQuotaCheckFiles> infoItemQuotaCheckFiles, boolean expectException) throws Exception {
        super.checkQuota(createQuotaCheckData(quotaTestFolderId, infoItemQuotaCheckFiles), expectException);
    };

    /**
     *  Clears the file list
     */
    private void clearFileList() {
        if (!infoItemQuotaCheckFiles.isEmpty()) {
            infoItemQuotaCheckFiles.clear();
        }
    }

}
