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

package com.openexchange.contact.storage.ldap.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import com.openexchange.contact.storage.ldap.mapping.LdapMapper;
import com.openexchange.contact.storage.ldap.mapping.LdapMapping;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.java.Collators;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.CompositeSearchTerm.CompositeOperation;
import com.openexchange.search.Operand;
import com.openexchange.search.Operand.Type;
import com.openexchange.search.SearchTerm;
import com.openexchange.search.SingleSearchTerm;
import com.openexchange.search.SingleSearchTerm.SingleOperation;

/**
 * {@link SearchFilter}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class SearchFilter {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(SearchFilter.class);

    private final Locale locale;
    private final SearchTerm<?> term;

    /**
     * Initializes a new {@link SearchFilter}.
     *
     * @param term the search term
     * @param locale the locale for string comparisons, or <code>null</code>
     *        if not relevant
     */
    public SearchFilter(SearchTerm<?> term, Locale locale) {
        super();
        this.term = term;
        this.locale = locale;
    }

    /**
     * Creates a new collection and adds all contacts from the supplied
     * collection fulfilling the search criteria.
     *
     * @param contacts
     * @return
     * @throws OXException
     */
    public Collection<Contact> filter(Collection<Contact> contacts) throws OXException {
        List<Contact> filteredContacts = new ArrayList<Contact>();
        if (null != contacts && null != this.term) {
            for (Contact contact : contacts) {
                if (matches(contact, this.term, this.locale)) {
                    filteredContacts.add(contact);
                }
            }
        }
        return filteredContacts;
    }

    private static boolean matches(Contact contact, SearchTerm<?> term, Locale locale) throws OXException {
        if (SingleSearchTerm.class.isInstance(term)) {
            return matches(contact, (SingleSearchTerm)term, locale);
        } else if (CompositeSearchTerm.class.isInstance(term)) {
            return matches(contact, (CompositeSearchTerm)term, locale);
        } else {
            throw new IllegalArgumentException("Need either a 'SingleSearchTerm' or 'CompositeSearchTerm'.");
        }
    }

    private static boolean matches(Contact contact, SingleSearchTerm term, Locale locale) throws OXException {
        /*
         * get relevant mapping for term
         */
        LdapMapping<? extends Object> mapping = LdapMapper.GENERIC.getMapping(term);
        if (null == mapping) {
            LOG.debug("No mapping for term '{}' available, excluding from search filter.", term);
            return true;
        }
        /*
         * get values to match
         */
        Object operandValue = null;
        for (Operand<?> operand : term.getOperands()) {
            if (Type.CONSTANT.equals(operand.getType())) {
                operandValue = operand.getValue();
                break;
            }
        }
        Object contactValue = mapping.get(contact);
        if (null != contactValue && String.class.isInstance(operandValue) && false == String.class.isInstance(contactValue)) {
            // normalize to strings for comparison (numerical IDs from contact)))
            contactValue = contactValue.toString();
        }
        /*
         * compare values
         */
        switch ((SingleOperation)term.getOperation()) {
        case EQUALS:
            return 0 == compare(contactValue, operandValue, locale);
        case GREATER_OR_EQUAL:
            return 0 <= compare(contactValue, operandValue, locale);
        case GREATER_THAN:
            return 0 < compare(contactValue, operandValue, locale);
        case ISNULL:
            return null == contactValue;
        case LESS_OR_EQUAL:
            return 0 >= compare(contactValue, operandValue, locale);
        case LESS_THAN:
            return 0 > compare(contactValue, operandValue, locale);
        case NOT_EQUALS:
            return 0 != compare(contactValue, operandValue, locale);
        default:
            throw new IllegalArgumentException("Unknown operation: " + term.getOperation());
        }
    }

    private static boolean matches(Contact contact, CompositeSearchTerm term, Locale locale) throws OXException {
        SearchTerm<?>[] terms = term.getOperands();
        switch ((CompositeOperation)term.getOperation()) {
        case AND:
            for (SearchTerm<?> searchTerm : terms) {
                if (false == matches(contact, searchTerm, locale)) {
                    return false;
                }
            }
            return true;
        case NOT:
            return false == matches(contact, terms[0], locale);
        case OR:
            for (SearchTerm<?> searchTerm : terms) {
                if (matches(contact, searchTerm, locale)) {
                    return true;
                }
            }
            return false;
        default:
            throw new IllegalArgumentException("Unknown operation: " + term.getOperation());
        }
    }

    private static int compare(Object o1, Object o2, Locale locale) {
        if (o1 == o2) {
            return 0;
        } else if (null == o1 && null != o2) {
            return -1;
        } else if (null == o2) {
            return 1;
        } else if (String.class.isInstance(o1) && String.class.isInstance(o2)) {
            String value1 = (String)o1;
            String value2 = (String)o2;
            if (value1.equals(value2) || matchesWildcard(value1.toLowerCase(), value2.toLowerCase(), locale)) {
                return 0;
            } else if (null == locale) {
                return value1.compareTo(value2);
            } else {
                return Collators.getDefaultInstance(locale).compare(value1, value2);
            }
        } else if (Comparable.class.isInstance(o1)) {
            return ((Comparable)o1).compareTo(o2);
        } else {
            throw new UnsupportedOperationException("Don't know how to compare two values of class " + o1.getClass().getName());
        }
    }

    private static boolean matchesWildcard(String value, String wildcardPattern, Locale locale) {
        return "*".equals(wildcardPattern) || matchesWildcard(
            value.toLowerCase(null != locale ? locale : Locale.ENGLISH),
            wildcardPattern.toLowerCase(null != locale ? locale : Locale.ENGLISH), 0, 0);
    }

    private static boolean matchesWildcard(String value, String wildcardPattern, int valueIndex, int patternIndex) {
        /*
         * based on http://www.java2s.com/Open-Source/Java/Development/jodd/jodd/util/Wildcard.java.htm
         */
        int patternLength = wildcardPattern.length();
        int valueLength = value.length();
        boolean nextIsNotWildcard = false;
        while (true) {
            // check if end of string and/or pattern occurred
            if (valueIndex >= valueLength) {   // end of string still may have pending '*' in pattern
                while (patternIndex < patternLength && '*' == wildcardPattern.charAt(patternIndex)) {
                    patternIndex++;
                }
                return patternIndex >= patternLength;
            }
            if (patternIndex >= patternLength) {         // end of pattern, but not end of the string
                return false;
            }
            char p = wildcardPattern.charAt(patternIndex);    // pattern char

            // perform logic
            if (nextIsNotWildcard == false) {
                if (p == '\\') {
                    patternIndex++;
                    nextIsNotWildcard = true;
                    continue;
                }
                if (p == '?') {
                    valueIndex++;
                    patternIndex++;
                    continue;
                }
                if (p == '*') {
                    char pnext = 0;           // next pattern char
                    if (patternIndex + 1 < patternLength) {
                        pnext = wildcardPattern.charAt(patternIndex + 1);
                    }
                    if (pnext == '*') {         // double '*' have the same effect as one '*'
                        patternIndex++;
                        continue;
                    }
                    int i;
                    patternIndex++;

                    // find recursively if there is any substring from the end of the
                    // line that matches the rest of the pattern !!!
                    for (i = value.length(); i >= valueIndex; i--) {
                        if (matchesWildcard(value, wildcardPattern, i, patternIndex) == true) {
                            return true;
                        }
                    }
                    return false;
                }
            } else {
                nextIsNotWildcard = false;
            }
            // check if pattern char and string char are equals
            if (p != value.charAt(valueIndex)) {
                return false;
            }
            // everything matches for now, continue
            valueIndex++;
            patternIndex++;
        }
    }

}
