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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import com.openexchange.exception.OXException;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
final class POTokenStream {

    // ---------------------------------------------------------------------------------------------------------------//

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(POTokenStream.class);

    private final InputStream stream;

    private Charset charset;

    private POToken nextToken;

    private POElement nextElement;

    private final String filename;

    private int line;

    public POTokenStream(final InputStream stream, final String filename) throws OXException {
        super();
        this.stream = stream;
        charset = Charset.defaultCharset();
        this.filename = filename;
        line = 1;
        initNextToken();
    }

    public void setCharset(final String charset) {
        try {
            this.charset = Charset.forName(charset);
        } catch (java.nio.charset.UnsupportedCharsetException e) {
            LOG.warn("Unsupported charset: \"{}\". Therefore using fall-back \"UTF-8\". Forgot to replace header appropriately?", charset);
            this.charset = Charsets.UTF_8;
        }
    }

    public boolean lookahead(final POToken token) {
        return nextToken == token;
    }

    public POElement consume(final POToken token) throws OXException {
        if (lookahead(token)) {
            final POElement element = nextElement;
            initNextToken();
            return element;
        }
        throw I18NExceptionCode.UNEXPECTED_TOKEN_CONSUME.create(
            nextToken.name().toLowerCase(),
            filename,
            Integer.valueOf(line),
            "[" + token.name().toLowerCase() + "]");
    }

    private void initNextToken() throws OXException {
        boolean repeat = true;
        while (repeat) {
            repeat = false;
            byte c = read();
            while (Character.isWhitespace(c)) {
                c = read();
            }
            switch (c) {
            case 'm':
                msgIdOrMsgStr();
                break;
            case '"':
                string();
                break;
            case '#':
                // Discard comment and repeat look-up for next token
                comment();
                repeat = true;
                break;
            case -1:
                eof();
                break;
            default:
                final ByteArrayOutputStream baos = Streams.newByteArrayOutputStream();
                baos.write(c);
                while ((c = read()) != -1 && c != '\n') {
                    baos.write(c);
                }
                throw I18NExceptionCode.UNEXPECTED_TOKEN.create(
                    toString(baos.toByteArray()),
                    filename,
                    Integer.valueOf(line - 1),
                    "[msgid, msgctxt, msgstr, string, comment, eof]");
            }
        }

    }

    private byte read() throws OXException {
        try {
            final byte b = (byte) stream.read();
            if ('\n' == b) {
                line++;
            }
            return b;
        } catch (IOException e) {
            throw I18NExceptionCode.IO_EXCEPTION.create(e, filename);
        }
    }

    private void comment() throws OXException {
        /*
         * StringBuilder data = new StringBuilder(); int c = -1; while ((c = read()) != '\n') { data.append((char) c); } nextToken =
         * POToken.COMMENT; element(data.toString());
         */
        // Ignore comments
        byte b;
        while ((b = read()) != '\n' && b != -1) {
            // Discard
        }
        //initNextToken();
    }

    private String toString(final byte[] b) {
        return charset.decode(ByteBuffer.wrap(b)).toString();
    }

    private static String decode(final String orig) {
        final StringBuilder sb = new StringBuilder(orig);
        int pos = sb.indexOf("\\");
        while (pos != -1) {
            final char c = sb.charAt(pos + 1);
            switch (c) {
            case 'n':
                sb.replace(pos, pos + 2, "\n");
                break;
            case '"':
                sb.replace(pos, pos + 2, "\"");
                break;
            default:
            }
            pos = sb.indexOf("\\", pos + 1);
        }
        return sb.toString();
    }

    private void string() throws OXException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte c = read();
        byte last = 0;
        while ((c != '"' || last == '\\') && c != '\n' && c != -1) {
            baos.write(c);
            last = c;
            c = read();
        }
        while (c != '\n' && c != -1) {
            c = read();
        }
        nextToken = POToken.TEXT;
        element(decode(toString(baos.toByteArray())));
    }

    private void eof() {
        nextToken = POToken.EOF;
        element(null);
    }

    private void msgIdOrMsgStr() throws OXException {
        expect('s', 'g');
        switch (read()) {
        case 'i':
            expect('d');
            switch (read()) {
            case '_':
                expect('p', 'l', 'u', 'r', 'a', 'l');
                msgIdPluralToken();
                break;
            default:
                msgIdToken();
            }
            break;
        case 's':
            expect('t', 'r');
            switch (read()) {
            case '[':
                final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte c;
                while ((c = read()) != ']') {
                    baos.write(c);
                }
                msgStrToken(toString(baos.toByteArray()));
                break;
            default:
                msgStrToken(null);
            }
            break;
        case 'c':
            expect('t', 'x', 't');
            msgctxtToken();
            break;
        }
    }

    private void msgctxtToken() {
        nextToken = POToken.MSGCTXT;
        element(null);
    }

    private void msgIdPluralToken() {
        nextToken = POToken.MSGID_PLURAL;
        element(null);
    }

    private void msgStrToken(final String number) throws OXException {
        nextToken = POToken.MSGSTR;
        try {
            if (number == null) {
                element(null);
            } else {
                element(Integer.valueOf(number));
            }
        } catch (NumberFormatException x) {
            throw I18NExceptionCode.EXPECTED_NUMBER.create(number, filename, Integer.valueOf(line));
        }
    }

    private void msgIdToken() {
        nextToken = POToken.MSGID;
        element(null);
    }

    private void element(final Object data) {
        nextElement = new POElement(nextToken, data);
    }

    private void expect(final char... characters) throws OXException {
        for (final char c : characters) {
            final byte readC = read();
            if (readC != c) {
                throw I18NExceptionCode.MALFORMED_TOKEN.create("" + (char) readC, "" + c, filename, Integer.valueOf(line));
            }
        }
    }
}
