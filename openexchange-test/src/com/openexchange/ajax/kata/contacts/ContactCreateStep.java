/*    OPEN-XCHANGE legal information
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


package com.openexchange.ajax.kata.contacts;

import java.util.Date;
import com.openexchange.ajax.contact.action.InsertRequest;
import com.openexchange.ajax.contact.action.InsertResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.kata.AbstractStep;
import com.openexchange.ajax.kata.IdentitySource;
import com.openexchange.groupware.container.Contact;
import com.openexchange.test.ContactTestManager;

/**
 *
 * {@link ContactCreateStep}
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 *
 */
public class ContactCreateStep extends AbstractStep implements IdentitySource<Contact>{

    private final Contact entry;
    private boolean inserted;
    private ContactTestManager manager;

    public ContactCreateStep(Contact entry, String name, String expectedError) {
        super(name, expectedError);
        this.entry = entry;
    }

    @Override
    public void cleanUp() throws Exception {
        if( inserted ){
            entry.setLastModified(new Date(Long.MAX_VALUE));
            manager.setFailOnError(false);
            manager.deleteAction(entry);
        }
    }

    @Override
    public void perform(AJAXClient client) throws Exception {
        this.client = client;
        this.manager = new ContactTestManager(client);

        InsertRequest insertRequest = new InsertRequest(entry, false);
        InsertResponse insertResponse = execute(insertRequest);
        insertResponse.fillObject(entry);
        inserted = !insertResponse.hasError();
        checkError(insertResponse);
    }

    @Override
    public void assumeIdentity(Contact contact) {
        contact.setObjectID( entry.getObjectID() );
        contact.setParentFolderID( entry.getParentFolderID());
        contact.setLastModified( entry.getLastModified());
    }

    @Override
    public void rememberIdentityValues(Contact contact) {
        contact.setLastModified( entry.getLastModified());
        contact.setParentFolderID(entry.getParentFolderID());
    }

    @Override
    public void forgetIdentity(Contact entry) {
        inserted = false;
    }

    @Override
    public Class<Contact> getType() {
        return Contact.class;
    }

}
