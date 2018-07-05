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

package com.openexchange.importexport.importers.ical;

import static com.openexchange.java.Autoboxing.I;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import com.openexchange.api2.TasksSQLInterface;
import com.openexchange.data.conversion.ical.ConversionError;
import com.openexchange.data.conversion.ical.ConversionWarning;
import com.openexchange.data.conversion.ical.ICalParser;
import com.openexchange.data.conversion.ical.ParseResult;
import com.openexchange.data.conversion.ical.TruncationInfo;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXException.Generic;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.importexport.ImportResult;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.groupware.tasks.TasksSQLImpl;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.importexport.exceptions.ImportExportExceptionCodes;
import com.openexchange.importexport.osgi.ImportExportServices;
import com.openexchange.tools.TimeZoneUtils;
import com.openexchange.tools.session.ServerSession;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TObjectProcedure;


/**
 * {@link ICalTaskImporter}
 *
 * @author <a href="mailto:Jan-Oliver.Huhn@open-xchange.com">Jan-Oliver Huhn</a>
 * @since v7.10.0
 */
public class ICalTaskImporter extends AbstractICalImporter {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ICalTaskImporter.class);

    public ICalTaskImporter(ServerSession session, UserizedFolder userizedFolder) {
        super(session, userizedFolder);
    }

    @Override
    public TruncationInfo importData(InputStream is, List<ImportResult> list, Map<String, String[]> optionalParams) throws OXException {
        final List<ConversionError> errors = new ArrayList<>();
        final List<ConversionWarning> warnings = new ArrayList<>();
        final TasksSQLInterface taskInterface = retrieveTaskInterface(Integer.parseInt(getUserizedFolder().getID()), getSession());
        final ICalParser parser = ImportExportServices.getIcalParser();
        final Context ctx = getSession().getContext();
        final TimeZone defaultTz = TimeZoneUtils.getTimeZone(UserStorage.getInstance().getUser(getSession().getUserId(), ctx).getTimeZone());
        return importTask(is, optionalParams, Integer.parseInt(getUserizedFolder().getID()), taskInterface, parser, ctx, defaultTz, list, errors, warnings);
    }

    private TruncationInfo importTask(final InputStream is, final Map<String, String[]> optionalParams, final int taskFolderId,
            final TasksSQLInterface taskInterface, final ICalParser parser,
            final Context ctx, final TimeZone defaultTz,
            final List<ImportResult> list, final List<ConversionError> errors,
            final List<ConversionWarning> warnings)
            throws OXException {
        ParseResult<Task> tasks = parser.parseTasks(is, defaultTz, ctx, errors, warnings);
        TruncationInfo truncationInfo = tasks.getTruncationInfo();
        final TIntObjectMap<ConversionError> errorMap = new TIntObjectHashMap<>();

        for (final ConversionError error : errors) {
            errorMap.put(error.getIndex(), error);
        }

        final TIntObjectMap<List<ConversionWarning>> warningMap = new TIntObjectHashMap<>();

        for (final ConversionWarning warning : warnings) {
            List<ConversionWarning> warningList = warningMap.get(warning
                    .getIndex());
            if (warningList == null) {
                warningList = new LinkedList<>();
                warningMap.put(warning.getIndex(), warningList);
            }
            warningList.add(warning);
        }

        boolean ignoreUIDs = isIgnoreUIDs(optionalParams);
        Map<String, String> uidReplacements = ignoreUIDs ? new HashMap<>() : null;
        int index = 0;
        final Iterator<Task> iter = tasks.getImportedObjects().iterator();
        while (iter.hasNext()) {
            final ImportResult importResult = new ImportResult();
            final ConversionError error = errorMap.get(index);
            if (error != null) {
                errorMap.remove(index);
                importResult.setException(error);
            } else {
                // IGNORE WARNINGS. Protocol doesn't allow for warnings.
                // TODO: Verify This
                final Task task = iter.next();
                task.setParentFolderID(taskFolderId);
                if (ignoreUIDs && task.containsUid()) {
                    // perform fixed UID replacement to keep recurring task relations
                    String originalUID = task.getUid();
                    String replacedUID = uidReplacements.get(originalUID);
                    if (null == replacedUID) {
                        replacedUID = UUID.randomUUID().toString();
                        uidReplacements.put(originalUID, replacedUID);
                    }
                    task.setUid(replacedUID);
                }
                try {
                    taskInterface.insertTaskObject(task);
                    importResult.setObjectId(String.valueOf(task
                            .getObjectID()));
                    importResult.setDate(task.getLastModified());
                    importResult.setFolder(String.valueOf(taskFolderId));
                } catch (final OXException e) {
                    LOG.error("", e);
                    importResult.setException(e);
                }

                final List<ConversionWarning> warningList = warningMap
                        .get(index);
                if (warningList != null) {
                    importResult.addWarnings(warningList);
                    importResult
                            .setException(ImportExportExceptionCodes.WARNINGS
                                    .create(I(warningList.size())));
                }
            }
            importResult.setEntryNumber(index);
            list.add(importResult);
            index++;
        }
        if (!errorMap.isEmpty()) {
            errorMap.forEachValue(new TObjectProcedure<ConversionError>() {

                @Override
                public boolean execute(final ConversionError error) {
                    final ImportResult importResult = new ImportResult();
                    importResult.setEntryNumber(error.getIndex());
                    importResult.setException(error);
                    list.add(importResult);
                    return true;
                }
            });
        }
        return truncationInfo;
    }

    private TasksSQLInterface retrieveTaskInterface(final int taskFolderId,
            final ServerSession session) throws OXException {
        if (taskFolderId == -1) {
            return null;
        }
        if (!UserConfigurationStorage
                .getInstance()
                .getUserConfigurationSafe(session.getUserId(),
                        session.getContext()).hasTask()) {
            throw ImportExportExceptionCodes.TASKS_DISABLED
                    .create().setGeneric(Generic.NO_PERMISSION);
        }
        return new TasksSQLImpl(session);
    }

}
