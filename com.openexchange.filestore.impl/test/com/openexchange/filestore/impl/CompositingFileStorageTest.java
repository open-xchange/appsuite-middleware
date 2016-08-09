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

package com.openexchange.filestore.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import junit.framework.TestCase;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorage;
import com.openexchange.sim.SimBuilder;

/**
 * {@link CompositingFileStorageTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class CompositingFileStorageTest extends TestCase {

    private Map<String, FileStorage> mapFor(String prefix, FileStorage fs) {
        return Collections.<String, FileStorage> singletonMap(prefix, fs);
    }

    public void testLookupWithoutPrefix() throws Exception {
        SimBuilder builder = new SimBuilder();
        builder.expectCall("getFile", "ab/cd/ef/12345");

        CompositingFileStorage cStore = new CompositingFileStorage(builder.getSim(FileStorage.class), null, mapFor("hash", new SimBuilder().getSim(FileStorage.class)));

        cStore.getFile("ab/cd/ef/12345");

        builder.assertAllWereCalled();
    }

    public void testLookupWithPrefix() throws Exception {
        SimBuilder builder = new SimBuilder();
        builder.expectCall("getFile", "ab/cd/ef/12345");

        CompositingFileStorage cStore = new CompositingFileStorage(new SimBuilder().getSim(FileStorage.class), null, mapFor("hash", builder.getSim(FileStorage.class)));

        cStore.getFile("hash/ab/cd/ef/12345");

        builder.assertAllWereCalled();

    }

    public void testSaveWithDefaultPrefix() throws Exception {

        InputStream is = new ByteArrayInputStream(new byte[] { 1 });


        SimBuilder builder = new SimBuilder();
        builder.expectCall("saveNewFile", is).andReturn("ab/cd/ef/12345");

        CompositingFileStorage cStore = new CompositingFileStorage(new SimBuilder().getSim(FileStorage.class), "hash", mapFor("hash", builder.getSim(FileStorage.class)));

        String fileId = cStore.saveNewFile(is);

        assertEquals("hash/ab/cd/ef/12345", fileId);

        builder.assertAllWereCalled();

    }

    public void testDeleteWithoutPrefix() throws Exception {

        SimBuilder builder = new SimBuilder();
        builder.expectCall("deleteFile", "ab/cd/ef/12345").andReturn(true);

        CompositingFileStorage cStore = new CompositingFileStorage(builder.getSim(FileStorage.class), null, mapFor("hash", new SimBuilder().getSim(FileStorage.class)));

        cStore.deleteFile("ab/cd/ef/12345");

        builder.assertAllWereCalled();
    }

    public void testDeleteWithPrefix() throws OXException {

        SimBuilder builder = new SimBuilder();
        builder.expectCall("deleteFile", "ab/cd/ef/12345").andReturn(true);

        CompositingFileStorage cStore = new CompositingFileStorage(new SimBuilder().getSim(FileStorage.class), null, mapFor("hash", builder.getSim(FileStorage.class)));

        cStore.deleteFile("hash/ab/cd/ef/12345");

        builder.assertAllWereCalled();
    }

    public void testBulkDeleteWithAndWithoutPrefix() throws OXException {

        SimFileStorage prefixedStorage = new SimFileStorage() {
            @Override
            public Set<String> deleteFiles(String[] arg0) throws OXException {
                assertEquals("ab/cd/ef/12345", arg0[0]);
                assertEquals("ab/cd/ef/54321", arg0[1]);

                remember(true);

                return new HashSet<String>(Arrays.asList("ab/cd/ef/12345"));
            }
        };

        SimFileStorage standardStorage = new SimFileStorage() {
            @Override
            public Set<String> deleteFiles(String[] arg0) throws OXException {
                assertEquals("ab/cd/ef/12345", arg0[0]);
                assertEquals("ab/cd/ef/54321", arg0[1]);

                remember(true);

                return new HashSet<String>(Arrays.asList("ab/cd/ef/54321"));
            }
        };

        CompositingFileStorage cStore = new CompositingFileStorage(standardStorage, null, mapFor("hash", prefixedStorage));

        Set<String> notDeleted = cStore.deleteFiles(new String[]{"ab/cd/ef/12345", "hash/ab/cd/ef/12345", "ab/cd/ef/54321", "hash/ab/cd/ef/54321"});

        assertEquals(notDeleted.size(), 2);
        assertTrue(notDeleted.contains("hash/ab/cd/ef/12345"));
        assertTrue(notDeleted.contains("ab/cd/ef/54321"));

        assertEquals(Boolean.TRUE, prefixedStorage.getMemory().get(0));
        assertEquals(Boolean.TRUE, standardStorage.getMemory().get(0));
    }

    public void testGetFileSizeWithoutPrefix() throws OXException {

        SimBuilder builder = new SimBuilder();
        builder.expectCall("getFileSize", "ab/cd/ef/12345").andReturn(12L);

        CompositingFileStorage cStore = new CompositingFileStorage(builder.getSim(FileStorage.class), null, mapFor("hash", new SimBuilder().getSim(FileStorage.class)));

        long fileSize = cStore.getFileSize("ab/cd/ef/12345");

        assertEquals(12L, fileSize);

        builder.assertAllWereCalled();
    }

    public void testGetFileSizeWithPrefix() throws OXException {

        SimBuilder builder = new SimBuilder();
        builder.expectCall("getFileSize", "ab/cd/ef/12345").andReturn(12L);

        CompositingFileStorage cStore = new CompositingFileStorage(new SimBuilder().getSim(FileStorage.class), null, mapFor("hash", builder.getSim(FileStorage.class)));

        long fileSize = cStore.getFileSize("hash/ab/cd/ef/12345");

        assertEquals(12L, fileSize);

        builder.assertAllWereCalled();
    }

    public void testGetMimeTypeOnStandardFS() throws OXException {

        SimBuilder builder = new SimBuilder();
        builder.expectCall("getMimeType", "TestFile.odt").andReturn("text/odt");

        CompositingFileStorage cStore = new CompositingFileStorage(builder.getSim(FileStorage.class), null, null);

        String mimeType = cStore.getMimeType("TestFile.odt");
        assertEquals("text/odt", mimeType);

        builder.assertAllWereCalled();
    }

    public void testCompositeFileList() throws OXException {

        SimBuilder prefixedBuilder = new SimBuilder();
        prefixedBuilder.expectCall("getFileList").andReturn(new TreeSet<String>(Arrays.asList("ab/cd/ef/12345")));

        SimBuilder standardBuilder = new SimBuilder();
        standardBuilder.expectCall("getFileList").andReturn(new TreeSet<String>(Arrays.asList("ab/cd/ef/54321")));

        CompositingFileStorage cStore = new CompositingFileStorage(standardBuilder.getSim(FileStorage.class), null, mapFor("hash", prefixedBuilder.getSim(FileStorage.class)));

        SortedSet<String> fileList = cStore.getFileList();

        assertEquals(2, fileList.size());
        assertTrue(fileList.contains("hash/ab/cd/ef/12345"));
        assertTrue(fileList.contains("ab/cd/ef/54321"));

        prefixedBuilder.assertAllWereCalled();
        standardBuilder.assertAllWereCalled();
    }

    public void testRemoveIsMultiplexed() throws OXException {

        SimBuilder prefixedBuilder = new SimBuilder();
        prefixedBuilder.expectCall("remove");

        SimBuilder standardBuilder = new SimBuilder();
        standardBuilder.expectCall("remove");

        CompositingFileStorage cStore = new CompositingFileStorage(standardBuilder.getSim(FileStorage.class), null, mapFor("hash", prefixedBuilder.getSim(FileStorage.class)));

        cStore.remove();

        prefixedBuilder.assertAllWereCalled();
        standardBuilder.assertAllWereCalled();
    }

    public void testIsStateFileCorrect() throws OXException {

        SimBuilder prefixedBuilder = new SimBuilder();
        prefixedBuilder.expectCall("stateFileIsCorrect").andReturn(false);

        SimBuilder standardBuilder = new SimBuilder();
        standardBuilder.expectCall("stateFileIsCorrect").andReturn(true);

        CompositingFileStorage cStore = new CompositingFileStorage(standardBuilder.getSim(FileStorage.class), null, mapFor("hash", prefixedBuilder.getSim(FileStorage.class)));

        boolean stateFileIsCorrect = cStore.stateFileIsCorrect();

        assertFalse(stateFileIsCorrect);

        prefixedBuilder.assertAllWereCalled();
        standardBuilder.assertAllWereCalled();

    }

    public void testIsStateFileCorrect2() throws OXException {

        SimBuilder prefixedBuilder = new SimBuilder();
        prefixedBuilder.expectCall("stateFileIsCorrect").andReturn(true);

        SimBuilder standardBuilder = new SimBuilder();
        standardBuilder.expectCall("stateFileIsCorrect").andReturn(true);

        CompositingFileStorage cStore = new CompositingFileStorage(standardBuilder.getSim(FileStorage.class), null, mapFor("hash", prefixedBuilder.getSim(FileStorage.class)));

        boolean stateFileIsCorrect = cStore.stateFileIsCorrect();

        assertTrue(stateFileIsCorrect);

        prefixedBuilder.assertAllWereCalled();
        standardBuilder.assertAllWereCalled();

    }

    public void testRecreateStateFile() throws OXException {

        SimBuilder prefixedBuilder = new SimBuilder();
        prefixedBuilder.expectCall("recreateStateFile");

        SimBuilder standardBuilder = new SimBuilder();
        standardBuilder.expectCall("recreateStateFile");

        CompositingFileStorage cStore = new CompositingFileStorage(standardBuilder.getSim(FileStorage.class), null, mapFor("hash", prefixedBuilder.getSim(FileStorage.class)));

        cStore.recreateStateFile();

        prefixedBuilder.assertAllWereCalled();
        standardBuilder.assertAllWereCalled();
    }

    private class SimFileStorage implements FileStorage {

        protected List<Object> remember = new ArrayList<Object>();

        @Override
        public boolean deleteFile(String identifier) throws OXException {
            return false;
        }

        @Override
        public Set<String> deleteFiles(String[] identifiers) throws OXException {
            return null;
        }

        @Override
        public InputStream getFile(String name) throws OXException {
            return null;
        }

        @Override
        public SortedSet<String> getFileList() throws OXException {
            return null;
        }

        @Override
        public long getFileSize(String name) throws OXException {
            return 0;
        }

        @Override
        public String getMimeType(String name) throws OXException {
            return null;
        }

        @Override
        public void recreateStateFile() throws OXException {

        }

        @Override
        public void remove() throws OXException {

        }

        @Override
        public String saveNewFile(InputStream file) throws OXException {
            return null;
        }

        @Override
        public boolean stateFileIsCorrect() throws OXException {
            return false;
        }

        protected void remember(Object o) {
            remember.add(o);
        }


        public List<Object> getMemory() {
            return remember;
        }

        @Override
        public long appendToFile(InputStream file, String name, long offset) throws OXException {
            return 0;
        }

        @Override
        public void setFileLength(long length, String name) throws OXException {
        }

        @Override
        public InputStream getFile(String name, long offset, long length) throws OXException {
            return null;
        }
    }
}
