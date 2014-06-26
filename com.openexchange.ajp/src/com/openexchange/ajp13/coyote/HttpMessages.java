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

package com.openexchange.ajp13.coyote;

/**
 * {@link HttpMessages}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class HttpMessages {

    static String st_200 = null;

    static String st_302 = null;

    static String st_400 = null;

    static String st_404 = null;

    /**
     * Get the status string associated with a status code. No I18N - return the messages defined in the HTTP spec. ( the user isn't
     * supposed to see them, this is the last thing to translate) Common messages are cached.
     */
    public static String getMessage(final int status) {
        // method from Response.

        // Does HTTP requires/allow international messages or
        // are pre-defined? The user doesn't see them most of the time
        switch (status) {
        case 200:
            if (st_200 == null) {
                st_200 = HttpServletResponseImpl.STATUS_MSGS.get(200);
            }
            return st_200;
        case 302:
            if (st_302 == null) {
                st_302 = HttpServletResponseImpl.STATUS_MSGS.get(302);
            }
            return st_302;
        case 400:
            if (st_400 == null) {
                st_400 = HttpServletResponseImpl.STATUS_MSGS.get(400);
            }
            return st_400;
        case 404:
            if (st_404 == null) {
                st_404 = HttpServletResponseImpl.STATUS_MSGS.get(404);
            }
            return st_404;
        }
        return HttpServletResponseImpl.STATUS_MSGS.get(status);
    }

    /**
     * Filter the specified message string for characters that are sensitive in HTML. This avoids potential attacks caused by including
     * JavaScript codes in the request URL that is often reported in error messages.
     *
     * @param message The message string to be filtered
     */
    public static String filter(final String message) {

        if (message == null) {
            return (null);
        }

        final char content[] = new char[message.length()];
        message.getChars(0, message.length(), content, 0);
        final StringBuffer result = new StringBuffer(content.length + 50);
        for (int i = 0; i < content.length; i++) {
            switch (content[i]) {
            case '<':
                result.append("&lt;");
                break;
            case '>':
                result.append("&gt;");
                break;
            case '&':
                result.append("&amp;");
                break;
            case '"':
                result.append("&quot;");
                break;
            default:
                result.append(content[i]);
            }
        }
        return (result.toString());
    }

    /**
     * Is the provided message safe to use in an HTTP header. Safe messages must meet the requirements of RFC2616 - i.e. must consist only
     * of TEXT.
     *
     * @param msg The message to test
     * @return <code>true</code> if the message is safe to use in an HTTP header else <code>false</code>
     */
    public static boolean isSafeInHttpHeader(final String msg) {
        // Nulls are fine. It is up to the calling code to address any NPE
        // concerns
        if (msg == null) {
            return true;
        }

        // Reason-Phrase is defined as *<TEXT, excluding CR, LF>
        // TEXT is defined as any OCTET except CTLs, but including LWS
        // OCTET is defined as an 8-bit sequence of data
        // CTL is defined as octets 0-31 and 127
        // LWS, if we exclude CR LF pairs, is defined as SP or HT (32, 9)
        final int len = msg.length();
        for (int i = 0; i < len; i++) {
            final char c = msg.charAt(i);
            if (32 <= c && c <= 126 || 128 <= c && c <= 255 || c == 9) {
                continue;
            }
            return false;
        }

        return true;
    }

}
