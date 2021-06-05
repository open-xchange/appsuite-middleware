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

package com.openexchange.mail.compose.mailstorage.cache.file;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import javax.mail.internet.SharedInputStream;
import javax.mail.util.SharedFileInputStream;
import com.openexchange.java.Streams;
import com.openexchange.mail.compose.mailstorage.cache.CacheReference;


/**
 * {@link FileCacheReferenceImpl}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.10.5
 */
public class FileCacheReferenceImpl implements CacheReference {

    private final File file;
    private int hash;
    private volatile SharedFileInputStream stream;

    /**
     * Initializes a new {@link FileCacheReferenceImpl}.
     *
     * @param file The file containing the MIME content
     */
    public FileCacheReferenceImpl(File file) {
        super();
        this.file = Objects.requireNonNull(file);
        hash = 0;
        this.stream = null;
    }

    @Override
    public boolean isValid() {
        return file.canRead();
    }

    @Override
    public SharedInputStream getMimeStream() throws IOException {
        SharedFileInputStream stream = this.stream;
        if (stream == null) {
            synchronized (file) {
                stream = this.stream;
                if (stream == null) {
                    stream = new SharedFileInputStream(file);
                    this.stream = stream;
                }
            }
        }

        // prevent from early eviction
        file.setLastModified(System.currentTimeMillis());

        return (SharedInputStream) stream.newStream(0, -1);
    }

    @Override
    public long getSize() {
        return file.exists() ? file.length() : -1L;
    }

    @Override
    public void cleanUp() {
        synchronized (file) {
            SharedFileInputStream stream = this.stream;
            if (stream != null) {
                Streams.close(stream);
                this.stream = null;
            }

            if (file.delete()) {
                FileCacheManager.incrementDeleteCounter("closed");
            }
        }
    }

    @Override
    public int hashCode() {
        int result = hash;
        if (result == 0) {
            int prime = 31;
            result = 1;
            result = prime * result + ((file == null) ? 0 : file.hashCode());
            hash = result;
        }
        return result;
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
        FileCacheReferenceImpl other = (FileCacheReferenceImpl) obj;
        if (file == null) {
            if (other.file != null) {
                return false;
            }
        } else if (!file.equals(other.file)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "FileCacheReference [" + file.getAbsolutePath() + "]";
    }

}
