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

package com.openexchange.ajax.contact;

import java.util.TimeZone;
import com.openexchange.ajax.config.actions.Tree;
import com.openexchange.ajax.contact.action.UpdateRequest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.user.actions.GetRequest;
import com.openexchange.ajax.user.actions.GetResponse;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;

/**
 * Checks if the context administrator is allowed to change the primary email of every user contact.
 * TODO This tests needs the context administrator account. The context administrators account is not configured with it login information
 * and therefore this test is not added to any test suite.
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class ChangePrimaryMailTest extends AbstractAJAXSession {

    private AJAXClient client;
    private TimeZone timeZone;
    private Contact userContact;
    private int contactId;

    public ChangePrimaryMailTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client = getClient();
        timeZone = getClient().getValues().getTimeZone();
        GetResponse response = client.execute(new GetRequest(client.getValues().getUserId(), timeZone));
        userContact = response.getContact();
        contactId = client.execute(new com.openexchange.ajax.config.actions.GetRequest(Tree.ContactID)).getInteger();
    }

    public void testChangeEMail1() throws Throwable {
        Contact testContact = new Contact();
        testContact.setObjectID(contactId);
        testContact.setParentFolderID(FolderObject.SYSTEM_LDAP_FOLDER_ID);
        testContact.setLastModified(userContact.getLastModified());
        testContact.setEmail1("fummel@fummel.de");
        UpdateRequest updateRequest = new UpdateRequest(testContact);
        client.execute(updateRequest);
    }
}
