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

package com.openexchange.appsuite;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
public class DefaultFileCache implements FileCache {

    /** The logger constant */
    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultFileCache.class);

    private static class CacheEntry {

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
            } catch (IOException e) {
                LOG.debug("Could not read from '{}'", path, e);
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

    public DefaultFileCache(File root) throws IOException {
        this(new File[] { root });
    }

    public DefaultFileCache(File[] roots) throws IOException {
        super();
        this.roots = roots;
        this.prefixes = new String[roots.length];
        int i = 0;
        for (File root : roots) {
            prefixes[i++] = root.getCanonicalPath() + File.separatorChar;
        }
    }

    @Override
    public byte[] get(String path, Filter filter) {
        String pathToUse = filter == null ? path : filter.resolve(path);
        for (int i = 0; i < roots.length; i++) {
            File f = new File(roots[i], pathToUse);
            try {
                checkAbsoluteInSubpath(f.getAbsoluteFile(), roots[i]);
                if (!f.getCanonicalPath().startsWith(prefixes[i])) {
                    continue;
                }
            } catch (IOException e) {
                continue;
            }
            if (f.isFile()) {
                CacheEntry entry = cache.get(pathToUse);
                if (entry == null) {
                    entry = new CacheEntry(f);
                    cache.put(pathToUse, entry);
                }
                return entry.getData(filter);
            }
        }
        StringBuilder sb = new StringBuilder("Could not find '");
        sb.append(new File(roots[0], pathToUse));
        for (int i = 1; i < roots.length; i++) {
            sb.append("'\n            or '");
            sb.append(new File(roots[i], pathToUse));
        }
        sb.append('\'');
        LOG.debug(sb.toString());
        return null;
    }

    @Override
    public void clear() {
        cache.clear();
    }

    private synchronized void checkAbsoluteInSubpath(File f, File parentDir) throws FileNotFoundException {
        File current = f;

        while (current != null) {
            current = current.getParentFile();
            if (current.equals(parentDir)) {
                return;
            }
        }
        LOG.error("Trying to leave designated directory with a relative path. Denying request.");
        throw new FileNotFoundException();
    }
}
