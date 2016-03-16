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

package com.openexchange.realtime.json.stanza;

import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.realtime.exception.RealtimeException;
import com.openexchange.realtime.exception.RealtimeExceptionCodes;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.Stanza;
import com.openexchange.realtime.payload.PayloadElement;
import com.openexchange.realtime.payload.PayloadTree;
import com.openexchange.realtime.payload.PayloadTreeNode;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link StanzaBuilder} - Abstract StanzaBuilder class. StanzaBuilders take incoming Request and are responsible for building the
 * appropriate Stanza Objects from them. Tis includes filling the Stanza with basic attributes from the JSONObject transported in the
 * Request and transforming payload arrays found in the JSON Object into PayloadTrees without actually transforming any Payload
 * data.
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public abstract class StanzaBuilder<T extends Stanza> {

    private final static String JSON = "json";

    protected ID from;

    protected JSONObject json;

    protected T stanza;

    private final ServerSession session;

    /**
     * Initializes a new {@link StanzaBuilder}.
     * @param session
     */
    public StanzaBuilder(ServerSession session) {
        this.session = session;
    }

    /**
     * Set the obligatory {@link Stanza} elements.
     *
     * @throws RealtimeException for errors happening while building the Stanza
     */
    protected void basics() throws RealtimeException {
        from();
        to();
        id();
        selector();
        sequence();
        tracer();
        payloads();
    }

    private void from() {
        stanza.setFrom(from);
    }

    private void to() {
        if (json.has("to")) {
            String defaultContext = null;
            if (session != null) {
                defaultContext = Integer.toString(session.getContext().getContextId());
            }
            stanza.setTo(new ID(json.optString("to"), defaultContext));
        }
    }

    private void id() {
        if (json.has("id")) {
            stanza.setId(json.optString("id"));
        }
    }

    private void selector() {
        if (json.has("selector")) {
            stanza.setSelector(json.optString("selector"));
        }
    }

    private void tracer() {
        if (json.has("tracer")) {
            stanza.setTracer(json.optString("tracer"));
        }
    }

    private void sequence() {
        if (json.has("seq")) {
            stanza.setSequenceNumber(json.optLong("seq"));
        }
    }

    /**
     * Process the payloads found in the JSONObject representing the Stanza.
     */
    protected void payloads() throws RealtimeException {
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
     * containing namespace, elementname and data.
     *
     * @param payload The payload to transform
     * @return A hierarchy of PayloadTreeNodes
     * @throws RealtimeException if building of the Stanza fails
     */
    private PayloadTree payloadToPayloadTree(JSONObject payload) throws RealtimeException {
        PayloadTree payloadTree = new PayloadTree();
        payloadTree.setRoot(payloadToPayloadTreeNode(payload));
        return payloadTree;
    }

    /**
     * Decide based on the type of payloadData how we have to create the PayloadTreeNode and possible children.
     * <p>
     * Data is transformed into a PayloadTreeNode (PTNi) eventually containing a PayloadElement (PEi).
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
     * If the payload is marked as verbatim, the data is not transformed further but kept as-is
     *
     * @param payload The payload data
     * @return the PayloadTreeNode with the filled PayloadElement and possible children attached.
     * @throws RealtimeException For payloads with broken syntax
     * @throws JSONException
     */
    private PayloadTreeNode payloadToPayloadTreeNode(JSONObject payload) throws RealtimeException {
        PayloadTreeNode node = new PayloadTreeNode();
        PayloadElement payloadElement = null;

        String namespace = payload.optString("namespace");
        String elementName;
        Object data;

        try {
            elementName = payload.getString("element");
        } catch (JSONException e) {
            RealtimeException realtimeException = RealtimeExceptionCodes.STANZA_BAD_REQUEST.create(String.format(
                "Obligatory key \"%1$s\" is missing from the Stanza",
                "element"));
            throw realtimeException;
        }
        try {
            data = payload.get("data");
        } catch (JSONException e) {
            RealtimeException realtimeException = RealtimeExceptionCodes.STANZA_BAD_REQUEST.create(String.format(
                "Obligatory key \"%1$s\" is missing from the Stanza",
                "data"));
            throw realtimeException;
        }

        payloadElement = new PayloadElement(null, JSON, namespace, elementName);
        if (payload.optBoolean("verbatim", false)) {
            payloadElement.setData(data, JSON);
        } else if (data instanceof JSONArray) {
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
                try {
                    String key = keys.next();
                    Object value = object.get(key);
                    if (value instanceof JSONArray) {
                        JSONArray array = (JSONArray) value;
                        addPayloadsFromArray(node, array);
                    } else if (value instanceof JSONObject) {
                        JSONObject nestedObject = (JSONObject) value;
                        node.addChild(payloadToPayloadTreeNode(nestedObject));
                    } else {
                        outputObject.put(key, value);
                    }
                } catch (JSONException e) {
                    throw RealtimeExceptionCodes.STANZA_BAD_REQUEST.create(e.getMessage());
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

    private void addPayloadsFromArray(PayloadTreeNode node, JSONArray array) throws RealtimeException {
        int arrayLength = array.length();
        // attach a new child for every payloadElement in the array
        for (int i = 0; i < arrayLength; i++) {
            try {
                JSONObject jsonObject = array.getJSONObject(i);
                node.addChild(payloadToPayloadTreeNode(jsonObject));
            } catch (JSONException e) {
                RealtimeExceptionCodes.STANZA_BAD_REQUEST.create(String.format(
                    "Error while building Stanza: \"%1$s\"",
                    "JSONObject expected"));
            }
        }
    }

    /**
     * Build a validated Stanza of type T
     *
     * @return a validated Stanza of type T
     * @throws RealtimeException if the Stanza couldn't be build due to validation or other errors
     */
    public abstract T build() throws RealtimeException;

    /**
     * Parses the supplied value into an enumeration constant, ignoring case.
     *
     * @param enumType The Class object of the enum type from which to return a constant
     * @param name The name of the constant to return
     * @return The enum constant
     * @throws IllegalArgumentException If there's no suitable enum constant for the supplied name
     */
    protected static <T extends Enum<T>> T parse(Class<T> enumeration, String name) {
        T value = parse(enumeration, name, null);
        if (null != value) {
            return value;
        }
        throw new IllegalArgumentException("No enum value '" + name + " in Enum " + enumeration.getClass().getName());
    }

    /**
     * Parses the supplied value into an enumeration constant, ignoring case.
     *
     * @param enumType The Class object of the enum type from which to return a constant
     * @param name The name of the constant to return
     * @param defaultValue The enumeration constant to return if parsing fails
     * @return The enum constant
     */
    protected static <T extends Enum<T>> T parse(Class<T> enumeration, String name, T defaultValue) {
        for (T value : enumeration.getEnumConstants()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return defaultValue;
    }

}
