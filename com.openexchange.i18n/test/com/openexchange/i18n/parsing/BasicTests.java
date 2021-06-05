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

import static org.junit.Assert.assertEquals;
import java.io.ByteArrayInputStream;
import org.junit.Test;

/**
 * {@link BasicTests}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.8.0
 */
public class BasicTests {

    //@formatter:off
    private static final String SIMPLE =
        "msgid \"\"\n" +
        "msgstr \"\"\n" +
        "\"Content-Type: text/plain; charset=UTF-8\\n\"\n" +
        "\n" +
        "msgid \"Hello World\"\n" +
        "msgstr \"Hallo Welt\"\n";

    private static final String PLURAL =
        "msgid \"\"\n" +
        "msgstr \"\"\n" +
        "\"Content-Type: text/plain; charset=UTF-8\\n\"\n" +
        "\n" +
        "msgid \"Hello World\"\n" +
        "msgid_plural \"Hello Worlds\"\n" +
        "msgstr[0] \"Hallo Welt\"\n" +
        "msgstr[1] \"Hallo Welten\"\n" +
        "msgstr[2] \"Hallo Weltens\"\n";

    private static final String CONTEXT =
        "msgid \"\"\n" +
        "msgstr \"\"\n" +
        "\"Content-Type: text/plain; charset=UTF-8\\n\"\n" +
        "\n" +
        "msgctxt \"first\"\n" +
        "msgid \"Hello World\"\n" +
        "msgstr \"Hallo Welt\"\n" +
        "\n" +
        "msgctxt \"second\"\n" +
        "msgid \"Hello World\"\n" +
        "msgstr \"Hallo Welt!!!\"\n";

    private static final String COMPLEX =
        "msgid \"\"\n" +
        "msgstr \"\"\n" +
        "\"Content-Type: text/plain; charset=UTF-8\\n\"\n" +
        "\n" +
        "msgctxt \"first\"\n" +
        "msgid \"Hello World\"\n" +
        "msgid_plural \"Hello Worlds\"\n" +
        "msgstr[0] \"Hallo Welt\"\n" +
        "msgstr[1] \"Hallo Welten\"\n" +
        "msgstr[2] \"Hallo Weltens\"\n" +
        "\n" +
        "msgctxt \"second\"\n" +
        "msgid \"Hello World\"\n" +
        "msgid_plural \"Hello Worlds\"\n" +
        "msgstr[0] \"Hallo Welt!!!\"\n" +
        "msgstr[1] \"Hallo Welten!!!\"\n" +
        "msgstr[2] \"Hallo Weltens!!!\"\n";

    //@formatter:on

     @Test
     public void testSimple() throws Exception {
        Translations translations = new POParser().parse(new ByteArrayInputStream(SIMPLE.getBytes("UTF-8")), "SimpleTest");
        String actual = translations.translate("Hello World");
        assertEquals("Translation is wrong.", "Hallo Welt", actual);
    }

     @Test
     public void testPlural() throws Exception {
        Translations translations = new POParser().parse(new ByteArrayInputStream(PLURAL.getBytes("UTF-8")), "SimpleTest");
        assertEquals("Translation is wrong.", "Hallo Welt", translations.translate("Hello World"));
        assertEquals("Translation is wrong.", "Hallo Welten", translations.translate("Hello World", 1));
        assertEquals("Translation is wrong.", "Hallo Welten", translations.translate("Hello Worlds", 1));
        assertEquals("Translation is wrong.", "Hallo Weltens", translations.translate("Hello World", 2));
        assertEquals("Translation is wrong.", "Hallo Weltens", translations.translate("Hello Worlds", 2));
    }

     @Test
     public void testContext() throws Exception {
        Translations translations = new POParser().parse(new ByteArrayInputStream(CONTEXT.getBytes("UTF-8")), "SimpleTest");
        assertEquals("Translation is wrong.", "Hallo Welt", translations.translate("first", "Hello World"));
        assertEquals("Translation is wrong.", "Hallo Welt!!!", translations.translate("second", "Hello World"));
    }

     @Test
     public void testComplex() throws Exception {
        Translations translations = new POParser().parse(new ByteArrayInputStream(COMPLEX.getBytes("UTF-8")), "SimpleTest");
        assertEquals("Translation is wrong.", "Hallo Welt", translations.translate("first", "Hello World"));
        assertEquals("Translation is wrong.", "Hallo Welten", translations.translate("first", "Hello World", 1));
        assertEquals("Translation is wrong.", "Hallo Welten", translations.translate("first", "Hello Worlds", 1));
        assertEquals("Translation is wrong.", "Hallo Weltens", translations.translate("first", "Hello World", 2));
        assertEquals("Translation is wrong.", "Hallo Weltens", translations.translate("first", "Hello Worlds", 2));

        assertEquals("Translation is wrong.", "Hallo Welt!!!", translations.translate("second", "Hello World"));
        assertEquals("Translation is wrong.", "Hallo Welten!!!", translations.translate("second", "Hello World", 1));
        assertEquals("Translation is wrong.", "Hallo Welten!!!", translations.translate("second", "Hello Worlds", 1));
        assertEquals("Translation is wrong.", "Hallo Weltens!!!", translations.translate("second", "Hello World", 2));
        assertEquals("Translation is wrong.", "Hallo Weltens!!!", translations.translate("second", "Hello Worlds", 2));
    }

}
