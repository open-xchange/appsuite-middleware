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

package com.openexchange.ajax.folder.api2;

import static com.openexchange.java.Autoboxing.I;
import java.util.Iterator;
import com.openexchange.ajax.folder.actions.AllowedModules;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.ListRequest;
import com.openexchange.ajax.folder.actions.ListResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.database.contentType.InfostoreContentType;
import com.openexchange.groupware.container.FolderObject;

/**
 * Verifies that InfoStore folder can be excluded from list requests.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class Bug16243Test extends AbstractAJAXSession {

    private static final int[] COLUMNS = {
        FolderObject.OBJECT_ID, FolderObject.FOLDER_ID, FolderObject.CREATED_BY, FolderObject.MODIFIED_BY, FolderObject.FOLDER_NAME,
        FolderObject.MODULE, FolderObject.TYPE, FolderObject.SUBFOLDERS, FolderObject.OWN_RIGHTS, FolderObject.PERMISSIONS_BITS,
        FolderObject.SUMMARY, FolderObject.STANDARD_FOLDER, FolderObject.TOTAL, FolderObject.NEW, FolderObject.UNREAD,
        FolderObject.DELETED, FolderObject.CAPABILITIES, FolderObject.SUBSCRIBED, FolderObject.SUBSCR_SUBFLDS, 316 };

    private AJAXClient client;

    public Bug16243Test(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client = getClient();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testListWithoutInfoStore() throws Throwable {
        ListRequest request = new ListRequest(EnumAPI.OUTLOOK, FolderStorage.PRIVATE_ID, COLUMNS, false);
        request.setAllowedModules(AllowedModules.CALENDAR, AllowedModules.MAIL, AllowedModules.CONTACTS, AllowedModules.TASKS);
        ListResponse response = client.execute(request);
        Iterator<FolderObject> iter = response.getFolder();
        assertTrue("List request does not return any folders.", iter.hasNext());
        while (iter.hasNext()) {
            FolderObject folder = iter.next();
            assertNotSame("Found some infostore folder.", I(InfostoreContentType.getInstance().getModule()), I(folder.getModule()));
        }
    }
}
