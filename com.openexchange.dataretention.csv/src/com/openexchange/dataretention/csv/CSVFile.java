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

package com.openexchange.dataretention.csv;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * {@link CSVFile} - Represents a CSV file with an exchangeable file reference.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CSVFile {

    private final AtomicInteger even;

    private final long limit;

    private final boolean unlimited;

    private volatile File file;

    /**
     * Initializes a new {@link CSVFile}.
     */
    public CSVFile(final File directory) {
        super();
        // Set to unique dummy file
        file = new File(directory, UUID.randomUUID().toString());
        even = new AtomicInteger();
        limit = CSVDataRetentionConfig.getInstance().getRotateLength();
        unlimited = (limit <= 0);
    }

    /**
     * Atomically tests for file existence and if file limit is reached (if any).
     *
     * @return <code>true</code> if file exists and has not reached limit, yet; <code>false</code> otherwise
     */
    public boolean exists() {
        boolean acquire = true;
        while (acquire) {
            final int s = even.get();
            if ((s & 1) == 0) {
                acquire = !(even.compareAndSet(s, s + 1));
            }
        }
        try {
            final File tmp = file;
            return (tmp.exists() && (unlimited || tmp.length() < limit));
        } finally {
            even.incrementAndGet();
        }
    }

    /**
     * Resets this CSV file's exchangeable file reference to an unique file.
     */
    public void reset2Unique() {
        file = new File(file.getParentFile(), UUID.randomUUID().toString());
    }

    /**
     * Gets the file.
     *
     * @return The file
     */
    public File getFile() {
        return file;
    }

    /**
     * Sets the file.
     *
     * @param file The file to set
     */
    public void setFile(final File file) {
        if (null == file) {
            throw new IllegalArgumentException("File reference is null.");
        }
        this.file = file;
    }

    @Override
    public String toString() {
        return file.toString();
    }
}
