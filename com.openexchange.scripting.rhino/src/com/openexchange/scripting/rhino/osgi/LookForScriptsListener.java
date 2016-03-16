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

package com.openexchange.scripting.rhino.osgi;

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
import static com.openexchange.scripting.rhino.SharedScope.*;
import com.openexchange.osgi.ExceptionUtils;
import com.openexchange.scripting.rhino.libs.Console;
import com.openexchange.scripting.rhino.require.RequireSupport;

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

			RequireSupport.initialize(serviceScope, cx, new BundleJSBundle(bundle), additionalModules);
			Console.initialize(serviceScope, bundle.getSymbolicName());

			cx.evaluateReader(serviceScope, r, bundle.getSymbolicName()+"/main.js", 1, null);

		} catch (Throwable t) {
		    ExceptionUtils.handleThrowable(t);
			LOGGER.error("", t);
		} finally {
			try {
				r.close();
			} catch (IOException e) {
				// Ignore
			}
			Context.exit();
		}
	}

}
