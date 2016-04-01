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

package com.openexchange.data.conversion.ical.ical4j.internal;

import java.util.ArrayList;
import java.util.List;
import net.fortuna.ical4j.model.component.VToDo;
import com.openexchange.data.conversion.ical.ical4j.internal.calendar.Alarm;
import com.openexchange.data.conversion.ical.ical4j.internal.calendar.Attach;
import com.openexchange.data.conversion.ical.ical4j.internal.calendar.Categories;
import com.openexchange.data.conversion.ical.ical4j.internal.calendar.CreatedAndDTStamp;
import com.openexchange.data.conversion.ical.ical4j.internal.calendar.Duration;
import com.openexchange.data.conversion.ical.ical4j.internal.calendar.Klass;
import com.openexchange.data.conversion.ical.ical4j.internal.calendar.LastModified;
import com.openexchange.data.conversion.ical.ical4j.internal.calendar.Note;
import com.openexchange.data.conversion.ical.ical4j.internal.calendar.Participants;
import com.openexchange.data.conversion.ical.ical4j.internal.calendar.Recurrence;
import com.openexchange.data.conversion.ical.ical4j.internal.calendar.Start;
import com.openexchange.data.conversion.ical.ical4j.internal.calendar.Title;
import com.openexchange.data.conversion.ical.ical4j.internal.calendar.Uid;
import com.openexchange.data.conversion.ical.ical4j.internal.task.DateCompleted;
import com.openexchange.data.conversion.ical.ical4j.internal.task.DueDate;
import com.openexchange.data.conversion.ical.ical4j.internal.task.PercentComplete;
import com.openexchange.data.conversion.ical.ical4j.internal.task.Priority;
import com.openexchange.data.conversion.ical.ical4j.internal.task.State;
import com.openexchange.groupware.tasks.Task;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class TaskConverters {

    public static final AttributeConverter<VToDo, Task>[] ALL;

    /**
     * Prevent instantiation.
     */
    private TaskConverters() {
        super();
    }

    static {
        final List<AttributeConverter<VToDo, Task>> tmp = new ArrayList<AttributeConverter<VToDo, Task>>();
        tmp.add(new Title<VToDo, Task>());
        tmp.add(new Note<VToDo, Task>());
        tmp.add(new Start<VToDo, Task>());
        tmp.add(new Duration<VToDo, Task>());
        tmp.add(new DueDate());
        tmp.add(new Klass<VToDo, Task>());
        tmp.add(new DateCompleted());
        tmp.add(new Participants<VToDo, Task>());
        tmp.add(new Categories<VToDo, Task>());
        tmp.add(new Recurrence<VToDo, Task>());
        tmp.add(new Alarm<VToDo, Task>());
        tmp.add(new State());
        tmp.add(new PercentComplete());
        tmp.add(new Priority());
        tmp.add(new Uid<VToDo, Task>());
        tmp.add(new CreatedAndDTStamp<VToDo, Task>());
        tmp.add(new LastModified<VToDo, Task>());
        tmp.add(new Attach<VToDo, Task>());
        ALL = tmp.toArray(new AttributeConverter[tmp.size()]);
    }
}
