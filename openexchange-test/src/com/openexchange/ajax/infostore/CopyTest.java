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

package com.openexchange.ajax.infostore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.json.JSONException;
import org.junit.Test;
import com.google.common.collect.Iterables;
import com.openexchange.ajax.InfostoreAJAXTest;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.test.common.test.OXTestToolkit;
import com.openexchange.test.common.test.TestInit;

public class CopyTest extends InfostoreAJAXTest {

    private final Set<String> skipKeys = new HashSet<String>(Arrays.asList(Metadata.ID_LITERAL.getName(), Metadata.CREATION_DATE_LITERAL.getName(), Metadata.LAST_MODIFIED_LITERAL.getName(), Metadata.LAST_MODIFIED_UTC_LITERAL.getName(), Metadata.VERSION_LITERAL.getName(), Metadata.CURRENT_VERSION_LITERAL.getName(), Metadata.SEQUENCE_NUMBER_LITERAL.getName(), Metadata.CONTENT_LITERAL.getName()));

    @Test
    public void testCopy() throws Exception {
        final String objectId = Iterables.get(itm.getCreatedEntities(), 0).getId();

        com.openexchange.file.storage.File file = itm.getAction(objectId);
        itm.copyAction(objectId, Integer.toString(folderId), file);
        final String id = file.getId();

        com.openexchange.file.storage.File orig = itm.getAction(objectId);
        com.openexchange.file.storage.File copy = itm.getAction(id);

        Map<String, Object> meta = orig.getMeta();
        for (Map.Entry<String, Object> entry : meta.entrySet()) {
            String key = entry.getKey();
            if (!skipKeys.contains(key)) {
                assertEquals(key + " seems to have a wrong value", orig.getMeta().get(key).toString(), copy.getMeta().get(key).toString());
            }
        }
    }

    @Test
    public void testCopyFile() throws Exception {
        final File upload = new File(TestInit.getTestProperty("ajaxPropertiesFile"));

        com.openexchange.file.storage.File data = createFile(folderId, "test upload");
        data.setFileMIMEType("text/plain");
        itm.newAction(data, upload);

        String id = data.getId();
        //FIXME Bug 4120
        com.openexchange.file.storage.File reload = itm.getAction(id);
        reload.setFileName("other.properties");

        final String objectId = Iterables.get(itm.getCreatedEntities(), 0).getId();
        itm.copyAction(objectId, Integer.toString(folderId), reload);
        final String copyId = reload.getId();

        com.openexchange.file.storage.File file = itm.getAction(id);

        com.openexchange.file.storage.File copy = itm.getAction(copyId);

        assertEquals("other.properties", copy.getFileName());
        assertEquals(file.getFileSize(), copy.getFileSize());
        assertEquals(file.getFileMIMEType(), copy.getFileMIMEType());

        InputStream is = null;
        InputStream is2 = null;
        try {
            is = new FileInputStream(upload);
            is2 = itm.document(Integer.toString(folderId), copyId, "1");

            OXTestToolkit.assertSameContent(is, is2);
        } finally {
            if (is != null) {
                is.close();
            }
            if (is2 != null) {
                is2.close();
            }
        }
    }

    @Test
    public void testModifyingCopy() throws Exception {
        final String id = Iterables.get(itm.getCreatedEntities(), 0).getId();

        com.openexchange.file.storage.File file = itm.getAction(id);
        file.setTitle("copy");

        itm.copyAction(id, Integer.toString(folderId), file);
        final String copyId = file.getId();

        com.openexchange.file.storage.File orig = itm.getAction(id);
        com.openexchange.file.storage.File copy = itm.getAction(copyId);

        Map<String, Object> meta = orig.getMeta();
        for (Map.Entry<String, Object> entry : meta.entrySet()) {
            String key = entry.getKey();
            if (!skipKeys.contains(key) && !key.equals("title")) {
                assertEquals(key + " seems to have a wrong value", orig.getMeta().get(key).toString(), copy.getMeta().get(key).toString());
            } else if (key.equals("title")) {
                assertEquals("copy", copy.getMeta().get(key));
            }
        }
    }

    @Test
    public void testUploadCopy() throws Exception {
        final String origId = Iterables.get(itm.getCreatedEntities(), 0).getId();

        com.openexchange.file.storage.File org = itm.getAction(origId);
        org.setTitle("copy");
        org.setFileMIMEType("text/plain");
        itm.copyAction(origId, String.valueOf(folderId), org);
        final String copyId = org.getId();

        com.openexchange.file.storage.File copy = itm.getAction(copyId);

        assertEquals("copy", copy.getTitle());
        assertEquals("text/plain", copy.getFileMIMEType());
    }

    //Bug 4269
    @Test
    public void testVirtualFolder() throws Exception {
        itm.setFailOnError(false);
        for (int folderId : virtualFolders) {
            virtualFolderTest(folderId);
        }
    }

    //Bug 4269
    public void virtualFolderTest(int folderId) throws Exception {
        try {
            final String origId = Iterables.get(itm.getCreatedEntities(), 0).getId();
            com.openexchange.file.storage.File file = itm.getAction(origId);
            file.setFolderId("" + folderId);

            itm.copyAction(origId, Integer.toString(folderId), file);
            AbstractAJAXResponse resp = itm.getLastResponse();
            assertTrue(resp.hasError());
        } catch (JSONException x) {
            assertTrue(x.getMessage(), x.getMessage().contains("IFO-1700"));
        }

    }
}
