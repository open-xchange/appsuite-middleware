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

package com.openexchange.appsuite;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.openexchange.java.Streams;

/**
 * An in-memory file cache for small files (mostly UI files). Since all data is held in RAM, this class should only be used as a singleton.
 * This pretty much restricts it to only storing publicly accessible files (e.g. the UI).
 *
 * @author <a href="mailto:viktor.pracht@open-xchange.com">Viktor Pracht</a>
 */
public class FileCache {

    private static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(FileCache.class);

    public interface Filter {

        String resolve(String path);

        byte[] filter(ByteArrayOutputStream baos);
    }

    private class CacheEntry {

        private final File path;

        private long timestamp = Long.MIN_VALUE;

        private byte[] data = null;

        public CacheEntry(File path) {
            this.path = path;
        }

        public byte[] getData(Filter filter) {
            long current = path.lastModified();
            if (current == timestamp) {
                return data;
            }
            timestamp = current;

            // Read the entire file into a byte array
            LOG.debug("Reading '{}'", path);
            final ByteArrayOutputStream baos = Streams.newByteArrayOutputStream(8192);
            InputStream in = null;
            try {
                in = new FileInputStream(path);
                final int buflen = 2048;
                final byte[] buf = new byte[buflen];
                for (int read = in.read(buf, 0, buflen); read > 0; read = in.read(buf, 0, buflen)) {
                    baos.write(buf, 0, read);
                }
                baos.flush(); // no-op
                data = filter == null ? baos.toByteArray() : filter.filter(baos);
            } catch (final IOException e) {
                LOG.debug("Could not read from '{}'", path);
                data = null;
            } finally {
                Streams.close(in);
            }

            return data;
        }
    }

    // ---------------------------------------------------------------------------------------------------------------------

    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<String, CacheEntry>();
    private final File[] roots;
    private final String[] prefixes;

    public FileCache(File root) throws IOException {
        this(new File[] { root });
    }

    public FileCache(File[] roots) throws IOException {
        this.roots = roots;
        this.prefixes = new String[roots.length];
        int i = 0;
        for (File root : roots) {
            prefixes[i++] = root.getCanonicalPath() + File.separatorChar;
        }
    }

    /**
     * Returns the file contents as a byte array.
     *
     * @param path The file to return.
     * @param filter An optional Filter which processes loaded file data.
     * @return The file contents as a byte array, or null if the file does not exist or is not a normal file.
     */
    public byte[] get(String path, Filter filter) {
        path = filter == null ? path : filter.resolve(path);
        for (int i = 0; i < roots.length; i++) {
            File f = new File(roots[i], path);
            try {
                if (!f.getCanonicalPath().startsWith(prefixes[i])) {
                    continue;
                }
            } catch (IOException e) {
                continue;
            }
            if (f.isFile()) {
                CacheEntry entry = cache.get(path);
                if (entry == null) {
                    entry = new CacheEntry(f);
                    cache.put(path, entry);
                }
                return entry.getData(filter);
            }
        }
        StringBuilder sb = new StringBuilder("Could not find '");
        sb.append(new File(roots[0], path));
        for (int i = 1; i < roots.length; i++) {
            sb.append("'\n            or '");
            sb.append(new File(roots[i], path));
        }
        sb.append('\'');
        LOG.debug(sb.toString());
        return null;
    }

    /**
     * Clears the cache.
     */
    public void clear() {
        cache.clear();
    }
}
