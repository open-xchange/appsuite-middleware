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

package com.openexchange.exception;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * {@link OXExceptionCodeSet} - A set for exception codes.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public class OXExceptionCodeSet {

    private final Set<Code> codes;

    /**
     * Initializes a new {@link OXExceptionCodeSet}.
     */
    public OXExceptionCodeSet() {
        super();
        codes = new HashSet<>();
    }

    /**
     * Initializes a new {@link OXExceptionCodeSet}.
     */
    public OXExceptionCodeSet(OXExceptionCode... codes) {
        super();
        this.codes = new HashSet<>(codes.length);
        for (OXExceptionCode code : codes) {
            this.codes.add(Code.codeFor(code));
        }
    }

    /**
     * Initializes a new {@link OXExceptionCodeSet}.
     */
    public OXExceptionCodeSet(Collection<? extends OXExceptionCode> codes) {
        super();
        this.codes = new HashSet<>(codes.size());
        for (OXExceptionCode code : codes) {
            this.codes.add(Code.codeFor(code));
        }
    }

    /**
     * Gets the number of codes contained in this set.
     *
     * @return The number of codes
     */
    public int size() {
        return codes.size();
    }

    /**
     * Checks if this set is empty.
     *
     * @return <code>true</code> if empty
     */
    public boolean isEmpty() {
        return codes.isEmpty();
    }

    /**
     * Checks if this set contains the given exception.
     *
     * @param exception The exception
     * @return <code>true</code> if contained; otherwise <code>false</code>
     */
    public boolean contains(OXException exception) {
        return codes.contains(Code.codeFor(exception));
    }

    /**
     * Adds the specified code to this set if it is not already present
     *
     * @param code The code to add
     * @return <code>true</code> if this set did not already contain the specified code; otherwise <code>false</code>
     */
    public boolean add(OXExceptionCode code) {
        return codes.add(Code.codeFor(code));
    }

    /**
     * Removes the specified code from this set if it is present.
     *
     * @param o The code to remove
     * @return <code>true</code> if this set contained the specified code; otherwise <code>false</code>
     */
    public boolean remove(OXExceptionCode code) {
        return codes.remove(Code.codeFor(code));
    }

    /**
     * Checks if this set contains all of the exceptions of the specified collection.
     *
     * @param exceptions The collection
     * @return <code>true</code> if this set contains all of the exceptions of the specified collection; otherwise <code>false</code>
     */
    public boolean containsAll(Collection<? extends OXException> exceptions) {
        Collection<Code> codes = new ArrayList<>(exceptions.size());
        for (OXException e : exceptions) {
            codes.add(Code.codeFor(e));
        }
        return this.codes.containsAll(codes);
    }

    /**
     * Adds all of the codes in the specified collection to this set if they're not already present
     *
     * @param codes The collection
     * @return <code>true</code> if this set changed as a result of the call; otherwise <code>false</code>
     */
    public boolean addAll(Collection<? extends OXExceptionCode> codes) {
        Collection<Code> codez = new ArrayList<>(codes.size());
        for (OXExceptionCode code : codes) {
            codez.add(Code.codeFor(code));
        }
        return this.codes.addAll(codez);
    }

    /**
     * Removes from this set all of its codes that are contained in the specified collection.
     *
     * @param codes The collection
     * @return <code>true</code> if this set changed as a result of the call; otherwise <code>false</code>
     */
    public boolean removeAll(Collection<? extends OXExceptionCode> codes) {
        Collection<Code> codez = new ArrayList<>(codes.size());
        for (OXExceptionCode code : codes) {
            codez.add(Code.codeFor(code));
        }
        return this.codes.removeAll(codez);
    }

    /**
     * Clears this set.
     */
    public void clear() {
        codes.clear();
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private static class Code {

        static Code codeFor(OXException e) {
            return new Code(e.getCode(), e.getPrefix());
        }

        static Code codeFor(OXExceptionCode c) {
            return new Code(c.getNumber(), c.getPrefix());
        }

        private final int number;
        private final String prefix;
        private final int hash;

        private Code(int number, String prefix) {
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
            if (this == obj) {
                return true;
            }
            if (obj.getClass() != Code.class) {
                return false;
            }
            Code other = (Code) obj;
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
