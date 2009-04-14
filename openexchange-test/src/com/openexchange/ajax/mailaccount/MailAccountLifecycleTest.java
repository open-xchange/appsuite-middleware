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

package com.openexchange.ajax.mailaccount;

import java.io.IOException;
import java.util.List;
import org.json.JSONException;
import org.xml.sax.SAXException;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.mailaccount.actions.MailAccountAllRequest;
import com.openexchange.ajax.mailaccount.actions.MailAccountAllResponse;
import com.openexchange.ajax.mailaccount.actions.MailAccountDeleteRequest;
import com.openexchange.ajax.mailaccount.actions.MailAccountGetRequest;
import com.openexchange.ajax.mailaccount.actions.MailAccountGetResponse;
import com.openexchange.ajax.mailaccount.actions.MailAccountInsertRequest;
import com.openexchange.ajax.mailaccount.actions.MailAccountInsertResponse;
import com.openexchange.ajax.mailaccount.actions.MailAccountListRequest;
import com.openexchange.ajax.mailaccount.actions.MailAccountListResponse;
import com.openexchange.mailaccount.MailAccountDescription;
import com.openexchange.mailaccount.servlet.fields.GetSwitch;
import com.openexchange.mailaccount.servlet.fields.MailAccountFields;
import com.openexchange.mailaccount.servlet.fields.MailAccountFields.Attribute;
import com.openexchange.tools.servlet.AjaxException;


/**
 * {@link MailAccountLifecycleTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class MailAccountLifecycleTest extends AbstractAJAXSession {
    /**
     * Initializes a new {@link MailAccountLifecycleTest}.
     * @param name
     */
   public MailAccountLifecycleTest(String name) {
        super(name);
    }
    
    public void tearDown() throws AjaxException, IOException, SAXException, JSONException {
        if(null != mailAccountDescription) {
            deleteMailAccount();
        }
    }

    private MailAccountDescription mailAccountDescription;

    public void testLifeCycle() throws AjaxException, IOException, SAXException, JSONException {
        
        createMailAccount();
        readByGet();
        readByAll();
        readByList();
        /*updateMailAccount();
        readByGet();
        readByAll();
        readByList();
        */
    }

    private void deleteMailAccount() throws AjaxException, IOException, SAXException, JSONException {
        getClient().execute(new MailAccountDeleteRequest(mailAccountDescription.getId()));
    }

    /**
     * 
    
    private void updateMailAccount() {
        // TODO Auto-generated method stub
        
    }
     * @throws JSONException 
     * @throws SAXException 
     * @throws IOException 
     * @throws AjaxException 

     * 
     */
    private void readByList() throws AjaxException, IOException, SAXException, JSONException {
        
        MailAccountListResponse response = getClient().execute(new MailAccountListRequest(new int[] {mailAccountDescription.getId()}, allFields()));
        
        List<MailAccountDescription> descriptions = response.getDescriptions();
        assertFalse(descriptions.isEmpty());
        assertEquals(1, descriptions.size());
        
        boolean found = false;
        for(MailAccountDescription description : descriptions) {
            if(description.getId() == mailAccountDescription.getId()) {
                compare(mailAccountDescription, description);
                found = true;
            }
        }
        assertTrue("Did not find mail account in response" ,found);
    }

    /**
     * @throws JSONException 
     * @throws SAXException 
     * @throws IOException 
     * @throws AjaxException 
     * 
     */
    private void readByAll() throws AjaxException, IOException, SAXException, JSONException {
        int[] fields = allFields();
        MailAccountAllResponse response = getClient().execute(new MailAccountAllRequest(fields));
        
        List<MailAccountDescription> descriptions = response.getDescriptions();
        assertFalse(descriptions.isEmpty());
        
        boolean found = false;
        for(MailAccountDescription description : descriptions) {
            if(description.getId() == mailAccountDescription.getId()) {
                compare(mailAccountDescription, description);
                found = true;
            }
        }
        assertTrue("Did not find mail account in response" ,found);
    }

    private int[] allFields() {
        int[] fields = new int[MailAccountFields.Attribute.values().length];
        int index = 0;
        for(Attribute attr : MailAccountFields.Attribute.values()) {
            fields[index++] = attr.getId();
        }
        return fields;
    }

    /**
     * @throws JSONException 
     * @throws SAXException 
     * @throws IOException 
     * @throws AjaxException 
     * 
     */
    private void readByGet() throws AjaxException, IOException, SAXException, JSONException {
        MailAccountGetRequest request = new MailAccountGetRequest(mailAccountDescription.getId());
        MailAccountGetResponse response = getClient().execute(request);
        
        MailAccountDescription loaded = response.getAsDescription();
        
        compare(mailAccountDescription, loaded);
        
    }

    private void compare(MailAccountDescription expectedAcc, MailAccountDescription actualAcc) {
        GetSwitch expectedSwitch = new GetSwitch(expectedAcc);
        GetSwitch actualSwitch = new GetSwitch(actualAcc);
        
        for(Attribute attribute : MailAccountFields.Attribute.values()) {
            if(attribute == MailAccountFields.Attribute.PASSWORD_LITERAL) {
                continue;
            }
            Object expected = attribute.doSwitch(expectedSwitch);
            Object actual = attribute.doSwitch(actualSwitch);
            
            assertEquals(attribute.getName()+" differs!", expected, actual);
        }
    }

    /**
     * @throws JSONException 
     * @throws SAXException 
     * @throws IOException 
     * @throws AjaxException 
     * 
     */
    private void createMailAccount() throws AjaxException, IOException, SAXException, JSONException {
        mailAccountDescription = new MailAccountDescription();
        mailAccountDescription.setConfirmedHam("confirmedHam");
        mailAccountDescription.setConfirmedSpam("confirmedSpam");
        mailAccountDescription.setDrafts("drafts");
        mailAccountDescription.setLogin("login");
        mailAccountDescription.setMailServerURL("imap://mail.test.invalid");
        mailAccountDescription.setName("Test Mail Account");
        mailAccountDescription.setPassword("Password");
        mailAccountDescription.setPrimaryAddress("bob@test.invalid");
        mailAccountDescription.setSent("sent");
        mailAccountDescription.setSpam("Spam");
        mailAccountDescription.setSpamHandler("spamHandler");
        mailAccountDescription.setTransportServerURL("localhost");
        mailAccountDescription.setTrash("trash");
        
        MailAccountInsertResponse response = getClient().execute(new MailAccountInsertRequest(mailAccountDescription));
        response.fillObject(mailAccountDescription);
        
    }
}
