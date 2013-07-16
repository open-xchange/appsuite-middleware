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

package com.openexchange.ajp13.coyote;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import javax.servlet.ServletOutputStream;

/**
 * {@link ActionAwarePrintWriter} - The {@link PrintWriter} backed by a {@link ServletOutputStream} instance.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ActionAwarePrintWriter extends PrintWriter {

    private static final NullWriter NULL_WRITER = new NullWriter();

    private final ActionAwareServletOutputStream outputStream;

    /**
     * Initializes a new {@link ActionAwarePrintWriter}.
     *
     * @param out
     */
    public ActionAwarePrintWriter(final ActionAwareServletOutputStream outputStream) {
        super(NULL_WRITER);
        this.outputStream = outputStream;
    }

    @Override
    public void print(final String s) {
        try {
            outputStream.print(s);
        } catch (final IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public void print(final boolean b) {
        try {
            outputStream.print(b);
        } catch (final IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public void print(final char c) {
        try {
            outputStream.print(c);
        } catch (final IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public void print(final int i) {
        try {
            outputStream.print(i);
        } catch (final IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public void flush() {
        try {
            outputStream.flush();
        } catch (final IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public void print(final long l) {
        try {
            outputStream.print(l);
        } catch (final IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public void print(final float f) {
        try {
            outputStream.print(f);
        } catch (final IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public void print(final double d) {
        try {
            outputStream.print(d);
        } catch (final IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public void println() {
        try {
            outputStream.println();
        } catch (final IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public void println(final String s) {
        try {
            outputStream.println(s);
        } catch (final IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public void println(final boolean b) {
        try {
            outputStream.println(b);
        } catch (final IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public void println(final char c) {
        try {
            outputStream.println(c);
        } catch (final IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public void println(final int i) {
        try {
            outputStream.println(i);
        } catch (final IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public void println(final long l) {
        try {
            outputStream.println(l);
        } catch (final IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public void println(final float f) {
        try {
            outputStream.println(f);
        } catch (final IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public void println(final double d) {
        try {
            outputStream.println(d);
        } catch (final IOException e) {
            // Ignore
        }
    }

    // ----------------------- Inner class --------------------------------s

    /**
     * This {@link Writer} writes all data to the famous <b>/dev/null</b>.
     * <p>
     * This <code>Writer</code> has no destination (file/socket etc.) and all characters written to it are ignored and lost.
     *
     * @version $Id: NullWriter.java 610010 2008-01-08 14:50:59Z niallp $
     */
    private static final class NullWriter extends Writer {

        /**
         * Constructs a new NullWriter.
         */
        public NullWriter() {
            super();
        }

        /**
         * Does nothing - output to <code>/dev/null</code>.
         *
         * @param idx The character to write
         */
        @Override
        public void write(final int idx) {
            // to /dev/null
        }

        /**
         * Does nothing - output to <code>/dev/null</code>.
         *
         * @param chr The characters to write
         */
        @Override
        public void write(final char[] chr) {
            // to /dev/null
        }

        /**
         * Does nothing - output to <code>/dev/null</code>.
         *
         * @param chr The characters to write
         * @param st The start offset
         * @param end The number of characters to write
         */
        @Override
        public void write(final char[] chr, final int st, final int end) {
            // to /dev/null
        }

        /**
         * Does nothing - output to <code>/dev/null</code>.
         *
         * @param str The string to write
         */
        @Override
        public void write(final String str) {
            // to /dev/null
        }

        /**
         * Does nothing - output to <code>/dev/null</code>.
         *
         * @param str The string to write
         * @param st The start offset
         * @param end The number of characters to write
         */
        @Override
        public void write(final String str, final int st, final int end) {
            // to /dev/null
        }

        @Override
        public void flush() {
            // to /dev/null
        }

        @Override
        public void close() {
            // to /dev/null
        }

    }
}
