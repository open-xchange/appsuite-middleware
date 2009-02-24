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

package com.openexchange.ajax.contact.action;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.CommonUpdatesParser;
import com.openexchange.ajax.framework.CommonUpdatesResponse;
import com.openexchange.ajax.task.actions.TaskUpdatesResponse;
import com.openexchange.groupware.contact.ContactException;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.DistributionListEntryObject;
import com.openexchange.groupware.container.LinkEntryObject;


/**
 * {@link ContactUpdatesParser}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class ContactUpdatesParser extends CommonUpdatesParser<ContactUpdatesResponse> {
    private int[] columns;
    
    protected ContactUpdatesParser(boolean failOnError, int[] columns) {
        super(failOnError, columns);
        this.columns = columns;
        
    }
  
    @Override
    protected ContactUpdatesResponse createResponse(Response response) throws JSONException {
        ContactUpdatesResponse contactUpdatesResponse = super.createResponse(response);
        List<ContactObject> contacts = new ArrayList<ContactObject>();
        JSONArray rows = (JSONArray) response.getData();
        if (rows == null) {
            return contactUpdatesResponse;
        }
        for (int i = 0, size = rows.length(); i < size; i++) {
            JSONArray row = rows.getJSONArray(i);
            ContactObject contact = new ContactObject();
            for (int colIndex = 0; colIndex < columns.length; colIndex++) {
                Object value = row.get(colIndex);
                if (value == JSONObject.NULL) {
                    continue;
                }
                int column = columns[colIndex];
                if (column == ContactObject.LAST_MODIFIED_UTC) {
                    continue;
                }
                value = transform(value, column);
                contact.set(column, value);
            }
            contacts.add(contact);
        }
        contactUpdatesResponse.setContacts(contacts);
        return contactUpdatesResponse;

    }
    
    protected ContactUpdatesResponse instanciateResponse(Response response)  {
        return new ContactUpdatesResponse(response);
    }
    
    private Object transform(Object actual, int column) throws JSONException {
        switch (column) {
            case ContactObject.CREATION_DATE:
            case ContactObject.LAST_MODIFIED:
            case ContactObject.ANNIVERSARY:
            case ContactObject.BIRTHDAY:
                return new Date( ( (Long) actual ).intValue() );
            case ContactObject.IMAGE1:
                return ((String) actual).getBytes();
            case ContactObject.LINKS:
                return transformLinks( (JSONArray) actual );
            case ContactObject.DISTRIBUTIONLIST:
                return transformDistributionList( (JSONArray) actual);

        }
        return actual;
    }
    
    private LinkEntryObject[] transformLinks(JSONArray arr) throws JSONException{
        LinkEntryObject[] results = new LinkEntryObject[arr.length()];
        for (int i = 0; i < arr.length(); i++) {
            JSONObject obj = (JSONObject) arr.get(i);
            LinkEntryObject entry = new LinkEntryObject();
            entry.setLinkID( ( (Integer) obj.get("id") ).intValue());
            results[i] = entry;
        }
        return results;
    }
    
    private DistributionListEntryObject[] transformDistributionList(JSONArray arr) throws JSONException{
        DistributionListEntryObject[] results = new DistributionListEntryObject[arr.length()];
        for (int i = 0; i < arr.length(); i++) {
            JSONObject obj = (JSONObject) arr.get(i);
            DistributionListEntryObject entry = new DistributionListEntryObject();
            if(obj.has("display_name"))
                entry.setDisplayname( obj.getString("display_name") );
            if(obj.has("mail"))
                try {
                    entry.setEmailaddress( obj.getString("mail") );
                } catch (ContactException e) {
                    // don't set E-Mail at all
                }
            if(obj.has("mail_field"))
                entry.setEmailfield( obj.getInt("mail_field"));
        }
        return results;
    }
}
