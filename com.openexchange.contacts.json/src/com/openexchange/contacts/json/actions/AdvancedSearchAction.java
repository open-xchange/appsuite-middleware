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
import java.util.List;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.openexchange.ajax.parser.SearchTermParser;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.contacts.json.ContactRequest;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.ContactSearchMultiplexer;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.search.Order;
import com.openexchange.search.SearchTerm;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link AdvancedSearchAction}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
@Action(method = RequestMethod.PUT, name = "advanchedSearch", description = "Search contacts by filter (since 6.20).", parameters = {
    @Parameter(name = "session", description = "A session ID previously obtained from the login module."),
    @Parameter(name = "columns", description = "The requested fields."),
    @Parameter(name = "sort", optional=true, description = "The identifier of a column which determines the sort order of the response. If this parameter is specified, then the parameter order must be also specified."),
    @Parameter(name = "order", optional=true, description = "\"asc\" if the response entires should be sorted in the ascending order, \"desc\" if the response entries should be sorted in the descending order. If this parameter is specified, then the parameter sort must be also specified."),
    @Parameter(name = "collation", description = "(preliminary, since 6.20) - allows you to specify a collation to sort the contacts by. As of 6.20, only supports \"gbk\" and \"gb2312\", not needed for other languages. Parameter sort should be set for this to work.")
}, requestBody = "An Object as described in Search Filter.",
responseDescription = "An array with contact data. Each array element describes one contact and is itself an array. The elements of each array contain the information specified by the corresponding identifiers in the columns parameter.")
public class AdvancedSearchAction extends ContactAction {

    /**
     * Initializes a new {@link AdvancedSearchAction}.
     * @param serviceLookup
     */
    public AdvancedSearchAction(final ServiceLookup serviceLookup) {
        super(serviceLookup);
    }

    @Override
    protected AJAXRequestResult perform(final ContactRequest req) throws OXException {
        final ServerSession session = req.getSession();
        final int[] columns = req.getColumns();
        final int sort = req.getSort();
        final Order order = req.getOrder();
        final String collation = req.getCollation();
        Date lastModified = null;
        Date timestamp = new Date(0);
        final JSONObject json = (JSONObject) req.getData();
        final TimeZone timeZone = req.getTimeZone();

        JSONArray filterContent;
        SearchIterator<Contact> it = null;
        final List<Contact> contacts = new ArrayList<Contact>();
        try {
            filterContent = json.getJSONArray("filter");
            final SearchTerm<?> searchTerm = SearchTermParser.parse(filterContent);

            final ContactSearchMultiplexer multiplexer = new ContactSearchMultiplexer(getContactInterfaceDiscoveryService());
            it = multiplexer.extendedSearch(session, searchTerm, sort, order, collation, columns);
            while (it.hasNext()) {
                final Contact contact = it.next();
                lastModified = contact.getLastModified();

                // Correct last modified and creation date with users timezone
                contact.setLastModified(getCorrectedTime(contact.getLastModified(), timeZone));
                contact.setCreationDate(getCorrectedTime(contact.getCreationDate(), timeZone));
                contacts.add(contact);

                if (lastModified != null && timestamp.before(lastModified)) {
                    timestamp = lastModified;
                }
            }
        } catch (final JSONException e) {
            throw OXJSONExceptionCodes.JSON_READ_ERROR.create(e);
        } finally {
            if (it != null) {
                it.close();
            }
        }

        return new AJAXRequestResult(contacts, lastModified, "contact");
    }

    @Override
    protected AJAXRequestResult perform2(ContactRequest request) throws OXException {
        List<Contact> contacts = new ArrayList<Contact>();
        Date lastModified = new Date(0);
        SearchIterator<Contact> searchIterator = null;
        try {
            searchIterator = getContactService().searchContacts(request.getSession(), request.getSearchFilter(), 
            		request.getFields(), request.getSortOptions());
            while (searchIterator.hasNext()) {
                Contact contact = searchIterator.next();
                lastModified = getLatestModified(lastModified, contact);
                contacts.add(contact);
            }
        } finally {
        	close(searchIterator);
        }
        request.sortInternalIfNeeded(contacts);
        return new AJAXRequestResult(contacts, lastModified, "contact");
    }
    
}
