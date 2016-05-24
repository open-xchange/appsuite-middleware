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

package com.openexchange.mail.search.service;

import java.text.MessageFormat;
import java.util.Arrays;
import com.openexchange.mail.search.ANDTerm;
import com.openexchange.mail.search.AttachmentTerm;
import com.openexchange.mail.search.BooleanTerm;
import com.openexchange.mail.search.NOTTerm;
import com.openexchange.mail.search.ORTerm;
import com.openexchange.mail.search.SearchTerm;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.CompositeSearchTerm.CompositeOperation;
import com.openexchange.search.Operand;
import com.openexchange.search.Operation;
import com.openexchange.search.SingleSearchTerm.SingleOperation;

/**
 * {@link SearchTermMapper} - Maps a given instance of {@link com.openexchange.search.SearchTerm} to an appropriate instance of
 * {@link com.openexchange.mail.search.SearchTerm}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SearchTermMapper {

    /**
     * Initializes a new {@link SearchTermMapper}.
     */
    private SearchTermMapper() {
        super();
    }

    /**
     * Generates an appropriate mail search term from specified search term.
     *
     * @param searchTerm The search term
     * @return An appropriate mail search term
     * @throws IllegalArgumentException If an appropriate mail search term cannot be generated
     */
    public static SearchTerm<?> map(final com.openexchange.search.SearchTerm<?> searchTerm) {
        final Operation operation = searchTerm.getOperation();
        if (CompositeOperation.AND.equals(operation)) {
            final com.openexchange.search.SearchTerm<?>[] searchTerms = ((CompositeSearchTerm) searchTerm).getOperands();
            final int length = searchTerms.length;
            if (length == 0) {
                return BooleanTerm.TRUE;
            }
            ANDTerm andTerm;
            if (1 == length) {
                andTerm = new ANDTerm(map(searchTerms[0]), BooleanTerm.TRUE); // Neutral element
            } else {
                andTerm = new ANDTerm(map(searchTerms[0]), map(searchTerms[1]));
                for (int i = 2; i < length; i++) {
                    andTerm = new ANDTerm(andTerm, map(searchTerms[i]));
                }
            }
            return andTerm;
        }
        if (CompositeOperation.OR.equals(operation)) {
            final com.openexchange.search.SearchTerm<?>[] searchTerms = ((CompositeSearchTerm) searchTerm).getOperands();
            final int length = searchTerms.length;
            if (length == 0) {
                return BooleanTerm.TRUE;
            }
            ORTerm orTerm;
            if (1 == length) {
                orTerm = new ORTerm(map(searchTerms[0]), BooleanTerm.FALSE); // Neutral element
            } else {
                orTerm = new ORTerm(map(searchTerms[0]), map(searchTerms[1]));
                for (int i = 2; i < length; i++) {
                    orTerm = new ORTerm(orTerm, map(searchTerms[i]));
                }
            }
            return orTerm;
        }
        if (CompositeOperation.NOT.equals(operation)) {
            final com.openexchange.search.SearchTerm<?>[] searchTerms = ((CompositeSearchTerm) searchTerm).getOperands();
            final int length = searchTerms.length;
            if (length == 0) {
                return BooleanTerm.TRUE;
            }
            return new NOTTerm(map(searchTerms[0]));
        }

        Operand<String>[] operands = (Operand<String>[]) searchTerm.getOperands();
        final Object[] values = getNameAndConstant(operands);
        if (null == values) {
            throw new IllegalArgumentException(MessageFormat.format("Invalid values for single search term: {0}", Arrays.toString(searchTerm.getOperands())));
        }
        final SearchTerm<?> term;
        switch (operands[0].getType()) {
            case COLUMN:
                term = MailAttributeFetcher.getInstance().getSearchTerm(values[0].toString(), getSingleOperation(operation), values[1]);
                return null == term ? BooleanTerm.TRUE : term;
            case ATTACHMENT:
                if (values[0].equals("NAME")) {
                    return new AttachmentTerm((String) values[1]);
                }
            default:
                return BooleanTerm.TRUE;

        }


    }

    private static SingleOperation getSingleOperation(final Operation operation) {
        if (SingleOperation.EQUALS.equals(operation)) {
            return SingleOperation.EQUALS;
        }
        if (SingleOperation.GREATER_THAN.equals(operation)) {
            return SingleOperation.GREATER_THAN;
        }
        if (SingleOperation.LESS_THAN.equals(operation)) {
            return SingleOperation.LESS_THAN;
        }
        throw new IllegalArgumentException(MessageFormat.format("Unknown single search term operation: {0}", operation));
    }

    private static Object[] getNameAndConstant(@SuppressWarnings("unchecked") final Operand[] operands) {
        if (Operand.Type.CONSTANT.equals((operands[0]).getType())) {
            return new Object[] { operands[1].getValue().toString(), operands[0].getValue() };
        } else if (Operand.Type.CONSTANT.equals((operands[1]).getType())) {
            return new Object[] { operands[0].getValue().toString(), operands[1].getValue() };
        }
        return null;
    }

}
