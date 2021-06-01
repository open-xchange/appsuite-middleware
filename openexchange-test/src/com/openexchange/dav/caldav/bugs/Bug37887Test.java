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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.HashMap;
import java.util.Map;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.junit.Test;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.GetResponse;
import com.openexchange.dav.Config;
import com.openexchange.dav.PropertyNames;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.caldav.CalDAVTest;
import com.openexchange.groupware.container.FolderObject;

/**
 * {@link Bug37887Test}
 *
 * CalDAV account creation in Mac OS Calendar sometimes fails
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug37887Test extends CalDAVTest {

    @Test
    public void testCalendarColor() throws Exception {
        /*
         * set a calendar color for personal calendar if not yet set
         */
        String color;
        int folderID = Integer.parseInt(getDefaultFolderID());
        GetResponse getResponse = getClient().execute(new com.openexchange.ajax.folder.actions.GetRequest(EnumAPI.OX_NEW, folderID));
        FolderObject folder = getResponse.getFolder();
        if (null == folder.getMeta() || false == folder.getMeta().containsKey("color")) {
            Map<String, Object> meta = folder.getMeta();
            if (null == meta) {
                meta = new HashMap<String, Object>();
            } else {
                meta = new HashMap<String, Object>(meta);
            }
            color = "#000000FF";
            FolderObject toUpdate = new FolderObject(folderID);
            toUpdate.setLastModified(getResponse.getTimestamp());
            meta.put("color", color);
            toUpdate.setMeta(meta);
            getClient().execute(new com.openexchange.ajax.folder.actions.UpdateRequest(EnumAPI.OX_NEW, toUpdate)).getResponse();
        } else {
            color = String.valueOf(folder.getMeta().get("color"));
        }
        /*
         * discover calendar colors via PROPFIND
         */
        DavPropertyNameSet props = new DavPropertyNameSet();
        props.add(PropertyNames.CALENDAR_COLOR);
        PropFindMethod propFind = new PropFindMethod(getWebDAVClient().getBaseURI() + Config.getPathPrefix() + "/caldav/", DavConstants.PROPFIND_BY_PROPERTY, props, DavConstants.DEPTH_1);
        MultiStatusResponse[] responses = getWebDAVClient().doPropFind(propFind);
        assertNotNull("got no response", responses);
        assertTrue("got no responses", 0 < responses.length);
        /*
         * ensure that either all or no collection have the calendar color-property, mixed 404 / 200 propfind responses cause the lousy
         * Mac OS client to crash with a null reference
         */
        Boolean found = null;
        for (MultiStatusResponse response : responses) {
            if ((Config.getPathPrefix() + "/caldav/").equals(response.getHref()) || (Config.getPathPrefix() + "/caldav/schedule-inbox/").equals(response.getHref()) ||
                (Config.getPathPrefix() + "/caldav/schedule-outbox/").equals(response.getHref())) {
                continue;
            }
            if (response.getProperties(StatusCodes.SC_OK).contains(PropertyNames.CALENDAR_COLOR)) {
                if (null == found) {
                    found = Boolean.TRUE;
                } else {
                    assertTrue("calendar-color with both status 200 and 404", found.booleanValue());
                }
            } else if (response.getProperties(StatusCodes.SC_NOT_FOUND).contains(PropertyNames.CALENDAR_COLOR)) {
                if (null == found) {
                    found = Boolean.FALSE;
                } else {
                    assertFalse("calendar-color with both status 200 and 404", found.booleanValue());
                }
            }
        }
    }

}
