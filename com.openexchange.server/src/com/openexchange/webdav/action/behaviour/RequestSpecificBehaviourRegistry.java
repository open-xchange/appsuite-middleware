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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.webdav.action.WebdavRequest;

public class RequestSpecificBehaviourRegistry {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(RequestSpecificBehaviourRegistry.class);


	//Generic-o-rama! Is it just me or does Java start to gain the same aesthetic appeal as c++?
	private final Map<Class<? extends Object>, List<Behaviour>> registry = new HashMap<Class<? extends Object>, List<Behaviour>>();

	public void add(final Behaviour behaviour) {
		for(final Class<? extends Object> clazz : behaviour.provides()) {
			List<Behaviour> behaviours = registry.get(clazz);
			if (behaviours == null) {
				behaviours = new ArrayList<Behaviour>();
				registry.put(clazz, behaviours);
			}
			behaviours.add(behaviour);
		}
	}

	public void addAll(final Collection<Behaviour> behaviours) {
		for(final Behaviour behaviour : behaviours) { add (behaviour); }
	}

	public void setBehaviours(final Collection<Behaviour> behaviours) {
		registry.clear();
		addAll(behaviours);
	}

	public <T> T get(final WebdavRequest request, final Class<T> clazz) {
		final List<Behaviour> behaviours = registry.get(clazz);
		if (behaviours == null) {
		    return null;
		}
		for(final Behaviour behaviour : behaviours) {
			if (behaviour.matches(request)) {
				return behaviour.get(clazz);
			}
		}
		return null;
	}

	public void log() {
		LOG.info("Using {} overrides for WebDAV", new Object() { @Override public String toString() {
            int sum = 0;
            for(final Map.Entry<Class<? extends Object>, List<Behaviour>> entry : registry.entrySet()) {
                sum += entry.getValue().size();
            }
            return Integer.toString(sum);
        }});

		if (LOG.isDebugEnabled()) {
			LOG.debug("Overrides for WebDAV:");
			for(final Map.Entry<Class<? extends Object>, List<Behaviour>> entry : registry.entrySet()) {
				for(final Behaviour behaviour : entry.getValue()) {
					LOG.debug("{} provides override for {}", behaviour, entry.getKey());
				}
			}
		}
	}

}
