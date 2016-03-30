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

package com.openexchange.realtime.packet;

import java.util.ArrayList;
import java.util.Collection;
import com.google.common.base.Predicate;
import com.openexchange.exception.OXException;
import com.openexchange.realtime.exception.RealtimeException;
import com.openexchange.realtime.payload.PayloadTree;
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

    private static final long serialVersionUID = -1947763158000197160L;

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
    public static final ElementPath MESSAGE_PATH = new ElementPath("message");

    public static final ElementPath STATUS_PATH = new ElementPath("status");

    public static final ElementPath PRIORITY_PATH = new ElementPath("priority");



    private static final ArrayList<ElementPath> defaultElements = new ArrayList<ElementPath>();

    /** Predicate to filter extension elements from {@link Stanza#payloads} */
    private transient Predicate<PayloadTree> defaultsPredicate = null;

    /** Predicate to filter default elements from {@link Stanza#payloads} */
    private transient Predicate<PayloadTree> extensionsPredicate = null;

    private Predicate<PayloadTree> getDefaultsPredicate() {
        if (defaultsPredicate == null) {
            defaultsPredicate = new Predicate<PayloadTree>() {

                @Override
                public boolean apply(PayloadTree input) {
                    return defaultElements.contains(input.getElementPath());
                }
            };
        }

        return defaultsPredicate;
    }

    private Predicate<PayloadTree> getExtensionsPredicate() {
        if (extensionsPredicate == null) {
            extensionsPredicate = new Predicate<PayloadTree>() {

                @Override
                public boolean apply(PayloadTree input) {
                    return !defaultElements.contains(input.getElementPath());
                }
            };
        }

        return extensionsPredicate;
    }

    /**
     * Initializes a new {@link Presence}.
     */
    public Presence() {
        defaultElements.add(MESSAGE_PATH);
        defaultElements.add(STATUS_PATH);
        defaultElements.add(PRIORITY_PATH);
        defaultElements.add(ERROR_PATH);
        setMessage(message);
        setState(state);
        setPriority(priority);
    }

    /**
     * Initializes a new {@link Presence} based on another Presence. This will produce a deep copy up to the leafs of the PayloadTreeNode,
     * more exactly the data Portion of the PayloadElement in the PayloadTreeNode as we are dealing with Objects that must not neccessarily
     * implement Cloneable or Serializable.
     * 
     * @param other The Presence to copy, must not be null
     * @throws IllegalArgumentException if the other Presence is null
     */
    public Presence(Presence other) {
        if (other == null) {
            throw new IllegalArgumentException("Other Presence must not be null.");
        }
        super.setFrom(other.getFrom());
        super.setTo(other.getTo());
        this.error = other.error;
        this.message = other.message;
        this.payloads = other.deepCopyPayloads();
        this.priority = other.priority;
        this.state = other.state;
        this.type = other.type;
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
     * Gets the type of Presence Stanza
     * 
     * @return The type
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
        writeThrough(STATUS_PATH, state);
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
     * Get the default payloads.
     * 
     * @return The default payloads as defined in the Presence specification.
     */
    public Collection<PayloadTree> getDefaultPayloads() {
        return filterPayloadTrees(getDefaultsPredicate());
    }

    /**
     * Get the extension payloads.
     * 
     * @return Extension payloads that aren't defined in the Presence specification and not accessible via getters and setters.
     */
    public Collection<PayloadTree> getExtensions() {
        return filterPayloadTrees(getExtensionsPredicate());
    }

    @Override
    public void initializeDefaults() throws OXException {
        Initializer initializer = new Initializer();
        Collection<PayloadTree> defaultPayloads = getDefaultPayloads();
        initializer.initializeFromDefaults(defaultPayloads);
    }

    /**
     * {@link Initializer} - Is used to initialize the default Stanza fields from PayloadTrees contained in the Stanza.
     * 
     * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
     */
    private final class Initializer {

        /**
         * Initializes a new {@link Initializer}.
         */
        public Initializer() {
            super();
        }

        /**
         * Initialize the default Stanza fields from PayloadTrees contained in the Stanza
         * 
         * @param defaultPayloads the PayloadTrees containing the PayloadElements needed to initialize the default Stanza fields
         * @throws OXException when the Stanza couldn't be initialized
         */
        public void initializeFromDefaults(Collection<PayloadTree> defaultPayloads) throws OXException {
            initShow(defaultPayloads);
            initStatus(defaultPayloads);
            initPriority(defaultPayloads);
        }

        /*
         * The Status Message
         */
        private void initStatus(Collection<PayloadTree> trees) {
            PayloadTree message = getSinglePayload(trees, Presence.MESSAGE_PATH);
            if (message != null) {
                Object data = message.getRoot().getPayloadElement().getData();
                if (!(data instanceof String)) {
                    throw new IllegalStateException("Payload not transformed yet");
                }
                setMessage((String) data);
            }
        }

        /*
         * The Status shown
         */
        private void initShow(Collection<PayloadTree> trees) {
            // final UNAVAILABLE Presence means user goes offline
            if (Presence.Type.UNAVAILABLE.equals(getType())) {
                setState(PresenceState.OFFLINE);
            } else {
                PayloadTree show = getSinglePayload(trees, Presence.STATUS_PATH);
                if (show != null) {
                    Object data = show.getRoot().getPayloadElement().getData();
                    if (!(data instanceof PresenceState)) {
                        throw new IllegalStateException("Payload not transformed yet");
                    }
                    setState((PresenceState) data);
                }
            }
        }

        /*
         * The Priority of the Stanza
         */
        private void initPriority(Collection<PayloadTree> trees) {
            PayloadTree priority = getSinglePayload(trees, Presence.PRIORITY_PATH);
            if (priority != null) {
                Object data = priority.getRoot().getPayloadElement().getData();
                if (!(data instanceof Byte)) {
                    throw new IllegalStateException("Payload not transformed yet");
                }
                setPriority((Byte) data);
            }
        }

        /**
         * @param presence The Presence Stanza to search in
         * @param elementPath The ElementPath of the PayloadTree we want
         * @return Null or the PayloadTree matching the ElementPath
         */
        private PayloadTree getSinglePayload(Collection<PayloadTree> trees, ElementPath elementPath) {
            PayloadTree candidate = null;
            for (PayloadTree tree : trees) {
                if (elementPath.equals(tree.getElementPath())) {
                    candidate = tree;
                    break;
                }
            }
            return candidate;
        }
    }

    @Override
    public void addPayload(PayloadTree tree) {
        ElementPath newRootPath = tree.getElementPath();
        if (defaultElements.contains(newRootPath)) {
            payloads.remove(newRootPath);
        }
        addPayloadToMap(tree, payloads);
    }

    /**
     * Initialize a Presence Builder
     * 
     * @return the Presence Builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Static {@link Builder} to create Presence Stanzas more fluently.
     * 
     * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
     */
    public final static class Builder {

        Presence presence;

        /**
         * Initializes a new {@link Builder} with an empty Presence that can then be configured via the Builder.
         */
        public Builder() {
            this.presence = new Presence();
        }

        /**
         * Set the sender of the Presence stanza.
         * 
         * @param from the sender of the Presence stanza.
         * @return the builder for further modification or building of the current Presence
         */
        public Builder from(ID from) {
            presence.setFrom(from);
            return this;
        }

        /**
         * Set the recipient of the Presence stanza
         * 
         * @param to the recipeint of the Presence stanza
         * @return the builder for further modification or building of the current Presence
         */
        public Builder to(ID to) {
            presence.setTo(to);
            return this;
        }

        /**
         * Set the error of the Presence stanza.
         * 
         * @param error the error of the Presence stanza.
         * @return the builder for further modification or building of the current Presence
         */
        public Builder error(RealtimeException error) {
            presence.setError(error);
            return this;
        }

        /**
         * Set the message of the Presence stanza.
         * 
         * @param message the message of the Presence stanza.
         * @return the builder for further modification or building of the current Presence
         */
        public Builder message(String message) {
            presence.setMessage(message);
            return this;
        }

        /**
         * Set the priority of the Presence stanza.
         * 
         * @param priority the priority of the Presence stanza.
         * @return the builder for further modification or building of the current Presence
         */
        public Builder priority(byte priority) {
            presence.setPriority(priority);
            return this;
        }

        /**
         * Set the state of the Presence stanza.
         * 
         * @param state the state of the Presence stanza.
         * @return the builder for further modification or building of the current Presence
         */
        public Builder state(PresenceState state) {
            presence.setState(state);
            return this;
        }

        /**
         * Set the type of the Presence stanza.
         * 
         * @param type the tyoe of the Presence stanza.
         * @return the builder for further modification or building of the current Presence
         */
        public Builder type(Type type) {
            presence.setType(type);
            return this;
        }

        /**
         * A valid minimal Presence(Initial Presence when coming online) only has to contain a sender.
         * 
         * @param presence The Presence to validate
         * @throws IllegalStateException when validation fails
         */
        private void validate() {
            if (presence.getFrom() == null) {
                throw new IllegalStateException("Presence is missing: from");
            }
        }

        /**
         * Validate and return the constructed Presence stanza.
         * 
         * @return the constructed Presence stanza
         * @throws IllegalStateException when validation of the configured presence object fails
         */
        public Presence build() {
            validate();
            return this.presence;
        }

    }

    @Override
    public Stanza newInstance() {
        return new Presence();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof Presence)) {
            return false;
        }
        Presence other = (Presence) obj;
        if (type != other.type) {
            return false;
        }
        return true;
    }

    
}
