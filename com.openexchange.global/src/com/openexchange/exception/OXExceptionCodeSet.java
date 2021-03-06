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

package com.openexchange.exception;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import com.google.common.collect.ImmutableSet;

/**
 * {@link OXExceptionCodeSet} - An immutable set for exception codes and/or prefixes.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public class OXExceptionCodeSet {

    private final Set<InternalCode> codes;
    private final Set<String> prefixes;

    /**
     * Initializes a new {@link OXExceptionCodeSet}.
     *
     * @param prefixes The prefixes/codes to add
     */
    public OXExceptionCodeSet(Prefix... prefixes) {
        super();
        ImmutableSet.Builder<InternalCode> thisCodes = null;
        ImmutableSet.Builder<String> thisPrefixes = null;
        for (Prefix prefix : prefixes) {
            if (prefix instanceof Code) {
                if (thisCodes == null) {
                    thisCodes = ImmutableSet.builder();
                }
                thisCodes.add(InternalCode.codeFor((Code) prefix));
            } else {
                if (thisPrefixes == null) {
                    thisPrefixes = ImmutableSet.builder();
                }
                thisPrefixes.add(prefix.getPrefix());
            }
        }
        this.codes = thisCodes == null ? ImmutableSet.of() : thisCodes.build();
        this.prefixes = thisPrefixes == null ? ImmutableSet.of() : thisPrefixes.build();
    }

    /**
     * Initializes a new {@link OXExceptionCodeSet}.
     *
     * @param prefixes The prefixes/codes to add
     */
    public OXExceptionCodeSet(Collection<? extends Prefix> prefixes) {
        super();
        ImmutableSet.Builder<InternalCode> thisCodes = null;
        ImmutableSet.Builder<String> thisPrefixes = null;
        for (Prefix prefix : prefixes) {
            if (prefix instanceof Code) {
                if (thisCodes == null) {
                    thisCodes = ImmutableSet.builder();
                }
                thisCodes.add(InternalCode.codeFor((Code) prefix));
            } else {
                if (thisPrefixes == null) {
                    thisPrefixes = ImmutableSet.builder();
                }
                thisPrefixes.add(prefix.getPrefix());
            }
        }
        this.codes = thisCodes == null ? ImmutableSet.of() : thisCodes.build();
        this.prefixes = thisPrefixes == null ? ImmutableSet.of() : thisPrefixes.build();
    }

    /**
     * Checks if this set is empty.
     *
     * @return <code>true</code> if empty
     */
    public boolean isEmpty() {
        return prefixes.isEmpty() && codes.isEmpty();
    }

    /**
     * Checks if this set contains the given exception.
     *
     * @param exception The exception
     * @return <code>true</code> if contained; otherwise <code>false</code>
     */
    public boolean contains(OXException exception) {
        return exception != null && (prefixes.contains(exception.getPrefix()) || codes.contains(InternalCode.codeFor(exception)));
    }

    /**
     * Checks if this set does <b>not</b> contain the given exception.
     *
     * @param exception The exception
     * @return <code>true</code> if <b>not</b> contained; otherwise <code>false</code> (if contained)
     */
    public boolean notContains(OXException exception) {
        return contains(exception) == false;
    }

    /**
     * Checks if this set contains all of the exceptions of the specified collection.
     *
     * @param exceptions The collection
     * @return <code>true</code> if this set contains all of the exceptions of the specified collection; otherwise <code>false</code>
     */
    public boolean containsAll(Collection<? extends OXException> exceptions) {
        if (exceptions == null || exceptions.isEmpty()) {
            return false;
        }

        List<OXException> l = new ArrayList<>(exceptions);
        for (Iterator<OXException> it = l.iterator(); it.hasNext();) {
            OXException exception = it.next();
            if (prefixes.contains(exception.getPrefix())) {
                it.remove();
            }
        }

        if (l.isEmpty()) {
            return true;
        }

        for (Iterator<OXException> it = l.iterator(); it.hasNext();) {
            OXException exception = it.next();
            if (codes.contains(InternalCode.codeFor(exception))) {
                it.remove();
            }
        }

        return l.isEmpty();
    }

    // ----------------------------------------------------- Modifying stuff ---------------------------------------------------------------

