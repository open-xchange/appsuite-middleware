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

import com.openexchange.resource.Resource;

/**
 * {@link ResourceParticipant} - Represents a resource participant.
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */
public class ResourceParticipant implements Participant, Comparable<Participant> {

    private static final long serialVersionUID = 4133897083017380091L;

    private int id = NO_ID;

    private String displayName;

    private String emailaddress;

    private boolean ignoreNotification;

    /**
     * Default constructor.
     *
     * @param id unique identifier of the resource.
     */
    public ResourceParticipant(final int id) {
        super();
        this.id = id;
    }

    /**
     * Constructor that takes values of from a given resource
     *
     * @param res - a resource to be used as base
     */
    public ResourceParticipant(Resource res){
        this(res.getIdentifier());
        setDisplayName(res.getDisplayName());
        setEmailAddress(res.getMail());
    }

    /**
     * @deprecated Use {@link #ResourceParticipant(int)}.
     */
    @Deprecated
    public ResourceParticipant() {
        this(0);
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
        return RESOURCE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + RESOURCE;
        result = prime * result + id;
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof ResourceParticipant)) {
            return false;
        }
        final ResourceParticipant other = (ResourceParticipant) obj;
        return id == other.id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final Participant part) {
        final int retval;
        if (RESOURCE == part.getType()) {
            retval = Integer.compare(id, part.getIdentifier());
        } else {
            retval = Integer.compare(RESOURCE, part.getType());
        }
        return retval;
    }

    @Override
    public ResourceParticipant clone() throws CloneNotSupportedException {
        ResourceParticipant retval = (ResourceParticipant) super.clone();

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
