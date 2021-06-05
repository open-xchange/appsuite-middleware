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

package com.openexchange.data.conversion.ical;

import java.io.OutputStream;
import java.util.List;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.osgi.annotation.SingletonService;

/**
 * {@link ICalEmitter}
 *
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
@SingletonService
public interface ICalEmitter {

    /**
     * Creates a new iCal session to use when exporting multiple iCal items.
     *
     * @param mode The operation mode to use.
     * @return A new iCal session
     */
    ICalSession createSession(Mode mode);

    /**
     * Creates a new iCal session for use during export.
     *
     * @return A new iCal session
     */
    ICalSession createSession();

    /**
     * Serializes the iCal session to an output stream.
     *
     * @param session The iCal session to write
     * @param stream The target output stream
     */
    void writeSession(ICalSession session, OutputStream stream) throws ConversionError;

    /**
     * Finishes and flushed the write operation of the iCal session.
     *
     * @param session The iCal session to flush
     * @param stream The target output stream
     */
    void flush(ICalSession session, OutputStream stream) throws ConversionError;

    /**
     * Writes a single task to an iCal session.
     *
     * @param session The underlying iCal session
     * @param task The task to write
     * @param ctx The context
     * @param iTip The associated iTIP container, or <code>null</code> if no iTIP scheduling is involved
     * @param errors A reference to store any conversion errors
     * @param warnings A reference to store any non-fatal conversion warnings
     * @return A reference to the exported iCal item
     */
    ICalItem writeTask(ICalSession session, Task task, Context context, List<ConversionError> errors, List<ConversionWarning> warnings) throws ConversionError;

    /**
     * Serializes one or more tasks directly as iCal file, i.e. not bound to an iCal session.
     *
     * @param tasks The tasks to write
     * @param ctx The context
     * @param errors A reference to store any conversion errors
     * @param warnings A reference to store any non-fatal conversion warnings
     * @return The exported iCal file as string
     */
    String writeTasks(List<Task> tasks, List<ConversionError> errors, List<ConversionWarning> warnings, Context ctx) throws ConversionError;

}
