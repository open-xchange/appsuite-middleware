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

package com.openexchange.ajax.contact;

import static com.openexchange.ajax.folder.Create.ocl;
import java.util.Date;
import org.json.JSONArray;
import com.openexchange.ajax.contact.action.AllRequest;
import com.openexchange.ajax.contact.action.InsertRequest;
import com.openexchange.ajax.folder.Create;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.ajax.framework.CommonInsertResponse;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class Bug13931Test extends AbstractAJAXSession {

    private int userId;

    private int privateFolderId, folderId;

    private FolderObject folder;

    private Contact AAA, aaa, BBB, bbb;

    private final int[] columns = new int[] { Contact.OBJECT_ID, Contact.SUR_NAME };

    public Bug13931Test(final String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        userId = getClient().getValues().getUserId();
        privateFolderId = getClient().getValues().getPrivateContactFolder();
        final OCLPermission ocl = ocl(
            userId,
            false,
            true,
            OCLPermission.ADMIN_PERMISSION,
            OCLPermission.ADMIN_PERMISSION,
            OCLPermission.ADMIN_PERMISSION,
            OCLPermission.ADMIN_PERMISSION);
        folder = Create.folder(privateFolderId, "Folder to test bug 13931 ("+new Date().getTime()+")", FolderObject.CONTACT, FolderObject.PRIVATE, ocl);
        final CommonInsertResponse response = getClient().execute(new com.openexchange.ajax.folder.actions.InsertRequest(EnumAPI.OX_OLD, folder));
        response.fillObject(folder);
        folderId = folder.getObjectID();

        AAA = new Contact();
        AAA.setParentFolderID(folderId);
        AAA.setSurName("AAA");
        client.execute(new InsertRequest(AAA)).fillObject(AAA);

        BBB = new Contact();
        BBB.setParentFolderID(folderId);
        BBB.setSurName("BBB");
        client.execute(new InsertRequest(BBB)).fillObject(BBB);

        aaa = new Contact();
        aaa.setParentFolderID(folderId);
        aaa.setSurName("aaa");
        client.execute(new InsertRequest(aaa)).fillObject(aaa);

        bbb = new Contact();
        bbb.setParentFolderID(folderId);
        bbb.setSurName("bbb");
        client.execute(new InsertRequest(bbb)).fillObject(bbb);
    }

    public void testBug13931() throws Exception {
        final AllRequest allRequest = new AllRequest(folderId, columns);
        final CommonAllResponse allResponse = client.execute(allRequest);
        final JSONArray jsonArray = (JSONArray) allResponse.getResponse().getData();
        assertNotNull("No result", jsonArray);
        assertEquals("Wrong amount of results", 4, jsonArray.length());
        assertEquals("Wrong order", AAA.getObjectID(), jsonArray.getJSONArray(0).getInt(0));
        assertEquals("Wrong order", aaa.getObjectID(), jsonArray.getJSONArray(1).getInt(0));
        assertEquals("Wrong order", BBB.getObjectID(), jsonArray.getJSONArray(2).getInt(0));
        assertEquals("Wrong order", bbb.getObjectID(), jsonArray.getJSONArray(3).getInt(0));
    }

    @Override
    public void tearDown() throws Exception {
        getClient().execute(new com.openexchange.ajax.folder.actions.DeleteRequest(EnumAPI.OX_OLD, folder.getObjectID(), folder.getLastModified()));

        super.tearDown();
    }

}
