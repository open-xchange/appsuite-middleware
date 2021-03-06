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

package com.openexchange.webdav.action.behaviour;

import com.openexchange.webdav.action.WebdavRequest;

public class BehaviourLookup {

	private static final BehaviourLookup INSTANCE = new BehaviourLookup();

	public static BehaviourLookup getInstance(){
		return INSTANCE;
	}


	private final ThreadLocal<WebdavRequest> requestHolder = new ThreadLocal<WebdavRequest>();
	private RequestSpecificBehaviourRegistry registry = null;


	public void setRequest(final WebdavRequest req) {
		requestHolder.set(req);
	}


	public void unsetRequest(){
		requestHolder.set(null);
	}

	public void setRegistry(final RequestSpecificBehaviourRegistry reg) {
		registry = reg;
	}

	public <T> T get(final Class<T> clazz) {
		if (null == registry) {
			return null;
		}
		final WebdavRequest req = requestHolder.get();
		if (req == null) {
			return null;
		}

		return registry.get(req, clazz);
	}
}
