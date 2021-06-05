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

package com.openexchange.groupware.tools.chunk;

import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.Test;
import com.openexchange.exception.OXException;


/**
 * {@link ChunkPerformerTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class ChunkPerformerTest {

    private static final int CHUNK_SIZE = 3;

     @Test
     public void testWithoutInitialOffset() throws OXException {
        final List<String> bunchOfStrings = prepareStrings();
        final List<String> results = new ArrayList<String>();
        final Incrementable i = new Incrementable();

        ChunkPerformer.perform(new Performable() {
            @Override
            public int perform(int off, int len) throws OXException {
                List<String> subList = bunchOfStrings.subList(off, len);
                for (String str : subList) {
                    results.add(str);
                }

                i.inc();
                return subList.size();
            }

            @Override
            public int getLength() {
                return bunchOfStrings.size();
            }

            @Override
            public int getInitialOffset() {
                return 0;
            }

            @Override
            public int getChunkSize() {
                return CHUNK_SIZE;
            }
        });

        assertEquals(4, i.get());
        assertEquals(bunchOfStrings.size(), results.size());
        int found = 0;
        for (String expected : bunchOfStrings) {
            for (String str : results) {
                if (str.equals(expected)) {
                    ++found;
                    break;
                }
            }
        }
        assertEquals(bunchOfStrings.size(), found);
    }

     @Test
     public void testWithoutInitialOffsetForList() throws OXException {
        final List<String> bunchOfStrings = prepareStrings();
        final List<String> results = new ArrayList<String>();
        final Incrementable i = new Incrementable();
        ChunkPerformer.perform(bunchOfStrings, 0, CHUNK_SIZE, new ListPerformable<String>() {
            @Override
            public void perform(List<String> subList) throws OXException {
                for (String str : subList) {
                    results.add(str);
                }

                i.inc();
            }
        });

        assertEquals(4, i.get());
        assertEquals(bunchOfStrings.size(), results.size());
        int found = 0;
        for (String expected : bunchOfStrings) {
            for (String str : results) {
                if (str.equals(expected)) {
                    ++found;
                    break;
                }
            }
        }
        assertEquals(bunchOfStrings.size(), found);
    }

     @Test
     public void testWithoutInitialOffsetForArray() throws OXException {
        final List<String> tmp = prepareStrings();
        final String[] bunchOfStrings = tmp.toArray(new String[tmp.size()]);
        final List<String> results = new ArrayList<String>();
        final Incrementable i = new Incrementable();

        ChunkPerformer.perform(bunchOfStrings, 0, CHUNK_SIZE, new ArrayPerformable<String>() {
            @Override
            public void perform(String[] subArray) throws OXException {
                for (String str : subArray) {
                    results.add(str);
                }

                i.inc();
            }
        });

        assertEquals(4, i.get());
        assertEquals(bunchOfStrings.length, results.size());
        int found = 0;
        for (String expected : bunchOfStrings) {
            for (String str : results) {
                if (str.equals(expected)) {
                    ++found;
                    break;
                }
            }
        }
        assertEquals(bunchOfStrings.length, found);
    }



     @Test
     public void testWithInitialOffset() throws OXException {
        final List<String> bunchOfStrings = prepareStrings();
        final List<String> results = new ArrayList<String>();
        List<String> toCompare = bunchOfStrings.subList(1, bunchOfStrings.size());
        final Incrementable i = new Incrementable();

        ChunkPerformer.perform(new Performable() {
            @Override
            public int perform(int off, int len) throws OXException {
                List<String> subList = bunchOfStrings.subList(off, len);
                for (String str : subList) {
                    results.add(str);
                }

                i.inc();
                return subList.size();
            }

            @Override
            public int getLength() {
                return bunchOfStrings.size() - 1;
            }

            @Override
            public int getInitialOffset() {
                return 1;
            }

            @Override
            public int getChunkSize() {
                return CHUNK_SIZE;
            }
        });

        assertEquals(3, i.get());
        assertEquals(toCompare.size() - 1, results.size());
        int found = 0;
        for (String expected : toCompare) {
            for (String str : results) {
                if (str.equals(expected)) {
                    ++found;
                    break;
                }
            }
        }
        assertEquals(toCompare.size() - 1, found);
    }

     @Test
     public void testWithInitialOffsetForList() throws OXException {
        final List<String> bunchOfStrings = prepareStrings();
        final List<String> results = new ArrayList<String>();
        List<String> toCompare = bunchOfStrings.subList(1, bunchOfStrings.size());
        final Incrementable i = new Incrementable();

        ChunkPerformer.perform(bunchOfStrings, 1, CHUNK_SIZE, new ListPerformable<String>() {
            @Override
            public void perform(List<String> subList) throws OXException {
                for (String str : subList) {
                    results.add(str);
                }

                i.inc();
            }
        });

        assertEquals(3, i.get());
        assertEquals(toCompare.size() - 1, results.size());
        int found = 0;
        for (String expected : toCompare) {
            for (String str : results) {
                if (str.equals(expected)) {
                    ++found;
                    break;
                }
            }
        }
        assertEquals(toCompare.size() - 1, found);
    }

     @Test
     public void testWithInitialOffsetForArray() throws OXException {
        final List<String> tmp = prepareStrings();
        final String[] bunchOfStrings = tmp.toArray(new String[tmp.size()]);
        final List<String> results = new ArrayList<String>();
        final String[] toCompare = Arrays.copyOfRange(bunchOfStrings, 1, bunchOfStrings.length);
        final Incrementable i = new Incrementable();

        ChunkPerformer.perform(bunchOfStrings, 1, CHUNK_SIZE, new ArrayPerformable<String>() {
            @Override
            public void perform(String[] subArray) throws OXException {
                for (String str : subArray) {
                    results.add(str);
                }

                i.inc();
            }
        });

        assertEquals(3, i.get());
        assertEquals(toCompare.length - 1, results.size());
        int found = 0;
        for (String expected : toCompare) {
            for (String str : results) {
                if (str.equals(expected)) {
                    ++found;
                    break;
                }
            }
        }
        assertEquals(toCompare.length - 1, found);
    }

     @Test
     public void testWithChunkSize0() throws OXException {
        final List<String> bunchOfStrings = prepareStrings();
        final List<String> results = new ArrayList<String>();
        final Incrementable i = new Incrementable();

        ChunkPerformer.perform(new Performable() {
            @Override
            public int perform(int off, int len) throws OXException {
                List<String> subList = bunchOfStrings.subList(off, len);
                for (String str : subList) {
                    results.add(str);
                }

                i.inc();
                return subList.size();
            }

            @Override
            public int getLength() {
                return bunchOfStrings.size();
            }

            @Override
            public int getInitialOffset() {
                return 0;
            }

            @Override
            public int getChunkSize() {
                return 0;
            }
        });

        assertEquals(1, i.get());
        assertEquals(bunchOfStrings.size(), results.size());
        int found = 0;
        for (String expected : bunchOfStrings) {
            for (String str : results) {
                if (str.equals(expected)) {
                    ++found;
                    break;
                }
            }
        }
        assertEquals(bunchOfStrings.size(), found);
    }

     @Test
     public void testWithChunkSize0ForList() throws OXException {
        final List<String> tmp = prepareStrings();
        final String[] bunchOfStrings = tmp.toArray(new String[tmp.size()]);
        final List<String> results = new ArrayList<String>();
        final Incrementable i = new Incrementable();

        ChunkPerformer.perform(bunchOfStrings, 0, 0, new ArrayPerformable<String>() {
            @Override
            public void perform(String[] subArray) throws OXException {
                for (String str : subArray) {
                    results.add(str);
                }

                i.inc();
            }
        });

        assertEquals(1, i.get());
        assertEquals(bunchOfStrings.length, results.size());
        int found = 0;
        for (String expected : bunchOfStrings) {
            for (String str : results) {
                if (str.equals(expected)) {
                    ++found;
                    break;
                }
            }
        }
        assertEquals(bunchOfStrings.length, found);
    }

     @Test
     public void testWithChunkSize0ForArray() throws OXException {
        final List<String> bunchOfStrings = prepareStrings();
        final List<String> results = new ArrayList<String>();
        final Incrementable i = new Incrementable();

        ChunkPerformer.perform(bunchOfStrings, 0, 0, new ListPerformable<String>() {
            @Override
            public void perform(List<String> subList) throws OXException {
                for (String str : subList) {
                    results.add(str);
                }

                i.inc();
            }
        });

        assertEquals(1, i.get());
        assertEquals(bunchOfStrings.size(), results.size());
        int found = 0;
        for (String expected : bunchOfStrings) {
            for (String str : results) {
                if (str.equals(expected)) {
                    ++found;
                    break;
                }
            }
        }
        assertEquals(bunchOfStrings.size(), found);
    }

    private static List<String> prepareStrings() {
        final List<String> bunchOfStrings = new ArrayList<String>();
        for (int i = 0; i < 10; i++) {
            bunchOfStrings.add(UUID.randomUUID().toString());
        }

        return bunchOfStrings;
    }

    private static final class Incrementable {

        private int i;

        public Incrementable() {
            super();
            reset();
        }

        public void inc() {
            i++;
        }

        public int get() {
            return i;
        }

        public void reset() {
            i = 0;
        }
    }

}
