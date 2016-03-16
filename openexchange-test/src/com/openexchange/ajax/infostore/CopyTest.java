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

package com.openexchange.ajax.infostore;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.InfostoreAJAXTest;
import com.openexchange.ajax.container.Response;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.realtime.json.fields.ResourceIDField;
import com.openexchange.test.OXTestToolkit;
import com.openexchange.test.TestInit;

public class CopyTest extends InfostoreAJAXTest {

    public CopyTest(final String name){
        super(name);
    }

    private final Set<String> skipKeys = new HashSet<String>(Arrays.asList(
        Metadata.ID_LITERAL.getName(),
        Metadata.CREATION_DATE_LITERAL.getName(),
        Metadata.LAST_MODIFIED_LITERAL.getName(),
        Metadata.LAST_MODIFIED_UTC_LITERAL.getName(),
        Metadata.VERSION_LITERAL.getName(),
        Metadata.CURRENT_VERSION_LITERAL.getName(),
        Metadata.SEQUENCE_NUMBER_LITERAL.getName(),
        Metadata.CONTENT_LITERAL.getName(),
        new ResourceIDField().getColumnName()
        ));

    public void testCopy() throws Exception {
        final String id = copy(getWebConversation(), getHostName(),sessionId,clean.get(0), String.valueOf(folderId), Long.MAX_VALUE, m());
        clean.add(id);

        Response res = get(getWebConversation(), getHostName(), sessionId, clean.get(0));
        assertNoError(res);

        final JSONObject orig = (JSONObject) res.getData();

        res = get(getWebConversation(), getHostName(), sessionId, id);
        assertNoError(res);

        final JSONObject copy = (JSONObject) res.getData();

        assertEquals(orig.length(), copy.length());

        for(final Iterator keys = orig.keys(); keys.hasNext();) {
            final String key = keys.next().toString();
            if(!skipKeys.contains(key)) {
                assertEquals(key+" seems to have a wrong value", orig.get(key).toString(), copy.get(key).toString());
            }
        }

        assertNotNull(res.getTimestamp());
    }

    public void testCopyFile() throws Exception {
        final File upload = new File(TestInit.getTestProperty("ajaxPropertiesFile"));
        final String id = createNew(
                getWebConversation(),
                getHostName(),
                sessionId,
                m(
                        "folder_id"         ,    ((Integer)folderId).toString(),
                        "title"          ,      "test upload",
                        "description"     ,     "test upload description"
                ), upload, "text/plain"
        );
        clean.add(id);
        //FIXME Bug 4120
        final String copyId = copy(getWebConversation(),getHostName(),sessionId,id, String.valueOf(folderId), Long.MAX_VALUE, m("filename" , "other.properties"));
        clean.add(copyId);

        Response res = get(getWebConversation(),getHostName(), sessionId, id);
        assertNoError(res);
        final JSONObject orig = (JSONObject) res.getData();

        res = get(getWebConversation(),getHostName(), sessionId, copyId);
        assertNoError(res);
        final JSONObject copy = (JSONObject) res.getData();

        assertEquals("other.properties", copy.get("filename"));
        assertEquals(orig.get("file_size"), copy.get("file_size"));
        assertEquals(orig.get("file_mimetype"), copy.get("file_mimetype"));

        InputStream is = null;
        InputStream is2 = null;
        try {
            is = new FileInputStream(upload);
            is2 = document(getWebConversation(),getHostName(),sessionId, copyId, 1);

            OXTestToolkit.assertSameContent(is,is2);
        } finally {
            if(is!=null) {
                is.close();
            }
            if(is2!=null) {
                is2.close();
            }
        }
    }

    public void testModifyingCopy() throws Exception {
        final String id = copy(getWebConversation(), getHostName(),sessionId,clean.get(0), String.valueOf(folderId), Long.MAX_VALUE, m("title" , "copy"));
        clean.add(id);

        Response res = get(getWebConversation(), getHostName(), sessionId, clean.get(0));
        assertNoError(res);

        final JSONObject orig = (JSONObject) res.getData();

        res = get(getWebConversation(), getHostName(), sessionId, id);
        assertNoError(res);

        final JSONObject copy = (JSONObject) res.getData();

        assertEquals(orig.length(), copy.length());

        for(final Iterator keys = orig.keys(); keys.hasNext();) {
            final String key = keys.next().toString();
            if(!skipKeys.contains(key) && !key.equals("title")) {
                assertEquals(key + " seems to have a wrong value", orig.get(key).toString(), copy.get(key).toString());
            } else if (key.equals("title")) {
                assertEquals("copy",copy.get(key));
            }
        }
    }

    public void testUploadCopy() throws Exception {
        final File upload = new File(TestInit.getTestProperty("webdavPropertiesFile"));
        final String id = copy(getWebConversation(), getHostName(),sessionId,clean.get(0), String.valueOf(folderId), Long.MAX_VALUE,m("title" , "copy"), upload, "text/plain");
        clean.add(id);

        final Response res = get(getWebConversation(), getHostName(), sessionId, id);
        assertNoError(res);

        final JSONObject copy = (JSONObject) res.getData();

        assertEquals(upload.getName(),copy.get("filename"));
        assertEquals("text/plain", copy.get("file_mimetype"));
    }

    //Bug 4269
    public void testVirtualFolder() throws Exception{

        for(int folderId : virtualFolders) {
            virtualFolderTest( folderId );
        }
    }

    //Bug 4269
    public void virtualFolderTest(int folderId) throws Exception{
        try {
            final String id = copy(getWebConversation(), getHostName(),sessionId,clean.get(0), String.valueOf(folderId), Long.MAX_VALUE, m("folder_id" , ""+folderId));
            clean.add(id);
            fail("Expected IOException");
        } catch (final JSONException x) {
            assertTrue(x.getMessage(), x.getMessage().contains("IFO-1700"));
        }

    }
}
