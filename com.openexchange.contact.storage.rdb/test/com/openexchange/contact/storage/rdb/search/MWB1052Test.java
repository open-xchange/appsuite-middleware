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

package com.openexchange.contact.storage.rdb.search;

import static com.openexchange.java.Autoboxing.C;
import static org.junit.Assert.assertEquals;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import com.openexchange.exception.OXException;
import com.openexchange.java.SimpleTokenizer;

/**
 * {@link MWB1052Test}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since 7.10.6
 */
@RunWith(Parameterized.class)
public class MWB1052Test {

    @Parameters(name = "{index}: {0}")
    public static Iterable<? extends Object> data() {
        return Arrays.asList(C('+'), C('\\'), C('-'), C('>'), C('<'), C('('), C(')'), C('~'), C('*'), C('\"'), C('@'));
    }

    // C('+'), C('\\'), C('-'), C('>'), C('<'), C('('), C(')'), C('~'), C('*'), C('\"'), C('@')
    private Character nonWordChar;

    /**
     * Initializes a new {@link MWB1052Test}.
     *
     * @param nonWordChar The non-word character to use in the test
     */
    public MWB1052Test(Character nonWordChar) {
        super();
        this.nonWordChar = nonWordChar;
    }

    @Test
    public void testTokenizeQuery() throws OXException {
        String query = nonWordChar + "x";
        String expectedPattern = "x*";
        List<String> patterns = FulltextAutocompleteAdapter.preparePatterns(SimpleTokenizer.tokenize(query));
        assertEquals("unexpected length of tokenized patterns", 1, patterns.size());
        assertEquals("unexpected tokenized pattern", expectedPattern, patterns.get(0));
    }

}
