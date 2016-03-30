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
			if(behaviours == null) {
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
		if(behaviours == null) {
		    return null;
		}
		for(final Behaviour behaviour : behaviours) {
			if(behaviour.matches(request)) {
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

		if(LOG.isDebugEnabled()) {
			LOG.debug("Overrides for WebDAV:");
			for(final Map.Entry<Class<? extends Object>, List<Behaviour>> entry : registry.entrySet()) {
				for(final Behaviour behaviour : entry.getValue()) {
					LOG.debug("{} provides override for {}", behaviour, entry.getKey());
				}
			}
		}
	}

}
