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

package com.openexchange.dav.carddav.bugs;

import static org.junit.Assert.assertNotNull;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.security.CurrentUserPrivilegeSetProperty;
import org.apache.jackrabbit.webdav.security.Privilege;
import org.junit.Test;
import com.openexchange.dav.Config;
import com.openexchange.dav.PropertyNames;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.carddav.CardDAVTest;
import com.openexchange.dav.carddav.UserAgents;

/**
 * {@link Bug68510Test}
 *
 * MAC contacts app does not allow to add new contacts to OX on Catalina
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since 7.10.4
 */
public class Bug68510Test extends CardDAVTest {

    @Override
    protected String getDefaultUserAgent() {
        return UserAgents.MACOS_10_15;
    }

    @Test
    public void testPriviligesOnRootCollection() throws Exception {
        DavPropertyNameSet props = new DavPropertyNameSet();
        props.add(PropertyNames.CURRENT_USER_PRIVILEGE_SET);
        String rootCollectionURI = getWebDAVClient().getBaseURI() + Config.getPathPrefix() + "/carddav/";
        PropFindMethod propFind = new PropFindMethod(rootCollectionURI, DavConstants.PROPFIND_BY_PROPERTY, props, DavConstants.DEPTH_0);
        MultiStatusResponse response = assertSingleResponse(getWebDAVClient().doPropFind(propFind));
        DavProperty<?> property = response.getProperties(StatusCodes.SC_OK).get(PropertyNames.CURRENT_USER_PRIVILEGE_SET);
        assertNotNull("No " + PropertyNames.CURRENT_USER_PRIVILEGE_SET, property);
        Privilege bindPrivilege = null;
        for (Privilege privilege : new CurrentUserPrivilegeSetProperty(property).getValue()) {
            if (Privilege.PRIVILEGE_WRITE_CONTENT.equals(privilege)) {
                bindPrivilege = privilege;
                break;
            }
        }
        assertNotNull("No " + Privilege.PRIVILEGE_BIND, bindPrivilege);
    }
}
