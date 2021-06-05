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

package com.openexchange.groupware.container;

/**
 * {@link GroupParticipant} - Represents an internal group participant.
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */
public class GroupParticipant implements Participant, Comparable<Participant> {

    private static final long serialVersionUID = 246682125014920210L;

    private int id;
    private String displayName;
    private String emailaddress;
    private boolean ignoreNotification;

    public GroupParticipant(int id) {
        super();
        this.id = id;
    }

    /**
     * @deprecated Use {@link #GroupParticipant(int)}
     */
    @Deprecated
    @Override
    public void setIdentifier(int id) {
        this.id = id;
    }

    @Override
    public int getIdentifier() {
        return id;
    }

    @Override
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getEmailAddress() {
        return emailaddress == null ? null : emailaddress.toLowerCase();
    }

    public void setEmailAddress(String emailaddress) {
        this.emailaddress = emailaddress == null ? null : emailaddress.toLowerCase();
    }

    @Override
    public int getType() {
        return GROUP;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + GROUP;
        result = prime * result + id;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GroupParticipant)) {
            return false;
        }
        final GroupParticipant other = (GroupParticipant) obj;
        return id == other.id;
    }

    @Override
    public int compareTo(Participant part) {
        final int retval;
        if (GROUP == part.getType()) {
            retval = Integer.compare(id, part.getIdentifier());
        } else {
            retval = Integer.compare(GROUP, part.getType());
        }
        return retval;
    }

    @Override
    public GroupParticipant clone() {
        GroupParticipant retval = new GroupParticipant(id);
        retval.setDisplayName(displayName);
        retval.setEmailAddress(emailaddress);
        retval.setIgnoreNotification(ignoreNotification);
        return retval;
    }

    @Override
    public Participant getClone() {
        return clone();
    }

    @Override
    public boolean isIgnoreNotification() {
        return ignoreNotification;
    }

    @Override
    public void setIgnoreNotification(boolean ignoreNotification) {
        this.ignoreNotification = ignoreNotification;
    }

    @Override
    public String toString() {
        return "Group participant " + id + ",\"" + displayName + "\"";
    }
}
