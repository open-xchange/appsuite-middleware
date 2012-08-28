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

package com.openexchange.index.solr.internal.attachments;

import com.openexchange.index.attachments.ANDTerm;
import com.openexchange.index.attachments.ORTerm;
import com.openexchange.index.attachments.ObjectIdTerm;
import com.openexchange.index.attachments.SearchTerm;
import com.openexchange.index.attachments.SearchTermVisitor;


/**
 * {@link SolrAttachmentSearchTermVisitor}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class SolrAttachmentSearchTermVisitor implements SearchTermVisitor {
    
    private StringBuilder queryBuilder;
    

    public SolrAttachmentSearchTermVisitor() {
        super();
        queryBuilder = new StringBuilder();
    }

    @Override
    public void visit(ORTerm term) {
        SearchTerm<?>[] searchTerms = term.getPattern();
        if (searchTerms == null || searchTerms.length == 0) {
            return;
        }
        
        SearchTerm<?> firstTerm = searchTerms[0];
        if (searchTerms.length == 1) {
            queryBuilder.append(toQuery(firstTerm));
            return;
        }        
        
        queryBuilder.append(" (");
        queryBuilder.append(toQuery(firstTerm));
        for (int i = 1; i < searchTerms.length; i++) {
            queryBuilder.append(" OR ");
            queryBuilder.append(toQuery(searchTerms[i]));            
        }
        queryBuilder.append(")");
    }
    
    @Override
    public void visit(ANDTerm term) {        
        SearchTerm<?>[] searchTerms = term.getPattern();
        if (searchTerms == null || searchTerms.length == 0) {
            return;
        }
        
        SearchTerm<?> firstTerm = searchTerms[0];
        if (searchTerms.length == 1) {
            queryBuilder.append(toQuery(firstTerm));
            return;
        }        
        
        queryBuilder.append(" (");
        queryBuilder.append(toQuery(firstTerm));
        for (int i = 1; i < searchTerms.length; i++) {
            queryBuilder.append(" AND ");
            queryBuilder.append(toQuery(searchTerms[i]));            
        }
        queryBuilder.append(")");
    }
    
    @Override
    public void visit(ObjectIdTerm objectIdTerm) {
        queryBuilder.append(" (")
            .append(SolrAttachmentField.OBJECT_ID.parameterName())
            .append(":")
            .append(objectIdTerm.getPattern())
            .append(")");
    }
    
    @Override
    public String toString() {
        return queryBuilder.toString().trim();
    }
    
    public static String toQuery(SearchTerm<?> searchTerm) {
        SolrAttachmentSearchTermVisitor visitor = new SolrAttachmentSearchTermVisitor();
        searchTerm.accept(visitor);
        
        return visitor.toString();
    }
}
