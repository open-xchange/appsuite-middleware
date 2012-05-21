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
import javax.activation.DataHandler;
import javax.mail.internet.InternetAddress;
import com.openexchange.exception.OXException;
import com.openexchange.index.IndexField;
import com.openexchange.index.QueryParameters.Order;
import com.openexchange.index.mail.MailIndexField;
import com.openexchange.index.solr.internal.mail.AddressComparator;
import com.openexchange.index.solr.mail.SolrMailField;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.QuotedInternetAddress;
import junit.framework.TestCase;


/**
 * {@link AddressComparatorTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class AddressComparatorTest extends TestCase {
    
    public void testCompare() throws Exception {
        MailMessage m1 = new MockMailMessage(new QuotedInternetAddress[] {new QuotedInternetAddress("aaa@abc.de", "Aa, Aa")});
        AddressComparator comp = new AddressComparator(MailIndexField.FROM, Order.ASC);
    }
    
    private static final class MockMailMessage extends MailMessage {
        
        private static final long serialVersionUID = -7674767132402239055L;
        
        private InternetAddress[] addrs;

        public MockMailMessage(InternetAddress[] addrs) {
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
        public void setMailId(String id) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public int getUnreadMessages() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public void setUnreadMessages(int unreadMessages) {
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
        public MailPart getEnclosedMailPart(int index) throws OXException {
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
