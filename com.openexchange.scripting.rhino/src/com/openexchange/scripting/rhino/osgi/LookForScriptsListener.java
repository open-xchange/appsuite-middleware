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

import static com.openexchange.scripting.rhino.SharedScope.SHARED_SCOPE;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.HashMap;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.slf4j.Logger;
import com.openexchange.osgi.ExceptionUtils;
import com.openexchange.scripting.rhino.libs.Console;
import com.openexchange.scripting.rhino.require.RequireSupport;

/**
 *
 * {@link LookForScriptsListener}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class LookForScriptsListener implements BundleListener {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(LookForScriptsListener.class);

	@Override
	public void bundleChanged(BundleEvent event) {
		switch(event.getType()) {
		case BundleEvent.STARTED: runStartScripts(event.getBundle());
		}
	}

	public void runStartScripts(final Bundle bundle) {
		URL entry = bundle.getEntry("/main.js");
		if (entry == null) {
			entry = bundle.getEntry("/js/main.js");
			if (entry == null) {
				return;
			}
		}
		Reader r = null;
		try {
			r = new InputStreamReader(entry.openStream(), "UTF-8");
			Context cx = Context.enter();
			Scriptable serviceScope = cx.newObject(SHARED_SCOPE);
			serviceScope.setParentScope(null);
			serviceScope.setPrototype(SHARED_SCOPE);

			HashMap<String, Object> additionalModules = new HashMap<String, Object>();
			additionalModules.put("osgi", new OSGiSupport(bundle.getBundleContext(), SHARED_SCOPE));

			RequireSupport.initialize(serviceScope, new BundleJSBundle(bundle), additionalModules);
			Console.initialize(serviceScope, bundle.getSymbolicName());

			cx.evaluateReader(serviceScope, r, bundle.getSymbolicName()+"/main.js", 1, null);

		} catch (Throwable t) {
		    ExceptionUtils.handleThrowable(t);
			LOGGER.error("", t);
		} finally {
			try {
                if (r != null) {
                    r.close();
                }
			} catch (@SuppressWarnings("unused") IOException e) {
				// Ignore
			}
			Context.exit();
		}
	}

}
