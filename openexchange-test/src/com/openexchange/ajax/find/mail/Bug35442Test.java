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

package com.openexchange.ajax.find.mail;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.util.List;
import org.junit.Test;
import com.openexchange.ajax.find.actions.AutocompleteRequest;
import com.openexchange.ajax.find.actions.AutocompleteResponse;
import com.openexchange.find.Module;
import com.openexchange.find.facet.DefaultFacet;
import com.openexchange.find.facet.Facet;
import com.openexchange.find.facet.Option;
import com.openexchange.find.mail.MailFacetType;

/**
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class Bug35442Test extends AbstractMailFindTest {

    @Test
    public void testDefaultContactOptionIsSwitchedToToInSentFolder() throws Exception {
        String prefix = defaultAddress.substring(0, 3);
        AutocompleteRequest autocompleteRequest = new AutocompleteRequest(prefix, Module.MAIL.getIdentifier(), prepareFacets());
        AutocompleteResponse autocompleteResponse = getClient().execute(autocompleteRequest);
        List<Facet> facets = autocompleteResponse.getFacets();
        DefaultFacet contactFacet = (DefaultFacet) findByType(MailFacetType.CONTACTS, facets);
        assertNotNull(contactFacet);

        /*
         * autocomplete in inbox - we expect the first option to be set to 'from'
         */
        Option option = contactFacet.getValues().get(0).getOptions().get(0);
        assertEquals("from", option.getId());

        autocompleteRequest = new AutocompleteRequest(prefix, Module.MAIL.getIdentifier(), prepareFacets(getClient().getValues().getSentFolder()));
        autocompleteResponse = getClient().execute(autocompleteRequest);
        facets = autocompleteResponse.getFacets();
        contactFacet = (DefaultFacet) findByType(MailFacetType.CONTACTS, facets);
        assertNotNull(contactFacet);

        /*
         * autocomplete in sent - we expect the first option to be set to 'to'
         */
        option = contactFacet.getValues().get(0).getOptions().get(0);
        assertEquals("to", option.getId());
    }

}
