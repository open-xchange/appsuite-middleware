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

package com.openexchange.ajax.attach;

import static org.junit.Assert.assertEquals;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.TimeZone;
import org.json.JSONException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.attach.actions.AttachRequest;
import com.openexchange.ajax.attach.actions.GetDocumentRequest;
import com.openexchange.ajax.attach.actions.GetDocumentResponse;
import com.openexchange.ajax.contact.action.DeleteRequest;
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

    @Override
    @After
    public void tearDown() throws Exception {
        try {
            getClient().execute(new DeleteRequest(contactA));
            getClient().logout();
        } finally {
            super.tearDown();
        }
    }

    @Test
    public void testGetDocumentWithOffLenParameter() throws OXException, IOException, JSONException {
        final int length = 8;
        GetDocumentRequest getDocReq = new GetDocumentRequest(folderID, attachmentID, 7, contactA.getObjectID(), "text/plain", 5, length, true);
        GetDocumentResponse getDocResp = getClient().execute(getDocReq);
        assertEquals("Wrong Content-Length in response", length, getDocResp.getContentLength());
    }
}
