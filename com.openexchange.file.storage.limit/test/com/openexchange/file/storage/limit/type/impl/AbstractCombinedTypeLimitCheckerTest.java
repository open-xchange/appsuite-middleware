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

package com.openexchange.file.storage.limit.type.impl;

import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.limit.LimitFile;
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

    private List<LimitFile> files = new ArrayList<>();

    private LimitFile file = new LimitFile();

    @Before
    public void setUp() {
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
        List<LimitFile> lFiles = new ArrayList<>();
        LimitFile file1 = new LimitFile();
        file1.setName("Good to know");
        file1.setSize(3);
        lFiles.add(file1);

        List<OXException> exceptions = service.checkMaxUploadSizePerFile(lFiles);

        assertEquals(1, exceptions.size());
    }

    @Test
    public void testCheckMaxUploadSizePerFile_multiplefFileExceedsLimit_addExceptions() {
        List<LimitFile> lFiles = new ArrayList<>();
        LimitFile file1 = new LimitFile();
        file1.setName("Good to know");
        file1.setSize(3);
        lFiles.add(file1);
        LimitFile file2 = new LimitFile();
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
