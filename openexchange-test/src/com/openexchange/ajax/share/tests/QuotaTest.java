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

import java.io.IOException;
import java.rmi.Naming;
import java.util.Collections;
import junitx.framework.Assert;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;
import com.openexchange.admin.rmi.OXUserInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.framework.UserValues;
import com.openexchange.ajax.infostore.actions.InfostoreTestManager;
import com.openexchange.ajax.share.ShareTest;
import com.openexchange.ajax.share.actions.GetLinkForQuotaLimitRequest;
import com.openexchange.ajax.share.actions.GetLinkForQuotaLimitResponse;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.configuration.AJAXConfig.Property;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFile;
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

    private static final String COM_OPENEXCHANGE_QUOTA_SHARE = "com.openexchange.quota.share";

    private InfostoreTestManager itm;

    private FolderObject infostore;

    private DefaultFile file;

    public void changeUserConfig() throws Exception {
        AJAXClient client4 = new AJAXClient(AJAXClient.User.User4);
        com.openexchange.admin.rmi.dataobjects.User user = new com.openexchange.admin.rmi.dataobjects.User(client4.getValues().getUserId());
        user.setUserAttribute("config", COM_OPENEXCHANGE_QUOTA_SHARE, "1");
        Credentials credentials = new Credentials(AJAXConfig.getProperty(AJAXClient.User.OXAdmin.getLogin()), AJAXConfig.getProperty(AJAXClient.User.OXAdmin.getPassword()));
        OXUserInterface iface = (OXUserInterface) Naming.lookup("rmi://" + AJAXConfig.getProperty(Property.RMI_HOST) + ":1099/" + OXUserInterface.RMI_NAME);
        iface.change(new Context(client4.getValues().getContextId()), user, credentials);
        client4.logout();
    }

    public void revertUserConfig() throws Exception {
        AJAXClient client4 = new AJAXClient(AJAXClient.User.User4);
        com.openexchange.admin.rmi.dataobjects.User user = new com.openexchange.admin.rmi.dataobjects.User(client4.getValues().getUserId());
        user.setUserAttribute("config", COM_OPENEXCHANGE_QUOTA_SHARE, null);
        Credentials credentials = new Credentials(AJAXConfig.getProperty(AJAXClient.User.OXAdmin.getLogin()), AJAXConfig.getProperty(AJAXClient.User.OXAdmin.getPassword()));
        OXUserInterface iface = (OXUserInterface) Naming.lookup("rmi://" + AJAXConfig.getProperty(Property.RMI_HOST) + ":1099/" + OXUserInterface.RMI_NAME);
        iface.change(new Context(client4.getValues().getContextId()), user, credentials);
        client4.logout();
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        client = new AJAXClient(User.User4);

        changeUserConfig();

        itm = new InfostoreTestManager(client);

        UserValues values = client.getValues();
        infostore = insertPrivateFolder(EnumAPI.OX_NEW, Module.INFOSTORE.getFolderConstant(), values.getPrivateInfostoreFolder());

        long now = System.currentTimeMillis();
        FolderObject parent = infostore;
        DefaultFile firstShare = new DefaultFile();
        firstShare.setFolderId(String.valueOf(parent.getObjectID()));
        firstShare.setTitle("QuotaTest_" + now);
        firstShare.setDescription(firstShare.getTitle());
        itm.newAction(firstShare);

        // Create First share to reach limit
        ShareTarget target = new ShareTarget(FolderObject.INFOSTORE, Integer.toString(infostore.getObjectID()), firstShare.getId());
        GetLinkForQuotaLimitRequest getLinkRequest = new GetLinkForQuotaLimitRequest(Collections.singletonList(target));
        getLinkRequest.setBits(createAnonymousGuestPermission().getPermissionBits());
        client.execute(getLinkRequest);
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();

        if (client != null) {
            client.logout();
        }
        revertUserConfig();
    }

    public void testCreateShareButQuotaLimitReached() throws OXException, IOException, SAXException, JSONException {
        long now = System.currentTimeMillis();
        FolderObject parent = infostore;
        file = new DefaultFile();
        file.setFolderId(String.valueOf(parent.getObjectID()));
        file.setTitle("QuotaTest_" + now);
        file.setDescription(file.getTitle());
        itm.newAction(file);

        ShareTarget target = new ShareTarget(FolderObject.INFOSTORE, Integer.toString(infostore.getObjectID()), file.getId());
        GetLinkForQuotaLimitRequest getLinkRequest = new GetLinkForQuotaLimitRequest(Collections.singletonList(target));
        getLinkRequest.setBits(createAnonymousGuestPermission().getPermissionBits());

        GetLinkForQuotaLimitResponse getLinkResponse = null;
        try {
            getLinkResponse = client.execute(getLinkRequest);
            JSONObject serverException = getLinkResponse.getServerException();
            String code = (String) serverException.get("code");
            String exceptionCode = QuotaExceptionCodes.QUOTA_EXCEEDED_SHARES.getPrefix() + "-000" + QuotaExceptionCodes.QUOTA_EXCEEDED_SHARES.getNumber();
            Assert.assertEquals(exceptionCode, code);
            String category = (String) serverException.get("error_desc");
            Assert.assertEquals("Quota exceeded for shares. Quota used: 1. Quota limit: 1.", category);
        } catch (Exception e) {
            fail();
        }
    }
}
