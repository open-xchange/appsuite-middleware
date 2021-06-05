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
            if (Strings.isNotEmpty(s)) {
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
