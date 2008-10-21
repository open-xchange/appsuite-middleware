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
package com.openexchange.i18n.parsing;

import java.io.Reader;
import java.io.IOException;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class POParser {
    public static final int CLASS_ID = 1;

    public Translations parse(Reader reader, String filename) throws I18NException {
        Translations translations = new Translations();

        POTokenStream tokens = new POTokenStream(reader, filename);
        skipContexts(tokens);
        while(tokens.lookahead(POToken.MSGID)) {
            readTranslation(translations, tokens);
            skipContexts(tokens);
        }

        return translations;
    }

    private void readTranslation(Translations translations, POTokenStream tokens) throws I18NException {
        tokens.consume(POToken.MSGID);
        StringBuilder key = new StringBuilder((String)tokens.consume(POToken.TEXT).data);
        collectTexts(tokens, key);

        StringBuilder alternateKey = null;
        if(tokens.lookahead(POToken.MSGID_PLURAL)) {
            alternateKey = new StringBuilder();
            tokens.consume(POToken.MSGID_PLURAL);
            collectTexts(tokens, alternateKey);
        }
        tokens.consume(POToken.MSGSTR);
        StringBuilder value = new StringBuilder((String)tokens.consume(POToken.TEXT).data);
        collectTexts(tokens, value);

        while(tokens.lookahead(POToken.MSGSTR)) {
            // Ignore other plurals for now
            tokens.consume(POToken.MSGSTR);
            collectTexts(tokens, null);
        }

        String valueString = value.toString();
        translations.setTranslation(key.toString(), valueString);
        if(alternateKey != null) {
            translations.setTranslation(alternateKey.toString(), valueString);
        }
    }
    private void skipContexts(POTokenStream tokens) throws I18NException {
        while(tokens.lookahead(POToken.MSGCTXT)) { tokens.consume(POToken.MSGCTXT); }
    }

    private void collectTexts(POTokenStream tokens, StringBuilder builder) throws I18NException {
        while(tokens.lookahead(POToken.TEXT)) {
            Object data = tokens.consume(POParser.POToken.TEXT).data;
            if(builder != null) {
                builder.append(data);
            }
        }
    }

    private static enum POToken {
        MSGCTXT,
        MSGID,
        MSGID_PLURAL,
        MSGSTR,
        TEXT,
        COMMENT,
        EOF
    }

    private static final class POElement {
        public POToken token;
        public Object data;

        private POElement(POToken token, Object data) {
            this.token = token;
            this.data = data;
        }
    }

    private static final class POTokenStream {
        private Reader reader;
        private POToken nextToken;
        private POElement nextElement;
        private String filename;
        private int line;

        public POTokenStream(Reader reader, String filename) throws I18NException {
            this.reader = reader;
            this.filename = filename;
            line = 1;
            initNextToken();
        }

        public boolean lookahead(POToken token) {
            return nextToken == token;
        }

        public POElement consume(POToken token) throws I18NException {
            if(lookahead(token)) {
                POElement element = nextElement;
                initNextToken();
                return element;
            }
            I18NErrorMessages.UNEXPECTED_TOKEN_CONSUME.throwException(nextToken.name().toLowerCase(), filename, line, "["+token.name().toLowerCase()+"]");
            return null; // Never reached
        }

        private void initNextToken() throws I18NException {
            int c = read();
            while(Character.isWhitespace(c)) {
                c = read();
            }
            switch(c) {
                case 'm' :
                    msgIdOrMsgStr(); break;
                case '"' :
                    string(); break;
                case '#':
                    comment(); break;
                case -1 :
                    eof(); break;
                default:
                    StringBuilder token = new StringBuilder();
                    token.append((char)c);
                    while((c = read()) != -1 && c != '\n') {
                        token.append((char)c);
                    }

                    I18NErrorMessages.UNEXPECTED_TOKEN.throwException(token.toString(), filename, line-1, "[msgid, msgctxt, msgstr, string, comment, eof]");
            }

        }

        private int read() throws I18NException {
            try {
                int c = reader.read();
                if(c == '\n') {
                    line++;
                }
                return c;
            } catch (IOException e) {
                I18NErrorMessages.IO_EXCEPTION.throwException(e, filename);            }
            return -1;
        }

        private void comment() throws I18NException {
            /*StringBuilder data = new StringBuilder();
            int c = -1;
            while((c = read()) != '\n') {
                data.append((char) c);    
            }
            nextToken = POToken.COMMENT;
            element(data.toString());*/
            // Ignore comments
            int c = -1;
            while((c = read()) != '\n') {
            }
            initNextToken();
        }

        private void string() throws I18NException {
            StringBuilder data = new StringBuilder();
            int c = read();
            while(c != '"' && c != '\n' && c != -1) {
                data.append((char)c);
                c = read();
            }
            while(c != '\n' && c != -1) {
                c = read();
            }
            nextToken = POToken.TEXT;
            element(data.toString());
        }

        private void eof() {
            nextToken = POToken.EOF;
            element(null);
        }

        private void msgIdOrMsgStr() throws I18NException {
            expect('s', 'g');
            switch(read()) {
                case 'i' : expect('d');
                    switch(read()) {
                        case '_' : expect('p','l','u','r','a','l'); msgIdPluralToken(); break;
                        default:
                            msgIdToken();
                    }
                    break;
                case 's' : expect('t','r');
                    switch(read()) {
                        case '[' :
                            StringBuilder number = new StringBuilder();
                            int c;
                            while((c = read()) != ']') {
                                number.append((char)c);
                            }
                            msgStrToken(number.toString());
                            break;
                        default:
                            msgStrToken(null);
                    }
                    break;
                case 'c':
                    expect('t','x','t');
                    int c = -1;
                    while(Character.isWhitespace(c = read())){}
                    StringBuilder context = new StringBuilder();
                    context.append((char)c);
                    while((c = read()) != '\n') {
                        context.append((char)c);
                    }
                    msgctxtToken(context.toString());
            }
        }

        private void msgctxtToken(String context) {
            nextToken = POToken.MSGCTXT;
            element(context);
        }

        private void msgIdPluralToken() {
            nextToken = POToken.MSGID_PLURAL;
            element(null);
        }

        private void msgStrToken(String number) throws I18NException {
            nextToken = POToken.MSGSTR;
            try {
                if(number == null) {
                    element(null);  
                } else {
                    element(Integer.valueOf(number));
                }
            } catch (NumberFormatException x) {
                I18NErrorMessages.EXPECTED_NUMBER.throwException(number, filename, line);
            }
        }

        private void msgIdToken() {
            nextToken = POToken.MSGID;
            element(null);
        }


        private void element(Object data) {
            nextElement = new POElement(nextToken, data);
        }

        private void expect(int...characters) throws I18NException {
            for(int c : characters) {
                int readC = read();
                if(readC != c) {
                    I18NErrorMessages.MALFORMED_TOKEN.throwException(""+(char)readC, ""+(char)c, filename, line);
                }
            }
        }

        private void parserErro2r() {
            //To change body of created methods use File | Settings | File Templates.
        }
    }
}
