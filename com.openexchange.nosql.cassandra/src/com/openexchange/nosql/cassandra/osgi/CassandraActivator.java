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
package com.openexchange.nosql.cassandra.osgi;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.xerial.snappy.SnappyServiceLookUp;

import com.openexchange.config.ConfigurationService;
import com.openexchange.nosql.cassandra.CassandraServiceLookUp;
import com.openexchange.nosql.cassandra.EmbeddedCassandraService;
import com.openexchange.osgi.HousekeepingActivator;

/**
 * Activates an embedded Cassandra instance along with its dependencies (i.e. snappy-java)
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public final class CassandraActivator extends HousekeepingActivator {
	
	private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CassandraActivator.class);
	
	private static String snappyPathProp = "com.openexchange.nosql.cassandra.snappyjava.nativelibs";
	
	private EmbeddedCassandraService cassandra;

	/**
	 * Default Constructor 
	 */
	public CassandraActivator() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see com.openexchange.osgi.DeferredActivator#getNeededServices()
	 */
	@Override
	protected Class<?>[] getNeededServices() {
		
		return new Class[]{ConfigurationService.class};
	}

	/*
	 * (non-Javadoc)
	 * @see com.openexchange.osgi.DeferredActivator#startBundle()
	 */
	@Override
	protected void startBundle() throws Exception {
		log.info("starting bundle: com.openexchange.nosql.cassandra");
		
		//-------------- INIT SNAPPYJAVA ----------------//
		
		SnappyServiceLookUp.set(this);
		
		ConfigurationService configSnappy = SnappyServiceLookUp.getService(ConfigurationService.class);
		final File snappyFile = configSnappy.getFileByName("snappy.properties");
		if (null != snappyFile) {
            System.setProperty("snappy.config", snappyFile.getAbsolutePath().toString());
        }
		
		Properties prop = new Properties();
		String configUrl = System.getProperty("snappy.config");
		prop.load(new FileInputStream(configUrl));
		
		String snappyNativePath = prop.getProperty(snappyPathProp);
		
    	String osName = System.getProperty("os.name");
    	String osArch = System.getProperty("os.arch");
    	//String fileSeparator = System.getProperty("file.separator");
    	log.info("{} {}", osName, osArch);
    	
    	String postfix = null;
    	if (osName.equals("Linux")) {
    		postfix = ".so";
    	} else if (osName.equals("Windows")) {
    		postfix = ".dll";
    	} else if (osName.subSequence(0, 3).equals("Mac")) {
    		postfix = ".jnilib";
    		osName = osName.subSequence(0, 3).toString();
    	} else {
    		log.error("Unsupported system");
    	}

		String path = snappyNativePath + "/native/" + osName + "/" + osArch + "/libsnappyjava" + postfix;
    	System.load(path);
		
		//----------------- INIT CASSANDRA -------------//
		
		CassandraServiceLookUp.set(this);
		
		ConfigurationService config = CassandraServiceLookUp.getService(ConfigurationService.class);

		final File file = config.getFileByName("cassandra.yaml");
		if (null != file) {
            System.setProperty("cassandra.config", file.toURI().toString());
        }
		
		//start embedded cassandra node
		cassandra = new EmbeddedCassandraService();
		cassandra.init();
		cassandra.start();
		
		registerService(EmbeddedCassandraService.class, cassandra);
        openTrackers();
        
        log.info("Cassandra Service started successfully.");
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.openexchange.osgi.HousekeepingActivator#stopBundle()
	 */
	@Override
	protected void stopBundle() throws Exception {
		log.info("stopping bundle: com.openexchange.nosql.cassandra");
		cassandra.stop();
		cassandra = null;
		
		CassandraServiceLookUp.set(null);
		cleanUp();
		
		log.info("stopped bundle: com.openexchange.nosql.cassandra");
	}
}
