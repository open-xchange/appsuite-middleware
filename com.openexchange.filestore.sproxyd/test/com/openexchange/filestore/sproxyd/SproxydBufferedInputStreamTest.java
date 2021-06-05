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

package com.openexchange.filestore.sproxyd;

import static com.openexchange.filestore.sproxyd.SproxydBufferedInputStream.getRelativeRange;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.util.Arrays;
import org.junit.Test;
import com.openexchange.filestore.sproxyd.chunkstorage.Chunk;

/**
 * {@link SproxydBufferedInputStreamTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class SproxydBufferedInputStreamTest {
         @Test
     public void testFirstChunk() {
        Chunk chunk = new Chunk(null, null, 0, 1000);

        assertNull(getRelativeRange(chunk, 1000, 1000));
        assertNull(getRelativeRange(chunk, 1000, -1));
        assertNull(getRelativeRange(chunk, 1000, 1200));
        assertNull(getRelativeRange(chunk, 1001, 1500));
        assertTrue(Arrays.equals(new long[0], getRelativeRange(chunk, -1, -1)));
        assertTrue(Arrays.equals(new long[0], getRelativeRange(chunk, 0, 999)));
        assertTrue(Arrays.equals(new long[0], getRelativeRange(chunk, 0, -1)));
        assertTrue(Arrays.equals(new long[0], getRelativeRange(chunk, -1, 999)));
        assertTrue(Arrays.equals(new long[0], getRelativeRange(chunk, -1, 1000)));

        assertNotNull(getRelativeRange(chunk, 0, 0));
        assertEquals(0, getRelativeRange(chunk, 0, 0)[0]);
        assertEquals(0, getRelativeRange(chunk, 0, 0)[1]);

        assertNotNull(getRelativeRange(chunk, -1, 0));
        assertEquals(0, getRelativeRange(chunk, -1, 0)[0]);
        assertEquals(0, getRelativeRange(chunk, -1, 0)[1]);

        assertNotNull(getRelativeRange(chunk, 0, 500));
        assertEquals(0, getRelativeRange(chunk, 0, 500)[0]);
        assertEquals(500, getRelativeRange(chunk, 0, 500)[1]);

        assertNotNull(getRelativeRange(chunk, 250, 500));
        assertEquals(250, getRelativeRange(chunk, 250, 500)[0]);
        assertEquals(500, getRelativeRange(chunk, 250, 500)[1]);

        assertNotNull(getRelativeRange(chunk, 0, 1));
        assertEquals(0, getRelativeRange(chunk, 0, 1)[0]);
        assertEquals(1, getRelativeRange(chunk, 0, 1)[1]);

        assertNotNull(getRelativeRange(chunk, 999, 1000));
        assertEquals(999, getRelativeRange(chunk, 999, 1000)[0]);
        assertEquals(999, getRelativeRange(chunk, 999, 1000)[1]);

        assertNotNull(getRelativeRange(chunk, 500, 1500));
        assertEquals(500, getRelativeRange(chunk, 500, 1500)[0]);
        assertEquals(999, getRelativeRange(chunk, 500, 1500)[1]);
    }

         @Test
     public void testMiddleChunk() {
        Chunk chunk = new Chunk(null, null, 1000, 1000);

        assertNull(getRelativeRange(chunk, 2000, 2000));
        assertNull(getRelativeRange(chunk, 2000, -1));
        assertNull(getRelativeRange(chunk, 2000, 2200));
        assertNull(getRelativeRange(chunk, 2001, 2500));
        assertTrue(Arrays.equals(new long[0], getRelativeRange(chunk, -1, -1)));
        assertTrue(Arrays.equals(new long[0], getRelativeRange(chunk, 1000, 1999)));
        assertTrue(Arrays.equals(new long[0], getRelativeRange(chunk, 1000, -1)));
        assertTrue(Arrays.equals(new long[0], getRelativeRange(chunk, -1, 1999)));
        assertTrue(Arrays.equals(new long[0], getRelativeRange(chunk, -1, 2000)));

        assertNotNull(getRelativeRange(chunk, 1000, 1000));
        assertEquals(0, getRelativeRange(chunk, 1000, 1000)[0]);
        assertEquals(0, getRelativeRange(chunk, 1000, 1000)[1]);

        assertNotNull(getRelativeRange(chunk, -1, 1000));
        assertEquals(0, getRelativeRange(chunk, -1, 1000)[0]);
        assertEquals(0, getRelativeRange(chunk, -1, 1000)[1]);

        assertNotNull(getRelativeRange(chunk, 1000, 1500));
        assertEquals(0, getRelativeRange(chunk, 1000, 1500)[0]);
        assertEquals(500, getRelativeRange(chunk, 1000, 1500)[1]);

        assertNotNull(getRelativeRange(chunk, 1250, 1500));
        assertEquals(250, getRelativeRange(chunk, 1250, 1500)[0]);
        assertEquals(500, getRelativeRange(chunk, 1250, 1500)[1]);

        assertNotNull(getRelativeRange(chunk, 1000, 1001));
        assertEquals(0, getRelativeRange(chunk, 1000, 1001)[0]);
        assertEquals(1, getRelativeRange(chunk, 1000, 1001)[1]);

        assertNotNull(getRelativeRange(chunk, 1999, 2000));
        assertEquals(999, getRelativeRange(chunk, 1999, 2000)[0]);
        assertEquals(999, getRelativeRange(chunk, 1999, 2000)[1]);

        assertNotNull(getRelativeRange(chunk, 1500, 2500));
        assertEquals(500, getRelativeRange(chunk, 1500, 2500)[0]);
        assertEquals(999, getRelativeRange(chunk, 1500, 2500)[1]);
    }

         @Test
     public void testRandomChunks() {
        Chunk chunk = new Chunk(null, null, 130, 148);
        assertNotNull(getRelativeRange(chunk, 0, 138));
        assertEquals(0, getRelativeRange(chunk, 0, 138)[0]);
        assertEquals(8, getRelativeRange(chunk, 0, 138)[1]);

        chunk = new Chunk(null, null, 130, 148);
        assertNotNull(getRelativeRange(chunk, 139, 302));
        assertEquals(9, getRelativeRange(chunk, 139, 302)[0]);
        assertEquals(147, getRelativeRange(chunk, 139, 302)[1]);

        chunk = new Chunk(null, null, 278, 129);
        assertNotNull(getRelativeRange(chunk, 139, 302));
        assertEquals(0, getRelativeRange(chunk, 139, 302)[0]);
        assertEquals(24, getRelativeRange(chunk, 139, 302)[1]);
    }

}
