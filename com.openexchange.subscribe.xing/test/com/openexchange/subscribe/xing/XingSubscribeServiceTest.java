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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.subscribe.xing;

import java.util.Collection;
import com.openexchange.groupware.container.Contact;
import com.openexchange.subscribe.Subscription;
import com.openexchange.subscribe.SubscriptionException;
import junit.framework.TestCase;


/**
 * {@link XingSubscribeServiceTest}
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class XingSubscribeServiceTest extends TestCase {

    private XingSubscribeService subscriptionService;
    private String password = "secret";
    private String login = "roxyexchanger@ox.io";
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        subscriptionService = new XingSubscribeService();
        
    }

    @Override
    protected void tearDown() throws Exception {
        // TODO Auto-generated method stub
        super.tearDown();
    }
    
    public void testModifyOutgoingShouldSetDisplaynameToLogin() throws SubscriptionException{
        Subscription subscription = new Subscription();
        subscription.getConfiguration().put("login","expected");
        subscriptionService.modifyOutgoing(subscription);
        assertEquals("Display name should be login name", "expected", subscription.getDisplayName());
    }

    public void testBasics() throws XingSubscriptionException{
        Subscription subscription = new Subscription();
        subscription.getConfiguration().put("login", login);
        subscription.getConfiguration().put("password", password);
        
        Collection<Contact> contacts = subscriptionService.getContent(subscription);
        assertTrue("Should know at least two people, but is " + contacts.size(), contacts.size() > 1);
        boolean foundMartin = false, foundCisco = false;
        for(Contact contact: contacts){
            if(contact.containsSurName() && "Francisco Laguna de la Vera".equals( contact.getSurName() ) )
                foundCisco = true;
            if(contact.containsSurName() && "Herfurth".equals( contact.getSurName() ) )
                foundMartin = true;
        }
        assertTrue("Should find Martin", foundMartin);
        assertTrue("Should find Cisco", foundCisco);
    }
}
