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

package com.openexchange.ajax.share.bugs;

import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.OCLGuestPermission;
import com.openexchange.ajax.share.GuestClient;
import com.openexchange.ajax.share.ShareTest;
import com.openexchange.ajax.share.actions.DeleteLinkRequest;
import com.openexchange.ajax.share.actions.GetLinkRequest;
import com.openexchange.ajax.share.actions.GetLinkResponse;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.share.ShareTarget;


/**
 * {@link Bug41287Test}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8.0
 */
public class Bug41287Test extends ShareTest {

    private FolderObject parent;
    private FolderObject subfolder;
    private ShareTarget target;

    public Bug41287Test(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        parent = insertPrivateFolder(EnumAPI.OX_NEW, FolderObject.INFOSTORE, client.getValues().getPrivateInfostoreFolder());
        subfolder = insertPrivateFolder(EnumAPI.OX_NEW, FolderObject.INFOSTORE, parent.getObjectID());
        remember(subfolder);
        remember(parent);
        target = new ShareTarget(FolderObject.INFOSTORE, String.valueOf(subfolder.getObjectID()));
        GetLinkRequest req = new GetLinkRequest(target);
        client.execute(req);
    }

    @Override
    public void tearDown() throws Exception {
        try {
            DeleteLinkRequest req = new DeleteLinkRequest(target, System.currentTimeMillis());
            client.execute(req);
        } finally {
            super.tearDown();
        }
    }

    public void testBug41287() throws Exception {
        ShareTarget t = new ShareTarget(FolderObject.INFOSTORE, String.valueOf(parent.getObjectID()));
        GetLinkRequest req = new GetLinkRequest(t);
        GetLinkResponse resp = client.execute(req);
        assertFalse(resp.hasError());
        String url = resp.getShareLink().getShareURL();
        GuestClient guestClient = resolveShare(url);
        OCLGuestPermission perm = createAnonymousGuestPermission();
        perm.setEntity(guestClient.getValues().getUserId());
        guestClient.checkFolderAccessible(String.valueOf(parent.getObjectID()), perm);
    }

}
