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

package com.openexchange.i18n.parsing;

import java.io.InputStream;
import java.util.Properties;
import com.openexchange.exception.OXException;

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
        skipContexts(tokens);
        while (tokens.lookahead(POToken.MSGID)) {
            readTranslation(translations, tokens);
            if (null != translations.translate("") && headers.isEmpty()) {
                parseHeader(translations.translate(""));
                setCharSet(tokens);
            }
            skipContexts(tokens);
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

    private void readTranslation(final Translations translations, final POTokenStream tokens) throws OXException {
        tokens.consume(POToken.MSGID);
        final StringBuilder key = new StringBuilder((String) tokens.consume(POToken.TEXT).getData());
        collectTexts(tokens, key);

        StringBuilder alternateKey = null;
        if (tokens.lookahead(POToken.MSGID_PLURAL)) {
            alternateKey = new StringBuilder();
            tokens.consume(POToken.MSGID_PLURAL);
            collectTexts(tokens, alternateKey);
        }
        tokens.consume(POToken.MSGSTR);
        final StringBuilder value = new StringBuilder((String) tokens.consume(POToken.TEXT).getData());
        collectTexts(tokens, value);

        while (tokens.lookahead(POToken.MSGSTR)) {
            // Ignore other plurals for now
            tokens.consume(POToken.MSGSTR);
            collectTexts(tokens, null);
        }

        final String valueString = value.toString();
        if (!"".equals(valueString)) {
            translations.setTranslation(key.toString(), valueString);
            if (alternateKey != null) {
                translations.setTranslation(alternateKey.toString(), valueString);
            }
        }
    }

    private void skipContexts(final POTokenStream tokens) throws OXException {
        while (tokens.lookahead(POToken.MSGCTXT)) {
            tokens.consume(POToken.MSGCTXT);
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
