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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.calendar.storage;

import java.sql.Connection;
import java.util.List;
import com.openexchange.calendar.ParticipantLogic;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.calendar.OXCalendarException;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.contexts.Context;

/**
 * Interface to the storage of external participants.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public abstract class ParticipantStorage {

    private static final ParticipantStorage SINGLETON = new RdbParticipantStorage();

    protected ParticipantStorage() {
        super();
    }

    public static ParticipantStorage getInstance() {
        return SINGLETON;
    }

    /**
     * Stores external participants and only external in the new calendar tables for external participants.
     * @param ctx Context.
     * @param con writable database connection. It should already be inside a transaction.
     * @param appointmentId unique identifier of the corresponding appointment.
     * @param participants participants. Only the {@link ExternalUserParticipant ExternalUserParticipants} while be stored.
     * @throws OXCalendarException if some problem occurs.
     */
    public abstract void insertParticipants(Context ctx, Connection con, int appointmentId, Participant[] participants) throws OXCalendarException;

    public abstract ExternalUserParticipant[] selectExternal(Context ctx, Connection con, int appointmentId) throws OXCalendarException;

    public void selectExternal(Context ctx, Connection con, List<CalendarDataObject> cdaos, int[] ids) throws OXCalendarException {
        for (int id : ids) {
            CalendarDataObject cdao = null;
            for (CalendarDataObject test : cdaos) {
                if (id == test.getObjectID()) {
                    cdao = test;
                    break;
                }
            }
            if (null == cdao) {
                continue;
            }
            ExternalUserParticipant[] externals = selectExternal(ctx, con, cdao.getObjectID());
            cdao.setParticipants(ParticipantLogic.mergeFallback(cdao.getParticipants(), externals));
            cdao.setConfirmations(ParticipantLogic.mergeConfirmations(externals, cdao.getParticipants()));
        }
    }
}
