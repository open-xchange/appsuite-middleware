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

package com.openexchange.data.conversion.ical.ical4j.internal.task;

import java.util.List;
import java.util.TimeZone;
import net.fortuna.ical4j.model.component.VToDo;
import net.fortuna.ical4j.model.property.Status;
import com.openexchange.data.conversion.ical.ConversionWarning;
import com.openexchange.data.conversion.ical.Mode;
import com.openexchange.data.conversion.ical.ical4j.internal.AbstractVerifyingAttributeConverter;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.tasks.Task;

/**
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class State extends AbstractVerifyingAttributeConverter<VToDo, Task> {

    public State() {
        super();
    }

    @Override
    public void emit(final Mode mode, final int index, final Task task, final VToDo vtodo, final List<ConversionWarning> warnings, final Context ctx, final Object... args) {
        try {
            final Status status = new Status(toStatus(index, task.getStatus()).getValue());
            vtodo.getProperties().add(status);
        } catch (final ConversionWarning e) {
            warnings.add(e);
        }
    }

    @Override
    public boolean hasProperty(final VToDo vtodo) {
        return null != vtodo.getStatus();
    }

    @Override
    public boolean isSet(final Task task) {
        return task.containsStatus();
    }

    @Override
    public void parse(final int index, final VToDo vtodo, final Task task, final TimeZone timeZone, final Context ctx, final List<ConversionWarning> warnings) {
        try {
            task.setStatus(toTask(index, vtodo.getStatus()));
        } catch (final ConversionWarning e) {
            warnings.add(e);
        }
    }

    public static Status toStatus(final int index, final int taskState) throws ConversionWarning {
        Status retval = null;
        switch (taskState) {
        case Task.NOT_STARTED:
            retval = Status.VTODO_NEEDS_ACTION;
            break;
        case Task.IN_PROGRESS:
            retval = Status.VTODO_IN_PROCESS;
            break;
        case Task.DONE:
            retval = Status.VTODO_COMPLETED;
            break;
        case Task.WAITING:
        case Task.DEFERRED:
            retval = Status.VTODO_CANCELLED;
            break;
        default:
            throw new ConversionWarning(index, ConversionWarning.Code.INVALID_STATUS, Integer.valueOf(taskState));
        }
        return retval;
    }

    public static int toTask(final int index, final Status status) throws ConversionWarning {
        int retval;
        if (Status.VTODO_NEEDS_ACTION.equals(status)) {
            retval = Task.NOT_STARTED;
        } else if (Status.VTODO_IN_PROCESS.equals(status)) {
            retval = Task.IN_PROGRESS;
        } else if (Status.VTODO_COMPLETED.equals(status)) {
            retval = Task.DONE;
        } else if (Status.VTODO_CANCELLED.equals(status)) {
            retval = Task.DEFERRED;
        } else {
            throw new ConversionWarning(index, ConversionWarning.Code.INVALID_STATUS, status.getValue());
        }
        return retval;
    }
}
