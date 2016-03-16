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
package com.openexchange.test.fixtures;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import com.openexchange.exception.OXException;

/**
 * @author Martin Braun <martin.braun@open-xchange.com>
 */
public class MessagingFixtureFactory implements FixtureFactory<Messaging> {

    private final FixtureLoader fixtureLoader;

	public MessagingFixtureFactory(FixtureLoader fixtureLoader) {
		super();
		this.fixtureLoader = fixtureLoader;
	}

	@Override
    public Fixtures<Messaging> createFixture(final String fixtureName, final Map<String, Map<String, String>> entries) {
		return new MessagingFixtures(fixtureName, entries, fixtureLoader);
    }

    private class MessagingFixtures  extends DefaultFixtures<Messaging> implements Fixtures<Messaging>{
        private final Map<String, Map<String, String>> entries;
        private final Map<String, Fixture<Messaging>>  messagings = new HashMap<String,Fixture<Messaging>>();

        public MessagingFixtures(final String fixtureName, final Map<String, Map<String, String>> entries, FixtureLoader fixtureLoader) {
            super(Messaging.class, entries, fixtureLoader);
            this.entries = entries;
        }

        @Override
        public Fixture<Messaging> getEntry(final String entryName) throws OXException {
            if (messagings.containsKey(entryName)) {
                return messagings.get(entryName);
            }
            final Map<String, String> values = entries.get(entryName);
            if (null == values) {
                throw new FixtureException("Entry with name " + entryName + " not found");
            }

            defaults(values);

            final Messaging messaging = new Messaging();

            Map<String, Object> config = new HashMap<String, Object>();

            if (values.containsKey("type")) {
                config.put("type", values.get("type"));
                values.remove("type");
            }
            if (values.containsKey("name")) {
                config.put("name", values.get("name"));
                values.remove("name");
            }
            if (values.containsKey("url")) {
                config.put("url", values.get("url"));
                values.remove("url");
            }
            if (values.containsKey("login")) {
                config.put("login", values.get("login"));
                values.remove("login");
            }
            if (values.containsKey("password")) {
                config.put("password", values.get("password"));
                values.remove("password");
            }
            if (values.containsKey("message")) {
                config.put("message", values.get("message")+ " - " + UUID.randomUUID().toString());
                values.remove("message");
            }

            messaging.setConfiguration(config);

            apply(messaging,values);

            final Fixture<Messaging> fixture = new Fixture<Messaging>(messaging, values.keySet().toArray(new String[values.size()]), values);

            messagings.put(entryName, fixture);
            return fixture;
        }

        private void defaults(final Map<String, String> values) {
        	if (false == values.containsKey("displayName")) {
        	    values.put("displayName", values.get("login"));
        	}
        }
    }
}
