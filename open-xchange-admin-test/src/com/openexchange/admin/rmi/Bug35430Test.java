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
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import junit.framework.JUnit4TestAdapter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.ContextExistsException;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.StorageException;

/**
 * {@link Bug35430Test}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public final class Bug35430Test extends AbstractRMITest {

    /** Keeps a list of created contexts for later cleanup */
    private Map<Integer, Context> contexts = new HashMap<Integer, Context>();

    public Bug35430Test() {
        super();
    }

    public static final junit.framework.Test suite() {
        return new JUnit4TestAdapter(Bug35430Test.class);
    }

    @Before
    public void setup() throws MalformedURLException, RemoteException, NotBoundException, StorageException, InvalidCredentialsException, InvalidDataException, ContextExistsException, NoSuchContextException, DatabaseUpdateException {
        contexts.put(31145, createContext("bug35430context.com", 314159265));
        //contexts.put(35430, createContext("00314159265.pi", 161803398));
    }

    @After
    public void tearDown() throws RemoteException, MalformedURLException, InvalidCredentialsException, NoSuchContextException, StorageException, DatabaseUpdateException, InvalidDataException, NotBoundException {
        for (Integer i : contexts.keySet()) {
            deleteContext(contexts.get(i));
        }
    }

    @Test
    public void test() throws Throwable {
        OXContextInterface contextInterface = getContextInterface();

        Context[] contexts = contextInterface.list("bug35430context.com", superAdminCredentials);
        assertEquals(1, contexts.length);
        assertEquals(new Integer(314159265), contexts[0].getId());
        assertEquals("bug35430context.com", contexts[0].getName());

        contexts = contextInterface.list("00314159265.pi", superAdminCredentials);
        assertEquals(0, contexts.length);
    }

    private Context createContext(String name, int cid) throws MalformedURLException, RemoteException, NotBoundException, StorageException, InvalidCredentialsException, InvalidDataException, ContextExistsException, NoSuchContextException, DatabaseUpdateException {
        OXContextInterface conInterface = getContextInterface();
        Context newContext = newContext(name, cid);
        User newAdmin = newUser("oxadmin", "secret", "New Admin", "New", "Admin", "newadmin@ox.invalid");
        boolean created = false;
        try {
            newContext = conInterface.create(newContext, newAdmin, superAdminCredentials);
            created = true;
            try {
                conInterface.create(newContext, newAdmin, superAdminCredentials);
                fail("Should throw ContextExistsException");
            } catch (ContextExistsException e) {
                assertTrue("Caught exception", true);
            }
        } catch (Exception e) {
            if (!created) {
                Context[] ctxs = conInterface.list(name, superAdminCredentials);
                if (ctxs.length > 0) {
                    newContext = ctxs[0];
                }
            }
        }
        return newContext;
    }

    private void deleteContext(Context context) throws RemoteException, MalformedURLException, InvalidCredentialsException, NoSuchContextException, StorageException, DatabaseUpdateException, InvalidDataException, NotBoundException {
        getContextInterface().delete(context, superAdminCredentials);
    }
}
