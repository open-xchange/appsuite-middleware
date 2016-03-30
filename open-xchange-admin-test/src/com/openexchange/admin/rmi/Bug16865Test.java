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
import static org.junit.Assert.assertNotNull;
import java.rmi.Naming;
import junit.framework.JUnit4TestAdapter;
import org.junit.Test;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Database;

/**
 * {@link Bug}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class Bug16865Test {

    public Bug16865Test() {
        super();
    }

    public static final junit.framework.Test suite() {
        return new JUnit4TestAdapter(Bug16865Test.class);
    }

    @Test
    public void testDefaultInitial() throws Throwable {
        Credentials cred = ContextTest.DummyMasterCredentials();
        String host = AbstractRMITest.getRMIHostUrl();
        OXUtilInterface util = (OXUtilInterface) Naming.lookup(host + OXUtilInterface.RMI_NAME);
        Database db = new Database();
        db.setName("test" + System.currentTimeMillis());
        db.setPassword("secret");
        db.setMaster(Boolean.TRUE);
        Database created = util.registerDatabase(db, cred);
        try {
            Database test = null;
            for (Database tmpDB : util.listAllDatabase(cred)) {
                if (tmpDB.getName().equals(db.getName())) {
                    test = tmpDB;
                }
            }
            assertNotNull("Just registered database not found.", test);
            assertEquals("Initial number of database connections must be zero by default.", Integer.valueOf(0), test.getPoolInitial());
        } finally {
            util.unregisterDatabase(created, cred);
        }
    }
}
