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

package com.openexchange.mail.structure;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import org.apache.commons.codec.binary.Base64OutputStream;
import org.apache.commons.logging.Log;
import org.json.JSONString;
import org.json.JSONStringOutputStream;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.java.StringAllocator;
import com.openexchange.mail.MailExceptionCode;

/**
 * {@link Base64JSONString} - A {@link JSONString JSON string} for one-time-retrieval of an input stream's base64-encoded bytes.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Base64JSONString implements JSONString {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(Base64JSONString.class);

    private final ThresholdFileHolder tfh;

    /**
     * Initializes a new {@link Base64JSONString}.
     *
     * @throws OXException If initialization fails
     */
    public Base64JSONString(final InputStream in) throws OXException {
        super();
        if (null == in) {
            final NullPointerException e = new NullPointerException("Input stream is null.");
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
        OutputStream out = null;
        try {
            /*
             * Suck input stream
             */
            final ThresholdFileHolder tfh = new ThresholdFileHolder();
            this.tfh = tfh;
            /*
             * Write encoded to threshold file holder
             */
            out = new Base64OutputStream(new JSONStringOutputStream(tfh.asOutputStream()), true, -1, null);
            final int buflen = 2048;
            final byte[] buf = new byte[buflen];
            for (int read; (read = in.read(buf, 0, buflen)) > 0;) {
                out.write(buf, 0, read);
            }
            out.flush();
        } catch (final IOException e) {
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            Streams.close(in, out);
        }
    }

    @Override
    public String toJSONString() {
        final StringAllocator sb = new StringAllocator(2048);

        final int cbuflen = 2048;
        final char[] cbuf = new char[cbuflen];

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(tfh.getStream(), com.openexchange.java.Charsets.UTF_8));
            for (int read = 0; (read = reader.read(cbuf, 0, cbuflen)) > 0;) {
                sb.append(cbuf, 0, read);
            }
            return sb.toString();
        } catch (final OXException e) {
            LOG.error(e.getMessage(), e);
            return "";
        } catch (final IOException e) {
            LOG.error(e.getMessage(), e);
            return "";
        } catch (final RuntimeException e) {
            LOG.error(e.getMessage(), e);
            return "";
        } finally {
            Streams.close(reader);
        }
    }

}
