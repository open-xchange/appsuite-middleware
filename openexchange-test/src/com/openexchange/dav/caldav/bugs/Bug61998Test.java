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
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.caldav.CalDAVTest;
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
public class Bug61998Test extends CalDAVTest {

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
        String href = "/caldav/" + encodeFolderID(String.valueOf(folder.getObjectID()));
        /*
         * prepare ace grant for user 1 and user 2
         */
        Principal principal1 = Principal.getHrefPrincipal("/principals/users/" + getClient().getValues().getUserId());
        Privilege[] privileges1 = { 
            Privilege.PRIVILEGE_BIND, Privilege.PRIVILEGE_READ, Privilege.PRIVILEGE_READ_ACL, 
            Privilege.PRIVILEGE_READ_CURRENT_USER_PRIVILEGE_SET,  Privilege.PRIVILEGE_UNBIND, Privilege.PRIVILEGE_WRITE, 
            Privilege.PRIVILEGE_WRITE_ACL, Privilege.PRIVILEGE_WRITE_CONTENT, Privilege.PRIVILEGE_WRITE_PROPERTIES
        };
        Ace grantAce1 = AclProperty.createGrantAce(principal1, privileges1, false, false, null);
        Principal principal2 = Principal.getHrefPrincipal("/principals/users/" + getClient2().getValues().getUserId());
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
            if (getClient2().getValues().getUserId() == permission.getEntity()) {
                permission2 = permission;
            }
        }
        assertNotNull(permission2);
        assertTrue(permission2.canReadAllObjects());
    }

}
