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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpState;
import org.apache.jackrabbit.webdav.client.methods.ReportMethod;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.junit.Test;
import com.openexchange.dav.PropertyNames;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.SyncToken;
import com.openexchange.dav.carddav.CardDAVTest;
import com.openexchange.dav.carddav.Photos;
import com.openexchange.dav.reports.SyncCollectionReportInfo;
import com.openexchange.groupware.container.Contact;

/**
 * {@link Bug48687Test}
 *
 * carddav data with xD at the end of all lines
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.3
 */
public class Bug48687Test extends CardDAVTest {

    /**
     * Initializes a new {@link Bug48687Test}.
     */
    public Bug48687Test() {
        super();
    }

    @Test
    public void testLineEndings() throws Exception {
        /*
         * fetch sync token for later synchronization
         */
        SyncToken syncToken = new SyncToken(fetchSyncToken());
        /*
         * create contact on server
         */
        String uid = randomUID();
        Contact contact = new Contact();
        contact.setImage1(Photos.PNG_100x100);
        contact.setImageContentType("image/png");
        contact.setUid(uid);
        rememberForCleanUp(create(contact));
        /*
         * sync collection
         */
        DavPropertyNameSet props = new DavPropertyNameSet();
        props.add(PropertyNames.GETETAG);
        props.add(PropertyNames.ADDRESS_DATA);
        SyncCollectionReportInfo reportInfo = new SyncCollectionReportInfo(syncToken.getToken(), props);
        String uri = getBaseUri() + buildCollectionHref(getDefaultCollectionName());
        String responseBody = null;
        ReportMethod report = null;
        try {
            report = new ReportMethod(uri, reportInfo) {

                @Override
                protected void processResponseBody(HttpState httpState, HttpConnection httpConnection) {
                    // prevent premature response body processing
                }

            };
            assertEquals("unexpected http status", StatusCodes.SC_MULTISTATUS, getWebDAVClient().executeMethod(report));
            responseBody = report.getResponseBodyAsString();
        } finally {
            release(report);
        }
        /*
         * check response body
         */
        assertNotNull("got no response", responseBody);
        assertFalse("unexpected carriage return in response", responseBody.contains("#xD;"));
    }
}
