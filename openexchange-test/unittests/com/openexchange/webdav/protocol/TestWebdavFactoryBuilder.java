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

package com.openexchange.webdav.protocol;

import com.openexchange.database.provider.DBPoolProvider;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.impl.FolderLockManagerImpl;
import com.openexchange.groupware.infostore.facade.impl.InfostoreFacadeImpl;
import com.openexchange.groupware.infostore.paths.impl.PathResolverImpl;
import com.openexchange.groupware.infostore.webdav.EntityLockManagerImpl;
import com.openexchange.groupware.infostore.webdav.InfostoreWebdavFactory;
import com.openexchange.groupware.infostore.webdav.PropertyStoreImpl;
import com.openexchange.setuptools.TestConfig;
import com.openexchange.setuptools.TestContextToolkit;
import com.openexchange.test.AjaxInit;
import com.openexchange.test.TestInit;
import com.openexchange.webdav.protocol.impl.DummyResourceManager;

public class TestWebdavFactoryBuilder {

    public static final int DUMMY = 0;
    public static final int INFO = 1;

    private static final int mode = INFO;

    public static WebdavFactory buildFactory() throws Exception {
        switch(mode) {
        case DUMMY : return buildDummyFactory();
        case INFO : return buildInfoFactory();
        }
        return null;
    }

    private static WebdavFactory buildDummyFactory() {
        return DummyResourceManager.getInstance();
    }

    private static WebdavFactory buildInfoFactory() throws Exception{
        final InfostoreWebdavFactory factory = new InfostoreWebdavFactory();
        final InfostoreFacadeImpl database = new InfostoreFacadeImpl();
        factory.setDatabase(database);
        factory.setSecurity(database.getSecurity());
        factory.setFolderLockManager(new FolderLockManagerImpl());
        factory.setFolderProperties(new PropertyStoreImpl("oxfolder_property"));
        factory.setInfoLockManager(new EntityLockManagerImpl("infostore_lock"));
        factory.setLockNullLockManager(new EntityLockManagerImpl("lock_null_lock"));
        factory.setInfoProperties(new PropertyStoreImpl("infostore_property"));
        factory.setProvider(new DBPoolProvider());
        factory.setResolver(new PathResolverImpl(factory.getDatabase()));

        final TestConfig config = new TestConfig();
        final TestContextToolkit tools = new TestContextToolkit();
        final String ctxName = config.getContextName();
        final Context ctx = null == ctxName || ctxName.trim().length() == 0 ? tools.getDefaultContext() : tools.getContextByName(ctxName);

        factory.setSessionHolder(new DummySessionHolder(getUsername(), ctx));
        return factory;
    }

    private static String getUsername() {
        final String un = AjaxInit.getAJAXProperty("login");
        final int pos = un.indexOf('@');
        return pos == -1 ? un : un.substring(0, pos);
    }

    public static void setUp() throws Exception {
        if(mode == INFO) {
            TestInit.loadTestProperties();
            Init.startServer();
        }
    }

    public static void tearDown() throws Exception {
        if(mode == INFO) {
            Init.stopServer();
        }
    }
}
