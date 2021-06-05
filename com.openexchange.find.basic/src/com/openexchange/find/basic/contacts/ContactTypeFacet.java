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

package com.openexchange.find.basic.contacts;

import java.util.ArrayList;
import java.util.List;
import com.openexchange.contact.ContactFieldOperand;
import com.openexchange.exception.OXException;
import com.openexchange.find.FindExceptionCode;
import com.openexchange.find.contacts.ContactsFacetType;
import com.openexchange.find.contacts.ContactsStrings;
import com.openexchange.find.facet.ExclusiveFacet;
import com.openexchange.find.facet.FacetValue;
import com.openexchange.find.facet.Filter;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.CompositeSearchTerm.CompositeOperation;
import com.openexchange.search.SearchTerm;
import com.openexchange.search.SingleSearchTerm;
import com.openexchange.search.SingleSearchTerm.SingleOperation;
import com.openexchange.search.internal.operands.ConstantOperand;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ContactTypeFacet}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ContactTypeFacet extends ExclusiveFacet {

    public static enum Type {

        /**
         * The type denoting 'normal' contacts.
         */
        CONTACT("contact"),
        /**
         * The type denoting distribution lists.
         */
        DISTRIBUTION_LIST("distribution list"), ;

        private final String identifier;

        private Type(final String identifier) {
            this.identifier = identifier;
        }

        /**
         * Gets the identifier
         *
         * @return The identifier
         */
        public String getIdentifier() {
            return identifier;
        }
    }

    private static final long serialVersionUID = -9031103652463933032L;

    private static final ContactTypeFacet INSTANCE = new ContactTypeFacet();

    /**
     * Gets the contact type facet instance.
     *
     * @return The instance
     */
    public static ContactTypeFacet getInstance() {
        return INSTANCE;
    }

    /**
     * Initializes a new {@link ContactTypeFacet}.
     */
    private ContactTypeFacet() {
        super(ContactsFacetType.CONTACT_TYPE, getFacetValues());
    }

    private static List<FacetValue> getFacetValues() {
        String id = ContactsFacetType.CONTACT_TYPE.getId();
        List<FacetValue> facetValues = new ArrayList<FacetValue>(2);
        facetValues.add(FacetValue.newBuilder(Type.CONTACT.getIdentifier())
            .withLocalizableDisplayItem(ContactsStrings.CONTACT_TYPE_CONTACT)
            .withFilter(Filter.of(id, Type.CONTACT.getIdentifier()))
            .build());

        facetValues.add(FacetValue.newBuilder(Type.DISTRIBUTION_LIST.getIdentifier())
            .withLocalizableDisplayItem(ContactsStrings.CONTACT_TYPE_DISTRIBUTION_LIST)
            .withFilter(Filter.of(id, Type.DISTRIBUTION_LIST.getIdentifier()))
            .build());

        return facetValues;
    }

    /**
     * Creates a new {@link SearchTerm} with the specified queries
     *
     * @param session The session
     * @param queries The queries
     * @return The new {@link SearchTerm}
     * @throws OXException if an error is occurred
     */
    public SearchTerm<?> getSearchTerm(ServerSession session, List<String> queries) throws OXException {
        if (!queries.isEmpty()) {
            if (Type.CONTACT.getIdentifier().equals(queries.get(0))) {
                CompositeSearchTerm searchTerm = new CompositeSearchTerm(CompositeOperation.OR);
                SingleSearchTerm term1 = new SingleSearchTerm(SingleOperation.ISNULL);
                term1.addOperand(new ContactFieldOperand(ContactField.MARK_AS_DISTRIBUTIONLIST));
                searchTerm.addSearchTerm(term1);
                SingleSearchTerm term2 = new SingleSearchTerm(SingleOperation.EQUALS);
                term2.addOperand(new ContactFieldOperand(ContactField.MARK_AS_DISTRIBUTIONLIST));
                term2.addOperand(new ConstantOperand<Boolean>(Boolean.FALSE));
                searchTerm.addSearchTerm(term2);
                return searchTerm;
            }
            if (Type.DISTRIBUTION_LIST.getIdentifier().equals(queries.get(0))) {
                SingleSearchTerm searchTerm = new SingleSearchTerm(SingleOperation.EQUALS);
                searchTerm.addOperand(new ContactFieldOperand(ContactField.MARK_AS_DISTRIBUTIONLIST));
                searchTerm.addOperand(new ConstantOperand<Boolean>(Boolean.TRUE));
                return searchTerm;
            }
        }
        throw FindExceptionCode.UNSUPPORTED_FILTER_QUERY.create(queries, getType().getId());
    }

}
