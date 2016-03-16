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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.anonymizer.AnonymizerService;
import com.openexchange.ajax.anonymizer.Anonymizers;
import com.openexchange.ajax.anonymizer.Module;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.Converter;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.ldap.User;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.json.actions.GetAction;

/**
 * {@link UserContactResultConverter} - JSON result converter for user contacts.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class UserContactResultConverter implements ResultConverter {

    private static final Set<String> EXPECTED_NAMES = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
    		AJAXServlet.PARAMETER_COLUMNS, AJAXServlet.PARAMETER_SORT, AJAXServlet.PARAMETER_ORDER, AJAXServlet.LEFT_HAND_LIMIT,
    		AJAXServlet.RIGHT_HAND_LIMIT, AJAXServlet.PARAMETER_TIMEZONE, AJAXServlet.PARAMETER_SESSION, AJAXServlet.PARAMETER_ACTION)));

	@Override
	public String getInputFormat() {
		return "usercontact";
	}

	@Override
	public String getOutputFormat() {
		return "json";
	}

	@Override
	public Quality getQuality() {
		return Quality.GOOD;
	}

	@Override
	public void convert(AJAXRequestData requestData, AJAXRequestResult result, ServerSession session, Converter converter) throws OXException {
    	/*
    	 * determine timezone
    	 */
        String timeZoneID = requestData.getParameter("timezone");
        if (null == timeZoneID) {
        	timeZoneID = session.getUser().getTimeZone();
        }
        /*
         * get requested column IDs and additional user attributes
         */
        int[] columnIDs = Utility.parseOptionalIntArrayParameter(AJAXServlet.PARAMETER_COLUMNS, requestData);
        Map<String, List<String>> attributeParameters = Utility.getAttributeParameters(EXPECTED_NAMES, requestData);
		/*
		 * convert current result object
		 */
        Object resultObject = result.getResultObject();
        if (null == resultObject) {
			resultObject = JSONObject.NULL;
        } else if (GetAction.ACTION.equalsIgnoreCase(requestData.getAction())) {
			/*
			 * convert single user contact
			 */
			UserContact userContact = (UserContact)resultObject;

			if (Anonymizers.isGuest(session) && null != userContact.getUser() && session.getUserId() != userContact.getUser().getId()) {
                userContact.setContact(Anonymizers.optAnonymize(userContact.getContact(), Module.CONTACT, session));
                userContact.setUser(Anonymizers.optAnonymize(userContact.getUser(), Module.USER, session));
            }

			resultObject = userContact.serialize(timeZoneID, session);
        } else {
            /*
             * convert multiple user contacts into array
             */
            List<UserContact> userContacts = (List<UserContact>) resultObject;
            JSONArray jArray = new JSONArray(userContacts.size());

            if (Anonymizers.isGuest(session)) {
                AnonymizerService<Contact> contactAnonymizer = Anonymizers.optAnonymizerFor(Module.CONTACT);
                AnonymizerService<User> userAnonymizer = Anonymizers.optAnonymizerFor(Module.USER);

                for (UserContact userContact : userContacts) {
                    if (null != userContact.getUser() && session.getUserId() != userContact.getUser().getId()) {
                        userContact.setContact(contactAnonymizer.anonymize(userContact.getContact(), session));
                        userContact.setUser(userAnonymizer.anonymize(userContact.getUser(), session));
                    }
                    jArray.put(userContact.serialize(session, columnIDs, timeZoneID, attributeParameters));
                }
            } else {
                for (UserContact userContact : userContacts) {
                    jArray.put(userContact.serialize(session, columnIDs, timeZoneID, attributeParameters));
                }
            }

            resultObject = jArray;
        }
        result.setResultObject(resultObject, "json");
	}

}
