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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.openexchange.realtime.payload.Payload;

/**
 * {@link Stanza} - Abstract information unit that can be send from one entity to another.
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public abstract class Stanza {

    // recipient and sender
    private ID to, from;

    // All 3 basic stanza types either have an optional or mandatory id field
    private String id;

    // All child elements of this Stanza
    protected List<Payload> payloads = new ArrayList<Payload>();
    
    // Extension elements of this Stanza, subset of payloads
    protected List<Payload> extensions = new ArrayList<Payload>();

    // The namespaces of child elements of this Stanza
    protected List<String> namespaces = new ArrayList<String>();

    /**
     * Initializes a new {@link Stanza}.
     */
    protected Stanza() {
        super();
    }

    /**
     * Gets the id
     * 
     * @return The id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the id
     * 
     * @param id The id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Get the {@link ID} describing the stanza's recipient.
     * 
     * @return null or the ID of the stanza's recipient
     */
    public ID getTo() {
        return to;
    }

    /**
     * Set the {@link ID} describing the Stanza's recipient.
     * 
     * @param to the ID of the stanza's recipient
     */
    public void setTo(final ID to) {
        this.to = to;
    }

    /**
     * Get the {@link ID} describing the Stanza's sender.
     * 
     * @return the {@link ID} describing the Stanza's sender.
     */
    public ID getFrom() {
        return from;
    }

    /**
     * Set the {@link ID} describing the Stanza's sender.
     * 
     * @param from the {@link ID} describing the Stanza's sender.
     */
    public void setFrom(final ID from) {
        this.from = from;
    }

    /**
     * Get a Set of namespaces of the payloads of this Stanza.
     * 
     * @return empty Set or the namespaces of the payloads of this Stanza.
     */
    public Set<String> getNamespaces() {
        return new HashSet<String>(namespaces);
    }

    /**
     * Add a Payload to the Stanza and keep track of the payload's namespace.
     * 
     * @param payload The Payload to add.
     * @return true if the Stanza didn't already contain the Payload
     */
    public abstract boolean addPayload(final Payload payload);

    /**
     * Remove a Payload from the Stanza and forget about its namespace.
     * 
     * @param payload The Payload to remove
     * @return true if the Stanza contained this Payload
     */
    public abstract boolean removePayload(final Payload payload);

    /**
     * Get an unmodifiable view of the structured information of this Stanza.
     * 
     * @return null or the structured information of this Stanza.
     */
    public List<Payload> getPayloads() {
        return Collections.unmodifiableList(payloads);
    }
    
    /**
     * Get all Payloads that aren't part of the basic Presence Schema.
     * 
     * @return A List of extension Payloads.
     */
    public List<Payload> getExtensions() {
        return extensions;
    }

}
