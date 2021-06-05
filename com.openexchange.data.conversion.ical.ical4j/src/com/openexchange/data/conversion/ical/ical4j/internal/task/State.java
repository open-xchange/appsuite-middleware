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

package com.openexchange.data.conversion.ical.ical4j.internal.task;

import java.util.List;
import java.util.TimeZone;
import com.openexchange.data.conversion.ical.ConversionWarning;
import com.openexchange.data.conversion.ical.Mode;
import com.openexchange.data.conversion.ical.ical4j.internal.AbstractVerifyingAttributeConverter;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.tasks.Task;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.ParameterList;
import net.fortuna.ical4j.model.component.VToDo;
import net.fortuna.ical4j.model.parameter.XParameter;
import net.fortuna.ical4j.model.property.Status;

/**
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class State extends AbstractVerifyingAttributeConverter<VToDo, Task> {

	private final static String OX_STATUS = "X-OX-STATUS";
	private final static String OX_STATUS_VALUE = "WAITING";

	private final static String STATUS_NEEDS_ACTION = "NEEDS-ACTION";
	private final static String STATUS_IN_PROCESS = "IN-PROCESS";
	private final static String STATUS_COMPLETED = "COMPLETED";
	private final static String STATUS_CANCELLED = "CANCELLED";

    public State() {
        super();
    }

    @Override
    public void emit(final Mode mode, final int index, final Task task, final VToDo vtodo, final List<ConversionWarning> warnings, final Context ctx, final Object... args) {
        try {
            final Status status = toStatus(index, task.getStatus());
            vtodo.getProperties().add(status);
        } catch (ConversionWarning e) {
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
        } catch (ConversionWarning e) {
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
        	ParameterList parameterList = new ParameterList(false);
        	parameterList.add(new XParameter(OX_STATUS, OX_STATUS_VALUE));
        	retval = new Status(parameterList, STATUS_CANCELLED);
            break;
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
        Parameter parameter = status.getParameter(OX_STATUS);
        switch (status.getValue()) {
		case STATUS_NEEDS_ACTION:
            retval = Task.NOT_STARTED;
			break;
		case STATUS_IN_PROCESS:
            retval = Task.IN_PROGRESS;
			break;
		case STATUS_COMPLETED:
			retval = Task.DONE;
			break;
		case STATUS_CANCELLED:
			if (null != parameter && parameter.getValue().equals(OX_STATUS_VALUE)) {
				retval = Task.WAITING;
				break;
			}
			retval = Task.DEFERRED;
			break;
		default:
			throw new ConversionWarning(index, ConversionWarning.Code.INVALID_STATUS, status.getValue());
		}
        return retval;
    }
}
