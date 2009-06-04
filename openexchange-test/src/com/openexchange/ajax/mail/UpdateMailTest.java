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

package com.openexchange.ajax.mail;

import java.io.IOException;
import org.json.JSONException;
import org.xml.sax.SAXException;
import com.openexchange.ajax.framework.UserValues;
import com.openexchange.ajax.mail.actions.UpdateMailRequest;
import com.openexchange.ajax.mail.actions.UpdateMailResponse;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.tools.servlet.AjaxException;


/**
 * {@link UpdateMailTest}
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class UpdateMailTest extends AbstractMailTest {

    private UserValues values;

    public UpdateMailTest(String name) {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        values = getClient().getValues();
    }

    @Override
    protected void tearDown() throws Exception {
        clearFolder( values.getSentFolder() );
        clearFolder( values.getInboxFolder() );
        super.tearDown();
    }

    public void testShouldBeAbleToAddFlags() throws AjaxException, IOException, SAXException, JSONException{
        String mail = values.getSendAddress();
        sendMail( createEMail(mail, "Update test for adding and removing a flag", "ALTERNATE", "Just a little bit").toString() );
        TestMail myMail = new TestMail( getFirstMailInFolder(values.getInboxFolder() ) );
        
        UpdateMailRequest updateRequest = new UpdateMailRequest( myMail.getFolder(), myMail.getId() );
        int additionalFlag = MailMessage.FLAG_FLAGGED; //note: doesn't work for 16 (recent) and 64 (user)
        updateRequest.setFlags( additionalFlag );
        updateRequest.updateFlags();
        UpdateMailResponse updateResponse = getClient().execute(updateRequest);
        
        TestMail updatedMail = getMail(updateResponse.getFolder(), updateResponse.getID());
        assertTrue("Flag should have been changed", (updatedMail.getFlags() & additionalFlag) == additionalFlag);
        
        updateRequest.removeFlags();
        updateResponse = getClient().execute(updateRequest);
        
        updatedMail = getMail(updateResponse.getFolder(), updateResponse.getID());
        assertTrue("Flag should have been changed back again", (updatedMail.getFlags() & additionalFlag) == 0);
    }

    
    public void testShouldBeAbleToSetColors() throws AjaxException, IOException, SAXException, JSONException{
        String mail = values.getSendAddress();
        sendMail( createEMail(mail, "Update test for changing colors", "ALTERNATE", "Just a little bit").toString() );
        TestMail myMail = new TestMail( getFirstMailInFolder(values.getInboxFolder() ) );
        
        UpdateMailRequest updateRequest = new UpdateMailRequest( myMail.getFolder(), myMail.getId() );
        int myColor = 8;
        updateRequest.setColor(myColor );
        UpdateMailResponse updateResponse = getClient().execute(updateRequest);
        
        TestMail updatedMail = getMail(updateResponse.getFolder(), updateResponse.getID());
        assertEquals("Color should have been changed", myColor, updatedMail.getColor());
        
        myColor = 4;
        updateRequest.setColor(myColor );
        updateResponse = getClient().execute(updateRequest);
        
        updatedMail = getMail(updateResponse.getFolder(), updateResponse.getID());
        assertEquals("Color should have been changed again", myColor, updatedMail.getColor());
    }
}
