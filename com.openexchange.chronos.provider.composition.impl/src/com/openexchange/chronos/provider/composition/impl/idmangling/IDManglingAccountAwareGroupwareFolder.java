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

package com.openexchange.chronos.provider.composition.impl.idmangling;

import java.util.Date;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.groupware.GroupwareCalendarFolder;
import com.openexchange.chronos.provider.groupware.GroupwareFolderType;

/**
 * {@link IDManglingAccountAwareGroupwareFolder}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class IDManglingAccountAwareGroupwareFolder extends IDManglingAccountAwareFolder implements GroupwareCalendarFolder {

    protected final String newParentId;
    protected final GroupwareCalendarFolder delegate;

    /**
     * Initializes a new {@link IDManglingAccountAwareGroupwareFolder}.
     *
     * @param delegate The calendar folder delegate
     * @param account The underlying calendar account
     * @param newId The new identifier to hide the delegate's one
     * @param newParentId The new parent identifier to hide the delegate's one
     */
    public IDManglingAccountAwareGroupwareFolder(GroupwareCalendarFolder delegate, CalendarAccount account, String newId, String newParentId) {
        super(delegate, account, newId);
        this.delegate = delegate;
        this.newParentId = newParentId;
    }

    @Override
    public String getParentId() {
        return newParentId;
    }

    @Override
    public boolean isDefaultFolder() {
        return delegate.isDefaultFolder();
    }

    @Override
    public int getModifiedBy() {
        return delegate.getModifiedBy();
    }

    @Override
    public int getCreatedBy() {
        return delegate.getCreatedBy();
    }

    @Override
    public Date getCreationDate() {
        return delegate.getCreationDate();
    }

    @Override
    public GroupwareFolderType getType() {
        return delegate.getType();
    }

    @Override
    public String toString() {
        return "IDManglingGroupwareFolder [newId=" + newId + ", newParentId=" + newParentId + ", delegate=" + delegate + "]";
    }
}
