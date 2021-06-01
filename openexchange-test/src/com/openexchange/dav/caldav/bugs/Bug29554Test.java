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
import static org.junit.Assert.fail;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.junit.Test;
import org.w3c.dom.Node;
import com.openexchange.dav.Config;
import com.openexchange.dav.PropertyNames;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.caldav.CalDAVTest;

/**
 * {@link Bug29554Test}
 *
 * Mountain Lion: The server responded: &quot;403&quot; to operation CalDAVWriteEntityQueueableOperation.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug29554Test extends CalDAVTest {

    @Test
    public void testSupportedComponentSets() throws Exception {
        /*
         * discover supported component sets of root collection
         */
        DavPropertyNameSet props = new DavPropertyNameSet();
        props.add(PropertyNames.SUPPORTED_CALENDAR_COMPONENT_SETS);
        PropFindMethod propFind = new PropFindMethod(getWebDAVClient().getBaseURI() + Config.getPathPrefix() + "/caldav/", DavConstants.PROPFIND_BY_PROPERTY, props, DavConstants.DEPTH_0);
        MultiStatusResponse[] responses = getWebDAVClient().doPropFind(propFind);
        assertNotNull("got no response", responses);
        assertTrue("got no responses", 0 < responses.length);
        Set<String> comps = new HashSet<String>();
        for (MultiStatusResponse response : responses) {
            if (response.getProperties(StatusCodes.SC_OK).contains(PropertyNames.SUPPORTED_CALENDAR_COMPONENT_SETS)) {
                List<Node> nodes = extractNodeListValue(PropertyNames.SUPPORTED_CALENDAR_COMPONENT_SETS, response);
                assertNotNull(nodes);
                for (Node node : nodes) {
                    comps.add(removeWhitspaceNodes(node.getChildNodes()).item(0).getAttributes().getNamedItem("name").getTextContent());
                }
            } else {
                fail("no multistatus response");
            }
        }
        assertTrue("no VEVENT found", comps.contains("VEVENT"));
        assertTrue("no VTODO found", comps.contains("VTODO"));
    }

}
