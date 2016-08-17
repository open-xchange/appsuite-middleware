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
        PropFindMethod propFind = new PropFindMethod(getWebDAVClient().getBaseURI() + "/caldav/", DavConstants.PROPFIND_BY_PROPERTY, props, DavConstants.DEPTH_1);
        MultiStatusResponse[] responses = getWebDAVClient().doPropFind(propFind);
        assertNotNull("got no response", responses);
        assertTrue("got no responses", 0 < responses.length);
        /*
         * ensure that either all or no collection have the calendar color-property, mixed 404 / 200 propfind responses cause the lousy
         * Mac OS client to crash with a null reference
         */
        Boolean found = null;
        for (MultiStatusResponse response : responses) {
            if ("/caldav/".equals(response.getHref()) || "/caldav/schedule-inbox/".equals(response.getHref()) || "/caldav/schedule-outbox/".equals(response.getHref())) {
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
