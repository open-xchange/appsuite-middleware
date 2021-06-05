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

    public TaskFixtureFactory(GroupResolver groupResolver, FixtureLoader fixtureLoader) {
        super();
        this.fixtureLoader = fixtureLoader;
        this.groupResolver = groupResolver;
    }

    @Override
    public Fixtures<Task> createFixture(final Map<String, Map<String, String>> entries) {
        return new TaskFixtures(entries, fixtureLoader, groupResolver);
    }

    private class TaskFixtures extends DefaultFixtures<Task> implements Fixtures<Task> {

        private final Map<String, Map<String, String>> entries;

        private final Map<String, Fixture<Task>> tasks = new HashMap<String, Fixture<Task>>();

        @SuppressWarnings("hiding")
        private final GroupResolver groupResolver;

        public TaskFixtures(final Map<String, Map<String, String>> entries, FixtureLoader fixtureLoader, GroupResolver groupResolver) {
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
                            users.add((UserParticipant) participant);
                        } else if (Participant.GROUP == participant.getType()) {
                            final GroupParticipant group = (GroupParticipant) participant;
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
