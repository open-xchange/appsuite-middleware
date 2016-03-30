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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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
import com.openexchange.exception.OXException;


/**
 * {@link CopyMailWithManagerTest}
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class CopyMailWithManagerTest extends AbstractMailTest {

    private UserValues values;

    public CopyMailWithManagerTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        values = getClient().getValues();
        clearFolder( values.getSentFolder() );
        clearFolder(values.getInboxFolder() );
        clearFolder( values.getDraftsFolder() );
    }

    public void testShouldCopyFromSendToDrafts() throws OXException, JSONException, IOException, SAXException{
        MailTestManager manager = new MailTestManager(client, false);
        String destination = values.getDraftsFolder();


        TestMail myMail = new TestMail(values.getSendAddress(), values.getSendAddress(), "Testing copy with manager", "alternative", "Copying a mail we just sent and received vom the inbox to the draft folder");
        myMail = manager.send(myMail);

        TestMail movedMail = manager.copy(myMail, destination);
        assertFalse("Should get no errors when copying e-mail", manager.getLastResponse().hasError() );
        String newID = movedMail.getId();

        manager.get(destination, newID);
        assertFalse("Should get no errors when getting copied e-mail", manager.getLastResponse().hasError() );
        assertFalse("Should produce no conflicts when getting copied e-mail", manager.getLastResponse().hasConflicts() );

        manager.get(myMail.getFolderAndId());
        assertFalse("Should still find original e-mail", manager.getLastResponse().hasError() );

        manager.cleanUp();

        manager.get(destination, newID);
        assertTrue("Should not find copied e-mail after cleaning up", manager.getLastResponse().hasError() );

        manager.get(myMail.getFolderAndId());
        assertTrue("Should not find original e-mail after cleaning up", manager.getLastResponse().hasError() );
    }


}
