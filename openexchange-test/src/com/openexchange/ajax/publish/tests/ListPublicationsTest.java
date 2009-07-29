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

package com.openexchange.ajax.publish.tests;

import static com.openexchange.java.Autoboxing.I;
import java.io.IOException;
import java.util.Arrays;
import org.json.JSONArray;
import org.json.JSONException;
import org.xml.sax.SAXException;
import com.openexchange.ajax.publish.actions.ListPublicationsRequest;
import com.openexchange.ajax.publish.actions.ListPublicationsResponse;
import com.openexchange.ajax.publish.actions.NewPublicationRequest;
import com.openexchange.ajax.publish.actions.NewPublicationResponse;
import com.openexchange.groupware.container.Contact;
import com.openexchange.publish.Publication;
import com.openexchange.publish.PublicationException;
import com.openexchange.publish.json.PublicationJSONException;
import com.openexchange.tools.servlet.AjaxException;


/**
 * {@link ListPublicationsTest}
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class ListPublicationsTest extends AbstractPublicationTest {

    public ListPublicationsTest(String name) {
        super(name);
    }

    public void testListExistingPublication() throws AjaxException, IOException, SAXException, JSONException, PublicationException, PublicationJSONException{
        final Contact contact = createDefaultContactFolderWithOneContact();
        String folderID = String.valueOf(contact.getParentFolderID() );
        String module = "contacts";
        
        Publication expected = generatePublication(module, folderID );
        expected.setDisplayName("This will be changed");
        NewPublicationRequest newReq = new NewPublicationRequest(expected);
        NewPublicationResponse newResp = getClient().execute(newReq);
        assertFalse("Precondition: Should be able to create a publication", newResp.hasError());
        expected.setId(newResp.getId());
        
        ListPublicationsRequest listReq = new ListPublicationsRequest(
            Arrays.asList(I(expected.getId())), 
            Arrays.asList("id","entity", "entityModule", "displayName", "target"));
        ListPublicationsResponse listResp = getClient().execute(listReq);
        
        assertEquals("Should only find one element", 1, listResp.getList().size());
        
        JSONArray actual = listResp.getList().get(0);
        assertEquals("Should have same publication ID", expected.getId(), actual.getInt(0));
        assertEquals(expected.getEntityId(), actual.getJSONObject(1).get("folder"));
        assertEquals("Should have same module", expected.getModule(), actual.getString(2));
        assertFalse("Should change display name", expected.getDisplayName().equals(actual.getString(3)));
        assertEquals("Should have same target ID", expected.getTarget().getId(), actual.getString(4));

    }
}
