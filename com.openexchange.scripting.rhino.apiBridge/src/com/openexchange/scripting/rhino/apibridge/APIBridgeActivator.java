/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.scripting.rhino.apibridge;


import com.openexchange.ajax.requesthandler.osgiservice.AJAXModuleActivator;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.scripting.rhino.require.DependencyResolver;
import com.openexchange.scripting.rhino.require.JSConverter;
import com.openexchange.scripting.rhino.require.ResolveEnhancement;

public class APIBridgeActivator extends AJAXModuleActivator {

	@Override
	protected Class<?>[] getNeededServices() {
		return new Class[]{ConfigViewFactory.class};
	}

	@Override
	protected void startBundle() throws Exception {
		registerService(ResolveEnhancement.class, new ResolveEnhancement() {

			@Override
			public void enhance(final DependencyResolver resolver,
					final JSConverter jsConverter) {
				resolver.remember("httpAPI", jsConverter.toJS(new HTTPAPISupport(APIBridgeActivator.this)));
			}

		});
	}


}
