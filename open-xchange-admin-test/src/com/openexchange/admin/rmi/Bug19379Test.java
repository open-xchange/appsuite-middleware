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

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.HashSet;
import junit.framework.JUnit4TestAdapter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.StorageException;

/**
 * {@link Bug19379Test}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class Bug19379Test extends AbstractTest {

    private Credentials superAdmin;
    private String url;
    private OXContextInterface contextIface;

    public Bug19379Test() {
        super();
    }

    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(Bug19379Test.class);
    }

    @Before
    public void setup() throws MalformedURLException, RemoteException, NotBoundException {
        superAdmin = DummyMasterCredentials();
        url = getRMIHostUrl();
        contextIface = (OXContextInterface) Naming.lookup(url + OXContextInterface.RMI_NAME);
    }

    @After
    public void teardown() {
        contextIface = null;
        url = null;
        superAdmin = null;
    }

    @Test
    public void testAddSingleMapping() throws Exception {
        try {
            Context c300 = createContext(300, createMappings("m1", "m2"));

            // check mappings
            c300 = contextIface.getData(new Context(I(300)), superAdmin);
            assertTrue(c300.getLoginMappings().equals(createMappings(getContextName(300), "m1", "m2")));

            // add some mappings...
            c300 = new Context(I(300));
            // ALWAYS use contextID + mappings!
            c300.setLoginMappings(createMappings("m1", "m2", "m4"));
            contextIface.change(c300, superAdmin);

            // check mappings
            c300 = contextIface.getData(new Context(I(300)), superAdmin);
            assertTrue(c300.getLoginMappings().equals(createMappings(getContextName(300), "m1", "m2", "m4")));
        } finally {
            deleteContext(300);
        }
    }

    @Test
    public void testChangeContextName() throws Exception {
        try {
            Context c300 = createContext(300, createMappings("m2"));

            // check context name and mappings...
            c300 = contextIface.getData(new Context(I(300)), superAdmin);
            assertEquals(c300.getName(), "300_test300.it");
            assertTrue(c300.getLoginMappings().equals(createMappings(getContextName(300), "m2")));

            // change contextName and mappings
            c300 = new Context(I(300));
            c300.setName("300_test333.it");
            c300.setLoginMappings(createMappings("m1"));
            contextIface.change(c300, superAdmin);

            // check context name and mappings
            c300 = contextIface.getData(new Context(I(300)), superAdmin);
            assertTrue(c300.getLoginMappings().equals(createMappings("300_test333.it", "m1")));
        } finally {
            deleteContext(300);
        }
    }

    @Test
    public void testDuplicateContextMappings() throws Exception {
        try {
            Context c300 = createContext(300, createMappings("m1", "m2"));
            Context c500 = createContext(500, createMappings("m3"));

            // check context names and mappings...
            c300 = contextIface.getData(new Context(I(300)), superAdmin);
            assertEquals(c300.getName(), "300_test300.it");
            assertTrue(c300.getLoginMappings().equals(createMappings(getContextName(300), "m1", "m2")));

            c500 = contextIface.getData(new Context(I(500)), superAdmin);
            assertEquals(c500.getName(), "500_test500.it");
            assertTrue(c500.getLoginMappings().equals(createMappings(getContextName(500), "m3")));

            // now add an ILLEGAL mapping...
            c300 = new Context(I(300));
            // use same c500 contextName!
            c300.setLoginMappings(createMappings("300", "m1", "m2", "m3"));
            try {
                contextIface.change(c300, superAdmin);
                fail("A StorageException must be thrown!");
            } catch (final StorageException e) {
                // Found the duplicate login mapping
                e.printStackTrace();
            }
            // check previous mappings
            c300 = contextIface.getData(new Context(I(300)), superAdmin);
            assertTrue(c300.getLoginMappings().equals(createMappings(getContextName(300), "m1", "m2")));
        } finally {
            deleteContext(300);
            deleteContext(500);
        }
    }

    // ====PRIVATE METHODS=========================================================================
    private String getContextName(final int contextID) {
        return contextID + "_test" + contextID + ".it";
    }

    private HashSet<String> createMappings(final String... mappings) {
        HashSet <String> result = new HashSet <String>();
        for (final String mapping : mappings) {
            result.add(mapping);
        }
        return result;
    }

    private Context createContext(final int contextID, final HashSet<String> lmappings) throws Exception {
        final Context context = new Context(I(contextID));
        context.setName(getContextName(contextID));
        context.setMaxQuota(L(512));
        if (null != lmappings) {
            context.setLoginMappings(lmappings);
        }

        final User admin = new User();
        admin.setName("admin@email" + contextID + ".it");
        admin.setPassword("password");
        admin.setGiven_name("Admin");
        admin.setSur_name("" + contextID);
        admin.setPrimaryEmail(admin.getName());
        admin.setEmail1(admin.getName());
        admin.setDisplay_name(admin.getGiven_name() + " " + admin.getSur_name());

        return contextIface.create(context, admin, superAdmin);
    }

    private void deleteContext(final int contextID) throws Exception {
        Context context = new Context(I(contextID));
        try {
            contextIface.delete(context, superAdmin);
        } catch (final NoSuchContextException e) {
            // Ignore
        }
    }
}
