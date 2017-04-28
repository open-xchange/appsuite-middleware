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

package org.jsoup.parser;

import java.util.ArrayList;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * {@link InterruptibleHtmlTreeBuilder}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public class InterruptibleHtmlTreeBuilder extends HtmlTreeBuilder {

    /**
     * Initializes a new {@link InterruptibleHtmlTreeBuilder}.
     */
    public InterruptibleHtmlTreeBuilder() {
        super();
    }

    @Override
    protected void initialiseParse(String input, String baseUri, ParseErrorList errors, ParseSettings settings) {
        Validate.notNull(input, "String input must not be null");
        Validate.notNull(baseUri, "BaseURI must not be null");

        doc = new Document(baseUri);
        this.settings = settings;
        reader = new InterruptibleCharacterReader(input);
        this.errors = errors;
        tokeniser = new Tokeniser(reader, errors);
        stack = new ArrayList<Element>(32);
        this.baseUri = baseUri;
    }

    @Override
    public ParseSettings defaultSettings() {
        return super.defaultSettings();
    }

    @Override
    public Document parse(String input, String baseUri, ParseErrorList errors, ParseSettings settings) {
        return super.parse(input, baseUri, errors, settings);
    }

    // ----------------------------------------------------------------------------------------------------------

    private static class InterruptibleCharacterReader extends CharacterReader {

        InterruptibleCharacterReader(String input) {
            super(input);
        }

        private void checkInterrupted() {
            if (Thread.interrupted()) { // clears flag if set
                throw new InterruptedParsingException();
            }
        }

        @Override
        public boolean isEmpty() {
            checkInterrupted();
            return super.isEmpty();
        }

        @Override
        public void advance() {
            checkInterrupted();
            super.advance();
        }

        @Override
        boolean matches(char c) {
            checkInterrupted();
            return super.matches(c);
        }

        @Override
        boolean matches(String seq) {
            checkInterrupted();
            return super.matches(seq);
        }

        @Override
        boolean matchesDigit() {
            checkInterrupted();
            return super.matchesDigit();
        }

        @Override
        boolean matchesLetter() {
            checkInterrupted();
            return super.matchesLetter();
        }

        @Override
        boolean matchesIgnoreCase(String seq) {
            checkInterrupted();
            return super.matchesIgnoreCase(seq);
        }

        @Override
        public char current() {
            checkInterrupted();
            return super.current();
        }

        @Override
        boolean matchesAnySorted(char[] seq) {
            checkInterrupted();
            return super.matchesAnySorted(seq);
        }

        @Override
        char consume() {
            checkInterrupted();
            return super.consume();
        }

        @Override
        String consumeAsString() {
            checkInterrupted();
            return super.consumeAsString();
        }

        @Override
        String consumeData() {
            checkInterrupted();
            return super.consumeData();
        }

        @Override
        String consumeDigitSequence() {
            checkInterrupted();
            return super.consumeDigitSequence();
        }

        @Override
        String consumeHexSequence() {
            checkInterrupted();
            return super.consumeHexSequence();
        }

        @Override
        String consumeLetterSequence() {
            checkInterrupted();
            return super.consumeLetterSequence();
        }

        @Override
        String consumeLetterThenDigitSequence() {
            checkInterrupted();
            return super.consumeLetterThenDigitSequence();
        }

        @Override
        String consumeTagName() {
            checkInterrupted();
            return super.consumeTagName();
        }

        @Override
        public String consumeTo(char c) {
            checkInterrupted();
            return super.consumeTo(c);
        }

        @Override
        String consumeTo(String seq) {
            checkInterrupted();
            return super.consumeTo(seq);
        }

        @Override
        public String consumeToAny(char... chars) {
            checkInterrupted();
            return super.consumeToAny(chars);
        }

        @Override
        String consumeToAnySorted(char... chars) {
            checkInterrupted();
            return super.consumeToAnySorted(chars);
        }

        @Override
        String consumeToEnd() {
            checkInterrupted();
            return super.consumeToEnd();
        }

    }

}
