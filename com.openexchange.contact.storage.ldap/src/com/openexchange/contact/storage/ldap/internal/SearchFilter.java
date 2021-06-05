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
        List<Contact> filteredContacts = new ArrayList<>();
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

    private static boolean matches(Contact contact, SingleSearchTerm term, Locale locale) {
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
        }
        if (null == o1) {
            return null != o2 ? -1 : 1;
        }
        if (null == o2) {
            return 1;
        }
        if ((o1 instanceof String) && (o2 instanceof String)) {
            String value1 = (String)o1;
            String value2 = (String)o2;
            if (value1.equals(value2) || matchesWildcard(value1.toLowerCase(), value2.toLowerCase(), locale)) {
                return 0;
            } else if (null == locale) {
                return value1.compareTo(value2);
            } else {
                return Collators.getDefaultInstance(locale).compare(value1, value2);
            }
        }
        if (o1 instanceof Comparable) {
            @SuppressWarnings("unchecked") Comparable<Object> comparable = Comparable.class.cast(o1);
            return comparable.compareTo(o2);
        }
        throw new UnsupportedOperationException("Don't know how to compare two values of class " + o1.getClass().getName());
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
        int patternIdx = patternIndex;
        int valueIdx = valueIndex;
        int patternLength = wildcardPattern.length();
        int valueLength = value.length();
        boolean nextIsNotWildcard = false;
        while (true) {
            // check if end of string and/or pattern occurred
            if (valueIdx >= valueLength) {   // end of string still may have pending '*' in pattern
                while (patternIdx < patternLength && '*' == wildcardPattern.charAt(patternIdx)) {
                    patternIdx++;
                }
                return patternIdx >= patternLength;
            }
            if (patternIdx >= patternLength) {         // end of pattern, but not end of the string
                return false;
            }
            char p = wildcardPattern.charAt(patternIdx);    // pattern char

            // perform logic
            if (nextIsNotWildcard == false) {
                if (p == '\\') {
                    patternIdx++;
                    nextIsNotWildcard = true;
                    continue;
                }
                if (p == '?') {
                    valueIdx++;
                    patternIdx++;
                    continue;
                }
                if (p == '*') {
                    char pnext = 0;           // next pattern char
                    if (patternIdx + 1 < patternLength) {
                        pnext = wildcardPattern.charAt(patternIdx + 1);
                    }
                    if (pnext == '*') {         // double '*' have the same effect as one '*'
                        patternIdx++;
                        continue;
                    }
                    int i;
                    patternIdx++;

                    // find recursively if there is any substring from the end of the
                    // line that matches the rest of the pattern !!!
                    for (i = value.length(); i >= valueIdx; i--) {
                        if (matchesWildcard(value, wildcardPattern, i, patternIdx) == true) {
                            return true;
                        }
                    }
                    return false;
                }
            } else {
                nextIsNotWildcard = false;
            }
            // check if pattern char and string char are equals
            if (p != value.charAt(valueIdx)) {
                return false;
            }
            // everything matches for now, continue
            valueIdx++;
            patternIdx++;
        }
    }

}
