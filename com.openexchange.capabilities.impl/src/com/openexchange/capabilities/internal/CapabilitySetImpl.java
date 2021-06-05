/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.capabilities.internal;

import static com.openexchange.java.Autoboxing.I;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.capabilities.Capability;
import com.openexchange.capabilities.CapabilitySet;
import com.openexchange.capabilities.internal.Operation.Type;
import com.openexchange.java.Strings;

/**
 * {@link CapabilitySetImpl} - A capability set.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CapabilitySetImpl implements CapabilitySet {

    private static final long serialVersionUID = -7226304751781497934L;

    /** The capability map */
    private final transient Map<String, Capability> capabilities;

    /** The history map */
    private final transient ConcurrentMap<CapabilitySource, History> histories;

    /**
     * Initializes a new {@link CapabilitySetImpl}.
     *
     * @param capacity The initial capacity
     */
    public CapabilitySetImpl(final int capacity) {
        super();
        capabilities = new ConcurrentHashMap<String, Capability>(capacity);
        histories = new ConcurrentHashMap<>(6, 0.9F, 1);
    }

    private CapabilitySetImpl(CapabilitySetImpl source) {
        super();
        Map<String, Capability> m = source.capabilities;
        capabilities = null == m ? null : new ConcurrentHashMap<String, Capability>(m);
        ConcurrentMap<CapabilitySource, History> h = source.histories;
        histories = null == h ? null : new ConcurrentHashMap<>(h);
    }

    @Override
    public CapabilitySetImpl clone() {
        return new CapabilitySetImpl(this);
    }

    /**
     * Adds given operation associated with specified capability source to this set's history.
     *
     * @param operation The operation to add
     * @param source The capability source associated with given operation
     */
    private void addToHistory(Operation operation, CapabilitySource source) {
        CapabilitySource src = source == null ? CapabilitySource.PROGRAMMATIC : source;
        History history = histories.get(src);
        if (history == null) {
            History newHistory = new History();
            history = histories.putIfAbsent(src, newHistory);
            if (history == null) {
                history = newHistory;
            }
        }
        history.addOperation(operation);
    }

    /**
     * Gets the size
     *
     * @return The size
     */
    @Override
    public int size() {
        return capabilities.size();
    }

    /**
     * Checks if set is empty
     *
     * @return <code>true</code> if empty; else <code>false</code>
     */
    @Override
    public boolean isEmpty() {
        return capabilities.isEmpty();
    }

    /**
     * Checks for presence of given capability.
     *
     * @param capability The capability to look for
     * @return <code>true</code> if contained; else <code>false</code>
     */
    @Override
    public boolean contains(final Capability capability) {
        return null == capability ? false : capabilities.containsKey(capability.getId());
    }

    /**
     * Checks for presence of denoted capability.
     *
     * @param id The capability identifier to look for
     * @return <code>true</code> if contained; else <code>false</code>
     */
    @Override
    public boolean contains(final String id) {
        return null == id ? false : capabilities.containsKey(id);
    }

    /**
     * Gets the capability identifies by the supplied ID.
     *
     * @param id The capability identifier to look for
     * @return The capability, or <code>null</code> if not found
     */
    @Override
    public Capability get(final String id) {
        return null == id ? null : capabilities.get(id);
    }

    /**
     * Gets an iterator for capabilities.
     *
     * @return An iterator for capabilities
     */
    @Override
    public Iterator<Capability> iterator() {
        return capabilities.values().iterator();
    }

    @Override
    public boolean add(final Capability capability) {
        return add(capability, null);
    }

    /**
     * Adds given capability.
     *
     * @param capability The capability to add
     * @param source The capability source
     * @return <code>true</code> if set changed; otherwise <code>false</code> if already contained
     */
    boolean add(final Capability capability, final CapabilitySource source) {
        return add(capability, source, null);
    }

    /**
     * Adds given capability.
     *
     * @param capability The capability to add
     * @param source The capability source
     * @param optionalReason The optional reason string; may be <code>null</code>
     * @return <code>true</code> if set changed; otherwise <code>false</code> if already contained
     */
    boolean add(final Capability capability, final CapabilitySource source, String optionalReason) {
        if (null == capability) {
            return false;
        }

        boolean changed = null == capabilities.put(capability.getId(), capability);
        if (changed) {
            addToHistory(Operation.addingOperation(capability.getId(), Optional.ofNullable(optionalReason)), source);
        } else {
            addToHistory(Operation.noopAddingOperation(capability.getId(), Optional.ofNullable(optionalReason)), source);
        }
        return changed;
    }

    @Override
    public boolean remove(final Capability capability) {
        return remove(capability, null);
    }

    /**
     * Removes the given capability.
     *
     * @param capability The capability
     * @param source The capability source
     * @return <code>true</code> if removed; otherwise <code>false</code> if no such capability was contained
     */
    boolean remove(final Capability capability, final CapabilitySource source) {
        return remove(capability, source, null);
    }

    /**
     * Removes the given capability.
     *
     * @param capability The capability
     * @param source The capability source
     * @param optionalReason The optional reason string; may be <code>null</code>
     * @return <code>true</code> if removed; otherwise <code>false</code> if no such capability was contained
     */
    boolean remove(final Capability capability, final CapabilitySource source, String optionalReason) {
        if (null == capability) {
            return false;
        }

        return remove(capability.getId(), source, optionalReason);
    }

    @Override
    public boolean remove(final String id) {
        return remove(id, null);
    }

    /**
     * Removes the denoted capability.
     *
     * @param id The capability identifier
     * @param source The capability source
     * @return <code>true</code> if removed; otherwise <code>false</code> if no such capability was contained
     */
    boolean remove(final String id, final CapabilitySource source) {
        return remove(id, source, null);
    }

    /**
     * Removes the denoted capability.
     *
     * @param id The capability identifier
     * @param source The capability source
     * @param optionalReason The optional reason string; may be <code>null</code>
     * @return <code>true</code> if removed; otherwise <code>false</code> if no such capability was contained
     */
    boolean remove(final String id, final CapabilitySource source, String optionalReason) {
        if (null == id) {
            return false;
        }

        boolean removed = null != capabilities.remove(id);
        if (removed) {
            addToHistory(Operation.removingOperation(id, Optional.ofNullable(optionalReason)), source);
        } else {
            addToHistory(Operation.noopRemovingOperation(id, Optional.ofNullable(optionalReason)), source);
        }
        return removed;
    }

    /**
     * Clears this set.
     */
    @Override
    public void clear() {
        capabilities.clear();
        histories.clear();
    }

    /**
     * Gets the history per capability source for this capability set
     *
     * @return The histories
     */
    public ConcurrentMap<CapabilitySource, History> getHistories() {
        return histories;
    }

    /**
     * Prints the history of this capability set to given logger using <code>DEBUG</code> log level on behalf of specified user.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param logger The logger to print to
     */
    public void printHistoryFor(int userId, int contextId, org.slf4j.Logger logger) {
        if (userId <= 0 || contextId <= 0) {
            return;
        }

        StringBuilder logMessage = new StringBuilder();
        List<Object> args = new ArrayList<>();

        logMessage.append("{}");
        args.add(Strings.getLineSeparator());

        logMessage.append("Capabilities of user {} in context {}:{}");
        args.add(I(userId));
        args.add(I(contextId));
        args.add(Strings.getLineSeparator());

        logMessage.append("    {}{}{}");
        args.add(toString());
        args.add(Strings.getLineSeparator());
        args.add(Strings.getLineSeparator());

        logMessage.append("History for these capabilities:{}");
        args.add(Strings.getLineSeparator());

        ConcurrentMap<CapabilitySource, History> histories = getHistories();
        for (CapabilitySource source : CapabilitySource.values()) {
            History history = histories.get(source);
            if (history != null && !history.isEmpty()) {
                // E.g. From CONFIGURATION: ADDED:[some-added-capabilities], REMOVED:[some-removed-capabilities]
                logMessage.append(" \u2023 From {}:{}");
                args.add(Strings.asciiLowerCase(source.toString()));
                args.add(Strings.getLineSeparator());

                for (Map.Entry<Type, List<Operation>> entry : history.getGroupedOperations().entrySet()) {
                    logMessage.append("    - {}: {}{}");

                    args.add(entry.getKey().getIdentifier());
                    args.add(entry.getValue());
                    args.add(Strings.getLineSeparator());
                }

                logMessage.append("{}");
                args.add(Strings.getLineSeparator());
            }
        }

        logMessage.append("Legend:{}");
        args.add(Strings.getLineSeparator());
        for (Type type : Type.values()) {
            logMessage.append(" \u2023 {}: {}{}");
            args.add(type.getIdentifier());
            args.add(type.getDescription());
            args.add(Strings.getLineSeparator());
        }

        logger.debug(logMessage.toString(), args.toArray(new Object[args.size()]));
    }

    /**
     * Creates the {@link Set set} view for this capability set.
     * <p>
     * Changes to returned set are <b>not</b> reflected in this capability set.
     *
     * @return The {@link Set set} view for this capability set
     */
    @Override
    public Set<Capability> asSet() {
        return new HashSet<Capability>(capabilities.values());
    }

    @Override
    public String toString() {
        return new TreeSet<Capability>(capabilities.values()).toString();
    }

}
