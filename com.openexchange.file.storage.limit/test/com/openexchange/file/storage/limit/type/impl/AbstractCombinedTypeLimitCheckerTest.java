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

package com.openexchange.file.storage.limit.type.impl;

import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.limit.File;
import com.openexchange.file.storage.limit.exceptions.FileLimitExceptionCodes;

/**
 * {@link AbstractCombinedTypeLimitCheckerTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.1
 */
public class AbstractCombinedTypeLimitCheckerTest {

    private AbstractCombinedTypeLimitChecker service = new FileStorageLimitChecker() {

        @Override
        protected long getMaxUploadSizePerModule() {
            return 2;
        };
    };

    private List<File> files = new ArrayList<>();

    private File file = new File();

    @Before
    public void setUp() throws Exception {
        file.setName("Good to know");
        file.setSize(1);
        files.add(file);
    }

    @Test
    public void testCheckMaxUploadSizePerFile_filesNull_doNothing() {
        List<OXException> exceptions = service.checkMaxUploadSizePerFile(null);

        assertEquals(0, exceptions.size());
    }

    @Test
    public void testCheckMaxUploadSizePerFile_emptyArrays_doNothing() {
        List<OXException> exceptions = service.checkMaxUploadSizePerFile(Collections.emptyList());

        assertEquals(0, exceptions.size());
    }

    @Test
    public void testCheckMaxUploadSizePerFile_fileExceedsLimit_addException() {
        List<File> lFiles = new ArrayList<>();
        File file1 = new File();
        file1.setName("Good to know");
        file1.setSize(3);
        lFiles.add(file1);

        List<OXException> exceptions = service.checkMaxUploadSizePerFile(lFiles);

        assertEquals(1, exceptions.size());
    }

    @Test
    public void testCheckMaxUploadSizePerFile_multiplefFileExceedsLimit_addExceptions() {
        List<File> lFiles = new ArrayList<>();
        File file1 = new File();
        file1.setName("Good to know");
        file1.setSize(3);
        lFiles.add(file1);
        File file2 = new File();
        file2.setName("Blaaa");
        file2.setSize(7);
        lFiles.add(file2);

        List<OXException> exceptions = service.checkMaxUploadSizePerFile(lFiles);

        assertEquals(2, exceptions.size());
        int numberOfExceptionsWithCorrectCode = 0;
        for (OXException oxException : exceptions) {
            if (oxException.getCode() == FileLimitExceptionCodes.FILE_QUOTA_PER_REQUEST_EXCEEDED.getNumber()) {
                numberOfExceptionsWithCorrectCode++;
            }
        }
        assertEquals(2, numberOfExceptionsWithCorrectCode);
    }

    @Test
    public void testCheckMaxUploadSizePerFile_oneFileThatDoesNotExceedLimit_keepEmptyExceptions() {
        List<OXException> exceptions = service.checkMaxUploadSizePerFile(files);

        assertEquals(0, exceptions.size());
    }

    @Test
    public void testGetMaxUploadSize_definedPerModule_return() {
        long maxUploadSize = service.getMaxUploadSize();

        assertEquals(2, maxUploadSize);
    }

    @Test
    public void testGetMaxUploadSize_definedForServer_return() {
        service = new FileStorageLimitChecker() {

            @Override
            protected long getMaxUploadSizePerModule() {
                return -1;
            };

            @Override
            protected long getServerMaxUploadSize() {
                return 77;
            }
        };
        long maxUploadSize = service.getMaxUploadSize();

        assertEquals(77, maxUploadSize);
    }
}
