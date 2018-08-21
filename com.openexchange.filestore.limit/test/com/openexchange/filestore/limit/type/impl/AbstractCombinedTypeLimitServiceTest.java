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

package com.openexchange.filestore.limit.type.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.File;
import com.openexchange.filestore.limit.exceptions.LimitExceptionCodes;

/**
 * {@link AbstractCombinedTypeLimitServiceTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.1
 */
public class AbstractCombinedTypeLimitServiceTest {

    private AbstractCombinedTypeLimitService service = new InfostoreLimitService() {

        @Override
        protected long getMaxUploadSizePerModule() {
            return 2;
        };
    };

    private List<File> files = new ArrayList<>();

    private List<OXException> exceptions = new ArrayList<>();

    private File file = new DefaultFile();

    @Before
    public void setUp() throws Exception {
        file.setFileName("Good to know");
        file.setFileSize(1);
        files.add(file);
    }

    @Test
    public void testCheckMaxUploadSizePerFile_filesNull_doNothing() {
        service.checkMaxUploadSizePerFile(null, exceptions);

        assertEquals(0, exceptions.size());
    }

    @Test
    public void testCheckMaxUploadSizePerFile_emptyArrays_doNothing() {
        service.checkMaxUploadSizePerFile(files, exceptions);

        assertEquals(0, exceptions.size());
    }

    @Test
    public void testCheckMaxUploadSizePerFile_fileExceedsLimitButExceptionsNull_doNothing() {
        file.setFileSize(3L);

        service.checkMaxUploadSizePerFile(files, null);

        //Just ensure that no NPE will be thrown
    }

    @Test
    public void testCheckMaxUploadSizePerFile_emptyFiles_keepExistingExceptions() {
        exceptions.add(OXException.general("bla"));

        service.checkMaxUploadSizePerFile(files, exceptions);

        assertEquals(1, exceptions.size());
        assertEquals(9999, exceptions.get(0).getCode());
    }

    @Test
    public void testCheckMaxUploadSizePerFile_oneFileThatDoesNotExceedLimit_keepEmptyExceptions() {
        service.checkMaxUploadSizePerFile(files, exceptions);

        assertEquals(0, exceptions.size());
    }

    @Test
    public void testCheckMaxUploadSizePerFile_oneFileThatDoesNotExceedLimit_keepExistingExceptions() {
        exceptions.add(OXException.general("bla"));

        service.checkMaxUploadSizePerFile(files, exceptions);

        assertEquals(1, exceptions.size());
        assertEquals(9999, exceptions.get(0).getCode());
    }

    @Test
    public void testCheckMaxUploadSizePerFile_oneFileThatDoesExceedLimit_addExceptions() {
        file.setFileSize(3L);
        service.checkMaxUploadSizePerFile(files, exceptions);

        assertEquals(1, exceptions.size());
    }

    @Test
    public void testCheckMaxUploadSizePerFile_oneFileThatDoesExceedLimit_addToExistingExceptions() {
        file.setFileSize(3L);
        exceptions.add(OXException.general("bla"));

        service.checkMaxUploadSizePerFile(files, exceptions);

        assertEquals(2, exceptions.size());
        boolean found = false;
        for (OXException oxException : exceptions) {
            if (oxException.getCode() == LimitExceptionCodes.FILE_QUOTA_PER_REQUEST_EXCEEDED.getNumber()) {
                found = true;
                break;
            }
        }
        assertTrue(found);
    }

    @Test
    public void testGetMaxUploadSize_definedPerModule_return() {
        long maxUploadSize = service.getMaxUploadSize();

        assertEquals(2, maxUploadSize);
    }

    @Test
    public void testGetMaxUploadSize_definedForServer_return() {
        service = new InfostoreLimitService() {

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
