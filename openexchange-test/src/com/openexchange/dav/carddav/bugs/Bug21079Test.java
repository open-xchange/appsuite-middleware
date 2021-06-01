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
import static org.junit.Assert.assertTrue;
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

/**
 * {@link Bug21079Test}
 *
 * Contacts can't be edited in Mac OS 10.7.3 Addressbook
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug21079Test extends CardDAVTest {

    public Bug21079Test() {
        super();
    }

    /**
     * Checks that at least one resource with "write-content" privileges is present in the carddav collections
     * 
     * @throws Exception
     */
    @Test
    public void testCheckWriteContentPrivilige() throws Exception {
        final DavPropertyNameSet props = new DavPropertyNameSet();
        props.add(PropertyNames.CURRENT_USER_PRIVILEGE_SET);
        final PropFindMethod propFind = new PropFindMethod(super.getWebDAVClient().getBaseURI() + Config.getPathPrefix() + "/carddav/", DavConstants.PROPFIND_BY_PROPERTY, props, DavConstants.DEPTH_1);
        final MultiStatusResponse[] responses = super.getWebDAVClient().doPropFind(propFind);
        assertNotNull("got no response", responses);
        assertTrue("got no responses", 0 < responses.length);
        boolean canWriteContent = false;
        for (final MultiStatusResponse response : responses) {
            final DavProperty<?> property = response.getProperties(StatusCodes.SC_OK).get(PropertyNames.CURRENT_USER_PRIVILEGE_SET);
            if (null != property) {
                final CurrentUserPrivilegeSetProperty privilegeSet = new CurrentUserPrivilegeSetProperty(property);
                for (final Privilege privilege : privilegeSet.getValue()) {
                    if (Privilege.PRIVILEGE_WRITE_CONTENT.equals(privilege)) {
                        canWriteContent = true;
                        break;
                    }
                }
                if (canWriteContent) {
                    break;
                }
            }
        }
        assertTrue("no write-content privilege found", canWriteContent);
    }
}
