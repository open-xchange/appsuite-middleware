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

import static com.openexchange.ajax.framework.AJAXClient.User.User1;
import static org.junit.Assert.assertEquals;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.TimeZone;
import org.json.JSONException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.attach.actions.AllRequest;
import com.openexchange.ajax.attach.actions.AllResponse;
import com.openexchange.ajax.attach.actions.AttachRequest;
import com.openexchange.ajax.contact.action.DeleteRequest;
import com.openexchange.ajax.contact.action.GetRequest;
import com.openexchange.ajax.contact.action.InsertRequest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.search.Order;

/**
 * {@link Bug26544Test}
 * 
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class Bug26544Test {

    private AJAXClient client;

    private Contact contactA;

    private TimeZone tz;

    @Before
    public void setUp() throws Exception {
        client = new AJAXClient(User1);
        tz = client.getValues().getTimeZone();
        int folderId = client.getValues().getPrivateContactFolder();
        contactA = new Contact();
        contactA.setDisplayName("Test for bug 26544");
        contactA.setParentFolderID(folderId);
        client.execute(new InsertRequest(contactA)).fillObject(contactA);
        client.execute(new AttachRequest(contactA, "C.txt", new ByteArrayInputStream("Test C".getBytes()), "text/plain")).getId();
        client.execute(new AttachRequest(contactA, "A.txt", new ByteArrayInputStream("Test A".getBytes()), "text/plain")).getId();
        client.execute(new AttachRequest(contactA, "B.txt", new ByteArrayInputStream("Test B".getBytes()), "text/plain")).getId();
        contactA = client.execute(new GetRequest(contactA, tz)).getContact();
        contactA.getLastModifiedOfNewestAttachment().getTime();
    }

    @After
    public void tearDown() throws Exception {
        client.execute(new DeleteRequest(contactA));
        client.logout();
    }

    @Test
    public void testAllRequestWithSortOrder() throws OXException, IOException, JSONException {
        AJAXClient client = new AJAXClient(User1);
        int cols[] = {800, 801, 802, 803, 804, 805, 806 };

        // test sort by id
        AllRequest allRequest = new AllRequest(contactA, cols, 1, Order.ASCENDING);
        AllResponse allResponse = client.execute(allRequest);
        List<AttachmentMetadata> attachmentMetadata = allResponse.getAttachments();
        assertEquals("Incorrect amount of attachments", 3, attachmentMetadata.size());
        assertEquals("Wrong sort order", "C.txt", attachmentMetadata.get(0).getFilename());
        assertEquals("Wrong sort order", "A.txt", attachmentMetadata.get(1).getFilename());
        assertEquals("Wrong sort order", "B.txt", attachmentMetadata.get(2).getFilename());
        
        // test sort by filename
        allRequest = new AllRequest(contactA, cols, 803, Order.ASCENDING);
        allResponse = client.execute(allRequest);
        attachmentMetadata = allResponse.getAttachments();
        assertEquals("Incorrect amount of attachments", 3, attachmentMetadata.size());
        assertEquals("Wrong sort order", "A.txt", attachmentMetadata.get(0).getFilename());
        assertEquals("Wrong sort order", "B.txt", attachmentMetadata.get(1).getFilename());
        assertEquals("Wrong sort order", "C.txt", attachmentMetadata.get(2).getFilename());
    }

    @Test
    public void testAllRequestWithoutSortOrder() throws OXException, IOException, JSONException {
        AJAXClient client = new AJAXClient(User1);
        int cols[] = {800, 801, 802, 803, 804, 805, 806 };
        AllRequest allRequest = new AllRequest(contactA, cols);
        AllResponse allResponse = client.execute(allRequest);
        List<AttachmentMetadata> attachmentMetadata = allResponse.getAttachments();
        assertEquals("Incorrect amount of attachments", 3, attachmentMetadata.size());
        assertEquals("Wrong sort order", "C.txt", attachmentMetadata.get(0).getFilename());
        assertEquals("Wrong sort order", "A.txt", attachmentMetadata.get(1).getFilename());
        assertEquals("Wrong sort order", "B.txt", attachmentMetadata.get(2).getFilename());
    }
}
