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

package com.openexchange.eav.json.parse.metadata.type;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.eav.EAVContainerType;
import com.openexchange.eav.EAVType;
import com.openexchange.eav.EAVTypeMetadataNode;
import com.openexchange.eav.json.exception.EAVJsonException;
import com.openexchange.eav.json.exception.EAVJsonExceptionMessage;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class JSONTypeMetadataParser implements JSONTypeMetadataParserInterface {

    private JSONObject json;

    private EAVTypeMetadataNode node;

    public JSONTypeMetadataParser(JSONObject json) {

    }

    public JSONTypeMetadataParser(String name, JSONObject json) {
        if (name == null) {
            node = new EAVTypeMetadataNode();
        } else {
            node = new EAVTypeMetadataNode(name);
        }
        this.json = json;
    }

    public EAVTypeMetadataNode getEAVNode() throws EAVJsonException {
        try {
            parseJson(json, node);
        } catch (JSONException e) {
            throw EAVJsonExceptionMessage.JSONException.create(e);
        }
        return node;
    }

    private void parseJson(JSONObject json, EAVTypeMetadataNode node) throws EAVJsonException, JSONException {

        boolean hasLeaf = false;
        boolean hasObject = false;

        for (String key : json.keySet()) {
            Object object = json.get(key);
            if (JSONArray.class.isInstance(object)) {
                throw EAVJsonExceptionMessage.InvalidTreeStructure.create();
            } else if (JSONObject.class.isInstance(object)) {
                hasObject = true;
                handleObject(hasLeaf, key, (JSONObject) object, node);
            } else {
                hasLeaf = true;
                handleLeaf(hasObject, key, object, node);
            }
        }
    }

    private void handleObject(boolean hasLeaf, String key, JSONObject json, EAVTypeMetadataNode node) throws EAVJsonException, JSONException {
        if (hasLeaf) {
            throw EAVJsonExceptionMessage.InvalidTreeStructure.create();
        }

        EAVTypeMetadataNode childNode = new EAVTypeMetadataNode(key);
        parseJson(json, childNode);
        node.addChild(childNode);
    }

    private void handleLeaf(boolean hasObject, String key, Object object, EAVTypeMetadataNode node) throws EAVJsonException {
        if (hasObject) {
            throw EAVJsonExceptionMessage.InvalidTreeStructure.create();
        }

        if (!String.class.isInstance(object)) {
            throw EAVJsonExceptionMessage.InvalidTreeStructure.create();
        }

        String objectString = (String) object;

        if (key.equals(EAVType.KEY)) {
            node.setType(EAVType.getType(objectString));
        } else if (key.equals(EAVContainerType.KEY)) {
            node.setContainerType(EAVContainerType.getType(objectString));
        } else {
            node.setOption(key, objectString);
        }
    }

}
