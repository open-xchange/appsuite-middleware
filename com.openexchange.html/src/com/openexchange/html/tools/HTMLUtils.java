/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
        } catch (DecoderException e) {
            return s;
        } catch (UnsupportedEncodingException e) {
            return s;
        }
    }
}
