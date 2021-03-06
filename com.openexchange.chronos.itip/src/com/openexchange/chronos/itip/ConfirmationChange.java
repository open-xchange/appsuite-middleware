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

package com.openexchange.chronos.itip;

import com.openexchange.chronos.ParticipationStatus;

/**
 * 
 * {@link ConfirmationChange}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.0
 */
public class ConfirmationChange {

    private ParticipationStatus oldStatus = null;

    private ParticipationStatus newStatus = null;

    private String oldMessage;

    private String newMessage;

    private final String identifier;

    public ConfirmationChange(String identifier) {
        this.identifier = identifier;
    }

    public void setStatus(ParticipationStatus oldStatus, ParticipationStatus newStatus) {
        setOldStatus(oldStatus);
        setNewStatus(newStatus);
    }

    public void setMessage(String oldMessage, String newMessage) {
        setOldMessage(oldMessage);
        setNewMessage(newMessage);
    }

    public ParticipationStatus getOldStatus() {
        return oldStatus;
    }

    public void setOldStatus(ParticipationStatus oldStatus) {
        this.oldStatus = oldStatus;
    }

    public ParticipationStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(ParticipationStatus newStatus) {
        this.newStatus = newStatus;
    }

    public String getOldMessage() {
        return oldMessage;
    }

    public void setOldMessage(String oldMessage) {
        this.oldMessage = oldMessage;
    }

    public String getNewMessage() {
        return newMessage;
    }

    public void setNewMessage(String newMessage) {
        this.newMessage = newMessage;
    }

    public String getIdentifier() {
        return identifier;
    }

}
