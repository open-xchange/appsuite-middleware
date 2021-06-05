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
import java.io.UnsupportedEncodingException;
import org.junit.Test;
import com.openexchange.exception.OXException;

/**
 * {@link Bug22803Test}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class Bug22803Test {

    @SuppressWarnings("static-method")
    @Test(timeout=1000)
     public void testWithTab() throws UnsupportedEncodingException, OXException {
        Translations translations = new POParser().parse(new ByteArrayInputStream(PO_CONTENTS.getBytes("UTF-8")), "Bug22803Test");
        String actual = translations.translate("Date range in search must contain 2 and not %d values.");
        assertEquals("Translation is wrong.", "Zakres dat w wyszukiwaniu musi zawiera\u0107 dwie warto\u015bci. Aktualna liczba warto\u015bci: %d. \\t", actual);
    }

    private static final String PO_CONTENTS = "msgid \"\"\n"
        + "msgstr \"\"\n"
        + "\"Content-Type: text/plain; charset=UTF-8\\n\"\n"
        + "\n"
        + "msgid \"Date range in search must contain 2 and not %d values.\"\n"
        + "msgstr \"\"\n"
        + "\"Zakres dat w wyszukiwaniu musi zawiera\u0107 dwie warto\u015bci. Aktualna liczba \"\n"
        + "\"warto\u015bci: %d. \\t\"\n";
}
