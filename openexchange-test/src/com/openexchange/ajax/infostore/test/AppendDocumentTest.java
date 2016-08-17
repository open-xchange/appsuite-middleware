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

package com.openexchange.ajax.infostore.test;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.junit.Assert;
import org.junit.Test;
import com.openexchange.ajax.infostore.actions.GetDocumentRequest;
import com.openexchange.ajax.infostore.actions.GetDocumentResponse;
import com.openexchange.ajax.infostore.actions.UpdateInfostoreRequest;
import com.openexchange.ajax.infostore.actions.UpdateInfostoreResponse;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.java.Streams;
import com.openexchange.java.util.UUIDs;

/**
 * {@link AppendDocumentTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.1
 */
public class AppendDocumentTest extends AbstractInfostoreTest {

    /**
     * Initializes a new {@link AppendDocumentTest}.
     *
     * @param name The test name
     */
    public AppendDocumentTest(String name) {
        super(name);
    }

    @Test
    public void testAppendFile() throws Exception {
        /*
         * prepare chunks to upload
         */
        String folderId = String.valueOf(client.getValues().getPrivateInfostoreFolder());
        long expectedLength = 0;
        List<byte[]> chunks = new ArrayList<byte[]>();
        for (int i = 0; i < 10; i++) {
            byte[] chunk = UUIDs.toByteArray(UUID.randomUUID());
            chunks.add(chunk);
            expectedLength += chunk.length;
        }
        byte[] expectedData = new byte[(int) expectedLength];
        {
            int offset = 0;
            for (byte[] chunk : chunks) {
                System.arraycopy(chunk, 0, expectedData, offset, chunk.length);
                offset += chunk.length;
            }
        }
        /*
         * create initial infostore item
         */
        long offset;
        Date timestamp;
        File document;
        java.io.File tempFile = null;
        try {
            FileOutputStream outputStream = null;
            try {
                tempFile = java.io.File.createTempFile("test", "bin");
                tempFile.deleteOnExit();
                outputStream = new FileOutputStream(tempFile);
                outputStream.write(chunks.get(0));
                outputStream.flush();
            } finally {
                Streams.close(outputStream);
            }
            document = new DefaultFile();
            document.setFolderId(folderId);
            document.setTitle(tempFile.getName());
            document.setFileName(tempFile.getName());
            document.setVersion(String.valueOf(1));
            document.setFileSize(tempFile.length());
            infoMgr.newAction(document, tempFile);
            timestamp = infoMgr.getLastResponse().getTimestamp();
            offset = tempFile.length();
        } finally {
            if (null != tempFile) {
                tempFile.delete();
            }
        }
        /*
         * append further chunks
         */
        for (int i = 1; i < chunks.size(); i++) {
            try {
                FileOutputStream outputStream = null;
                try {
                    tempFile = java.io.File.createTempFile("test", "bin");
                    tempFile.deleteOnExit();
                    outputStream = new FileOutputStream(tempFile);
                    outputStream.write(chunks.get(i));
                    outputStream.flush();
                } finally {
                    Streams.close(outputStream);
                }
                UpdateInfostoreRequest updateRequest = new UpdateInfostoreRequest(document, new Field[0], tempFile, timestamp);
                updateRequest.setOffset(offset);
                UpdateInfostoreResponse updateResponse = getClient().execute(updateRequest);
                assertFalse(updateResponse.hasError());
                timestamp = updateResponse.getTimestamp();
                offset += tempFile.length();
            } finally {
                if (null != tempFile) {
                    tempFile.delete();
                }
            }
        }
        /*
         * verify file
         */
        GetDocumentRequest getRequest = new GetDocumentRequest(folderId, document.getId());
        GetDocumentResponse getResponse = getClient().execute(getRequest);
        byte[] contents = getResponse.getContentAsByteArray();
        assertNotNull(contents);
        assertEquals((int) expectedLength, contents.length);
        Assert.assertArrayEquals(expectedData, contents);
    }

    @Test
    public void testAppendWithWrongOffsetFile() throws Exception {
        /*
         * prepare chunks to upload
         */
        String folderId = String.valueOf(client.getValues().getPrivateInfostoreFolder());
        long expectedLength = 0;
        List<byte[]> chunks = new ArrayList<byte[]>();
        for (int i = 0; i < 10; i++) {
            byte[] chunk = UUIDs.toByteArray(UUID.randomUUID());
            chunks.add(chunk);
            expectedLength += chunk.length;
        }
        byte[] expectedData = new byte[(int) expectedLength];
        {
            int offset = 0;
            for (byte[] chunk : chunks) {
                System.arraycopy(chunk, 0, expectedData, offset, chunk.length);
                offset += chunk.length;
            }
        }
        /*
         * create initial infostore item
         */
        long offset;
        Date timestamp;
        File document;
        java.io.File tempFile = null;
        try {
            FileOutputStream outputStream = null;
            try {
                tempFile = java.io.File.createTempFile("test", "bin");
                tempFile.deleteOnExit();
                outputStream = new FileOutputStream(tempFile);
                outputStream.write(UUIDs.toByteArray(UUID.randomUUID()));
                outputStream.flush();
            } finally {
                Streams.close(outputStream);
            }
            document = new DefaultFile();
            document.setFolderId(folderId);
            document.setTitle(tempFile.getName());
            document.setFileName(tempFile.getName());
            document.setVersion(String.valueOf(1));
            document.setFileSize(tempFile.length());
            infoMgr.newAction(document, tempFile);
            timestamp = infoMgr.getLastResponse().getTimestamp();
            offset = tempFile.length();
        } finally {
            if (null != tempFile) {
                tempFile.delete();
            }
        }
        /*
         * try to append next chunk with wrong offset
         */
        try {
            FileOutputStream outputStream = null;
            try {
                tempFile = java.io.File.createTempFile("test", "bin");
                tempFile.deleteOnExit();
                outputStream = new FileOutputStream(tempFile);
                outputStream.write(UUIDs.toByteArray(UUID.randomUUID()));
                outputStream.flush();
            } finally {
                Streams.close(outputStream);
            }
            UpdateInfostoreRequest updateRequest = new UpdateInfostoreRequest(document, new Field[0], tempFile, timestamp);
            updateRequest.setOffset(offset + 12);
            updateRequest.setFailOnError(false);
            UpdateInfostoreResponse updateResponse = getClient().execute(updateRequest);
            assertTrue(updateResponse.hasError());
            assertEquals("FLS-0019", updateResponse.getException().getErrorCode());
        } finally {
            if (null != tempFile) {
                tempFile.delete();
            }
        }
    }

}
