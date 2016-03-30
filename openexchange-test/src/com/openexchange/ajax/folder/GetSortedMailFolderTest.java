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

package com.openexchange.ajax.folder;

import java.util.Iterator;
import java.util.List;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.GetRequest;
import com.openexchange.ajax.folder.actions.GetResponse;
import com.openexchange.ajax.folder.actions.ListRequest;
import com.openexchange.ajax.folder.actions.ListResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.utils.MailFolderUtility;

/**
 * {@link GetSortedMailFolderTest}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class GetSortedMailFolderTest extends AbstractAJAXSession {

    private static final int[] COLUMNS = {
        FolderObject.OBJECT_ID, FolderObject.FOLDER_NAME, FolderObject.OWN_RIGHTS, FolderObject.PERMISSIONS_BITS };

    private AJAXClient client;

    /**
     * Initializes a new {@link GetSortedMailFolderTest}.
     * @param name test name.
     */
    public GetSortedMailFolderTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client = getClient();
    }

    public void testGetSortedMailFolder() throws Throwable {
        ListRequest request = new ListRequest(EnumAPI.OX_OLD, String.valueOf(FolderObject.SYSTEM_PRIVATE_FOLDER_ID));
        ListResponse response = client.execute(request);
        Iterator<FolderObject> iter = response.getFolder();
        FolderObject rootMailFolder = null;
        String primaryMailFolder = MailFolderUtility.prepareFullname(0, MailFolder.DEFAULT_FOLDER_ID);
        while (iter.hasNext()) {
            final FolderObject fo = iter.next();
            if (fo.containsFullName() && primaryMailFolder.equals(fo.getFullName())) {
                rootMailFolder = fo;
                break;
            }
        }
        assertNotNull("Default email folder not found.", rootMailFolder);
        assertTrue("Default email folder has no subfolders.", rootMailFolder.hasSubfolders());
        request = new ListRequest(EnumAPI.OX_OLD, rootMailFolder.getFullName());
        response = client.execute(request);
        List<FolderObject> l = FolderTools.convert(response.getFolder());
        assertTrue("No folders below virtual primary mail account folder found.", l.size() > 0);
        int pos = 0;
        final FolderObject inboxFolder = l.get(pos++);
        {
            assertTrue("Default inbox folder not found.", inboxFolder.getFullName().endsWith("INBOX"));
            GetRequest request2 = new GetRequest(EnumAPI.OX_OLD, inboxFolder.getFullName(), COLUMNS);
            GetResponse response2 = client.execute(request2);
            assertFalse("Getting folder information failed.", response2.hasError());
            if (l.size() == 1) {
                request = new ListRequest(EnumAPI.OX_OLD, inboxFolder.getFullName());
                response = client.execute(request);
                l = FolderTools.convert(response.getFolder());
                pos = 0;
            }
        }
        {
            final FolderObject draftsFolder = l.get(pos++);
            assertTrue("Default drafts folder not found.", draftsFolder.isDefaultFolder());
            GetRequest request2 = new GetRequest(EnumAPI.OX_OLD, draftsFolder.getFullName(), COLUMNS);
            GetResponse response2 = client.execute(request2);
            assertFalse("Getting folder information failed.", response2.hasError());
        }
        {
            final FolderObject sentFolder = l.get(pos++);
            assertTrue("Default sent folder not found", sentFolder.isDefaultFolder());
            GetRequest request2 = new GetRequest(EnumAPI.OX_OLD, sentFolder.getFullName(), COLUMNS);
            GetResponse response2 = client.execute(request2);
            assertFalse("Getting folder information failed.", response2.hasError());
        }
        {
            final FolderObject spamFolder = l.get(pos++);
            assertTrue("Default spam folder not found", spamFolder.isDefaultFolder());
            GetRequest request2 = new GetRequest(EnumAPI.OX_OLD, spamFolder.getFullName(), COLUMNS);
            GetResponse response2 = client.execute(request2);
            assertFalse("Getting folder information failed.", response2.hasError());
        }
        {
            final FolderObject trashFolder = l.get(pos++);
            assertTrue("Default trash folder not found", trashFolder.isDefaultFolder());
            GetRequest request2 = new GetRequest(EnumAPI.OX_OLD, trashFolder.getFullName(), COLUMNS);
            GetResponse response2 = client.execute(request2);
            assertFalse("Getting folder information failed.", response2.hasError());
        }
    }
}
