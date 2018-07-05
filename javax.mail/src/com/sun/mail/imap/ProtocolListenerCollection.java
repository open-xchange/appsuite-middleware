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
     * Gets an iterator over contained {@code CommandListener} instances.
     *
     * @return The iterator
     */
    public abstract Iterator<CommandListener> commandListeners();

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
        private final List<CommandListener> commandListeners;

        /**
         * Initializes a new {@link ProtocolListenerCollection}.
         *
         * @param protocolListener The initial protocol listener to add
         */
        ConcurrentProtocolListenerCollection(ProtocolListener protocolListener) {
            super();
            protocolListeners = new CopyOnWriteArrayList<>(new ProtocolListener[] { protocolListener });
            if (protocolListener instanceof CommandListener) {
                commandListeners = new CopyOnWriteArrayList<>(new CommandListener[] { (CommandListener) protocolListener });
            } else {
                commandListeners = new CopyOnWriteArrayList<>();
            }
        }

        @Override
        public void add(ProtocolListener protocolListener) {
            boolean added = protocolListeners.add(protocolListener);
            if (added && (protocolListener instanceof CommandListener)) {
                commandListeners.add((CommandListener) protocolListener);
            }
        }

        @Override
        public boolean remove(ProtocolListener protocolListener) {
            boolean removed = protocolListeners.remove(protocolListener);
            if (removed && (protocolListener instanceof CommandListener)) {
                commandListeners.remove(protocolListener);
            }
            return removed && protocolListeners.isEmpty();
        }

        @Override
        public boolean isEmpty() {
            return protocolListeners.isEmpty();
        }

        @Override
        public Iterator<ProtocolListener> protocolListeners() {
            return new ProtocolListenersIterator(protocolListeners.iterator(), commandListeners);
        }

        @Override
        public Iterator<CommandListener> commandListeners() {
            return new CommandListenersIterator(commandListeners.iterator(), protocolListeners);
        }

        @Override
        public ProtocolListenerCollection snapshot() {
            return new SnapshotProtocolListenerCollection(protocolListeners, commandListeners);
        }
    }

    private static class SnapshotProtocolListenerCollection extends ProtocolListenerCollection {

        private final List<ProtocolListener> protocolListeners;
        private final List<CommandListener> commandListeners;

        /**
         * Initializes a new {@link SnapshotProtocolListenerCollection}.
         */
        SnapshotProtocolListenerCollection(List<ProtocolListener> protocolListeners, List<CommandListener> commandListeners) {
            super();
            this.protocolListeners = new ArrayList<>(protocolListeners);
            this.commandListeners = new ArrayList<>(commandListeners);
        }

        @Override
        public void add(ProtocolListener protocolListener) {
            boolean added = protocolListeners.add(protocolListener);
            if (added && (protocolListener instanceof CommandListener)) {
                commandListeners.add((CommandListener) protocolListener);
            }
        }

        @Override
        public boolean remove(ProtocolListener protocolListener) {
            boolean removed = protocolListeners.remove(protocolListener);
            if (removed && (protocolListener instanceof CommandListener)) {
                commandListeners.remove(protocolListener);
            }
            return removed && protocolListeners.isEmpty();
        }

        @Override
        public boolean isEmpty() {
            return protocolListeners.isEmpty();
        }

        @Override
        public Iterator<ProtocolListener> protocolListeners() {
            return new ProtocolListenersIterator(protocolListeners.iterator(), commandListeners);
        }

        @Override
        public Iterator<CommandListener> commandListeners() {
            return new CommandListenersIterator(commandListeners.iterator(), protocolListeners);
        }

        @Override
        public ProtocolListenerCollection snapshot() {
            return new SnapshotProtocolListenerCollection(protocolListeners, commandListeners);
        }
    }
    
    private static class ProtocolListenersIterator implements Iterator<ProtocolListener> {

        private final List<CommandListener> commandListeners;
        private final Iterator<ProtocolListener> iterator;
        private ProtocolListener next;

        ProtocolListenersIterator(Iterator<ProtocolListener> iterator, List<CommandListener> commandListeners) {
            super();
            this.commandListeners = commandListeners;
            this.iterator = iterator;
        }

        @Override
        public boolean hasNext() {
            next = null;
            return iterator.hasNext();
        }

        @Override
        public ProtocolListener next() {
            ProtocolListener next = iterator.next();
            this.next = next;
            return next;
        }

        @Override
        public void remove() {
            iterator.remove();
            ProtocolListener next = this.next;
            if (null != next) {                        
                commandListeners.remove(next);
                this.next = null;
            }
        }
    }
    
    private static class CommandListenersIterator implements Iterator<CommandListener> {

        private final List<ProtocolListener> protocolListeners;
        private final Iterator<CommandListener> iterator;
        private CommandListener next;

        CommandListenersIterator(Iterator<CommandListener> iterator, List<ProtocolListener> protocolListeners) {
            super();
            this.protocolListeners = protocolListeners;
            this.iterator = iterator;
        }

        @Override
        public boolean hasNext() {
            next = null;
            return iterator.hasNext();
        }

        @Override
        public CommandListener next() {
            CommandListener next = iterator.next();
            this.next = next;
            return next;
        }

        @Override
        public void remove() {
            iterator.remove();
            CommandListener next = this.next;
            if (null != next) {                        
                protocolListeners.remove(next);
                this.next = null;
            }
        }

    }

}
