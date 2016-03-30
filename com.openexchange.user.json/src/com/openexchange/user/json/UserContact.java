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

package com.openexchange.user.json;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.contacts.json.mapping.ContactMapper;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.ldap.User;
import com.openexchange.session.Session;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.user.json.comparator.Comparators;
import com.openexchange.user.json.field.UserField;
import com.openexchange.user.json.mapping.UserMapper;

/**
 * {@link UserContact} - Wraps {@link User}s and {@link Contact}s together for
 * serialization.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class UserContact {

	public static final String ALL_ATTRIBUTES = "*";

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(UserContact.class);

	private User user;
	private Contact contact;

    /**
     * Initializes a new {@link UserContact}.
     *
     * @param contact the contact
     * @param user the user
     */
    public UserContact(Contact contact, User user) {
        super();
        this.contact = contact;
        this.user = user;
    }

    /**
     * Initializes a new {@link UserContact} based on a the supplied user. Contact information is added in a virtual way, based on the
     * basic properties also available in the user reference.
     *
     * @param user The user
     */
    public UserContact(User user) {
        this(getVirtualContact(user), user);
    }

    /**
     * Gets the contact
     *
     * @return The contact
     */
    public Contact getContact() {
        return contact;
    }

    /**
     * Gets the user
     *
     * @return The user
     */
    public User getUser() {
        return user;
    }

    /**
     * Sets the user contact
     *
     * @param contact The contact to set
     */
    public void setContact(Contact contact) {
        this.contact = contact;
    }

    /**
     * Sets the user
     *
     * @param user The user to set
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Serializes the user- and contact data into a JSON object.
     *
     * @param contactFields the contact fields to consider
     * @param userFields the user fields to consider
     * @param timeZoneID the client timezone ID
     * @return the serialized user contact
     * @throws OXException
     */
    private JSONObject serialize(Session session, ContactField[] contactFields, UserField[] userFields, String timeZoneID) throws OXException {
    	final JSONObject jsonObject = new JSONObject();
    	try {
        	ContactMapper.getInstance().serialize(contact, jsonObject, contactFields, timeZoneID, session);
        	UserMapper.getInstance().serialize(user, jsonObject, userFields, timeZoneID, session);
    	} catch (final JSONException e) {
    		throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
		}
    	return jsonObject;
    }

    /**
     * Serializes the user- and contact data into a JSON object.
     *
     * @param timeZoneID the client timezone ID
     * @return the serialized user contact
     * @throws OXException
     */
    public JSONObject serialize(String timeZoneID, Session session) throws OXException {
    	JSONObject jsonObject = null;
    	try {
    		// always add NUMBER_OF_IMAGES to contact result (bug #13960)
    		ContactField[] contactFields = ContactMapper.getInstance().getAssignedFields(contact, ContactField.NUMBER_OF_IMAGES);
    		jsonObject = ContactMapper.getInstance().serialize(contact, contactFields, timeZoneID, session);
    		UserField[] userFields = UserMapper.getInstance().getAssignedFields(user);
        	UserMapper.getInstance().serialize(user, jsonObject, userFields, timeZoneID);
    	} catch (final JSONException e) {
    		throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
		} catch (final RuntimeException x) {
			System.out.print(x.getMessage());
		}
    	return jsonObject;
    }

    /**
     * Serializes the user- and contact data into a JSON array.
     *
     * @param columnIDs the column identifiers of the corresponding fields to serialize
     * @param timeZoneID the client timezone ID
     * @param attributeParameters the attribute parameters to append to the array
     * @return the serialized user contact
     * @throws OXException
     */
    public JSONArray serialize(Session session, int[] columnIDs, String timeZoneID, Map<String, List<String>> attributeParameters) throws OXException {
		UserField[] userFields = UserMapper.getInstance().getFields(columnIDs);
		ContactField[] contactFields = ContactMapper.getInstance().getFields(columnIDs);
		JSONObject temp = this.serialize(session, contactFields, userFields, timeZoneID);
		JSONArray jsonArray = new JSONArray(columnIDs.length);

		// Iterate column identifiers
		for (int columnID : columnIDs) {
			UserField userField = UserMapper.getInstance().getMappedField(columnID);
			if (null == userField) {
    			ContactField contactField = ContactMapper.getInstance().getMappedField(columnID);
    			if (null != contactField) {
    				final String ajaxName = ContactMapper.getInstance().get(contactField).getAjaxName();
    				jsonArray.put(temp.opt(ajaxName));
    			} else {
    			    LOG.warn("Unknown field: {}", Integer.valueOf(columnID), new Throwable());
    			    jsonArray.put(JSONObject.NULL);
    			}
			} else {
				final String ajaxName = UserMapper.getInstance().get(userField).getAjaxName();
				jsonArray.put(temp.opt(ajaxName));
			}
		}

		// Append attributes
		if (null != attributeParameters && 0 < attributeParameters.size()) {
			try {
				for (Map.Entry<String, List<String>> entry : attributeParameters.entrySet()) {
					appendUserAttribute(jsonArray, entry.getKey(), entry.getValue());
				}
			} catch (JSONException e) {
	    		throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
			}
		}
    	return jsonArray;
    }

    private void appendUserAttribute(final JSONArray jsonArray, final String attributePrefix, final List<String> attributes) throws JSONException {
        if (null != attributes && 0 < attributes.size()) {
        	final Map<String, Set<String>> userAttributes = user.getAttributes();
            if (1 == attributes.size() && ALL_ATTRIBUTES.equals(attributes.get(0))) {
                /*
                 * Wildcard
                 */
                if (null == userAttributes || userAttributes.isEmpty()) {
                    jsonArray.put(JSONObject.NULL);
                } else {
                	final JSONObject jsonObject = new JSONObject();
                	for (Entry<String, Set<String>> entry : userAttributes.entrySet()) {
                		if (entry.getKey().startsWith(attributePrefix)) {
                			jsonObject.put(entry.getKey(), toJSONValue(entry.getValue()));
                		}
					}
                	jsonArray.put(jsonObject);
                }
            } else {
                /*
                 * Non wildcard
                 */
            	for (final String attribute : attributes) {
                    if (null == userAttributes || userAttributes.isEmpty()) {
                        jsonArray.put(JSONObject.NULL);
                    } else {
                		final String attributeName = String.format("%s/%s", attributePrefix, attribute);
                		jsonArray.put(toJSONValue(userAttributes.get(attributeName)));
                    }
				}
            }
        }
    }

    private static Object toJSONValue(final Set<String> values) {
        if (null == values || values.isEmpty()) {
            return JSONObject.NULL;
        }
        if (values.size() > 1) {
            final JSONArray ja = new JSONArray();
            for (final String value : values) {
                ja.put(value);
            }
            return ja;
        }
        return values.iterator().next();
    }

    /**
     * Creates a "virtual" contact for the supplied user, taking over the most basic properties.
     *
     * @param user The user to generate the virtual contact for
     * @return The virtual contact
     */
    private static Contact getVirtualContact(User user) {
        Contact contact = new Contact();
        contact.setInternalUserId(user.getId());
        contact.setDisplayName(user.getDisplayName());
        contact.setSurName(user.getSurname());
        contact.setGivenName(user.getGivenName());
        contact.setEmail1(user.getMail());
        return contact;
    }

    /**
     * Gets the appropriate {@link Comparator} for given {@link UserField user field}.
     *
     * @param userField The user field
     * @param sessionLocale The session user's locale
     * @param descending <code>true</code> to sort in descending order; otherwise <code>false</code>
     * @return The appropriate {@link Comparator} for given {@link UserField user field} or <code>null</code>
     */
    public static Comparator<UserContact> getComparator(final UserField userField, final Locale sessionLocale, final boolean descending) {
    	final Comparator<User> userComparator = Comparators.getComparator(userField, sessionLocale, descending);
    	return new Comparator<UserContact>() {

			@Override
			public int compare(UserContact o1, UserContact o2) {
				return userComparator.compare(o1.user, o2.user);
			}
		};
    }
}
