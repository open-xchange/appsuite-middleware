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

package com.openexchange.importexport.exporters.ical;

import java.io.OutputStream;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.api2.TasksSQLInterface;
import com.openexchange.data.conversion.ical.ConversionError;
import com.openexchange.data.conversion.ical.ConversionWarning;
import com.openexchange.data.conversion.ical.ICalEmitter;
import com.openexchange.data.conversion.ical.ICalSession;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderChildObject;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.groupware.tasks.TasksSQLImpl;
import com.openexchange.importexport.exceptions.ImportExportExceptionCodes;
import com.openexchange.importexport.osgi.ImportExportServices;
import com.openexchange.java.Streams;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link ICalTaskExporter}
 *
 * @author <a href="mailto:Jan-Oliver.Huhn@open-xchange.com">Jan-Oliver Huhn</a>
 * @since v7.10.0
 */
public class ICalTaskExporter extends AbstractICalExporter {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ICalTaskExporter.class);

    public ICalTaskExporter(String folderId, Map<String, List<String>> batchIds, int[] fieldsToBeExported) {
        super(folderId, batchIds);
        this.fieldsToBeExported = fieldsToBeExported;
    }

    private static final Date DATE_ZERO = new Date(0);

    int[] fieldsToBeExported;

    protected final static int[] _taskFields = {
        DataObject.OBJECT_ID,
        DataObject.CREATED_BY,
        DataObject.CREATION_DATE,
        DataObject.LAST_MODIFIED,
        DataObject.MODIFIED_BY,
        FolderChildObject.FOLDER_ID,
        CommonObject.PRIVATE_FLAG,
        CommonObject.CATEGORIES,
        CalendarObject.TITLE,
        CalendarObject.START_DATE,
        CalendarObject.END_DATE,
        CalendarObject.NOTE,
        CalendarObject.RECURRENCE_TYPE,
        CalendarObject.INTERVAL,
        CalendarObject.DAY_IN_MONTH,
        CalendarObject.DAYS,
        CalendarObject.MONTH,
        CalendarObject.PARTICIPANTS,
        Task.UID,
        Task.ACTUAL_COSTS,
        Task.ACTUAL_DURATION,
        Task.ALARM,
        Task.BILLING_INFORMATION,
        Task.CATEGORIES,
        Task.COMPANIES,
        Task.CURRENCY,
        Task.DATE_COMPLETED,
        Task.IN_PROGRESS,
        Task.PERCENT_COMPLETED,
        Task.PRIORITY,
        Task.STATUS,
        Task.TARGET_COSTS,
        Task.TARGET_DURATION,
        Task.TRIP_METER,
        Task.COLOR_LABEL
    };

    @Override
    protected ThresholdFileHolder exportFolderData(ServerSession session, OutputStream out) throws OXException {
        ICalEmitter emitter = ImportExportServices.getICalEmitter();
        List<ConversionError> errors = new LinkedList<>();
        List<ConversionWarning> warnings = new LinkedList<>();
        ICalSession iCalSession = emitter.createSession();
        TasksSQLInterface tasksSql = new TasksSQLImpl(session);
        int[] fields = null != fieldsToBeExported ? fieldsToBeExported : _taskFields;
        SearchIterator<Task> searchIterator = tasksSql.getModifiedTasksInFolder(Integer.parseInt(getFolderId()), fields, DATE_ZERO);
        try {
            while (searchIterator.hasNext()) {
                Task task = searchIterator.next();
                if (null != task) {
                    emitter.writeTask(iCalSession, task, session.getContext(), errors, warnings);
                }
            }
        } finally {
            SearchIterators.close(searchIterator);
        }
        log(errors, warnings);
        if (null != out) {
            emitter.writeSession(iCalSession, out);
            return null;
        }
        ThresholdFileHolder sink = new ThresholdFileHolder();
        boolean error = true;
        try {
            emitter.writeSession(iCalSession, sink.asOutputStream());
            error = false;
            return sink;
        } finally {
            if (error) {
                Streams.close(sink);
            }
        }
    }

    @Override
    protected ThresholdFileHolder exportBatchData(ServerSession session, OutputStream out) throws OXException {
        ICalEmitter emitter = ImportExportServices.getICalEmitter();
        List<ConversionError> errors = new LinkedList<>();
        List<ConversionWarning> warnings = new LinkedList<>();
        ICalSession iCalSession = emitter.createSession();
        TasksSQLInterface tasksSql = new TasksSQLImpl(session);
        if (null != out) {

            try {
                for (Map.Entry<String, List<String>> batchEntry : getBatchIds().entrySet()) {
                    for (String object : batchEntry.getValue()) {
                        emitter.writeTask(iCalSession, tasksSql.getTaskById(Integer.parseInt(object), Integer.parseInt(batchEntry.getKey())), session.getContext(), errors, warnings);
                    }
                }
                emitter.writeSession(iCalSession, out);
            } catch (OXException e) {
                throw ImportExportExceptionCodes.ICAL_CONVERSION_FAILED.create(e);
            }
            log(errors, warnings);
        }
        if (null != out) {
            emitter.writeSession(iCalSession, out);
            return null;
        }
        ThresholdFileHolder sink = new ThresholdFileHolder();
        boolean error = true;
        try {
            for (Map.Entry<String, List<String>> batchEntry : getBatchIds().entrySet()) {
                for (String object : batchEntry.getValue()) {
                    emitter.writeTask(iCalSession, tasksSql.getTaskById(Integer.parseInt(object), Integer.parseInt(batchEntry.getKey())), session.getContext(), errors, warnings);
                }
            }
            log(errors, warnings);
            emitter.writeSession(iCalSession, sink.asOutputStream());
            error = false;
            return sink;
        } finally {
            if (error) {
                Streams.close(sink);
            }
        }
    }

    public static void log(final List<ConversionError> errors, final List<ConversionWarning> warnings) {
        for(final ConversionError error : errors) {
            LOG.warn(error.getMessage());
        }
        for(final ConversionWarning warning : warnings) {
            LOG.warn(warning.getMessage());
        }
    }

}
