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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.index.solr.test;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import com.openexchange.admin.rmi.OXContextInterface;
import com.openexchange.admin.rmi.OXUserInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Filestore;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.config.ConfigurationService;
import com.openexchange.index.IndexFacadeService;
import com.openexchange.index.IndexManagementService;


/**
 * {@link AbstractSolrIndexAccessTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public abstract class AbstractSolrIndexAccessTest {

    protected static IndexFacadeService indexFacade;

    protected static ConfigurationService config;

    protected static IndexManagementService managementService;

    protected static Credentials superAdminCredentials;

    protected static OXContextInterface contextInterface;

    protected static OXUserInterface userInterface;

    protected static Context context;

    protected static User user;


    @BeforeClass
    public static void setUpClass() throws Exception {
        int rmiPort = config.getIntProperty("RMI_PORT", 1099);
        Registry registry = LocateRegistry.getRegistry("localhost", rmiPort);
        Thread.sleep(1000);
        contextInterface = (OXContextInterface) registry.lookup(OXContextInterface.RMI_NAME);
        userInterface = (OXUserInterface) registry.lookup(OXUserInterface.RMI_NAME);

        Context contextToCreate = newContext("SolrMailIndexAccessTestContext", Integer.MAX_VALUE);
        User userToCreate = newUser("oxuser", "secret", "OX User", "OX", "User", "oxuser@ox.invalid");
        User superAdmin = newUser("oxadminmaster", "secret", "ContextCreatingAdmin", "Ad", "Min", "adminmaster@ox.invalid");
        superAdminCredentials = new Credentials(superAdmin.getName(), superAdmin.getPassword());
        context = contextInterface.create(contextToCreate, userToCreate, "all", superAdminCredentials);
        user = userInterface.getContextAdmin(context, new Credentials(userToCreate.getName(), userToCreate.getPassword()));
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        if (context != null) {
            contextInterface.delete(context, superAdminCredentials);
        }
    }

    public static void setIndexFacade(IndexFacadeService indexFacade) {
        AbstractSolrIndexAccessTest.indexFacade = indexFacade;
    }

    public static void setConfigurationService(ConfigurationService config) {
        AbstractSolrIndexAccessTest.config = config;
    }

    public static void setIndexManagementService(IndexManagementService managementService) {
        AbstractSolrIndexAccessTest.managementService = managementService;
    }

    private static Context newContext(String name, int id) {
        Context newContext = new Context();
        Filestore filestore = new Filestore();
        filestore.setSize(Long.valueOf(128l));
        newContext.setFilestoreId(filestore.getId());
        newContext.setName(name);
        newContext.setMaxQuota(filestore.getSize());
        newContext.setId(Integer.valueOf(id));
        return newContext;
    }

    public static User newUser(String name, String passwd, String displayName, String givenName, String surname, String email) {
        User user = new User();
        user.setName(name);
        user.setPassword(passwd);
        user.setDisplay_name(displayName);
        user.setGiven_name(givenName);
        user.setSur_name(surname);
        user.setPrimaryEmail(email);
        user.setEmail1(email);
        return user;
    }

}
