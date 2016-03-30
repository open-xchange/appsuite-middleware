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
import java.util.TimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.attach.actions.AttachRequest;
import com.openexchange.ajax.attach.actions.ListRequest;
import com.openexchange.ajax.attach.actions.ListResponse;
import com.openexchange.ajax.contact.action.DeleteRequest;
import com.openexchange.ajax.contact.action.GetRequest;
import com.openexchange.ajax.contact.action.InsertRequest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.MultipleRequest;
import com.openexchange.groupware.attach.AttachmentField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.java.util.TimeZones;

/**
 * {@link Bug24876Test}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class Bug24876Test {

    private AJAXClient client;
    private Contact contact;
    private TimeZone tz;
    private long timestamp;
    private int attachmentId;

    public Bug24876Test() {
        super();
    }

    @Before
    public void setUp() throws Exception {
        client = new AJAXClient(User1);
        tz = client.getValues().getTimeZone();
        int folderId = client.getValues().getPrivateContactFolder();
        contact = new Contact();
        contact.setDisplayName("Test for bug 24876");
        contact.setParentFolderID(folderId);
        client.execute(new InsertRequest(contact)).fillObject(contact);
        attachmentId = client.execute(new AttachRequest(contact, "test.txt", new ByteArrayInputStream("Test".getBytes()), "text/plain")).getId();
        contact = client.execute(new GetRequest(contact, tz)).getContact();
        timestamp = contact.getLastModifiedOfNewestAttachment().getTime();
    }

    @After
    public void tearDown() throws Exception {
        client.execute(new DeleteRequest(contact));
        client.logout();
    }

    @Test
    public void testList() throws Exception {
        ListResponse response = client.execute(new ListRequest(contact, new int[] { attachmentId }, new int[] { AttachmentField.CREATION_DATE }, TimeZones.UTC));
        assertEquals("attachment listing did not return the only created attachment", 1, response.getArray().length);
        long toTest = 0;
        for (Object[] values : response) {
            toTest = ((Long) values[response.getColumnPos(AttachmentField.CREATION_DATE)]).longValue();
        }
        assertEquals("time stamp for attachments is wrong in list request", timestamp, toTest);
    }

    /**
     * Test is disabled. Multiple servlet needs parameter module which conflicts with module parameter of attachment list request.
     */
    public void testMultipleList() throws Exception {
        ListResponse response = client.execute(MultipleRequest.create(new ListRequest(contact, new int[] { attachmentId }, new int[] { AttachmentField.CREATION_DATE }, TimeZones.UTC))).getResponse(0);
        assertEquals("attachment listing did not return the only created attachment", 1, response.getArray().length);
        long toTest = 0;
        for (Object[] values : response) {
            toTest = ((Long) values[response.getColumnPos(AttachmentField.CREATION_DATE)]).longValue();
        }
        assertEquals("time stamp for attachments is wrong in list request", timestamp, toTest);
    }
}
