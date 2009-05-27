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
import com.openexchange.ajax.mail.actions.GetRequest;
import com.openexchange.ajax.mail.actions.GetResponse;
import com.openexchange.ajax.mail.actions.MoveMailRequest;
import com.openexchange.ajax.mail.actions.UpdateMailResponse;
import com.openexchange.tools.servlet.AjaxException;


/**
 * {@link MoveMailTest}
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class CopyMailTest extends AbstractMailTest {

    private UserValues values;

    public CopyMailTest(String name) {
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
        clearFolder( values.getDraftsFolder() );
        super.tearDown();
    }
    
    public void testShouldCopyFromSentToDrafts() throws AjaxException, IOException, SAXException, JSONException{
        MailTestManager manager = new MailTestManager(client, false);
        
        String mail = values.getSendAddress();
        sendMail( createEMail(mail, "Copy a mail", "ALTERNATE", "Copy from sent to drafts").toString() );
        
        String origin = values.getInboxFolder();
        String destination = values.getDraftsFolder();
        
        TestMail myMail = TestMail.create( getFirstMailInFolder( origin) );
        String oldID = myMail.getId();
        
        TestMail copiedMail = manager.copy(myMail, destination);
        String newID = copiedMail.getId();
        System.out.println("***** newID : "+newID);
        
        manager.get(destination, newID);
        assertTrue("Should produce no errors when getting copied e-mail", !manager.getLastResponse().hasError() );
        assertTrue("Should produce no conflicts when getting copied e-mail", !manager.getLastResponse().hasConflicts() );
                
    }

}
