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
import java.util.Date;
import javax.mail.FetchProfile;
import javax.mail.Message;
import javax.mail.MessagingException;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailField;
import com.openexchange.mail.dataobjects.MailMessage;

/**
 * {@link ReceivedDateTerm}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ReceivedDateTerm extends SearchTerm<ComparablePattern<java.util.Date>> {

    private static final long serialVersionUID = -3566780904070234005L;

    private final ComparablePattern<java.util.Date> pattern;

    /**
     * Initializes a new {@link ReceivedDateTerm}
     */
    public ReceivedDateTerm(final ComparisonType comparisonType, final java.util.Date receivedDate) {
        super();
        pattern = new ComparablePattern<java.util.Date>() {

            @Override
            public ComparisonType getComparisonType() {
                return comparisonType;
            }

            @Override
            public Date getPattern() {
                return receivedDate;
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
    public void addMailField(final Collection<MailField> col) {
        col.add(MailField.RECEIVED_DATE);
    }

    @Override
    public boolean matches(final MailMessage mailMessage) {
        final Date receivedDate = mailMessage.getReceivedDate();
        if (null == receivedDate) {
            return false;
        }
        final ComparablePattern<java.util.Date> pattern = getPattern();
        final ComparisonType comparisonType = pattern.getComparisonType();
        switch (comparisonType) {
            case EQUALS:
                return toSeconds(pattern.getPattern()) == toSeconds(receivedDate);
            case LESS_THAN:
                return toSeconds(pattern.getPattern()) > toSeconds(receivedDate);
            case LESS_EQUALS:
                return toSeconds(pattern.getPattern()) >= toSeconds(receivedDate);
            case GREATER_THAN:
                return toSeconds(pattern.getPattern()) < toSeconds(receivedDate);
            case GREATER_EQUALS:
                return toSeconds(pattern.getPattern()) <= toSeconds(receivedDate);
            default:
                return toSeconds(pattern.getPattern()) == toSeconds(receivedDate);
        }
    }

    @Override
    public boolean matches(final Message msg) throws OXException {
        final Date receivedDate;
        try {
            receivedDate = msg.getReceivedDate();
        } catch (final MessagingException e) {
            org.slf4j.LoggerFactory.getLogger(ReceivedDateTerm.class).warn("Error during search.", e);
            return false;
        }
        if (null == receivedDate) {
            return false;
        }
        final ComparablePattern<java.util.Date> pattern = getPattern();
        final ComparisonType comparisonType = pattern.getComparisonType();
        switch (comparisonType) {
            case EQUALS:
                return toSeconds(pattern.getPattern()) == toSeconds(receivedDate);
            case LESS_THAN:
                return toSeconds(pattern.getPattern()) > toSeconds(receivedDate);
            case LESS_EQUALS:
                return toSeconds(pattern.getPattern()) >= toSeconds(receivedDate);
            case GREATER_THAN:
                return toSeconds(pattern.getPattern()) < toSeconds(receivedDate);
            case GREATER_EQUALS:
                return toSeconds(pattern.getPattern()) <= toSeconds(receivedDate);
            default:
                return toSeconds(pattern.getPattern()) == toSeconds(receivedDate);
        }
    }

    @Override
    public javax.mail.search.SearchTerm getJavaMailSearchTerm() {
        final ComparablePattern<java.util.Date> pattern = getPattern();
        return new javax.mail.search.ReceivedDateTerm(pattern.getComparisonType().getType(), pattern.getPattern());
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
