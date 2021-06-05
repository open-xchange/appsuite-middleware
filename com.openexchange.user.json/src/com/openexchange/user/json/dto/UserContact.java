/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.user.json.dto;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.contacts.json.mapping.ContactMapper;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.session.Session;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.user.User;
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
    	} catch (JSONException e) {
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
    	} catch (JSONException e) {
    		throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
		} catch (RuntimeException x) {
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
        	final Map<String, String> userAttributes = user.getAttributes();
            if (1 == attributes.size() && ALL_ATTRIBUTES.equals(attributes.get(0))) {
                /*
                 * Wildcard
                 */
                if (null == userAttributes || userAttributes.isEmpty()) {
                    jsonArray.put(JSONObject.NULL);
                } else {
                	final JSONObject jsonObject = new JSONObject();
                	for (Entry<String, String> entry : userAttributes.entrySet()) {
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

    private static Object toJSONValue(String value) {
        return null == value ? JSONObject.NULL : value;
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

			@SuppressWarnings("synthetic-access")
            @Override
			public int compare(UserContact o1, UserContact o2) {
				return userComparator.compare(o1.user, o2.user);
			}
		};
    }
}
