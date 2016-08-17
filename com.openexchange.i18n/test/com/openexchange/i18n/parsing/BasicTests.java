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
