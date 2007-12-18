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

package com.openexchange.i18n.osgi;


import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;

import com.openexchange.config.Configuration;
import com.openexchange.server.ServiceHolderListener;
import com.openexchange.server.osgiservice.BundleServiceTracker;
import com.openexchange.i18n.impl.I18nConfiguration;
import com.openexchange.i18n.impl.I18nImpl;
import com.openexchange.i18n.impl.ResourceBundleDiscoverer;
import com.openexchange.i18n.I18nTools;


public class I18nActivator implements BundleActivator {

	private static final class I18nServiceHolderListener implements ServiceHolderListener<Configuration> {

		private final BundleContext context;
		private final ServiceRegistration serviceRegister;
		
		public I18nServiceHolderListener(final BundleContext context, ServiceRegistration serviceRegister) {
			super();
			this.context = context;
			this.serviceRegister = serviceRegister;
		}
		
		public void onServiceAvailable(final Configuration service) throws Exception {
			initI18nServices(context, serviceRegister);
		}

		public void onServiceRelease() throws Exception {
			// TODO Auto-generated method stub		
		}
		
	}
	
	private static final Log LOG = LogFactory.getLog(I18nActivator.class);
	
	private ServiceRegistration serviceRegister = null;	
	
	private final List<ServiceTracker> serviceTrackerList = new ArrayList<ServiceTracker>();
	
	
	
	public void start(BundleContext context) throws Exception {

		if (LOG.isDebugEnabled())
			LOG.debug("I18n Starting");
		
		
		try {
			serviceTrackerList.add(new ServiceTracker(context, Configuration.class.getName(),
					new BundleServiceTracker<Configuration>(context, I18nConfiguration.getInstance(),
							Configuration.class)));

			for (ServiceTracker tracker : serviceTrackerList) {
				tracker.open();
			}
			
			I18nConfiguration.getInstance().addServiceHolderListener(new I18nServiceHolderListener(context, serviceRegister));

		} catch (final Throwable e) {
			throw e instanceof Exception ? (Exception) e : new Exception(e);
		}	
		
		
		if (LOG.isDebugEnabled())
			LOG.debug("I18n Started");		
	}
	
	private static void initI18nServices(BundleContext context, ServiceRegistration serviceRegister) throws MalformedURLException, FileNotFoundException {
		
		//File dir = new File("/home/fred/i18n/osgi/");
		
		Configuration conf = I18nConfiguration.getInstance().getService();
		String directory_name = null;
		try {
			directory_name = conf.getProperty("i18n.language.path");
		} finally {
			I18nConfiguration.getInstance().ungetService(conf);
		}
		
		File dir = new File(directory_name);
        


        for (ResourceBundle rc : new ResourceBundleDiscoverer(dir).getResourceBundles()){
			Locale l = null;
			
		    I18nTools i18n = new I18nImpl(rc);
				
			Properties prop = new Properties();
			prop.put("language", rc.getLocale());
				
			serviceRegister = context.registerService(I18nTools.class.getName(), i18n, prop);
		}
	}


    public void stop(BundleContext context) throws Exception {
		if (LOG.isDebugEnabled())
			LOG.debug("Stopping I18n");
		
		try {
			serviceRegister.unregister();
			serviceRegister = null;

		} catch (final Throwable e) {
			LOG.error("SessiondActivator: start: ", e);
			throw e instanceof Exception ? (Exception) e : new Exception(e);
		}
		if (LOG.isDebugEnabled())
			LOG.debug("I18n stopped");
	}

}
 