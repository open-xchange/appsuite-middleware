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

package com.openexchange.user.json.actions;

import static com.openexchange.user.json.Utility.checkForRequiredField;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import org.json.JSONArray;

import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.fields.OrderFields;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.api2.ContactInterfaceFactory;
import com.openexchange.contact.ContactService;
import com.openexchange.contact.SortOptions;
import com.openexchange.contact.SortOrder;
import com.openexchange.contacts.json.mapping.ContactMapper;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.Type;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.ContactInterface;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.search.Order;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.UserService;
import com.openexchange.user.json.Constants;
import com.openexchange.user.json.UserContact;
import com.openexchange.user.json.comparator.Comparators;
import com.openexchange.user.json.field.UserField;
import com.openexchange.user.json.mapping.UserMapper;
import com.openexchange.user.json.services.ServiceRegistry;
import com.openexchange.user.json.writer.UserWriter;

/**
 * {@link AllAction} - Maps the action to an <tt>all</tt> action.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
@Action(method = RequestMethod.GET, name = "all", description = "Get all users.", parameters = { 
		@Parameter(name = "session", description = "A session ID previously obtained from the login module."),
		@Parameter(name = "columns", description = "A comma-separated list of columns to return. Each column is specified by a numeric column identifier. Column identifiers for users are defined in Common object data, Detailed contact data and Detailed user data."),
		@Parameter(name = "sort", optional = true, type = Type.NUMBER, description = "The identifier of a column which determines the sort order of the response. If this parameter is specified, then the parameter order must be also specified."),
		@Parameter(name = "order", optional = true, description = "\"asc\" if the response entires should be sorted in the ascending order, \"desc\" if the response entries should be sorted in the descending order. If this parameter is specified, then the parameter sort must be also specified.")
}, responseDescription = "Response with timestamp: An array with user data. Each array element describes one user and is itself an array. The elements of each array contain the information specified by the corresponding identifiers in the columns parameter.")
public final class AllAction extends AbstractUserAction {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(AllAction.class));

    /**
     * The <tt>all</tt> action string.
     */
    public static final String ACTION = AJAXServlet.ACTION_ALL;

    /**
     * Initializes a new {@link AllAction}.
     */
    public AllAction() {
        super();
    }

    private static final Set<String> EXPECTED_NAMES =
        Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
            AJAXServlet.PARAMETER_COLUMNS,
            AJAXServlet.PARAMETER_SORT,
            AJAXServlet.PARAMETER_ORDER,
            AJAXServlet.LEFT_HAND_LIMIT,
            AJAXServlet.RIGHT_HAND_LIMIT,
            AJAXServlet.PARAMETER_TIMEZONE,
            AJAXServlet.PARAMETER_SESSION,
            AJAXServlet.PARAMETER_ACTION)));

    @Override
    public AJAXRequestResult perform(final AJAXRequestData request, final ServerSession session) throws OXException {
        try {
            /*
             * Parse parameters
             */
            final int[] columns = parseIntArrayParameter(AJAXServlet.PARAMETER_COLUMNS, request);
            final int orderBy = parseIntParameter(AJAXServlet.PARAMETER_SORT, request);
            final Order order = OrderFields.parse(request.getParameter(AJAXServlet.PARAMETER_ORDER));
            final int leftHandLimit = parseIntParameter(AJAXServlet.LEFT_HAND_LIMIT, request);
            final int rightHandLimit = parseIntParameter(AJAXServlet.RIGHT_HAND_LIMIT, request);
            /*
             * Determine sort options
             */
            final int lhl = leftHandLimit < 0 ? 0 : leftHandLimit;
            final int rhl = rightHandLimit <= 0 ? 50000 : rightHandLimit;
            final SortOptions sortOptions = new SortOptions(lhl,  rhl - lhl);
            final UserField orderByUserField = UserMapper.getInstance().getMappedField(orderBy);
        	if (null == orderByUserField) {
        		final ContactField orderByContactField = ContactMapper.getInstance().getMappedField(orderBy);
        		if (null != orderByContactField) {        			
            		// Sort field is a contact field: pass as it is
        			sortOptions.setOrderBy(new SortOrder[] { SortOptions.Order(orderByContactField, order) });
        		}
        	}
        	/*
        	 * Get contacts and users
        	 */        	
        	Date lastModified = new Date(0);
        	final List<UserContact> userContacts = new ArrayList<UserContact>();
            final ContactService contactService = ServiceRegistry.getInstance().getService(ContactService.class);
            final ContactField[] contactFields = ContactMapper.getInstance().getFields(columns, 
            		ContactField.INTERNAL_USERID, ContactField.LAST_MODIFIED);            
            final UserService userService = ServiceRegistry.getInstance().getService(UserService.class, true);
            SearchIterator<Contact> searchIterator = null;
            try {
            	searchIterator = contactService.getAllUsers(session, contactFields, sortOptions);
            	/*
            	 * Process results
            	 */
                while (searchIterator.hasNext()) {
                	final Contact contact = searchIterator.next();
                	/*
                	 * Check last modified
                	 */
                	if (contact.getLastModified().after(lastModified)) {
                		lastModified = contact.getLastModified();
                	}                	
                	/*
                	 * Get corresponding user
                	 */
                	final User user = userService.getUser(contact.getInternalUserId(), session.getContext());
                	userContacts.add(new UserContact(contact, user));
                }
            } finally {
            	if (null != searchIterator) {
            		searchIterator.close();
            	}
            }
            /*
             * Sort by users if a user field was denoted by sort field
             */
            if (1 < userContacts.size() && null != orderByUserField) {
        		Collections.sort(userContacts, UserContact.getComparator(
        				orderByUserField, session.getUser().getLocale(), Order.DESCENDING.equals(order)));
            }
            /*
             * Return appropriate result
             */
            return new AJAXRequestResult(userContacts, lastModified, "usercontact");
        } catch (final OXException e) {
            throw new OXException(e);
        }
    }
    
    public AJAXRequestResult performOLD(final AJAXRequestData request, final ServerSession session) throws OXException {
        try {
            /*
             * Parse parameters
             */
            final int[] columns = parseIntArrayParameter(AJAXServlet.PARAMETER_COLUMNS, request);
            final int orderBy = parseIntParameter(AJAXServlet.PARAMETER_SORT, request);
            final Order order = OrderFields.parse(request.getParameter(AJAXServlet.PARAMETER_ORDER));

            final int leftHandLimit = parseIntParameter(AJAXServlet.LEFT_HAND_LIMIT, request);
            final int rightHandLimit = parseIntParameter(AJAXServlet.RIGHT_HAND_LIMIT, request);

            final String timeZoneId = request.getParameter(AJAXServlet.PARAMETER_TIMEZONE);
            /*
             * Get remaining parameters
             */
            final Map<String, List<String>> attributeParameters = getAttributeParameters(EXPECTED_NAMES, request);
            /*
             * Get services
             */
            final UserService userService = ServiceRegistry.getInstance().getService(UserService.class, true);
            final ContactInterface contactInterface =
                ServiceRegistry.getInstance().getService(ContactInterfaceFactory.class, true).create(
                    Constants.USER_ADDRESS_BOOK_FOLDER_ID,
                    session);
            /*
             * Get all users/contacts
             */
            final User[] users;
            final Contact[] contacts;
            /*
             * Order and fill contact array
             */
            if (-1 != orderBy) {
                /*
                 * Order them
                 */
                final UserField orderField = UserField.getUserOnlyField(orderBy);
                /*
                 * Ensure UserField.INTERNAL_USERID is requested to properly load corresponding users
                 */
                final int[] checkedCols = checkForRequiredField(columns, UserField.INTERNAL_USERID.getColumn());
                if (null == orderField) {
                    /*
                     * Order by contact field
                     */
                    final int lhl = leftHandLimit < 0 ? 0 : leftHandLimit;
                    final int rhl = rightHandLimit <= 0 ? 50000 : rightHandLimit;
                    final SearchIterator<Contact> it;
                    it =
                        contactInterface.getContactsInFolder(
                            Constants.USER_ADDRESS_BOOK_FOLDER_ID,
                            lhl,
                            rhl,
                            orderBy,
                            order,
                            null,
                            checkedCols);
                    try {
                        final List<Contact> contactList = new ArrayList<Contact>(128);
                        while (it.hasNext()) {
                            contactList.add(it.next());
                        }
                        contacts = contactList.toArray(new Contact[contactList.size()]);
                        users = new User[contacts.length];
                        for (int j = 0; j < users.length; j++) {
                            final int userId = contacts[j].getInternalUserId();
                            if (userId > 0) {
                                users[j] = userService.getUser(userId, session.getContext());
                            } else {
                                LOG.error("Missing internal user ID in contact " + contacts[j].getObjectID() + ": " + contacts[j].getDisplayName());
                            }
                        }
                    } finally {
                        try {
                            it.close();
                        } catch (final Exception e) {
                            LOG.error(e.getMessage(), e);
                        }
                    }
                } else {
                    // Order by user field
                    final List<User> allUsers = new ArrayList<User>();
                    for (final User user : userService.getUser(session.getContext())) {
                        allUsers.add(user);
                    }
                    Collections.sort(allUsers, Comparators.getComparator(
                        orderField,
                        session.getUser().getLocale(),
                        Order.DESCENDING.equals(order)));
                    final int lhl = leftHandLimit < 0 ? 0 : leftHandLimit;
                    int rhl = rightHandLimit <= 0 ? 50000 : rightHandLimit;
                    if (rhl - lhl >= allUsers.size()) {
                        rhl = allUsers.size();
                    }
                    users = allUsers.subList(lhl, rhl).toArray(new User[rhl - lhl]);
                    final int[] userIds = new int[users.length];
                    for (int i = 0; i < users.length; i++) {
                        userIds[i] = users[i].getId();
                    }
                    contacts = contactInterface.getUsersById(userIds, false);
                }
            } else {
                // No sorting required
                final List<User> allUsers = new ArrayList<User>();
                for (final User user : userService.getUser(session.getContext())) {
                    allUsers.add(user);
                }
                final int lhl = leftHandLimit < 0 ? 0 : leftHandLimit;
                int rhl = rightHandLimit <= 0 ? 50000 : rightHandLimit;
                if (rhl - lhl >= allUsers.size()) {
                    rhl = allUsers.size();
                }
                users = allUsers.subList(lhl, rhl).toArray(new User[rhl - lhl]);
                final int[] userIds = new int[users.length];
                for (int i = 0; i < users.length; i++) {
                    userIds[i] = users[i].getId();
                }
                contacts = contactInterface.getUsersById(userIds, false);
            }
            /*
             * Determine max. last-modified time stamp
             */
            Date lastModified = contacts[0].getLastModified();
            for (int i = 1; i < contacts.length; i++) {
                final Date lm = contacts[i].getLastModified();
                if (lastModified.before(lm)) {
                    lastModified = lm;
                }
            }
            /*
             * Write users as JSON arrays to JSON array
             */
            censor(session, contacts);
            censor(session, users);
            final JSONArray jsonArray = UserWriter.writeMultiple2Array(columns, attributeParameters, users, contacts, timeZoneId);
            /*
             * Return appropriate result
             */
            return new AJAXRequestResult(jsonArray, lastModified);
        } catch (final OXException e) {
            throw new OXException(e);
        }
    }
}
