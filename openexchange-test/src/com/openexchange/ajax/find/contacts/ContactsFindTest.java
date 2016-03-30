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

package com.openexchange.ajax.find.contacts;

import java.util.Date;
import java.util.List;
import com.openexchange.ajax.find.AbstractFindTest;
import com.openexchange.ajax.find.PropDocument;
import com.openexchange.ajax.framework.UserValues;
import com.openexchange.find.Module;
import com.openexchange.find.facet.ActiveFacet;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.test.ContactTestManager;
import com.openexchange.tools.arrays.Arrays;


/**
 * {@link ContactsFindTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class ContactsFindTest extends AbstractFindTest {

    static final int[] PHONE_COLUMNS = new int[] {
        Contact.TELEPHONE_ASSISTANT,
        Contact.TELEPHONE_BUSINESS1,
        Contact.TELEPHONE_BUSINESS2,
        Contact.TELEPHONE_CALLBACK,
        Contact.TELEPHONE_CAR,
        Contact.TELEPHONE_COMPANY,
        Contact.TELEPHONE_HOME1,
        Contact.TELEPHONE_HOME2,
        Contact.TELEPHONE_IP,
        Contact.TELEPHONE_ISDN,
        Contact.TELEPHONE_OTHER,
        Contact.TELEPHONE_PAGER,
        Contact.TELEPHONE_PRIMARY,
        Contact.TELEPHONE_RADIO,
        Contact.TELEPHONE_TELEX,
        Contact.TELEPHONE_TTYTDD
    };

    static final int[] NAME_COLUMNS = new int[] {
        Contact.DISPLAY_NAME,
        Contact.SUR_NAME,
        Contact.MIDDLE_NAME,
        Contact.GIVEN_NAME,
        Contact.TITLE,
        Contact.YOMI_FIRST_NAME,
        Contact.YOMI_LAST_NAME,
        Contact.SUFFIX
    };

    static final int[] EMAIL_COLUMNS = new int[] {
        Contact.EMAIL1,
        Contact.EMAIL2,
        Contact.EMAIL3,
    };

    static final int[] ADDRESS_COLUMNS = new int[] {
        Contact.STREET_BUSINESS,
        Contact.STREET_HOME,
        Contact.STREET_OTHER,
        Contact.POSTAL_CODE_BUSINESS,
        Contact.POSTAL_CODE_HOME,
        Contact.POSTAL_CODE_OTHER,
        Contact.CITY_BUSINESS,
        Contact.CITY_HOME,
        Contact.CITY_OTHER,
        Contact.STATE_BUSINESS,
        Contact.STATE_HOME,
        Contact.STATE_OTHER,
        Contact.COUNTRY_BUSINESS,
        Contact.COUNTRY_HOME,
        Contact.COUNTRY_OTHER,
    };

    static final int[] ADDRESSBOOK_COLUMNS =
        Arrays.addUniquely(NAME_COLUMNS, Arrays.addUniquely(ADDRESS_COLUMNS, Arrays.addUniquely(PHONE_COLUMNS, EMAIL_COLUMNS)));

    protected ContactTestManager manager;

    protected int folderID;

    /**
     * Initializes a new {@link ContactsFindTest}.
     *
     * @param name The test name
     */
    public ContactsFindTest(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        manager = new ContactTestManager(getClient());
        UserValues values = getClient().getValues();
        FolderObject folder = folderManager.generatePublicFolder(
                "ManagedContactTest_"+(new Date().getTime()),
                com.openexchange.groupware.modules.Module.CONTACTS.getFolderConstant(),
                values.getPrivateContactFolder(),
                values.getUserId());
        folder = folderManager.insertFolderOnServer(folder);
        folderID = folder.getObjectID();
    }

    @Override
    protected void tearDown() throws Exception {
        manager.cleanUp();
        super.tearDown();
    }

    /**
     * Performs a query request using the supplied active facets.
     *
     * @param facets The active facets
     * @return The found documents
     * @throws Exception
     */
    protected List<PropDocument> query(List<ActiveFacet> facets) throws Exception {
        return query(Module.CONTACTS, facets);
    }

    /**
     * Creates a new, random contact instance containing some basic random data, with the folder ID being set to <code>folderID</code>.
     * The contact is not created at the server automatically.
     *
     * @return The contact
     */
    protected Contact randomContact() {
        Contact contact = new Contact();
        contact.setParentFolderID(folderID);
        contact.setSurName(randomUID());
        contact.setGivenName(randomUID());
        contact.setDisplayName(contact.getGivenName() + " " + contact.getSurName());
        contact.setEmail1(randomUID() + "@example.com");
        contact.setUid(randomUID());
        return contact;
    }

    protected Contact randomContact(int parentFolder) {
        Contact contact = new Contact();
        contact.setParentFolderID(parentFolder);
        contact.setSurName(randomUID());
        contact.setGivenName(randomUID());
        contact.setDisplayName(contact.getGivenName() + " " + contact.getSurName());
        contact.setEmail1(randomUID() + "@example.com");
        contact.setUid(randomUID());
        return contact;
    }

}
