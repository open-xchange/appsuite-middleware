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

package com.openexchange.ajax.infostore.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
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

    @Test
    public void testAppendFile() throws Exception {
        /*
         * prepare chunks to upload
         */
        String folderId = String.valueOf(getClient().getValues().getPrivateInfostoreFolder());
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
            itm.newAction(document, tempFile);
            timestamp = itm.getLastResponse().getTimestamp();
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
        String folderId = String.valueOf(getClient().getValues().getPrivateInfostoreFolder());
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
            itm.newAction(document, tempFile);
            timestamp = itm.getLastResponse().getTimestamp();
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
