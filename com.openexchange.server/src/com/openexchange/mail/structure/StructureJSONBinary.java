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

package com.openexchange.mail.structure;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import org.apache.commons.codec.binary.Base64OutputStream;
import org.json.JSONBinary;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.mail.MailExceptionCode;

/**
 * {@link StructureJSONBinary} - A {@link JSONBinary JSON binary} for one-time-retrieval of an input stream's base64-encoded bytes.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class StructureJSONBinary implements JSONBinary {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(StructureJSONBinary.class);

    private final ThresholdFileHolder tfh;
    private final InputStream in;

    /**
     * Initializes a new {@link StructureJSONBinary}.
     *
     * @throws OXException If initialization fails
     */
    public StructureJSONBinary(final InputStream in) throws OXException {
        this(in, true);
    }

    /**
     * Initializes a new {@link StructureJSONBinary}.
     *
     * @throws OXException If initialization fails
     */
    public StructureJSONBinary(final InputStream in, final boolean copy) throws OXException {
        super();
        if (null == in) {
            final NullPointerException e = new NullPointerException("Input stream is null.");
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
        if (copy) {
            // Copy to ThresholdFileHolder instance
            this.in = null;
            try {
                // Write input stream into ThresholdFileHolder instance
                final ThresholdFileHolder tfh = new ThresholdFileHolder();
                this.tfh = tfh;
                tfh.write(in);
            } catch (final RuntimeException e) {
                throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
            } finally {
                Streams.close(in);
            }
        } else {
            // Just assign stream
            tfh = null;
            this.in = in;
        }
    }

    @Override
    public String toString() {
        InputStream in = null;
        try {
            in = this.in;
            if (null == in) {
                in = tfh.getStream();
            }
            ByteArrayOutputStream bout = Streams.newByteArrayOutputStream(8192);
            Base64OutputStream base64Out = new Base64OutputStream(bout, true, -1, null);
            final int blen = 2048;
            final byte[] buf = new byte[blen];
            for (int read; (read = in.read(buf, 0, blen)) > 0;) {
                base64Out.write(buf, 0, read);
            }
            base64Out.flush();
            base64Out.close();
            return bout.toString("US-ASCII");
        } catch (final Exception e) {
            LOG.error("", e);
            return "";
        } finally {
            Streams.close(in);
        }
    }

    @Override
    public InputStream getBinary() {
        final ThresholdFileHolder tfh = this.tfh;
        if (null == tfh) {
            return in;
        }
        try {
            return tfh.getClosingStream();
        } catch (OXException e) {
            LOG.error("", e);
            return Streams.EMPTY_INPUT_STREAM;
        }
    }

    @Override
    public long length() {
        final ThresholdFileHolder tfh = this.tfh;
        return null == tfh ? -1L : tfh.getLength();
    }

}
