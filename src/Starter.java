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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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



import java.net.ServerSocket;
import java.text.DecimalFormat;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.configuration.ConfigurationInit;
import com.openexchange.database.DatabaseInit;
import com.openexchange.database.Server;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.BackendServicesInit;
import com.openexchange.groupware.GroupwareInit;
import com.openexchange.server.ComfireConfig;
import com.openexchange.server.ComfireInitWorker;
import com.openexchange.server.DBPoolingException;
import com.openexchange.server.Version;

/**
 * Starter
 * 
 * @author <a href="mailto:martin.kauss@open-xchange.org">Martin Kauss</a>
 */

public class Starter {

	private ComfireConfig cf;

	private static String path;

	private static ServerSocket ss;

	private static final Log LOG = LogFactory.getLog(Starter.class);

	public Starter(String args[]) {

		if (LOG.isInfoEnabled()) {
			LOG.info("Open-Xchange 6.0");
			LOG.info("(c) Open-Xchange Inc. , Open-Xchange GmbH");
		}

		try {
			Properties p = System.getProperties();
			if (LOG.isInfoEnabled()) {
				LOG.info(p.getProperty("os.name") + " " + p.getProperty("os.arch") + " " + p.getProperty("os.version"));
			}
			path = p.getProperty("user.dir");
			if (LOG.isInfoEnabled()) {
				LOG.info(p.getProperty("java.runtime.version"));
			}
			long totalMemory = Runtime.getRuntime().totalMemory() / 1024;
			if (LOG.isInfoEnabled()) {
				LOG.info("VM Total Memory       : " + DecimalFormat.getNumberInstance().format(totalMemory) + " KB");
			}
			long freeMemory = Runtime.getRuntime().freeMemory() / 1024;
			if (LOG.isInfoEnabled()) {
				LOG.info("VM Free Memory        : " + DecimalFormat.getNumberInstance().format(freeMemory) + " KB");
			}
			long usedMemory = totalMemory - freeMemory;
			if (LOG.isInfoEnabled()) {
				LOG.info("VM Used Memory        : " + DecimalFormat.getNumberInstance().format(usedMemory) + " KB");
			}
		} catch (Exception gee) {
			LOG.error(gee.getMessage(), gee);
		}

		if (LOG.isInfoEnabled()) {
			LOG.info("System version : Open-Xchange Server [" + Version.BUILDNUMBER + "] initializing ...");
			LOG.info("Server Footprint : " + AbstractOXException.SERVER_ID);
		}

		/* Config done */
        ComfireConfig.loadProperties(System.getProperty("openexchange.propfile"));
        try {
            ConfigurationInit.init();
        } catch (AbstractOXException e) {
            LOG.error("Initializing the configuration failed.", e);
            System.exit(1);
        }
        
		try {
			if (LOG.isInfoEnabled()) {
				LOG.info("DEBUG: CLL -> " + ComfireConfig.properties.getProperty("InitWorker"));
			}
			Class.forName(ComfireConfig.properties.getProperty("InitWorker"));
			ComfireInitWorker worker = ComfireInitWorker.getWorker();
			worker.doInit();
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}

        try {
        	if (LOG.isInfoEnabled()) {
        		LOG.info("Server name: " + Server.getServerName());
        	}
            DatabaseInit.init();
        } catch (DBPoolingException e) {
            LOG.error("Initializing the database system failed.", e);
            System.exit(1);
        }
		if (LOG.isInfoEnabled()) {
			LOG.info("Database system successfully initialized");
		}

        // DBPool dbpool = new DBPool(ComfireConfig.getDBPool(),
		// ComfireConfig.getWriteDBPool());

        // New server startup.
		try {
            GroupwareInit.init();
        } catch (AbstractOXException e) {
            LOG.error("Initializing the groupware server failed.", e);
            System.exit(1);
        }
		if (LOG.isInfoEnabled()) {
			LOG.info("Groupware server successfully initialized.");
		}
        
        try {
        	BackendServicesInit.init();
        } catch (AbstractOXException e) {
            LOG.error("Initializing the backend services failed.", e);
            System.exit(1);
        }
		if (LOG.isInfoEnabled()) {
			LOG.info("Backend services successfully initialized.");
		}
        
        /*
         * TODO: Check property ENABLE_INTERNAL_USER_EDIT
         * OXFolderSQL.updateCtxAddrBookPermission(FolderCacheProperties.isEnableInternalUsersEdit())
         */

		if (LOG.isInfoEnabled()) {
			LOG.info("SYSTEM IS UP & RUNNING...");
		}
		
        // FIXME implement a server shutdown
		try {
            synchronized (Starter.class) {
                Starter.class.wait();
            }
        } catch (InterruptedException e) {
            LOG.error(e.getMessage(), e);
        }
	}
}
