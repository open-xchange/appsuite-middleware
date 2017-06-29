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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.chronos.storage.rdb.migration;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;

/**
 * {@link MigrationResult}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class MigrationResult {

    private final int contextId;

    private int migratedEventTombstones;
    private int migratedEvents;
    private int migratedAttendees;
    private int migratedAttendeeTombstones;
    private int migratedAlarms;
    private Date start;
    private Date end;
    private Map<String, List<OXException>> errors;

    /**
     * Initializes a new {@link MigrationResult}.
     *
     * @param contextId The context identifier
     */
    public MigrationResult(int contextId) {
        super();
        this.contextId = contextId;
        this.errors = new HashMap<String, List<OXException>>();
    }

    public int getMigratedEventTombstones() {
        return migratedEventTombstones;
    }

    public void setMigratedEventTombstones(int migratedEventTombstones) {
        this.migratedEventTombstones = migratedEventTombstones;
    }

    public int getMigratedEvents() {
        return migratedEvents;
    }

    public void setMigratedEvents(int migratedEvents) {
        this.migratedEvents = migratedEvents;
    }

    public int getMigratedAttendees() {
        return migratedAttendees;
    }

    public void setMigratedAttendees(int migratedAttendees) {
        this.migratedAttendees = migratedAttendees;
    }

    public int getMigratedAttendeeTombstones() {
        return migratedAttendeeTombstones;
    }

    public void setMigratedAttendeeTombstones(int migratedAttendeeTombstones) {
        this.migratedAttendeeTombstones = migratedAttendeeTombstones;
    }

    public int getMigratedAlarms() {
        return migratedAlarms;
    }

    public void setMigratedAlarms(int migratedAlarms) {
        this.migratedAlarms = migratedAlarms;
    }

    public int getContextId() {
        return contextId;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public Map<String, List<OXException>> getErrors() {
        return errors;
    }

    public void addErrors(Map<String, List<OXException>> errors) {
        this.errors.putAll(errors);
    }

    @Override
    public String toString() {
        return "MigrationResult [contextId=" + contextId + ", start=" + start + ", end=" + end + ", migratedEvents=" + migratedEvents + ", migratedAttendees=" + migratedAttendees + ", migratedAlarms=" + migratedAlarms + ", migratedEventTombstones=" + migratedEventTombstones + ", migratedAttendeeTombstones=" + migratedAttendeeTombstones + ", errors=" + errors + "]";
    }

}

