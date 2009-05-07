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

package com.openexchange.subscribe.json;

import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.subscribe.Subscription;


/**
 * {@link SubscriptionJSONWriter}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class SubscriptionJSONWriter {

    private static final String ID = "id";
    private static final String FOLDER = "folder";
    
    /**
     * @param subscription
     * @return
     * @throws JSONException 
     */
    public JSONObject write(Subscription subscription) throws JSONException {
        JSONObject object = new JSONObject();
        object.put(ID, subscription.getId());
        object.put(FOLDER, subscription.getFolderId());
        object.put("source", subscription.getSource().getId());
        writeConfiguration(object, subscription.getSource().getId(), subscription.getConfiguration());
        return object;
    }

    private void writeConfiguration(JSONObject object, String id, Map<String, String> configuration) throws JSONException {
        JSONObject configJSON = new JSONObject();
        for(Map.Entry<String, String> entry : configuration.entrySet()) {
            configJSON.put(entry.getKey(), entry.getValue());
        }
        object.put(id, configJSON);
    }

    public JSONArray writeArray(Subscription subscription, String[] basicCols, Map<String, String[]> specialCols, List<String> specialsList) {
        JSONArray array = new JSONArray();
        writeBasicCols(array, subscription, basicCols);
        for(String identifier : specialsList) {
            writeSpecialCols(array, subscription, specialCols.get(identifier), identifier);
        }
        return array;
    }

    private void writeSpecialCols(JSONArray array, Subscription subscription, String[] strings, String externalId) {
        boolean writeNulls = !subscription.getSource().getId().equals(externalId);
        Map<String, String> configuration  = subscription.getConfiguration();
        for(String col : strings) {
            if(writeNulls) {
                array.put(JSONObject.NULL);
            } else {
                array.put(configuration.get(col));
            }
        }
    }

    private void writeBasicCols(JSONArray array, Subscription subscription, String[] basicCols) {
        for(String basicCol : basicCols) {
            if("id".equals(basicCol)) {
                array.put(subscription.getId());
            } else if ("folder".equals(basicCol)) {
                array.put(subscription.getFolderId());
            } else if ("source".equals(basicCol)) {
                array.put(subscription.getSource().getId());
            }
        }
    }

}
