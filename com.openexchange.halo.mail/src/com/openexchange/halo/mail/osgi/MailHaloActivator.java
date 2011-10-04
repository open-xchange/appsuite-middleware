package com.openexchange.halo.mail.osgi;

import com.openexchange.halo.HaloContactDataSource;
import com.openexchange.halo.mail.MailHaloDataSource;
import com.openexchange.mail.service.MailService;
import com.openexchange.server.osgiservice.HousekeepingActivator;

public class MailHaloActivator extends HousekeepingActivator {

	@Override
	protected Class<?>[] getNeededServices() {
		return new Class[]{MailService.class};
	}

	@Override
	protected void startBundle() throws Exception {
		registerService(HaloContactDataSource.class, new MailHaloDataSource(this));
	}
}