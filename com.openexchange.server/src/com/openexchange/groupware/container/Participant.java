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

package com.openexchange.groupware.container;

import java.io.Serializable;

/**
 * {@link Participant} - Represents a participant of either a group appointment or group task.
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */
public interface Participant extends Serializable, Cloneable {

    static final int USER = 1;

    static final int GROUP = 2;

    static final int RESOURCE = 3;

    static final int RESOURCEGROUP = 4;

    static final int EXTERNAL_USER = 5;

    static final int EXTERNAL_GROUP = 6;

    static final int NO_ID = -1;

    /**
     * @deprecated Use explicit constructor. {@link UserParticipant#UserParticipant(int)}, {@link GroupParticipant#GroupParticipant(int)},
     *             {@link ResourceParticipant#ResourceParticipant(int)}, {@link ResourceGroupParticipant#ResourceGroupParticipant(int)}
     */
    @Deprecated
    void setIdentifier(final int id);

    /**
     * Gets this participant's identifier.
     *
     * @return This participant's identifier
     */
    int getIdentifier();

    /**
     * Sets this participant's display name.
     *
     * @param displayName The display name to set
     */
    void setDisplayName(final String displayName);

    /**
     * Gets this participant's display name.
     *
     * @return This participant's display name
     */
    String getDisplayName();

    /**
     * Gets this participant's email address.
     *
     * @return This participant's email address.
     */
    String getEmailAddress();

    /**
     * Gets this participant's type.
     *
     * @return This participant's type; either {@link #USER}, {@link #GROUP}, {@link #RESOURCE}, {@link #RESOURCEGROUP},
     *         {@link #EXTERNAL_USER} , or {@link #EXTERNAL_GROUP}
     */
    int getType();

    /**
     * Checks if notification for this participant shall be ignored.<br>
     * Default is <code>false</code>.
     *
     * @return <code>true</code> if notification for this participant shall be ignored; otherwise <code>false</code>
     */
    boolean isIgnoreNotification();

    /**
     * Sets whether notification for this participant are discarded.
     *
     * @param ignoreNotification <code>true</code> to ignore any notification for this participant; otherwise <code>false</code>
     */
    void setIgnoreNotification(boolean ignoreNotification);

    /**
     * Should delegate to {@link java.lang.Object#clone()}
     *
     * @return The clone
     * @throws CloneNotSupportedException If {@link Cloneable} interface is not implemented
     */
    Participant getClone() throws CloneNotSupportedException;
}
