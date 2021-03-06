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

package com.openexchange.file.storage.json.actions.files;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.json.FileTest;
import com.openexchange.groupware.ldap.SimUser;

/**
 * {@link InfostoreRequestTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
@SuppressWarnings("unused")
public class InfostoreRequestTest extends FileTest {

    @Test
    public void testShouldGetTheFolderId() throws OXException {
        request().param("folder", "12");
        assertEquals(request.getFolderId(), "12");

    }

    @Test
    public void testShouldGetColumns() throws OXException {
        request().param("columns", "1,700,702");
        assertEquals(Arrays.asList(File.Field.ID, File.Field.TITLE, File.Field.FILENAME), request.getFieldsToLoad());
    }

    @Test
    public void testShouldGetColumnsByName() throws OXException {
        request().param("columns", "id,title,filename");
        assertEquals(Arrays.asList(File.Field.ID, File.Field.TITLE, File.Field.FILENAME), request.getFieldsToLoad());
    }

    @Test
    public void testColumnsDefaultToAllColumns() throws OXException {
        assertEquals(Arrays.asList(File.Field.values()), request().getFieldsToLoad());
    }

    @Test
    public void testUnknownColumns() {
        request().param("columns", "1,700,702,1023");
        try {
            request.getFieldsToLoad();
            fail("Expected Exception");
        } catch (OXException x) {
            assertTrue(true);
        }
    }

    @Test
    public void testInvalidColumnString() {
        request().param("columns", "1,700,702,niceColumn");
        try {
            request.getFieldsToLoad();
            fail("Expected Exception");
        } catch (OXException x) {
            assertTrue(true);

        }
    }

    @Test
    public void testSortingColumn() throws OXException {
        request().param("sort", "700");
        assertEquals(File.Field.TITLE, request.getSortingField());

        request().param("sort", "title");
        assertEquals(File.Field.TITLE, request.getSortingField());
    }

    @Test
    public void testUnsetSortingColumn() throws OXException {
        assertEquals(null, request().getSortingField());
    }

    @Test
    public void testUnknownSortingColumn() {
        try {
            request().param("sort", "1024").getSortingField();
            fail("Expected Exception");
        } catch (OXException x) {
            assertTrue(true);
        }

        try {
            request().param("sort", "gnitzness").getSortingField();
            fail("Expected Exception");
        } catch (OXException x) {
            assertTrue(true);
        }

    }

    @Test
    public void testGetSortingOrder() throws OXException {
        request().param("order", "asc");
        assertEquals(FileStorageFileAccess.SortDirection.ASC, request.getSortingOrder());

        request().param("order", "desc");
        assertEquals(FileStorageFileAccess.SortDirection.DESC, request.getSortingOrder());
    }

    @Test
    public void testSortingOrderDefault() throws OXException {
        assertEquals(FileStorageFileAccess.SortDirection.ASC, request().getSortingOrder());
    }

    @Test
    public void testInvalidSortingOrder() {
        try {
            request().param("order", "supercalifragilisticexplialidocious").getSortingOrder();
            fail("Expected Exception");
        } catch (OXException x) {
            assertTrue(true);
        }
    }

    @Test
    public void testGetTimezone() {
        request().param("timezone", "Europe/Berlin");
        assertEquals(TimeZone.getTimeZone("Europe/Berlin"), request.getTimezone());
    }

    @Test
    public void testGetTimezoneDefaultsToUserTimeZone() {
        final SimUser simUser = new SimUser();
        simUser.setTimeZone("Europe/Berlin");
        request().getSimSession().setUser(simUser);

        assertEquals(TimeZone.getTimeZone("Europe/Berlin"), request.getTimezone());
    }

    @Test
    public void testGetId() {
        final String id = request().param("id", "12").getId();
        assertEquals("12", id);
    }

    @Test
    public void testGetVersion() {
        final String version = request().param("version", "2").getVersion();
        assertEquals("2", version);
    }

    @Test
    public void testGetVersionDefaultsToCurrentVersion() {
        final String version = request().getVersion();
        assertEquals(FileStorageFileAccess.CURRENT_VERSION, version);
    }

    @Test
    public void testGetIgnore() {
        final Set<String> ignore = request().param("ignore", "deleted,nice,blue").getIgnore();
        assertEquals(new HashSet<>(Arrays.asList("deleted", "nice", "blue")), ignore);
    }

    @Test
    public void testGetTimestamp() {
        final long timestamp = request().param("timestamp", "1337").getTimestamp();
        assertEquals(1337, timestamp);
    }

    @Test
    public void testTimestampDefaultsToDistantPast() {
        final long timestamp = request().getTimestamp();
        assertEquals(FileStorageFileAccess.UNDEFINED_SEQUENCE_NUMBER, timestamp);
    }

    @Test
    public void testGetIDs() throws JSONException, OXException {
        final List<String> ids = request().body(new JSONArray("[{id: 'id1', folder: 'folder'}, {id: 'id2', folder: 'folder'}]")).getIds();
        assertEquals(Arrays.asList("folder/id1", "folder/id2"), ids);

    }

    @Test
    public void testGetVersions() throws JSONException, OXException {
        final String[] versions = request().body(new JSONArray("[1,3,5]")).getVersions();

        assertEquals("1", versions[0]);
        assertEquals("3", versions[1]);
        assertEquals("5", versions[2]);

    }

    @Test
    public void testGetDiff() {
        long diff = request().param("diff", "1337").getDiff();
        assertEquals(1337L, diff);

        diff = request().getDiff();
        assertEquals(-1L, diff);
    }

    @Test
    public void testStartAndEnd() throws OXException {
        request().param("start", "10").param("end", "20");
        assertEquals(10, request.getStart());
        assertEquals(20, request.getEnd());

    }

    @Test
    public void testLimit() throws OXException {
        request().param("limit", "12");

        assertEquals(0, request.getStart());
        assertEquals(11, request.getEnd());
    }

    @Test
    public void testStartAndEndUnset() throws OXException {
        request();
        assertEquals(FileStorageFileAccess.NOT_SET, request.getStart());
        assertEquals(FileStorageFileAccess.NOT_SET, request.getEnd());
    }

    @Test
    public void testSearchFolder() throws OXException {
        final String searchFolderId = request().param("folder", "12").getSearchFolderId();
        assertEquals("12", searchFolderId);
    }

    @Test
    public void testSearchFolderDefaultsToAllFolders() throws OXException {
        final String searchFolderId = request().getSearchFolderId();
        assertEquals(FileStorageFileAccess.ALL_FOLDERS, searchFolderId);
    }

    @Test
    public void testSearchQuery() throws JSONException, OXException {
        final String searchQuery = request().body(new JSONObject("{pattern: 'somePattern'}")).getSearchQuery();
        assertEquals("somePattern", searchQuery);
    }

}
