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

package com.openexchange.passwordchange.history.tracker;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.openexchange.passwordchange.history.tracker.PasswordChangeInfo;
import com.openexchange.passwordchange.history.tracker.PasswordChangeTracker;
import com.openexchange.passwordchange.history.tracker.RuntimeTracker;
import com.openexchange.session.Session;

/**
 * {@link RuntimeTracker} - Does not save anything to a DB. Dies with server shutdown. ~DummyTracker for testing
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public class RuntimeTracker implements PasswordChangeTracker {

    Map<Session, PasswordChangeInfo> infos;

    /**
     * Initializes a new {@link RuntimeTracker}.
     */
    public RuntimeTracker() {
        super();
        infos = new HashMap<>();
    }

    @Override
    public List<PasswordChangeInfo> listPasswordChanges(Session session) {
        List<PasswordChangeInfo> lst = new LinkedList<>();
        for (Session ses : infos.keySet()) {
            // Check if same user and context
            if (sameSession(session, ses)) {
                lst.add(infos.get(ses));
            }
        }
        return lst;
    }

    @Override
    public void trackPasswordChange(Session session, PasswordChangeInfo info) {
        if (null != session && null != info) {
            infos.put(session, info);
        }
    }

    @Override
    public void clear(Session session, int limit) {
        if (limit <= 0) {
            // Remove all
            for (Session ses : infos.keySet()) {
                if (sameSession(session, ses)) {
                    infos.remove(ses);
                }
            }
        } else {
            // Gather and delete only oldest
            List<Session> check = new LinkedList<>();
            for (Session ses : infos.keySet()) {
                if (sameSession(session, ses)) {
                    check.add(ses);
                }
            }
            if (check.size() > limit) {
                //TODO Remove oldest with some fancy algorithm
            }
        }
    }

    private boolean sameSession(Session one, Session two) {
        return one.getUserId() == two.getUserId() && one.getContextId() == two.getContextId();
    }
}
