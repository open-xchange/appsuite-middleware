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

package com.openexchange.textxtraction.internal;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.io.IOUtils;
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
import com.openexchange.textxtraction.internal.TikaTextXtractService;

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
    public void testParserConfig() throws Exception {
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
    public void testWithDelegateAndMimeType() throws OXException {
        textXtraction.addDelegateTextXtraction(delegate);
        InputStream is = new ByteArrayInputStream(TestData.TEST_PDF);
        try {
            String text = textXtraction.extractFrom(is, TestData.MIME_TYPE);
            assertTrue(text.contains(TestData.PLAIN_TEXT));
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    @Test
    public void testWithDestructiveDelegateAndMimeType() throws OXException {
        textXtraction.addDelegateTextXtraction(destructiveDelegate);
        InputStream is = new ByteArrayInputStream(TestData.TEST_PDF);
        try {
            String text = textXtraction.extractFrom(is, TestData.MIME_TYPE);
            assertTrue(text.contains(TestData.PLAIN_TEXT));
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    @Test
    public void testWithDelegate() throws OXException {
        textXtraction.addDelegateTextXtraction(delegate);
        InputStream is = new ByteArrayInputStream(TestData.TEST_PDF);
        try {
            String text = textXtraction.extractFrom(is, null);
            assertTrue(text.contains(TestData.PLAIN_TEXT));
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    @Test
    public void testWithDoubleDelegate() throws OXException {
        assertTrue(textXtraction.addDelegateTextXtraction(delegate));
        assertFalse(textXtraction.addDelegateTextXtraction(delegate));
    }

    @Test
    public void testremoveDelegate() throws OXException {
        assertTrue(textXtraction.addDelegateTextXtraction(delegate));
        textXtraction.removeDelegateTextXtraction(delegate);
    }

    @Test
    public void testWithDestructiveDelegate() throws OXException {
        textXtraction.addDelegateTextXtraction(destructiveDelegate);
        InputStream is = new ByteArrayInputStream(TestData.TEST_PDF);
        try {
            String text = textXtraction.extractFrom(is, null);
            assertTrue(text.contains(TestData.PLAIN_TEXT));
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    @Test
    public void testExtractContentNull() throws OXException {
        String tmpString = null;
        textXtraction.extractFrom(tmpString, null);
    }

    @Test
    public void testExtractContentNotNull() throws OXException {
        textXtraction.addDelegateTextXtraction(delegate);
        InputStream is = new ByteArrayInputStream(TestData.TEST_PDF);
        String text = null;
        try {
            text = textXtraction.extractFrom(is, null);
            assertTrue(text.contains(TestData.PLAIN_TEXT));
        } finally {
            IOUtils.closeQuietly(is);
        }
        assertNotNull(text);
        String text2 = textXtraction.extractFrom(text, null);
        assertTrue(text2.contains(TestData.PLAIN_TEXT));
    }

}
