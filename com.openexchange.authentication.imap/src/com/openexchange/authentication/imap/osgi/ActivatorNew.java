package com.openexchange.authentication.imap.osgi;

import static com.openexchange.authentication.imap.osgi.ImapAuthServiceRegistry.getServiceRegistry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceRegistration;

import com.openexchange.authentication.AuthenticationService;
import com.openexchange.authentication.imap.impl.IMAPAuthentication;
import com.openexchange.config.ConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.server.osgiservice.DeferredActivator;
import com.openexchange.server.osgiservice.ServiceRegistry;
import com.openexchange.user.UserService;

public class ActivatorNew extends DeferredActivator {
	
	private static transient final Log LOG = LogFactory.getLog(ActivatorNew.class);

	private static final Class<?>[] NEEDED_SERVICES = { ConfigurationService.class,ContextService.class,UserService.class};
	
	private ServiceRegistration registration;
	
	//private ServiceRegistration serviceRegistration;

	public ActivatorNew() {
		super();		
	}
	
	@Override
	protected Class<?>[] getNeededServices() {
		return NEEDED_SERVICES;
	}

	@Override
	protected void handleAvailability(Class<?> clazz) {
		if (LOG.isWarnEnabled()) {
			LOG.warn("Absent service: " + clazz.getName());
		}
		
		getServiceRegistry().addService(clazz, getService(clazz));
		// wenn alle services da und nicht authservice published, dann authservice publishen
		if(registration==null){
			ContextService contextService = getServiceRegistry().getService(ContextService.class);
			UserService userService = getServiceRegistry().getService(UserService.class);
			if(contextService!=null && userService!=null){
				registration = context.registerService(AuthenticationService.class.getName(), new IMAPAuthentication(contextService, userService), null);
			}
		}
	}

	@Override
	protected void handleUnavailability(Class<?> clazz) {
		if (LOG.isInfoEnabled()) {
			LOG.info("Re-available service: " + clazz.getName());
		}		
		getServiceRegistry().removeService(clazz);
		// wenn authservice gepublished, dann publish wegnehmen
		if(registration!=null){
			registration.unregister();
			registration = null;
		}
	}

	@Override
	protected void startBundle() throws Exception {
		
		try {
			{
				final ServiceRegistry registry = getServiceRegistry();
				registry.clearRegistry();
				final Class<?>[] classes = getNeededServices();
				for (int i = 0; i < classes.length; i++) {
					final Object service = getService(classes[i]);
					if (null != service) {
						registry.addService(classes[i], service);
					}
				}
			}
			if(registration==null){
					ContextService contextService = getServiceRegistry().getService(ContextService.class);
					UserService userService = getServiceRegistry().getService(UserService.class);
					// 	authservice publishen
					registration = context.registerService(AuthenticationService.class.getName(), new IMAPAuthentication(contextService, userService), null);
			}
		} catch (final Throwable t) {
			LOG.error(t.getMessage(), t);
			throw t instanceof Exception ? (Exception) t : new Exception(t);
		}
		
	}

	@Override
	protected void stopBundle() throws Exception {
		try {			
			// wenn authservice gepublished, dann publish wegnehmen
			if(registration!=null){
				registration.unregister();
				registration = null;
			}
			
			
			getServiceRegistry().clearRegistry();
		} catch (final Throwable t) {
			LOG.error(t.getMessage(), t);
			throw t instanceof Exception ? (Exception) t : new Exception(t);
		}
	}
}
