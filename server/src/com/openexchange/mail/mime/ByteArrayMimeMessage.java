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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.mail.mime;

import javax.mail.Flags;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeMessage;
import com.openexchange.tools.stream.UnsynchronizedByteArrayInputStream;

/**
 * {@link ByteArrayMimeMessage} - A {@link MimeMessage} backed by an array.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ByteArrayMimeMessage extends MimeMessage {

    /**
     * Initializes a new {@link ByteArrayMimeMessage}.
     * 
     * @param session The session
     * @param sourceBytes The RFC822 source bytes
     * @throws MessagingException
     */
    public ByteArrayMimeMessage(final Session session, final byte[] sourceBytes) throws MessagingException {
        super(session);
        flags = new Flags(); // empty Flags object
        final byte[][] splitted = split(sourceBytes);
        headers = splitted[0].length == 0 ? new InternetHeaders() : new InternetHeaders(new UnsynchronizedByteArrayInputStream(splitted[0]));
        content = splitted[1];
        modified = false;
        saved = true;
    }

    private static final byte[][] split(final byte[] sourceBytes) {
        byte[] pattern = new byte[] { '\r', '\n', '\r', '\n' };
        int pos = indexOf(sourceBytes, pattern, 0, computeFailure(pattern));
        if (pos >= 0) {
            // Double CRLF found
            final byte[] a = new byte[pos];
            final int endPos = pos + 4;
            final byte[] b = new byte[sourceBytes.length - endPos];
            System.arraycopy(sourceBytes, 0, a, 0, a.length);
            System.arraycopy(sourceBytes, endPos, b, 0, b.length);
            return new byte[][] { a, b };
        }
        pattern = new byte[] { '\n', '\n' };
        pos = indexOf(sourceBytes, pattern, 0, computeFailure(pattern));
        if (pos >= 0) {
            // Double LF found
            final byte[] a = new byte[pos];
            final int endPos = pos + 2;
            final byte[] b = new byte[sourceBytes.length - endPos];
            System.arraycopy(sourceBytes, 0, a, 0, a.length);
            System.arraycopy(sourceBytes, endPos, b, 0, b.length);
            return new byte[][] { a, b };
        }
        // Neither double CRLF nor double LF found
        return new byte[][] { new byte[] {}, sourceBytes };
    }

    private static int indexOf(final byte[] data, final byte[] pattern, final int beginIndex, final int[] failure) {
        final int length = data.length;
        if ((beginIndex < 0) || (beginIndex > length)) {
            throw new IndexOutOfBoundsException(String.valueOf(beginIndex));
        }

        int j = 0;
        if (length == 0) {
            return -1;
        }

        for (int i = beginIndex; i < length; i++) {
            while (j > 0 && pattern[j] != data[i]) {
                j = failure[j - 1];
            }
            if (pattern[j] == data[i]) {
                j++;
            }
            if (j == pattern.length) {
                return i - pattern.length + 1;
            }
        }
        return -1;
    }

    private static int[] computeFailure(final byte[] pattern) {
        if (pattern == null) {
            return null;
        }
        final int[] failure = new int[pattern.length];

        int j = 0;
        for (int i = 1; i < pattern.length; i++) {
            while (j > 0 && pattern[j] != pattern[i]) {
                j = failure[j - 1];
            }
            if (pattern[j] == pattern[i]) {
                j++;
            }
            failure[i] = j;
        }
        return failure;
    }

}
