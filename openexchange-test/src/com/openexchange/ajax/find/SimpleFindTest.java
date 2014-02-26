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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.ajax.find;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import com.openexchange.ajax.find.actions.AutocompleteRequest;
import com.openexchange.ajax.find.actions.AutocompleteResponse;
import com.openexchange.ajax.find.actions.QueryRequest;
import com.openexchange.ajax.find.actions.QueryResponse;
import com.openexchange.find.facet.Facet;
import com.openexchange.find.facet.FacetValue;
import com.openexchange.find.facet.Filter;

/**
 * {@link SimpleFindTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class SimpleFindTest extends AbstractFindTest {

    /**
     * Initializes a new {@link SimpleFindTest}.
     */
    public SimpleFindTest(final String name) {
        super(name);
    }

    @BeforeClass
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @AfterClass
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testQuerySimple() {
        try {

            List<String> queries = Collections.singletonList("afe");
            List<Filter> filters = Collections.emptyList();
            final QueryResponse queryResponse = getClient().execute(new QueryRequest(0, 10, queries, filters, "drive"));

            assertNotNull(queryResponse);

            System.err.println(queryResponse.getData());

        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    public void testAutocompleteSimple() {
        try {
            final AutocompleteResponse autocompleteResponse = getClient().execute(new AutocompleteRequest("ste", "drive"));

            assertNotNull(autocompleteResponse);

            final List<Facet> facets = autocompleteResponse.getFacets();
            assertNotNull("Missing response", facets);

            final int size = facets.size();
            assertTrue("No autocomplete result, but expected", size > 0);

            boolean foundContacFacet = false;
            boolean foundSteffen = false;
            boolean foundFileNameFacet = false;
            boolean foundFileContentFacet = false;
            for (int i = 0; i < size; i++) {
                final Facet facet = facets.get(i);

                foundFileNameFacet |= "file_name".equals(facet.getType().getId());
                foundFileContentFacet |= "file_content".equals(facet.getType().getId());

                final boolean isContactFacet = "contacts".equals(facet.getType().getId());

                foundContacFacet |= isContactFacet;

                if (isContactFacet) {
                    List<FacetValue> values = facet.getValues();
                    for (FacetValue facetValue : values) {
                        Map<String, Object> item = (Map<String, Object>) facetValue.getDisplayItem().getItem();
                        final String sEmail1 = (String) item.get("email1");
                        foundSteffen |= ((null != sEmail1) && (sEmail1.indexOf("steffen") >= 0));
                    }
                }
            }

            assertTrue("Missing \"file_name\" in static facets.", foundFileNameFacet);

            assertTrue("Missing \"file_content\" in static facets.", foundFileContentFacet);

            assertTrue("Contacts facet missing in auto-complete response", foundContacFacet);

            assertTrue("Expected facet value missing in auto-complete response", foundSteffen);

        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

}
