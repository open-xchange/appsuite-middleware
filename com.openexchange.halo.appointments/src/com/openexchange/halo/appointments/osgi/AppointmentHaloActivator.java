package com.openexchange.halo.appointments.osgi;


import com.openexchange.groupware.calendar.AppointmentSqlFactoryService;
import com.openexchange.halo.HaloContactDataSource;
import com.openexchange.halo.appointments.AppointmentContactHalo;
import com.openexchange.osgi.HousekeepingActivator;

public class AppointmentHaloActivator extends HousekeepingActivator {

	@Override
	protected Class<?>[] getNeededServices() {
		return new Class[]{AppointmentSqlFactoryService.class};
	}

	@Override
	protected void startBundle() throws Exception {
		registerService(HaloContactDataSource.class, new AppointmentContactHalo(this));
	}

	@Override
	protected void stopBundle() throws Exception {
	    unregisterServices();
	}



}
