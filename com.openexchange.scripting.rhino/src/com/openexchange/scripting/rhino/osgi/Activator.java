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

package com.openexchange.scripting.rhino.osgi;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.SimpleRegistryListener;
import com.openexchange.scripting.rhino.require.RequireSupport;
import com.openexchange.scripting.rhino.require.ResolveEnhancement;

public class Activator extends HousekeepingActivator {


	@Override
	protected Class<?>[] getNeededServices() {
		return null;
	}

	@Override
	protected void startBundle() throws Exception {
		LookForScriptsListener listener = new LookForScriptsListener();
		Bundle[] bundles = context.getBundles();
		for (Bundle bundle : bundles) {
			if (bundle.getState() == Bundle.ACTIVE) {
				listener.runStartScripts(bundle);
			}
		}

		context.addBundleListener(listener);
		track(ResolveEnhancement.class, new SimpleRegistryListener<ResolveEnhancement>() {

			@Override
			public void added(ServiceReference<ResolveEnhancement> ref,
					ResolveEnhancement service) {
				RequireSupport.addResolveEnhancement(service);

			}

			@Override
			public void removed(ServiceReference<ResolveEnhancement> ref,
					ResolveEnhancement service) {
				//RequireSupport.resolveEnhancements.remove(service);
				// TODO
			}
		});

		openTrackers();
	}


}
