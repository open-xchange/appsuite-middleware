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

package com.openexchange.file.storage.json;

import static com.openexchange.file.storage.File.Field.CATEGORIES;
import static com.openexchange.file.storage.File.Field.COLOR_LABEL;
import static com.openexchange.file.storage.File.Field.DESCRIPTION;
import static com.openexchange.file.storage.File.Field.FILENAME;
import static com.openexchange.file.storage.File.Field.FOLDER_ID;
import static com.openexchange.file.storage.File.Field.MODIFIED_BY;
import static com.openexchange.file.storage.File.Field.NUMBER_OF_VERSIONS;
import static com.openexchange.file.storage.File.Field.VERSION;
import static com.openexchange.file.storage.File.Field.VERSION_COMMENT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;

/**
 * {@link FileParserTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class FileParserTest {

    @Test
    public void testParse() throws JSONException, OXException {
        final JSONObject object = new JSONObject();
        object.put("categories", new JSONArray("['cat1', 'cat2', 'cat3']"));
        object.put("color_label", 12);
        object.put("creation_date", 1337);
        object.put("created_by", 13);
        object.put("description", "description");
        object.put("file_md5sum", "12345");
        object.put("file_mimetype", "mime/type");
        object.put("file_size", 1337);
        object.put("filename", "filename");
        object.put("folder_id", "12");
        object.put("id", "23");
        object.put("last_modified", 1337);
        object.put("locked_until", 1337);
        object.put("modified_by", 14);
        object.put("number_of_versions", 3000);
        object.put("title", "nice title");
        object.put("url", "http://some.url");
        object.put("version", 33);
        object.put("version_comment", "This is the best version");

        final File file = FileMetadataParser.getInstance().parse(object);

        assertNotNull(file);
        assertEquals("cat1, cat2, cat3", file.getCategories());
        assertEquals(12, file.getColorLabel());
        assertEquals(new Date(1337), file.getCreated());
        assertEquals(13, file.getCreatedBy());
        assertEquals("description", file.getDescription());
        assertEquals("12345", file.getFileMD5Sum());
        assertEquals("mime/type", file.getFileMIMEType());
        assertEquals(1337L, file.getFileSize());
        assertEquals("filename", file.getFileName());
        assertEquals("12", file.getFolderId());
        assertEquals("23", file.getId());
        assertEquals(new Date(1337), file.getLastModified());
        assertEquals(new Date(1337), file.getLockedUntil());
        assertEquals(14, file.getModifiedBy());
        assertEquals(3000, file.getNumberOfVersions());
        assertEquals("nice title", file.getTitle());
        assertEquals("http://some.url", file.getURL());
        assertEquals("33", file.getVersion());
        assertEquals("This is the best version", file.getVersionComment());
    }

    @Test
    public void testFindFields() throws JSONException {
        final JSONObject object = new JSONObject();
        object.put("categories", new JSONArray("['cat1', 'cat2', 'cat3']"));
        object.put("color_label", 12);
        object.put("description", "description");
        object.put("filename", "filename");
        object.put("folder_id", "12");
        object.put("modified_by", 14);
        object.put("number_of_versions", 3000);
        object.put("version", 33);
        object.put("version_comment", "This is the best version");

        final List<File.Field> fields = FileMetadataParser.getInstance().getFields(object);

        for (final File.Field field : EnumSet.of(CATEGORIES, COLOR_LABEL, DESCRIPTION, FILENAME, FOLDER_ID, MODIFIED_BY, NUMBER_OF_VERSIONS, VERSION, VERSION_COMMENT)) {
            assertTrue("Missing field " + field, fields.contains(field));
        }
    }
}
