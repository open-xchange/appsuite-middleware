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

package javax.servlet.sim;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;

/**
 * {@link ByteArrayServletOutputStream} - A {@code ServletOutputStream} backed by a {@code ByteArrayOutputStream}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ByteArrayServletOutputStream extends ServletOutputStream {

    private final ByteArrayOutputStream out;

    /**
     * Initializes a new {@link ByteArrayServletOutputStream}.
     */
    public ByteArrayServletOutputStream() {
        super();
        out = new ByteArrayOutputStream(2048);
    }

    @Override
    public void write(final byte[] b) throws IOException {
        out.write(b);
    }

    @Override
    public void write(final int b) {
        out.write(b);
    }

    @Override
    public void write(final byte[] b, final int off, final int len) {
        out.write(b, off, len);
    }

    public void reset() {
        out.reset();
    }

    @Override
    public boolean equals(final Object obj) {
        return out.equals(obj);
    }

    /**
     * Creates a newly allocated byte array. Its size is the current size of this output stream and the valid contents of the buffer have
     * been copied into it.
     *
     * @return the current contents of this output stream, as a byte array.
     */
    public byte[] toByteArray() {
        return out.toByteArray();
    }

    @Override
    public void flush() throws IOException {
        out.flush();
    }

    /**
     * Returns the current size of the buffer.
     *
     * @return the value of the <code>count</code> field, which is the number of valid bytes in this output stream.
     */
    public int size() {
        return out.size();
    }

    @Override
    public String toString() {
        return out.toString();
    }

    public String toString(final String charsetName) throws UnsupportedEncodingException {
        return out.toString(charsetName);
    }

    @Override
    public void close() throws IOException {
        out.close();
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void setWriteListener(WriteListener writeListener) {
        // Nope
    }

}
