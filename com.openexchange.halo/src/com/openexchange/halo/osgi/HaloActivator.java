package com.openexchange.halo.osgi;

import org.osgi.framework.ServiceReference;

import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.groupware.contact.ContactInterfaceDiscoveryService;
import com.openexchange.halo.ContactHalo;
import com.openexchange.halo.internal.ContactHaloImpl;
import com.openexchange.halo.HaloContactDataSource;
import com.openexchange.halo.contacts.ContactDataSource;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.SimpleRegistryListener;
import com.openexchange.session.SessionSpecificContainerRetrievalService;
import com.openexchange.user.UserService;

public class HaloActivator extends HousekeepingActivator {

	@Override
	protected Class<?>[] getNeededServices() {
		return new Class[]{UserService.class, ContactInterfaceDiscoveryService.class, SessionSpecificContainerRetrievalService.class, ConfigViewFactory.class};
	}

	@Override
	protected void startBundle() throws Exception {
		UserService userService = getService(UserService.class);
		ContactInterfaceDiscoveryService contactDiscoveryService = getService(ContactInterfaceDiscoveryService.class);
		SessionSpecificContainerRetrievalService sessionScope = getService(SessionSpecificContainerRetrievalService.class);
		ConfigViewFactory configViews = getService(ConfigViewFactory.class);

		final ContactHaloImpl halo = new ContactHaloImpl(userService, contactDiscoveryService, sessionScope, configViews);

		halo.addContactDataSource(new ContactDataSource());

		registerService(ContactHalo.class, halo);

		track(HaloContactDataSource.class, new SimpleRegistryListener<HaloContactDataSource>(){

			@Override
			public void added(ServiceReference<HaloContactDataSource> ref,
					HaloContactDataSource service) {
				halo.addContactDataSource(service);
			}

			@Override
			public void removed(ServiceReference<HaloContactDataSource> ref,
					HaloContactDataSource service) {
				halo.removeContactDataSource(service);
			}

		});

		openTrackers();

	}

}
