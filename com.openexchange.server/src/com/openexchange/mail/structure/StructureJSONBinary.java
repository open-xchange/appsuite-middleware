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
    public StructureJSONBinary(InputStream in) throws OXException {
        this(in, true);
    }

    /**
     * Initializes a new {@link StructureJSONBinary}.
     *
     * @throws OXException If initialization fails
     */
    public StructureJSONBinary(InputStream in, boolean copy) throws OXException {
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
            } catch (RuntimeException e) {
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
        } catch (Exception e) {
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
