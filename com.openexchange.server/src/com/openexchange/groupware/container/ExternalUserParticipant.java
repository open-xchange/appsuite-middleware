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

import com.openexchange.groupware.container.participants.AbstractConfirmableParticipant;
import com.openexchange.groupware.container.participants.ConfirmableParticipant;

/**
 * {@link ExternalUserParticipant} - Represents an external user participant.
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */
public class ExternalUserParticipant extends AbstractConfirmableParticipant implements Comparable<Participant> {

    private static final long serialVersionUID = 7731174024066565165L;

    private int id = NO_ID;

    private String displayName;

    private String emailaddress;

    private boolean ignoreNotification;

    /**
     * Default constructor.
     *
     * @param emailAddress The unique email address of the external participant.
     */
    public ExternalUserParticipant(final String emailAddress) {
        super();
        setEmailAddress(emailAddress);
    }

    @Deprecated
    @Override
    public void setIdentifier(final int id) {
        this.id = id;
    }

    @Override
    public int getIdentifier() {
        return id;
    }

    @Override
    public void setDisplayName(final String displayName) {
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
        return EXTERNAL_USER;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + EXTERNAL_USER;
        result = prime * result + id;
        result = prime * result + ((emailaddress == null) ? 0 : emailaddress.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ExternalUserParticipant)) {
            return false;
        }
        final ExternalUserParticipant other = (ExternalUserParticipant) obj;
        if (emailaddress == null) {
            if (other.emailaddress != null) {
                return false;
            }
        } else if (!emailaddress.equals(other.emailaddress)) {
            return false;
        }
        if (id != other.id) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(final Participant part) {
        final int retval;
        if (EXTERNAL_USER == part.getType()) {
            if (null == emailaddress) {
                if (null == part.getEmailAddress()) {
                    retval = 0;
                } else {
                    retval = -1;
                }
            } else {
                if (null == part.getEmailAddress()) {
                    retval = 1;
                } else {
                    retval = emailaddress.compareTo(part.getEmailAddress());
                }
            }
        } else {
            retval = Integer.compare(EXTERNAL_USER, part.getType());
        }
        return retval;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Extern mail:");
        sb.append(getEmailAddress());
        return sb.toString();
    }

    @Override
    public ExternalUserParticipant clone() throws CloneNotSupportedException {
        ExternalUserParticipant retval = (ExternalUserParticipant) super.clone();

        retval.setDisplayName(this.getDisplayName());
        retval.emailaddress = this.emailaddress;
        retval.setIdentifier(this.getIdentifier());
        retval.setIgnoreNotification(this.isIgnoreNotification());

        return retval;
    }

    @Override
    public ConfirmableParticipant getClone() throws CloneNotSupportedException {
        return clone();
    }

    @Override
    public boolean isIgnoreNotification() {
        return ignoreNotification;
    }

    @Override
    public void setIgnoreNotification(final boolean ignoreNotification) {
        this.ignoreNotification = ignoreNotification;
    }
}
