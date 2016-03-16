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

package com.openexchange.test;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.json.JSONException;
import org.xml.sax.SAXException;
import com.openexchange.ajax.contact.action.DoAssociationRequest;
import com.openexchange.ajax.contact.action.DoAssociationResponse;
import com.openexchange.ajax.contact.action.DoSeparationRequest;
import com.openexchange.ajax.contact.action.DoSeparationResponse;
import com.openexchange.ajax.contact.action.GetAssociatedContactsRequest;
import com.openexchange.ajax.contact.action.GetAssociatedContactsResponse;
import com.openexchange.ajax.contact.action.GetAssociationRequest;
import com.openexchange.ajax.contact.action.GetAssociationResponse;
import com.openexchange.ajax.contact.action.GetContactByUIDRequest;
import com.openexchange.ajax.contact.action.GetResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.ContactUnificationState;
import com.openexchange.groupware.container.Contact;


/**
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class AggregatingContactTestManager extends ContactTestManager {

    private final Set<UUID> createdAssociations = new HashSet<UUID>();

    public AggregatingContactTestManager(AJAXClient client) throws OXException, IOException, SAXException, JSONException {
        super(client);
    }

    public ContactUnificationState getAssociationBetween(Contact contributor, Contact aggregator) throws IOException, SAXException, JSONException, OXException {
        GetAssociationResponse response = getClient().execute(new GetAssociationRequest(contributor, aggregator));

        doJanitorialTasks(response);
        return response.getState();
    }

    public void associateTwoContacts(Contact aggregator, Contact contributor) {
        try {
            DoAssociationResponse doAssociationResponse = getClient().execute(new DoAssociationRequest(contributor, aggregator));

            Contact temp;
            temp = getAction(aggregator);
            aggregator.setUserField20(temp.getUserField20());
            temp = getAction(contributor);
            contributor.setUserField20(temp.getUserField20());

            doJanitorialTasks(doAssociationResponse);
        } catch(Exception e){
            doExceptionHandling(e, "DoAssociationRequest");
        }
    }

    public void separateTwoContacts(Contact aggregator, Contact contributor) {
        try {
            DoSeparationResponse doSeparationResponse = getClient().execute(new DoSeparationRequest(contributor, aggregator));
            aggregator = getAction(aggregator);
            contributor = getAction(contributor);
            doJanitorialTasks(doSeparationResponse);

        } catch(Exception e){
            doExceptionHandling(e, "DoSeparationRequest");
        }
    }

    public Contact getContactByUID(UUID uid){
        try {
            GetResponse response = getClient().execute(new GetContactByUIDRequest(uid, timeZone));
            doJanitorialTasks(response);
            return response.getContact();
        } catch(Exception e){
            doExceptionHandling(e, "GetContactByUIDRequest");
        }
        return null;
    }

    public List<UUID> getAssociatedContactsByUID(UUID uid){
        try {
            GetAssociatedContactsResponse response = getClient().execute(new GetAssociatedContactsRequest(uid, timeZone));
            doJanitorialTasks(response);
            return response.getUUIDs();
        } catch(Exception e){
            doExceptionHandling(e, "GetAssociatedContactsRequest");
        }
        return null;
    }

    public List<UUID> getAssociatedContacts(Contact c){
        try {
            GetAssociatedContactsResponse response = getClient().execute(new GetAssociatedContactsRequest(c, timeZone));
            doJanitorialTasks(response);
            return response.getUUIDs();
        } catch(Exception e){
            doExceptionHandling(e, "GetAssociatedContactsRequest");
        }
        return null;
    }

}
