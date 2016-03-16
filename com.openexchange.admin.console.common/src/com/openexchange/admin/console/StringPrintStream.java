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

package com.openexchange.admin.console;

import java.io.OutputStream;
import java.io.PrintStream;

/**
 * {@link StringPrintStream} - Gathers everything written to print stream as a string.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class StringPrintStream extends PrintStream {

    /**
     * Creates a new {@link StringPrintStream} instance
     *
     * @param capacity The capacity
     * @return The new {@link StringPrintStream} instance
     */
    public static StringPrintStream newInstance(final int capacity) {
        return new StringPrintStream(new StringOutputStream(capacity));
    }

    // -------------------------------------------------------------------------------------------- //

    final StringOutputStream sos;

    private StringPrintStream(final StringOutputStream sos) {
        super(sos);
        this.sos = sos;
    }

    @Override
    public String toString() {
        return sos.builder.toString();
    }

    private static class StringOutputStream extends OutputStream {

        final StringBuilder builder;

        StringOutputStream(final int capacity) {
            builder = new StringBuilder(capacity);
        }

        @Override()
        public void close() {
            // Nope
        }

        @Override()
        public void flush() {
            // Nope
        }

        @Override()
        public void write(final byte[] b) {
            builder.append(new String(b));
        }

        @Override()
        public void write(final byte[] b, final int off, final int len) {
            final byte[] bytes = new byte[len];
            System.arraycopy(b, off, bytes, 0, len);
            builder.append(new String(bytes));
        }

        @Override
        public void write(final int b) {
            builder.append(b);
        }
    }

}
