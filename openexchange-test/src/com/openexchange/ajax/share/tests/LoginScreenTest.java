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

package com.openexchange.ajax.share.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.Collections;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.share.GuestClient;
import com.openexchange.ajax.share.ShareTest;
import com.openexchange.ajax.share.actions.GetLinkRequest;
import com.openexchange.ajax.share.actions.GetLinkResponse;
import com.openexchange.ajax.share.actions.RedeemRequest;
import com.openexchange.ajax.share.actions.RedeemResponse;
import com.openexchange.ajax.share.actions.ResolveShareResponse;
import com.openexchange.ajax.share.actions.ShareLink;
import com.openexchange.ajax.share.actions.UpdateLinkRequest;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.modules.Module;
import com.openexchange.java.util.UUIDs;
import com.openexchange.share.ShareTarget;

/**
 * {@link LoginScreenTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class LoginScreenTest extends ShareTest {

    private FolderObject folder;


    @Before
    public void setUp() throws Exception {
        super.setUp();
        folder = insertPrivateFolder(EnumAPI.OX_NEW, Module.INFOSTORE.getFolderConstant(), getClient().getValues().getPrivateInfostoreFolder());
    }

    @After
    public void tearDown() throws Exception {
        try {
            deleteFoldersSilently(getClient(), Collections.singletonList(folder.getObjectID()));
        } finally {
            super.tearDown();
        }
    }

    @Test
    public void testLinkWithPassword() throws Exception {
        /*
         * Create link and set password
         */
        ShareTarget target = new ShareTarget(folder.getModule(), Integer.toString(folder.getObjectID()));
        GetLinkRequest getLinkRequest = new GetLinkRequest(target);
        GetLinkResponse getLinkResponse = getClient().execute(getLinkRequest);
        ShareLink shareLink = getLinkResponse.getShareLink();
        assertTrue(shareLink.isNew());
        UpdateLinkRequest updateLinkRequest = new UpdateLinkRequest(target, getLinkResponse.getTimestamp().getTime());
        String newPW = UUIDs.getUnformattedStringFromRandom();
        updateLinkRequest.setPassword(newPW);
        getClient().execute(updateLinkRequest);
        /*
         * Login and check params
         */
        GuestClient guestClient = resolveShare(shareLink.getShareURL(), null, newPW);
        guestClient.checkSessionAlive(false);
        ResolveShareResponse resolveResponse = guestClient.getShareResolveResponse();
        String token = resolveResponse.getToken();
        assertNotNull(token);
        RedeemRequest req = new RedeemRequest(token);
        RedeemResponse resp = guestClient.execute(req);
        assertEquals("anonymous_password", resp.getLoginType());
        assertEquals("INFO", resp.getMessageType());
        assertNotNull(resp.getMessage());
    }

}
