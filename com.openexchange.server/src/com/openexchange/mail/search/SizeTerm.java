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

package com.openexchange.mail.search;

import java.util.Collection;
import javax.mail.FetchProfile;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.search.AndTerm;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailField;
import com.openexchange.mail.dataobjects.MailMessage;

/**
 * {@link SizeTerm}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SizeTerm extends SearchTerm<ComparablePattern<Integer>> {

    private static final long serialVersionUID = 6011159685554702125L;

    private final ComparablePattern<Integer> pattern;

    /**
     * Initializes a new {@link SizeTerm}
     */
    public SizeTerm(ComparisonType comparisonType, int size) {
        super();
        pattern = new ComparablePattern<Integer>() {

            private static final long serialVersionUID = -1654705730708028618L;

            private final Integer i = Integer.valueOf(size);

            @Override
            public ComparisonType getComparisonType() {
                return comparisonType;
            }

            @Override
            public Integer getPattern() {
                return i;
            }
        };
    }

    @Override
    public void accept(SearchTermVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public ComparablePattern<Integer> getPattern() {
        return pattern;
    }

    @Override
    public void addMailField(Collection<MailField> col) {
        col.add(MailField.SIZE);
    }

    @Override
    public boolean matches(Message msg) throws OXException {
        final int size;
        try {
            size = msg.getSize();
        } catch (MessagingException e) {
            org.slf4j.LoggerFactory.getLogger(SizeTerm.class).warn("Error during search.", e);
            return false;
        }
        final ComparablePattern<Integer> pattern = getPattern();
        final ComparisonType comparisonType = pattern.getComparisonType();
        if (ComparisonType.EQUALS == comparisonType) {
            return size == pattern.getPattern().intValue();
        } else if (ComparisonType.LESS_THAN == comparisonType) {
            return size < pattern.getPattern().intValue();
        } else if (ComparisonType.GREATER_THAN == comparisonType) {
            return size > pattern.getPattern().intValue();
        } else {
            return size == pattern.getPattern().intValue();
        }
    }

    @Override
    public boolean matches(MailMessage mailMessage) {
        final long size = mailMessage.getSize();
        final ComparablePattern<Integer> pattern = getPattern();
        final ComparisonType comparisonType = pattern.getComparisonType();
        if (ComparisonType.EQUALS == comparisonType) {
            return size == pattern.getPattern().intValue();
        } else if (ComparisonType.LESS_THAN == comparisonType) {
            return size < pattern.getPattern().intValue();
        } else if (ComparisonType.GREATER_THAN == comparisonType) {
            return size > pattern.getPattern().intValue();
        } else {
            return size == pattern.getPattern().intValue();
        }
    }

    @Override
    public javax.mail.search.SearchTerm getJavaMailSearchTerm() {
        /*
         * IMAP only allows LT and GT
         */
        final ComparablePattern<Integer> pattern = getPattern();
        switch (pattern.getComparisonType()) {
            case EQUALS:
                return new AndTerm(
                    new javax.mail.search.SizeTerm(ComparisonType.GREATER_THAN.getType(), pattern.getPattern().intValue() - 1),
                    new javax.mail.search.SizeTerm(ComparisonType.LESS_THAN.getType(), pattern.getPattern().intValue() + 1));
            case GREATER_EQUALS:
                return new javax.mail.search.SizeTerm(ComparisonType.GREATER_THAN.getType(), pattern.getPattern().intValue() - 1);
            case LESS_EQUALS:
                return new javax.mail.search.SizeTerm(ComparisonType.LESS_THAN.getType(), pattern.getPattern().intValue() + 1);
            default:
                return new javax.mail.search.SizeTerm(pattern.getComparisonType().getType(), pattern.getPattern().intValue());
        }
    }

    @Override
    public javax.mail.search.SearchTerm getNonWildcardJavaMailSearchTerm() {
        return getJavaMailSearchTerm();
    }

    @Override
    public void contributeTo(FetchProfile fetchProfile) {
        if (!fetchProfile.contains(FetchProfile.Item.SIZE)) {
            fetchProfile.add(FetchProfile.Item.SIZE);
        }
    }

}
