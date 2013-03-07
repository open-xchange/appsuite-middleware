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

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.openexchange.caching.hazelcast.util;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

/**
 * {@link LineInputStream} - This class is to support reading CRLF terminated lines that contain only US-ASCII characters from an input
 * stream.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class LineInputStream extends FilterInputStream {

    private char[] lineBuffer = null; // reusable byte buffer

    private static int MAX_INCR = 1024 * 1024; // 1MB

    public LineInputStream(InputStream in) {
        super(in);
    }

    /**
     * Reads a line containing only ASCII characters from the input stream. A line is terminated by a CR or NL or CR-NL sequence. A common
     * error is a CR-CR-NL sequence, which will also terminate a line. The line terminator is not returned as part of the returned string.
     * Returns <code>null</code> if no data is available.
     */
    public String readLine() throws IOException {
        char[] buf = lineBuffer;
        if (buf == null) {
            buf = lineBuffer = new char[128];
        }

        int c1;
        int room = buf.length;
        int offset = 0;

        while ((c1 = in.read()) != -1) {
            if (c1 == '\n') { // Got NL, outa here.
                break;
            } else if (c1 == '\r') {
                // Got CR, is the next char NL ?
                boolean twoCRs = false;
                if (in.markSupported()) {
                    in.mark(2);
                }
                int c2 = in.read();
                if (c2 == '\r') { // discard extraneous CR
                    twoCRs = true;
                    c2 = in.read();
                }
                if (c2 != '\n') {
                    /*
                     * If the stream supports it (which we hope will always be the case), reset to after the first CR. Otherwise, we wrap a
                     * PushbackInputStream around the stream so we can unread the characters we don't need. The only problem with that is
                     * that the caller might stop reading from this LineInputStream, throw it away, and then start reading from the
                     * underlying stream. If that happens, the pushed back characters will be lost forever.
                     */
                    if (in.markSupported()) {
                        in.reset();
                    } else {
                        if (!(in instanceof PushbackInputStream)) {
                            in /* = this.in */= new PushbackInputStream(in, 2);
                        }
                        if (c2 != -1) {
                            ((PushbackInputStream) in).unread(c2);
                        }
                        if (twoCRs) {
                            ((PushbackInputStream) in).unread('\r');
                        }
                    }
                }
                break; // outa here.
            }

            // Not CR, NL or CR-NL ...
            // .. Insert the byte into our byte buffer
            if (--room < 0) { // No room, need to grow.
                if (buf.length < MAX_INCR) {
                    buf = new char[buf.length << 1];
                } else {
                    buf = new char[buf.length + MAX_INCR];
                }
                room = buf.length - offset - 1;
                System.arraycopy(lineBuffer, 0, buf, 0, offset);
                lineBuffer = buf;
            }
            buf[offset++] = (char) c1;
        }

        if ((c1 == -1) && (offset == 0)) {
            return null;
        }

        return String.copyValueOf(buf, 0, offset);
    }
}
