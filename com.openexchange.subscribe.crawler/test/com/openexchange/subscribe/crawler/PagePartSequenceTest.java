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

package com.openexchange.subscribe.crawler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import com.openexchange.groupware.container.Contact;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class PagePartSequenceTest extends TestCase {
	public void testPagePartSequence() {
		File file = new File("test-resources/GoogleMailResultTestPage.txt");
		StringBuilder contents = new StringBuilder();
	    
	    try {
	      BufferedReader input =  new BufferedReader(new FileReader(file));
	      try {
	        String line = null;
	        while (( line = input.readLine()) != null){
	          contents.append(line);
	          contents.append(System.getProperty("line.separator"));
	        }
	      }
	      finally {
	        input.close();
	      }
	    }
	    catch (IOException ex){
	      ex.printStackTrace();
	    }
	    
	    String page = contents.toString();
	    
	    ArrayList<PagePart> pageParts = new ArrayList<PagePart>();
	    pageParts.add(new PagePart("(<input[\\s]{1}name=ct_nm[\\s]{1}id=ct_nm[\\s]{1}size=[0-9]{2}[\\s]{1}value=\")([a-zA-Z\\s]*)(\"><br></td>)", "display_name"));
	    pageParts.add(new PagePart("(<input[\\s]{1}name=ct_em[\\s]{1}id=ct_em[\\s]{1}size=[0-9]{2}[\\s]{1}value=\")([a-z@A-Z\\.]*)(\"><br></td>)", "email1"));
	    pageParts.add(new PagePart("(<textarea[\\s]{1}name=ctf_n[\\s]{1}id=ctf_n[\\s]{1}cols=[0-9]{1,2}[\\s]{1}rows=[0-9]{1,2}>)([^<]*)(</textarea>)", "note"));
	    pageParts.add(new PagePart("(<input[\\s]size=[0-9]{2}[\\s]name=\"ctsf_00_00_e\"[\\s]value=\")([a-zA-Z@\\.]*)(\">)", "email2"));
	    pageParts.add(new PagePart("(<input[\\s]size=[0-9]{2}[\\s]name=\"ctsf_00_01_p\"[\\s]value=\")([+0-9\\s]*)(\">)", "telephone_home1"));
	    pageParts.add(new PagePart("(<input[\\s]size=[0-9]{2}[\\s]name=\"ctsf_00_02_m\"[\\s]value=\")([+0-9\\s]*)(\">)", "cellular_telephone2"));
	    pageParts.add(new PagePart("(<input[\\s]size=[0-9]{2}[\\s]name=\"ctsf_00_03_i\"[\\s]value=\")([a-z\\.@A-Z]*)(\">)", "instant_messenger2"));    
	    pageParts.add(new PagePart("(<input[\\s]size=[0-9]{2}[\\s]name=\"ctsf_01_00_e\"[\\s]value=\")([a-zA-Z@\\.]*)(\">)", "email3"));
	    pageParts.add(new PagePart("(<input[\\s]size=[0-9]{2}[\\s]name=\"ctsf_01_01_p\"[\\s]value=\")([+0-9\\s]*)(\">)", "telephone_business1"));
	    pageParts.add(new PagePart("(<input[\\s]size=[0-9]{2}[\\s]name=\"ctsf_01_02_m\"[\\s]value=\")([+0-9\\s]*)(\">)", "cellular_telephone1"));
	    pageParts.add(new PagePart("(<input[\\s]size=[0-9]{2}[\\s]name=\"ctsf_01_03_i\"[\\s]value=\")([a-z\\.@A-Z]*)(\">)", "instant_messenger1"));
	    PagePartSequence sequence = new PagePartSequence(pageParts, page);
	    HashMap<String, String> map = sequence.retrieveInformation();
	    
	    assertEquals("Kevin Ortiz", map.get("display_name"));
	    assertEquals("topshooter@gmail.com", map.get("email1"));
	    assertEquals("some personal note", map.get("note"));
	    assertEquals("personal@mail.com", map.get("email2"));
	    assertEquals("+49 221 987456", map.get("telephone_home1"));
	    assertEquals("+49 171 234765", map.get("cellular_telephone2"));
	    assertEquals("personal@im.com", map.get("instant_messenger2"));
	    assertEquals("business@mail.com", map.get("email3"));	    
	    assertEquals("+49 221 123000", map.get("telephone_business1"));
	    assertEquals("+49 171 123000", map.get("cellular_telephone1"));
	    assertEquals("business@im.com", map.get("instant_messenger1"));
	    	    
	}
}
