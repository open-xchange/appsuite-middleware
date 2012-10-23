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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.activation.DataHandler;
import javax.mail.internet.InternetAddress;
import junit.framework.TestCase;
import com.openexchange.exception.OXException;
import com.openexchange.index.IndexDocument;
import com.openexchange.index.QueryParameters.Order;
import com.openexchange.index.StandardIndexDocument;
import com.openexchange.index.solr.internal.Services;
import com.openexchange.index.solr.internal.mail.AddressComparator;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.index.MailIndexField;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.server.ServiceLookup;


/**
 * {@link AddressComparatorTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class AddressComparatorTest extends TestCase {
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Services.setServiceLookup(new ServiceLookup() {            
            @Override
            public <S> S getService(Class<? extends S> clazz) {
                return (S) new MockConfigurationService();
            }
            
            @Override
            public <S> S getOptionalService(Class<? extends S> clazz) {
                return null;
            }
        });
    }
    
    public void testCompare() throws Exception {    	
        IndexDocument<MailMessage> m1 = new StandardIndexDocument<MailMessage>(new MockMailMessage(new QuotedInternetAddress[] {
        		new QuotedInternetAddress("aaa@abc.de", "Aa, Aa"),
        		new QuotedInternetAddress("bbb@abc.de", "Bb, Bb"),
        		new QuotedInternetAddress("iii@abc.de", "Ii, Ii")        		
        		}));
        IndexDocument<MailMessage> m2 = new StandardIndexDocument<MailMessage>(new MockMailMessage(new QuotedInternetAddress[] {
        		new QuotedInternetAddress("hhh@abc.de", "Hh, Hh"),
        		new QuotedInternetAddress("ccc@abc.de", "Cc, Cc"), 
        		new QuotedInternetAddress("fff@abc.de", "Ff, Ff")
        		}));
        IndexDocument<MailMessage> m3 = new StandardIndexDocument<MailMessage>(new MockMailMessage(new QuotedInternetAddress[] {
        		new QuotedInternetAddress("ggg@abc.de", "Gg, Gg"),
        		new QuotedInternetAddress("eee@abc.de", "Ee, Ee"),
        		new QuotedInternetAddress("ddd@abc.de", "Dd, Dd")
        		}));
        List<IndexDocument<MailMessage>> documents = new ArrayList<IndexDocument<MailMessage>>();
        documents.add(m1);
        documents.add(m2);
        documents.add(m3);
        Collections.shuffle(documents);      

        AddressComparator comp = new AddressComparator(MailIndexField.FROM, Order.ASC);
        Collections.sort(documents, comp);
        assertTrue(documents.get(0) == m1);
        assertTrue(documents.get(1) == m2);
        assertTrue(documents.get(2) == m3);
        
        comp = new AddressComparator(MailIndexField.FROM, Order.DESC);
        Collections.sort(documents, comp);
        assertTrue(documents.get(0) == m1);
        assertTrue(documents.get(1) == m2);
        assertTrue(documents.get(2) == m3);
    }
    
    public void testNull() throws Exception {
        IndexDocument<MailMessage> m1 = new StandardIndexDocument<MailMessage>(new MockMailMessage(new QuotedInternetAddress[] {
            new QuotedInternetAddress("aaa@abc.de", "Aa, Aa"),
            new QuotedInternetAddress("bbb@abc.de", "Bb, Bb"),
            null               
            }));
        IndexDocument<MailMessage> m2 = new StandardIndexDocument<MailMessage>(new MockMailMessage(null));
        IndexDocument<MailMessage> m3 = new StandardIndexDocument<MailMessage>(new MockMailMessage(new QuotedInternetAddress[] {
            new QuotedInternetAddress("ggg@abc.de", "Gg, Gg"),
            new QuotedInternetAddress("eee@abc.de", "Ee, Ee"),
            new QuotedInternetAddress("ddd@abc.de", "Dd, Dd")
            }));
    
        List<IndexDocument<MailMessage>> documents = new ArrayList<IndexDocument<MailMessage>>();
        documents.add(m1);
        documents.add(m2);
        documents.add(m3);
        Collections.shuffle(documents);   
        
        AddressComparator comp = new AddressComparator(MailIndexField.FROM, Order.ASC);
        Collections.sort(documents, comp);
        assertTrue(documents.get(0) == m1);
        assertTrue(documents.get(1) == m3);
        assertTrue(documents.get(2) == m2);
    }
    
    private static final class MockMailMessage extends MailMessage {

		private static final long serialVersionUID = -7674767132402239055L;
        
        private final InternetAddress[] addrs;

        public MockMailMessage(final InternetAddress[] addrs) {
            super();
            this.addrs = addrs;
        }
        
        @Override
        public InternetAddress[] getFrom() {
            return addrs;
        }

        @Override
        public String getMailId() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void setMailId(final String id) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public int getUnreadMessages() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public void setUnreadMessages(final int unreadMessages) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public Object getContent() throws OXException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public DataHandler getDataHandler() throws OXException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public InputStream getInputStream() throws OXException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public int getEnclosedCount() throws OXException {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public MailPart getEnclosedMailPart(final int index) throws OXException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void loadContent() throws OXException {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void prepareForCaching() {
            // TODO Auto-generated method stub
            
        }
        
    }

}
