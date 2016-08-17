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

package com.openexchange.i18n.parsing;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class POParser {

    private final Properties headers = new Properties();

    public POParser() {
        super();
    }

    public Translations parse(final InputStream stream, final String filename) throws OXException {
        final Translations translations = new Translations();

        final POTokenStream tokens = new POTokenStream(stream, filename);
        while (tokens.lookahead(POToken.MSGCTXT) || tokens.lookahead(POToken.MSGID)) {
            readFullTranslation(translations, tokens);
            if (headers.isEmpty() && Strings.isNotEmpty(translations.translate(""))) {
                parseHeader(translations.translate(""));
                setCharSet(tokens);
                translations.setTranslation("", ""); // remove header section again after headers are parsed
            }
        }

        return translations;
    }

    private void parseHeader(final String header) {
        final String[] lines = header.split("\\n");
        for (final String line : lines) {
            if (null == line || 0 == line.length()) {
                continue;
            }
            final int separator = line.indexOf(':');
            if (-1 == separator) {
                continue;
            }
            headers.put(line.substring(0, separator), line.substring(separator + 1));
        }
    }

    private void setCharSet(final POTokenStream tokens) {
        final String contentType = headers.getProperty("Content-Type");
        if (null == contentType) {
            return;
        }
        final int pos = contentType.indexOf("charset=");
        final String charset = contentType.substring(pos + 8);
        tokens.setCharset(charset);
    }

    private void readFullTranslation(Translations translations, POTokenStream tokens) throws OXException {

        String context = null;
        if (tokens.lookahead(POToken.MSGCTXT)) {
            tokens.consume(POToken.MSGCTXT);
            StringBuilder sb = new StringBuilder();
            collectTexts(tokens, sb);
            context = sb.toString();
        }

        tokens.consume(POToken.MSGID);
        StringBuilder key = new StringBuilder();
        collectTexts(tokens, key);

        String keyPlural = null;
        if (tokens.lookahead(POToken.MSGID_PLURAL)) {
            StringBuilder sb = new StringBuilder();
            tokens.consume(POToken.MSGID_PLURAL);
            collectTexts(tokens, sb);
            keyPlural = sb.toString();
        }

        List<String> strings = new LinkedList<String>();
        do {
            tokens.consume(POToken.MSGSTR);
            StringBuilder string = new StringBuilder();
            collectTexts(tokens, string);
            String s = string.toString();
            if (!Strings.isEmpty(s)) {
                strings.add(s);
            }
        } while (tokens.lookahead(POToken.MSGSTR));

        if (!strings.isEmpty()) {
            translations.setContextTranslationPlural(context, key.toString(), keyPlural, strings);
        }
    }

    private void collectTexts(final POTokenStream tokens, final StringBuilder builder) throws OXException {
        while (tokens.lookahead(POToken.TEXT)) {
            final Object data = tokens.consume(POToken.TEXT).getData();
            if (builder != null) {
                builder.append(data);
            }
        }
    }
}
