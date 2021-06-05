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

package com.openexchange.ajax.find.calendar;

import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.find.actions.AutocompleteRequest;
import com.openexchange.ajax.find.actions.AutocompleteResponse;
import com.openexchange.ajax.user.actions.GetRequest;
import com.openexchange.ajax.user.actions.GetResponse;
import com.openexchange.find.Module;
import com.openexchange.find.facet.FacetValue;
import com.openexchange.find.util.DisplayItems;
import com.openexchange.groupware.container.Contact;
import com.openexchange.test.ContactTestManager;

/**
 * {@link AutocompleteTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class AutocompleteTest extends CalendarFindTest {

    private ContactTestManager contactManager;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        contactManager = new ContactTestManager(getClient());
    }

    /**
     * Initializes a new {@link AutocompleteTest}.
     */
    public AutocompleteTest() {
        super();
    }

    @Test
    public void testAutocompleteCurrentUser() throws Exception {
        String defaultAddress = getClient().getValues().getDefaultAddress();
        GetRequest getRequest = new GetRequest(getClient().getValues().getUserId(), getClient().getValues().getTimeZone());
        GetResponse getResponse = getClient().execute(getRequest);
        Contact contact = getResponse.getContact();
        String displayName = DisplayItems.convert(contact, getClient().getValues().getLocale(), i18nServiceRegistry).getDisplayName();
        assertFoundFacetInAutocomplete(defaultAddress.substring(0, 3), displayName);
    }

    @Test
    public void testAutocompleteOtherContact() throws Exception {
        Contact contact = new Contact();
        contact.setParentFolderID(getClient().getValues().getPrivateContactFolder());
        contact.setSurName(randomUID());
        contact.setGivenName(randomUID());
        contact.setDisplayName(contact.getGivenName() + " " + contact.getSurName());
        contact.setEmail1(randomUID() + "@example.com");
        contact.setEmail2(randomUID() + "@example.com");
        contact.setEmail3(randomUID() + "@example.com");
        contact.setUid(randomUID());
        contact = contactManager.newAction(contact);
        String displayName = DisplayItems.convert(contact, getClient().getValues().getLocale(), i18nServiceRegistry).getDisplayName();
        assertFoundFacetInAutocomplete(contact.getDisplayName().substring(0, 3), displayName);
        assertFoundFacetInAutocomplete(contact.getSurName().substring(0, 4), displayName);
        assertFoundFacetInAutocomplete(contact.getGivenName().substring(0, 5), displayName);
        assertFoundFacetInAutocomplete(contact.getEmail1().substring(0, 3), displayName);
        assertFoundFacetInAutocomplete(contact.getEmail2().substring(0, 6), displayName);
        assertFoundFacetInAutocomplete(contact.getEmail3().substring(0, 5), displayName);
    }

    private FacetValue assertFoundFacetInAutocomplete(String prefix, String displayName) throws Exception {
        AutocompleteRequest autocompleteRequest = new AutocompleteRequest(prefix, Module.CALENDAR.getIdentifier());
        AutocompleteResponse autocompleteResponse = getClient().execute(autocompleteRequest);
        FacetValue foundFacetValue = findByDisplayName(autocompleteResponse.getFacets(), displayName);
        assertNotNull("no facet value found for: " + displayName, foundFacetValue);
        return foundFacetValue;
    }

}