//    /**
//     * Adds the specified code to this set if it is not already present
//     *
//     * @param code The code to add
//     * @return <code>true</code> if this set did not already contain the specified code; otherwise <code>false</code>
//     */
//    public boolean add(Code code) {
//        return code != null && codes.add(InternalCode.codeFor(code));
//    }
//
//    /**
//     * Removes the specified code from this set if it is present.
//     *
//     * @param code The code to remove
//     * @return <code>true</code> if this set contained the specified code; otherwise <code>false</code>
//     */
//    public boolean remove(Code code) {
//        return code != null && codes.remove(InternalCode.codeFor(code));
//    }
//
//    /**
//     * Adds the specified prefix to this set if it is not already present
//     *
//     * @param prefix The prefix to add
//     * @return <code>true</code> if this set did not already contain the specified prefix; otherwise <code>false</code>
//     */
//    public boolean add(Prefix prefix) {
//        return prefix != null && prefixes.add(prefix.getPrefix());
//    }
//
//    /**
//     * Removes the specified prefix from this set if it is present.
//     *
//     * @param prefix The prefix to remove
//     * @return <code>true</code> if this set contained the specified prefix; otherwise <code>false</code>
//     */
//    public boolean remove(Prefix prefix) {
//        return prefix != null && prefixes.remove(prefix.getPrefix());
//    }
//
//    /**
//     * Adds all of the codes in the specified collection to this set if they're not already present
//     *
//     * @param codes The collection
//     * @return <code>true</code> if this set changed as a result of the call; otherwise <code>false</code>
//     */
//    public boolean addAll(Collection<? extends Code> codes) {
//        if (codes == null || codes.isEmpty()) {
//            return false;
//        }
//
//        boolean added = false;
//        for (Code code : codes) {
//            added |= this.codes.add(InternalCode.codeFor(code));
//        }
//        return added;
//    }
//
//    /**
//     * Removes from this set all of its codes that are contained in the specified collection.
//     *
//     * @param codes The collection
//     * @return <code>true</code> if this set changed as a result of the call; otherwise <code>false</code>
//     */
//    public boolean removeAll(Collection<? extends Code> codes) {
//        if (codes == null || codes.isEmpty()) {
//            return false;
//        }
//
//        boolean removed = false;
//        for (Code code : codes) {
//            removed |= this.codes.remove(InternalCode.codeFor(code));
//        }
//        return removed;
//    }
//
//    /**
//     * Adds all of the prefixes in the specified collection to this set if they're not already present
//     *
//     * @param prefixes The collection
//     * @return <code>true</code> if this set changed as a result of the call; otherwise <code>false</code>
//     */
//    public boolean addAllPrefixes(Collection<? extends String> prefixes) {
//        if (prefixes == null || prefixes.isEmpty()) {
//            return false;
//        }
//
//        boolean added = false;
//        for (String prefix : prefixes) {
//            added |= this.prefixes.add(prefix);
//        }
//        return added;
//    }
//
//    /**
//     * Removes from this set all of its prefixes that are contained in the specified collection.
//     *
//     * @param prefixes The collection
//     * @return <code>true</code> if this set changed as a result of the call; otherwise <code>false</code>
//     */
//    public boolean removeAllPrefixes(Collection<? extends String> prefixes) {
//        if (prefixes == null || prefixes.isEmpty()) {
//            return false;
//        }
//
//        boolean removed = false;
//        for (String prefix : prefixes) {
//            removed |= this.prefixes.remove(prefix);
//        }
//        return removed;
//    }
//
//    /**
//     * Clears this set.
//     */
//    public void clear() {
//        codes.clear();
//    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private static class InternalCode {

        static InternalCode codeFor(OXException e) {
            return new InternalCode(e.getCode(), e.getPrefix());
        }

        static InternalCode codeFor(Code c) {
            return new InternalCode(c.getNumber(), c.getPrefix());
        }

        private final int number;
        private final String prefix;
        private final int hash;

        private InternalCode(int number, String prefix) {
            super();
            this.number = number;
            this.prefix = prefix;

            int prime = 31;
            int result = 1;
            result = prime * result + number;
            result = prime * result + ((prefix == null) ? 0 : prefix.hashCode());
            this.hash = result;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (this == obj) {
                return true;
            }
            if (obj.getClass() != InternalCode.class) {
                return false;
            }
            InternalCode other = (InternalCode) obj;
            if (number != other.number) {
                return false;
            }
            if (prefix == null) {
                if (other.prefix != null) {
                    return false;
                }
            } else if (!prefix.equals(other.prefix)) {
                return false;
            }
            return true;
        }

    }

}
