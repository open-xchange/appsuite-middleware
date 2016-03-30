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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.GroupParticipant;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.test.fixtures.transformators.BigDecimalTransformator;
import com.openexchange.test.fixtures.transformators.BooleanTransformator;
import com.openexchange.test.fixtures.transformators.ParticipantTransformator;
import com.openexchange.test.fixtures.transformators.PriorityTransformator;
import com.openexchange.test.fixtures.transformators.StatusTransformator;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 * @author Martin Braun <martin.braun@open-xchange.com>
 */
public class TaskFixtureFactory implements FixtureFactory<Task> {
    private final FixtureLoader fixtureLoader;
	private final GroupResolver groupResolver;

	public TaskFixtureFactory(GroupResolver groupResolver , FixtureLoader fixtureLoader) {
		super();
		this.fixtureLoader = fixtureLoader;
		this.groupResolver = groupResolver;
	}

	@Override
    public Fixtures<Task> createFixture(final String fixtureName, final Map<String, Map<String, String>> entries) {
        return new TaskFixtures(fixtureName, entries, fixtureLoader, groupResolver);
    }

    private class TaskFixtures extends DefaultFixtures<Task> implements Fixtures<Task> {
        private final Map<String, Map<String, String>> entries;

        private final Map<String, Fixture<Task>> tasks = new HashMap<String, Fixture<Task>>();

		private final GroupResolver groupResolver;

        public TaskFixtures(final String fixtureName, final Map<String, Map<String, String>> entries, FixtureLoader fixtureLoader, GroupResolver groupResolver) {
            super(Task.class, entries, fixtureLoader);
            this.entries = entries;
            this.groupResolver = groupResolver;

            addTransformator(new PriorityTransformator(), "priority");
            addTransformator(new StatusTransformator(), "status");
            addTransformator(new BooleanTransformator(), "private_flag");
            addTransformator(new BigDecimalTransformator(), "target_costs");
            addTransformator(new BigDecimalTransformator(), "actual_costs");
            addTransformator(new ParticipantTransformator(fixtureLoader), "participants");
        }

        @Override
        public Fixture<Task> getEntry(final String entryName) throws OXException {
            if (tasks.containsKey(entryName)) {
                return tasks.get(entryName);
            }
            final Map<String, String> values = entries.get(entryName);
            if (null == values) {
                throw new FixtureException("Entry with name " + entryName + " not found");
            }
            final Task task = new Task();
            apply(task, values);
            applyUsers(task, groupResolver);
            final Fixture<Task> fixture = new Fixture<Task>(task, values.keySet().toArray(new String[values.size()]), values);
            tasks.put(entryName, fixture);
            return fixture;
        }

        private void applyUsers(final Task task, GroupResolver groupResolver) {
        	if (null != task) {
        		final Participant[] participants = task.getParticipants();
        		if (null != participants) {
        			final List<UserParticipant> users = new ArrayList<UserParticipant>();
        			for (Participant participant : participants) {
						if (Participant.USER == participant.getType()) {
							users.add((UserParticipant)participant);
						} else if (Participant.GROUP == participant.getType()) {
							final GroupParticipant group = (GroupParticipant)participant;
							final Contact[] groupMembers = groupResolver.resolveGroup(group.getIdentifier());
							if (null != groupMembers) {
								for (Contact groupMember : groupMembers) {
									final UserParticipant userParticipant = new UserParticipant(groupMember.getInternalUserId());
									userParticipant.setDisplayName(groupMember.getDisplayName());
									userParticipant.setEmailAddress(groupMember.getEmail1());
									users.add(userParticipant);
								}
							}
						}
					}
        			task.setUsers(users);
        		}
        	}
        }
    }
}
