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

package com.openexchange.groupware.datahandler;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.fields.FolderChildFields;
import com.openexchange.api2.TasksSQLInterface;
import com.openexchange.conversion.DataExceptionCodes;
import com.openexchange.conversion.DataHandler;
import com.openexchange.data.conversion.ical.ConversionError;
import com.openexchange.data.conversion.ical.ConversionWarning;
import com.openexchange.data.conversion.ical.ICalParser;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.groupware.tasks.TasksSQLImpl;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;

/**
 * {@link ICalDataHandler}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public abstract class ICalDataHandler implements DataHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ICalDataHandler.class);

    private static final int LIMIT = 1048576;

    protected final ServiceLookup services;

    /**
     * Initializes a new {@link ICalDataHandler}.
     *
     * @param services The {@link ServiceLookup}
     *
     */
    public ICalDataHandler(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public String[] getRequiredArguments() {
        return new String[0];
    }

    protected void insertTasks(final Session session, final int taskFolder, final List<Task> tasks, final JSONArray folderAndIdArray) throws OXException, JSONException {
        final TasksSQLInterface taskSql = new TasksSQLImpl(session);
        for (final Task task : tasks) {
            task.setParentFolderID(taskFolder);
            taskSql.insertTaskObject(task);
            folderAndIdArray.put(new JSONObject().put(FolderChildFields.FOLDER_ID, taskFolder).put(DataFields.ID, task.getObjectID()));
        }
    }

    protected List<Task> parseTaskStream(final Context ctx, final ICalParser iCalParser, final InputStreamCopy inputStreamCopy, final List<ConversionError> conversionErrors, final List<ConversionWarning> conversionWarnings, final TimeZone defaultZone) throws IOException, ConversionError {
        final InputStream inputStream = inputStreamCopy.getInputStream();
        try {
            return iCalParser.parseTasks(inputStream, defaultZone, ctx, conversionErrors, conversionWarnings).getImportedObjects();
        } finally {
            safeClose(inputStream);
        }
    }

    protected static InputStreamCopy copyStream(final InputStream orig, final long size) throws OXException {
        try {
            return new InputStreamCopy(orig, "openexchange-ical-", (size <= 0 || size > LIMIT));
        } catch (IOException e) {
            throw DataExceptionCodes.ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Closes a {@link Closeable} save. In case of an error while closing, the error will be logged instead of being thrown
     * so that possible earlier errors won't be 'overriden'
     *
     * @param closeable The {@link Closeable}
     */
    protected static void safeClose(Closeable closeable) {
        if (null != closeable) {
            try {
                closeable.close();
            } catch (Exception e) {
                LOGGER.debug("Error while closing.", e);
            }
        }
    }

    /**
     * Container Class for Confirm status.
     * {@link Confirm}
     *
     * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
     */
    protected static class Confirm {

        private final int confirm;
        private final String confirmMessage;

        public Confirm(final int confirm, final String confirmMessage) {
            this.confirm = confirm;
            this.confirmMessage = confirmMessage;
        }

        public int getConfirm() {
            return confirm;
        }

        public String getConfirmMessage() {
            return confirmMessage;
        }
    }

}
