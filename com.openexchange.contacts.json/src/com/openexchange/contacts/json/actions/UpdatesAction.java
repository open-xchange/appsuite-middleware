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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.contacts.json.actions;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.contact.ContactService;
import com.openexchange.contacts.json.ContactRequest;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.iterator.SearchIterator;


/**
 * {@link UpdatesAction}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
@Action(method = RequestMethod.GET, name = "updates", description = "Get updated contacts.", parameters = {
    @Parameter(name = "session", description = "A session ID previously obtained from the login module."),
    @Parameter(name = "folder", description = "Object ID of the folder, whose contents are queried."),
    @Parameter(name = "columns", description = "A comma-separated list of columns to return. Each column is specified by a numeric column identifier. Column identifiers for contacts are defined in Common object data and Detailed contact data."),
    @Parameter(name = "sort", optional=true, description = "The identifier of a column which determines the sort order of the response. If this parameter is specified, then the parameter order must be also specified."),
    @Parameter(name = "order", optional=true, description = "\"asc\" if the response entires should be sorted in the ascending order, \"desc\" if the response entries should be sorted in the descending order. If this parameter is specified, then the parameter sort must be also specified."),
    @Parameter(name = "timestamp", description = "Timestamp of the last update of the requested contacts."),
    @Parameter(name = "ignore", description = "(mandatory - should be set to \"deleted\") (deprecated) - Which kinds of updates should be ignored. Currently, the only valid value - \"deleted\" - causes deleted object IDs not to be returned.")
}, responseDescription = "Response with timestamp: An array with new, modified and deleted contacts. New and modified contacts are represented by arrays. The elements of each array contain the information specified by the corresponding identifiers in the columns parameter. Deleted contacts (should the ignore parameter be ever implemented) would be identified by their object IDs as plain strings, without being part of a nested array.")
public class UpdatesAction extends ContactAction {

    /**
     * Initializes a new {@link UpdatesAction}.
     * @param serviceLookup
     */
    public UpdatesAction(ServiceLookup serviceLookup) {
        super(serviceLookup);
    }

    @Override
    protected AJAXRequestResult perform(ContactRequest request) throws OXException {
        Date lastModified = new Date(0);
        ContactService contactService = getContactService();
        Date since = new Date(request.getTimestamp());
        SearchIterator<Contact> searchIterator = null;
        /*
         * add modified contacts
         */
        List<Contact> modifiedContacts = new ArrayList<Contact>(); 
        try {
            searchIterator = contactService.getModifiedContacts(request.getSession(), request.getFolderID(), since, request.getFields());
            while (searchIterator.hasNext()) {
                Contact contact = searchIterator.next();
                lastModified = getLatestModified(lastModified, contact);
                modifiedContacts.add(contact);
            }
        } finally {
            close(searchIterator);
        }
        /*
         * add deleted contacts
         */
        List<Contact> deletedContacts = new ArrayList<Contact>(); 
        if (false == "deleted".equals(request.getIgnore())) {
	        try {
	            searchIterator = contactService.getDeletedContacts(request.getSession(), request.getFolderID(), since, request.getFields());
	            while (searchIterator.hasNext()) {
	                Contact contact = searchIterator.next();
	                lastModified = getLatestModified(lastModified, contact);
	                deletedContacts.add(contact);
	            }
	        } finally {
	            close(searchIterator);
	        }
        }
        Map<String, List<Contact>> responseMap = new HashMap<String, List<Contact>>(2);
        responseMap.put("modified", modifiedContacts);
        responseMap.put("deleted", deletedContacts);
        return new AJAXRequestResult(responseMap, lastModified, "contact");
    }
}
