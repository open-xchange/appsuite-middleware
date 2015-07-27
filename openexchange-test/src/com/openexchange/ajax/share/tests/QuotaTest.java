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

import java.rmi.Naming;
import com.openexchange.admin.rmi.OXUserInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.InsertResponse;
import com.openexchange.ajax.folder.actions.UpdateRequest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.UserValues;
import com.openexchange.ajax.share.ShareTest;
import com.openexchange.ajax.share.actions.GetLinkRequest;
import com.openexchange.ajax.share.actions.GetLinkResponse;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.configuration.AJAXConfig.Property;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.modules.Module;
import com.openexchange.quota.QuotaExceptionCodes;
import com.openexchange.share.ShareTarget;

/**
 * {@link QuotaTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */
public class QuotaTest extends ShareTest {

    public QuotaTest(String name) {
        super(name);
    }

    private static final String COM_OPENEXCHANGE_QUOTA = "com.openexchange.quota.";

    private FolderObject sharedFolder;

    private FolderObject privateFolder;

    public void changeUserConfig() throws Exception {
        com.openexchange.admin.rmi.dataobjects.User user = new com.openexchange.admin.rmi.dataobjects.User(client.getValues().getUserId());
        user.setUserAttribute("config", COM_OPENEXCHANGE_QUOTA + "share_links", "1");
        user.setUserAttribute("config", COM_OPENEXCHANGE_QUOTA + "invite_guests", "1");
        Credentials credentials = new Credentials(AJAXConfig.getProperty(AJAXClient.User.OXAdmin.getLogin()), AJAXConfig.getProperty(AJAXClient.User.OXAdmin.getPassword()));
        OXUserInterface iface = (OXUserInterface) Naming.lookup("rmi://" + AJAXConfig.getProperty(Property.RMI_HOST) + ":1099/" + OXUserInterface.RMI_NAME);
        iface.change(new Context(client.getValues().getContextId()), user, credentials);
    }

    public void revertUserConfig() throws Exception {
        com.openexchange.admin.rmi.dataobjects.User user = new com.openexchange.admin.rmi.dataobjects.User(client.getValues().getUserId());
        user.setUserAttribute("config", COM_OPENEXCHANGE_QUOTA + "share_links", null);
        user.setUserAttribute("config", COM_OPENEXCHANGE_QUOTA + "invite_guests", null);
        Credentials credentials = new Credentials(AJAXConfig.getProperty(AJAXClient.User.OXAdmin.getLogin()), AJAXConfig.getProperty(AJAXClient.User.OXAdmin.getPassword()));
        OXUserInterface iface = (OXUserInterface) Naming.lookup("rmi://" + AJAXConfig.getProperty(Property.RMI_HOST) + ":1099/" + OXUserInterface.RMI_NAME);
        iface.change(new Context(client.getValues().getContextId()), user, credentials);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        changeUserConfig();

        UserValues values = client.getValues();
        long now = System.currentTimeMillis();
        sharedFolder = insertSharedFolder(EnumAPI.OX_NEW, Module.INFOSTORE.getFolderConstant(), values.getPrivateInfostoreFolder(), createNamedGuestPermission("testShareQuota" + now + "@example.org", "Test " + now));
        privateFolder = insertPrivateFolder(EnumAPI.OX_NEW, Module.INFOSTORE.getFolderConstant(), values.getPrivateInfostoreFolder());

        ShareTarget target = new ShareTarget(FolderObject.INFOSTORE, Integer.toString(sharedFolder.getObjectID()));
        GetLinkRequest getLinkRequest = new GetLinkRequest(target);
        client.execute(getLinkRequest);
    }

    @Override
    public void tearDown() throws Exception {
        try {
            revertUserConfig();
        } finally {
            super.tearDown();
        }
    }

    public void testShareLinkButQuotaLimitReached() throws Exception {
        ShareTarget target = new ShareTarget(FolderObject.INFOSTORE, Integer.toString(privateFolder.getObjectID()));
        GetLinkRequest getLinkRequest = new GetLinkRequest(target);
        getLinkRequest.setFailOnError(false);
        GetLinkResponse getLinkResponse = client.execute(getLinkRequest);
        assertTrue(getLinkResponse.hasError());
        OXException serverException = getLinkResponse.getException();
        assertTrue("Unexpected exception: " + serverException, QuotaExceptionCodes.QUOTA_EXCEEDED_SHARES.equals(serverException));
    }

    public void testInviteGuestButQuotaLimitReached() throws Exception {
        privateFolder.getPermissions().add(createNamedGuestPermission(randomUID() + "@example.com", randomUID()));
        UpdateRequest request = new UpdateRequest(EnumAPI.OX_NEW, privateFolder);
        request.setFailOnError(false);
        InsertResponse updateResponse = client.execute(request);

        assertTrue(updateResponse.hasError());
        OXException serverException = updateResponse.getException();
        assertTrue("Unexpected exception: " + serverException, QuotaExceptionCodes.QUOTA_EXCEEDED_SHARES.equals(serverException));
    }

}
