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

package com.openexchange.filestore.sproxyd;

import static com.openexchange.filestore.sproxyd.SproxydBufferedInputStream.getRelativeRange;
import java.util.Arrays;
import junit.framework.TestCase;
import com.openexchange.filestore.sproxyd.chunkstorage.Chunk;

/**
 * {@link SproxydBufferedInputStreamTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class SproxydBufferedInputStreamTest extends TestCase {

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
