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

package com.openexchange.index.solr;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import com.openexchange.groupware.attach.index.ANDTerm;
import com.openexchange.groupware.attach.index.ORTerm;
import com.openexchange.groupware.attach.index.ObjectIdTerm;
import com.openexchange.groupware.attach.index.SearchTerm;
import com.openexchange.index.solr.internal.attachments.SolrAttachmentField;
import com.openexchange.index.solr.internal.attachments.SolrAttachmentSearchTermVisitor;


/**
 * {@link SolrAttachmentSearchTermVisitorTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class SolrAttachmentSearchTermVisitorTest {
    
    @Test
    public void testVisitor() {
        ObjectIdTerm t1 = new ObjectIdTerm("1");
        ObjectIdTerm t2 = new ObjectIdTerm("2");
        ObjectIdTerm t3 = new ObjectIdTerm("3");
        ObjectIdTerm t4 = new ObjectIdTerm("4");
        
        ORTerm orTerm1 = new ORTerm(new SearchTerm<?>[] { t1, t2 });
        ORTerm orTerm2 = new ORTerm(new SearchTerm<?>[] { t3, t4 });
        ANDTerm andTerm = new ANDTerm(new SearchTerm<?>[] { orTerm1, orTerm2 });
        String query = SolrAttachmentSearchTermVisitor.toQuery(andTerm);
        String field = SolrAttachmentField.OBJECT_ID.parameterName();
        String expected = "(((" + field + ":1) OR (" + field + ":2)) AND ((" + field + ":3) OR (" + field + ":4)))";
        assertEquals(expected, query);
    }

}
