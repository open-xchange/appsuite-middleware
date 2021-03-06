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

package com.openexchange.importexport.helpers;

import java.io.IOException;
import java.io.OutputStream;
import javax.servlet.http.HttpServletResponse;

/**
 * {@link DelayInitServletOutputStream} - Delegates to an HTTP response's output stream, which is only obtained when actually needed.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.1
 */
public class DelayInitServletOutputStream extends OutputStream {

    private final HttpServletResponse response;
    private OutputStream out;
    private boolean closed;

    /**
     * Initializes a new {@link DelayInitServletOutputStream}.
     */
    public DelayInitServletOutputStream(HttpServletResponse response) {
        super();
        this.response = response;
        closed = false;
    }

    private OutputStream out() throws IOException {
        OutputStream out = this.out;
        if (null == out) {
            out = response.getOutputStream();
            this.out = out;
        }
        return out;
    }

    @Override
    public void write(int b) throws IOException {
        OutputStream out = out();
        out.write(b);
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        OutputStream out = out();
        out.write(bytes);
    }

    @Override
    public void write(byte[] bytes, int off, int len) throws IOException {
        OutputStream out = out();
        out.write(bytes, off, len);
    }

    @Override
    public void flush() throws IOException {
        OutputStream out = out();
        out.flush();
    }

    @Override
    public void close() throws IOException {
        OutputStream out = this.out;
        if (null == out) {
            // Not yet initialized
            return;
        }

        if (closed) {
            return;
        }

        try {
            out.flush();
        } finally {
            closed = true;
            out.close();
        }
    }

}
