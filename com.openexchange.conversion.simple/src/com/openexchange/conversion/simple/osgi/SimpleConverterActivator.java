/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

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

/**
 * {@link SimpleConverterActivator} To register a simple conversion service to
 * convert Object "data" from a certain format into another certain format.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class SimpleConverterActivator extends HousekeepingActivator {

	@Override
	protected Class<?>[] getNeededServices() {
		return new Class[]{Converter.class};
	}

	/*
	 * Register the SimpleConverterService and listen for registrations of new
	 * SimplePayloadConverters. When new SimplePayloadConverters are added wrap
	 * them in a PayloadConverterAdapter and register them as ResultConverter
	 * service so they can be added to the DefaultConverter (as the
	 * DispatcherActivator is listening for new ResultConverter services)
	 */
	@Override
	protected void startBundle() throws Exception {
	    //Get the Default converter that is able to convert AJAXRequestResults from..to
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
