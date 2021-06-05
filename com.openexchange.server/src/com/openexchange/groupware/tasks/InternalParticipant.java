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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.Set;
import com.openexchange.groupware.container.UserParticipant;

/**
 * TaskInternalParticipant.
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class InternalParticipant extends TaskParticipant implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 6300127692940891584L;

    /**
     * An empty set of participants for tasks.
     */
    static final Set<InternalParticipant> EMPTY_INTERNAL = Collections.emptySet();

    /**
     * User.
     */
    private final UserParticipant user;

    /**
     * Unique identifier of the group if this participant is added through a
     * group or <code>null</code>.
     */
    private Integer groupId;

    /**
     * Default constructor.
     * 
     * @param user User.
     * @param groupId unique identifier of the group if this participant is
     *            added through a group.
     */
    public InternalParticipant(final UserParticipant user, final Integer groupId) {
        super();
        this.user = user;
        this.groupId = groupId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    Type getType() {
        return Type.INTERNAL;
    }

    /**
     * @return the unique identifier of the user.
     */
    public int getIdentifier() {
        return user.getIdentifier();
    }

    /**
     * @return Returns the groupId.
     */
    Integer getGroupId() {
        return groupId;
    }

    /**
     * @return the private folder of the user.
     */
    int getFolderId() {
        return user.getPersonalFolderId();
    }

    /**
     * Sets the private folder of the user.
     * 
     * @param folderId unique identifier of the folder.
     */
    final void setFolderId(final int folderId) {
        user.setPersonalFolderId(folderId);
    }

    /**
     * @param confirm the confirm to set
     */
    final void setConfirm(final int confirm) {
        user.setConfirm(confirm);
    }

    /**
     * @return the confirm.
     */
    int getConfirm() {
        return user.getConfirm();
    }

    /**
     * @return the confirmation message or <code>null</code> if it is not set.
     */
    public String getConfirmMessage() {
        return user.getConfirmMessage();
    }

    /**
     * Sets the confirm message.
     * 
     * @param confirmMessage new confirm message.
     */
    final void setConfirmMessage(final String confirmMessage) {
        user.setConfirmMessage(confirmMessage);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return getIdentifier();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof InternalParticipant)) {
            return false;
        }
        return getIdentifier() == ((InternalParticipant) obj).getIdentifier();
    }

    /**
     * @return Returns the user.
     */
    UserParticipant getUser() {
        return user;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "TaskParticipant: " + getIdentifier() + ", Group: " + groupId + ", Folder: " + getFolderId();
    }

    /**
     * @param groupId the groupId to set
     */
    void setGroupId(final Integer groupId) {
        this.groupId = groupId;
    }

    public InternalParticipant deepClone() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(this);
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            return (InternalParticipant) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            org.slf4j.LoggerFactory.getLogger(InternalParticipant.class).error("Unable to create a deep clone of the participant with id {} in folder {}. Will return original participant.", this.user.getIdentifier(), this.user.getPersonalFolderId(), e);
            return this;
        }
    }
}
