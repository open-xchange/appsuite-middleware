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

package com.openexchange.realtime.packet;

import java.util.ArrayList;
import java.util.Collection;
import com.google.common.base.Predicate;
import com.openexchange.exception.OXException;
import com.openexchange.realtime.payload.PayloadElement;
import com.openexchange.realtime.payload.PayloadTree;
import com.openexchange.realtime.payload.PayloadTreeNode;
import com.openexchange.realtime.util.ElementPath;

/**
 * {@link Presence} - Exchanges presence information. A Presence Stanza is a broadcast from a single entity X to a set of entities
 * subscribed to this specific entity X. Being a broadcast this stanza normally doesn't specify a recipient via the <code>to</code> field.
 * Nevertheless clients are able to send directed Presence Stanzas to single recipients by specifying a <code>to</code>.
 * <p>
 * This class allows access to the default Presence specific fields and knows how to access the default payload fields within the associated
 * PayloadTrees. Extensions to Presence Stanza can be queried via the {@link Presence#getExtensions()} function and programmatically
 * extracted from the Stanza via {@link Stanza#getPayload(com.openexchange.realtime.util.ElementPath)} function.
 * </p>
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a> JavaDoc
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */

public class Presence extends Stanza {

    /**
     * {@link Type} - Specifies the Presence Type
     * <ol>
     * <li>unavailable: entity is no longer available for communication</li>
     * <li>subscribe: sender wants to subscribe to recipient's presence</li>
     * <li>subscribed: sender allowed recipient to subscribe to their presence</li>
     * <li>unsubscribe: sender wants to unsubscribe from recipient's presence</li>
     * <li>unsubscribed: <code>subscribe</code> request has been denied or an existing subscription has been cancelled</li>
     * <li>error: if an error occurred during processing or delivery of the last stanza</li>
     * <li>none: is used for the initial presence message of a client to signal its availability for communications.</li>
     * <li>pending:</li>
     * </ol>
     * 
     * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
     */
    public enum Type {
        UNAVAILABLE, SUBSCRIBE, SUBSCRIBED, UNSUBSCRIBE, UNSUBSCRIBED, ERROR, NONE, PENDING;
    }

    // Names of the payload elements from the default schema we are interested in
    public static final ElementPath MESSAGE_PATH = new ElementPath("status");

    public static final ElementPath PRESENCE_STATE_PATH = new ElementPath("show");

    public static final ElementPath PRIORITY_PATH = new ElementPath("priority");

    public static final ElementPath ERROR_PATH = new ElementPath("error");

    private ArrayList<ElementPath> defaultElements = new ArrayList<ElementPath>();

    /** Predicate to filter extension elements from {@link Stanza#payloads} */
    private final Predicate<PayloadTree> defaultsPredicate = new Predicate<PayloadTree>() {

        @Override
        public boolean apply(PayloadTree input) {
            return defaultElements.contains(input.getElementPath());
        }
    };

    /** Predicate to filter default elements from {@link Stanza#payloads} */
    private final Predicate<PayloadTree> extensionsPredicate = new Predicate<PayloadTree>() {

        @Override
        public boolean apply(PayloadTree input) {
            return !defaultElements.contains(input.getElementPath());
        }
    };

    /**
     * Initializes a new {@link Presence}.
     */
    public Presence() {
        defaultElements.add(MESSAGE_PATH);
        defaultElements.add(PRESENCE_STATE_PATH);
        defaultElements.add(PRIORITY_PATH);
        defaultElements.add(ERROR_PATH);
    }

    /**
     * Optional attribute. The default of none means the client is available.
     */
    private Type type = Type.NONE;

    /**
     * Empty message as default. Clients may set a different message.
     */
    private String message = "";

    /**
     * Signal Availability by choosing ONLINE as default. Clients may set different states.
     */
    private PresenceState state = PresenceState.ONLINE;

    /**
     * The server should deliver messages to the highest-priority available resource or decide on metrics like most recent connect,
     * activity, PresenceState if several resources with the same priority are connected.
     */
    private byte priority = 0;

    /**
     * The error object for Presence Stanza of type error
     */
    private OXException error = null;

    /**
     * Gets the type of Presence Stanza
     * 
     * @return The state
     */
    public Type getType() {
        return type;
    }

    /**
     * Sets the type of the Presence Stanza
     * 
     * @param type The state to set
     */
    public void setType(Type type) {
        this.type = type;
    }

    /**
     * Gets the message.
     * 
     * @return The message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the message.
     */
    public void setMessage(String message) {
        this.message = message;
        writeThrough(MESSAGE_PATH, message);
    }

    /**
     * Gets the state e.g. online or away
     * 
     * @return The state
     */
    public PresenceState getState() {
        return state;
    }

    /**
     * Sets the state e.g. online or away
     * 
     * @param state The state
     */
    public void setState(PresenceState state) {
        this.state = state;
        writeThrough(PRESENCE_STATE_PATH, state);
    }

    /**
     * Gets the priority.
     * 
     * @return The priority
     */
    public byte getPriority() {
        return priority;
    }

    /**
     * Sets the priority.
     * 
     * @param priority The priority to set
     */
    public void setPriority(byte priority) {
        this.priority = priority;
        writeThrough(PRIORITY_PATH, priority);
    }

    /**
     * Get the error element describing the error-type Stanza in more detail.
     * 
     * @return Null or the OXException representing the error
     */
    public OXException getError() {
        return error;
    }

    /**
     * Set the error element describing the error-type Stanza in more detail.
     * 
     * @param error The OXException representing the error
     */
    public void setError(OXException error) {
        this.error = error;
        writeThrough(ERROR_PATH, error);
    }

    /**
     * Get the default payloads.
     * 
     * @return The default payloads as defined in the Presence specification.
     */
    public Collection<PayloadTree> getDefaultPayloads() {
        return filterPayloads(defaultsPredicate);
    }

    /**
     * Get the extension payloads.
     * 
     * @return Extension payloads that aren't defined in the Presence specification and not accessible via getters and setters.
     */
    public Collection<PayloadTree> getExtensions() {
        return filterPayloads(extensionsPredicate);
    }

    /**
     * Write a payload to the PayloadTree identified by the ElementPath. The trees for the default elements only contain one node so we can
     * set the data by directly writing to the root node.
     * 
     * @param path The ElementPath identifying the PayloadTree.
     * @param data The payload data to write into the root node.
     */
    private void writeThrough(ElementPath path, Object data) {
        PayloadTree payloadTree = payloads.get(path);
        if (payloadTree == null) {
            PayloadElement payloadElement = new PayloadElement(data, data.getClass().getSimpleName(), path.getNamespace(), path.getElement());
            PayloadTreeNode payloadTreeNode = new PayloadTreeNode(payloadElement);
            payloadTree = new PayloadTree(payloadTreeNode);
        }
        PayloadTreeNode node = payloadTree.getRoot();
        if (node == null) {
            throw new IllegalStateException("PayloadTreeNode removed? This shouldn't happen!");
        }
        node.setData(data, data.getClass().getSimpleName());
    }

}
