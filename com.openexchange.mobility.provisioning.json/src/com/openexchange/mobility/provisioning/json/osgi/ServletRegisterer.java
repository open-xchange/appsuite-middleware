/**
 * 
 */
package com.openexchange.mobility.provisioning.json.osgi;

import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

import com.openexchange.server.ServiceException;

/**
 * @author Benjamin Otterbach
 *
 */
public class ServletRegisterer {

	private static final Log LOG = LogFactory.getLog(ServletRegisterer.class);

	private final static String SERVLET_PATH = "/ajax/mobilityprovisioning";
	
	public ServletRegisterer (){
		super();
	}
	
	
	public void registerServlet() {
		final HttpService http_service;
		try {
			http_service = MobilityProvisioningServiceRegistry.getServiceRegistry().getService(HttpService.class, true);
		} catch (ServiceException e) {
			LOG.error("Error registering mobility provisioning servlet!", e);
			return;
		}
		try {
			http_service.registerServlet(SERVLET_PATH, new com.openexchange.mobility.provisioning.json.servlet.MobilityProvisioningServlet(), null, null);
		} catch (ServletException e) {
			LOG.error("Error registering mobility provisioning servlet!", e);
		} catch (NamespaceException e) {
			LOG.error("Error registering mobility provisioning servlet!", e);
		}
	}

	
	public void unregisterServlet() {
		final HttpService http_service;
		try {
			http_service = MobilityProvisioningServiceRegistry.getServiceRegistry().getService(HttpService.class, true);
		} catch (ServiceException e) {
			LOG.error("Error unregistering mobility provisioning servlet!", e);
			return;
		}
		http_service.unregister(SERVLET_PATH);
	}


}
