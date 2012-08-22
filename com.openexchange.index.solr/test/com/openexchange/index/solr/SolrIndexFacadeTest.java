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

import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;
import com.openexchange.groupware.Types;
import com.openexchange.index.IndexAccess;
import com.openexchange.index.IndexDocument;
import com.openexchange.index.IndexFacadeService;
import com.openexchange.index.IndexResult;
import com.openexchange.index.QueryParameters;
import com.openexchange.index.SearchHandler;
import com.openexchange.index.StandardIndexDocument;
import com.openexchange.index.solr.internal.Services;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.converters.MimeMessageConverter;
import com.openexchange.mail.search.FromTerm;


/**
 * {@link SolrIndexFacadeTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class SolrIndexFacadeTest extends TestCase {
    
    private static final byte[] MAIL = ("Date: Sat, 14 Nov 2009 17:03:09 +0100 (CET)\n" +
        "From: Alice Doe <alice@foobar.com>\n" +
        "To: bob@foobar.com\n" +
        "Message-ID: <1837640730.5.1258214590077.JavaMail.foobar@foobar>\n" +
        "Subject: The mail subject\n" +
        "MIME-Version: 1.0\n" +
        "Content-Type: text/plain\n" +
        "Content-Disposition: inline; filename=foo.txt\n" +
        "X-Priority: 3\n" +
        "\n" +
        "Mail text.\n" +
        "\n" +
        "People have been asking for support for the IMAP IDLE command for quite\n" +
        "a few years and I think I've finally figured out how to provide such\n" +
        "support safely. The difficulty isn't in executing the command, which\n" +
        "is quite straightforward, the difficulty is in deciding how to expose\n" +
        "it to applications, and inhandling the multithreading issues that\n" +
        "arise.").getBytes();
    
    public void testAddAndGetMessage() throws Exception {
        try {
            final IndexFacadeService facade = Services.getService(IndexFacadeService.class);
            final IndexAccess<MailMessage> indexAccess = facade.acquireIndexAccess(Types.EMAIL, 999, 1);
            final MailMessage message = MimeMessageConverter.convertMessage(MAIL);
            final IndexDocument<MailMessage> document = new StandardIndexDocument<MailMessage>(message);
            indexAccess.addContent(document, true);
            final FromTerm fromTerm = new FromTerm("Alice");
            final Map<String, Object> params = new HashMap<String, Object>();
//            params.put("accountId", 0);
            final QueryParameters qp = new QueryParameters.Builder(params).setHandler(SearchHandler.CUSTOM).setSearchTerm(fromTerm).build();
            final IndexResult<MailMessage> result = indexAccess.query(qp, null);
            facade.releaseIndexAccess(indexAccess);
        } catch (final Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
//        final SolrAccessService sas = Services.getService(SolrAccessService.class);
//        sas.optimize(new SolrCoreIdentifier(1, 3, Types.EMAIL));
//        final MailMessage message = MimeMessageConverter.convertMessage(MAIL);
//        final IndexDocument<MailMessage> document = new StandardIndexDocument<MailMessage>(message, Type.MAIL);
//        indexAccess.addContent(document);
//        
//        final Map<String, Object> params = new HashMap<String, Object>(4);
//        params.put("sort", "received_date");
//        params.put("order", "desc");
//        final QueryParameters queryParameter = new QueryParameters.Builder("(user:3) AND (context:1) AND (content_flag:true) AND (from_personal:alice)").setType(IndexDocument.Type.MAIL).setParameters(params).build();
//        final IndexResult<MailMessage> result = indexAccess.query(queryParameter);
//        assertEquals("Found wrong number of mails.", 1, result.getNumFound());        
//        final MailMessage foundMessage = result.getResults().get(0).getObject();
//        assertEquals("Dates were not equal.", message.getSentDate(), foundMessage.getSentDate());
//        assertTrue("From were not equal.", Arrays.equals(message.getFrom(), foundMessage.getFrom()));
//        assertTrue("To were not equal.", Arrays.equals(message.getTo(), foundMessage.getTo()));
//        assertEquals("Message-ID were not equal.", message.getMailId(), foundMessage.getMailId());
//        assertEquals("Subject were not equal.", message.getSubject(), foundMessage.getSubject());
//        assertEquals("Mail size were not equal.", message.getSize(), foundMessage.getSize());
//        facade.releaseIndexAccess(indexAccess);
    }

}
