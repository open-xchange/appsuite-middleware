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

import java.util.ArrayList;
import java.util.Iterator;
import com.openexchange.ajax.folder.actions.DeleteRequest;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.InsertRequest;
import com.openexchange.ajax.folder.actions.InsertResponse;
import com.openexchange.ajax.folder.actions.ListRequest;
import com.openexchange.ajax.folder.actions.ListResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonDeleteResponse;
import com.openexchange.groupware.container.FolderObject;

/**
 * @author <a href="mailto:steffen.templin@open-xchange.com>Steffen Templin</a>
 */
public class Bug16899Test extends AbstractAJAXSession {

    private AJAXClient client;

    /**
     * Initializes a new {@link Bug16899Test}.
     *
     * @param name name of the test.
     */
    public Bug16899Test(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client = getClient();
    }

    public void testBug16899() throws Exception {
        FolderObject folder = Create.createPrivateFolder("Bug 16899 Test", FolderObject.MAIL, client.getValues().getUserId());
        folder.setFullName("default0/INBOX/Bug 16899 Test");
        InsertRequest insertFolder = new InsertRequest(EnumAPI.OX_OLD, folder);
        InsertResponse execute = client.execute(insertFolder);

        execute.fillObject(folder);

        String inbox = client.getValues().getInboxFolder();

        ArrayList<FolderObject> folders = performListRequest(inbox);
        boolean firstMatch = false;
        for (FolderObject f : folders) {
        	if (f.getFullName().equals(folder.getFullName())) {
        		firstMatch = true;
        		break;
        	}
        }

        assertTrue("Testfolder not found in inbox.", firstMatch);
        folders = null;

        DeleteRequest deleteFolder = new DeleteRequest(EnumAPI.OX_OLD, folder);
        CommonDeleteResponse deleteResponse = client.execute(deleteFolder);

        assertNull("Error during folder deletion", deleteResponse.getException());

        folders = performListRequest(inbox);
        boolean secondMatch = false;
        for (FolderObject f : folders) {
        	// System.out.println(f.getFullName());
        	if (f.getFullName().equals(folder.getFullName())) {
        		secondMatch = true;
        		break;
        	}
        }

        assertFalse("Testfolder was not deleted.", secondMatch);
    }

    private ArrayList<FolderObject> performListRequest(String inFolder) throws Exception {
    	ArrayList<FolderObject> folderList = new ArrayList<FolderObject>();

    	ListRequest request = new ListRequest(EnumAPI.OUTLOOK, inFolder, FolderObject.ALL_COLUMNS, false, false);
        ListResponse response = client.execute(request);
        assertNull("Error during ListRequest.", response.getException());

        Iterator<FolderObject> iter = response.getFolder();

        while (iter.hasNext()) {
            final FolderObject fo = iter.next();
            folderList.add(fo);
        }

    	return folderList;
    }

}
