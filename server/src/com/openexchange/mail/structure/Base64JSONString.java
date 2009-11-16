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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.mail.structure;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;
import org.json.JSONString;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;

/**
 * {@link Base64JSONString} - A {@link JSONString JSON string} for one-time-retrieval of an input stream's base64-encoded bytes.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Base64JSONString implements JSONString {

    private static final int BUFLEN = 8192;

    private final AtomicReference<InputStream> inReference;

    /**
     * Initializes a new {@link Base64JSONString}.
     */
    public Base64JSONString(final InputStream in) {
        super();
        if (null == in) {
            throw new NullPointerException("Input stream is null.");
        }
        inReference = new AtomicReference<InputStream>(in);
    }

    public String toJSONString() {
        final InputStream in = inReference.get();
        if (null == in) {
            return "null";
        }
        if (!inReference.compareAndSet(in, null)) {
            return "null";
        }
        final byte[] bytes;
        try {
            final byte[] buf = new byte[BUFLEN];
            final ByteArrayOutputStream out = new UnsynchronizedByteArrayOutputStream(BUFLEN << 2);
            int read;
            while ((read = in.read(buf, 0, BUFLEN)) >= 0) {
                out.write(buf, 0, read);
            }
            bytes = out.toByteArray();
        } catch (final IOException e) {
            throw new IllegalStateException("Input stream could not be read", e);
        } finally {
            try {
                in.close();
            } catch (final IOException e) {
                org.apache.commons.logging.LogFactory.getLog(Base64JSONString.class).error(e.getMessage(), e);
            }
        }
        try {
            return JSONObject.quote(new String(Base64.encodeBase64(bytes, false), "US-ASCII"));
        } catch (final UnsupportedEncodingException e) {
            // Cannot occur
            throw new IllegalStateException(e);
        }
    }

}
