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

package com.openexchange.eav.json.write.metadata.type;

import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.eav.EAVContainerType;
import com.openexchange.eav.EAVType;
import com.openexchange.eav.EAVTypeMetadataNode;
import com.openexchange.eav.json.exception.EAVJsonException;
import com.openexchange.eav.json.exception.EAVJsonExceptionMessage;
import com.openexchange.eav.json.write.JSONWriterInterface;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class JSONTypeMetadataWriter implements JSONWriterInterface {
    
    private EAVTypeMetadataNode node;
    private JSONObject json;

    public JSONTypeMetadataWriter(EAVTypeMetadataNode node) {
        this.node = node;
        this.json = new JSONObject();
    }

    public JSONObject getJson() throws EAVJsonException {
        try {
            write(node, json);
        } catch (JSONException e) {
            throw EAVJsonExceptionMessage.JSONException.create(e);
        }
        return json;
    }

    public Object getValue() throws EAVJsonException {
        Object retval = null;
        try {
            write(node, json);
            retval = json.get(node.getName());
        } catch (JSONException e) {
            throw EAVJsonExceptionMessage.JSONException.create(e);
        }
        return retval;
    }
    
    private void write(EAVTypeMetadataNode node, JSONObject json) throws JSONException {
        
        if (node.getChildren().size() > 0) {
            handleObject(node, json);
        } else {
            handleLeaf(node, json);
        }
    }

    private void handleLeaf(EAVTypeMetadataNode node, JSONObject json) throws JSONException {
        JSONObject leafJson = new JSONObject();
        leafJson.put(EAVType.KEY, node.getType().getKeyword());
        leafJson.put(EAVContainerType.KEY, node.getContainerType().getKeyword()); 
        for (String optionKey : node.getOptions().keySet()) {
            leafJson.put(optionKey, node.getOption(optionKey));
        }
        json.put(node.getName(), leafJson);
    }

    private void handleObject(EAVTypeMetadataNode node, JSONObject json) throws JSONException {
        JSONObject childJson = new JSONObject();
        for (EAVTypeMetadataNode childNode : node.getChildren()) {
            write(childNode, childJson);
        }
        json.put(node.getName(), childJson);
    }

}
