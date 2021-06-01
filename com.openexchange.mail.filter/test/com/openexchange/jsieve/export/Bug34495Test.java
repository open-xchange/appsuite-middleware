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

package com.openexchange.jsieve.export;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.Optional;
import java.util.StringTokenizer;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.jsieve.export.exceptions.OXSieveHandlerException;


/**
 * {@link Bug34495Test}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class Bug34495Test extends SieveHandler {


    private static final String ERROR_MSG_1 = "\"Zeile 7: Fehlender String: Hier muss entweder \\\"text:\\\" oder `\\\"` folgen\"";

    private static final String ERROR_MSG_2 = "\"Cited from RFC 5804: \\\"Client implementations should note that this may be a\r\n" +
                                              "multiline literal string with more than one error message separated\r\n" +
                                              "by CRLFs.\\\"\"";

    private static final String DELIMS = "\"\\\r\n ";

    public Bug34495Test() {
        super(null, null, null, "localhost", 0, null, null, Optional.empty(), -1, -1);
    }


    @Before
    public void setUp() {
        AUTH = true;
        bos_sieve = new BufferedOutputStream(new OutputStream() {
            @Override
            public void write(int b) throws IOException {
            }
        });
    }

     @Test
     public void testParseMessageWithQuotes() throws Exception {
        testParseMessage(ERROR_MSG_1);
    }

     @Test
     public void testParseMessageWithLineBreaks() throws Exception {
        testParseMessage(ERROR_MSG_2);
    }

    private void testParseMessage(String msg) throws Exception {
        bis_sieve = new BufferedReader(new StringReader("NO " + msg));
        boolean errorThrown = false;
        try {
            setScript("test", new byte[1], new StringBuilder());
        } catch (OXSieveHandlerException e) {
            errorThrown = true;
//          System.out.println(e.getMessage());
          int c1 = new StringTokenizer(msg, DELIMS, false).countTokens();
          int c2 = new StringTokenizer(e.getMessage(), DELIMS, false).countTokens();
          assertTrue(c1 > 1);
          assertEquals(c1, c2);
        }

        assertTrue(errorThrown);
    }

}
