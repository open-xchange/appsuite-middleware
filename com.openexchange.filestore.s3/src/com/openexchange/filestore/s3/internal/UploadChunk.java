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
 *     Copyright (C) 2004-2013 Open-Xchange, Inc.
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

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.tools.encoding.Base64;


/**
 * {@link UploadChunk}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class UploadChunk implements Closeable {

    /**
     * The minimum allowed chunk size for AWS multipart uploads, which is 5MB according to
     * http://docs.aws.amazon.com/AmazonS3/latest/API/mpUploadComplete.html
     */
    static final int MIN_CHUNK_SIZE = 5 * 1024 * 1024;

    private final String md5digest;
    private final ThresholdFileHolder fileHolder;

    /**
     * Initializes a new {@link UploadChunk} served by the supplied file holder.
     *
     * @param fileHolder The underlying file holder
     * @param md5digest The message digest
     */
    public UploadChunk(ThresholdFileHolder fileHolder, byte[] md5digest) {
        super();
        this.fileHolder = fileHolder;
        this.md5digest = Base64.encode(md5digest);
    }

    /**
     * Gets the size.
     *
     * @return the size
     */
    public long getSize() {
        return fileHolder.getCount();
    }

    /**
     * Gets the data.
     *
     * @return The data
     * @throws OXException
     */
    public InputStream getData() throws OXException {
        return fileHolder.getClosingStream();
    }

    /**
     * Gets the MD5 digest,
     *
     * @return The MD5 digest
     */
    public String getMD5Digest() {
        return md5digest;
    }

    @Override
    public void close() throws IOException {
        Streams.close(fileHolder);
    }

}
