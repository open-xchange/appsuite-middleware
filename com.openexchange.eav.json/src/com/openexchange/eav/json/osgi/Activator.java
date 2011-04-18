package com.openexchange.eav.json.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import com.openexchange.eav.EAVStorage;
import com.openexchange.eav.json.exception.EAVJsonExceptionMessage;
import com.openexchange.eav.json.multiple.EAVMultipleHandlerFactory;
import com.openexchange.eav.json.multiple.EAVServlet;
import com.openexchange.exceptions.osgi.ComponentRegistration;
import com.openexchange.multiple.MultipleHandlerFactoryService;
import com.openexchange.server.osgiservice.Whiteboard;
import com.openexchange.tools.service.SessionServletRegistration;
import com.openexchange.tools.servlet.http.HTTPServletRegistration;

public class Activator implements BundleActivator {

	private Whiteboard whiteboard;
    private ComponentRegistration componentRegistration;
    private SessionServletRegistration servletRegistration;

    public void start(BundleContext context) throws Exception {
	    whiteboard = new Whiteboard(context);
	    EAVMultipleHandlerFactory factory = new EAVMultipleHandlerFactory(whiteboard.getService(EAVStorage.class));
        context.registerService(MultipleHandlerFactoryService.class.getName(), factory, null);
        EAVServlet.setEAVMultipleHandlerFactory(factory);
        
	    componentRegistration = new ComponentRegistration(context, "EAVH", "com.openexchange.eav.json", EAVJsonExceptionMessage.EXCEPTIONS);
	    
	    servletRegistration = new SessionServletRegistration(context, new EAVServlet(), "/ajax/eav");
	    servletRegistration.open();
    }

	public void stop(BundleContext context) throws Exception {
        componentRegistration.unregister();
        servletRegistration.close();
	    whiteboard.close();
	}

}
