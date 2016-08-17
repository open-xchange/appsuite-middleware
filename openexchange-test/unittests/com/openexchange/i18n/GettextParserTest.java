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

package com.openexchange.i18n;

import static com.openexchange.java.Autoboxing.i;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import junit.framework.TestCase;
import com.openexchange.exception.OXException;
import com.openexchange.i18n.parsing.I18NExceptionCode;
import com.openexchange.i18n.parsing.POParser;
import com.openexchange.i18n.parsing.Translations;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class GettextParserTest extends TestCase {

    public GettextParserTest() {
        super();
    }

    @Override
    public void setUp() {
      }

    public void testShouldParseSingleLineEntries() throws OXException {
        final String poText = "msgid \"I am a message.\"\n"
            + "msgstr \"Ich bin eine Nachricht.\"\n"
            + "msgid \"I am another message.\"\n"
            + "msgstr \"Ich bin eine andere Nachricht.\"\n";

        final Translations translations = parse(poText);
        assertNotNull(translations);

        assertTranslation(translations, "I am a message.",
            "Ich bin eine Nachricht.");
        assertTranslation(translations, "I am another message.",
            "Ich bin eine andere Nachricht.");
    }

    public void testShouldParseMultiLineEntries() throws OXException {
        final String poText = "msgid \"\"\n"
            + "\"This is part of a longer string\\n\"\n"
            + "\"Typically multiline\"\n" + "msgstr \"\"\n"
            + "\"Dies ist ein Teil einerer l\u00e4ngeren Zeichenkette\\n\"\n"
            + "\"Typischerweise mehrzeilig\"\n" + "msgid \"\"\n"
            + "\"This is another long string\\n\"\n"
            + "\"Again multiline\\n\"\n" + "msgstr \"\"\n"
            + "\"Dies ist ein weitere lange Zeichenkette\\n\"\n"
            + "\"Ebenfalls mehrzeilig\\n\"\n";

        final Translations translations = parse(poText);
        assertNotNull(translations);

        assertTranslation(translations,
            "This is part of a longer string\nTypically multiline",
            "Dies ist ein Teil einerer l\u00e4ngeren Zeichenkette\nTypischerweise mehrzeilig");
    }

    public void testShouldBeRobustWithCarriageReturns() throws OXException {
        final String poText = "msgid \"\"\r\n"
                + "\"This is part of a longer string\\n\"\r\n"
                + "\"Typically multiline\"\n" + "msgstr \"\"\r\n"
                + "\"Dies ist ein Teil einerer l\u00e4ngeren Zeichenkette\\n\"\r\n"
                + "\"Typischerweise mehrzeilig\"\n" + "msgid \"\"\r\n"
                + "\"This is another long string\\n\"\r\n"
                + "\"Again multiline\\n\"\n" + "msgstr \"\"\r\n"
                + "\"Dies ist ein weitere lange Zeichenkette\\n\"\r\n"
                + "\"Ebenfalls mehrzeilig\\n\"\r\n";

        final Translations translations = parse(poText);
        assertNotNull(translations);

        assertTranslation(translations,
                "This is part of a longer string\nTypically multiline",
                "Dies ist ein Teil einerer l\u00e4ngeren Zeichenkette\nTypischerweise mehrzeilig");
    }


    public void testShouldParsePluralForms() throws OXException {
        // Do no die on plurals, but until we need more complex handling
        // this will ignore every value but the first
        final String poText = "msgid \"%d message\"\n"
            + "msgid_plural \"%d messages\"\n" + "msgstr[0] \"%d Nachricht\"\n"
            + "msgstr[1] \"%d Nachrichten\"\n" + "msgid \"Another message\"\n"
            + "msgstr \"Andere Nachricht\"\n";

        final Translations translations = parse(poText);

        assertNotNull(translations);
        assertTranslation(translations, "%d message", "%d Nachricht");
        assertTranslation(translations, "%d messages", "%d Nachricht");
        assertTranslation(translations, "Another message", "Andere Nachricht");
    }

    public void testShouldParseMultilinePlurals() throws OXException {
        // Do no die on plurals, but until we need more complex handling
        // this will ignore every value but the first
        final String poText = "msgid \"\"\n"
            + "\"A multiline message about\\n\"\n" + "\"%d message\"\n"
            + "msgid_plural \"\"\n" + "\"A multiline message about\\n\"\n"
            + "\"%d messages\"\n" + "msgstr[0] \"\"\n"
            + "\"Eine mehrzeilige Nachricht \u00fcber\\n\"\n"
            + "\"%d Nachricht\"\n" + "msgstr[1] \"\"\n"
            + "\"Eine mehrzeilige Nachricht \u00fcber\\n\"\n"
            + "\"%d Nachrichten\"\n" + "msgid \"Another message\"\n"
            + "msgstr \"Andere Nachricht\"\n";

        final Translations translations = parse(poText);

        assertNotNull(translations);
        assertTranslation(translations,
            "A multiline message about\n%d message",
            "Eine mehrzeilige Nachricht \u00fcber\n%d Nachricht");
        assertTranslation(translations,
            "A multiline message about\n%d messages",
            "Eine mehrzeilige Nachricht \u00fcber\n%d Nachricht");

        assertTranslation(translations, "Another message", "Andere Nachricht");

    }

    public void testShouldIgnoreComments() throws OXException {
        final String poText = "# This is a comment line.\n"
            + "msgid \"I am a message.\"\n"
            + "msgstr \"Ich bin eine Nachricht.\"\n"
            + "# Comments are lines that start with #\n"
            + "msgid \"I am another message.\"\n"
            + "# Comments can appear practically everywhere\n"
            + "msgstr \"Ich bin eine andere Nachricht.\"\n";

        final Translations translations = parse(poText);
        assertNotNull(translations);

        assertTranslation(translations, "I am a message.",
            "Ich bin eine Nachricht.");
        assertTranslation(translations, "I am another message.",
            "Ich bin eine andere Nachricht.");
    }

    public void testShouldIgnoreWhitespace() throws OXException {
        final String poText = "msgid     \"I am a message.\"\n"
            + "msgstr    \"Ich bin eine Nachricht.\"    \n"
            + "msgid   \"I am another message.\"\n"
            + "msgstr \"Ich bin eine andere Nachricht.\"";

        final Translations translations = parse(poText);
        assertNotNull(translations);

        assertTranslation(translations, "I am a message.",
            "Ich bin eine Nachricht.");
        assertTranslation(translations, "I am another message.",
            "Ich bin eine andere Nachricht.");
    }

    public void testShouldIgnoreEmptyLines() throws OXException {
        final String poText = "msgid \"I am a message.\"\n\n"
            + "msgstr \"Ich bin eine Nachricht.\"\n\n\n"
            + "msgid \"I am another message.\"\n\n  \n"
            + "msgstr \"Ich bin eine andere Nachricht.\"\n";

        final Translations translations = parse(poText);
        assertNotNull(translations);

        assertTranslation(translations, "I am a message.",
            "Ich bin eine Nachricht.");
        assertTranslation(translations, "I am another message.",
            "Ich bin eine andere Nachricht.");
    }

    public void testShouldSurviveRunawayStrings() throws OXException {
        final String poText = "msgid \"I am a message.\"\n"
            + "msgstr \"Ich bin eine Nachricht.\n"
            + "msgid \"I am another message.\"\n"
            + "msgstr \"Ich bin eine andere Nachricht.";

        final Translations translations = parse(poText);
        assertNotNull(translations);

        assertTranslation(translations, "I am a message.",
            "Ich bin eine Nachricht.");
        assertTranslation(translations, "I am another message.",
            "Ich bin eine andere Nachricht.");
    }

    public void testSyntaxError1() throws UnsupportedEncodingException {
        final String poText = "msgid \"\"\n"
            + "msgstr \"Content-Type: text/plain; charset=UTF-8\\n\"\n"
            + "msgid \"I am a message.\"\n"
            + "msgstr \"Ich bin eine Nachricht.\n"
            + "BLUPP! \"I am another message.\"\n"
            + "msgstr \"Ich bin eine andere Nachricht.\"";

        try {
            parse(poText.getBytes(com.openexchange.java.Charsets.UTF_8));
            fail("Expected parsing error");
        } catch (final OXException x) {
            assertEquals(I18NExceptionCode.UNEXPECTED_TOKEN.getNumber(), x
                .getCode());
            final Object[] messageArgs = x.getLogArgs();
            final String incorrectToken = (String) messageArgs[0];
            final String filename = (String) messageArgs[1];
            final int line = i((Integer) messageArgs[2]);
            final String expectedList = (String) messageArgs[3];

            assertEquals("BLUPP! \"I am another message.\"", incorrectToken);
            assertEquals("test.po", filename);
            assertEquals(5, line);
            assertEquals("[msgid, msgctxt, msgstr, string, comment, eof]",
                expectedList);
        }

    }

    public void testSyntaxError2() throws UnsupportedEncodingException {
        final String poText = "msgid \"\"\n"
            + "msgstr \"Content-Type: text/plain; charset=UTF-8\\n\"\n"
            + "msgid \"I am a message.\"\n"
            + "msgid \"Ich bin eine Nachricht.\n"
            + "msgid \"I am another message.\"\n"
            + "msgstr \"Ich bin eine andere Nachricht.\"";
        try {
            parse(poText.getBytes(com.openexchange.java.Charsets.UTF_8));
            fail("Expected parsing error");
        } catch (final OXException x) {
            assertEquals(
                com.openexchange.i18n.parsing.I18NExceptionCode.UNEXPECTED_TOKEN_CONSUME
                    .getNumber(), x.getCode());
            final Object[] messageArgs = x.getLogArgs();
            final String incorrectToken = (String) messageArgs[0];
            final String filename = (String) messageArgs[1];
            final int line = i((Integer) messageArgs[2]);
            final String expectedList = (String) messageArgs[3];

            assertEquals("msgid", incorrectToken);
            assertEquals("test.po", filename);
            assertEquals(4, line);
            assertEquals("[msgstr]", expectedList);
        }

    }

    public void testSyntaxError3() throws UnsupportedEncodingException {
        final String poText = "msgid \"\"\n"
            + "msgstr \"Content-Type: text/plain; charset=UTF-8\\n\"\n"
            + "msgid \"I am a message.\"\n"
            + "msgstr[Blupp] \"Ich bin eine andere Nachricht.\"";

        try {
            parse(poText.getBytes(com.openexchange.java.Charsets.UTF_8));
            fail("Expected parsing error");
        } catch (final OXException x) {
            assertEquals(I18NExceptionCode.EXPECTED_NUMBER.getNumber(), x
                .getCode());
            final Object[] messageArgs = x.getLogArgs();
            final String incorrectToken = (String) messageArgs[0];
            final String filename = (String) messageArgs[1];
            final int line = i((Integer) messageArgs[2]);

            assertEquals("Blupp", incorrectToken);
            assertEquals("test.po", filename);
            assertEquals(4, line);
        }

    }

    public void testSyntaxError4() throws UnsupportedEncodingException {
        final String poText = "msgid \"\"\n"
            + "msgstr \"Content-Type: text/plain; charset=UTF-8\\n\"\n"
            + "msgid \"I am a message.\"\n"
            + "msgstTUEDELUE \"Ich bin eine andere Nachricht.\"";

        try {
            parse(poText.getBytes(com.openexchange.java.Charsets.UTF_8));
            fail("Expected parsing error");
        } catch (final OXException x) {
            assertEquals(I18NExceptionCode.MALFORMED_TOKEN.getNumber(), x
                .getCode());
            final Object[] messageArgs = x.getLogArgs();
            final String incorrectToken = (String) messageArgs[0];
            final String expected = (String) messageArgs[1];
            final String filename = (String) messageArgs[2];
            final int line = i((Integer) messageArgs[3]);

            assertEquals("T", incorrectToken);
            assertEquals("r", expected);
            assertEquals("test.po", filename);
            assertEquals(4, line);
        }

    }

    public void testIOException() {
        try {
            new POParser().parse(new ExceptionThrowingInputStream(), "test.po");
        } catch (final OXException e) {
            assertEquals(I18NExceptionCode.IO_EXCEPTION.getNumber(), e
                .getCode());
            assertEquals("test.po", e.getLogArgs()[0]);
            assertEquals("BUMM!", e.getCause().getMessage());
        }
    }

    protected Translations parse(final String poText) throws OXException {
        final String withContentType = "msgid \"\"\n"
            + "msgstr \"\"\n\"Content-Type: text/plain; charset=UTF-8\\n\"\r\n"
            + poText;
        return parse(withContentType.getBytes(com.openexchange.java.Charsets.UTF_8));
    }

    protected Translations parse(final byte[] poText) throws OXException {
        return new POParser().parse(new ByteArrayInputStream(poText), "test.po");
    }

    protected static void assertTranslation(final Translations translations,
        final String original, final String expectedTranslation) {
        final String actualTranslation = translations.translate(original);
        assertNotNull("Could not find \'" + original + "' in "
            + translations.getKnownStrings(), actualTranslation);
        assertEquals(expectedTranslation, actualTranslation);
    }

    static final class ExceptionThrowingInputStream extends InputStream {

        @Override
        public int read() throws IOException {
            throw new IOException("BUMM!");
        }

        @Override
        public void close() {
            // Nothing to close.
        }
    }

}
