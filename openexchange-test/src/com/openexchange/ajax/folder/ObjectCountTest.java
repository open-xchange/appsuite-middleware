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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

import java.io.IOException;
import java.util.UUID;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.GetRequestNew;
import com.openexchange.ajax.folder.actions.GetResponseNew;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.exception.OXException;
import com.openexchange.folder.json.services.ServiceRegistry;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.internal.ContentTypeRegistry;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.test.ContactTestManager;
import com.openexchange.test.FolderTestManager;


/**
 * {@link ObjectCountTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class ObjectCountTest extends AbstractAJAXSession {
    
    private static final int[] DEFAULT_COLUMNS = new int[] { 1, 2, 3, 4, 5, 6, 20, 300, 301, 302, 309 };
    
    private AJAXClient client1;
    
    private AJAXClient client2;

    public ObjectCountTest(String name) {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client1 = getClient();
        client2 = new AJAXClient(User.User2);
        
        ContentTypeRegistry ctr = ContentTypeRegistry.getInstance();
        ServiceRegistry.getInstance().addService(ContentTypeRegistry.class, ctr);
    }
    
    /*
     * ----------------------- Tests for contact module -----------------------
     */
    public void testCountInPrivateFolderForContacts() throws Exception {
        FolderTestManager ftm = new FolderTestManager(client1);
        ContactTestManager ctm = new ContactTestManager(client1);
        try {
            FolderObject created = createPrivateFolder(ftm, FolderObject.CONTACT);
            Folder folder = getFolder(client1, created.getObjectID(), DEFAULT_COLUMNS);
            assertEquals("Wrong object count", 0, folder.getTotal());
            ctm.newAction(ContactTestManager.generateContact(created.getObjectID()));
            Folder reloaded = getFolder(client1, created.getObjectID(), DEFAULT_COLUMNS);
            assertEquals("Wrong object count", 1, reloaded.getTotal());
        } catch (Exception e) {
            ctm.cleanUp();
            ftm.cleanUp();
        }
    }
    
    private Folder getFolder(AJAXClient client, int folderId, int[] columns) throws OXException, IOException, JSONException {
        GetRequestNew req = new GetRequestNew(EnumAPI.OX_NEW, String.valueOf(folderId), columns);
        GetResponseNew resp = client.execute(req);
        return resp.getFolder();
    }
    
    /**
     * Creates a private folder for the given module (see modules section in {@link FolderObject}).
     * Client1 will be the folder owner.
     */
    private FolderObject createPrivateFolder(FolderTestManager ftm, int module) throws OXException, IOException, JSONException {
        FolderObject folder = ftm.generatePrivateFolder(
            UUID.randomUUID().toString(),
            module,
            getParentFolderForModule(client1, module),
            client1.getValues().getUserId());
        return ftm.insertFolderOnServer(folder);
    }
    
    /**
     * Creates a shared folder for the given module (see modules section in {@link FolderObject}).
     * Client1 will be the folder owner and can read all objects.
     * Client2 will be the user the folder is shared to. He can only see his own objects.
     */
    private FolderObject createSharedFolder(int module) throws OXException, IOException, JSONException {
        FolderTestManager ftm = new FolderTestManager(client1);
        FolderObject folder = ftm.generateSharedFolder(
            UUID.randomUUID().toString(),
            module,
            getParentFolderForModule(client1, module),
            client1.getValues().getUserId());
        
        OCLPermission permissions = new OCLPermission();
        permissions.setEntity(client2.getValues().getUserId());
        permissions.setGroupPermission(false);
        permissions.setFolderAdmin(false);
        permissions.setAllPermission(
            OCLPermission.READ_FOLDER,
            OCLPermission.READ_OWN_OBJECTS,
            OCLPermission.CREATE_OBJECTS_IN_FOLDER,
            OCLPermission.DELETE_OWN_OBJECTS);
        folder.addPermission(permissions);
        return ftm.insertFolderOnServer(folder);
    }
    
    private int getParentFolderForModule(AJAXClient client, int module) throws OXException, IOException, JSONException {
        switch (module) {
            case FolderObject.CALENDAR:
                return client.getValues().getPrivateAppointmentFolder();
                
            case FolderObject.TASK:
                return client.getValues().getPrivateTaskFolder();
                
            case FolderObject.INFOSTORE:
                return client.getValues().getPrivateInfostoreFolder();
                
            case FolderObject.CONTACT:
                return client.getValues().getPrivateContactFolder();
            
            default:
                return -1;
        }
    }

}
