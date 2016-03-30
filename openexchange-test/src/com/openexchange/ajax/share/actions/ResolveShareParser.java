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

package com.openexchange.ajax.share.actions;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import com.openexchange.ajax.framework.AbstractRedirectParser;
import com.openexchange.java.Charsets;
import com.openexchange.java.Strings;

/**
 * {@link ResolveShareParser}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ResolveShareParser extends AbstractRedirectParser<ResolveShareResponse> {

    /**
     * Initializes a new {@link ResolveShareParser}.
     */
    public ResolveShareParser() {
        this(true);
    }

    /**
     * Initializes a new {@link ResolveShareParser}.
     *
     * @param failOnNonRedirect <code>true</code> to fail if request is not redirected, <code>false</code>, otherwise
     */
    public ResolveShareParser(boolean failOnNonRedirect) {
        super(false, failOnNonRedirect, failOnNonRedirect);
    }

    @Override
    public String checkResponse(HttpResponse resp, HttpRequest request) throws ParseException, IOException {
        return super.checkResponse(resp, request);
    }

    @Override
    protected ResolveShareResponse createResponse(String location) {
        Map<String, String> map = Collections.emptyMap();
        String path = location;
        if (false == Strings.isEmpty(location)) {
            int fragIndex = location.indexOf('#');
            if (-1 != fragIndex) {
                path = location.substring(0, fragIndex);
                String hashData = location.substring(fragIndex + 1);
                if (hashData.startsWith("?")) {
                    map = deserialize(rot(decodeURIComponent(hashData.substring(1)), -1));
                } else {
                    map = deserialize(hashData);
                }
            }
        }
        return new ResolveShareResponse(getStatusCode(), path, map);
    }

    private Map<String, String> deserialize(String str) {
        Map<String, String> result = new HashMap<>();
        if (str == null) {
            str = "";
        }
        String[] pairs = str.split("&");
        for (int i = 0; i < pairs.length; i++) {
            String pair = pairs[i];
            String[] keyValue = pair.split("=");
            String key = null;
            String value = null;
            if (keyValue.length == 2) {
                key = keyValue[0];
                value = keyValue[1];
                if (Strings.isNotEmpty(key) || value != null) {
                    result.put(decodeURIComponent(key), decodeURIComponent(value));
                }
            }
        }
        return result;
    }

    private String rot(String str, int shift) {
        char[] chars = str.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            chars[i] = (char) (chars[i] + shift);
        }

        return new String(chars);
    }

    /**
     * Taken from org.apache.http.client.utils.URLEncodedUtils.urldecode() but slightly adjusted
     */
    private static String decodeURIComponent(String content) {
        if (content == null) {
            return null;
        }
        ByteBuffer bb = ByteBuffer.allocate(content.length());
        CharBuffer cb = CharBuffer.wrap(content);
        while (cb.hasRemaining()) {
            char c = cb.get();
            if (c == '%' && cb.remaining() >= 2) {
                char uc = cb.get();
                char lc = cb.get();
                int u = Character.digit(uc, 16);
                int l = Character.digit(lc, 16);
                if (u != -1 && l != -1) {
                    bb.put((byte) ((u << 4) + l));
                } else {
                    bb.put((byte) '%');
                    bb.put((byte) uc);
                    bb.put((byte) lc);
                }
            } else {
                bb.put((byte) c);
            }
        }
        bb.flip();
        return Charsets.UTF_8.decode(bb).toString();
    }

}
