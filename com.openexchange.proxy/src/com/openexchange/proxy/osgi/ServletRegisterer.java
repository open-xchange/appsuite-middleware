/**
 * 
 */
package com.openexchange.proxy.osgi;

import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import com.openexchange.proxy.ReverseProxy;

/**
 * @author mbiggeleben
 *
 */
public class ServletRegisterer implements ServiceTrackerCustomizer {

	private static final Log LOG = LogFactory.getLog(ServletRegisterer.class);
	private final BundleContext context;
	
	public ServletRegisterer(BundleContext context) {
		
		super();
		this.context = context;
	}

	public Object addingService(ServiceReference reference) {
		HttpService service = (HttpService) context.getService(reference);
		try {
			service.registerServlet("/proxy", new ReverseProxy(), null, null);
		} catch (ServletException e) {
			LOG.error(e.getMessage(), e);
		} catch (NamespaceException e) {
			LOG.error(e.getMessage(), e);
		}
		return service;
	}

	public void modifiedService(ServiceReference reference, Object service) {
	}

	public void removedService(ServiceReference reference, Object serv) {
		HttpService service = (HttpService) serv;
		service.unregister("/proxy");
		context.ungetService(reference);
	}
}
