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
import java.util.Date;
import javax.mail.FetchProfile;
import javax.mail.Message;
import javax.mail.MessagingException;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailField;
import com.openexchange.mail.dataobjects.MailMessage;

/**
 * {@link SentDateTerm}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SentDateTerm extends SearchTerm<ComparablePattern<java.util.Date>> {

    private static final long serialVersionUID = -1602773060126517090L;

    private final ComparablePattern<java.util.Date> pattern;

    /**
     * Initializes a new {@link SentDateTerm}
     */
    public SentDateTerm(ComparisonType comparisonType, java.util.Date sentDate) {
        super();
        pattern = new ComparablePattern<java.util.Date>() {

            private static final long serialVersionUID = -8289583335080081522L;

            @Override
            public ComparisonType getComparisonType() {
                return comparisonType;
            }

            @Override
            public Date getPattern() {
                return sentDate;
            }
        };
    }

    @Override
    public void accept(SearchTermVisitor visitor) {
        visitor.visit(this);
    }

    /**
     * @return The sent date to match
     */
    @Override
    public ComparablePattern<java.util.Date> getPattern() {
        return pattern;
    }

    @Override
    public void addMailField(Collection<MailField> col) {
        col.add(MailField.SENT_DATE);
    }

    @Override
    public boolean matches(MailMessage mailMessage) {
        final Date sentDate = mailMessage.getSentDate();
        if (null == sentDate) {
            return false;
        }
        final ComparablePattern<java.util.Date> pattern = getPattern();
        final ComparisonType comparisonType = pattern.getComparisonType();
        switch (comparisonType) {
            case EQUALS:
                return toSeconds(pattern.getPattern()) == toSeconds(sentDate);
            case LESS_THAN:
                return toSeconds(pattern.getPattern()) > toSeconds(sentDate);
            case LESS_EQUALS:
                return toSeconds(pattern.getPattern()) >= toSeconds(sentDate);
            case GREATER_THAN:
                return toSeconds(pattern.getPattern()) < toSeconds(sentDate);
            case GREATER_EQUALS:
                return toSeconds(pattern.getPattern()) <= toSeconds(sentDate);
            default:
                return toSeconds(pattern.getPattern()) == toSeconds(sentDate);
        }
    }

    @Override
    public boolean matches(Message msg) throws OXException {
        final Date sentDate;
        try {
            sentDate = msg.getSentDate();
        } catch (MessagingException e) {
            org.slf4j.LoggerFactory.getLogger(SentDateTerm.class).warn("Error during search.", e);
            return false;
        }
        if (null == sentDate) {
            return false;
        }
        final ComparablePattern<java.util.Date> pattern = getPattern();
        final ComparisonType comparisonType = pattern.getComparisonType();
        switch (comparisonType) {
            case EQUALS:
                return toSeconds(pattern.getPattern()) == toSeconds(sentDate);
            case LESS_THAN:
                return toSeconds(pattern.getPattern()) > toSeconds(sentDate);
            case LESS_EQUALS:
                return toSeconds(pattern.getPattern()) >= toSeconds(sentDate);
            case GREATER_THAN:
                return toSeconds(pattern.getPattern()) < toSeconds(sentDate);
            case GREATER_EQUALS:
                return toSeconds(pattern.getPattern()) <= toSeconds(sentDate);
            default:
                return toSeconds(pattern.getPattern()) == toSeconds(sentDate);
        }
    }

    @Override
    public javax.mail.search.SearchTerm getJavaMailSearchTerm() {
        final ComparablePattern<java.util.Date> pattern = getPattern();
        return new javax.mail.search.SentDateTerm(pattern.getComparisonType().getType(), pattern.getPattern());
    }

    @Override
    public javax.mail.search.SearchTerm getNonWildcardJavaMailSearchTerm() {
        return getJavaMailSearchTerm();
    }

    @Override
    public void contributeTo(FetchProfile fetchProfile) {
        if (!fetchProfile.contains(FetchProfile.Item.ENVELOPE)) {
            fetchProfile.add(FetchProfile.Item.ENVELOPE);
        }
    }

    private static final long toSeconds(Date date) {
        return date.getTime() / 1000;
    }

}
