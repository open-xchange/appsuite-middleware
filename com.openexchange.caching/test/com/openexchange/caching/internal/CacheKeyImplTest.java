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

package com.openexchange.caching.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import com.openexchange.caching.CacheKey;

/**
 * {@link CacheKeyImplTest}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class CacheKeyImplTest {

    public CacheKeyImplTest() {
        super();
    }

    /**
     * Tests if the class generates the same hash codes even if different constructors are used.
     */
    @Test
    public final void testSameHashCode() {
        final CacheKey key1 = new CacheKeyImpl(424242669, "teststring");
        final CacheKey key2 = new CacheKeyImpl(424242669, new String[] { "teststring" });
        assertEquals("Generated hashes are not the same.", key1.hashCode(), key2.hashCode());
    }

    /**
     * Tests if the class generates the same hash codes even if different constructors are used.
     */
    @Test
    public final void testEqualsObject() {
        final CacheKey key1 = new CacheKeyImpl(424242669, "teststring");
        final CacheKey key2 = new CacheKeyImpl(424242669, new String[] { "teststring" });
        assertTrue("Equals failes when using different constructors.", key1.equals(key2));
        assertTrue("Equals failes when using different constructors.", key2.equals(key1));
    }
}
