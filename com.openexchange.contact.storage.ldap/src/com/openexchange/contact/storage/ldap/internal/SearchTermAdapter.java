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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.contact.storage.ldap.internal;

import com.openexchange.contact.storage.ldap.id.LdapIDResolver;
import com.openexchange.contact.storage.ldap.mapping.LdapMapper;
import com.openexchange.contact.storage.ldap.mapping.LdapMapping;
import com.openexchange.exception.OXException;
import com.openexchange.log.LogFactory;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.Operand;
import com.openexchange.search.Operation;
import com.openexchange.search.SearchTerm;
import com.openexchange.search.SingleSearchTerm;
import com.openexchange.search.SingleSearchTerm.SingleOperation;

/**
 * {@link SearchAdapter}
 * 
 * Helps constructing LDAP filters for a search term.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class SearchTermAdapter {
	
    private static final org.apache.commons.logging.Log LOG = LogFactory.getLog(SearchTermAdapter.class);

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
            return getTerm((SingleSearchTerm)term);
        } else if (CompositeSearchTerm.class.isInstance(term)) {
            return getTerm((CompositeSearchTerm)term);
        } else {
            throw new IllegalArgumentException("Need either an 'SingleSearchTerm' or 'CompositeSearchTerm'.");
        }
    }    
    
    private String getTerm(SingleSearchTerm term) throws OXException {
        /*
         * get relevant mapping for term 
         */
        LdapMapping<? extends Object> ldapMapping = mapper.getMapping(term);
        if (null == ldapMapping || null == ldapMapping.getLdapAttributeName(true)) {
            LOG.debug("No LDAP attribute mapping for term '" + term.toString() + "' available, excluding from search filter.");
            return null;
        }
        /*
         * get LDAP format of operands
         */
        Operand<?>[] operands = term.getOperands();
        Object[] formatArgs = new String[operands.length];
        Object[] alternativeFormatArgs = null != ldapMapping.getAlternativeLdapAttributeName(true) ? new String[operands.length] : null;
        for (int i = 0; i < operands.length; i++) {
            if (Operand.Type.COLUMN.equals(operands[i].getType())) {
                formatArgs[i] = ldapMapping.getLdapAttributeName(true);
                if (null != alternativeFormatArgs) {
                    alternativeFormatArgs[i] = ldapMapping.getAlternativeLdapAttributeName(true);
                }
            } else if (Operand.Type.CONSTANT.equals(operands[i].getType())) {
                String encoded = ldapMapping.encodeForFilter(operands[i].getValue(), idResolver); 
                formatArgs[i] = encoded;
                if (null != alternativeFormatArgs) {
                    alternativeFormatArgs[i] = encoded;
                }
            } else {
                throw new IllegalArgumentException("unknown type in operand: " + operands[i].getType());
            }
        }
        /*
         * build filter 
         */
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
        if (0 < stringBuilder.length()) {
            return "(" + String.format(operation.getLdapRepresentation(), stringBuilder.toString()) + ")"; 
        } else {
            return null;
        }
	}

}
