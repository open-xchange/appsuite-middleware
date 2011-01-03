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

package com.openexchange.eav.json.parse.arrayupdate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.eav.EAVSetTransformation;
import com.openexchange.eav.EAVType;
import com.openexchange.eav.json.exception.EAVJsonException;
import com.openexchange.eav.json.exception.EAVJsonExceptionMessage;
import com.openexchange.eav.json.parse.JSONParserInterface;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class JSONArrayUpdateParser implements JSONParserInterface<EAVSetTransformation> {
    
    private static final String ADD = "add";
    
    private static final String REMOVE = "remove";

    private JSONObject json;

    private EAVSetTransformation node;

    public JSONArrayUpdateParser(String name, JSONObject json) {
        if (name == null) {
            node = new EAVSetTransformation();
        } else {
            node = new EAVSetTransformation(name);
        }
        this.json = json;
    }

    public JSONArrayUpdateParser(JSONObject json) {
        this(null, json);
    }

    public EAVSetTransformation getNode() throws EAVJsonException {
        try {
            parse(json, node);
        } catch (JSONException e) {
            throw EAVJsonExceptionMessage.JSONException.create(e);
        }
        return node;
    }
    
    private void parse(JSONObject json, EAVSetTransformation node) throws JSONException, EAVJsonException {
        
        for (String key : json.keySet()) {
            Object object = json.get(key);
            if (JSONArray.class.isInstance(object)) {
                handleArray(key, (JSONArray) object, node);
            } else if (JSONObject.class.isInstance(object)) {
                handleObject(key, (JSONObject) object, node);
            } else {
                throw EAVJsonExceptionMessage.InvalidTreeStructure.create();
            }
        }
    }

    private void handleArray(String key, JSONArray array, EAVSetTransformation node) throws EAVJsonException, JSONException {
        if (array.length() == 0) {
            return;
        }
        if (key.equalsIgnoreCase(ADD)) {
            node.setAdd(getValues(array));
        } else if (key.equalsIgnoreCase(REMOVE)) {
            node.setRemove(getValues(array));
        } else {
            throw EAVJsonExceptionMessage.InvalidTreeStructure.create();
        }
        node.setType(EAVType.guessType(array.get(0).getClass()));
    }

    private void handleObject(String key, JSONObject json, EAVSetTransformation node) throws EAVJsonException, JSONException {
        EAVSetTransformation childNode = new EAVSetTransformation(key);
        parse(json, childNode);
        node.addChild(childNode);
    }

    private Object[] getValues(JSONArray array) throws JSONException, EAVJsonException {
        Object[] values = new Object[array.length()];
        EAVType currentType = null;
        for (int i = 0; i < array.length(); i++) {
            Object object = array.get(i);
            EAVType newType = EAVType.guessType(object.getClass());
            if (newType == null) {
                throw EAVJsonExceptionMessage.InvalidTreeStructure.create();
            }
            if (currentType != null && currentType != newType) {
                throw EAVJsonExceptionMessage.DifferentTypesInArray.create();
            }
            currentType = newType;
            values[i] = object;
        }
        return values;
    }

}
