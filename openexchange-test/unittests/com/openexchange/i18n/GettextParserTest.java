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
package com.openexchange.i18n;

import junit.framework.TestCase;

import java.io.StringReader;
import java.io.Reader;
import java.io.IOException;

import com.openexchange.exceptions.StringComponent;
import com.openexchange.i18n.parsing.I18NErrorMessages;
import com.openexchange.i18n.parsing.I18NException;
import com.openexchange.i18n.parsing.POParser;
import com.openexchange.i18n.parsing.Translations;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class GettextParserTest extends TestCase {

    public void setUp() {
        I18NErrorMessages.FACTORY.setApplicationId("com.openexchange.test");
        I18NErrorMessages.FACTORY.setComponent(new StringComponent("TST"));

    }

    public void testShouldParseSingleLineEntries() throws I18NException {
        String poText =
                "msgid \"I am a message.\"\n"+
                "msgstr \"Ich bin eine Nachricht.\"\n"+
                "msgid \"I am another message.\"\n"+
                "msgstr \"Ich bin eine andere Nachricht.\"\n";

        Translations translations = parse(poText);
        assertNotNull(translations);

        assertTranslation(translations, "I am a message.", "Ich bin eine Nachricht.");
        assertTranslation(translations, "I am another message.", "Ich bin eine andere Nachricht.");
    }


    public void testShouldParseMultiLineEntries() throws I18NException {
        String poText =
                "msgid \"\"\n"+
                "\"This is part of a longer string\\n\"\n"+
                "\"Typically multiline\"\n"+
                "msgstr \"\"\n"+
                "\"Dies ist ein Teil einerer längeren Zeichenkette\\n\"\n"+
                "\"Typischerweise mehrzeilig\"\n"+
                "msgid \"\"\n"+
                "\"This is another long string\\n\"\n"+
                "\"Again multiline\\n\"\n"+
                "msgstr \"\"\n"+
                "\"Dies ist ein weitere lange Zeichenkette\\n\"\n"+
                "\"Ebenfalls mehrzeilig\\n\"\n";

        Translations translations = parse(poText);
        assertNotNull(translations);

        assertTranslation(translations,
                "This is part of a longer string\\nTypically multiline",
                "Dies ist ein Teil einerer längeren Zeichenkette\\nTypischerweise mehrzeilig");

    }

    public void testShouldParsePluralForms() throws I18NException {
        // Do no die on plurals, but until we need more complex handling
        // this will ignore every value but the first
        String poText =
                "msgid \"%d message\"\n"+
                "msgid_plural \"%d messages\"\n"+
                "msgstr[0] \"%d Nachricht\"\n"+
                "msgstr[1] \"%d Nachrichten\"\n"+
                "msgid \"Another message\"\n"+
                "msgstr \"Andere Nachricht\"\n";

        Translations translations = parse(poText);

        assertNotNull(translations);
        assertTranslation(translations, "%d message", "%d Nachricht");
        assertTranslation(translations, "%d messages", "%d Nachricht");
        assertTranslation(translations, "Another message", "Andere Nachricht");
    }

    public void testShouldParseMultilinePlurals() throws I18NException {
        // Do no die on plurals, but until we need more complex handling
        // this will ignore every value but the first
        String poText =
                "msgid \"\"\n"+
                "\"A multiline message about\\n\"\n"+
                "\"%d message\"\n"+
                "msgid_plural \"\"\n"+
                "\"A multiline message about\\n\"\n"+
                "\"%d messages\"\n"+
                "msgstr[0] \"\"\n"+
                "\"Eine mehrzeilige Nachricht über\\n\"\n"+
                "\"%d Nachricht\"\n"+
                "msgstr[1] \"\"\n"+
                "\"Eine mehrzeilige Nachricht über\\n\"\n"+
                "\"%d Nachrichten\"\n"+
                "msgid \"Another message\"\n"+
                "msgstr \"Andere Nachricht\"\n";

        Translations translations = parse(poText);

        assertNotNull(translations);
        assertTranslation(translations,
                "A multiline message about\\n%d message",
                "Eine mehrzeilige Nachricht über\\n%d Nachricht");
        assertTranslation(translations,
                "A multiline message about\\n%d messages",
                "Eine mehrzeilige Nachricht über\\n%d Nachricht");

        assertTranslation(translations, "Another message", "Andere Nachricht");
           
    }

    public void testShouldIgnoreComments() throws I18NException {
        String poText =
                "# This is a comment line.\n"+
                "msgid \"I am a message.\"\n"+
                "msgstr \"Ich bin eine Nachricht.\"\n"+
                "# Comments are lines that start with #\n"+
                "msgid \"I am another message.\"\n"+
                "# Comments can appear practically everywhere\n"+
                "msgstr \"Ich bin eine andere Nachricht.\"\n";

        Translations translations = parse(poText);
        assertNotNull(translations);

        assertTranslation(translations, "I am a message.", "Ich bin eine Nachricht.");
        assertTranslation(translations, "I am another message.", "Ich bin eine andere Nachricht.");
    }

    public void testShouldIgnoreWhitespace() throws I18NException {
        String poText =
                "msgid     \"I am a message.\"\n"+
                "msgstr    \"Ich bin eine Nachricht.\"    \n"+
                "msgid   \"I am another message.\"\n"+
                "msgstr \"Ich bin eine andere Nachricht.\"";

        Translations translations = parse(poText);
        assertNotNull(translations);

        assertTranslation(translations, "I am a message.", "Ich bin eine Nachricht.");
        assertTranslation(translations, "I am another message.", "Ich bin eine andere Nachricht.");
    }

    public void testShouldIgnoreEmptyLines() throws I18NException {
        String poText =
                "msgid \"I am a message.\"\n\n"+
                "msgstr \"Ich bin eine Nachricht.\"\n\n\n"+
                "msgid \"I am another message.\"\n\n  \n"+
                "msgstr \"Ich bin eine andere Nachricht.\"\n";

        Translations translations = parse(poText);
        assertNotNull(translations);

        assertTranslation(translations, "I am a message.", "Ich bin eine Nachricht.");
        assertTranslation(translations, "I am another message.", "Ich bin eine andere Nachricht.");
    }

    public void testShouldIgnoreMessageContext() throws I18NException {
        String poText =
                "msgctxt Testing\n"+
                "msgid \"I am a message.\"\n\n"+
                "msgstr \"Ich bin eine Nachricht.\"\n\n\n"+
                "msgctxt Testing\n"+
                "msgid \"I am another message.\"\n\n  \n"+
                "msgstr \"Ich bin eine andere Nachricht.\"\n";

        Translations translations = parse(poText);
        assertNotNull(translations);

        assertTranslation(translations, "I am a message.", "Ich bin eine Nachricht.");
        assertTranslation(translations, "I am another message.", "Ich bin eine andere Nachricht.");   
    }

    public void testShouldSurviveRunawayStrings() throws I18NException {
        String poText =
                "msgid \"I am a message.\"\n"+
                "msgstr \"Ich bin eine Nachricht.\n"+
                "msgid \"I am another message.\"\n"+
                "msgstr \"Ich bin eine andere Nachricht.";

        Translations translations = parse(poText);
        assertNotNull(translations);

        assertTranslation(translations, "I am a message.", "Ich bin eine Nachricht.");
        assertTranslation(translations, "I am another message.", "Ich bin eine andere Nachricht.");
    }

    public void testSyntaxError1() {
        String poText =
                "msgid \"I am a message.\"\n"+
                "msgstr \"Ich bin eine Nachricht.\n"+
                "BLUPP! \"I am another message.\"\n"+
                "msgstr \"Ich bin eine andere Nachricht.\"";

        try {
            parse(poText);
            fail("Expected parsing error");
        } catch (I18NException x) {
            assertEquals(I18NErrorMessages.UNEXPECTED_TOKEN.getErrorCode(), x.getDetailNumber());
            Object[] messageArgs = x.getMessageArgs();
            String incorrectToken = (String) messageArgs[0];
            String filename = (String) messageArgs[1];
            int line = (Integer) messageArgs[2];
            String expectedList = (String) messageArgs[3];

            assertEquals("BLUPP! \"I am another message.\"", incorrectToken);
            assertEquals("test.po", filename);
            assertEquals(3, line);
            assertEquals("[msgid, msgctxt, msgstr, string, comment, eof]", expectedList);
        }

    }

    public void testSyntaxError2() {
        String poText =
                "msgid \"I am a message.\"\n"+
                        "msgid \"Ich bin eine Nachricht.\n"+
                        "msgid \"I am another message.\"\n"+
                        "msgstr \"Ich bin eine andere Nachricht.\"";

        try {
            parse(poText);
            fail("Expected parsing error");
        } catch (I18NException x) {
            assertEquals(com.openexchange.i18n.parsing.I18NErrorMessages.UNEXPECTED_TOKEN_CONSUME.getErrorCode(), x.getDetailNumber());
            Object[] messageArgs = x.getMessageArgs();
            String incorrectToken = (String) messageArgs[0];
            String filename = (String) messageArgs[1];
            int line = (Integer) messageArgs[2];
            String expectedList = (String) messageArgs[3];

            assertEquals("msgid", incorrectToken);
            assertEquals("test.po", filename);
            assertEquals(2, line);
            assertEquals("[msgstr]", expectedList);
        }

    }

    public void testSyntaxError3() {
        String poText =
                "msgid \"I am a message.\"\n"+
                 "msgstr[Blupp] \"Ich bin eine andere Nachricht.\"";

        try {
            parse(poText);
            fail("Expected parsing error");
        } catch (I18NException x) {
            assertEquals(I18NErrorMessages.EXPECTED_NUMBER.getErrorCode(), x.getDetailNumber());
            Object[] messageArgs = x.getMessageArgs();
            String incorrectToken = (String) messageArgs[0];
            String filename = (String) messageArgs[1];
            int line = (Integer) messageArgs[2];

            assertEquals("Blupp", incorrectToken);
            assertEquals("test.po", filename);
            assertEquals(2, line);
        }

    }

    public void testSyntaxError4() {
        String poText =
                "msgid \"I am a message.\"\n"+
                "msgstTUEDELUE \"Ich bin eine andere Nachricht.\"";

        try {
            parse(poText);
            fail("Expected parsing error");
        } catch (I18NException x) {
            assertEquals(I18NErrorMessages.MALFORMED_TOKEN.getErrorCode(), x.getDetailNumber());
            Object[] messageArgs = x.getMessageArgs();
            String incorrectToken = (String) messageArgs[0];
            String expected = (String) messageArgs[1];
            String filename = (String) messageArgs[2];
            int line = (Integer) messageArgs[3];

            assertEquals("T", incorrectToken);
            assertEquals("r", expected);
            assertEquals("test.po", filename);
            assertEquals(2, line);
        }

    }


    public void testIOException() {
        try {
            new POParser().parse(new ExceptionThrowingReader(), "test.po");
        } catch (I18NException e) {
            assertEquals(I18NErrorMessages.IO_EXCEPTION.getErrorCode(),  e.getDetailNumber());
            assertEquals("test.po", e.getMessageArgs()[0]);
            assertEquals("BUMM!", e.getCause().getMessage());
        }
    }

    protected Translations parse(String poText) throws I18NException{
        StringReader reader = new StringReader(poText);
        return new POParser().parse(reader, "test.po");
    }

    protected static void assertTranslation(Translations translations, String original, String expectedTranslation) {
        String actualTranslation = translations.translate(original);
        assertNotNull("Could not find \'"+original+"' in "+translations.getKnownStrings(),  actualTranslation);
        assertEquals(expectedTranslation, actualTranslation);
    }

    private static final class ExceptionThrowingReader extends Reader {

        public int read(char cbuf[], int off, int len) throws IOException {
            throw new IOException("BUMM!");
        }

        public void close() throws IOException {
        }
    }

}
