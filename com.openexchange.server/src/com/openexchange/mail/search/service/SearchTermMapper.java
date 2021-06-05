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

package com.openexchange.mail.search.service;

import java.text.MessageFormat;
import java.util.Arrays;
import com.openexchange.mail.search.ANDTerm;
import com.openexchange.mail.search.BooleanTerm;
import com.openexchange.mail.search.FileNameTerm;
import com.openexchange.mail.search.HeaderTerm;
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
    public static SearchTerm<?> map(com.openexchange.search.SearchTerm<?> searchTerm) {
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
            case HEADER:
                SingleOperation singleOperation = getSingleOperation(operation);
                if (SingleOperation.EQUALS != singleOperation) {
                    throw new IllegalArgumentException("Unsupported operation for header search: " + operation);
                }
                return new HeaderTerm(values[0].toString(), values[1].toString());
            case ATTACHMENT:
                return ("name".equals(values[0].toString().toLowerCase())) ? new FileNameTerm((String) values[1]) : BooleanTerm.TRUE;
            default:
                return BooleanTerm.TRUE;

        }


    }

    private static SingleOperation getSingleOperation(Operation operation) {
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
