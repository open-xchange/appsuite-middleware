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
