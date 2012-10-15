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

import com.openexchange.exception.OXException;
import com.openexchange.realtime.payload.PayloadTree;
import com.openexchange.realtime.payload.PayloadTreeNode;

/**
 * {@link Presence} - Exchanges presence information. A Presence Stanza is a broadcast from a single entity X to a set of entities
 * subscribed to this specific entity X. Being a broadcast this stanza normally doesn't specify a recipient via the <code>to</code> field.
 * Nevertheless clients are able to send directed Presence Stanzas to single recipients by specifying a <code>to</code>. Modeled after
 * RFC3291 <xs:element name='presence'> <xs:complexType> <xs:sequence> <xs:choice minOccurs='0' maxOccurs='unbounded'> <xs:element
 * ref='show'/> <xs:element ref='status'/> <xs:element ref='priority'/> </xs:choice> <xs:any namespace='##other' minOccurs='0'
 * maxOccurs='unbounded'/> <xs:element ref='error' minOccurs='0'/> </xs:sequence> <xs:attribute name='from' type='xs:string'
 * use='required'/> <xs:attribute name='id' type='xs:NMTOKEN' use='optional'/> <xs:attribute name='to' type='xs:string' use='required'/>
 * <xs:attribute name='type' use='optional'> <xs:simpleType> <xs:restriction base='xs:NCName'> <xs:enumeration value='error'/>
 * <xs:enumeration value='probe'/> <xs:enumeration value='subscribe'/> <xs:enumeration value='subscribed'/> <xs:enumeration
 * value='unavailable'/> <xs:enumeration value='unsubscribe'/> <xs:enumeration value='unsubscribed'/> </xs:restriction> </xs:simpleType>
 * </xs:attribute> <xs:attribute ref='xml:lang' use='optional'/> </xs:complexType> </xs:element> <xs:element name='show'> <xs:simpleType>
 * <xs:restriction base='xs:NCName'> <xs:enumeration value='away'/> <xs:enumeration value='chat'/> <xs:enumeration value='dnd'/>
 * <xs:enumeration value='xa'/> </xs:restriction> </xs:simpleType> </xs:element> <xs:element name='status'> <xs:complexType>
 * <xs:simpleContent> <xs:extension base='xs:string'> <xs:attribute ref='xml:lang' use='optional'/> </xs:extension> </xs:simpleContent>
 * </xs:complexType> </xs:element> <xs:element name='priority' type='xs:byte'/> <xs:element name='error'> <xs:complexType> <xs:sequence
 * xmlns:err='urn:ietf:params:xml:ns:xmpp-stanzas'> <xs:group ref='err:stanzaErrorGroup'/> <xs:element ref='err:text' minOccurs='0'/>
 * </xs:sequence> <xs:attribute name='code' type='xs:byte' use='optional'/> <xs:attribute name='type' use='required'> <xs:simpleType>
 * <xs:restriction base='xs:NCName'> <xs:enumeration value='auth'/> <xs:enumeration value='cancel'/> <xs:enumeration value='continue'/>
 * <xs:enumeration value='modify'/> <xs:enumeration value='wait'/> </xs:restriction> </xs:simpleType> </xs:attribute> </xs:complexType>
 * </xs:element>
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

    // names of the payload elements from the default schema we are interested in
    public static final String ERROR = "error";

    public static final String MESSAGE = "message";

    public static final String PRIORITY = "priority";

    public static final String STATE = "state";

    /**
     * Optional attribute. The default of none means the client is available.
     */
    private Type type = Type.NONE;

    /**
     * The server should deliver messages to the highest-priority available resource or decide on metrics like most recent connect,
     * activity, PresenceState if several resources with the same priority are connected.
     */
    private byte priority = 0;

    /**
     * Signal Availability by choosing ONLINE as default. Clients may set different states.
     */
    private PresenceState state = PresenceState.ONLINE;

    /**
     * Empty message as default. Clients may set a different message.
     */
    private String message = "";

    /**
     * The error object for Presence Stanza of type error
     */
    private OXException error = null;

    /**
     * Get the error element describing the error-type Stanza in more detail.
     * 
     * @return The OXException representing the error
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
     * Sets the priority.
     * 
     * @return The message
     */
    public void setMessage(String message) {
        this.message = message;
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
    }

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
     * Add a PayloadTree to the Stanza and keep track of the payload's namespace. When PayloadTrees containing elements of the standard presence
     * schema are added, we try to assign the payload's data to the corresponding field so it can easily accessed by setters and getters.
     * 
     * @param tree The PayloadTree to add.
     * @return true if the Stanza didn't already contain the PayloadTree
     */
    @Override
    public boolean addPayload(final PayloadTree tree) {
        if (tree == null) {
            throw new IllegalArgumentException("tree must not be null");
        }

        boolean isAdded = true;
        PayloadTreeNode node = tree.getRoot();
        if (node.getNamespace() == null) { // default namespace means no namespace
            String elementName = node.getElementName();
            if (ERROR.equalsIgnoreCase(elementName) && node.getData() instanceof OXException) {
                setError((OXException) node.getData());
            } else if (MESSAGE.equalsIgnoreCase(elementName) && node.getData() instanceof String) {
                setMessage((String) node.getData());
            } else if (PRIORITY.equalsIgnoreCase(elementName) && node.getData() instanceof Byte) {
                setPriority((Byte) node.getData());
            } else if (STATE.equalsIgnoreCase(elementName) && node.getData() instanceof PresenceState) {
                setState((PresenceState) node.getData());
            }
        } else { // payload has a namespace -> add as extension
            isAdded &= extensions.add(tree);
        }
        // track payload anyway in the list of all payloads for further transformation
        isAdded &= payloads.add(tree);

        return isAdded;
    }

    /**
     * Remove a Payload from the Stanza and set fields to default values.
     * 
     * @param node The PayloadTreeNode to remove
     * @return True if the Stanza contained this Payload
     */
    @Override
    public boolean removePayload(final PayloadTree tree) {
        if (tree == null) {
            throw new IllegalArgumentException("Payload must not be null");
        }

        boolean isRemoved = payloads.remove(tree);

        PayloadTreeNode node = tree.getRoot();

        if (node.getNamespace() == null) { // default namespace means no namespace, reset to defaults
            String elementName = node.getElementName();
            if (ERROR.equalsIgnoreCase(elementName)) {
                setError(null);
            } else if (MESSAGE.equalsIgnoreCase(elementName)) {
                setMessage("");
            } else if (PRIORITY.equalsIgnoreCase(elementName)) {
                setPriority((byte) 0);
            } else if (STATE.equalsIgnoreCase(elementName)) {
                setState(PresenceState.ONLINE);
            }
        } else { // payload has a namespace -> remove from extension
            isRemoved &= extensions.remove(node);
        }

        return isRemoved;
    }

}
