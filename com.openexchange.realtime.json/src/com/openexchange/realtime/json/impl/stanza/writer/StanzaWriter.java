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

package com.openexchange.realtime.json.impl.stanza.writer;

import java.util.Collection;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.realtime.packet.IQ;
import com.openexchange.realtime.packet.Message;
import com.openexchange.realtime.packet.Presence;
import com.openexchange.realtime.packet.Stanza;
import com.openexchange.realtime.payload.PayloadElement;
import com.openexchange.realtime.payload.PayloadTree;
import com.openexchange.realtime.payload.PayloadTreeNode;

/**
 * {@link StanzaWriter} - Transforms Stanza objects into their JSON representation.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a> JavaDoc
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class StanzaWriter {

    /**
     * Writes specified stanza into its JSON representation.
     *
     * @param stanza The stanza to write
     * @return The appropriate JSON representation
     * @throws OXException If a JSON write error occurs
     */
    public JSONObject write(final Stanza stanza) throws OXException {
        try {
            JSONObject object = new JSONObject();

            writeBasics(stanza, object);
            writePayloadTrees(stanza, object);

            if (stanza instanceof Message) {
                writeMessage((Message) stanza, object);
            } else if (stanza instanceof Presence) {
                writePresence((Presence) stanza, object);
            } else if (stanza instanceof IQ) {
                writeQuery((IQ) stanza, object);
            }
            return object;
        } catch (JSONException x) {
            throw OXException.general("JSON error: " + x.toString());
        }
    }

    private void writeBasics(Stanza stanza, JSONObject object) throws JSONException {
        if (stanza.getFrom() != null) {
            object.put("from", stanza.getFrom().toString());
        }
        if (stanza.getTo() != null) {
            object.put("to", stanza.getTo().toString());
        }
        object.put("selector", stanza.getSelector());

        String id = stanza.getId();
        if (id != null && !id.isEmpty()) {
            object.put("id", id);
        }

        String tracer = stanza.getTracer();
        if (tracer != null) {
            object.put("tracer", tracer);
            JSONArray arr = new JSONArray(stanza.getLogEntries().size());
            for (String entry: stanza.getLogEntries()) {
                arr.put(entry);
            }
            object.put("log", arr);
        }

        if (stanza.getSequenceNumber() > -1) {
            object.put("seq", stanza.getSequenceNumber());
        }
    }

    /**
     * Write the PayloadTrees contained in the Stanza into a given JSONObject.
     *
     * @param stanza The Stanza conaining the PayloadTrees that have to be written as JSON
     * @param jsonStanza The Stanza as JSONObject
     * @throws JSONException If writing the PayloadTrees fails
     * @throws OXException 
     */
    private void writePayloadTrees(final Stanza stanza, final JSONObject jsonStanza) throws JSONException, OXException {
        stanza.transformPayloads("json");
        Collection<PayloadTree> payloadTrees = stanza.getPayloadTrees();
        JSONArray payloadArray = new JSONArray();
        for (PayloadTree payloadTree : payloadTrees) {
            payloadArray.put(writePayloadTreeNode(payloadTree.getRoot()));
        }
        jsonStanza.putOpt("payloads", payloadArray);
    }

    /**
     * Decide based on the hierarchy of PayloadTreeNodes how we have to create the JSONObject.
     * <p>
     * Data is transformed from a PayloadTreeNode (<b>PTNi</b>) eventually containing a PayloadElement (<b>PEi</b>).
     * <ol>
     * <li>SimpleType: <b>PEi</b> is already transformed into a String by one of the converters. <b>PTNi</b> without children -> Produce a
     * payload JSONObject from data of the PayloadElement</li>
     * <li>Array: <b>PEi</b> without data but <b>PTNi</b> with children -> The data of the PayloadElement will be set to null. Elements of
     * the array are attached as seperate PayloadTreeNodes (PTNj, PTNk, ...) below PTNi containing their own PayloadElements PEj, PEk and so
     * on.</li>
     * <li>ComplexType (already transformed into a JSONObject by one of the converters). PayloadTreeNode with data and children: The data of
     * the PayloadElement will be set to the Object. If the object contains nested container objects those are attached as seperate
     * PayloadTreeNodes (PTNj, PTNk, ...) below PTNi containing their own PayloadElements PEj, PEk and so on.</li>
     * </ol>
     * If data contains nested container objects those are attached as seperate PayloadTreeNodes (PTNj, PTNk, ...) below PTNi containing
     * their own PayloadElements PEj, PEk and so on. If a JSONObject contains a nested array container it is attached as seperate
     * PayloadTreeNode (PTNi) but the contained PayloadElement (PEi) doesn't contain any data. Instead the Elements of the array are
     * attached as children to the PayloadTreeNode.
     *
     * @param node The PayloadTreeNode that has be be written into a JSONObject
     * @param jsonObject The JSONObject to write the PayloadTreeNode into
     * @return the PayloadTreeNode written into a JSONObject
     * @throws JSONException If writing the PayloadTreeNode to JSON fails
     */
    private JSONObject writePayloadTreeNode(PayloadTreeNode node) throws JSONException {
        if (isSimpleNode(node)) {
            return createJSONPayload(node.getPayloadElement());
        } else if (isArrayNode(node)) {
            JSONObject jsonArrayNode = new JSONObject();
            jsonArrayNode.put("namespace", node.getNamespace());
            jsonArrayNode.put("element", node.getElementName());
            JSONArray dataArray = new JSONArray();
            for (PayloadTreeNode treeNode : node.getChildren()) {
                dataArray.put(writePayloadTreeNode(treeNode));
            }
            jsonArrayNode.put("data", dataArray);
            return jsonArrayNode;
        } else {
            /*
             * Complex Node, maybe with childNodes
             * PEi.data is a JSONObject

             */
            JSONObject complexJSONPayload = createJSONPayload(node.getPayloadElement());
//            /*
//             * TODO: Implement nesting of objects, need to add keynames as names to payloadTreeNode
//             */
//            for (PayloadTreeNode child : node.getChildren()) {
//                String name = child.getNodeName();
//                JSONObject jsonChild = createJSONPayload(node.getPayloadElement());
//                complexJSONPayload.put(name, jsonChild);
//            }
            return complexJSONPayload;
        }
    }

    private boolean isSimpleNode(PayloadTreeNode node) {
        Object data = node.getData();
        if (data != null && data instanceof String) {
            return true;
        }
        return false;
    }

    private boolean isArrayNode(PayloadTreeNode node) {
        Object data = node.getData();
        if ((data == null || JSONObject.NULL.equals(data))&& node.hasChildren()) {
            return true;
        }
        return false;
    }

    /**
     * Create a JSONObject from a PayloadElement.
     * @param payloadElement The PayloadElement to convert
     * @return  A JSONObject in the form <tt>{namespace: ... , element: ... , data: ...}</tt>
     * @throws JSONException If the JSONObject couldn't be created
     */
    private JSONObject createJSONPayload(PayloadElement payloadElement) throws JSONException {
        JSONObject jsonPayload = new JSONObject();
        jsonPayload.put("namespace", payloadElement.getNamespace());
        jsonPayload.put("element", payloadElement.getElementName());
        jsonPayload.put("data", payloadElement.getData());

        return jsonPayload;
    }

    private void writeQuery(IQ stanza, JSONObject object) throws JSONException {
        object.put("element", "iq");
        object.put("type", stanza.getType().name().toLowerCase());
    }

    private void writePresence(Presence stanza, JSONObject object) throws JSONException {
        object.put("element", "presence");
        object.put("type", stanza.getType().name().toLowerCase());
    }

    private void writeMessage(Message stanza, JSONObject object) throws JSONException {
        object.put("element", "message");
    }

}
