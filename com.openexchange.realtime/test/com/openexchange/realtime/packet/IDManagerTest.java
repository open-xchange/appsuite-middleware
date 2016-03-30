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

package com.openexchange.realtime.packet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.realtime.exception.RealtimeException;


/**
 * {@link IDManagerTest}
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 * @since 7.6.0
 */
public class IDManagerTest extends IDManager {

    private static String scope1 = "scope1", scope2="scope2", scope3="scope3";
    private ID marens, cisco;

    @Before
    public void setUp() {
        ID.ID_MANAGER_REF.set(this);
        marens = new ID("marc.arens@premium");
        cisco = new ID("francisco.laguna@premium");
    }
    
    @After
    public void tearDown() {
        ID.ID_MANAGER_REF.set(null);
    }

    @Test
    public void testGetLock() {
        Set<Lock> locksM = new HashSet<Lock>();
        Set<Lock> locksC = new HashSet<Lock>();
        locksM.add(getLock(marens, scope1));
        locksM.add(getLock(marens, scope2));
        locksM.add(getLock(marens, scope3));
        locksC.add(getLock(cisco, scope1));
        locksC.add(getLock(cisco, scope2));
        locksC.add(getLock(cisco, scope3));
        assertEquals(3, LOCKS.get(marens).size());
        assertEquals(3, LOCKS.get(cisco).size());
        assertNotEquals(locksM, locksC);
    }

    @Test
    public void testCleanup() throws RealtimeException {
        getLock(marens, scope1);
        getLock(marens, scope2);
        getLock(marens, scope3);
        getLock(cisco, scope1);
        getLock(cisco, scope2);
        getLock(cisco, scope3);

        cleanupForId(marens);
        assertNull(LOCKS.get(marens));
        assertEquals(3, LOCKS.get(cisco).size());

        cleanupForId(cisco);
        assertNull(LOCKS.get(marens));
        assertNull(LOCKS.get(cisco));

        assertEquals(0, LOCKS.entrySet().size());
    }
}
