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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.realtime.atmosphere.impl.stanza.builder;

import java.util.Iterator;
import org.apache.commons.logging.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.realtime.atmosphere.AtmosphereExceptionCode;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.Presence;
import com.openexchange.realtime.packet.Presence.Type;
import com.openexchange.realtime.payload.PayloadElement;
import static com.openexchange.realtime.payload.PayloadElement.PayloadFormat.*;
import com.openexchange.realtime.payload.PayloadTree;
import com.openexchange.realtime.payload.PayloadTreeNode;

/**
 * {@link PresenceBuilder} - Parse an atmosphere client's presence message and build a Presence Stanza from it by adding the recipients ID.
 * 
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class PresenceBuilder extends StanzaBuilder<Presence> {

    private static Log LOG = com.openexchange.log.Log.loggerFor(PresenceBuilder.class);

    /**
     * Create a new PresenceBuilder Initializes a new {@link PresenceBuilder}.
     * 
     * @param from the sender's ID, must not be null
     * @param json the sender's message, must not be null
     * @throws IllegalArgumentException if from or json are null
     */
    public PresenceBuilder(ID from, JSONObject json) {
        if (from == null || json == null) {
            throw new IllegalArgumentException();
        }
        this.from = from;
        this.json = json;
        this.stanza = new Presence();
    }

    @Override
    public Presence build() throws OXException {
        basics();

        type();
        return stanza;
    }

    private void type() {
        if (json.has("type")) {
            String type = json.optString("type");
            for (Presence.Type t : Presence.Type.values()) {
                if (t.name().equalsIgnoreCase(type)) {
                    stanza.setType(t);
                    break;
                }
            }
        } else {
            stanza.setType(Type.NONE);
        }
    }

    @Override
    protected void payloads() throws OXException {
        if (json.has("payloads")) {
            JSONArray payloads = json.optJSONArray("payloads");
            for (int i = 0; i < payloads.length(); i++) {
                JSONObject payload = payloads.optJSONObject(i);
                PayloadTree payloadTree = payloadToPayloadTree(payload);
                stanza.addPayload(payloadTree);
            }
        }
    }

    /**
     * Transform a JSONObject representing a Stanza payload into a PayloadTree.
     * 
     * @param payload JSONObject representing a Stanza payload
     * @return Stanza payload transformed into a PayloadTree
     */
    /**
     * Generate a hierarchy of PayloadTreeNodes from a nested JSON payload. Each incoming payload has the form of:
     * 
     * <pre>
     *  {
     *      "namespace" : "this is the namespace of the payload element"
     *      "element" : "this is the name of the payload element"
     *      "data" : 
     *  }
     * </pre>
     * 
     * The namespace has to be provided only if the element isn't part of the default namespace or if the hierarchie doesn't carry a
     * namespace already. Data is a JSON value. It can either be one of the two container types (JSONObject, JSONArray) or one of the simple
     * types (String, Number, Boolean, Null). Nested Container objects of JSONObject or JSONArray must follow the payload for above,
     * carrying namepsace, elementname and data.
     * <p>
     * Data is transformed into a PayloadTreeNode (PTNi) containing a PayloadElement (PEi).
     * <ol>
     * <li>Simple type: The data of the PayloadElement will be set to the simple type</li>
     * <li>Array: The data of the PayloadElement will be set to null. Elements of the array are attached as seperate PayloadTreeNodes (PTNj,
     * PTNk, ...) below PTNi containing their own PayloadElements PEj, PEk and so on.</li>
     * <li>Object: The data of the PayloadElement will be set to the Object. If the object contains nested container objects those are
     * attached as seperate PayloadTreeNodes (PTNj, PTNk, ...) below PTNi containing their own PayloadElements PEj, PEk and so on.</li>
     * </ol>
     * If data contains nested container objects those are attached as seperate PayloadTreeNodes (PTNj, PTNk, ...) below PTNi containing
     * their own PayloadElements PEj, PEk and so on. If a JSONObject contains a nested array container it is attached as seperate
     * PayloadTreeNode (PTNi) but the contained PayloadElement (PEi) doesn't contain any data. Instead the Elements of the array are
     * attached as children to the PayloadTreeNode.
     * 
     * @param payload The payload to transform
     * @return A hierarchy of PayloadTreeNodes
     * @throws OXException if building of the Stanza fails
     */
    private PayloadTree payloadToPayloadTree(JSONObject payload) throws OXException {
        PayloadTree payloadTree = new PayloadTree();
        try {
            payloadTree.setRoot(payloadToPayloadTreeNode(payload));
        } catch (JSONException ex) {
            LOG.error(ex);
            throw AtmosphereExceptionCode.ERROR_WHILE_BUILDING.create(ex);
        }
        return payloadTree;
    }

    /**
     * Decide based on the type of payloadData how we have to create the PayloadTreeNode and possible children.
     * 
     * @param node The current PayloadTreeNode holding an empty PayloadElement
     * @param data The payload data
     * @return the PayloadTreeNode with the filled PayloadElement and possible children attached.
     * @throws OXException For payloads with broken syntax
     * @throws JSONException For missing or access to mistyped JSONObjects
     */
    private PayloadTreeNode payloadToPayloadTreeNode(JSONObject payload) throws OXException, JSONException {
        PayloadTreeNode node = new PayloadTreeNode();
        PayloadElement payloadElement = null;

        String namespace = payload.optString("namespace");
        String elementName;
        Object data;

        try {
            elementName = payload.getString("element");
        } catch (JSONException e) {
            OXException exception = AtmosphereExceptionCode.MISSING_KEY.create("element");
            LOG.error(exception);
            throw exception;
        }
        try {
            data = payload.get("data");
        } catch (JSONException e) {
            OXException exception = AtmosphereExceptionCode.MISSING_KEY.create("data");
            LOG.error(exception);
            throw exception;
        }

        payloadElement = new PayloadElement(null, JSON, namespace, elementName);

        if (data instanceof JSONArray) {
            // attach nested containers as children
            JSONArray array = (JSONArray) data;
            addPayloadsFromArray(node, array);
        } else if (data instanceof JSONObject) {
            // collect simple values in new JSONObject and set it as data in the payloadelement, attach nested containers as children
            JSONObject object = (JSONObject) data;
            Iterator<String> keys = object.keys();
            // Object to set in the PayloadElement
            JSONObject outputObject = new JSONObject();
            while (keys.hasNext()) {
                String key = keys.next();
                Object value = payload.get(key);
                if (value instanceof JSONArray) {
                    JSONArray array = (JSONArray) data;
                    addPayloadsFromArray(node, array);
                } else if (value instanceof JSONObject) {
                    JSONObject nestedObject = (JSONObject) value;
                    node.addChild(payloadToPayloadTreeNode(nestedObject));
                } else {
                    outputObject.put(key, value);
                }
            }
            payloadElement.setData(outputObject, JSON);
        } else {
            // payloadData only consists of a value, set it in the PayloadElement
            payloadElement.setData(data, JSON);
        }
        node.setPayloadElement(payloadElement);
        return node;
    }

    private void addPayloadsFromArray(PayloadTreeNode node, JSONArray array) throws JSONException, OXException {
        int arrayLength = array.length();
        // attach a new child for every payloadElement in the array
        for (int i = 0; i < arrayLength; i++) {
            JSONObject jsonObject = array.getJSONObject(i);
            node.addChild(payloadToPayloadTreeNode(jsonObject));
        }
    }

}
