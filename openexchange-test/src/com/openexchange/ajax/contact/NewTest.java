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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.ajax.contact;

import java.io.ByteArrayInputStream;
import com.openexchange.ajax.ContactTest;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.groupware.attach.impl.AttachmentImpl;
import com.openexchange.groupware.container.Contact;

public class NewTest extends ContactTest {

    public NewTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testNew() throws Exception {
        final Contact contactObj = createContactObject("testNew");
        insertContact(getWebConversation(), contactObj, PROTOCOL + getHostName(), getSessionId());
    }

    public void testNewWithDistributionList() throws Exception {
        final Contact contactEntry = createContactObject("internal contact");
        contactEntry.setEmail1("internalcontact@x.de");
        final int contactId = insertContact(getWebConversation(), contactEntry, PROTOCOL + getHostName(), getSessionId());
        contactEntry.setObjectID(contactId);

        createContactWithDistributionList("testNewWithDistributionList", contactEntry);
    }

    public void testNewWithLinks() throws Exception {
        final Contact link1 = createContactObject("link1");
        final Contact link2 = createContactObject("link2");
        final int linkId1 = insertContact(getWebConversation(), link1, PROTOCOL + getHostName(), getSessionId());
        link1.setObjectID(linkId1);
        final int linkId2 = insertContact(getWebConversation(), link2, PROTOCOL + getHostName(), getSessionId());
        link2.setObjectID(linkId2);

        createContactWithLinks("testNewWithLinks", link1, link2);
    }

    public void testNewContactWithAttachment() throws Exception {
        final Contact contactObj = createContactObject("testNewContactWithAttachment");
        final int objectId = insertContact(getWebConversation(), contactObj, getHostName(), getSessionId());
        contactObj.setObjectID(objectId);

        final AttachmentMetadata attachmentObj = new AttachmentImpl();
        attachmentObj.setFilename(System.currentTimeMillis() + "test1.txt");
        attachmentObj.setModuleId(Types.CONTACT);
        attachmentObj.setAttachedId(objectId);
        attachmentObj.setFolderId(contactFolderId);
        attachmentObj.setRtfFlag(false);
        attachmentObj.setFileMIMEType("plain/text");

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream("t1".getBytes());
        com.openexchange.webdav.xml.AttachmentTest.insertAttachment(getWebConversation(), attachmentObj, byteArrayInputStream, getHostName(), getLogin(), getPassword(), "");
        contactObj.setNumberOfAttachments(1);

        byteArrayInputStream = new ByteArrayInputStream("t2".getBytes());
        com.openexchange.webdav.xml.AttachmentTest.insertAttachment(getWebConversation(), attachmentObj, byteArrayInputStream, getHostName(), getLogin(), getPassword(), "");
        contactObj.setNumberOfAttachments(2);

        final Contact loadContact = ContactTest.loadContact(getWebConversation(), objectId, contactFolderId, PROTOCOL, getHostName(), getSessionId());
        compareObject(contactObj, loadContact);
    }
}
