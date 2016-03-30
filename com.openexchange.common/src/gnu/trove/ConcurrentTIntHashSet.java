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

package gnu.trove;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.procedure.TIntProcedure;
import gnu.trove.set.hash.TIntHashSet;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * {@link ConcurrentTIntHashSet}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ConcurrentTIntHashSet extends TIntHashSet implements Cloneable {

    private final TIntHashSet set;

    private ReadWriteLock readWriteLock;

    /**
     * Initializes a new {@link ConcurrentTIntHashSet}.
     */
    public ConcurrentTIntHashSet() {
        super();
        set = new TIntHashSet();
        readWriteLock = new ReentrantReadWriteLock();
    }

    /**
     * Initializes a new {@link ConcurrentTIntHashSet}.
     *
     * @param initialCapacity used to find a prime capacity for the table.
     * @param loadFactor used to calculate the threshold over which rehashing takes place.
     */
    public ConcurrentTIntHashSet(final int initialCapacity, final float loadFactor) {
        super();
        set = new TIntHashSet(initialCapacity, loadFactor);
        readWriteLock = new ReentrantReadWriteLock();
    }

    /**
     * Initializes a new {@link ConcurrentTIntHashSet}.
     *
     * @param initialCapacity used to find a prime capacity for the table.
     */
    public ConcurrentTIntHashSet(final int initialCapacity) {
        super();
        set = new TIntHashSet(initialCapacity);
        readWriteLock = new ReentrantReadWriteLock();
    }

    /**
     * Initializes a new {@link ConcurrentTIntHashSet}.
     *
     * @param array an array of int primitives
     */
    public ConcurrentTIntHashSet(final int[] array) {
        super();
        set = new TIntHashSet(array);
        readWriteLock = new ReentrantReadWriteLock();
    }

    @Override
    public ConcurrentTIntHashSet clone() {
        try {
            final ConcurrentTIntHashSet clone = (ConcurrentTIntHashSet) super.clone();
            clone.readWriteLock = new ReentrantReadWriteLock();
            return clone;
        } catch (final CloneNotSupportedException e) {
            // Cannot occur
            throw new InternalError("Clone not supported although Cloneable implemented.");
        }
    }

    @Override
    public TIntIterator iterator() {
        final Lock l = readWriteLock.readLock();
        l.lock();
        try {
            return set.iterator();
        } finally {
            l.unlock();
        }
    }

    @Override
    public boolean isEmpty() {
        final Lock l = readWriteLock.readLock();
        l.lock();
        try {
            return set.isEmpty();
        } finally {
            l.unlock();
        }
    }

    @Override
    public boolean add(final int val) {
        final Lock l = readWriteLock.writeLock();
        l.lock();
        try {
            return set.add(val);
        } finally {
            l.unlock();
        }
    }

    @Override
    public boolean contains(final int val) {
        final Lock l = readWriteLock.readLock();
        l.lock();
        try {
            return set.contains(val);
        } finally {
            l.unlock();
        }
    }

    @Override
    public int size() {
        final Lock l = readWriteLock.readLock();
        l.lock();
        try {
            return set.size();
        } finally {
            l.unlock();
        }
    }

    @Override
    public boolean forEach(final TIntProcedure procedure) {
        final Lock l = readWriteLock.readLock();
        l.lock();
        try {
            return set.forEach(procedure);
        } finally {
            l.unlock();
        }
    }

    @Override
    public void ensureCapacity(final int desiredCapacity) {
        final Lock l = readWriteLock.writeLock();
        l.lock();
        try {
            set.ensureCapacity(desiredCapacity);
        } finally {
            l.unlock();
        }
    }

    @Override
    public void compact() {
        final Lock l = readWriteLock.writeLock();
        l.lock();
        try {
            set.compact();
        } finally {
            l.unlock();
        }
    }

    @Override
    public int[] toArray() {
        final Lock l = readWriteLock.readLock();
        l.lock();
        try {
            return set.toArray();
        } finally {
            l.unlock();
        }
    }

    @Override
    public void clear() {
        final Lock l = readWriteLock.writeLock();
        l.lock();
        try {
            set.clear();
        } finally {
            l.unlock();
        }
    }

    @Override
    public boolean equals(final Object other) {
        final Lock l = readWriteLock.readLock();
        l.lock();
        try {
            return set.equals(other);
        } finally {
            l.unlock();
        }
    }

    @Override
    public void setAutoCompactionFactor(final float factor) {
        final Lock l = readWriteLock.writeLock();
        l.lock();
        try {
            set.setAutoCompactionFactor(factor);
        } finally {
            l.unlock();
        }
    }

    @Override
    public int hashCode() {
        final Lock l = readWriteLock.readLock();
        l.lock();
        try {
            return set.hashCode();
        } finally {
            l.unlock();
        }
    }

    @Override
    public float getAutoCompactionFactor() {
        final Lock l = readWriteLock.readLock();
        l.lock();
        try {
            return set.getAutoCompactionFactor();
        } finally {
            l.unlock();
        }
    }

    @Override
    public boolean remove(final int val) {
        final Lock l = readWriteLock.writeLock();
        l.lock();
        try {
            return set.remove(val);
        } finally {
            l.unlock();
        }
    }

    @Override
    public boolean containsAll(final int[] array) {
        final Lock l = readWriteLock.readLock();
        l.lock();
        try {
            return set.containsAll(array);
        } finally {
            l.unlock();
        }
    }

    @Override
    public boolean addAll(final int[] array) {
        final Lock l = readWriteLock.writeLock();
        l.lock();
        try {
            return set.addAll(array);
        } finally {
            l.unlock();
        }
    }

    @Override
    public boolean removeAll(final int[] array) {
        final Lock l = readWriteLock.writeLock();
        l.lock();
        try {
            return set.removeAll(array);
        } finally {
            l.unlock();
        }
    }

    @Override
    public String toString() {
        final Lock l = readWriteLock.readLock();
        l.lock();
        try {
            return set.toString();
        } finally {
            l.unlock();
        }
    }

    @Override
    public boolean retainAll(final int[] array) {
        final Lock l = readWriteLock.writeLock();
        l.lock();
        try {
            return set.retainAll(array);
        } finally {
            l.unlock();
        }
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        final Lock l = readWriteLock.readLock();
        l.lock();
        try {
            set.writeExternal(out);
        } finally {
            l.unlock();
        }
    }

    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        final Lock l = readWriteLock.writeLock();
        l.lock();
        try {
            set.readExternal(in);
        } finally {
            l.unlock();
        }
    }

}
