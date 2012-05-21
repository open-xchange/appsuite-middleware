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

import java.util.Comparator;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import com.openexchange.exception.OXException;
import com.openexchange.index.IndexDocument;
import com.openexchange.index.IndexField;
import com.openexchange.index.QueryParameters.Order;
import com.openexchange.index.mail.MailIndexField;
import com.openexchange.index.solr.mail.SolrMailField;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.solr.SolrExceptionCodes;

public class AddressComparator implements Comparator<IndexDocument<MailMessage>> {
    
    private SolrMailField sortField;
    
    private final Order order;
    
    public AddressComparator(IndexField indexField, Order order) throws OXException {
        super();
        this.order = order;
        sortField = null;
        if (indexField != null && indexField instanceof MailIndexField) {
            sortField = SolrMailField.solrMailFieldFor((MailIndexField) indexField);
        }
        
        if (sortField == null) {
            throw SolrExceptionCodes.INVALID_SORT_FIELD.create(indexField);
        }
    }
    
    @Override
    public int compare(IndexDocument<MailMessage> firstDocument, IndexDocument<MailMessage> secondDocument) {            
        MailMessage firstMail = firstDocument.getObject();
        MailMessage secondMail = secondDocument.getObject();
        InternetAddress[] firstAddrs;
        InternetAddress[] secondAddrs;
        switch(sortField) {
            case FROM:
                firstAddrs = firstMail.getFrom();
                secondAddrs = secondMail.getFrom();
                break;
                
            case TO:
                firstAddrs = firstMail.getTo();
                secondAddrs = secondMail.getTo();
                break;
                
            case CC:
                firstAddrs = firstMail.getCc();
                secondAddrs = secondMail.getCc();
                break;
                
            case BCC:
                firstAddrs = firstMail.getBcc();
                secondAddrs = secondMail.getBcc();
                break;
                
            default:
                firstAddrs = null;
                secondAddrs = null;                 
        }
        
        if (firstAddrs != null
            && secondAddrs != null
            && firstAddrs.length != 0
            && secondAddrs.length != 0) {
            
            String firstHighest = getHighest(firstAddrs);
            String secondHighest = getHighest(secondAddrs);
            if (isFirstHigherThanSecond(firstHighest, secondHighest)) {
                return -1;
            } else {
                return 1;
            }                
        }
        
        return 0;
    }
    
    private String getHighest(InternetAddress[] addrs) {
        String highest = null;
        for (InternetAddress addr : addrs) {
            QuotedInternetAddress quoted;
            if (addr instanceof QuotedInternetAddress) {
                quoted = (QuotedInternetAddress) addr;
            } else {
                try {
                    quoted = new QuotedInternetAddress(addr.toUnicodeString());
                } catch (AddressException e) {
                    return null;
                }
            }
            
            String quotedStr = quoted.toUnicodeString();
            if (highest == null) {
                highest = quotedStr;
            } else {
                if (isFirstHigherThanSecond(highest, quotedStr)) {
                    highest = quotedStr;
                }
            }
        }
        
        return highest;
    }
    
    private boolean isFirstHigherThanSecond(String first, String second) {
        if ((first.compareToIgnoreCase(second) > 0 && order.equals(Order.ASC))
            || (first.compareToIgnoreCase(second) < 0 && order.equals(Order.DESC))) {
            return false;
        }
        
        return true;
    }
    
}