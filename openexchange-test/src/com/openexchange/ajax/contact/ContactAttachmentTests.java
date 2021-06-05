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

package com.openexchange.ajax.contact;

import static org.junit.Assert.assertEquals;
import java.io.ByteArrayInputStream;
import java.util.Date;
import java.util.TimeZone;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.attach.actions.AttachRequest;
import com.openexchange.ajax.contact.action.AllRequest;
import com.openexchange.ajax.contact.action.GetRequest;
import com.openexchange.ajax.contact.action.GetResponse;
import com.openexchange.ajax.contact.action.InsertRequest;
import com.openexchange.ajax.contact.action.ListRequest;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.ajax.framework.CommonListResponse;
import com.openexchange.ajax.framework.ListIDs;
import com.openexchange.groupware.container.Contact;

/**
 * Attachment tests for contacts.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class ContactAttachmentTests extends AbstractAJAXSession {

    private int folderId;

    private TimeZone tz;

    private Contact contact;

    private int attachmentId;

    private Date creationDate;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        folderId = getClient().getValues().getPrivateContactFolder();
        tz = getClient().getValues().getTimeZone();
        contact = new Contact();
        contact.setParentFolderID(folderId);
        contact.setDisplayName("Test contact for testing attachments");
        getClient().execute(new InsertRequest(contact)).fillObject(contact);
        attachmentId = getClient().execute(new AttachRequest(contact, "test.txt", new ByteArrayInputStream("Test".getBytes()), "text/plain")).getId();
        com.openexchange.ajax.attach.actions.GetResponse response = getClient().execute(new com.openexchange.ajax.attach.actions.GetRequest(contact, attachmentId));
        long timestamp = response.getAttachment().getCreationDate().getTime();
        creationDate = new Date(timestamp - tz.getOffset(timestamp));
    }

    @Test
    public void testLastModifiedOfNewestAttachmentWithGet() throws Throwable {
        GetResponse response = getClient().execute(new GetRequest(contact.getParentFolderID(), contact.getObjectID(), tz));
        contact.setLastModified(response.getTimestamp());
        Contact test = response.getContact();
        assertEquals("Creation date of attachment does not match.", creationDate, test.getLastModifiedOfNewestAttachment());
    }

    @Test
    public void testLastModifiedOfNewestAttachmentWithAll() throws Throwable {
        CommonAllResponse response = getClient().execute(new AllRequest(contact.getParentFolderID(), new int[] { Contact.OBJECT_ID, Contact.LAST_MODIFIED_OF_NEWEST_ATTACHMENT }));
        contact.setLastModified(response.getTimestamp());
        Contact test = null;
        int objectIdPos = response.getColumnPos(Contact.OBJECT_ID);
        int lastModifiedOfNewestAttachmentPos = response.getColumnPos(Contact.LAST_MODIFIED_OF_NEWEST_ATTACHMENT);
        for (Object[] objA : response) {
            if (contact.getObjectID() == Integer.parseInt(String.class.cast(objA[objectIdPos]))) {
                test = new Contact();
                test.setLastModifiedOfNewestAttachment(new Date(((Long) objA[lastModifiedOfNewestAttachmentPos]).longValue()));
                break;
            }
        }
        Assert.assertNotNull("Can not find the created appointment with an attachment.", test);
        assertEquals("Creation date of attachment does not match.", creationDate, test.getLastModifiedOfNewestAttachment());
    }

    @Test
    public void testLastModifiedOfNewestAttachmentWithList() throws Throwable {
        CommonListResponse response = getClient().execute(new ListRequest(ListIDs.l(new int[] { contact.getParentFolderID(), contact.getObjectID() }), new int[] { Contact.OBJECT_ID, Contact.LAST_MODIFIED_OF_NEWEST_ATTACHMENT }));
        contact.setLastModified(response.getTimestamp());
        Contact test = null;
        int objectIdPos = response.getColumnPos(Contact.OBJECT_ID);
        int lastModifiedOfNewestAttachmentPos = response.getColumnPos(Contact.LAST_MODIFIED_OF_NEWEST_ATTACHMENT);
        for (Object[] objA : response) {
            if (contact.getObjectID() == Integer.parseInt(String.class.cast(objA[objectIdPos]))) {
                test = new Contact();
                test.setLastModifiedOfNewestAttachment(new Date(((Long) objA[lastModifiedOfNewestAttachmentPos]).longValue()));
                break;
            }
        }
        Assert.assertNotNull("Can not find the created appointment with an attachment.", test);
        assertEquals("Creation date of attachment does not match.", creationDate, test.getLastModifiedOfNewestAttachment());
    }
}
