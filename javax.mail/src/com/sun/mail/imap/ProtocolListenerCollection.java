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

package com.sun.mail.imap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * {@link ProtocolListenerCollection} - A collection for {@code ProtocolListener} instances.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public abstract class ProtocolListenerCollection implements Iterable<ProtocolListener> {

    /**
     * Creates a new {@code ProtocolListenerCollection} instance.
     *
     * @param protocolListener The initial protocol listener to add
     * @return The new collection
     */
    public static ProtocolListenerCollection newCollection(ProtocolListener protocolListener) {
        return new ConcurrentProtocolListenerCollection(protocolListener);
    }

    // ----------------------------------------------------------------------------------------------------------------

    /**
     * Initializes a new {@link ProtocolListenerCollection}.
     */
    protected ProtocolListenerCollection() {
        super();
    }

    /**
     * Adds specified  protocol listener to this collection.
     *
     * @param protocolListener The protocol listener to add
     */
    public abstract void add(ProtocolListener protocolListener);

    /**
     * Removes specified  protocol listener from this collection.
     *
     * @param protocolListener The protocol listener to remove
     * @return <code>true</code> if last protocol listener has been removed (and thus this collection is empty now); otherwise <code>false</code>
     */
    public abstract boolean remove(ProtocolListener protocolListener);

    /**
     * Checks if this collection is empty.
     *
     * @return <code>true</code> if empty; otherwise <code>false</code>
     */
    public abstract boolean isEmpty();

    @Override
    public Iterator<ProtocolListener> iterator() {
        return protocolListeners();
    }

    /**
     * Gets an iterator over contained {@code ProtocolListener} instances.
     *
     * @return The iterator
     */
    public abstract Iterator<ProtocolListener> protocolListeners();

    /**
     * Creates a snapshot from this collection.
     * <p>
     * Modifications to the snapshot are <b>not</b> reflected to this collection.
     *
     * @return The snapshot
     */
    public abstract ProtocolListenerCollection snapshot();

    // ------------------------------------------------ Implementations ------------------------------------------------

    private static class ConcurrentProtocolListenerCollection extends ProtocolListenerCollection {

        private final List<ProtocolListener> protocolListeners;

        /**
         * Initializes a new {@link ProtocolListenerCollection}.
         *
         * @param protocolListener The initial protocol listener to add
         */
        ConcurrentProtocolListenerCollection(ProtocolListener protocolListener) {
            super();
            protocolListeners = new CopyOnWriteArrayList<>(new ProtocolListener[] { protocolListener });
        }

        @Override
        public void add(ProtocolListener protocolListener) {
            protocolListeners.add(protocolListener);
        }

        @Override
        public boolean remove(ProtocolListener protocolListener) {
            boolean removed = protocolListeners.remove(protocolListener);
            return removed && protocolListeners.isEmpty();
        }

        @Override
        public boolean isEmpty() {
            return protocolListeners.isEmpty();
        }

        @Override
        public Iterator<ProtocolListener> protocolListeners() {
            return protocolListeners.iterator();
        }

        @Override
        public ProtocolListenerCollection snapshot() {
            return new SnapshotProtocolListenerCollection(protocolListeners);
        }
    }

    private static class SnapshotProtocolListenerCollection extends ProtocolListenerCollection {

        private final List<ProtocolListener> protocolListeners;

        /**
         * Initializes a new {@link SnapshotProtocolListenerCollection}.
         */
        SnapshotProtocolListenerCollection(List<ProtocolListener> protocolListeners) {
            super();
            this.protocolListeners = new ArrayList<>(protocolListeners);
        }

        @Override
        public void add(ProtocolListener protocolListener) {
            protocolListeners.add(protocolListener);
        }

        @Override
        public boolean remove(ProtocolListener protocolListener) {
            boolean removed = protocolListeners.remove(protocolListener);
            return removed && protocolListeners.isEmpty();
        }

        @Override
        public boolean isEmpty() {
            return protocolListeners.isEmpty();
        }

        @Override
        public Iterator<ProtocolListener> protocolListeners() {
            return protocolListeners.iterator();
        }

        @Override
        public ProtocolListenerCollection snapshot() {
            return new SnapshotProtocolListenerCollection(protocolListeners);
        }
    }

}
