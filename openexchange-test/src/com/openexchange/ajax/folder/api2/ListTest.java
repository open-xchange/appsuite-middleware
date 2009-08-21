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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.ajax.folder.api2;

import org.json.JSONArray;
import com.openexchange.ajax.folder.actions.ListRequest;
import com.openexchange.ajax.folder.actions.ListResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.FolderObject;

/**
 * {@link ListTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ListTest extends AbstractAJAXSession {

    private AJAXClient client;

    /**
     * Initializes a new {@link ListTest}.
     * 
     * @param name name of the test.
     */
    public ListTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client = getClient();
    }

    public void testListRoot() throws Throwable {
        // List root's subfolders
        final ListRequest request = new ListRequest(String.valueOf(FolderObject.SYSTEM_ROOT_FOLDER_ID));
        request.setFolderURL("/ajax/folder2");
        final ListResponse response = client.execute(request);

        final JSONArray jsonArray = (JSONArray) response.getResponse().getData();
        final int length = jsonArray.length();
        assertEquals("Unexpected number of subfolders below root folder.", 4, length);

        for (int i = 0; i < length; i++) {
            final JSONArray jsonSubArray = jsonArray.getJSONArray(i);
            if (0 == i) {
                assertEquals("Private folder expected at first position, but isn't.", "1", jsonSubArray.get(0));
                assertTrue("Subfolders expected below private folder.", jsonSubArray.getBoolean(3));
            } else if (1 == i) {
                assertEquals("Public folder expected at second position, but isn't.", "2", jsonSubArray.get(0));
                assertTrue("Subfolders expected below public folder.", jsonSubArray.getBoolean(3));
            } else if (2 == i) {
                assertEquals("Shared folder expected at third position, but isn't.", "3", jsonSubArray.get(0));
            } else {
                assertEquals("InfoStore folder expected at fourth position, but isn't.", "9", jsonSubArray.get(0));
                assertTrue("Subfolders expected below infostore folder.", jsonSubArray.getBoolean(3));
            }
        }

        /*-
         * 
        ListRequest request = new ListRequest(String.valueOf(FolderObject.SYSTEM_PRIVATE_FOLDER_ID));
        ListResponse response = client.execute(request);
        Iterator<FolderObject> iter = response.getFolder();
        FolderObject defaultIMAPFolder = null;
        String primaryMailFolder = MailFolderUtility.prepareFullname(0, MailFolder.DEFAULT_FOLDER_ID);
        while (iter.hasNext()) {
            final FolderObject fo = iter.next();
            if (fo.containsFullName() && primaryMailFolder.equals(fo.getFullName())) {
                defaultIMAPFolder = fo;
                break;
            }
        }
        assertNotNull("Default email folder not found.", defaultIMAPFolder);
        assertTrue("Default email folder has no subfolders.", defaultIMAPFolder.hasSubfolders());
        request = new ListRequest(defaultIMAPFolder.getFullName());
        response = client.execute(request);
        iter = response.getFolder();
        FolderObject inboxFolder = null;
        while (iter.hasNext()) {
            final FolderObject fo = iter.next();
            if (fo.getFullName().endsWith("INBOX")) {
                inboxFolder = fo;
                break;
            }
        }
        assertNotNull("Inbox folder for default mail account not found.", inboxFolder);
        GetRequest request2 = new GetRequest(inboxFolder.getFullName(), new int[] {
            FolderObject.OBJECT_ID, FolderObject.FOLDER_NAME, FolderObject.OWN_RIGHTS, FolderObject.PERMISSIONS_BITS });
        GetResponse response2 = client.execute(request2);
        assertFalse("Get failed.", response2.hasError());
         */
    }

    public void testListPrivate() throws Throwable {
        // List root's subfolders
        final ListRequest request = new ListRequest(String.valueOf(FolderObject.SYSTEM_PRIVATE_FOLDER_ID));
        request.setFolderURL("/ajax/folder2");
        final ListResponse response = client.execute(request);

        final JSONArray jsonArray = (JSONArray) response.getResponse().getData();
        final int length = jsonArray.length();
        assertTrue("Subfolders expected below private folder.", length > 0);

        System.out.println("\n\n#######################################\n\n");
        for (int i = 0; i < length; i++) {
            final JSONArray folderArray = jsonArray.getJSONArray(i);
            System.out.println(folderArray);
        }

        // final JSONArray email = jsonArray.getJSONArray(0);
        // System.out.println(email);
    }

    public void testListPublic() throws Throwable {
        // List root's subfolders
        final ListRequest request = new ListRequest(String.valueOf(FolderObject.SYSTEM_PUBLIC_FOLDER_ID));
        request.setFolderURL("/ajax/folder2");
        final ListResponse response = client.execute(request);

        final JSONArray jsonArray = (JSONArray) response.getResponse().getData();
        final int length = jsonArray.length();
        assertTrue("Subfolders expected below public folder.", length > 0);

        System.out.println("\n\n#######################################\n\n");
        for (int i = 0; i < length; i++) {
            final JSONArray folderArray = jsonArray.getJSONArray(i);
            System.out.println(folderArray);
        }

        // final JSONArray email = jsonArray.getJSONArray(0);
        // System.out.println(email);
    }

    public void testListShared() throws Throwable {
        // List root's subfolders
        final ListRequest request = new ListRequest(String.valueOf(FolderObject.SYSTEM_SHARED_FOLDER_ID));
        request.setFolderURL("/ajax/folder2");
        final ListResponse response = client.execute(request);

        final JSONArray jsonArray = (JSONArray) response.getResponse().getData();
        final int length = jsonArray.length();
        assertTrue("Subfolders expected below shared folder.", length > 0);

        System.out.println("\n\n#######################################\n\n");
        for (int i = 0; i < length; i++) {
            final JSONArray folderArray = jsonArray.getJSONArray(i);
            System.out.println(folderArray);
        }

        // final JSONArray email = jsonArray.getJSONArray(0);
        // System.out.println(email);
    }

}
