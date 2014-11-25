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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.ajax.share.tests;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.share.ShareTest;
import com.openexchange.ajax.share.actions.AllRequest;
import com.openexchange.ajax.share.actions.AllResponse;
import com.openexchange.ajax.share.actions.DeleteRequest;
import com.openexchange.ajax.share.actions.InviteRequest;
import com.openexchange.ajax.share.actions.ParsedShare;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.Permissions;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.modules.Module;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.recipient.AnonymousRecipient;
import com.openexchange.share.recipient.ShareRecipient;


/**
 * {@link StoreMetaInformationTest}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8.0
 */
public class StoreMetaInformationTest extends ShareTest {

    private static final int FOLDER_READ_PERMISSION = Permissions.createPermissionBits(
        Permission.READ_FOLDER,
        Permission.READ_ALL_OBJECTS,
        Permission.NO_PERMISSIONS,
        Permission.NO_PERMISSIONS,
        false);

    private Map<String, Object> meta;
    private FolderObject folder;
    private Random rnd;
    private ShareTarget target;
    private AJAXClient client2;
    private ParsedShare toDelete;

    /**
     * Initializes a new {@link StoreMetaInformationTest}.
     * @param name
     */
    public StoreMetaInformationTest(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        meta = new HashMap<String, Object>();
        rnd = new Random(System.currentTimeMillis());
        int entries = rnd.nextInt(10) + 1;
        for (int i = 0; i < entries; i++) {
            meta.put("element" + i, rnd.nextInt());
        }
        folder = insertPrivateFolder(EnumAPI.OX_NEW, Module.INFOSTORE.getFolderConstant(), client.getValues().getPrivateInfostoreFolder());
        remember(folder);
        client2 = new AJAXClient(User.User2);
        AnonymousRecipient recipient = new AnonymousRecipient();
        recipient.setBits(FOLDER_READ_PERMISSION);
        target = new ShareTarget(Module.INFOSTORE.getFolderConstant(), Integer.toString(folder.getObjectID()));
        target.setMeta(meta);
        InviteRequest request = new InviteRequest(Collections.singletonList(target), Collections.<ShareRecipient>singletonList(recipient), true);
        client.execute(request);
    }

    @Override
    public void tearDown() throws Exception {
        DeleteRequest request = new DeleteRequest(toDelete, System.currentTimeMillis());
        client.execute(request);
        super.tearDown();
    }

    public void testStoreMetaInformation() throws Exception {
        AllRequest request = new AllRequest(false);
        AllResponse response = client.execute(request);
        for (ParsedShare ps : response.getParsedShares()) {
            if (ps.getTarget().equals(target)) {
                toDelete = ps;
                assertEquals("Meta information was not stored for target", meta, ps.getTarget().getMeta());
                return;
            }
        }
        fail("Share created for meta information test not found.");
    }

}
