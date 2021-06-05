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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.junit.Test;
import com.openexchange.ajax.attach.actions.AttachRequest;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.groupware.attach.impl.AttachmentImpl;
import com.openexchange.groupware.container.Contact;

public class NewTest extends AbstractContactTest {

    @Test
    public void testNew() throws Exception {
        final Contact contactObj = createContactObject();
        insertContact(contactObj);
    }

    @Test
    public void testNewWithDistributionList() throws Exception {
        final Contact contactEntry = createContactObject();
        contactEntry.setEmail1("internalcontact@x.de");
        final int contactId = insertContact(contactEntry);
        contactEntry.setObjectID(contactId);

        createContactWithDistributionList("testNewWithDistributionList", contactEntry);
    }

    @Test
    public void testNewContactWithAttachment() throws Exception {
        final Contact contactObj = createContactObject();
        final int objectId = insertContact(contactObj);
        contactObj.setObjectID(objectId);

        final AttachmentMetadata attachmentObj = new AttachmentImpl();
        attachmentObj.setFilename(System.currentTimeMillis() + "test1.txt");
        attachmentObj.setModuleId(Types.CONTACT);
        attachmentObj.setAttachedId(objectId);
        attachmentObj.setFolderId(contactFolderId);
        attachmentObj.setRtfFlag(false);
        attachmentObj.setFileMIMEType("plain/text");

        InputStream byteArrayInputStream = new ByteArrayInputStream("t1".getBytes());
        AttachRequest request1 = new AttachRequest(contactObj, System.currentTimeMillis() + "test1.txt", byteArrayInputStream, "plain/text");
        getClient().execute(request1);
        contactObj.setNumberOfAttachments(1);

        byteArrayInputStream = new ByteArrayInputStream("t2".getBytes());
        AttachRequest request2 = new AttachRequest(contactObj, System.currentTimeMillis() + "test1.txt", byteArrayInputStream, "plain/text");
        getClient().execute(request2);
        contactObj.setNumberOfAttachments(2);

        final Contact loadContact = loadContact(objectId, contactFolderId);
        compareObject(contactObj, loadContact);
    }
}
