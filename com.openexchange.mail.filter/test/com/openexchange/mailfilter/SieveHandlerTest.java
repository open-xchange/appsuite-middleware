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
package com.openexchange.mailfilter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.junit.Test;

import com.openexchange.jsieve.SieveHandler;
import com.openexchange.jsieve.SieveHandler.Capabilities;
import com.openexchange.jsieve.exceptions.OXSieveHandlerException;
import com.openexchange.jsieve.exceptions.OXSieveHandlerInvalidCredentialsException;

/**
 * This test case is used to test the whole functionality of the SieveHandler.
 * It is independent of any RMI call and just creates a SieveHandler instance
 * and do the tests.
 * 
 * @author d7
 * 
 */
public class SieveHandlerTest {

    @Test
    public void testwhole() throws OXSieveHandlerException, IOException, OXSieveHandlerInvalidCredentialsException {
        final String CRLF = "\r\n";
        final SieveHandler sh = new SieveHandler("test4.test4", "secret", "192.168.73.128", 2000);
        sh.initializeConnection();
        final Capabilities capabilities = sh.getCapabilities();
        final ArrayList<String> sieve = capabilities.getSieve();
        System.out.println(sieve);
        ArrayList<String> scriptlist = sh.getScriptList();
        System.out.println("The script list before:");
        System.out.println("-----------------------");
        for (final String scriptname : scriptlist) {
            System.out.println(scriptname);
            sh.remove(scriptname);
            System.out.println("Script " + scriptname + " removed.");
            // sh.setScriptStatus(temp1, false);
        }
        final StringBuilder defaultscript = new StringBuilder();
        defaultscript.append("require [\"fileinto\", \"reject\"];");
        defaultscript.append(CRLF);
        defaultscript.append("if anyof (header :contains");
        defaultscript.append(CRLF);
        defaultscript.append("\"from\" [\"ebay.de\", \"ebay.comasdfasf\"],");
        defaultscript.append(CRLF);
        defaultscript.append("header :contains");
        defaultscript.append(CRLF);
        defaultscript.append("[\"In-Reply-To\", \"Message-ID\"]");
        defaultscript.append(CRLF);
        defaultscript.append("\"JavaMail.ebayapp\") {");
        defaultscript.append(CRLF);
        defaultscript.append("fileinto \"INBOX.eBay\"; }");
        defaultscript.append(CRLF);
        defaultscript.append("elsif allof (header :contains \"from\" \"zitate.at\",");
        defaultscript.append(CRLF);
        defaultscript.append("header :contains \"subject\" \"Zitat des Tages\")");
        defaultscript.append(CRLF);
        defaultscript.append("{");
        defaultscript.append(CRLF);
        defaultscript.append("fileinto \"pub.Mailinglists.Zitate\";");
        defaultscript.append(CRLF);
        defaultscript.append("}");
        defaultscript.append(CRLF);
        defaultscript.append("else { fileinto \"INBOX\"; }");
        defaultscript.append(CRLF);

        System.out.println(defaultscript.toString());
        final String defaultscriptname = "default";
        sh.setScript(defaultscriptname, defaultscript.toString().getBytes("utf-8"));

        scriptlist = sh.getScriptList();
        System.out.println("The script list after:");
        System.out.println("-----------------------");
        for (final String scriptname : scriptlist) {
            System.out.println(scriptname);
            sh.setScriptStatus(scriptname, true);
            System.out.println("Script " + scriptname + " set to active");
        }
        assertEquals(scriptlist.get(0), defaultscriptname);

        String activescript = sh.getActiveScript();
        System.out.println("The active script: " + activescript);
        sh.setScriptStatus(activescript, false);
        System.out.println("Script " + activescript + " set to disabled");
        assertEquals(scriptlist.get(0), defaultscriptname);

        activescript = sh.getActiveScript();
        System.out.println("The active script: " + activescript);
        assertTrue(activescript == null);

        scriptlist = sh.getScriptList();
        System.out.println("The script list after:");
        System.out.println("-----------------------");
        for (final String scriptname : scriptlist) {
            System.out.println(scriptname);
            System.out.println("Script " + scriptname + " removed.");
            final String script = new String(sh.getScript(scriptname));
            final String test = defaultscript.toString();
            assertEquals(script, test);
        }
        sh.close();

    }
    
    @Test
    public void testadminauth() throws OXSieveHandlerException, UnsupportedEncodingException, IOException, OXSieveHandlerInvalidCredentialsException {
        final SieveHandler sh = new SieveHandler("testuser","oxadmin","secret","localhost",2000);
        sh.initializeConnection();
        sh.close();
    }
}
