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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.messaging.generic;

import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import com.openexchange.messaging.MessagingException;
import com.openexchange.messaging.MessagingExceptionCodes;
import com.openexchange.messaging.MessagingField;
import com.openexchange.messaging.MessagingHeader;
import com.openexchange.messaging.MessagingMessage;
import com.openexchange.messaging.MessagingMessageGetSwitch;
import static com.openexchange.messaging.MessagingField.*;

/**
 * {@link MessagingComparator}
 * Note: Users must catch RuntimeExceptions that wrap messaging exceptions.
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class MessagingComparator implements Comparator<MessagingMessage> {

    private static final EnumSet<MessagingField> WHITELIST = EnumSet.of(
        ID,
        FOLDER_ID,
        CONTENT_TYPE,
        FROM,
        TO,
        BCC,
        CC,
        SUBJECT,
        SIZE,
        SENT_DATE,
        RECEIVED_DATE,
        FLAGS,
        THREAD_LEVEL,
        DISPOSITION_NOTIFICATION_TO,
        PRIORITY,
        COLOR_LABEL
    );
    
    private MessagingField field;
    private MessagingMessageGetSwitch get = new MessagingMessageGetSwitch();
    
    
    public MessagingComparator(MessagingField field) throws MessagingException {
        checkField(field);
        this.field = field;
    }
    
    private void checkField(MessagingField field) throws MessagingException {
        if(!WHITELIST.contains(field)) {
            throw MessagingExceptionCodes.INVALID_SORTING_COLUMN.create(field);
        }
    }

    public int compare(MessagingMessage o1, MessagingMessage o2) {
        try {
            Object c1 = field.doSwitch(get, o1);
            Object c2 = field.doSwitch(get, o2);
            
            if(c1 == c2) {
                return 0;
            }
            
            if(c1 == null) {
                return -1;
            }
            
            if(c2 == null) {
                return 1;
            }
            
            if(null != field.getEquivalentHeader()) {
                Collection<MessagingHeader> headers1 = (Collection<MessagingHeader>) c1;
                Collection<MessagingHeader> headers2 = (Collection<MessagingHeader>) c2;

                MessagingHeader h1 = (headers1.isEmpty()) ? null : headers1.iterator().next();
                MessagingHeader h2 = (headers2.isEmpty()) ? null : headers2.iterator().next();
                
                c1 = h1.getValue();
                c2 = h2.getValue();
                
                if(c1 == c2) {
                    return 0;
                }
                
                if(c1 == null) {
                    return -1;
                }
                
                if(c2 == null) {
                    return 1;
                }
            }
            
            c1 = transform(c1);
            c2 = transform(c2);
            
            if(Comparable.class.isInstance(c1)) {
                return ((Comparable)c1).compareTo(c2);
            }
            
            throw MessagingExceptionCodes.INVALID_SORTING_COLUMN.create(field);
        } catch (MessagingException x) {
            throw new RuntimeException(x);
        }
    }

    private static final EnumSet<MessagingField> INT_FIELDS = EnumSet.of(PRIORITY, THREAD_LEVEL);
    
    private Object transform(Object o) {
        if(INT_FIELDS.contains(field) && String.class.isInstance(o)) {
            try {
                return Integer.parseInt((String)o);
            } catch (NumberFormatException x) {
                return o;
            }
        }
        return o;
    }
    

}
