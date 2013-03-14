package com.openexchange.printing.osgi;

import java.util.concurrent.atomic.AtomicReference;

import com.openexchange.i18n.I18nService;
import com.openexchange.server.ServiceLookup;

public class PrintingServices {
	public static final AtomicReference<ServiceLookup> LOOKUP = new AtomicReference<ServiceLookup>();
	
	public static I18nService getI18nService(){
		return LOOKUP.get().getService(I18nService.class);
	}

}
