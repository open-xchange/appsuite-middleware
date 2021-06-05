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

package com.openexchange.contact.storage.ldap.internal;

import org.slf4j.Logger;
import com.openexchange.contact.storage.ldap.id.LdapIDResolver;
import com.openexchange.contact.storage.ldap.mapping.LdapBooleanMapping;
import com.openexchange.contact.storage.ldap.mapping.LdapMapper;
import com.openexchange.contact.storage.ldap.mapping.LdapMapping;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.DistributionListEntryObject;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.Operand;
import com.openexchange.search.Operation;
import com.openexchange.search.SearchTerm;
import com.openexchange.search.SingleSearchTerm;
import com.openexchange.search.SingleSearchTerm.SingleOperation;
import com.openexchange.search.internal.operands.ConstantOperand;

/**
 * {@link SearchAdapter}
 *
 * Helps constructing LDAP filters for a search term.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class SearchTermAdapter {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(SearchTermAdapter.class);

    private final String filter;
    private final LdapMapper mapper;
    private final LdapIDResolver idResolver;

    /**
     * Initializes a new {@link SearchAdapter}.
     *
     * @param term
     * @throws OXException
     */
    public SearchTermAdapter(SearchTerm<?> term, LdapMapper mapper, LdapIDResolver idResolver) throws OXException {
        super();
        this.mapper = mapper;
        this.idResolver = idResolver;
        this.filter = getTerm(term);
    }

    public String getFilter() {
        return filter;
    }

    private String getTerm(SearchTerm<?> term) throws OXException {
        if (SingleSearchTerm.class.isInstance(term)) {
            return getTerm((SingleSearchTerm) term);
        } else if (CompositeSearchTerm.class.isInstance(term)) {
            return getTerm((CompositeSearchTerm) term);
        } else {
            throw new IllegalArgumentException("Need either an 'SingleSearchTerm' or 'CompositeSearchTerm'.");
        }
    }

    private String getTerm(SingleSearchTerm searchTerm) throws OXException {
        /*
         * get relevant mapping for term
         */
        SingleSearchTerm term = searchTerm;
        LdapMapping<? extends Object> ldapMapping = mapper.getMapping(term);
        if (null == ldapMapping || null == ldapMapping.getLdapAttributeName(true)) {
            LOG.debug("No LDAP attribute mapping for term '{}' available, excluding from search filter.", term);
            return null;
        }
        /*
         * get LDAP format of operands
         */
        Operand<?>[] operands = term.getOperands();
        Object[] formatArgs = new String[operands.length];
        Object[] alternativeFormatArgs = null != ldapMapping.getAlternativeLdapAttributeName(true) ? new String[operands.length] : null;

        // Workaround to negate boolean false filters for ldap
        if (SingleOperation.EQUALS.equals(term.getOperation()) && LdapBooleanMapping.class.isInstance(ldapMapping) && 2 == operands.length && Boolean.FALSE.equals(operands[1].getValue())) {
            operands[1] = new ConstantOperand<Boolean>(Boolean.TRUE);
            term = new SingleSearchTerm(SingleOperation.NOT_EQUALS);
            term.addOperand(operands[0]);
            term.addOperand(operands[1]);
        }

        boolean dList = false;
        for (int i = 0; i < operands.length; i++) {
            if (Operand.Type.COLUMN.equals(operands[i].getType())) {
                formatArgs[i] = ldapMapping.getLdapAttributeName(true);
                if (null != alternativeFormatArgs) {
                    alternativeFormatArgs[i] = ldapMapping.getAlternativeLdapAttributeName(true);
                }
                dList = ContactField.DISTRIBUTIONLIST.equals(operands[i].getValue());
            } else if (Operand.Type.CONSTANT.equals(operands[i].getType())) {
                Object value = (dList) ? compileDistributionListEntryObject(operands[i].getValue()) : operands[i].getValue();
                String encoded = ldapMapping.encodeForFilter(value, idResolver);
                formatArgs[i] = encoded;
                if (null != alternativeFormatArgs) {
                    alternativeFormatArgs[i] = encoded;
                }
            } else {
                throw new IllegalArgumentException("unknown type in operand: " + operands[i].getType());
            }
        }

        return buildFilter(term, ldapMapping, formatArgs, alternativeFormatArgs);
    }

    private DistributionListEntryObject[] compileDistributionListEntryObject(Object value) throws OXException {
        // Search in distribution lists is not supported. The yielding filter from the object of this method will return no results.
        DistributionListEntryObject dleo = new DistributionListEntryObject();
        dleo.setEmailaddress(value.toString());
        return new DistributionListEntryObject[] { dleo };
    }

    /**
     * Builds the LDAP filter
     *
     * @param term The SearchTerm
     * @param ldapMapping The LDAPMapping
     * @param formatArgs The format args
     * @param alternativeFormatArgs The alternative format args
     * @return The filter
     */
    private String buildFilter(SingleSearchTerm term, LdapMapping<? extends Object> ldapMapping, Object[] formatArgs, Object[] alternativeFormatArgs) {
        String filter = String.format(term.getOperation().getLdapRepresentation(), formatArgs);
        if (null != alternativeFormatArgs) {
            String alternativeFilter = String.format(term.getOperation().getLdapRepresentation(), alternativeFormatArgs);
            Object[] isNullFormatArgs = new String[] { ldapMapping.getLdapAttributeName(true) };
            String isNullFilter = String.format(SingleOperation.ISNULL.getLdapRepresentation(), isNullFormatArgs);
            filter = "|(" + filter + ")(&(" + isNullFilter + ")(" + alternativeFilter + "))";
        }
        return "(" + filter + ")";
    }

    private String getTerm(CompositeSearchTerm term) throws OXException {
        Operation operation = term.getOperation();
        SearchTerm<?>[] terms = term.getOperands();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < terms.length; i++) {
            String filter = getTerm(terms[i]);
            if (null != filter && 0 < filter.length()) {
                stringBuilder.append(filter);
            }
        }
        if (stringBuilder.length() <= 0) {
            return null;
        }
        return "(" + String.format(operation.getLdapRepresentation(), stringBuilder.toString()) + ")";
    }

}
