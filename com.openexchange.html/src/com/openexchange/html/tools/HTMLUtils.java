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

package com.openexchange.html.tools;

import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.codec.CharEncoding;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.URLCodec;
import com.openexchange.html.HtmlService;

/**
 * {@link HTMLUtils}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class HTMLUtils {

    private static final Pattern BR = Pattern.compile("<br />( *)");

    private final HtmlService htmlService;

    public HTMLUtils(final HtmlService htmlService) {
        super();
        this.htmlService = htmlService;
    }

    public String htmlFormat(final String plainText) {
        String html = htmlService.htmlFormat(plainText);
        final StringBuffer sb = new StringBuffer(html.length());
        final Matcher matcher = BR.matcher(html);
        while (matcher.find()) {
            final String spaces = matcher.group(1);
            final StringBuilder replacement = new StringBuilder("<br />");
            for (int i = 0; i < spaces.length(); i++) {
                replacement.append("&#160;");
            }
            matcher.appendReplacement(sb, com.openexchange.java.Strings.quoteReplacement(replacement.toString()));
        }
        html = matcher.appendTail(sb).toString();
        return html;
    }

    private static final URLCodec URL_CODEC = new URLCodec(CharEncoding.ISO_8859_1);

    /**
     * URL decodes given string.
     * <p>
     * Using <code>org.apache.commons.codec.net.URLCodec</code>.
     */
    public static String decodeUrl(final String s, final String charset) {
        try {
            return com.openexchange.java.Strings.isEmpty(s) ? s : (com.openexchange.java.Strings.isEmpty(charset) ? URL_CODEC.decode(s) : URL_CODEC.decode(s, charset));
        } catch (final DecoderException e) {
            return s;
        } catch (final UnsupportedEncodingException e) {
            return s;
        }
    }
}
