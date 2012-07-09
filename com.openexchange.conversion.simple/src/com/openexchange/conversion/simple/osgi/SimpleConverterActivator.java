package com.openexchange.conversion.simple.osgi;

import org.osgi.framework.ServiceReference;

import com.openexchange.ajax.requesthandler.Converter;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.conversion.simple.SimpleConverter;
import com.openexchange.conversion.simple.SimplePayloadConverter;
import com.openexchange.conversion.simple.impl.AJAXConverterAdapter;
import com.openexchange.conversion.simple.impl.PayloadConverterAdapter;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.SimpleRegistryListener;

public class SimpleConverterActivator extends HousekeepingActivator {

	@Override
	protected Class<?>[] getNeededServices() {
		return new Class[]{Converter.class};
	}

	@Override
	protected void startBundle() throws Exception {
		Converter converter = getService(Converter.class);
		final AJAXConverterAdapter rtConverter = new AJAXConverterAdapter(converter);
		registerService(SimpleConverter.class, rtConverter);
		
		
		track(SimplePayloadConverter.class, new SimpleRegistryListener<SimplePayloadConverter>() {

			public void added(ServiceReference<SimplePayloadConverter> ref,
					SimplePayloadConverter service) {
				registerService(ResultConverter.class, new PayloadConverterAdapter(service, rtConverter));
			}

			public void removed(ServiceReference<SimplePayloadConverter> ref,
					SimplePayloadConverter service) {
				// TODO: Figure out something here
			}
		});
		
		openTrackers();
		
	}

}
