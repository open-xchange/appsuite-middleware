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

package com.openexchange.ajax.requesthandler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;
import com.openexchange.ajax.requesthandler.DefaultConverter.NoSuchPath;
import com.openexchange.ajax.requesthandler.DefaultConverter.Step;
import com.openexchange.ajax.requesthandler.ResultConverter.Quality;
import com.openexchange.exception.OXException;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link DefaultConverterTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class DefaultConverterTest {
    private final DefaultConverter converter = new DefaultConverter();

         @Test
     public void testSingleStepConversion() {
        try {
            TestConverter tc = new TestConverter("A", "B", Quality.GOOD);
            converter.addConverter(tc);
            Step shortestPath = converter.getShortestPath("A", "B");

            assertTrue(shortestPath.next == null);
            assertEquals(tc, shortestPath.converter);
        } catch (NoSuchPath e) {
            fail(e.getMessage());
        }
    }

         @Test
     public void testMultiStepConversion() {
        try {
            TestConverter a = new TestConverter("A", "B", Quality.GOOD);
            TestConverter b = new TestConverter("B", "C", Quality.GOOD);
            TestConverter c = new TestConverter("C", "D", Quality.GOOD);
            converter.addConverter(a);
            converter.addConverter(b);
            converter.addConverter(c);

            Step shortestPath = converter.getShortestPath("A", "D");

            assertPath(shortestPath, a, b, c);
        } catch (NoSuchPath e) {
            fail(e.getMessage());
        }
    }

         @Test
     public void testRetrace() {
        try {
            TestConverter a = new TestConverter("A", "B", Quality.GOOD);
            TestConverter b = new TestConverter("B", "C", Quality.GOOD);
            TestConverter b2 = new TestConverter("B", "E", Quality.GOOD);
            TestConverter c = new TestConverter("C", "D", Quality.GOOD);
            converter.addConverter(a);
            converter.addConverter(b);
            converter.addConverter(b2);
            converter.addConverter(c);

            Step shortestPath = converter.getShortestPath("A", "D");

            assertPath(shortestPath, a, b, c);
        } catch (NoSuchPath e) {
            fail(e.getMessage());
        }
    }

         @Test
     public void testPreferHighQuality() {
        try {
            TestConverter a = new TestConverter("A", "B", Quality.GOOD);
            TestConverter b = new TestConverter("B", "C", Quality.GOOD);
            TestConverter b2 = new TestConverter("B", "C", Quality.BAD);
            TestConverter c = new TestConverter("C", "D", Quality.GOOD);
            converter.addConverter(a);
            converter.addConverter(b);
            converter.addConverter(b2);
            converter.addConverter(c);

            Step shortestPath = converter.getShortestPath("A", "D");

            assertPath(shortestPath, a, b, c);
        } catch (NoSuchPath e) {
            fail(e.getMessage());
        }
    }

         @Test
     public void testImpossible() {
        try {
            TestConverter a = new TestConverter("A", "B", Quality.GOOD);
            TestConverter b = new TestConverter("B", "C", Quality.GOOD);
            TestConverter c = new TestConverter("C", "D", Quality.GOOD);
            converter.addConverter(a);
            converter.addConverter(b);
            converter.addConverter(c);

            try {
                converter.getShortestPath("A", "E");
                fail("Huh?!");
            } catch (IllegalArgumentException x) {
                assertTrue(true);
            }
        } catch (NoSuchPath e) {
            fail(e.getMessage());
        }

    }

    private void assertPath(Step shortestPath, TestConverter... converters) {
        Step current = shortestPath;
        for (TestConverter testConverter : converters) {
            assertNotNull("Missing one for " + testConverter, current);
            assertEquals(testConverter, current.converter);
            current = current.next;
        }
    }

    public static class TestConverter implements ResultConverter {

        private final String input;

        private final String output;

        private final Quality quality;

        public TestConverter(String input, String output, Quality quality) {
            super();
            this.input = input;
            this.output = output;
            this.quality = quality;
        }

        @Override
        public void convert(AJAXRequestData requestData, AJAXRequestResult result, ServerSession session, Converter converter) throws OXException {

        }

        @Override
        public String getInputFormat() {
            return input;
        }

        @Override
        public String getOutputFormat() {
            return output;
        }

        @Override
        public Quality getQuality() {
            return quality;
        }

        /**
         * @param request  
         * @param result 
         */
        public boolean handles(AJAXRequestData request, AJAXRequestResult result) {
            return true;
        }

        @Override
        public String toString() {
            return input + " -> " + output;
        }

    }

}
