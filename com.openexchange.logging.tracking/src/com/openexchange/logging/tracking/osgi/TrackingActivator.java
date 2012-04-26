package com.openexchange.logging.tracking.osgi;

import org.apache.commons.logging.Log;

import com.openexchange.context.ContextService;
import com.openexchange.log.LogWrapperFactory;
import com.openexchange.logging.tracking.TrackingConfiguration;
import com.openexchange.logging.tracking.SessionBasedTrackingConfiguration;
import com.openexchange.logging.tracking.TrackingFileLog;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.tools.session.SessionHolder;
import com.openexchange.user.UserService;

public class TrackingActivator extends HousekeepingActivator {

	private TrackingJMXActivator trackingJMXActivator;
	public TrackingActivator(TrackingJMXActivator trackingJMXActivator) {
		this.trackingJMXActivator = trackingJMXActivator;
	}

	@Override
	protected Class<?>[] getNeededServices() {
		return new Class<?>[]{SessionHolder.class, UserService.class, ContextService.class};
	}

	@Override
	protected void startBundle() throws Exception {
		final TrackingConfiguration config = new TrackingConfiguration(getService(UserService.class), getService(ContextService.class));
		
		final SessionHolder sh = getService(SessionHolder.class);
		
		
		registerService(LogWrapperFactory.class, new LogWrapperFactory() {
			
			public Log wrap(String className, Log log) {
				return new TrackingFileLog(new SessionBasedTrackingConfiguration(className, config, sh), log);
			}
		});
		
		trackingJMXActivator.setConfig(config);
	}

}
