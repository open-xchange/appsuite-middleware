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
 * {@link ExternalGroupParticipant} - Represent an external group participant.
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */
public class ExternalGroupParticipant implements Participant, Comparable<Participant> {

    private static final long serialVersionUID = -2048639372069048097L;

    private int id;

    private String displayName;

    private String emailaddress;

    private boolean ignoreNotification;

    /**
     * Default constructor.
     *
     * @param emailAddress unique email address of the external group participant.
     */
    public ExternalGroupParticipant(final String emailAddress) {
        super();
        setEmailAddress(emailAddress);
    }

    /**
     * @deprecated Use {@link #ExternalGroupParticipant(String)}.
     */
    @Deprecated
    public ExternalGroupParticipant() {
        this(null);
    }

    /**
     * {@inheritDoc}
     * @deprecated
     */
    @Deprecated
    @Override
    public void setIdentifier(final int id) {
        this.id = id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getIdentifier() {
        return id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDisplayName() {
        return displayName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEmailAddress() {
        return emailaddress == null ? null : emailaddress.toLowerCase();
    }

    /**
     * {@inheritDoc}
     */
    public void setEmailAddress(final String emailaddress) {
        this.emailaddress = emailaddress == null ? null : emailaddress.toLowerCase();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getType() {
        return EXTERNAL_GROUP;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + EXTERNAL_GROUP;
        result = prime * result + ((emailaddress == null) ? 0 : emailaddress.hashCode());
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof ExternalGroupParticipant)) {
            return false;
        }
        final ExternalGroupParticipant other = (ExternalGroupParticipant) obj;
        if (null != emailaddress && null != other.emailaddress) {
            return emailaddress.equals(other.emailaddress);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final Participant part) {
        final int retval;
        if (EXTERNAL_GROUP == part.getType()) {
            if (null != emailaddress && null != part.getEmailAddress()) {
                retval = emailaddress.compareTo(part.getEmailAddress());
            } else {
                retval = 0;
            }
        } else {
            retval = Integer.compare(EXTERNAL_GROUP, part.getType());
        }
        return retval;
    }

    @Override
    public ExternalGroupParticipant clone() throws CloneNotSupportedException {
        ExternalGroupParticipant retval = (ExternalGroupParticipant) super.clone();

        retval.setDisplayName(this.getDisplayName());
        retval.setEmailAddress(this.getEmailAddress());
        retval.setIdentifier(this.getIdentifier());
        retval.setIgnoreNotification(this.isIgnoreNotification());

        return retval;
    }

    @Override
    public Participant getClone() throws CloneNotSupportedException {
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
