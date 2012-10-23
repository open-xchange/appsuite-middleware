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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.ui7;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An in-memory file cache for small files (mostly UI files).
 * Since all data is held in RAM, this class should only be used as a singleton.
 * This pretty much restricts it to only storing publicly accessible files
 * (e.g. the UI).
 * 
 * @author <a href="mailto:viktor.pracht@open-xchange.com">Viktor Pracht</a>
 */
public class FileCache {

    private static org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(FileCache.class));

    private class CacheEntry {

        private File path;

        private long timestamp = Long.MIN_VALUE;

        private byte[] data = null;

        public CacheEntry(File path) {
            this.path = path;
        }

        public byte[] getData() {
            if (!path.isFile()) {
                return null;
            }
            long current = path.lastModified();
            if (current > timestamp) {
                timestamp = current;
                // Read the entire file into a byte array
                LOG.debug("Reading " + path);
                try {
                    RandomAccessFile f = new RandomAccessFile(path, "r");
                    data = new byte[(int) f.length()];
                    f.readFully(data);
                    f.close();
                } catch (IOException e) {
                    data = null;
                }
            }
            return data;
        }
    }

    private Map<File, CacheEntry> cache = new ConcurrentHashMap<File, CacheEntry>();

    /**
     * Returns the file contents as a byte array.
     * @param path The file to return.
     * @return The file contents as a byte array, or null if the file does
     * not exist or is not a normal file.
     */
    public byte[] get(File path) {
        CacheEntry entry = cache.get(path);
        if (entry == null) {
            entry = new CacheEntry(path);
            cache.put(path, entry);
        }
        return entry.getData();
    }
    
    /**
     * Clears the cache.
     */
    public void clear() {
        cache.clear();
    }

}
