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

package com.openexchange.groupware.tasks;

import java.util.Collections;
import java.util.Set;
import com.openexchange.groupware.container.UserParticipant;

/**
 * TaskInternalParticipant.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class InternalParticipant extends TaskParticipant {

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
     * @param user User.
     * @param groupId unique identifier of the group if this participant is
     * added through a group.
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
        return getIdentifier() == ((InternalParticipant) obj)
            .getIdentifier();
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
        return "TaskParticipant: " + getIdentifier() + ", Group: " + groupId
            + ", Folder: " + getFolderId();
    }

    /**
     * @param groupId the groupId to set
     */
    void setGroupId(final Integer groupId) {
        this.groupId = groupId;
    }
}
