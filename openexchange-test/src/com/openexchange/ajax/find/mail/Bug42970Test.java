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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
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
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class Bug42970Test extends AbstractMailFindTest {

    /**
     * Initialises a new {@link Bug42970Test}.
     * 
     * @param name The test case's name
     */
    public Bug42970Test() {
        super();
    }

    /**
     * Asserts that the 'bcc' field is included in the contacts facet
     * 
     * @throws Exception
     */
    @Test
    public void testBccFieldIsIncludedInContacts() throws Exception {
        List<String> folders = new ArrayList<String>(4);
        folders.add(getClient().getValues().getInboxFolder());
        folders.add(getClient().getValues().getSentFolder());
        folders.add(getClient().getValues().getDraftsFolder());
        folders.add(getClient().getValues().getTrashFolder());

        for (String folder : folders) {
            assertBccFieldInFolder(folder);
        }
    }

    /**
     * Asserts if the 'bcc' field is included in the contacts facet when performing an 'autocomplete' query in the specified folder
     * 
     * @param folder The folder to perform the 'autocomplete' query
     * @throws Exception If an error is occurred
     */
    private void assertBccFieldInFolder(String folder) throws Exception {
        String prefix = defaultAddress.substring(0, 3);
        AutocompleteRequest autocompleteRequest = new AutocompleteRequest(prefix, Module.MAIL.getIdentifier(), prepareFacets(folder));
        AutocompleteResponse autocompleteResponse = getClient().execute(autocompleteRequest);
        List<Facet> facets = autocompleteResponse.getFacets();
        DefaultFacet contactFacet = (DefaultFacet) findByType(MailFacetType.CONTACTS, facets);
        assertNotNull(contactFacet);

        // Find the 'to' option
        Option option = findOption(contactFacet.getValues().get(0).getOptions(), "to");
        assertEquals("to", option.getId());
        assertFields(option.getFilter().getFields(), "bcc");
    }

    /**
     * Finds the {@link Option} in the specified list with the specified optionName
     * 
     * @param options The list with {@link Option}s
     * @param optionName The option's name
     * @return The {@link Option}
     * @throws IllegalArgumentException if the option with the specified optionName is not included in the specified list
     */
    private Option findOption(List<Option> options, String optionName) {
        for (Option option : options) {
            if (option.getId().equals(optionName)) {
                return option;
            }
        }
        throw new IllegalArgumentException("The option '" + optionName + "' was not found in the 'options' list");
    }

    /**
     * Asserts if the specified list of fields contains the field with the specified name
     * 
     * @param fields The list of fields
     * @param fieldName The field name
     */
    private void assertFields(List<String> fields, String fieldName) {
        assertNotNull("The fields list is null", fields);
        assertFalse("The fields list is empty", fields.isEmpty());
        boolean foundBcc = false;
        for (String field : fields) {
            if (field.equals(fieldName)) {
                foundBcc = true;
                break;
            }
        }
        assertTrue("The 'bcc' field is not included in the contacts facet", foundBcc);
    }

}
