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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.groupware.update;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import com.openexchange.configuration.SystemConfig;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.server.Initialization;

/**
 * {@link UpdateTaskCollectionInit}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UpdateTaskCollectionInit implements Initialization {

    private static final UpdateTaskCollectionInit instance = new UpdateTaskCollectionInit();

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(UpdateTaskCollectionInit.class);

    private static final String PROPERTYNAME = "UPDATETASKSCFG";

    /**
     * Gets the {@link UpdateTaskCollectionInit} instance.
     * 
     * @return The {@link UpdateTaskCollectionInit} instance
     */
    public static UpdateTaskCollectionInit getInstance() {
        return instance;
    }

    private final AtomicBoolean started;

    /**
     * Initializes a new {@link UpdateTaskCollectionInit}.
     */
    private UpdateTaskCollectionInit() {
        super();
        started = new AtomicBoolean();
    }

    private List<UpdateTask> getStaticUpdateTasks() {
        final String propStr;
        if ((propStr = SystemConfig.getProperty(PROPERTYNAME)) == null) {
            // Property not found in system.properties
            // LOG.error("Missing property 'UPDATETASKSCFG' in system.properties");
            return null;
        }
        final File updateTasksFile = new File(propStr);
        if (!updateTasksFile.exists() || !updateTasksFile.isFile()) {
            // File not found
            // LOG.error("Missing file " + propStr);
            return null;
        }
        final List<UpdateTask> updateTaskList = new ArrayList<UpdateTask>();
        BufferedReader reader = null;
        try {
            final Class<?>[] parameterTypes = new Class<?>[0];
            final Object[] initArgs = new Object[0];
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(updateTasksFile)));
            String line = null;
            while ((line = reader.readLine()) != null) {
                final String l = line.trim();
                if ((l.length() == 0) || (l.charAt(0) == '#')) {
                    continue;
                }
                try {
                    updateTaskList.add(Class.forName(l).asSubclass(UpdateTask.class).getConstructor(parameterTypes).newInstance(initArgs));
                } catch (final ClassNotFoundException e) {
                    LOG.error(e.getMessage(), e);
                    continue;
                } catch (final IllegalArgumentException e) {
                    LOG.error(e.getMessage(), e);
                    continue;
                } catch (final SecurityException e) {
                    LOG.error(e.getMessage(), e);
                    continue;
                } catch (final InstantiationException e) {
                    LOG.error(e.getMessage(), e);
                    continue;
                } catch (final IllegalAccessException e) {
                    LOG.error(e.getMessage(), e);
                    continue;
                } catch (final InvocationTargetException e) {
                    LOG.error(e.getMessage(), e);
                    continue;
                } catch (final NoSuchMethodException e) {
                    LOG.error(e.getMessage(), e);
                    continue;
                }
            }
            return updateTaskList;
        } catch (final FileNotFoundException e) {
            LOG.error(e.getMessage(), e);
            return null;
        } catch (final IOException e) {
            LOG.error(e.getMessage(), e);
            return null;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        }
    }

    public void start() throws AbstractOXException {
        if (!started.compareAndSet(false, true)) {
            LOG.error("UpdateTaskCollection has already been started", new Throwable());
        }
        // Get static update tasks from configuration file
        final List<UpdateTask> staticTasks = getStaticUpdateTasks();

        UpdateTaskCollection.initialize(staticTasks);
        UpdateTaskRegistry.initInstance();

        // Fill static update tasks programmatically if retrieval from configuration file returned null

        if (null == staticTasks) {
            // Version 1
            final UpdateTaskRegistry registry = UpdateTaskRegistry.getInstance();
            registry.addUpdateTask(new com.openexchange.groupware.update.tasks.CreateTableVersion());
            registry.addUpdateTask(new com.openexchange.groupware.update.tasks.SpamUpdateTask());
            registry.addUpdateTask(new com.openexchange.groupware.update.tasks.PasswordMechUpdateTask());
            // Version 2
            registry.addUpdateTask(new com.openexchange.groupware.calendar.update.AlterMailAddressLength());
            registry.addUpdateTask(new com.openexchange.groupware.update.tasks.TaskModifiedByNotNull());
            // Version 4
            registry.addUpdateTask(new com.openexchange.groupware.update.tasks.DelFolderTreeTableUpdateTask());
            // Version 5
            registry.addUpdateTask(new com.openexchange.groupware.update.tasks.UnboundFolderReplacementUpdateTask());
            // Version 6
            registry.addUpdateTask(new com.openexchange.groupware.calendar.update.AlterCreatingDate());
            registry.addUpdateTask(new com.openexchange.groupware.update.tasks.TaskReminderFolderZero());
            registry.addUpdateTask(new com.openexchange.groupware.update.tasks.MailUploadQuotaUpdateTask());
            // Version 7
            registry.addUpdateTask(new com.openexchange.groupware.update.tasks.NewAdminExtensionsUpdateTask());
            // Version 8
            registry.addUpdateTask(new com.openexchange.groupware.update.tasks.InfostoreRenamePersonalInfostoreFolders());
            // Version 10
            registry.addUpdateTask(new com.openexchange.groupware.calendar.update.UpdateFolderIdInReminder());
            // Version 11
            registry.addUpdateTask(new com.openexchange.groupware.update.tasks.ClearLeftoverAttachmentsUpdateTask());
            // Version 12
            // Searches for duplicate infostore folder names and changes them.
            registry.addUpdateTask(new com.openexchange.groupware.update.tasks.InfostoreResolveFolderNameCollisions());
            // Changes URL columns for infostore items to VARCHAR(256).
            registry.addUpdateTask(new com.openexchange.groupware.update.tasks.InfostoreLongerURLFieldTask());
            // Version 13
            // Creates necessary table for spell check in database: spellcheck_user_dict
            registry.addUpdateTask(new com.openexchange.groupware.update.tasks.SpellCheckUserDictTableTask());
            // Version 14
            // Sets a not defined changed_from column for contacts to created_from.
            registry.addUpdateTask(new com.openexchange.groupware.update.tasks.ContactsChangedFromUpdateTask());
            // Version 15
            // Checks and fixes the VARCHAR column sizes for contacts tables.
            registry.addUpdateTask(new com.openexchange.groupware.update.tasks.ContactsFieldSizeUpdateTask());
            // Version 16
            // Moves contacts illegally moved to global addressbook into private contact folder.
            registry.addUpdateTask(new com.openexchange.groupware.update.tasks.ContactsGlobalMoveUpdateTask());
            // Version 17
            // Removes attachments and links to deleted contacts.
            registry.addUpdateTask(new com.openexchange.groupware.update.tasks.ContactsRepairLinksAttachments());
            // Version 18
            // Enlarges the column for task titles to VARCHAR(256).
            registry.addUpdateTask(new com.openexchange.groupware.update.tasks.EnlargeTaskTitle());
            // Version 19
            // Changes the column for series appointments exceptions to type TEXT to be able
            // to store a lot of exceptions.
            registry.addUpdateTask(new com.openexchange.groupware.calendar.update.AlterDeleteExceptionFieldLength());
            // Version 20
            // Removes broken reminder caused by a bad SQL update command.
            registry.addUpdateTask(new com.openexchange.groupware.update.tasks.RemoveBrokenReminder());
            // Version 21
            // Bug 12099 caused some appointments to have the attribute modifiedBy stored as
            // 0 in the database. This attribute is fixed with the creator.
            registry.addUpdateTask(new com.openexchange.groupware.update.tasks.AppointmentChangedFromZeroTask());
            // Version 22
            // Bug 12326 caused appointment exceptions to be treated at some code parts as
            // series. Fix for bug 12212 added an check to discover those exceptions and not
            // to treat them anymore as series. Fix for bug 12326 did this, too. Fix for bug
            // 12442 adds an update task that corrects invalid data in the database for those
            // appointments.
            registry.addUpdateTask(new com.openexchange.groupware.update.tasks.AppointmentExceptionRemoveDuplicateDatePosition());
            // Version 23
            // Bug 12495 caused appointment change exceptions to not have the recurrence date
            // position. This is essential for clients to determine the original position of
            // the change exception. This task tries to restore the missing recurrence date
            // position.
            registry.addUpdateTask(new com.openexchange.groupware.update.tasks.AppointmentRepairRecurrenceDatePosition());
            // Version 24
            // Bug 12528 caused appointment change exception to not have the recurrence
            // string anymore. This is essential for handling the recurrence date position
            // correctly. This task tries to restore the missing recurrence string by copying
            // it from the series appointment.
            registry.addUpdateTask(new com.openexchange.groupware.update.tasks.AppointmentRepairRecurrenceString());
            // Version 25
            // Bug 12595 caused a wrong folder identifier for participants of an appointment
            // change exception. Then for this participant the change exception is not
            // viewable anymore in the calendar. This update task tries to replace the wrong
            // folder identifier with the correct one.
            registry.addUpdateTask(new com.openexchange.groupware.update.tasks.CorrectWrongAppointmentFolder());
            // Version 26
            // Introduces foreign key constraints on infostore_document and del_infostore_document.
            // Assures these constraints are met.
            registry.addUpdateTask(new com.openexchange.groupware.update.tasks.ClearOrphanedInfostoreDocuments());
            // Version 27
            // Initial User Server Setting table
            registry.addUpdateTask(new com.openexchange.groupware.update.tasks.TaskCreateUserSettingServer());
            // Version 28
            // Adding column 'system' to both tables 'oxfolder_permissions' and 'del_oxfolder_permissions'
            registry.addUpdateTask(new com.openexchange.groupware.update.tasks.FolderAddPermColumnUpdateTask());
            // Version 29
            // Run task of version 17 again. The SP4 version was not fast enough for database
            // connection timeouts.
            registry.addUpdateTask(new com.openexchange.groupware.update.tasks.ContactsRepairLinksAttachments2());
            // Version 30
            // This update task combines several optimizations on the schema. Especially some
            // indexes are improved.
            registry.addUpdateTask(new com.openexchange.groupware.update.tasks.CorrectIndexes());
            // Version 31
            // This task corrects the charset and collation on all tables and the database
            // itself.
            registry.addUpdateTask(new com.openexchange.groupware.update.tasks.CorrectCharsetAndCollationTask());
            // Version 32
            // New infostore folder tree
            registry.addUpdateTask(new com.openexchange.groupware.update.tasks.NewInfostoreFolderTreeUpdateTask());
            // Version 33
            // Extends size of VARCHAR column 'dn' in both working and backup table of 'prg_date_rights'.
            registry.addUpdateTask(new com.openexchange.groupware.update.tasks.CalendarExtendDNColumnTask());
            // Version 34
            // Adds necessary tables for multiple mail accounts and migrates mail account data
            registry.addUpdateTask(new com.openexchange.groupware.update.tasks.MailAccountCreateTablesTask());
            registry.addUpdateTask(new com.openexchange.groupware.update.tasks.MailAccountMigrationTask());
            // Version 35
            // Adds necessary tables to support missing POP3 features
            registry.addUpdateTask(new com.openexchange.groupware.update.tasks.POP3CreateTableTask());
            // Version 36
            // Adds necessary tables to support generic configuration storage
            registry.addUpdateTask(new com.openexchange.groupware.update.tasks.CreateGenconfTablesTask());
            // Version 37
            // Adds necessary tables for subscribe service
            registry.addUpdateTask(new com.openexchange.groupware.update.tasks.CreateSubscribeTableTask());
            // Version 38
            // Adds necessary tables for publish service
            registry.addUpdateTask(new com.openexchange.groupware.update.tasks.CreatePublicationTablesTask());
            // Version 39
            // Adds necessary column in contact table for counting usage.
            registry.addUpdateTask(new com.openexchange.groupware.update.tasks.ContactsAddUseCountColumnUpdateTask());
        }
        if (LOG.isInfoEnabled()) {
            LOG.info("UpdateTaskCollection successfully started");
        }
    }

    public void stop() throws AbstractOXException {
        if (!started.compareAndSet(true, false)) {
            LOG.error("UpdateTaskCollection cannot be stopped since it has not been started before", new Throwable());
        }
        UpdateTaskRegistry.releaseInstance();
        UpdateTaskCollection.dispose();
        if (LOG.isInfoEnabled()) {
            LOG.info("UpdateTaskCollection successfully stopped");
        }
    }
}
