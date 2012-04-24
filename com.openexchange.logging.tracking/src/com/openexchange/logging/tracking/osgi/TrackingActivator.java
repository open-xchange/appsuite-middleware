package com.openexchange.logging.tracking.osgi;

import org.apache.commons.logging.Log;

import com.openexchange.log.LogWrapperFactory;
import com.openexchange.logging.tracking.GlobalTrackingConfiguration;
import com.openexchange.logging.tracking.SessionBasedTrackingConfiguration;
import com.openexchange.logging.tracking.TrackingFileLog;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.tools.session.SessionHolder;

public class TrackingActivator extends HousekeepingActivator {

	@Override
	protected Class<?>[] getNeededServices() {
		return new Class<?>[]{SessionHolder.class};
	}

	@Override
	protected void startBundle() throws Exception {
		final GlobalTrackingConfiguration config = new GlobalTrackingConfiguration();
		final SessionHolder sh = getService(SessionHolder.class);
		
		
		registerService(LogWrapperFactory.class, new LogWrapperFactory() {
			
			public Log wrap(String className, Log log) {
				return new TrackingFileLog(new SessionBasedTrackingConfiguration(className, config, sh), log);
			}
		});
	}

}
