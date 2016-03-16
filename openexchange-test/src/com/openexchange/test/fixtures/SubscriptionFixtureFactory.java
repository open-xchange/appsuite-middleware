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
import com.openexchange.exception.OXException;
import com.openexchange.subscribe.Subscription;

/**
 * @author Martin Braun <martin.braun@open-xchange.com>
 */
public class SubscriptionFixtureFactory implements FixtureFactory<Subscription> {

    private final FixtureLoader fixtureLoader;

	public SubscriptionFixtureFactory(FixtureLoader fixtureLoader) {
		super();
		this.fixtureLoader = fixtureLoader;
	}

	@Override
    public Fixtures<Subscription> createFixture(final String fixtureName, final Map<String, Map<String, String>> entries) {
		return new SubscriptionFixtures(fixtureName, entries, fixtureLoader);
    }

    private class SubscriptionFixtures  extends DefaultFixtures<Subscription> implements Fixtures<Subscription>{
        private final Map<String, Map<String, String>> entries;
        private final Map<String, Fixture<Subscription>>  subscriptions = new HashMap<String,Fixture<Subscription>>();

        public SubscriptionFixtures(final String fixtureName, final Map<String, Map<String, String>> entries, FixtureLoader fixtureLoader) {
            super(Subscription.class, entries, fixtureLoader);
            this.entries = entries;
        }

        @Override
        public Fixture<Subscription> getEntry(final String entryName) throws OXException {
            if (subscriptions.containsKey(entryName)) {
                return subscriptions.get(entryName);
            }
            final Map<String, String> values = entries.get(entryName);
            if (null == values) {
                throw new FixtureException("Entry with name " + entryName + " not found");
            }

            defaults(values);

            final Subscription subscription = new Subscription();

            Map<String, Object> config = new HashMap<String, Object>();

            if (values.containsKey("source")) {
                config.put("source", values.get("source"));
                values.remove("source");
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

            subscription.setConfiguration(config);

            apply(subscription,values);

            final Fixture<Subscription> fixture = new Fixture<Subscription>(subscription, values.keySet().toArray(new String[values.size()]), values);

            subscriptions.put(entryName, fixture);
            return fixture;
        }

        private void defaults(final Map<String, String> values) {
        	if (false == values.containsKey("displayName")) {
        	    values.put("displayName", values.get("login"));
        	}

        	if (false == values.containsKey("secret")) {
        	    values.put("secret", values.get("false"));
        	}
        }
    }
}
