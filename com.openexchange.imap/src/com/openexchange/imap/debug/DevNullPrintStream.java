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

package com.openexchange.imap.debug;

import java.io.PrintStream;

/**
 * {@link DevNullPrintStream} - A print stream writing passed bytes to an instance of <code>org.slf4j.Logger</code>.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.4
 */
public class DevNullPrintStream extends PrintStream {

    private static final DevNullPrintStream INSTANCE = new DevNullPrintStream();

    /**
     * Gets the instance.
     *
     * @return The instance
     */
    public static DevNullPrintStream getInstance() {
        return INSTANCE;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Initializes a new {@link DevNullPrintStream}.
     */
    private DevNullPrintStream() {
        super(System.out); // hopefully nothing will actually reach stdout
    }

    @Override
    public void close() {
        // Nothing
    }

    @Override
    public void flush() {
        // Nothing
    }

    @Override
    public void print(boolean b) {
        // Nothing
    }

    @Override
    public void print(char c) {
        // Nothing
    }

    @Override
    public void print(char[] s) {
        // Nothing
    }

    @Override
    public void print(double d) {
        // Nothing
    }

    @Override
    public void print(float f) {
        // Nothing
    }

    @Override
    public void print(int i) {
        // Nothing
    }

    @Override
    public void print(long l) {
        // Nothing
    }

    @Override
    public void print(Object obj) {
        // Nothing
    }

    @Override
    public void print(String s) {
        // Nothing
    }

    @Override
    public void println() {
        // Nothing
    }

    @Override
    public void println(boolean x) {
        // Nothing
    }

    @Override
    public void println(char x) {
        // Nothing
    }

    @Override
    public void println(char[] x) {
        // Nothing
    }

    @Override
    public void println(double x) {
        // Nothing
    }

    @Override
    public void println(float x) {
        // Nothing
    }

    @Override
    public void println(int x) {
        // Nothing
    }

    @Override
    public void println(long x) {
        // Nothing
    }

    @Override
    public void println(Object x) {
        // Nothing
    }

    @Override
    public void println(String x) {
        // Nothing
    }

    @Override
    public void write(byte[] aBuf, int off, int len) {
        // Nothing
    }

    @Override
    public void write(int b) {
        // Nothing
    }

}
