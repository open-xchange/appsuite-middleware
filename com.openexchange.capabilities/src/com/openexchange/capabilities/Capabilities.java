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

package com.openexchange.capabilities;

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

/**
 * {@link Capabilities} -  A utility class for capabilities.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.4
 */
public class Capabilities {

    /**
     * Initializes a new {@link Capabilities}.
     */
    private Capabilities() {
        super();
    }

    private static final CapabilitySet EMPTY_SET = new EmptyCapabilitySet();

    /**
     * Gets an empty set (immutable). This set is serializable.
     *
     * @return the empty set
     */
    public static final CapabilitySet emptyCapabilitySet() {
        return EMPTY_SET;
    }

   /**
    * Gets an immutable capability set containing only the specified capability. This set is serializable.
    *
    * @param capability The sole capability to be stored in the returned capability set.
    * @return An immutable capability set containing only the specified capability
    * @throws IllegalArgumentException If given capability is <code>null</code>
    */
   public static final CapabilitySet singletonCapabilitySet(Capability capability) {
       if (capability == null) {
        throw new IllegalArgumentException("Capability must not be null");
    }
       return new SingletonCapabilitySet(capability);
   }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private static class SingletonCapabilitySet implements CapabilitySet, Serializable {

        private static final long serialVersionUID = -8834649119751603760L;

        private final Capability capability;

        SingletonCapabilitySet(Capability capability) {
            super();
            this.capability = capability;
        }

        @Override
        public int size() {
            return 1;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public boolean contains(Capability capability) {
            return this.capability.equals(capability);
        }

        @Override
        public boolean contains(String id) {
            return this.capability.getId().equals(id);
        }

        @Override
        public Capability get(String id) {
            return this.capability.getId().equals(id) ? this.capability : null;
        }

        @Override
        public Iterator<Capability> iterator() {
            return Collections.<Capability> singletonList(capability).iterator();
        }

        @Override
        public boolean add(Capability capability) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean remove(Capability capability) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean remove(String id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Set<Capability> asSet() {
            return Collections.singleton(capability);
        }
    }

    private static class EmptyCapabilitySet implements CapabilitySet, Serializable {

        private static final long serialVersionUID = -2940333299499389866L;

        EmptyCapabilitySet() {
            super();
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public boolean contains(Capability capability) {
            return false;
        }

        @Override
        public boolean contains(String id) {
            return false;
        }

        @Override
        public Capability get(String id) {
            return null;
        }

        @Override
        public Iterator<Capability> iterator() {
            return Collections.<Capability> emptySet().iterator();
        }

        @Override
        public boolean add(Capability capability) {
            return false;
        }

        @Override
        public boolean remove(Capability capability) {
            return false;
        }

        @Override
        public boolean remove(String id) {
            return false;
        }

        @Override
        public void clear() {
            // Nothing to do
        }

        @Override
        public Set<Capability> asSet() {
            return Collections.emptySet();
        }

    }

}
