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

package com.openexchange.dav.caldav.bugs;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.List;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import com.openexchange.dav.Config;
import com.openexchange.dav.PropertyNames;
import com.openexchange.dav.caldav.Abstract2UserCalDAVTest;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.test.CalendarTestManager;
import com.openexchange.test.common.test.PermissionTools;
import com.openexchange.testing.httpclient.models.UserData;
import com.openexchange.testing.httpclient.models.UserResponse;
import com.openexchange.testing.httpclient.modules.UserApi;

/**
 * {@link MWB1096Test}
 *
 * XML serialization problems when handling PROPFINDs in CalDAV
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.6
 */
public class MWB1096Test extends Abstract2UserCalDAVTest {

    private CalendarTestManager manager2;
    private FolderObject sharedFolder;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        /*
         * as user 1, create subfolder shared to user b
         */
        manager2 = new CalendarTestManager(client2);
        manager2.setFailOnError(true);
        sharedFolder = new FolderObject();
        sharedFolder.setModule(FolderObject.CALENDAR);
        sharedFolder.setParentFolderID(catm.getPrivateFolder());
        sharedFolder.setPermissions(
            PermissionTools.P(Integer.valueOf(getClient().getValues().getUserId()),
            PermissionTools.ADMIN, Integer.valueOf(client2.getValues().getUserId()), "vr")
        );
        sharedFolder.setFolderName(randomUID());
        sharedFolder = ftm.insertFolderOnServer(sharedFolder);
    }

    @Test
    public void testInvitePropertyEscaping() throws Exception {
        /*
         * as user 2, change display name to contain a '&' character
         */
        String userId = String.valueOf(client2.getValues().getUserId());
        UserResponse userResponse = new UserApi(apiClient2).getUser(userId);
        UserData userData = new UserData();
        userData.setFirstName("Klaus & Klaus");
        userData.setLastName("Fischer");
        userData.setDisplayName("Klaus & Klaus Fischer");
        new UserApi(apiClient2).updateUser(String.valueOf(client2.getValues().getUserId()), userResponse.getTimestamp(), userData);
        /*
         * as user1, discover invite property from shares folder
         */
        DavPropertyNameSet props = new DavPropertyNameSet();
        props.add(PropertyNames.INVITE);
        String uri = getWebDAVClient().getBaseURI() + Config.getPathPrefix() + "/caldav/" + encodeFolderID(String.valueOf(sharedFolder.getObjectID()));
        PropFindMethod propFind = new PropFindMethod(uri, DavConstants.PROPFIND_BY_PROPERTY, props, DavConstants.DEPTH_0);
        MultiStatusResponse[] responses = getWebDAVClient().doPropFind(propFind);
        assertNotNull("got no response", responses);
        assertTrue("got no responses", 1 == responses.length);

        boolean found = false;
        List<Node> nodes = extractNodeListValue(PropertyNames.INVITE, responses[0]);
        assertNotNull(nodes);
        for (Node node : nodes) {
            NodeList childNodeList = removeWhitspaceNodes(node.getChildNodes());
            for (int i = 0; i < childNodeList.getLength(); i++) {
                Node item = childNodeList.item(i);
                if ("common-name".equals(item.getLocalName()) && userData.getDisplayName().equals(item.getTextContent())) {
                    found = true;
                }
            }
        }
        assertTrue(userData.getDisplayName() + " not found", found);
    }

}
