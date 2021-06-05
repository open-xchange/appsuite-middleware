/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
