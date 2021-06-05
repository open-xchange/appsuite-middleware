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

package com.openexchange.groupware.tasks;

import com.openexchange.groupware.container.ExternalUserParticipant;

/**
 * TaskExternalParticipant.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class ExternalParticipant extends TaskParticipant {

    private final ExternalUserParticipant external;

    public ExternalParticipant(final ExternalUserParticipant external) {
        super();
        this.external = external;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    Type getType() {
        return Type.EXTERNAL;
    }

    /**
     * @return the external
     */
    public ExternalUserParticipant getExternal() {
        return external;
    }

    public String getMail() {
        return external.getEmailAddress();
    }

    public String getDisplayName() {
        return external.getDisplayName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof ExternalParticipant)) {
            return false;
        }
        final ExternalParticipant other = (ExternalParticipant) obj;
        return (null == getMail() && null == other.getMail())
            || getMail().equals(other.getMail());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return null == getMail() ? super.hashCode() : getMail().hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("ExternalParticipant,mail:");
        sb.append(getMail());
        return sb.toString();
    }
}
