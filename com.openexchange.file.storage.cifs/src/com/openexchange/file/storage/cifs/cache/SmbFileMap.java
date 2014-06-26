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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.file.storage.cifs.cache;

import java.util.Iterator;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import jcifs.smb.SmbFile;
import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import com.googlecode.concurrentlinkedhashmap.Weighers;


/**
 * {@link SmbFileMap} - An in-memory map with LRU eviction policy.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SmbFileMap {

    protected static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SmbFileMap.class);

    private final ConcurrentMap<String, Wrapper> map;
    private final int maxLifeMillis;

    /**
     * Initializes a new {@link SmbFileMap}.
     *
     * @param maxCapacity the max capacity
     * @param maxLifeUnits the max life units
     * @param unit the unit
     */
    public SmbFileMap(final int maxCapacity, final int maxLifeUnits, final TimeUnit unit) {
        super();
        map = new ConcurrentLinkedHashMap.Builder<String, Wrapper>().maximumWeightedCapacity(maxCapacity).weigher(Weighers.entrySingleton()).build();
        this.maxLifeMillis = (int) unit.toMillis(maxLifeUnits);
    }

    /**
     * Initializes a new {@link SmbFileMap}.
     *
     * @param maxCapacity the max capacity
     * @param maxLifeMillis the max life millis
     */
    public SmbFileMap(final int maxCapacity, final int maxLifeMillis) {
        this(maxCapacity, maxLifeMillis, TimeUnit.MILLISECONDS);
    }

    /**
     * Removes elapsed entries from map.
     */
    public void shrink() {
        final long minStamp = System.currentTimeMillis() - maxLifeMillis;
        for (final Iterator<Wrapper> it = map.values().iterator(); it.hasNext();) {
            if (it.next().getStamp() < minStamp) {
                it.remove();
            }
        }
    }

    /**
     * Put if absent.
     *
     * @param smbFile the SMB file
     * @return The SMB file
     */
    public SmbFile putIfAbsent(final SmbFile smbFile) {
        return putIfAbsent(smbFile.getPath(), smbFile);
    }

    /**
     * Put if absent.
     *
     * @param path the SMB file path
     * @param treeId the tree id
     * @param smbFile the SMB file
     * @return The SMB file
     */
    public SmbFile putIfAbsent(final String path, final SmbFile smbFile) {
        final Wrapper wrapper = wrapperFor(smbFile);
        Wrapper prev = map.putIfAbsent(path, wrapper);
        if (null == prev) {
            // Successfully put into map
            return null;
        }
        if (prev.elapsed(maxLifeMillis)) {
            if (map.replace(path, prev, wrapper)) {
                // Successfully replaced with elapsed one
                return null;
            }
            prev = map.get(path);
            if (null == prev) {
                prev = map.putIfAbsent(path, wrapper);
                return null == prev ? null : prev.getValue();
            }
            return prev.getValue();
        }
        return prev.getValue();
    }

    /**
     * Size.
     *
     * @return The size
     */
    public int size() {
        return map.size();
    }

    /**
     * Checks if empty flag is set.
     *
     * @return <code>true</code> if empty flag is set; otherwise <code>false</code>
     */
    public boolean isEmpty() {
        return map.isEmpty();
    }

    /**
     * Contains.
     *
     * @param path the SMB file path
     * @return <code>true</code> if successful; otherwise <code>false</code>
     */
    public boolean contains(final String path) {
        return map.containsKey(path);
    }

    /**
     * Gets the SMB file.
     *
     * @param path the SMB file path
     * @return The SMB file
     */
    public SmbFile get(final String path) {
        final Wrapper wrapper = map.get(path);
        if (null == wrapper) {
            return null;
        }
        if (wrapper.elapsed(maxLifeMillis)) {
            map.remove(path);
            shrink();
            return null;
        }
        return wrapper.getValue();
    }

    /**
     * Puts specified SMB file.
     *
     * @param smbFile the SMB file
     * @return The SMB file
     */
    public SmbFile put(final SmbFile smbFile) {
        return put(smbFile.getPath(), smbFile);
    }

    /**
     * Puts specified SMB file.
     *
     * @param path the SMB file path
     * @param smbFile the SMB file
     * @return The SMB file
     */
    public SmbFile put(final String path, final SmbFile smbFile) {
        final Wrapper wrapper = map.put(path, wrapperFor(smbFile));
        if (null == wrapper) {
            return null;
        }
        if (wrapper.elapsed(maxLifeMillis)) {
            map.remove(path);
            shrink();
            return null;
        }
        return wrapper.getValue();
    }

    /**
     * Removes the SMB file.
     *
     * @param path the SMB file path
     * @return The SMB file
     */
    public SmbFile remove(final String path) {
        final Wrapper wrapper = map.remove(path);
        return null == wrapper ? null : wrapper.getIfNotElapsed(maxLifeMillis);
    }

    /**
     * Clears this map.
     */
    public void clear() {
        map.clear();
    }

    @Override
    public String toString() {
        return map.toString();
    }

    private Wrapper wrapperFor(final SmbFile smbFile) {
        return new Wrapper(smbFile);
    }

    private static final class Wrapper {

        final SmbFile value;
        private volatile long stamp;

        public Wrapper(final SmbFile value) {
            super();
            this.value = value;
            stamp = System.currentTimeMillis();
        }

        public long getStamp() {
            return stamp;
        }

        public boolean elapsed(final int maxLifeMillis) {
            return (System.currentTimeMillis() - stamp) > maxLifeMillis;
        }

        public SmbFile getIfNotElapsed(final int maxLifeMillis) {
            return elapsed(maxLifeMillis) ? null : value;
        }

        public SmbFile getValue() {
            stamp = System.currentTimeMillis();
            return value;
        }

        @Override
        public String toString() {
            return value.getPath();
        }

    } // End of class Wrapper

}
