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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.messaging.json;

import java.util.Collection;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.messaging.MessageHeader;
import com.openexchange.messaging.MessagingContent;
import com.openexchange.messaging.MessagingException;
import com.openexchange.messaging.SimpleMessagingMessage;


/**
 * {@link MessagingMessageWriter}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MessagingMessageWriter {

    public JSONObject write(SimpleMessagingMessage message) throws JSONException, MessagingException {
        JSONObject messageJSON = new JSONObject();
        
        messageJSON.put("id", message.getId());
        
        if(message.getColorLabel() > 0) {
            messageJSON.put("colorLabel", message.getColorLabel());
        }
        messageJSON.put("flags", message.getFlags());
        
        if(message.getReceivedDate() > 0) {
            messageJSON.put("received_date", message.getReceivedDate());
        }
        
        messageJSON.put("size", message.getSize());
        messageJSON.put("threadLevel", message.getThreadLevel());
        
        if(null != message.getDisposition()) {
            messageJSON.put("disposition", message.getDisposition());
        }
        
        if(null != message.getUserFlags()) {
            JSONArray userFlagsJSON = new JSONArray();
            for (String flag : message.getUserFlags()) {
                userFlagsJSON.put(flag);
            }
            messageJSON.put("user", userFlagsJSON);
        }
        
        if(null != message.getHeaders() && ! message.getHeaders().isEmpty()) {
            JSONObject headerJSON = new JSONObject();
            
            for (Map.Entry<String, Collection<MessageHeader>> entry : message.getHeaders().entrySet()) {

                JSONArray array = new JSONArray();
                
                for (MessageHeader header : entry.getValue()) {
                    array.put(header.getValue());
                }
                
                if(array.length() == 1) {
                    headerJSON.put(entry.getKey(), array.get(0));
                } else {
                    headerJSON.put(entry.getKey(), array);
                }
            }
            
            messageJSON.put("headers", headerJSON);
        }
        
        MessagingContent content = message.getContent();
        
        
        return messageJSON;
    }

}
