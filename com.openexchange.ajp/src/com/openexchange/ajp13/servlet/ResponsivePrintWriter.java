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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.ajp13.servlet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

/**
 * {@link ResponsivePrintWriter} - A {@link PrintWriter print writer} responsive to thrown I/O errors.
 * <p>
 * Like {@link PrintWriter} a possible I/O error is swallowed quietly, but further attempts to write data are treated as a no-op since
 * {@link #checkError()} returns <code>true</code>.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ResponsivePrintWriter extends PrintWriter {

    /**
     * Initializes a new {@link ResponsivePrintWriter}.
     *
     * @param out
     */
    public ResponsivePrintWriter(final Writer out) {
        super(out);
    }

    /**
     * Initializes a new {@link ResponsivePrintWriter}.
     *
     * @param out
     */
    public ResponsivePrintWriter(final OutputStream out) {
        super(out);
    }

    /**
     * Initializes a new {@link ResponsivePrintWriter}.
     *
     * @param fileName
     * @throws FileNotFoundException
     */
    public ResponsivePrintWriter(final String fileName) throws FileNotFoundException {
        super(fileName);
    }

    /**
     * Initializes a new {@link ResponsivePrintWriter}.
     *
     * @param file
     * @throws FileNotFoundException
     */
    public ResponsivePrintWriter(final File file) throws FileNotFoundException {
        super(file);
    }

    /**
     * Initializes a new {@link ResponsivePrintWriter}.
     *
     * @param out
     * @param autoFlush
     */
    public ResponsivePrintWriter(final Writer out, final boolean autoFlush) {
        super(out, autoFlush);
    }

    /**
     * Initializes a new {@link ResponsivePrintWriter}.
     *
     * @param out
     * @param autoFlush
     */
    public ResponsivePrintWriter(final OutputStream out, final boolean autoFlush) {
        super(out, autoFlush);
    }

    /**
     * Initializes a new {@link ResponsivePrintWriter}.
     *
     * @param fileName
     * @param csn
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     */
    public ResponsivePrintWriter(final String fileName, final String csn) throws FileNotFoundException, UnsupportedEncodingException {
        super(fileName, csn);
    }

    /**
     * Initializes a new {@link ResponsivePrintWriter}.
     *
     * @param file
     * @param csn
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     */
    public ResponsivePrintWriter(final File file, final String csn) throws FileNotFoundException, UnsupportedEncodingException {
        super(file, csn);
    }

    /**
     * Write a single character.
     * <p>
     * If {@link #checkError()} is <code>true</code>, it is treated as a no-op.
     *
     * @param c int specifying a character to be written.
     */
    @Override
    public void write(final int c) {
        if (checkError()) {
            return;
        }
        super.write(c);
    }

    /**
     * Write A Portion of an array of characters.
     * <p>
     * If {@link #checkError()} is <code>true</code>, it is treated as a no-op.
     *
     * @param buf Array of characters
     * @param off Offset from which to start writing characters
     * @param len Number of characters to write
     */
    @Override
    public void write(final char buf[], final int off, final int len) {
        if (checkError()) {
            return;
        }
        super.write(buf, off, len);
    }

    /**
     * Write an array of characters. This method cannot be inherited from the Writer class because it must suppress I/O exceptions.
     * <p>
     * If {@link #checkError()} is <code>true</code>, it is treated as a no-op.
     *
     * @param buf Array of characters to be written
     */
    @Override
    public void write(final char buf[]) {
        write(buf, 0, buf.length);
    }

    /**
     * Write a portion of a string.
     * <p>
     * If {@link #checkError()} is <code>true</code>, it is treated as a no-op.
     *
     * @param s A String
     * @param off Offset from which to start writing characters
     * @param len Number of characters to write
     */
    @Override
    public void write(final String s, final int off, final int len) {
        if (checkError()) {
            return;
        }
        super.write(s, off, len);
    }

    /**
     * Write a string. This method cannot be inherited from the Writer class because it must suppress I/O exceptions.
     * <p>
     * If {@link #checkError()} is <code>true</code>, it is treated as a no-op.
     *
     * @param s String to be written
     */
    @Override
    public void write(final String s) {
        write(s, 0, s.length());
    }

}
