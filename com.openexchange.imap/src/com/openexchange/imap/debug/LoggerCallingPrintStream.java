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
import java.nio.charset.StandardCharsets;
import com.openexchange.java.Strings;

/**
 * {@link LoggerCallingPrintStream} - A print stream writing passed bytes to an instance of <code>org.slf4j.Logger</code>.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.4
 */
public class LoggerCallingPrintStream extends PrintStream {

    private org.slf4j.Logger logger;
    private final StringBuffer buf;
    private final javax.mail.Session imapSession;
    private final String server;
    private final int userId;
    private final int contextId;

    /**
     * Initializes a new {@link LoggerCallingPrintStream}.
     *
     * @param imapSession The IMAP session to enabled debug logging for
     * @param server The IMAP server
     * @param userId The user identifier
     * @param contextId The context identifier
     */
    public LoggerCallingPrintStream(javax.mail.Session imapSession, String server, int userId, int contextId) {
        super(System.out); // hopefully nothing will actually reach stdout
        this.imapSession = imapSession;
        this.server = server;
        this.userId = userId;
        this.contextId = contextId;
        buf = new StringBuffer();
    }

    @Override
    public void close() {
        flush();
    }

    @Override
    public synchronized void flush() {
        if (buf.length() > 0) {
            org.slf4j.Logger logger = this.logger;
            if (logger == null) {
                logger = IMAPDebugLoggerGenerator.generateLoggerFor(imapSession, server, userId, contextId);
                this.logger = logger;
            }
            logger.info(buf.toString());
            buf.setLength(0);
        }
    }

    @Override
    public void print(boolean b) {
        print(Boolean.toString(b));
    }

    @Override
    public void print(char c) {
        print(Character.toString(c));
    }

    @Override
    public void print(char[] s) {
        print(new String(s));
    }

    @Override
    public void print(double d) {
        print(Double.toString(d));
    }

    @Override
    public void print(float f) {
        print(Float.toString(f));
    }

    @Override
    public void print(int i) {
        print(Integer.toString(i));
    }

    @Override
    public void print(long l) {
        print(Long.toString(l));
    }

    @Override
    public void print(Object obj) {
        print(obj == null ? "null" : obj.toString());
    }

    @Override
    public void print(String s) {
        buf.append(s);
    }

    @Override
    public void println() {
        flush();
    }

    @Override
    public void println(boolean x) {
        println(Boolean.toString(x));
    }

    @Override
    public void println(char x) {
        println(Character.toString(x));
    }

    @Override
    public void println(char[] x) {
        println(new String(x));
    }

    @Override
    public void println(double x) {
        println(Double.toString(x));
    }

    @Override
    public void println(float x) {
        println(Float.toString(x));
    }

    @Override
    public void println(int x) {
        println(Integer.toString(x));
    }

    @Override
    public void println(long x) {
        println(Long.toString(x));
    }

    @Override
    public void println(Object x) {
        println(x == null ? "null" : x.toString());
    }

    @Override
    public void println(String x) {
        buf.append(x);
        flush();
    }

    @Override
    public void write(byte[] aBuf, int off, int len) {
        String str = new String(aBuf, off, len, StandardCharsets.UTF_8);
        String[] lines = Strings.splitByCRLF(str);
        for (int i = 0; i < lines.length - 1; i++) {
            println(lines[i]);
        }
        String lastLine = lines[lines.length - 1];
        if (lastLine.endsWith("\n")) {
            println(lastLine);
        } else {
            print(lastLine);
        }
    }

    @Override
    public void write(int b) {
        print(new String(new byte[] { (byte) b }, StandardCharsets.UTF_8));
    }

}
