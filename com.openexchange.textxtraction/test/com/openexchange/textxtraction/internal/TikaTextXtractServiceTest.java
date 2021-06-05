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

package com.openexchange.textxtraction.internal;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.tika.Tika;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.CompositeParser;
import org.apache.tika.parser.Parser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.textxtraction.DelegateTextXtraction;
import com.openexchange.textxtraction.TestData;

/**
 * {@link TikaTextXtractServiceTest}
 * 
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class TikaTextXtractServiceTest {

    private TikaTextXtractService textXtraction;

    private DelegateTextXtraction delegate;

    private DelegateTextXtraction destructiveDelegate;

    @Before
    public void setUp() {
        textXtraction = new TikaTextXtractService();
        delegate = new DelegateTextXtraction() {

            @Override
            public String extractFromResource(String resource, String optMimeType) throws OXException {
                return null;
            }

            @Override
            public String extractFrom(String content, String optMimeType) throws OXException {
                return null;
            }

            @Override
            public String extractFrom(InputStream inputStream, String optMimeType) throws OXException {
                return null;
            }

            @Override
            public boolean isDestructive() {
                return false;
            }
        };

        destructiveDelegate = new DelegateTextXtraction() {

            @Override
            public String extractFromResource(String resource, String optMimeType) throws OXException {
                return null;
            }

            @Override
            public String extractFrom(String content, String optMimeType) throws OXException {
                return null;
            }

            @Override
            public String extractFrom(InputStream inputStream, String optMimeType) throws OXException {
                return null;
            }

            @Override
            public boolean isDestructive() {
                return true;
            }
        };
    }

     @Test
     public void testParserConfig() {
        Assert.assertNotNull("Tika was null", textXtraction.tika);
        Tika tika = textXtraction.tika;
        AutoDetectParser autoDetectParser = (AutoDetectParser) tika.getParser();
        Map<MediaType, Parser> wrappedParser = autoDetectParser.getParsers();
        CompositeParser compositeParser = (CompositeParser) wrappedParser.values().iterator().next();
        Map<MediaType, Parser> parserMap = compositeParser.getParsers();
        Set<Parser> allParsers = new HashSet<Parser>(parserMap.values());
        Assert.assertEquals("Wrong number of parsers.", TikaTextXtractService.PARSERS.size(), allParsers.size());

        for (Parser parser : allParsers) {
            String className = parser.getClass().getName();
            Assert.assertTrue("Missing parser " + className, TikaTextXtractService.PARSERS.contains(className));
        }
    }

     @Test
     public void testWithDelegateAndMimeType() throws OXException, IOException {
        textXtraction.addDelegateTextXtraction(delegate);
        try (InputStream is = new ByteArrayInputStream(TestData.TEST_PDF)) {
            String text = textXtraction.extractFrom(is, TestData.MIME_TYPE);
            assertTrue(text.contains(TestData.PLAIN_TEXT));
        }
    }

     @Test
     public void testWithDestructiveDelegateAndMimeType() throws OXException, IOException {
        textXtraction.addDelegateTextXtraction(destructiveDelegate);
        try (InputStream is = new ByteArrayInputStream(TestData.TEST_PDF)) {
            String text = textXtraction.extractFrom(is, TestData.MIME_TYPE);
            assertTrue(text.contains(TestData.PLAIN_TEXT));
        }
    }

     @Test
     public void testWithDelegate() throws OXException, IOException {
        textXtraction.addDelegateTextXtraction(delegate);
        try (InputStream is = new ByteArrayInputStream(TestData.TEST_PDF)) {
            String text = textXtraction.extractFrom(is, null);
            assertTrue(text.contains(TestData.PLAIN_TEXT));
        }
    }

     @Test
     public void testWithDoubleDelegate() {
        assertTrue(textXtraction.addDelegateTextXtraction(delegate));
        assertFalse(textXtraction.addDelegateTextXtraction(delegate));
    }

     @Test
     public void testremoveDelegate() {
        assertTrue(textXtraction.addDelegateTextXtraction(delegate));
        textXtraction.removeDelegateTextXtraction(delegate);
    }

     @Test
     public void testWithDestructiveDelegate() throws OXException, IOException {
        textXtraction.addDelegateTextXtraction(destructiveDelegate);
        try (InputStream is = new ByteArrayInputStream(TestData.TEST_PDF)) {
            String text = textXtraction.extractFrom(is, null);
            assertTrue(text.contains(TestData.PLAIN_TEXT));
        }
    }

     @Test
     public void testExtractContentNull() throws OXException {
        String tmpString = null;
        textXtraction.extractFrom(tmpString, null);
    }

     @Test
     public void testExtractContentNotNull() throws OXException, IOException {
        textXtraction.addDelegateTextXtraction(delegate);
        String text = null;
        try (InputStream is = new ByteArrayInputStream(TestData.TEST_PDF)) {
            text = textXtraction.extractFrom(is, null);
            assertTrue(text.contains(TestData.PLAIN_TEXT));
        }
        assertNotNull(text);
        String text2 = textXtraction.extractFrom(text, null);
        assertTrue(text2.contains(TestData.PLAIN_TEXT));
    }

}
