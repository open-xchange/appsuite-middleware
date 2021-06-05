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

import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import com.openexchange.ajax.find.actions.AutocompleteRequest;
import com.openexchange.ajax.find.actions.AutocompleteResponse;
import com.openexchange.ajax.user.actions.GetRequest;
import com.openexchange.find.Module;
import com.openexchange.find.facet.ComplexDisplayItem;
import com.openexchange.find.facet.FacetValue;
import com.openexchange.find.util.DisplayItems;
import com.openexchange.groupware.container.Contact;
import com.openexchange.java.util.TimeZones;

/**
 * {@link AutoCompleteTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class AutoCompleteTest extends ContactsFindTest {

    @Test
    public void testAutocompleteCurrentUser() throws Exception {
        String defaultAddress = getClient().getValues().getDefaultAddress();
        Contact ownContact = getClient().execute(new GetRequest(getClient().getValues().getUserId(), TimeZones.UTC)).getContact();
        ComplexDisplayItem displayItem = DisplayItems.convert(ownContact, getClient().getValues().getLocale(), i18nServiceRegistry);
        assertFoundFacetInAutocomplete(defaultAddress.substring(0, 3), displayItem.getDisplayName());
    }

    @Test
    public void testAutocompleteOtherContact() throws Exception {
        Contact contact = cotm.newAction(randomContact());
        ComplexDisplayItem displayItem = DisplayItems.convert(contact, getClient().getValues().getLocale(), i18nServiceRegistry);
        assertFoundFacetInAutocomplete(contact.getDisplayName().substring(0, 3), displayItem.getDisplayName());
        assertFoundFacetInAutocomplete(contact.getSurName().substring(0, 4), displayItem.getDisplayName());
        assertFoundFacetInAutocomplete(contact.getGivenName().substring(0, 5), displayItem.getDisplayName());
        assertFoundFacetInAutocomplete(contact.getEmail1().substring(0, 3), displayItem.getDisplayName());
    }

    private FacetValue assertFoundFacetInAutocomplete(String prefix, String expectedEmail1) throws Exception {
        AutocompleteRequest autocompleteRequest = new AutocompleteRequest(prefix, Module.CONTACTS.getIdentifier());
        AutocompleteResponse autocompleteResponse = getClient().execute(autocompleteRequest);
        FacetValue foundFacetValue = findByDisplayName(autocompleteResponse.getFacets(), expectedEmail1);
        assertNotNull("no facet value found for: " + expectedEmail1, foundFacetValue);
        return foundFacetValue;
    }

}
