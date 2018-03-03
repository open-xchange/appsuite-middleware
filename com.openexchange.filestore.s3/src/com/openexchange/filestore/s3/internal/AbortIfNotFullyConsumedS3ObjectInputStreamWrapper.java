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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.filestore.s3.internal;

import java.io.IOException;
import java.io.InputStream;
import com.amazonaws.services.s3.model.S3ObjectInputStream;

/**
 * {@link AbortIfNotFullyConsumedS3ObjectInputStreamWrapper}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class AbortIfNotFullyConsumedS3ObjectInputStreamWrapper extends InputStream {

    private final S3ObjectInputStream objectContent;

    /**
     * Initializes a new {@link AbortIfNotFullyConsumedS3ObjectInputStreamWrapper}.
     *
     * @param objectContent The input stream containing the contents of an object
     */
    public AbortIfNotFullyConsumedS3ObjectInputStreamWrapper(S3ObjectInputStream objectContent) {
        super();
        this.objectContent = objectContent;
    }

    @Override
    public int read() throws IOException {
        return objectContent.read();
    }

    @Override
    public int read(byte b[]) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte b[], int off, int len) throws IOException {
        return objectContent.read(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException {
        return objectContent.skip(n);
    }

    @Override
    public int available() throws IOException {
        return objectContent.available();
    }

    @Override
    public void close() throws IOException {
        if (objectContent.read() >= 0) {
            // Abort HTTP connection in case not all bytes were read from the S3ObjectInputStream
            objectContent.abort();
        }
        objectContent.close();
    }

    @Override
    public void mark(int readlimit) {
        objectContent.mark(readlimit);
    }

    @Override
    public void reset() throws IOException {
        objectContent.reset();
    }

    @Override
    public boolean markSupported() {
        return objectContent.markSupported();
    }

}
