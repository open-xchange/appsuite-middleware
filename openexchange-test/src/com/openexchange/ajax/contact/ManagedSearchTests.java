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

package com.openexchange.ajax.contact;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.search.ContactSearchObject;
import com.openexchange.groupware.search.Order;

/**
 *
 * @author tobiasp
 *
 */
public class ManagedSearchTests extends AbstractManagedContactTest {

	public List<String> sinographs = Arrays.asList( "\u963f", "\u6ce2","\u6b21","\u7684","\u9e45","\u5bcc","\u54e5","\u6cb3","\u6d01","\u79d1","\u4e86","\u4e48","\u5462","\u54e6","\u6279","\u4e03","\u5982","\u56db","\u8e22","\u5c4b","\u897f","\u8863","\u5b50");

	public ManagedSearchTests(String name) {
		super(name);
	}

	public void testGuiLikeSearch(){
		List<ContactSearchObject> searches = new LinkedList<ContactSearchObject>();

		for(String name: sinographs){
			//create
			Contact tmp = generateContact();
			tmp.setSurName(name);
			manager.newAction(tmp);

			//prepare search
			ContactSearchObject search = new ContactSearchObject();
			search.setFolder(folderID);
			search.setGivenName(name);
			search.setSurname(name);
			search.setDisplayName(name);
			search.setEmail1(name);
			search.setEmail2(name);
			search.setEmail3(name);
			search.setCatgories(name);
			search.setYomiFirstname(name);
			search.setYomiLastName(name);
			search.setOrSearch(true);
			searches.add(search);
		}
		for(int i = 0; i < sinographs.size(); i++){
			Contact[] results= manager.searchAction(searches.get(i));

			assertEquals("#"+i+" Should find one contact", 1, results.length);
			assertEquals("#"+i+" Should find the right contact", sinographs.get(i), results[0].getSurName());
		}
	}

	public void testSearchPattern(){
		for(String name: sinographs){
			Contact tmp = generateContact();
			tmp.setSurName(name);
			manager.newAction(tmp);
		}
		Contact[] contacts= manager.searchAction("*", folderID, ContactField.SUR_NAME.getNumber(), Order.ASCENDING, "gb2312", Contact.ALL_COLUMNS);

		for(int i = 0; i < sinographs.size(); i++){
			String name = contacts[i].getSurName();
			assertEquals("#"+i+" Should have the right order", sinographs.get(i), name);
		}
	}

}
