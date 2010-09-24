/**
 * 
 */
package com.openexchange.upsell.multiple.osgi;

import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

import com.openexchange.config.ConfigurationService;
import com.openexchange.server.ServiceException;

/**
 * @author Manuel Kraft
 *
 */
public class MyServletRegisterer {

	private static final Log LOG = LogFactory.getLog(MyServletRegisterer.class);
	
	public MyServletRegisterer (){
		super();	
	}
	
	
	public void registerServlet() {
		final HttpService http_service;
		try {
			http_service = MyServiceRegistry.getServiceRegistry().getService(HttpService.class, true);
		} catch (ServiceException e) {
			LOG.error("Error registering servlet!", e);
			return;
		}
		try {
			
			http_service.registerServlet(getFromConfig("com.openexchange.upsell.multiple.servlet"), new com.openexchange.upsell.multiple.impl.MyServlet(), null, null);
		} catch (ServletException e) {
			LOG.error("Error registering servlet!", e);
		} catch (NamespaceException e) {
			LOG.error("Error registering servlet!", e);
		} catch (ServiceException e) {
			LOG.error("Error registering servlet cause of missing property!", e);
		}catch (NullPointerException e) {
			LOG.error("Error registering servlet cause of missing property!", e);
		}
	}
	
	private String getFromConfig(String key) throws ServiceException{
		ConfigurationService configservice = MyServiceRegistry.getServiceRegistry().getService(ConfigurationService.class,true);
		return configservice.getProperty(key);
	}

	
	public void unregisterServlet() {
		final HttpService http_service;
		try {
			http_service = MyServiceRegistry.getServiceRegistry().getService(HttpService.class, true);
		} catch (ServiceException e) {
			LOG.error("Error unregistering servlet!", e);
			return;
		}
		try {
			http_service.unregister(getFromConfig("com.openexchange.upsell.multiple.servlet"));
		} catch (ServiceException e) {
			LOG.error("Error unregistering  config servlet cause of missing property!", e);
		}catch (NullPointerException e) {
			LOG.error("Error unregistering  config servlet cause of missing property!", e);
		}
	}

}
