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

package com.openexchange.ajax.find.contacts;

import java.util.List;
import org.junit.Before;
import com.openexchange.ajax.find.AbstractFindTest;
import com.openexchange.ajax.find.PropDocument;
import com.openexchange.ajax.framework.UserValues;
import com.openexchange.find.Module;
import com.openexchange.find.facet.ActiveFacet;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.tools.arrays.Arrays;

/**
 * {@link ContactsFindTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class ContactsFindTest extends AbstractFindTest {

    static final int[] PHONE_COLUMNS = new int[] { Contact.TELEPHONE_ASSISTANT, Contact.TELEPHONE_BUSINESS1, Contact.TELEPHONE_BUSINESS2, Contact.TELEPHONE_CALLBACK, Contact.TELEPHONE_CAR, Contact.TELEPHONE_COMPANY, Contact.TELEPHONE_HOME1, Contact.TELEPHONE_HOME2, Contact.TELEPHONE_IP, Contact.TELEPHONE_ISDN, Contact.TELEPHONE_OTHER, Contact.TELEPHONE_PAGER, Contact.TELEPHONE_PRIMARY, Contact.TELEPHONE_RADIO, Contact.TELEPHONE_TELEX, Contact.TELEPHONE_TTYTDD
    };

    static final int[] NAME_COLUMNS = new int[] { Contact.DISPLAY_NAME, Contact.SUR_NAME, Contact.MIDDLE_NAME, Contact.GIVEN_NAME, Contact.TITLE, Contact.YOMI_FIRST_NAME, Contact.YOMI_LAST_NAME, Contact.SUFFIX
    };

    static final int[] EMAIL_COLUMNS = new int[] { Contact.EMAIL1, Contact.EMAIL2, Contact.EMAIL3,
    };

    static final int[] ADDRESS_COLUMNS = new int[] { Contact.STREET_BUSINESS, Contact.STREET_HOME, Contact.STREET_OTHER, Contact.POSTAL_CODE_BUSINESS, Contact.POSTAL_CODE_HOME, Contact.POSTAL_CODE_OTHER, Contact.CITY_BUSINESS, Contact.CITY_HOME, Contact.CITY_OTHER, Contact.STATE_BUSINESS, Contact.STATE_HOME, Contact.STATE_OTHER, Contact.COUNTRY_BUSINESS, Contact.COUNTRY_HOME, Contact.COUNTRY_OTHER,
    };

    static final int[] ADDRESSBOOK_COLUMNS = Arrays.addUniquely(NAME_COLUMNS, Arrays.addUniquely(ADDRESS_COLUMNS, Arrays.addUniquely(PHONE_COLUMNS, EMAIL_COLUMNS)));

    protected int folderID;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        UserValues values = getClient().getValues();
        FolderObject folder = ftm.generatePublicFolder("ManagedContactTest_" + randomUID(), com.openexchange.groupware.modules.Module.CONTACTS.getFolderConstant(), values.getPrivateContactFolder(), values.getUserId());
        folder = ftm.insertFolderOnServer(folder);
        folderID = folder.getObjectID();
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
