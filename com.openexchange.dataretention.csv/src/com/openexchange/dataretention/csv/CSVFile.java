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
