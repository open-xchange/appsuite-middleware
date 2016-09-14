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

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang.Validate;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.openexchange.exception.OXException;
import com.openexchange.realtime.exception.RealtimeException;
import com.openexchange.realtime.payload.PayloadElement;
import com.openexchange.realtime.payload.PayloadTree;
import com.openexchange.realtime.payload.PayloadTreeNode;
import com.openexchange.realtime.util.ElementPath;

/**
 * {@link Stanza} - Abstract information unit that can be send from one entity to another. Actual Data is held as leafs of a tree-like
 * structure identified by an ElementPath leading to that data. A Stanza can carry multiple of those trees, each again identified by an
 * Elementpath.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public abstract class Stanza implements Serializable {

    public static final String DEFAULT_SELECTOR = "default";

    public static final ElementPath ERROR_PATH = new ElementPath("error");

    private static final long serialVersionUID = 1L;

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Stanza.class);

    // recipient and sender
    private volatile ID to;
    private volatile ID from;
    private volatile ID sequencePrincipal;
    private volatile ID onBehalfOf;

    // All 3 basic stanza types either have an optional or mandatory id field
    private String id = "";

    /**
     * The error object for Presence Stanza of type error.
     */
    protected RealtimeException error = null;

    private String selector = DEFAULT_SELECTOR;

    private long sequenceNumber = -1;

    private String tracer;

    private final List<String> logEntries = new LinkedList<String>();

    // Payloads carried by this Stanza as n-ary trees
    protected volatile Map<ElementPath, List<PayloadTree>> payloads;
    
    protected int resendCount = 0;
    
    private Map<String, Object> channelAttributes = new HashMap<String, Object>();

    /**
     * Initializes a new {@link Stanza}.
     */
    protected Stanza() {
        super();
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
     * Get the error element describing the error-type Stanza in more detail.
     *
     * @return Null or the OXException representing the error
     */
    public RealtimeException getError() {
        return error;
    }

    /**
     * Set the error element describing the error-type Stanza in more detail.
     *
     * @param error The OXException representing the error
     */
    public void setError(RealtimeException error) {
        this.error = error;
        writeThrough(ERROR_PATH, error);
    }

    /**
     * Sets the onBehalfOf
     *
     * @param onBehalfOf The onBehalfOf to set
     */
    public void setOnBehalfOf(ID onBehalfOf) {
        this.onBehalfOf = onBehalfOf;
    }


    /**
     * Gets the onBehalfOf
     *
     * @return The onBehalfOf
     */
    public ID getOnBehalfOf() {
        return onBehalfOf;
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
     * @return The sequenceNumber, -1 if the sequenceNumber is invalid/not set
     */
    public long getSequenceNumber() {
        return sequenceNumber;
    }


    /**
     * Sets the sequencePrincipal
     *
     * @param sequencePrincipal The sequencePrincipal to set
     */
    public void setSequencePrincipal(ID sequencePrincipal) {
        this.sequencePrincipal = sequencePrincipal;
    }


    /**
     * Gets the sequencePrincipal
     *
     * @return The SequencePrincipal (ID) set via setSequencePrincipal or the SequencePrincipal of the sender ID
     */
    public ID getSequencePrincipal() {
        return (sequencePrincipal != null) ? sequencePrincipal : (onBehalfOf != null) ? onBehalfOf : from;
    }

    /**
     * Get a List of namespaces of the payloads of this Stanza.
     *
     * @return Empty Set or the namespaces of the payloads of this Stanza.
     */
    public Collection<ElementPath> getElementPaths() {
        Set<ElementPath> paths = new HashSet<ElementPath>();
        for (PayloadTree tree : getPayloadTrees()) {
            paths.addAll(tree.getElementPaths());
        }
        return paths;
    }

    /**
     * Get all Payloads of this Stanza.
     *
     * @return A List of PayloadTrees.
     */
    public Collection<PayloadTree> getPayloadTrees() {
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
        Map<ElementPath, List<PayloadTree>> newPayloads = new ConcurrentHashMap<ElementPath, List<PayloadTree>>();
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
     * Gets the resendCount
     *
     * @return The resendCount
     */
    public int getResendCount() {
        return resendCount;
    }
    
    /**
     * Sets the resendCount
     *
     * @param resendCount The resendCount to set
     */
    public void setResendCount(int resendCount) {
        this.resendCount = resendCount;
    }

    /**
     * Add a payload to this Stanza.
     *
     * @param tree The PayloadTreeNoode to add to this Stanza
     * @return true if the PayloadTreeNode could be added to this Stanza
     */
    public abstract void addPayload(final PayloadTree tree);

    /**
     * Add a PayloadTree into a Map containing lists of PayloadTrees mapped to their ElementPaths.
     *
     * @param tree The tree to add
     * @param payloadTreeMap The Map containing the trees
     */
    protected void addPayloadToMap(PayloadTree tree, Map<ElementPath, List<PayloadTree>> payloadTreeMap) {
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
     * Get a Collection of Payloadtrees that match an ElementPath
     *
     * @param elementPath The Elementpath identifying the Payload
     * @return A Collection of PayloadTrees
     */
    @SuppressWarnings("unchecked")
    public Collection<PayloadTree> getPayloadTrees(final ElementPath elementPath) {
        List<PayloadTree> list = payloads.get(elementPath);
        if (list == null) {
            list = Collections.EMPTY_LIST;
        }

        return list;
    }

    /**
     * Filter the payload trees based on a Predicate.
     *
     * @param predicate
     * @return Payloads matching the Predicate or an empty Collection
     */
    public Collection<PayloadTree> filterPayloadTrees(Predicate<PayloadTree> predicate) {
        Collection<PayloadTree> result = new ArrayList<PayloadTree>();
        for (PayloadTree element : getPayloadTrees()) {
            if (predicate.apply(element)) {
                result.add(element);
            }
        }
        return result;
    }

    /**
     *
     * @param nodePaths
     * @return
     */
    public Collection<PayloadElement> filterPayloadElements(ElementPath... nodePaths) {
        Validate.notEmpty(nodePaths, "Mandatory parameter nodePath is missing.");
        return filterPayloadElements(getPayloadTrees(), nodePaths);
    }

    /**
     * Filter matching PayloadElements from a PayloadTree
     *
     * @param treePath The {@link ElementPath} identifying the PayloadTree
     * @param nodePaths The {@link ElementPath} identifying the matching PayloadElements
     * @return
     */
    public Collection<PayloadElement> filterPayloadElementsFromTree(ElementPath treePath, ElementPath... nodePaths) {
        Validate.notNull(treePath, "Mandatory parameter treePath is missing.");
        Validate.notEmpty(nodePaths, "Mandatory parameter nodePath is missing.");
        return filterPayloadElements(getPayloadTrees(treePath), nodePaths);
    }

    /**
     * Filter matching PayloadElements
     * @param trees
     * @param nodePaths
     * @return
     */
    private Collection<PayloadElement> filterPayloadElements(Collection<PayloadTree> trees, ElementPath... nodePaths) {
        Set<PayloadElement> matchingElements = new HashSet<PayloadElement>();
        if(trees.isEmpty() || nodePaths.length == 0) {
            return matchingElements;
        }
        for (PayloadTree tree : trees) {
            for (ElementPath elementPath : nodePaths) {
                Collection<PayloadTreeNode> matchingNodes = tree.search(elementPath);
                //PayloadTreeNodes contain the actual PayloadElements we are interested in plus tree metadata
                for (PayloadTreeNode payloadTreeNode : matchingNodes) {
                    matchingElements.add(payloadTreeNode.getPayloadElement());
                }
            }
        }
        return matchingElements;
    }

    /**
     * Filter a single payload from this {@link Stanza} based only on the {@link ElementPath} of the wanted payload. This will search in all
     * of
     * this {@link Stanza}'s {@link PayloadTree}s.
     *
     * @param elementPath The {@link ElementPath} of the wanted payload
     * @param clazz The {@link Class} of the wanted Payload
     * @return An {@link Optional} containing the single Payload or an empty {@link Optional} if the Stanza did contain exactly one matching
     *         Payload.
     */
    public <T> Optional<T> getSinglePayload(ElementPath elementPath, Class<T> clazz) {
        return getSinglePayload0(null, elementPath, clazz);
    }

    /**
     * Filter a single Payload from this {@link Stanza}'s {@link PayloadTree} based on the {@link ElementPath}s of the wanted Payload and
     * the {@link PayloadTree} to search.
     *
     * @param treePath The {@link ElementPath} identifying a {@link PayloadTree} within this {@link Stanza} that should be searched
     * @param elementPath The {@link ElementPath} of the wanted Payload
     * @param clazz The {@link Class} of the wanted Payload
     * @return An {@link Optional} containing the single Payload or an empty {@link Optional} if the Stanza did contain exactly one matching
     *         Payload.
     */
    public <T> Optional<T> getSinglePayload(ElementPath treePath, ElementPath elementPath, Class<T> clazz) {
        return getSinglePayload0(treePath, elementPath, clazz);
    }

    private <T> Optional<T> getSinglePayload0(ElementPath treePath, ElementPath elementPath, Class<T> clazz) {
        Optional<T> retval = Optional.absent();

        Collection<PayloadElement> filteredPayloadElements = null;
        if(treePath == null) {
            filteredPayloadElements = filterPayloadElements(elementPath);
        } else {
            filteredPayloadElements = filterPayloadElements(getPayloadTrees(treePath), elementPath);
        }
        int numResults = filteredPayloadElements.size();
        if (numResults != 1) {
            LOG.debug("Was expecting a single {} payload but found {} within the Stanza. Returning absent Optional instead.", elementPath, numResults);
            return retval;
        }
        Object data = filteredPayloadElements.iterator().next().getData();
        if (clazz.isInstance(data)) {
            retval = Optional.of(clazz.cast(data));
        } else {
            LOG.warn("Was expecting a payload  of class \"{}\", but found {} within the Stanza. Returning absent Optional instead.", clazz.getName(), data == null ? "null" : data.getClass().getName());
        }
        return retval;
    }

    /**
     * Return a Map<ElementPath, List<PayloadTree>> containing deep copies of this stanza's payloads.
     *
     * @return a Map<ElementPath, List<PayloadTree>> containing deep copies of this stanza's payloads.
     */
    protected Map<ElementPath, List<PayloadTree>> deepCopyPayloads() {
        HashMap<ElementPath, List<PayloadTree>> copiedPayloads = new HashMap<ElementPath, List<PayloadTree>>();
        for (PayloadTree tree : getPayloadTrees()) {
            PayloadTree copiedTree = new PayloadTree(tree);
            addPayloadToMap(copiedTree, copiedPayloads);
        }
        return copiedPayloads;
    }

    /**
     * Gets the selector that is used to identify GroupDispatcher instances on the server side.
     * Example: If you join a chatroom or a collaboratively edited document yo may receive messages from this chatroom. Those messages will
     * contain the sender of the message and a selector that idenifies the chatroom that distributed the message to you. Clients have to
     * choose a selector when joining a chatroom and have to take care of the mapping selector <-> chatroom themselves.
     *
     * @return the selector
     */
    public String getSelector() {
        return selector;
    }

    /**
     * Sets the selector that is used to identify GroupDispatcher instances on the server side.
     * Example: If you join a chatroom or a collaboratively edited document yo may receive messages from this chatroom. Those messages will
     * contain the sender of the message and a selector that idenifies the chatroom that distributed the message to you. Clients have to
     * choose a selector when joining a chatroom and have to take care of the mapping selector <-> chatroom themselves.
     *
     * @param selector the selector
     */
    public void setSelector(String selector) {
        this.selector = selector;
    }

    public void setTracer(String tracer) {
        this.tracer = tracer;
    }

    public String getTracer() {
        return tracer;
    }

    public boolean traceEnabled() {
        return tracer != null;
    }

    public void trace(Object trace) {
        if (traceEnabled()) {
            StringBuilder sb = new StringBuilder(tracer)
                .append(": ")
                .append(trace)
                .append(", from: ")
                .append(getFrom())
                .append(", to: ")
                .append(getTo())
                .append(", selector: ")
                .append(getSelector());
            LOG.info(sb.toString());
//            logEntries.add(trace.toString());
        }
    }

    public void trace(Object trace, Throwable t) {
        if (traceEnabled()) {
            StringBuilder sb = new StringBuilder(tracer)
                .append(": ")
                .append(trace)
                .append(", from: ")
                .append(getFrom())
                .append(", to: ")
                .append(getTo())
                .append(", selector: ")
                .append(getSelector());
            LOG.info(sb.toString(), t);
            StringWriter w = new StringWriter();
            t.printStackTrace(new PrintWriter(w));
//            logEntries.add(trace.toString());
//            logEntries.add(w.toString());
        }
    }

    public void addLogMessages(List<String> logEntries) {
        this.logEntries.addAll(logEntries);
    }



    public List<String> getLogEntries() {
        return logEntries;
    }

    public void transformPayloads(String format) throws OXException {
        List<PayloadTree> copy = new ArrayList<PayloadTree>(getPayloadTrees().size());
        for (PayloadTree tree : getPayloadTrees()) {
            tree = tree.toExternal(format);
            copy.add(tree);
        }
        setPayloads(copy);
    }

    public void transformPayloadsToInternal() throws OXException {
        List<PayloadTree> copy = new ArrayList<PayloadTree>(getPayloadTrees().size());
        for (PayloadTree tree : getPayloadTrees()) {
            tree = tree.toInternal();
            copy.add(tree);
        }
        setPayloads(copy);
    }
    
    public Object getChannelAttribute(String name) {
    	return channelAttributes.get(name);
    }
    
    public void setChannelAttribute(String name, Object o) {
    	channelAttributes.put(name, o);
    }

    /**
     * Write a payload to the PayloadTree identified by the ElementPath. There is only one tree for the default elements which only contains
     * one node so we can set the data by directly writing to the root node.
     *
     * @param path The ElementPath identifying the PayloadTree.
     * @param data The payload data to write into the root node.
     */
    protected void writeThrough(ElementPath path, Object data) {
        if (data == null) {
            Collection<PayloadTree> payloadTrees = getPayloadTrees(path);
            if (payloadTrees.size() == 1) {
                removePayload(payloadTrees.iterator().next());
            } else {
                throw new IllegalStateException("Number of basic elementPaths should have been equal to 1.");
            }
        } else {
            List<PayloadTree> payloadTrees = payloads.get(path);
            if (payloadTrees == null) {
                payloadTrees = new ArrayList<PayloadTree>();
            }
            if (payloadTrees.size() > 1) {
                throw new IllegalStateException("Stanza shouldn't contain more than one PayloadTree per basic ElementPath");
            }
            PayloadTree tree;
            if (payloadTrees.isEmpty()) {
                PayloadElement payloadElement = new PayloadElement(
                    data,
                    data.getClass().getSimpleName(),
                    path.getNamespace(),
                    path.getElement());
                PayloadTreeNode payloadTreeNode = new PayloadTreeNode(payloadElement);
                tree = new PayloadTree(payloadTreeNode);
                addPayload(tree);
            } else {
                tree = payloadTrees.get(0);
                PayloadTreeNode node = tree.getRoot();
                if (node == null) {
                    throw new IllegalStateException("PayloadTreeNode removed? This shouldn't happen!");
                }
                node.setData(data, data.getClass().getSimpleName());
            }
        }
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
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Stanza)) {
            return false;
        }
        Stanza other = (Stanza) obj;
        final ID thisFrom = from;
        if (thisFrom == null) {
            if (other.from != null) {
                return false;
            }
        } else if (!thisFrom.equals(other.from)) {
            return false;
        }
        final String thisId = id;
        if (thisId == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!thisId.equals(other.id)) {
            return false;
        }
        final Map<ElementPath, List<PayloadTree>> thisPayloads = payloads;
        if (thisPayloads == null) {
            if (other.payloads != null) {
                return false;
            }
        } else if (!thisPayloads.equals(other.payloads)) {
            return false;
        }
        final String thisSelector = selector;
        if (thisSelector == null) {
            if (other.selector != null) {
                return false;
            }
        } else if (!thisSelector.equals(other.selector)) {
            return false;
        }
        final ID thisTo = to;
        if (thisTo == null) {
            if (other.to != null) {
                return false;
            }
        } else if (!thisTo.equals(other.to)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        for(Entry<ElementPath, List<PayloadTree>> entry : payloads.entrySet()) {
            sb.append("\n").append(entry.getKey()).append(":");
            for(PayloadTree tree : entry.getValue()) {
                sb.append("\t\n").append(tree);
            }
            sb.append("\n");
        }
        return "\nFrom: " + from + "\nTo: " + to + "\nPayloads:\n" + sb.toString();
    }
}
