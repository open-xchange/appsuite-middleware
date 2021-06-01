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
import org.w3c.dom.Node;
import com.openexchange.dav.Config;
import com.openexchange.dav.PropertyNames;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.caldav.CalDAVTest;
import com.openexchange.java.Strings;

/**
 * {@link Bug32897Test}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug32897Test extends CalDAVTest {

    @Test
    public void testDefaultAlarms() throws Exception {
        /*
         * discover default alarms
         */
        final DavPropertyNameSet props = new DavPropertyNameSet();
        props.add(PropertyNames.DEFAULT_ALARM_VEVENT_DATE);
        props.add(PropertyNames.DEFAULT_ALARM_VEVENT_DATETIME);
        props.add(PropertyNames.SUPPORTED_CALENDAR_COMPONENT_SET);
        final PropFindMethod propFind = new PropFindMethod(getWebDAVClient().getBaseURI() + Config.getPathPrefix() + "/caldav/", DavConstants.PROPFIND_BY_PROPERTY, props, DavConstants.DEPTH_1);
        final MultiStatusResponse[] responses = getWebDAVClient().doPropFind(propFind);
        assertNotNull("got no response", responses);
        assertTrue("got no responses", 0 < responses.length);
        for (final MultiStatusResponse response : responses) {
            if (response.getProperties(StatusCodes.SC_OK).contains(PropertyNames.SUPPORTED_CALENDAR_COMPONENT_SET)) {
                final Node node = extractNodeValue(PropertyNames.SUPPORTED_CALENDAR_COMPONENT_SET, response);
                if (null != node && null != node.getAttributes() && null != node.getAttributes().getNamedItem("name") && "VEVENT".equals(node.getAttributes().getNamedItem("name").getTextContent())) {
                    Object defaultAlarmVEventDate = response.getProperties(StatusCodes.SC_OK).get(PropertyNames.DEFAULT_ALARM_VEVENT_DATE).getValue();
                    assertTrue("wrong default alarm", null == defaultAlarmVEventDate || Strings.isEmpty(String.valueOf(defaultAlarmVEventDate)));
                    Object defaultAlarmVEventDatetime = response.getProperties(StatusCodes.SC_OK).get(PropertyNames.DEFAULT_ALARM_VEVENT_DATETIME).getValue();
                    assertTrue("wrong default alarm", null == defaultAlarmVEventDatetime || Strings.isEmpty(String.valueOf(defaultAlarmVEventDatetime)));
                }
            }
        }

    }

}
