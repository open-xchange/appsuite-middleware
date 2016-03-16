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

package com.openexchange.search.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.CompositeSearchTerm.CompositeOperation;
import com.openexchange.search.Operand;
import com.openexchange.search.Operation;
import com.openexchange.search.SearchAttributeFetcher;
import com.openexchange.search.SearchService;
import com.openexchange.search.SearchTerm;
import com.openexchange.search.SingleSearchTerm;
import com.openexchange.search.SingleSearchTerm.SingleOperation;

/**
 * {@link SearchServiceImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SearchServiceImpl implements SearchService {

    /*-
     * ################################## STATIC HELPERS ####################################
     */

    private static abstract class SingleTermMatcher {

        public SingleTermMatcher() {
            super();
        }

        public abstract <C> boolean matches(Operand<?>[] operands, C candidate, SearchAttributeFetcher<C> attributeFetcher);

        public static <C> Object getValue(final Operand<?> operand, final C candidate, final SearchAttributeFetcher<C> attributeFetcher) {
            if (Operand.Type.CONSTANT.equals(operand.getType())) {
                return operand.getValue();
            }
            return attributeFetcher.getAttribute(operand.getValue().toString(), candidate);
        }
    }

    private static final Map<SingleOperation, SingleTermMatcher> MATCHERS;

    static {
        final Map<SingleOperation, SingleTermMatcher> m = new EnumMap<SingleOperation, SingleTermMatcher>(SingleOperation.class);

        m.put(SingleOperation.EQUALS, new SingleTermMatcher() {

            @Override
            public <C> boolean matches(final Operand<?>[] operands, final C candidate, final SearchAttributeFetcher<C> attributeFetcher) {
                final Object value1 = getValue(operands[0], candidate, attributeFetcher);
                final Object value2 = getValue(operands[1], candidate, attributeFetcher);
                if (null == value1) {
                    if (null == value2) {
                        return true;
                    }
                    return false;
                }
                return value1.equals(value2);
            }
        });

        m.put(SingleOperation.GREATER_THAN, new SingleTermMatcher() {

            @Override
            public <C> boolean matches(final Operand<?>[] operands, final C candidate, final SearchAttributeFetcher<C> attributeFetcher) {
                final Object value1 = getValue(operands[0], candidate, attributeFetcher);
                final Object value2 = getValue(operands[1], candidate, attributeFetcher);
                // TODO: Check for number, etc.
                if (null == value1 || null == value2) {
                    return false;
                }
                if (value1 instanceof Comparable && value2 instanceof Comparable) {
                    return ((Comparable) value1).compareTo(value2) > 0;
                }
                return false;
            }
        });

        m.put(SingleOperation.LESS_THAN, new SingleTermMatcher() {

            @Override
            public <C> boolean matches(final Operand<?>[] operands, final C candidate, final SearchAttributeFetcher<C> attributeFetcher) {
                final Object value1 = getValue(operands[0], candidate, attributeFetcher);
                final Object value2 = getValue(operands[1], candidate, attributeFetcher);
                // TODO: Check for number, etc.
                if (null == value1 || null == value2) {
                    return false;
                }
                if (value1 instanceof Comparable && value2 instanceof Comparable) {
                    return ((Comparable) value1).compareTo(value2) < 0;
                }
                return false;
            }
        });

        MATCHERS = Collections.unmodifiableMap(m);
    }

    private static <C> boolean singleTermMatch(final C candidate, final SingleSearchTerm searchTerm, final SearchAttributeFetcher<C> attributeFetcher) {
        final SingleTermMatcher stm = MATCHERS.get(searchTerm.getOperation());
        if (null == stm) {
            return false;
        }
        return stm.matches(searchTerm.getOperands(), candidate, attributeFetcher);
    }

    /*-
     * ################################## MEMBERS ####################################
     */

    /**
     * Initializes a new {@link SearchServiceImpl}.
     */
    public SearchServiceImpl() {
        super();
    }

    @Override
    public <C> boolean matches(final C candidate, final SearchTerm<?> searchTerm, final SearchAttributeFetcher<C> attributeFetcher) {
        final Operation operation = searchTerm.getOperation();
        if (CompositeOperation.AND.equals(operation)) {
            final SearchTerm<?>[] searchTerms = ((CompositeSearchTerm) searchTerm).getOperands();
            if (searchTerms.length == 0) {
                return true;
            }
            boolean matches = matches(candidate, searchTerms[0], attributeFetcher);
            for (int i = 1; i < searchTerms.length && matches; i++) {
                matches &= matches(candidate, searchTerms[1], attributeFetcher);
            }
            return matches;
        }
        if (CompositeOperation.OR.equals(operation)) {
            final SearchTerm<?>[] searchTerms = ((CompositeSearchTerm) searchTerm).getOperands();
            if (searchTerms.length == 0) {
                return true;
            }
            boolean matches = matches(candidate, searchTerms[0], attributeFetcher);
            for (int i = 1; i < searchTerms.length && !matches; i++) {
                matches |= matches(candidate, searchTerms[1], attributeFetcher);
            }
            return matches;
        }
        if (CompositeOperation.NOT.equals(operation)) {
            final SearchTerm<?>[] searchTerms = ((CompositeSearchTerm) searchTerm).getOperands();
            if (searchTerms.length == 0) {
                return true;
            }
            return !matches(candidate, searchTerms[0], attributeFetcher);
        }
        return singleTermMatch(candidate, (SingleSearchTerm) searchTerm, attributeFetcher);
    }

    @Override
    public <C> Collection<C> filter(final Collection<C> candidates, final SearchTerm<?> searchTerm, final SearchAttributeFetcher<C> attributeFetcher) {
        final List<C> retval = new ArrayList<C>(candidates.size());
        for (final C candidate : candidates) {
            if (matches(candidate, searchTerm, attributeFetcher)) {
                retval.add(candidate);
            }
        }
        return retval;
    }

}
