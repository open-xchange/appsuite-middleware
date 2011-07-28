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

package com.openexchange.contact.json.osgi;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.fields.ContactFields;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.Converter;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.conversion.DataArguments;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.datasource.ContactImageDataSource;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.contact.helpers.ContactGetter;
import com.openexchange.groupware.container.Contact;
import com.openexchange.image.ImageService;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link ContactResultConverter}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class ContactResultConverter implements ResultConverter {
    
    private static final Log LOG = com.openexchange.log.Log.valueOf(org.apache.commons.logging.LogFactory.getLog(ContactResultConverter.class));
    
    private static final Map<Integer, String> specialColumns = new HashMap<Integer, String>();
    
    static {
        specialColumns.put(Contact.LAST_MODIFIED_OF_NEWEST_ATTACHMENT, "date");
        specialColumns.put(Contact.CREATION_DATE, "date");
        specialColumns.put(Contact.LAST_MODIFIED, "date");
        specialColumns.put(Contact.BIRTHDAY, "date");
        specialColumns.put(Contact.ANNIVERSARY, "date");
        specialColumns.put(Contact.IMAGE_LAST_MODIFIED, "date");
    }
    
    
    private ImageService imageService;
    
    
    public ContactResultConverter(ImageService imageService) {
        super();
        this.imageService = imageService;
    }
    
    public ContactResultConverter() {
        super();
    }

    @Override
    public String getInputFormat() {
        return "contact";
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
    public void convert(AJAXRequestData request, AJAXRequestResult result, ServerSession session, Converter converter) throws OXException {
        Contact contact = (Contact) result.getResultObject();
        result.setResultObject(convert(contact, session), "json");
    }

    private Object convert(Contact contact, ServerSession session) throws OXException {
        JSONObject json = new JSONObject();

        ContactGetter cg = new ContactGetter();
        for (int column : Contact.JSON_COLUMNS) {
            ContactField field = ContactField.getByValue(column);
            if (field != null && !field.getAjaxName().isEmpty()) {                
                try {
                    Object value = field.doSwitch(cg, contact);
                    if (value != null && !String.valueOf(value).isEmpty()) {
                        if (specialColumns.containsKey(column)) {
                            treatSpecially(field, value, json);
                        } else {
                            String jsonKey = field.getAjaxName();
                            json.put(jsonKey, value);
                        }
                    }
                } catch (JSONException e) {
                    OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e);
                }
            }
        }
        
        // Set last_modified_utc
        try {
            Date lastModified = contact.getLastModified();
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(lastModified);
            int offset = calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET);
            calendar.add(Calendar.MILLISECOND, -offset);
            json.put(ContactFields.LAST_MODIFIED_UTC, calendar.getTime().getTime());
        } catch (JSONException e) {
            OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e);
        }
        
        // Set image url
        if (contact.containsImage1()) {
            if (null == imageService) {
                LOG.warn("Contact image URL cannot be written. Missing service: " + ImageService.class.getName());
            } else {
                final byte[] imageData = contact.getImage1();
                if (imageData != null) {
                    final String imageURL;
                    final ContactImageDataSource imgSource = new ContactImageDataSource();
                    final DataArguments args = new DataArguments();
                    final String[] argsNames = imgSource.getRequiredArguments();
                    args.put(argsNames[0], String.valueOf(contact.getParentFolderID()));
                    args.put(argsNames[1], String.valueOf(contact.getObjectID()));
                    imageURL = imageService.addImageData(session, imgSource, args).getImageURL();                    
                    try {
                        json.put(ContactFields.IMAGE1_URL, imageURL);
                    } catch (JSONException e) {
                        OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e);
                    }
                }
            }
        }
        
        return json;
    }

    private void treatSpecially(ContactField field, Object value, JSONObject json) throws JSONException {
        String type = specialColumns.get(field.getNumber());
        if (type.equals("date")) {
            String jsonKey = field.getAjaxName();
            Date date = (Date) value;
            json.put(jsonKey, date.getTime());
        }
    }

}
