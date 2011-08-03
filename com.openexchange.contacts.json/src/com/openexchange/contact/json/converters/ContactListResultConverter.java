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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.contact.json.converters;

import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.Converter;
import com.openexchange.contact.json.RequestTools;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.contact.helpers.ContactGetter;
import com.openexchange.groupware.container.Contact;
import com.openexchange.image.ImageService;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link ContactListResultConverter}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class ContactListResultConverter extends JSONResultConverter {    
    
    public ContactListResultConverter(ImageService imageService) {
        super(imageService);
    }

    @Override
    public String getInputFormat() {
        return "contacts";
    }

    @Override
    public void convertResult(AJAXRequestData request, AJAXRequestResult result, ServerSession session, Converter converter) throws OXException {
        Map<String, List<Contact>> contactMap = (Map<String, List<Contact>>) result.getResultObject();
        List<Contact> contacts = null;
        List<Contact> deleted = null;
        if (contactMap.size() == 1) {
            contacts = contactMap.get("contacts");
        } else {
            contacts = contactMap.get("modified");
            deleted = contactMap.get("deleted");
        }
        
        int[] columns = RequestTools.getColumnsAsIntArray(request, "columns");
        
        JSONArray resultArray = new JSONArray();
        for (Contact contact : contacts) {
            JSONArray contactArray = new JSONArray();
            
            ContactGetter cg = new ContactGetter();
            for (int column : columns) {
                ContactField field = ContactField.getByValue(column);
                if (field != null && !field.getAjaxName().isEmpty()) {                
                    Object value = field.doSwitch(cg, contact);
                    if (value == null) {
                        contactArray.put(JSONObject.NULL);
                    } else if (isSpecial(column)) {
                        Object special = convertSpecial(field, contact, cg);
                        if (special == null) {
                            contactArray.put(JSONObject.NULL);                            
                        } else {
                            contactArray.put(special);
                        }                        
                    } else {
                        contactArray.put(value);
                    }
                }
            }
            
            resultArray.put(contactArray);
        }
        
        if (deleted != null) {
            for (Contact contact : deleted) {
                resultArray.put(contact.getObjectID());
            }
        }
        
        result.setResultObject(resultArray, "json");
    }

}
