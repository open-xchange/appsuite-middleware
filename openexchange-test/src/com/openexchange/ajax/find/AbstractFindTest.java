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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import com.openexchange.ajax.find.actions.AutocompleteRequest;
import com.openexchange.ajax.find.actions.AutocompleteResponse;
import com.openexchange.ajax.find.actions.QueryRequest;
import com.openexchange.ajax.find.actions.QueryResponse;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.find.Document;
import com.openexchange.find.Module;
import com.openexchange.find.SearchResult;
import com.openexchange.find.common.CommonFacetType;
import com.openexchange.find.common.FolderTypeDisplayItem;
import com.openexchange.find.facet.ActiveFacet;
import com.openexchange.find.facet.Facet;
import com.openexchange.find.facet.FacetType;
import com.openexchange.find.facet.FacetValue;
import com.openexchange.find.facet.Filter;
import com.openexchange.find.facet.FilterBuilder;
import com.openexchange.test.FolderTestManager;

/**
 * {@link AbstractFindTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractFindTest extends AbstractAJAXSession {

    protected FolderTestManager folderManager;

    protected Random random;

    /**
     * Default constructor.
     *
     * @param name name of the test.
     */
    protected AbstractFindTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        random = new Random();
        folderManager = new FolderTestManager(getClient());
    }

    @Override
    protected void tearDown() throws Exception {
        folderManager.cleanUp();
        super.tearDown();
    }

    /**
     * Performs a query request using the supplied active facets.
     *
     * @param module The module
     * @param facets The active facets
     * @return The found documents
     * @throws Exception
     */
    protected List<PropDocument> query(Module module, List<ActiveFacet> facets) throws Exception {
        QueryRequest queryRequest = new QueryRequest(0, Integer.MAX_VALUE, facets, module.getIdentifier());
        QueryResponse queryResponse = client.execute(queryRequest);
        SearchResult result = queryResponse.getSearchResult();
        List<PropDocument> propDocuments = new ArrayList<PropDocument>();
        List<Document> documents = result.getDocuments();
        for (Document document : documents) {
            propDocuments.add((PropDocument) document);
        }
        return propDocuments;
    }

    /**
     * Performs a query request using the supplied active facets.
     *
     * @param module The module
     * @param facets The active facets
     * @param start The start index for pagination
     * @param size The page size
     * @return The found documents
     * @throws Exception
     */
    protected List<PropDocument> query(Module module, List<ActiveFacet> facets, int start, int size) throws Exception {
        QueryRequest queryRequest = new QueryRequest(start, size, facets, module.getIdentifier());
        QueryResponse queryResponse = client.execute(queryRequest);
        SearchResult result = queryResponse.getSearchResult();
        List<PropDocument> propDocuments = new ArrayList<PropDocument>();
        List<Document> documents = result.getDocuments();
        for (Document document : documents) {
            propDocuments.add((PropDocument) document);
        }
        return propDocuments;
    }

    /**
     * Performs a query request using the supplied active facets.
     *
     * @param module The module
     * @param facets The active facets
     * @param columns The columns
     * @return The found documents
     * @throws Exception
     */
    protected List<PropDocument> query(Module module, List<ActiveFacet> facets, int[] columns) throws Exception {
        QueryRequest queryRequest = new QueryRequest(0, Integer.MAX_VALUE, facets, module.getIdentifier(), columns);
        QueryResponse queryResponse = client.execute(queryRequest);
        SearchResult result = queryResponse.getSearchResult();
        List<PropDocument> propDocuments = new ArrayList<PropDocument>();
        List<Document> documents = result.getDocuments();
        for (Document document : documents) {
            propDocuments.add((PropDocument) document);
        }
        return propDocuments;
    }

    /**
     * Performs an autocomplete request and returns the facets.
     *
     * @param module The module
     * @param prefix The prefix
     * @return The facets
     * @throws Exception
     */
    protected List<Facet> autocomplete(Module module, String prefix) throws Exception {
        AutocompleteRequest autocompleteRequest = new AutocompleteRequest(prefix, module.getIdentifier());
        AutocompleteResponse autocompleteResponse = client.execute(autocompleteRequest);
        return autocompleteResponse.getFacets();
    }

    /**
     * Performs an autocomplete request and returns the facets.
     *
     * @param module The module
     * @param prefix The prefix
     * @param facets The active facets
     * @return The facets
     * @throws Exception
     */
    protected List<Facet> autocomplete(Module module, String prefix, List<ActiveFacet> facets) throws Exception {
        AutocompleteRequest autocompleteRequest = new AutocompleteRequest(prefix, module.getIdentifier(), facets);
        AutocompleteResponse autocompleteResponse = client.execute(autocompleteRequest);
        return autocompleteResponse.getFacets();
    }

    /**
     * Gets a random substring of the supplied value, using a minimum length of 4.
     *
     * @param value The value to get the substring from
     * @return The substring
     */
    protected String randomSubstring(String value) {
        return randomSubstring(value, 4);
    }

    /**
     * Gets a random substring of the supplied value.
     *
     * @param value The value to get the substring from
     * @return The substring
     */
    protected String randomSubstring(String value, int minLength) {
        if (minLength >= value.length()) {
            fail(value + " is too short to get a substring from");
        }
        int start = random.nextInt(value.length() - minLength);
        int stop = start + minLength + random.nextInt(value.length() - start - minLength);
        return value.substring(start, stop);
    }

    /**
     * Creates a new, random UUID string.
     *
     * @return The UUID string
     */
    protected static String randomUID() {
        return UUID.randomUUID().toString();
    }

    /**
     * Searches the given facet type within the list of facets.
     *
     * @param type The facet type to search for
     * @param facets The facets to search in
     * @return The found facet or <code>null</code>
     */
    protected static Facet findByType(FacetType type, List<Facet> facets) {
        for (Facet facet : facets) {
            if (facet.getType() == type) {
                return facet;
            }
        }

        return null;
    }

    /**
     * Searches the supplied list of property documents matching the supplied value in one of its properties.
     *
     * @param documents The documents to check
     * @param property The property name to check
     * @param value The value to check
     * @return The found document, or <code>null</code> if not found
     */
    protected static PropDocument findByProperty(List<PropDocument> documents, String property, String value) {
        for (PropDocument propDocument : documents) {
            if (value.equals(propDocument.getProps().get(property))) {
                return propDocument;
            }
        }
        return null;
    }

    /**
     * Searches a FacetValue by its display name in a list of facets.
     *
     * @param facets The facets to check
     * @param displayName The display name to check
     */
    protected static FacetValue findByDisplayName(List<Facet> facets, String displayName) {
        for (Facet facet : facets) {
            List<FacetValue> values = facet.getValues();
            for (FacetValue value : values) {
                if (displayName.equals(value.getDisplayItem().getDefaultValue())) {
                    return value;
                }
            }
        }
        return null;
    }

    protected static ActiveFacet createFolderTypeFacet(FolderTypeDisplayItem.Type type) {
        return createActiveFacet(
            CommonFacetType.FOLDER_TYPE,
            type.getIdentifier(),
            new Filter(Collections.singletonList(CommonFacetType.FOLDER_TYPE.getId()), type.getIdentifier()));
    }

    protected static ActiveFacet createQuery(String query) {
        return createActiveFacet(CommonFacetType.GLOBAL, CommonFacetType.GLOBAL.getId(), CommonFacetType.GLOBAL.getId(), query);
    }

    protected static ActiveFacet createActiveFacet(FacetType type, int valueId, Filter filter) {
        return new ActiveFacet(type, Integer.toString(valueId), filter);
    }

    protected static ActiveFacet createActiveFacet(FacetType type, String valueId, Filter filter) {
        return new ActiveFacet(type, valueId, filter);
    }

    protected static ActiveFacet createActiveFacet(FacetType type, int valueId, String field, String query) {
        Filter filter = new FilterBuilder()
            .addField(field)
            .addQuery(query)
            .build();
        return new ActiveFacet(type, Integer.toString(valueId), filter);
    }

    protected static ActiveFacet createActiveFacet(FacetType type, String valueId, String field, String query) {
        Filter filter = new FilterBuilder()
            .addField(field)
            .addQuery(query)
            .build();
        return new ActiveFacet(type, valueId, filter);
    }

    protected static ActiveFacet createActiveFieldFacet(FacetType type, String field, String query) {
        Filter filter = new FilterBuilder()
            .addField(field)
            .addQuery(query)
            .build();
        return new ActiveFacet(type, type.getId(), filter);
    }
}
