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

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.ContextExistsException;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.StorageException;

/**
 * Tries to reproduce the log messages described in bug 27065. This test does not really test or assert something. Therefore the logs must
 * be readable and the master -> slave replication needs to be slower than calling RMI methods. Especially the second requirement is hard to
 * provide automatically for this test. Maybe a parallel thread that causes high load on the replication can be used for this.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class Bug27065Test extends AbstractTest {

    private Credentials superAdmin;
    private String url;
    private OXContextInterface contextIface;
    private User contextAdmin;
    private Context context;

    public Bug27065Test() {
        super();
    }

    @Before
    public void setup() throws MalformedURLException, RemoteException, NotBoundException, StorageException, InvalidCredentialsException, InvalidDataException {
        superAdmin = DummyMasterCredentials();
        url = getRMIHostUrl();
        contextIface = (OXContextInterface) Naming.lookup(url + OXContextInterface.RMI_NAME);
        context = ContextTest.getTestContextObject(ContextTest.createNewContextID(superAdmin), 10l);
        contextAdmin = UserTest.getTestUserObject("admin","secret", context);
    }

    @After
    public void tearDown() throws RemoteException, InvalidDataException, StorageException, InvalidCredentialsException, NoSuchContextException, DatabaseUpdateException {
        if (contextIface.exists(new Context(context.getId()), superAdmin)) {
            contextIface.delete(context, superAdmin);
        }
    }

    @Test
    public void reproduceMessages() throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, ContextExistsException, NoSuchContextException, DatabaseUpdateException, InterruptedException {
        context = contextIface.create(context, contextAdmin, superAdmin);
        Thread.sleep(400); // Only necessary if the replication from master to slave becomes a little bit slow.
        contextIface.changeModuleAccess(new Context(context.getId()), "webmail", superAdmin);
        contextIface.downgrade(new Context(context.getId()), superAdmin);
    }
}
