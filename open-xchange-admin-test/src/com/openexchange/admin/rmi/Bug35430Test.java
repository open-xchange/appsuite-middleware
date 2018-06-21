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

package com.openexchange.admin.rmi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.ContextExistsException;

/**
 * {@link Bug35430Test}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public final class Bug35430Test extends AbstractRMITest {

    /**
     * Initialises a new {@link Bug35430Test}.
     */
    public Bug35430Test() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.admin.rmi.AbstractRMITest#setUp()
     */
    @Before
    public void setUp() throws Exception {
        super.setUp();
        createContext("bug35430context.com", 314159265);
    }

    @Test
    public void test() throws Throwable {
        Context[] contexts = getContextManager().searchContext("bug35430context.com");
        assertEquals(1, contexts.length);
        assertEquals(new Integer(314159265), contexts[0].getId());
        assertEquals("bug35430context.com", contexts[0].getName());

        contexts = getContextManager().searchContext("00314159265.pi");
        assertEquals(0, contexts.length);
    }

    private Context createContext(String name, int cid) throws Exception {
        Context newContext = newContext(name, cid);
        User newAdmin = newUser("oxadmin", "secret", "New Admin", "New", "Admin", "newadmin@ox.invalid");
        boolean created = false;
        try {
            newContext = getContextManager().createContext(newContext, newAdmin);
            created = true;
            try {
                getContextManager().createContext(newContext, newAdmin);
                fail("Should throw ContextExistsException");
            } catch (ContextExistsException e) {
                assertTrue("Caught exception", true);
            }
        } catch (Exception e) {
            if (!created) {
                Context[] ctxs = getContextManager().searchContext(name);
                if (ctxs.length > 0) {
                    newContext = ctxs[0];
                }
            }
        }
        return newContext;
    }
}
