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
import com.openexchange.find.Module;
import com.openexchange.find.facet.FacetValue;
import com.openexchange.find.util.DisplayItems;
import com.openexchange.groupware.container.Contact;

/**
 * {@link Bug33576Test}
 *
 * Autocomplete for contacts only shows contacts with e-mail address
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug33576Test extends ContactsFindTest {

    @Test
    public void testAutocompleteContactWithoutEMailAddress() throws Exception {
        Contact contact = randomContact();
        contact.removeEmail1();
        contact = cotm.newAction(contact);
        String prefix = contact.getGivenName().substring(0, 6);
        AutocompleteRequest autocompleteRequest = new AutocompleteRequest(prefix, Module.CONTACTS.getIdentifier());
        AutocompleteResponse autocompleteResponse = getClient().execute(autocompleteRequest);
        FacetValue foundFacetValue = findByDisplayName(autocompleteResponse.getFacets(), DisplayItems.convert(contact, getClient().getValues().getLocale(), i18nServiceRegistry).getDisplayName());
        assertNotNull("no facet value found for: " + contact.getGivenName(), foundFacetValue);
    }

}
