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

package com.openexchange.file.storage.json.actions.files;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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
public class InfostoreRequestTest extends FileTest {

    public void testShouldGetTheFolderId() throws OXException {

        request().param("folder", "12");
        assertEquals(request.getFolderId(), "12");

    }

    public void testShouldGetColumns() throws OXException {
        request().param("columns", "1,700,702");
        assertEquals(Arrays.asList(File.Field.ID, File.Field.TITLE, File.Field.FILENAME), request.getFieldsToLoad());
    }

    public void testShouldGetColumnsByName() throws OXException {
        request().param("columns", "id,title,filename");
        assertEquals(Arrays.asList(File.Field.ID, File.Field.TITLE, File.Field.FILENAME), request.getFieldsToLoad());
    }

    public void testColumnsDefaultToAllColumns() throws OXException {
        assertEquals(Arrays.asList(File.Field.values()), request().getFieldsToLoad());
    }

    public void testUnknownColumns() {
        request().param("columns", "1,700,702,1023");
        try {
            request.getFieldsToLoad();
            fail("Expected Exception");
        } catch (final OXException x) {
            assertTrue(true);
        }
    }

    public void testInvalidColumnString() {
        request().param("columns", "1,700,702,niceColumn");
        try {
            request.getFieldsToLoad();
            fail("Expected Exception");
        } catch (final OXException x) {
            assertTrue(true);

        }
    }

    public void testSortingColumn() throws OXException {
        request().param("sort", "700");
        assertEquals(File.Field.TITLE, request.getSortingField());

        request().param("sort", "title");
        assertEquals(File.Field.TITLE, request.getSortingField());
}

    public void testUnsetSortingColumn() throws OXException {
        assertEquals(null, request().getSortingField());
    }

    public void testUnknownSortingColumn() {
        try {
            request().param("sort", "1024").getSortingField();
            fail("Expected Exception");
        } catch (final OXException x) {
            assertTrue(true);
        }

        try {
            request().param("sort", "gnitzness").getSortingField();
            fail("Expected Exception");
        } catch (final OXException x) {
            assertTrue(true);
        }

    }

    public void testGetSortingOrder() throws OXException {
        request().param("order", "asc");
        assertEquals(FileStorageFileAccess.SortDirection.ASC, request.getSortingOrder());

        request().param("order", "desc");
        assertEquals(FileStorageFileAccess.SortDirection.DESC, request.getSortingOrder());
    }

    public void testSortingOrderDefault() throws OXException {
        assertEquals(FileStorageFileAccess.SortDirection.ASC, request().getSortingOrder());
    }

    public void testInvalidSortingOrder() {
        try {
            request().param("order", "supercalifragilisticexplialidocious").getSortingOrder();
            fail("Expected Exception");
        } catch (final OXException x) {
            assertTrue(true);
        }
    }

    public void testGetTimezone() throws OXException {
        request().param("timezone", "Europe/Berlin");
        assertEquals(TimeZone.getTimeZone("Europe/Berlin"), request.getTimezone());
    }

    public void testGetTimezoneDefaultsToUserTimeZone() throws OXException {
        final SimUser simUser = new SimUser();
        simUser.setTimeZone("Europe/Berlin");
        request().getSimSession().setUser(simUser);

        assertEquals(TimeZone.getTimeZone("Europe/Berlin"), request.getTimezone());
    }

    public void testGetId() {
        final String id = request().param("id", "12").getId();
        assertEquals("12", id);
    }

    public void testGetVersion() {
        final String version = request().param("version", "2").getVersion();
        assertEquals("2", version);
    }

    public void testGetVersionDefaultsToCurrentVersion()  {
        final String version = request().getVersion();
        assertEquals(FileStorageFileAccess.CURRENT_VERSION, version);
    }

    public void testGetIgnore() {
        final Set<String> ignore = request().param("ignore", "deleted,nice,blue").getIgnore();
        assertEquals(new HashSet<String>(Arrays.asList("deleted", "nice", "blue")), ignore);
    }

    public void testGetTimestamp() {
        final long timestamp = request().param("timestamp", "1337").getTimestamp();
        assertEquals(1337, timestamp);
    }

    public void testTimestampDefaultsToDistantPast() {
        final long timestamp = request().getTimestamp();
        assertEquals(FileStorageFileAccess.UNDEFINED_SEQUENCE_NUMBER, timestamp);
    }

    public void testGetIDs() throws JSONException, OXException {
        final List<String> ids = request().body(new JSONArray("[{id: 'id1', folder: 'folder'}, {id: 'id2', folder: 'folder'}]")).getIds();
        assertEquals(Arrays.asList("id1", "id2"), ids);

    }

    public void testGetVersions() throws JSONException, OXException {
        final String[] versions = request().body(new JSONArray("[1,3,5]")).getVersions();

        assertEquals("1", versions[0]);
        assertEquals("3", versions[1]);
        assertEquals("5", versions[2]);

    }

    public void testGetDiff() {
        long diff = request().param("diff", "1337").getDiff();
        assertEquals(1337l, diff);

        diff = request().getDiff();
        assertEquals(-1l, diff);
    }

    public void testStartAndEnd() throws OXException {
        request().param("start", "10").param("end", "20");
        assertEquals(10, request.getStart());
        assertEquals(20, request.getEnd());

    }

    public void testLimit() throws OXException {
        request().param("limit", "12");

        assertEquals(0, request.getStart());
        assertEquals(11, request.getEnd());
    }

    public void testStartAndEndUnset() throws OXException {
        request();
        assertEquals(FileStorageFileAccess.NOT_SET, request.getStart());
        assertEquals(FileStorageFileAccess.NOT_SET, request.getEnd());
    }

    public void testSearchFolder() throws OXException {
        final String searchFolderId = request().param("folder", "12").getSearchFolderId();
        assertEquals("12", searchFolderId);
    }

    public void testSearchFolderDefaultsToAllFolders() throws OXException {
        final String searchFolderId = request().getSearchFolderId();
        assertEquals(FileStorageFileAccess.ALL_FOLDERS, searchFolderId);
    }

    public void testSearchQuery() throws JSONException, OXException {
        final String searchQuery = request().body(new JSONObject("{pattern: 'somePattern'}")).getSearchQuery();
        assertEquals("somePattern", searchQuery);
    }

}
