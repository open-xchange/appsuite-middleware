package com.openexchange.halo.osgi;

import org.osgi.framework.ServiceReference;

import com.openexchange.groupware.contact.ContactInterfaceDiscoveryService;
import com.openexchange.halo.AsynchronousHaloContactDataSource;
import com.openexchange.halo.ContactHalo;
import com.openexchange.halo.internal.ContactHaloImpl;
import com.openexchange.halo.HaloContactDataSource;
import com.openexchange.halo.contacts.ContactDataSource;
import com.openexchange.server.osgiservice.HousekeepingActivator;
import com.openexchange.server.osgiservice.SimpleRegistryListener;
import com.openexchange.session.SessionSpecificContainerRetrievalService;
import com.openexchange.user.UserService;

public class HaloActivator extends HousekeepingActivator {

	@Override
	protected Class<?>[] getNeededServices() {
		return new Class[]{UserService.class, ContactInterfaceDiscoveryService.class, SessionSpecificContainerRetrievalService.class};
	}

	@Override
	protected void startBundle() throws Exception {
		UserService userService = getService(UserService.class);
		ContactInterfaceDiscoveryService contactDiscoveryService = getService(ContactInterfaceDiscoveryService.class);
		SessionSpecificContainerRetrievalService sessionScope = getService(SessionSpecificContainerRetrievalService.class);
		
		final ContactHaloImpl halo = new ContactHaloImpl(userService, contactDiscoveryService, sessionScope);
		
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
		
		track(AsynchronousHaloContactDataSource.class, new SimpleRegistryListener<AsynchronousHaloContactDataSource>() {

			@Override
			public void added(
					ServiceReference<AsynchronousHaloContactDataSource> ref,
					AsynchronousHaloContactDataSource service) {
				halo.addAsyncContactDataSource(service);
			}

			@Override
			public void removed(
					ServiceReference<AsynchronousHaloContactDataSource> ref,
					AsynchronousHaloContactDataSource service) {
				halo.removeAsyncContactDataSource(service);
			}
		});
		
	}

}
