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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import com.google.common.base.Predicate;
import com.openexchange.exception.OXException;
import com.openexchange.realtime.payload.PayloadElement;
import com.openexchange.realtime.payload.PayloadTree;
import com.openexchange.realtime.util.ElementPath;

/**
 * {@link Stanza} - Abstract information unit that can be send from one entity to another.
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public abstract class Stanza implements Serializable {

    private static final long serialVersionUID = 1L;

    // recipient and sender
    private ID to, from;

    // All 3 basic stanza types either have an optional or mandatory id field
    private String id = "";

    private String selector = "default";
    
    private long sequenceNumber = -1;

    // Payloads carried by this Stanza as n-ary trees
    Map<ElementPath, List<PayloadTree>> payloads;

    /**
     * Initializes a new {@link Stanza}.
     */
    protected Stanza() {
        payloads = new ConcurrentHashMap<ElementPath, List<PayloadTree>>();
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
    public void setId(final String id) {
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
     * Sets the sequenceNumber
     *
     * @param sequenceNumber The sequenceNumber to set
     */
    public void setSequenceNumber(long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }
    
    
    /**
     * Gets the sequenceNumber
     *
     * @return The sequenceNumber
     */
    public long getSequenceNumber() {
        return sequenceNumber;
    }

    /**
     * Get a List of namespaces of the payloads of this Stanza.
     * 
     * @return Empty Set or the namespaces of the payloads of this Stanza.
     */
    public Collection<ElementPath> getElementPaths() {
        Set<ElementPath> paths = new HashSet<ElementPath>();
        for (PayloadTree tree : getPayloads()) {
            paths.addAll(tree.getElementPaths());
        }
        return paths;
    }

    /**
     * Get all Payloads of this Stanza.
     * 
     * @return A List of PayloadTrees.
     */
    public Collection<PayloadTree> getPayloads() {
        ArrayList<PayloadTree> resultList = new ArrayList<PayloadTree>();
        Collection<List<PayloadTree>> values = payloads.values();
        for (List<PayloadTree> list : values) {
            resultList.addAll(list);
        }
        return resultList;
    }

    /**
     * A very common case: Get the single payload contained in this Stanza.
     * 
     * @return null if the Stanza doesn't contain a Payload, otherwise the Payload 
     */
    public PayloadElement getPayload() {
        Iterator<List<PayloadTree>> iterator = payloads.values().iterator();
        if (iterator.hasNext()) {
            return iterator.next().get(0).getRoot().getPayloadElement();
        }

        return null;
    }

    /**
     * Set all Payloads of this Stanza.
     * 
     * @param payloadTrees The PayloadTrees forming the Payloads.
     */
    public void setPayloads(Collection<PayloadTree> payloadTrees) {
        HashMap<ElementPath, List<PayloadTree>> newPayloads = new HashMap<ElementPath, List<PayloadTree>>();
        for (PayloadTree tree : payloadTrees) {
            ElementPath elementPath = tree.getRoot().getElementPath();
            List<PayloadTree> list = newPayloads.get(elementPath);
            if (list == null) {
                list = new ArrayList<PayloadTree>();
            }
            list.add(tree);
            newPayloads.put(tree.getElementPath(), list);
        }
        payloads = newPayloads;
    }

    /**
     * Add a payload to this Stanza.
     * 
     * @param tree The PayloadTreeNoode to add to this Stanza
     * @return true if the PayloadTreeNode could be added to this Stanza
     */
    public void addPayload(final PayloadTree tree) {
        addPayloadToMap(tree, this.payloads);
    }

    /**
     * Add a PayloadTree into a Map containing lists of PayloadTrees mapped to their ElementPaths.
     * 
     * @param tree The tree to add
     * @param payloadTreeMap The Map containing the trees
     */
    private void addPayloadToMap(PayloadTree tree, Map<ElementPath, List<PayloadTree>> payloadTreeMap) {
        ElementPath elementPath = tree.getElementPath();
        List<PayloadTree> list = payloadTreeMap.get(elementPath);
        if (list == null) {
            list = new ArrayList<PayloadTree>();
        }
        list.add(tree);
        payloadTreeMap.put(tree.getElementPath(), list);
    }

    /**
     * Remove a PayloadTree from this Stanza.
     * 
     * @param tree The PayloadTree to remove from this Stanza
     */
    public void removePayload(final PayloadTree tree) {
        ElementPath elementPath = tree.getElementPath();
        List<PayloadTree> list = payloads.get(elementPath);
        if (list != null) {
            list.remove(tree);
            payloads.put(elementPath, list);
        }
    }

    /**
     * Get a Collection of Payloads that match an ElementPath
     * 
     * @param elementPath The Elementpath identifying the Payload
     * @return A Collection of PayloadTrees
     */
    @SuppressWarnings("unchecked")
    public Collection<PayloadTree> getPayloads(final ElementPath elementPath) {
        List<PayloadTree> list = payloads.get(elementPath);
        if (list == null) {
            list = Collections.EMPTY_LIST;
        }

        return list;
    }

    /**
     * Filter the payloads based on a Predicate.
     * 
     * @param predicate
     * @return Payloads matching the Predicate or an empty Collection
     */
    public Collection<PayloadTree> filterPayloads(Predicate<PayloadTree> predicate) {
        Collection<PayloadTree> result = new ArrayList<PayloadTree>();
        for (PayloadTree element : getPayloads()) {
            if (predicate.apply(element)) {
                result.add(element);
            }
        }
        return result;
    }

    /**
     * Return a Map<ElementPath, List<PayloadTree>> containing deep copies of this stanza's payloads.
     * 
     * @return a Map<ElementPath, List<PayloadTree>> containing deep copies of this stanza's payloads.
     */
    protected Map<ElementPath, List<PayloadTree>> deepCopyPayloads() {
        HashMap<ElementPath, List<PayloadTree>> copiedPayloads = new HashMap<ElementPath, List<PayloadTree>>();
        for (PayloadTree tree : getPayloads()) {
            PayloadTree copiedTree = new PayloadTree(tree);
            addPayloadToMap(copiedTree, copiedPayloads);
        }
        return copiedPayloads;
    }

    public String getSelector() {
        return selector;
    }

    public void setSelector(String selector) {
        this.selector = selector;
    }

    public void transformPayloads(String format) throws OXException {
        List<PayloadTree> copy = new ArrayList<PayloadTree>(getPayloads().size());
        for (PayloadTree tree : getPayloads()) {
            tree = tree.toExternal(format);
            copy.add(tree);
        }
        setPayloads(copy);
    }

    public void transformPayloadsToInternal() throws OXException {
        List<PayloadTree> copy = new ArrayList<PayloadTree>(getPayloads().size());
        for (PayloadTree tree : getPayloads()) {
            tree = tree.toInternal();
            copy.add(tree);
        }
        setPayloads(copy);
    }

    /**
     * Init default fields from values found in the PayloadTrees of the Stanza.
     * 
     * @throws OXException when the Stanza couldn't be initialized
     */
    public abstract void initializeDefaults() throws OXException;

    public abstract Stanza newInstance();

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((from == null) ? 0 : from.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((payloads == null) ? 0 : payloads.hashCode());
        result = prime * result + ((selector == null) ? 0 : selector.hashCode());
        result = prime * result + ((to == null) ? 0 : to.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof Stanza))
            return false;
        Stanza other = (Stanza) obj;
        if (from == null) {
            if (other.from != null)
                return false;
        } else if (!from.equals(other.from))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (payloads == null) {
            if (other.payloads != null)
                return false;
        } else if (!payloads.equals(other.payloads))
            return false;
        if (selector == null) {
            if (other.selector != null)
                return false;
        } else if (!selector.equals(other.selector))
            return false;
        if (to == null) {
            if (other.to != null)
                return false;
        } else if (!to.equals(other.to))
            return false;
        return true;
    }
    
    @Override
    public String toString() {

        return "From: " + from + "\nTo: " + to + "\nPayloads:\n" + payloads;
    }

}
