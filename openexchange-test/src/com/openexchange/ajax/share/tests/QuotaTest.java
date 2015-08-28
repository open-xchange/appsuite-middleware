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
import com.openexchange.ajax.share.ShareTest;
import com.openexchange.ajax.share.actions.GetLinkRequest;
import com.openexchange.ajax.share.actions.GetLinkResponse;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.configuration.AJAXConfig.Property;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.quota.QuotaExceptionCodes;
import com.openexchange.share.ShareTarget;

/**
 * {@link QuotaTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */
public class QuotaTest extends ShareTest {

    /**
     * Initializes a new {@link QuotaTest}.
     *
     * @param name The test name
     */
    public QuotaTest(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        com.openexchange.admin.rmi.dataobjects.User user = new com.openexchange.admin.rmi.dataobjects.User(client.getValues().getUserId());
        user.setUserAttribute("config", "com.openexchange.quota.share_links", "1");
        user.setUserAttribute("config", "com.openexchange.quota.invite_guests", "1");
        Credentials credentials = new Credentials(AJAXConfig.getProperty(AJAXClient.User.OXAdmin.getLogin()), AJAXConfig.getProperty(AJAXClient.User.OXAdmin.getPassword()));
        OXUserInterface iface = (OXUserInterface) Naming.lookup("rmi://" + AJAXConfig.getProperty(Property.RMI_HOST) + ":1099/" + OXUserInterface.RMI_NAME);
        iface.change(new Context(client.getValues().getContextId()), user, credentials);
    }

    @Override
    public void tearDown() throws Exception {
        try {
            com.openexchange.admin.rmi.dataobjects.User user = new com.openexchange.admin.rmi.dataobjects.User(client.getValues().getUserId());
            user.setUserAttribute("config", "com.openexchange.quota.share_links", null);
            user.setUserAttribute("config", "com.openexchange.quota.invite_guests", null);
            Credentials credentials = new Credentials(AJAXConfig.getProperty(AJAXClient.User.OXAdmin.getLogin()), AJAXConfig.getProperty(AJAXClient.User.OXAdmin.getPassword()));
            OXUserInterface iface = (OXUserInterface) Naming.lookup("rmi://" + AJAXConfig.getProperty(Property.RMI_HOST) + ":1099/" + OXUserInterface.RMI_NAME);
            iface.change(new Context(client.getValues().getContextId()), user, credentials);
        } finally {
            super.tearDown();
        }
    }

    public void testShareLinkButQuotaLimitReached() throws Exception {
        /*
         * try and create more links than allowed
         */
        FolderObject folder = insertPrivateFolder(EnumAPI.OX_NEW, FolderObject.INFOSTORE, getDefaultFolder(FolderObject.INFOSTORE));
        GetLinkRequest getLinkRequest = new GetLinkRequest(new ShareTarget(FolderObject.INFOSTORE, String.valueOf(folder.getObjectID())));
        getLinkRequest.setFailOnError(false);
        GetLinkResponse getLinkResponse = client.execute(getLinkRequest);
        if (getLinkResponse.hasError()) {
            /*
             * one or more share links existed before, expect appropriate exception
             */
            OXException e = getLinkResponse.getException();
            assertTrue("Unexpected exception: " + e, QuotaExceptionCodes.QUOTA_EXCEEDED_SHARES.equals(e));
        } else {
            /*
             * no errors in first getLink request - a second link will exceed the quota for sure
             */
            folder = insertPrivateFolder(EnumAPI.OX_NEW, FolderObject.INFOSTORE, getDefaultFolder(FolderObject.INFOSTORE));
            getLinkRequest = new GetLinkRequest(new ShareTarget(FolderObject.INFOSTORE, String.valueOf(folder.getObjectID())));
            getLinkRequest.setFailOnError(false);
            getLinkResponse = client.execute(getLinkRequest);
            assertTrue("No errors in response", getLinkResponse.hasError());
            OXException e = getLinkResponse.getException();
            assertTrue("Unexpected exception: " + e, QuotaExceptionCodes.QUOTA_EXCEEDED_SHARES.equals(e));
        }
    }

    public void testInviteGuestButQuotaLimitReached() throws Exception {
        /*
         * try and invite more guests than allowed
         */
        FolderObject folder = insertPrivateFolder(EnumAPI.OX_NEW, FolderObject.INFOSTORE, getDefaultFolder(FolderObject.INFOSTORE));
        folder.getPermissions().add(createNamedGuestPermission(randomUID() + "@example.com", randomUID()));
        UpdateRequest request = new UpdateRequest(EnumAPI.OX_NEW, folder);
        request.setFailOnError(false);
        InsertResponse updateResponse = client.execute(request);
        if (updateResponse.hasError()) {
            /*
             * one or more guest invitations existed before, expect appropriate exception
             */
            OXException e = updateResponse.getException();
            assertTrue("Unexpected exception: " + e, QuotaExceptionCodes.QUOTA_EXCEEDED_SHARES.equals(e));
        } else {
            /*
             * no errors during first invitation - a second guest will exceed the quota for sure
             */
            folder = getFolder(EnumAPI.OX_NEW, folder.getObjectID());
            folder.getPermissions().add(createNamedGuestPermission(randomUID() + "@example.com", randomUID()));
            request = new UpdateRequest(EnumAPI.OX_NEW, folder);
            request.setFailOnError(false);
            updateResponse = client.execute(request);
            assertTrue("No errors in response", updateResponse.hasError());
            OXException e = updateResponse.getException();
            assertTrue("Unexpected exception: " + e, QuotaExceptionCodes.QUOTA_EXCEEDED_SHARES.equals(e));
        }
    }

}
