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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

/**
 * {@link CommandExecutorCollection} - A collection for {@code ProtocolListener} instances.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public abstract class CommandExecutorCollection implements Iterable<CommandExecutor> {

    /**
     * Creates a new {@code CommandExecutorCollection} instance.
     *
     * @param commandExecutor The initial executor to add
     * @return The new collection
     */
    public static CommandExecutorCollection newCollection(CommandExecutor commandExecutor) {
        return new ConcurrentCommandExecutorCollection(commandExecutor);
    }

    // ----------------------------------------------------------------------------------------------------------------

    /**
     * Initializes a new {@link CommandExecutorCollection}.
     */
    protected CommandExecutorCollection() {
        super();
    }

    /**
     * Adds specified executor to this collection.
     *
     * @param commandExecutor The executor to add
     */
    public abstract void add(CommandExecutor commandExecutor);

    /**
     * Removes specified executor from this collection.
     *
     * @param commandExecutor The executor to remove
     * @return <code>true</code> if last executor has been removed (and thus this collection is empty now); otherwise <code>false</code>
     */
    public abstract boolean remove(CommandExecutor commandExecutor);

    /**
     * Checks if this collection is empty.
     *
     * @return <code>true</code> if empty; otherwise <code>false</code>
     */
    public abstract boolean isEmpty();

    @Override
    public Iterator<CommandExecutor> iterator() {
        return commandExecutors();
    }

    /**
     * Gets an iterator over contained {@code ProtocolListener} instances.
     *
     * @return The iterator
     */
    public abstract Iterator<CommandExecutor> commandExecutors();

    /**
     * Creates a snapshot from this collection.
     * <p>
     * Modifications to the snapshot are <b>not</b> reflected to this collection.
     *
     * @return The snapshot
     */
    public abstract CommandExecutorCollection snapshot();

    /**
     * Gets the matching command executor for given protocol instance.
     *
     * @param protocolAccess The protocol access
     * @return The matching command executor or <code>null</code>
     */
    public abstract Optional<CommandExecutor> getMatchingCommandExecutorFor(ProtocolAccess protocolAccess);

    // ------------------------------------------------ Implementations ------------------------------------------------

    private static class ConcurrentCommandExecutorCollection extends CommandExecutorCollection {

        private final List<CommandExecutor> commandExecutors;
        private final Cache<HostAndPortAndUser, Optional<CommandExecutor>> matchingCommandExecutors;

        /**
         * Initializes a new {@link CommandExecutorCollection}.
         *
         * @param commandExecutor The initial executor to add
         */
        ConcurrentCommandExecutorCollection(CommandExecutor commandExecutor) {
            super();
            commandExecutors = new CopyOnWriteArrayList<>(new CommandExecutor[] { commandExecutor });
            matchingCommandExecutors = CacheBuilder.newBuilder().initialCapacity(256).maximumSize(256000).expireAfterAccess(10, TimeUnit.MINUTES).build();
        }

        @Override
        public void add(CommandExecutor commandExecutor) {
            boolean added = commandExecutors.add(commandExecutor);
            if (added) {
                matchingCommandExecutors.invalidateAll();
            }
        }

        @Override
        public boolean remove(CommandExecutor commandExecutor) {
            boolean removed = commandExecutors.remove(commandExecutor);
            if (removed) {
                matchingCommandExecutors.invalidateAll();
            }
            return removed && commandExecutors.isEmpty();
        }

        @Override
        public boolean isEmpty() {
            return commandExecutors.isEmpty();
        }

        @Override
        public Iterator<CommandExecutor> commandExecutors() {
            /*-
             * Element-changing operations on CopyOnWriteArrayList iterators (remove, set, and add) are not supported.
             * These methods throw UnsupportedOperationException.
             */
            return commandExecutors.iterator();
        }

        @Override
        public CommandExecutorCollection snapshot() {
            return new SnapshotCommandExecutorCollection(commandExecutors);
        }

        @Override
        public Optional<CommandExecutor> getMatchingCommandExecutorFor(ProtocolAccess protocolAccess) {
            HostAndPortAndUser hostAndPortAndUser = new HostAndPortAndUser(protocolAccess);
            Optional<CommandExecutor> optionalCommandExecutor = matchingCommandExecutors.getIfPresent(hostAndPortAndUser);
            if (optionalCommandExecutor != null) {
                return optionalCommandExecutor;
            }

            CommandExecutor matching = null;
            for (CommandExecutor commandExecutor : commandExecutors) {
                if ((matching == null || commandExecutor.getRanking() > matching.getRanking()) && commandExecutor.isApplicable(protocolAccess)) {
                    matching = commandExecutor;
                }
            }
            optionalCommandExecutor = Optional.ofNullable(matching);
            matchingCommandExecutors.put(hostAndPortAndUser, optionalCommandExecutor);
            return optionalCommandExecutor;
        }
    }

    private static class SnapshotCommandExecutorCollection extends CommandExecutorCollection {

        private final List<CommandExecutor> commandExecutors;

        /**
         * Initializes a new {@link SnapshotProtocolListenerCollection}.
         */
        SnapshotCommandExecutorCollection(List<CommandExecutor> commandExecutors) {
            super();
            this.commandExecutors = new ArrayList<>(commandExecutors);
        }

        @Override
        public void add(CommandExecutor commandExecutor) {
            commandExecutors.add(commandExecutor);
        }

        @Override
        public boolean remove(CommandExecutor commandExecutor) {
            boolean removed = commandExecutors.remove(commandExecutor);
            return removed && commandExecutors.isEmpty();
        }

        @Override
        public boolean isEmpty() {
            return commandExecutors.isEmpty();
        }

        @Override
        public Iterator<CommandExecutor> commandExecutors() {
            return commandExecutors.iterator();
        }

        @Override
        public CommandExecutorCollection snapshot() {
            return new SnapshotCommandExecutorCollection(commandExecutors);
        }

        @Override
        public Optional<CommandExecutor> getMatchingCommandExecutorFor(ProtocolAccess protocolAccess) {
            CommandExecutor matching = null;
            for (CommandExecutor commandExecutor : commandExecutors) {
                if (commandExecutor.isApplicable(protocolAccess) && (matching == null || commandExecutor.getRanking() > matching.getRanking())) {
                    matching = commandExecutor;
                }
            }
            return Optional.ofNullable(matching);
        }
    }

    private static class HostAndPortAndUser {

        private final String host;
        private final int port;
        private final String user;
        private final int hash;

        HostAndPortAndUser(ProtocolAccess protocolAccess) {
            this(protocolAccess.getHost(), protocolAccess.getPort(), protocolAccess.getUser());
        }

        HostAndPortAndUser(String host, int port, String user) {
            super();
            this.host = host;
            this.port = port;
            this.user = user;

            int prime = 31;
            int result = 1;
            result = prime * result + port;
            result = prime * result + ((user == null) ? 0 : user.hashCode());
            result = prime * result + ((host == null) ? 0 : host.hashCode());
            hash = result;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            HostAndPortAndUser other = (HostAndPortAndUser) obj;
            if (port != other.port) {
                return false;
            }
            if (user == null) {
                if (other.user != null) {
                    return false;
                }
            } else if (!user.equals(other.user)) {
                return false;
            }
            if (host == null) {
                if (other.host != null) {
                    return false;
                }
            } else if (!host.equals(other.host)) {
                return false;
            }
            return true;
        }
    }

}
