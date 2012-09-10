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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.zmal.search;

import java.text.SimpleDateFormat;
import java.util.Date;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.search.ANDTerm;
import com.openexchange.mail.search.BccTerm;
import com.openexchange.mail.search.BodyTerm;
import com.openexchange.mail.search.BooleanTerm;
import com.openexchange.mail.search.CcTerm;
import com.openexchange.mail.search.ComparablePattern;
import com.openexchange.mail.search.FlagTerm;
import com.openexchange.mail.search.FromTerm;
import com.openexchange.mail.search.HeaderTerm;
import com.openexchange.mail.search.NOTTerm;
import com.openexchange.mail.search.ORTerm;
import com.openexchange.mail.search.ReceivedDateTerm;
import com.openexchange.mail.search.SearchTerm;
import com.openexchange.mail.search.SearchTermVisitor;
import com.openexchange.mail.search.SentDateTerm;
import com.openexchange.mail.search.SizeTerm;
import com.openexchange.mail.search.SubjectTerm;
import com.openexchange.mail.search.ToTerm;


/**
 * {@link ZmalSearchTermVisitor}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ZmalSearchTermVisitor implements SearchTermVisitor {

    private static final SimpleDateFormat SDF;
    static {
        SDF = new SimpleDateFormat("MM/dd/yyyy"); // date:2/1/2007 would find messages dated February 1, 2007
    }

    private final StringBuilder sb;

    /**
     * Initializes a new {@link ZmalSearchTermVisitor}.
     */
    public ZmalSearchTermVisitor() {
        super();
        sb = new StringBuilder(256);
    }

    /**
     * Resets this visitor's string builder.
     * 
     * @return This visitor
     */
    public ZmalSearchTermVisitor reset() {
        sb.setLength(0);
        return this;
    }

    /**
     * Gets the composed query.
     * 
     * @return The query
     */
    public String getQuery() {
        final String query = sb.toString();
        return query.substring(1);
    }

    @Override
    public void visit(ANDTerm term) {
        final SearchTerm<?>[] terms = term.getPattern();
        if (null == terms || 0 >= terms.length) {
            return;
        }
        final ZmalSearchTermVisitor visitor = new ZmalSearchTermVisitor();
        terms[0].accept(visitor);
        sb.append(visitor.getQuery());
        for (int i = 1; i < terms.length; i++) {
            terms[i].accept(visitor.reset());
            sb.append(" AND").append(visitor.getQuery());
        }
    }

    @Override
    public void visit(BccTerm term) {
        sb.append(" bcc:").append(term.getPattern());
    }

    @Override
    public void visit(BodyTerm term) {
        sb.append(" content:").append(term.getPattern());
    }

    @Override
    public void visit(BooleanTerm term) {
        // Not supported
    }

    @Override
    public void visit(CcTerm term) {
        sb.append(" cc:").append(term.getPattern());
    }

    @Override
    public void visit(FlagTerm term) {
        /*
         * Searches for messages with a certain status - for example, is: unread will find all unread messages. Allowable values are
         * "unread", "read", "flagged", "unflagged", "sent", "draft", "received", "replied", "unreplied", "forwarded",
         * unforwarded", "anywhere
         * ", "remote" (in a shared folder), "local", "sent", "invite", "solo" (no other messages in conversation), "tome
         * ", "fromme", "ccme", "tofromme". "fromccme", "tofromccme" (to, from cc me, including my aliases)
         */
        final int flags = term.getPattern().intValue();
        if ((flags & MailMessage.FLAG_ANSWERED) > 0) {
            sb.append(" is:replied");
        }
        if ((flags & MailMessage.FLAG_DRAFT) > 0) {
            sb.append(" is:draft");
        }
        if ((flags & MailMessage.FLAG_FLAGGED) > 0) {
            sb.append(" is:flagged");
        }
        if ((flags & MailMessage.FLAG_FORWARDED) > 0) {
            sb.append(" is:forwarded");
        }
        if ((flags & MailMessage.FLAG_SEEN) > 0) {
            sb.append(" is:read");
        }
        if ((flags & MailMessage.FLAG_SPAM) > 0) {
            sb.append(" in:spam");
        }
    }

    @Override
    public void visit(FromTerm term) {
        sb.append(" from:").append(term.getPattern());
    }

    @Override
    public void visit(HeaderTerm term) {
        String[] pattern = term.getPattern();
        final String name = pattern[0];
        if ("message-id".equalsIgnoreCase(name)) {
            sb.append( "msgid:").append(pattern[1]);
        } else if ("from".equalsIgnoreCase(name)) {
            sb.append(" from:").append(pattern[1]);
        } else if ("to".equalsIgnoreCase(name)) {
            sb.append(" to:").append(pattern[1]);
        } else if ("cc".equalsIgnoreCase(name)) {
            sb.append(" cc:").append(pattern[1]);
        } else if ("bcc".equalsIgnoreCase(name)) {
            sb.append(" bcc:").append(pattern[1]);
        } else if ("subject".equalsIgnoreCase(name)) {
            sb.append(" subject:").append(pattern[1]);
        }
    }

    @Override
    public void visit(NOTTerm term) {
        sb.append(" not");
    }

    @Override
    public void visit(ORTerm term) {
        final SearchTerm<?>[] terms = term.getPattern();
        if (null == terms || 0 >= terms.length) {
            return;
        }
        final ZmalSearchTermVisitor visitor = new ZmalSearchTermVisitor();
        terms[0].accept(visitor);
        sb.append(visitor.getQuery());
        for (int i = 1; i < terms.length; i++) {
            terms[i].accept(visitor.reset());
            sb.append(" OR").append(visitor.getQuery());
        }
    }

    @Override
    public void visit(final ReceivedDateTerm term) {
        final ComparablePattern<Date> comparablePattern = term.getPattern();
        final long time = comparablePattern.getPattern().getTime();
        switch (comparablePattern.getComparisonType()) {
            case EQUALS:
                {
                    synchronized (SDF) {
                        sb.append(" date:").append(SDF.format(new Date(time)));
                    }
                }
                break;
            case GREATER_THAN:
                {
                    synchronized (SDF) {
                        sb.append(" after:").append(SDF.format(new Date(time)));
                    }
                }
                break;
            case LESS_THAN:
                {
                    synchronized (SDF) {
                        sb.append(" before:").append(SDF.format(new Date(time)));
                    }
                }
                break;
            default:
                throw new IllegalStateException("Unknown operator: " + comparablePattern.getComparisonType());
        }
    }

    @Override
    public void visit(SentDateTerm term) {
        final ComparablePattern<Date> comparablePattern = term.getPattern();
        final long time = comparablePattern.getPattern().getTime();
        switch (comparablePattern.getComparisonType()) {
            case EQUALS:
                {
                    synchronized (SDF) {
                        sb.append(" date:").append(SDF.format(new Date(time)));
                    }
                }
                break;
            case GREATER_THAN:
                {
                    synchronized (SDF) {
                        sb.append(" after:").append(SDF.format(new Date(time)));
                    }
                }
                break;
            case LESS_THAN:
                {
                    synchronized (SDF) {
                        sb.append(" before:").append(SDF.format(new Date(time)));
                    }
                }
                break;
            default:
                throw new IllegalStateException("Unknown operator: " + comparablePattern.getComparisonType());
        }
    }

    @Override
    public void visit(SizeTerm term) {
        final ComparablePattern<Integer> comparablePattern = term.getPattern();
        final long size = comparablePattern.getPattern().longValue();
        switch (comparablePattern.getComparisonType()) {
            case EQUALS:
                {
                    sb.append(" size:").append(size);
                }
                break;
            case GREATER_THAN:
                {
                    sb.append(" larger:").append(size);
                }
                break;
            case LESS_THAN:
                {
                    sb.append(" smaller:").append(size);
                }
                break;
            default:
                throw new IllegalStateException("Unknown operator: " + comparablePattern.getComparisonType());
        }
    }

    @Override
    public void visit(SubjectTerm term) {
        sb.append(" subject:").append(term.getPattern());
    }

    @Override
    public void visit(ToTerm term) {
        sb.append(" to:").append(term.getPattern());
    }

}
