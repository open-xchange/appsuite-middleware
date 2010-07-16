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

package com.openexchange.ajax.mail;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import org.json.JSONArray;
import org.json.JSONObject;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.UserValues;
import com.openexchange.ajax.mail.actions.DeleteRequest;
import com.openexchange.ajax.mail.actions.ImportMailRequest;
import com.openexchange.ajax.mail.actions.ImportMailResponse;

/**
 * 
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 *
 */
public class Bug16141Test extends AbstractAJAXSession {
    
    private AJAXClient client;

    private String folder;

    private UserValues values;
    
    private String[][] ids = null;
    
    public Bug16141Test(String name) {
        super(name);
    }
    
    @Override
    public void setUp() throws Exception {
        super.setUp();
        client = getClient();
        values = client.getValues();
        folder = values.getInboxFolder();
    }
    
    public void testMailImport() throws Exception {
        InputStream[] is = createABunchOfMails();
        
        final ImportMailRequest importReq = new ImportMailRequest(folder, MailFlag.SEEN.getValue(), is);
        final ImportMailResponse importResp = client.execute(importReq);
        JSONArray json = (JSONArray) importResp.getData();
        
        int err = 0;
        for (int i = 0; i < json.length(); i++) {
            JSONObject jo = json.getJSONObject(i);
            if (jo.has("Error")) {
                err++;
            }
        }
        
        if (err != 1) {
            fail("Number of corrupt mails is wrong");
        }
        
        if (json.length() - err != 3) {
            fail("Import did not run til end.");
        }
    }
    
    private InputStream[] createABunchOfMails() {        
        String msg = "";
        
        InputStream is1 = null;
        try {
            is1 = new FileInputStream("../open-xchange-test/testData/bug16141_1.eml");
        } catch (FileNotFoundException e) {
            msg += e.getMessage() + "\n";
        }
        
        InputStream is2 = null;
        try {
            is2 = new FileInputStream("../open-xchange-test/testData/bug16141_2.eml");
        } catch (FileNotFoundException e) {
            msg += e.getMessage() + "\n";
        }
        
        InputStream is3 = null;
        try {
            is3 = new FileInputStream("../open-xchange-test/testData/bug16141_3.eml");
        } catch (FileNotFoundException e) {
            msg += e.getMessage() + "\n";
        }
        
        InputStream is4 = null;
        try {
            is4 = new FileInputStream("../open-xchange-test/testData/bug16141_4.eml");
        } catch (FileNotFoundException e) {
            msg += e.getMessage() + "\n";
        }
        
        if (is1 == null || is2 == null || is3 == null || is4 == null) {
            fail("Testmails konnten nicht geladen werden. Fehler: " + msg);
        }
        
        final InputStream mail[] = {is1, is2, is3, is4};
        return mail;
    }
    
    @Override
    public void tearDown() throws Exception {
        if (ids != null) {
            client.execute(new DeleteRequest(ids));
        }
        super.tearDown();
    }

}
