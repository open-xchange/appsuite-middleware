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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

import static org.junit.Assert.*;
import java.util.concurrent.locks.Lock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.realtime.packet.ID;


/**
 * {@link IDTest}
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class IDTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testIDString() {
        ID newID = new ID("ox.component://user@context/resource");
        assertEquals("ox",newID.getProtocol());
        assertEquals("component", newID.getComponent());
        assertEquals("user",newID.getUser());
        assertEquals("context",newID.getContext());
        assertEquals("resource",newID.getResource());
    }
    
    @Test
    public void testDefaultContext() {
        ID newID = new ID("ox.component://user/resource", "context");
        assertEquals("ox",newID.getProtocol());
        assertEquals("component", newID.getComponent());
        assertEquals("user",newID.getUser());
        assertEquals("context",newID.getContext());
        assertEquals("resource",newID.getResource());
    }

    @Test
    public void testIDStringObligatory() {
        ID newID = new ID("user@context");
        assertNull(newID.getProtocol());
        assertEquals("user",newID.getUser());
        assertEquals("context",newID.getContext());
        assertNull(newID.getResource());
    }

    /**
     * ID creation has to fail with an IllegalArgumentException if user or context are missing from the String constructor.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testIDWithDefaultContext() {
        new ID("thorben");
    }
    
    @Test
    public void testIDLocking() throws Exception {
        ID id1 = new ID("protocol", "component", "user", "context", "resource");
        ID id2 = new ID("protocol", "component", "user", "context", "resource");
        assertEquals("IDs were not equal", id1, id2);
        
        Lock lock1 = id1.getLock("scope");
        Lock lock2 = id2.getLock("scope");
        assertTrue("Locks were not identical", lock1 == lock2);
    }

}
