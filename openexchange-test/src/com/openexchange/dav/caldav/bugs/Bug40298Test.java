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
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.junit.Test;
import com.openexchange.dav.Config;
import com.openexchange.dav.PropertyNames;
import com.openexchange.dav.caldav.CalDAVTest;

/**
 * {@link Bug40298Test}
 *
 * Thunderbird/Lightning assumes Free/Busy not available in CalDAV
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug40298Test extends CalDAVTest {

    @Test
    public void testResourceProperties() throws Exception {
        /*
         * discover current user principal & owner
         */
        DavPropertyNameSet props = new DavPropertyNameSet();
        props.add(PropertyNames.RESOURCETYPE);
        props.add(PropertyNames.OWNER);
        props.add(PropertyNames.CURRENT_USER_PRINCIPAL);
        props.add(PropertyNames.SUPPORTED_REPORT_SET);
        props.add(PropertyNames.SUPPORTED_CALENDAR_COMPONENT_SET);
        props.add(PropertyNames.GETCTAG);
        PropFindMethod propFind = new PropFindMethod(getWebDAVClient().getBaseURI() + Config.getPathPrefix() + "/caldav/" + encodeFolderID(getDefaultFolderID()), DavConstants.PROPFIND_BY_PROPERTY, props, DavConstants.DEPTH_0);
        MultiStatusResponse[] responses = getWebDAVClient().doPropFind(propFind);
        assertNotNull("got no response", responses);
        MultiStatusResponse response = assertSingleResponse(responses);
        String currentUserPrincipal = extractHref(PropertyNames.CURRENT_USER_PRINCIPAL, response);
        assertNotNull(currentUserPrincipal);
        assertTrue(currentUserPrincipal.contains("/" + getClient().getValues().getUserId()));
    }

}
