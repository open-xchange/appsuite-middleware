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

package com.openexchange.mobile.configuration.json.action.sms.osgi;

import java.util.Hashtable;
import com.openexchange.config.ConfigurationService;
import com.openexchange.mobile.configuration.json.action.ActionService;
import com.openexchange.mobile.configuration.json.action.ActionTypes;
import com.openexchange.mobile.configuration.json.action.sms.impl.ActionSMS;
import com.openexchange.osgi.HousekeepingActivator;

/**
 * @author Benjamin Otterbach
 */
public class ActionActivator extends HousekeepingActivator {

	public ActionActivator() {
		super();
	}

	@Override
	protected Class<?>[] getNeededServices() {
		return new Class<?>[] { ConfigurationService.class };
	}

	@Override
	protected void startBundle() throws Exception {
	    org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ActionActivator.class);
		try {
	        final Hashtable<String, ActionTypes> ht = new Hashtable<String, ActionTypes>();
	        ht.put("action", ActionTypes.TELEPHONE);
	        registerService(ActionService.class, new ActionSMS(this), ht);
		} catch (Throwable t) {
			LOG.error("", t);
			throw t instanceof Exception ? (Exception) t : new Exception(t);
		}

	}

}
