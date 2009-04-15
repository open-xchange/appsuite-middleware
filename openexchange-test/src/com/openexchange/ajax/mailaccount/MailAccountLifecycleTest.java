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
import java.util.EnumSet;
import java.util.List;
import org.json.JSONException;
import org.xml.sax.SAXException;
import com.openexchange.ajax.mailaccount.actions.MailAccountAllRequest;
import com.openexchange.ajax.mailaccount.actions.MailAccountAllResponse;
import com.openexchange.ajax.mailaccount.actions.MailAccountGetRequest;
import com.openexchange.ajax.mailaccount.actions.MailAccountGetResponse;
import com.openexchange.ajax.mailaccount.actions.MailAccountListRequest;
import com.openexchange.ajax.mailaccount.actions.MailAccountListResponse;
import com.openexchange.ajax.mailaccount.actions.MailAccountUpdateRequest;
import com.openexchange.ajax.mailaccount.actions.MailAccountUpdateResponse;
import com.openexchange.mailaccount.Attribute;
import com.openexchange.mailaccount.MailAccountDescription;
import com.openexchange.mailaccount.servlet.fields.GetSwitch;
import com.openexchange.tools.servlet.AjaxException;

/**
 * {@link MailAccountLifecycleTest}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class MailAccountLifecycleTest extends AbstractMailAccountTest {

    /**
     * Initializes a new {@link MailAccountLifecycleTest}.
     * 
     * @param name
     */
    public MailAccountLifecycleTest(String name) {
        super(name);
    }

    public void tearDown() throws Exception {
        if (null != mailAccountDescription && 0 != mailAccountDescription.getId()) {
            deleteMailAccount();
        }
        super.tearDown();
    }

    public void testLifeCycle() throws AjaxException, IOException, SAXException, JSONException {

        createMailAccount();
        readByGet();
        readByAll();
        readByList();

        updateMailAccount();
        readByGet();
        readByAll();
        readByList();

    }

    private void updateMailAccount() throws AjaxException, IOException, SAXException, JSONException {
        mailAccountDescription.setName("Other Name");
        mailAccountDescription.setLogin("Other Login");
        mailAccountDescription.setPassword("New Password");
        
        MailAccountUpdateResponse response = getClient().execute(new MailAccountUpdateRequest(mailAccountDescription, EnumSet.of(Attribute.NAME_LITERAL, Attribute.LOGIN_LITERAL, Attribute.PASSWORD_LITERAL)));
        // *shrugs* don't need the response
    }

    private void readByList() throws AjaxException, IOException, SAXException, JSONException {

        MailAccountListResponse response = getClient().execute(
            new MailAccountListRequest(new int[] { mailAccountDescription.getId() }, allFields()));

        List<MailAccountDescription> descriptions = response.getDescriptions();
        assertFalse(descriptions.isEmpty());
        assertEquals(1, descriptions.size());

        boolean found = false;
        for (MailAccountDescription description : descriptions) {
            if (description.getId() == mailAccountDescription.getId()) {
                compare(mailAccountDescription, description);
                found = true;
            }
        }
        assertTrue("Did not find mail account in response", found);
    }

    /**
     * @throws JSONException
     * @throws SAXException
     * @throws IOException
     * @throws AjaxException
     */
    private void readByAll() throws AjaxException, IOException, SAXException, JSONException {
        int[] fields = allFields();
        MailAccountAllResponse response = getClient().execute(new MailAccountAllRequest(fields));

        List<MailAccountDescription> descriptions = response.getDescriptions();
        assertFalse(descriptions.isEmpty());

        boolean found = false;
        for (MailAccountDescription description : descriptions) {
            if (description.getId() == mailAccountDescription.getId()) {
                compare(mailAccountDescription, description);
                found = true;
            }
        }
        assertTrue("Did not find mail account in response", found);
    }

    private int[] allFields() {
        int[] fields = new int[Attribute.values().length];
        int index = 0;
        for (Attribute attr : Attribute.values()) {
            fields[index++] = attr.getId();
        }
        return fields;
    }

    /**
     * @throws JSONException
     * @throws SAXException
     * @throws IOException
     * @throws AjaxException
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

        for (Attribute attribute : Attribute.values()) {
            if (attribute == Attribute.PASSWORD_LITERAL) {
                continue;
            }
            Object expected = attribute.doSwitch(expectedSwitch);
            Object actual = attribute.doSwitch(actualSwitch);

            assertEquals(attribute.getName() + " differs!", expected, actual);
        }
    }

}
