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

package com.openexchange.ajax.attach;

import static org.junit.Assert.assertEquals;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.TimeZone;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.attach.actions.AttachRequest;
import com.openexchange.ajax.attach.actions.GetDocumentRequest;
import com.openexchange.ajax.attach.actions.GetDocumentResponse;
import com.openexchange.ajax.contact.action.GetRequest;
import com.openexchange.ajax.contact.action.InsertRequest;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;

/**
 * {@link Bug30701Test}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class Bug30701Test extends AbstractAJAXSession {

    private Contact contactA;

    private int folderID;

    private int attachmentID;

    private TimeZone tz;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        folderID = getClient().getValues().getPrivateContactFolder();
        tz = getClient().getValues().getTimeZone();
        contactA = new Contact();
        contactA.setGivenName("Test");
        contactA.setMiddleName("for");
        contactA.setSurName("Bug 30701");
        contactA.setDisplayName("Test for bug 30701");
        contactA.setParentFolderID(folderID);
        getClient().execute(new InsertRequest(contactA)).fillObject(contactA);
        InputStream data = new ByteArrayInputStream("Test document with arbitrary data".getBytes());
        attachmentID = getClient().execute(new AttachRequest(contactA, "doc.txt", data, "text/plain")).getId();
        contactA = getClient().execute(new GetRequest(contactA, tz)).getContact();
        contactA.getLastModifiedOfNewestAttachment().getTime();
    }

    @Test
    public void testGetDocumentWithOffLenParameter() throws OXException, IOException, JSONException {
        final int length = 8;
        GetDocumentRequest getDocReq = new GetDocumentRequest(folderID, attachmentID, 7, contactA.getObjectID(), "text/plain", 5, length, true);
        GetDocumentResponse getDocResp = getClient().execute(getDocReq);
        assertEquals("Wrong Content-Length in response", length, getDocResp.getContentLength());
    }
}
