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
import org.junit.AfterClass;
import org.junit.BeforeClass;
import com.openexchange.admin.rmi.OXUserInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.framework.UserValues;
import com.openexchange.ajax.infostore.actions.InfostoreTestManager;
import com.openexchange.ajax.share.ShareTest;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.configuration.AJAXConfig.Property;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.modules.Module;


/**
 * {@link QuotaTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */
public class QuotaTest extends ShareTest {

    private static final String COM_OPENEXCHANGE_QUOTA_SHARE = "com.openexchange.quota.share";

    private InfostoreTestManager itm;

    private FolderObject infostore;

    private DefaultFile file;

    public QuotaTest(String name) {
        super(name);
    }

    @BeforeClass
    public static void beforeClass() throws Exception {
        User oxadmin = AJAXClient.User.OXAdmin;
        AJAXClient client2 = new AJAXClient(AJAXClient.User.User2);
        com.openexchange.admin.rmi.dataobjects.User user = new com.openexchange.admin.rmi.dataobjects.User(client2.getValues().getUserId());
        user.setUserAttribute("config", COM_OPENEXCHANGE_QUOTA_SHARE, "0");
        Credentials credentials = new Credentials(AJAXConfig.getProperty(oxadmin.getLogin()), AJAXConfig.getProperty(oxadmin.getPassword()));
        OXUserInterface iface = (OXUserInterface) Naming.lookup("rmi://" + AJAXConfig.getProperty(Property.RMI_HOST) + ":1099/" + OXUserInterface.RMI_NAME);
        iface.change(new Context(client2.getValues().getContextId()), user, credentials);
        client2.logout();
    }

    @AfterClass
    public static void afterClass() throws Exception {
        User oxadmin = AJAXClient.User.OXAdmin;
        AJAXClient client2 = new AJAXClient(AJAXClient.User.User2);
        com.openexchange.admin.rmi.dataobjects.User user = new com.openexchange.admin.rmi.dataobjects.User(client2.getValues().getUserId());
        user.setUserAttribute("config", COM_OPENEXCHANGE_QUOTA_SHARE, null);
        Credentials credentials = new Credentials(AJAXConfig.getProperty(oxadmin.getLogin()), AJAXConfig.getProperty(oxadmin.getPassword()));
        OXUserInterface iface = (OXUserInterface) Naming.lookup("rmi://" + AJAXConfig.getProperty(Property.RMI_HOST) + ":1099/" + OXUserInterface.RMI_NAME);
        iface.change(new Context(client2.getValues().getContextId()), user, credentials);
        client2.logout();
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        itm = new InfostoreTestManager(client);

        UserValues values = client.getValues();
        infostore = insertPrivateFolder(EnumAPI.OX_NEW, Module.INFOSTORE.getFolderConstant(), values.getPrivateInfostoreFolder());

        long now = System.currentTimeMillis();
        FolderObject parent = infostore;
        file = new DefaultFile();
        file.setFolderId(String.valueOf(parent.getObjectID()));
        file.setTitle("QuotaTest_" + now);
        file.setDescription(file.getTitle());
        itm.newAction(file);
    }

    @Override
    public void tearDown() throws Exception {
        if (itm != null) {
            itm.cleanUp();
        }

        super.tearDown();
    }

    public void testQuota() {

    }

}
