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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.openexchange.chronos.service.CalendarResult;
import com.openexchange.chronos.service.CreateResult;
import com.openexchange.chronos.service.DeleteResult;
import com.openexchange.chronos.service.UpdateResult;
import com.openexchange.session.Session;

/**
 * {@link IDManglingCalendarResult}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class IDManglingCalendarResult implements CalendarResult {

    protected final CalendarResult delegate;
    protected final int accountId;

    /**
     * Initializes a new {@link IDManglingCalendarResult}.
     *
     * @param delegate The result delegate
     * @param accountId The identifier of the calendar account the result originates in
     */
    public IDManglingCalendarResult(CalendarResult delegate, int accountId) {
        super();
        this.delegate = delegate;
        this.accountId = accountId;
    }

    @Override
    public Session getSession() {
        return delegate.getSession();
    }

    @Override
    public int getCalendarUser() {
        return delegate.getCalendarUser();
    }

    @Override
    public long getTimestamp() {
        return delegate.getTimestamp();
    }

    @Override
    public String getFolderID() {
        return IDMangling.getUniqueFolderId(accountId, delegate.getFolderID());
    }

    @Override
    public List<DeleteResult> getDeletions() {
        List<DeleteResult> deletions = delegate.getDeletions();
        if (null == deletions) {
            return Collections.emptyList();
        }
        List<DeleteResult> idManglingDeletions = new ArrayList<DeleteResult>(deletions.size());
        for (DeleteResult deletion : deletions) {
            idManglingDeletions.add(new IDManglingDeleteResult(deletion, accountId));
        }
        return idManglingDeletions;
    }

    @Override
    public List<UpdateResult> getUpdates() {
        List<UpdateResult> updates = delegate.getUpdates();
        if (null == updates) {
            return Collections.emptyList();
        }
        List<UpdateResult> idManglingUpdates = new ArrayList<UpdateResult>(updates.size());
        for (UpdateResult update : updates) {
            idManglingUpdates.add(new IDManglingUpdateResult(update, accountId));
        }
        return idManglingUpdates;
    }

    @Override
    public List<CreateResult> getCreations() {
        List<CreateResult> creations = delegate.getCreations();
        if (null == creations) {
            return Collections.emptyList();
        }
        List<CreateResult> idManglingCreations = new ArrayList<CreateResult>(creations.size());
        for (CreateResult creation : creations) {
            idManglingCreations.add(new IDManglingCreateResult(creation, accountId));
        }
        return idManglingCreations;
    }

    @Override
    public String toString() {
        return "IDManglingCalendarResult [accountId=" + accountId + ", delegate=" + delegate + "]";
    }

}
