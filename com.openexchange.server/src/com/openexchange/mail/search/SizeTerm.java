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
    public SizeTerm(final ComparisonType comparisonType, final int size) {
        super();
        pattern = new ComparablePattern<Integer>() {

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
    public void addMailField(final Collection<MailField> col) {
        col.add(MailField.SIZE);
    }

    @Override
    public boolean matches(final Message msg) throws OXException {
        final int size;
        try {
            size = msg.getSize();
        } catch (final MessagingException e) {
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
    public boolean matches(final MailMessage mailMessage) {
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
