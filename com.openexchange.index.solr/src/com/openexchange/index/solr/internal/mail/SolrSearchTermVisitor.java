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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.index.solr.internal.mail;

import java.util.Date;
import org.apache.solr.client.solrj.SolrQuery;
import com.openexchange.index.solr.mail.SolrMailField;
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
 * {@link SolrSearchTermVisitor}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class SolrSearchTermVisitor implements SearchTermVisitor {
    
    private final SolrQuery query;
    

    private SolrSearchTermVisitor(SolrQuery query) {
        super();
        this.query = query;
    }
    
    public static void setQuery(SolrQuery query, SearchTerm<?> searchTerm) {
        searchTerm.accept(new SolrSearchTermVisitor(query));
    }

    @Override
    public void visit(ANDTerm term) {
        SearchTerm<?>[] terms = term.getPattern();
        setQuery(query, terms[0]);
        for (int i = 1; i < terms.length; i++) {
            setQuery(query, terms[i]);
        }
    }
    
    @Override
    public void visit(ORTerm term) {
        SearchTerm<?>[] terms = term.getPattern();
        setQuery(query, terms[0]);
        for (int i = 1; i < terms.length; i++) {
            setQuery(query, terms[i]);
        }
    }
    
    @Override
    public void visit(NOTTerm term) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(ReceivedDateTerm term) {
        ComparablePattern<Date> comparablePattern = term.getPattern();
        long time = comparablePattern.getPattern().getTime();
        if (SolrMailField.RECEIVED_DATE.isIndexed()) {
            switch (comparablePattern.getComparisonType()) {
                case EQUALS:
                    query.add(SolrMailField.RECEIVED_DATE.parameterName(), String.valueOf(time));
                    break;
                case GREATER_THAN:
                    query.add(SolrMailField.RECEIVED_DATE.parameterName(), '[' + String.valueOf(time + 1) + " TO " + String.valueOf(Long.MAX_VALUE) + ']');
                    break;
                case LESS_THAN:
                    query.add(SolrMailField.RECEIVED_DATE.parameterName(), '[' + String.valueOf(Long.MIN_VALUE) + " TO " + String.valueOf(time - 1) + ']');
                    break;
                default:
                    throw new IllegalStateException("Unknown operator: " + comparablePattern.getComparisonType());
            }
        }
    }

    @Override
    public void visit(SentDateTerm term) {
        ComparablePattern<Date> comparablePattern = term.getPattern();
        long time = comparablePattern.getPattern().getTime();
        if (SolrMailField.SENT_DATE.isIndexed()) {
            switch (comparablePattern.getComparisonType()) {
                case EQUALS:
                    query.add(SolrMailField.SENT_DATE.parameterName(), String.valueOf(time));
                    break;
                case GREATER_THAN:
                    query.add(SolrMailField.SENT_DATE.parameterName(), '[' + String.valueOf(time + 1) + " TO " + String.valueOf(Long.MAX_VALUE) + ']');
                    break;
                case LESS_THAN:
                    query.add(SolrMailField.SENT_DATE.parameterName(), '[' + String.valueOf(Long.MIN_VALUE) + " TO " + String.valueOf(time - 1) + ']');
                    break;
                default:
                    throw new IllegalStateException("Unknown operator: " + comparablePattern.getComparisonType());
            }
        }
    }

    @Override
    public void visit(SizeTerm term) {
        ComparablePattern<Integer> comparablePattern = term.getPattern();
        int size = comparablePattern.getPattern().intValue();
        if (SolrMailField.SIZE.isIndexed()) {
            switch (comparablePattern.getComparisonType()) {
                case EQUALS:
                    query.add(SolrMailField.SIZE.parameterName(), String.valueOf(size));
                    break;
                case GREATER_THAN:
                    query.add(SolrMailField.SENT_DATE.parameterName(), '[' + String.valueOf(size + 1) + " TO " + String.valueOf(Integer.MAX_VALUE) + ']');
                    break;
                case LESS_THAN:
                    query.add(SolrMailField.SENT_DATE.parameterName(), '[' + String.valueOf(Integer.MIN_VALUE) + " TO " + String.valueOf(size - 1) + ']');
                    break;
                default:
                    throw new IllegalStateException("Unknown operator: " + comparablePattern.getComparisonType());
            }
        }
    }
    
    @Override
    public void visit(FlagTerm term) {
        int flags = term.getPattern().intValue();
        boolean set = flags >= 0;
        if (!set) {
            return;
        }
        if ((flags & MailMessage.FLAG_ANSWERED) > 0 && SolrMailField.FLAG_ANSWERED.isIndexed()) {
            query.add(SolrMailField.FLAG_ANSWERED.parameterName(), String.valueOf(set));
        }
        if ((flags & MailMessage.FLAG_DELETED) > 0 && SolrMailField.FLAG_DELETED.isIndexed()) {
            query.add(SolrMailField.FLAG_DELETED.parameterName(), String.valueOf(set));
        }
        if ((flags & MailMessage.FLAG_DRAFT) > 0 && SolrMailField.FLAG_DRAFT.isIndexed()) {
            query.add(SolrMailField.FLAG_DRAFT.parameterName(), String.valueOf(set));
        }
        if ((flags & MailMessage.FLAG_FLAGGED) > 0 && SolrMailField.FLAG_FLAGGED.isIndexed()) {
            query.add(SolrMailField.FLAG_FLAGGED.parameterName(), String.valueOf(set));
        }
        if ((flags & MailMessage.FLAG_RECENT) > 0 && SolrMailField.FLAG_RECENT.isIndexed()) {
            query.add(SolrMailField.FLAG_RECENT.parameterName(), String.valueOf(set));
        }
        if ((flags & MailMessage.FLAG_SEEN) > 0 && SolrMailField.FLAG_SEEN.isIndexed()) {
            query.add(SolrMailField.FLAG_SEEN.parameterName(), String.valueOf(set));
        }
        if ((flags & MailMessage.FLAG_USER) > 0 && SolrMailField.FLAG_USER.isIndexed()) {
            query.add(SolrMailField.FLAG_USER.parameterName(), String.valueOf(set));
        }
        if ((flags & MailMessage.FLAG_SPAM) > 0 && SolrMailField.FLAG_SPAM.isIndexed()) {
            query.add(SolrMailField.FLAG_SPAM.parameterName(), String.valueOf(set));
        }
        if ((flags & MailMessage.FLAG_FORWARDED) > 0 && SolrMailField.FLAG_FORWARDED.isIndexed()) {
            query.add(SolrMailField.FLAG_FORWARDED.parameterName(), String.valueOf(set));
        }
        if ((flags & MailMessage.FLAG_READ_ACK) > 0 && SolrMailField.FLAG_READ_ACK.isIndexed()) {
            query.add(SolrMailField.FLAG_READ_ACK.parameterName(), String.valueOf(set));
        }
    }

    @Override
    public void visit(BccTerm term) {
        if (SolrMailField.BCC.isIndexed()) {
            query.add(SolrMailField.BCC.parameterName(), term.getPattern());            
        }
    }

    @Override
    public void visit(BodyTerm term) {
        if (SolrMailField.CONTENT.isIndexed()) {
            query.add(SolrMailField.CONTENT.parameterName(), term.getPattern());            
        }
    }


    @Override
    public void visit(CcTerm term) {
        if (SolrMailField.CC.isIndexed()) {
            query.add(SolrMailField.CC.parameterName(), term.getPattern());            
        }
    }

    @Override
    public void visit(FromTerm term) {
        if (SolrMailField.FROM.isIndexed()) {
            query.add(SolrMailField.FROM.parameterName(), term.getPattern());            
        }
    }

    @Override
    public void visit(SubjectTerm term) {
        if (SolrMailField.SUBJECT.isIndexed()) {
            query.add(SolrMailField.SUBJECT.parameterName(), term.getPattern());            
        }
    }

    @Override
    public void visit(ToTerm term) {
        if (SolrMailField.TO.isIndexed()) {
            query.add(SolrMailField.TO.parameterName(), term.getPattern());            
        }
    }
    
    @Override
    public void visit(HeaderTerm term) {
        throw new IllegalStateException("Unsupported search term: " + HeaderTerm.class.getName());
    }
    
    @Override
    public void visit(BooleanTerm term) {
        throw new IllegalStateException("Unsupported search term: " + BooleanTerm.class.getName());
    }

}
