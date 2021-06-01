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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.apache.jackrabbit.webdav.client.methods.AclMethod;
import org.apache.jackrabbit.webdav.security.AclProperty;
import org.apache.jackrabbit.webdav.security.AclProperty.Ace;
import org.apache.jackrabbit.webdav.security.Principal;
import org.apache.jackrabbit.webdav.security.Privilege;
import org.junit.Assert;
import org.junit.Test;
import com.openexchange.dav.Config;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.caldav.Abstract2UserCalDAVTest;
import com.openexchange.dav.caldav.UserAgents;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;

/**
 * {@link Bug61998Test}
 *
 * Not able to change access control in emClient
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.2
 */
public class Bug61998Test extends Abstract2UserCalDAVTest {

    @Override
    protected String getDefaultUserAgent() {
        return UserAgents.EM_CLIENT_6_0;
    }

    @Test
    public void testAcl() throws Exception {
        /*
         * create calendar folder on server
         */
        FolderObject folder = createFolder(randomUID());
        String href = Config.getPathPrefix() + "/caldav/" + encodeFolderID(String.valueOf(folder.getObjectID()));
        /*
         * prepare ace grant for user 1 and user 2
         */
        Principal principal1 = Principal.getHrefPrincipal(Config.getPathPrefix() + "/principals/users/" + getClient().getValues().getUserId());
        Privilege[] privileges1 = {
            Privilege.PRIVILEGE_BIND, Privilege.PRIVILEGE_READ, Privilege.PRIVILEGE_READ_ACL,
            Privilege.PRIVILEGE_READ_CURRENT_USER_PRIVILEGE_SET,  Privilege.PRIVILEGE_UNBIND, Privilege.PRIVILEGE_WRITE,
            Privilege.PRIVILEGE_WRITE_ACL, Privilege.PRIVILEGE_WRITE_CONTENT, Privilege.PRIVILEGE_WRITE_PROPERTIES
        };
        Ace grantAce1 = AclProperty.createGrantAce(principal1, privileges1, false, false, null);
        Principal principal2 = Principal.getHrefPrincipal(Config.getPathPrefix() + "/principals/users/" + client2.getValues().getUserId());
        Privilege[] privileges2 = { Privilege.PRIVILEGE_READ };
        Ace grantAce2 = AclProperty.createGrantAce(principal2, privileges2, false, false, null);
        /*
         * perform acl request on collection
         */
        AclMethod acl = null;
        try {
            acl = new AclMethod(getBaseUri() + href, new AclProperty(new Ace[] { grantAce1, grantAce2 }));
            Assert.assertEquals("response code wrong", StatusCodes.SC_OK, getWebDAVClient().executeMethod(acl));
        } finally {
            release(acl);
        }
        /*
         * verify updated permissions
         */
        folder = getFolder(folder.getObjectID());
        assertEquals(2, folder.getPermissions().size());
        OCLPermission permission2 = null;
        for (OCLPermission permission : folder.getPermissions()) {
            if (client2.getValues().getUserId() == permission.getEntity()) {
                permission2 = permission;
            }
        }
        assertNotNull(permission2);
        assertTrue(permission2.canReadAllObjects());
    }

}
