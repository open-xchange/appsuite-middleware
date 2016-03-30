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

package com.openexchange.ajax.find.mail;

import java.util.ArrayList;
import java.util.List;
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
    public Bug42970Test(String name) {
        super(name);
    }

    /**
     * Asserts that the 'bcc' field is included in the contacts facet
     * 
     * @throws Exception
     */
    public void testBccFieldIsIncludedInContacts() throws Exception {
        List<String> folders = new ArrayList<String>(4);
        folders.add(client.getValues().getInboxFolder());
        folders.add(client.getValues().getSentFolder());
        folders.add(client.getValues().getDraftsFolder());
        folders.add(client.getValues().getTrashFolder());

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
        AutocompleteResponse autocompleteResponse = client.execute(autocompleteRequest);
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
