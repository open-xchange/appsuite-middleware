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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.groupware.update.internal;

import org.apache.commons.logging.Log;
import com.openexchange.groupware.update.UpdateTask;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.groupware.update.UpdateTaskV2;
import com.openexchange.groupware.update.tasks.AllowTextInValuesOfDynamicContextAttributesTask;
import com.openexchange.groupware.update.tasks.AllowTextInValuesOfDynamicUserAttributesTask;
import com.openexchange.groupware.update.tasks.CorrectAttachmentCountInAppointments;
import com.openexchange.groupware.update.tasks.CorrectFileAsInContacts;
import com.openexchange.groupware.update.tasks.CorrectOrganizerInAppointments;
import com.openexchange.groupware.update.tasks.CreateIndexOnContextAttributesTask;
import com.openexchange.groupware.update.tasks.CreateIndexOnUserAttributesForAliasLookupTask;
import com.openexchange.groupware.update.tasks.MailAccountAddReplyToTask;
import com.openexchange.groupware.update.tasks.VirtualFolderAddSortNumTask;
import com.openexchange.log.LogFactory;

/**
 * Lists all update tasks of the com.openexchange.server bundle.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class InternalList {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(InternalList.class));

    private static final InternalList SINGLETON = new InternalList();

    private InternalList() {
        super();
    }

    public static final InternalList getInstance() {
        return SINGLETON;
    }

    public void start() {
        final DynamicList registry = DynamicList.getInstance();
        for (final UpdateTask task : OLD_TASKS) {
            if (!registry.addUpdateTask(task)) {
                LOG.error("Internal update task \"" + task.getClass().getName() + "\" could not be registered.", new Exception());
            }
        }
        for (final UpdateTaskV2 task : TASKS) {
            if (!registry.addUpdateTask(task)) {
                LOG.error("Internal update task \"" + task.getClass().getName() + "\" could not be registered.", new Exception());
            }
        }
    }

    public void stop() {
        final DynamicList registry = DynamicList.getInstance();
        for (final UpdateTaskV2 task : TASKS) {
            registry.removeUpdateTask(task);
        }
        for (final UpdateTask task : OLD_TASKS) {
            registry.removeUpdateTask(task);
        }
    }

    private static final UpdateTask[] OLD_TASKS = new UpdateTask[] {
        new com.openexchange.groupware.update.tasks.CreateTableVersion(),
        new com.openexchange.groupware.update.tasks.SpamUpdateTask(),
        new com.openexchange.groupware.update.tasks.PasswordMechUpdateTask(),
        // Version 2
        new com.openexchange.groupware.calendar.update.AlterMailAddressLength(),
        new com.openexchange.groupware.update.tasks.TaskModifiedByNotNull(),
        // Version 4
        new com.openexchange.groupware.update.tasks.DelFolderTreeTableUpdateTask(),
        // Version 5
        new com.openexchange.groupware.update.tasks.UnboundFolderReplacementUpdateTask(),
        // Version 6
        new com.openexchange.groupware.calendar.update.AlterCreatingDate(),
        new com.openexchange.groupware.update.tasks.TaskReminderFolderZero(),
        new com.openexchange.groupware.update.tasks.MailUploadQuotaUpdateTask(),
        // Version 7
        new com.openexchange.groupware.update.tasks.NewAdminExtensionsUpdateTask(),
        // Version 8
        new com.openexchange.groupware.update.tasks.InfostoreRenamePersonalInfostoreFolders(),
        // Version 10
        new com.openexchange.groupware.calendar.update.UpdateFolderIdInReminder(),
        // Version 11
        new com.openexchange.groupware.update.tasks.ClearLeftoverAttachmentsUpdateTask(),

        // +++++++++++++++++++++++++++++++++ SP4 starts here. +++++++++++++++++++++++++++++++++

        // Version 12
        // Searches for duplicate infostore folder names and changes them.
        new com.openexchange.groupware.update.tasks.InfostoreResolveFolderNameCollisions(),
        // Changes URL columns for infostore items to VARCHAR(256).
        new com.openexchange.groupware.update.tasks.InfostoreLongerURLFieldTask(),
        // Version 13
        // Creates necessary table for spell check in database: spellcheck_user_dict
        new com.openexchange.groupware.update.tasks.SpellCheckUserDictTableTask(),
        // Version 14
        // Sets a not defined changed_from column for contacts to created_from.
        new com.openexchange.groupware.update.tasks.ContactsChangedFromUpdateTask(),
        // Version 15
        // Checks and fixes the VARCHAR column sizes for contacts tables.
        new com.openexchange.groupware.update.tasks.ContactsFieldSizeUpdateTask(),
        // Version 16
        // Moves contacts illegally moved to global addressbook into private contact folder.
        new com.openexchange.groupware.update.tasks.ContactsGlobalMoveUpdateTask(),
        // Version 17
        // Removes attachments and links to deleted contacts.
        new com.openexchange.groupware.update.tasks.ContactsRepairLinksAttachments(),
        // Version 18
        // Enlarges the column for task titles to VARCHAR(256).
        new com.openexchange.groupware.update.tasks.EnlargeTaskTitle(),
        // Version 19
        // Changes the column for series appointments exceptions to type TEXT to be able
        // to store a lot of exceptions.
        new com.openexchange.groupware.calendar.update.AlterDeleteExceptionFieldLength(),
        // Version 20
        // Removes broken reminder caused by a bad SQL update command.
        new com.openexchange.groupware.update.tasks.RemoveBrokenReminder(),
        // Version 21
        // Bug 12099 caused some appointments to have the attribute modifiedBy stored as
        // 0 in the database. This attribute is fixed with the creator.
        new com.openexchange.groupware.update.tasks.AppointmentChangedFromZeroTask(),
        // Version 22
        // Bug 12326 caused appointment exceptions to be treated at some code parts as
        // series. Fix for bug 12212 added an check to discover those exceptions and not
        // to treat them anymore as series. Fix for bug 12326 did this, too. Fix for bug
        // 12442 adds an update task that corrects invalid data in the database for those
        // appointments.
        new com.openexchange.groupware.update.tasks.AppointmentExceptionRemoveDuplicateDatePosition(),
        // Version 23
        // Bug 12495 caused appointment change exceptions to not have the recurrence date
        // position. This is essential for clients to determine the original position of
        // the change exception. This task tries to restore the missing recurrence date
        // position.
        new com.openexchange.groupware.update.tasks.AppointmentRepairRecurrenceDatePosition(),
        // Version 24
        // Bug 12528 caused appointment change exception to not have the recurrence
        // string anymore. This is essential for handling the recurrence date position
        // correctly. This task tries to restore the missing recurrence string by copying
        // it from the series appointment.
        new com.openexchange.groupware.update.tasks.AppointmentRepairRecurrenceString(),
        // Version 25
        // Bug 12595 caused a wrong folder identifier for participants of an appointment
        // change exception. Then for this participant the change exception is not
        // viewable anymore in the calendar. This update task tries to replace the wrong
        // folder identifier with the correct one.
        new com.openexchange.groupware.update.tasks.CorrectWrongAppointmentFolder(),

        // +++++++++++++++++++++++++++++++++ SP5 starts from here. +++++++++++++++++++++++++++++++++

        // Version 26
        // Introduces foreign key constraints on infostore_document and del_infostore_document.
        // Assures these constraints are met.
        new com.openexchange.groupware.update.tasks.ClearOrphanedInfostoreDocuments(),
        // Version 27
        // Initial User Server Setting table
        new com.openexchange.groupware.update.tasks.TaskCreateUserSettingServer(),
        // Version 28
        // Adding column 'system' to both tables 'oxfolder_permissions' and 'del_oxfolder_permissions'
        new com.openexchange.groupware.update.tasks.FolderAddPermColumnUpdateTask(),
        // Version 29
        // Run task of version 17 again. The SP4 version was not fast enough for database
        // connection timeouts.
        new com.openexchange.groupware.update.tasks.ContactsRepairLinksAttachments2(),
        // Version 30
        // This update task combines several optimizations on the schema. Especially some
        // indexes are improved.
        new com.openexchange.groupware.update.tasks.CorrectIndexes(),
        // Version 31
        // This task corrects the charset and collation on all tables and the database
        // itself.
        new com.openexchange.groupware.update.tasks.CorrectCharsetAndCollationTask(),
        // Version 32
        // New infostore folder tree
        new com.openexchange.groupware.update.tasks.NewInfostoreFolderTreeUpdateTask(),
        // Version 33
        // Extends size of VARCHAR column 'dn' in both working and backup table of 'prg_date_rights'.
        new com.openexchange.groupware.update.tasks.CalendarExtendDNColumnTask(),
        // Version 34
        // Adds an index on prg_dates_members to improve performance in large contexts.
        new com.openexchange.groupware.update.tasks.AddAppointmentParticipantsIndexTask(),

        // +++++++++++++++++++++++++++++++++ Version 6.10 starts here. +++++++++++++++++++++++++++++++++

        // Version 40
        // Adds necessary tables for multiple mail accounts and migrates mail account data
        new com.openexchange.groupware.update.tasks.MailAccountCreateTablesTask(),
        new com.openexchange.groupware.update.tasks.MailAccountMigrationTask(),
        // Version 42
        // Adds necessary tables to support missing POP3 features
        new com.openexchange.groupware.update.tasks.POP3CreateTableTask(),
        // Version 44
        // Adds necessary tables to support generic configuration storage
        new com.openexchange.groupware.update.tasks.CreateGenconfTablesTask(),
        // Version 46
        // Adds necessary tables for subscribe service
        new com.openexchange.groupware.update.tasks.CreateSubscribeTableTask(),
        // Version 48
        // Adds necessary tables for publish service
        new com.openexchange.groupware.update.tasks.CreatePublicationTablesTask(),
        // Version 50
        // Adds necessary column in contact table for counting usage.
        new com.openexchange.groupware.update.tasks.ContactsAddUseCountColumnUpdateTask(),
        // Version 52
        // Renames the standard group of all users.
        new com.openexchange.groupware.update.tasks.RenameGroupTask(),
        // Version 54
        // This update tasks improves performance of indexes for InfoStore tables.
        new com.openexchange.groupware.update.tasks.CorrectIndexes6_10(),
        // Version 56
        // Changes the column for series appointments exceptions to type TEXT to be able
        // to store a lot of exceptions.
        new com.openexchange.groupware.calendar.update.AlterChangeExceptionFieldLength(),
        // Version 58
        // Due to a bug, there are several appointments with the String "null" in the recurrence pattern
        // instead of SQL NULL. This update task repairs these broken lines in the database.
        new com.openexchange.groupware.calendar.update.RepairRecurrencePatternNullValue(),
        // Version 60
        // The collation of column uid in table login2user must be changed to utf8_bin to prevent a collision of login
        // names that are equal except some accent or some German umlauts.
        new com.openexchange.groupware.update.tasks.AlterUidCollation(),
        // Version 62
        // Runs the task AlterUidCollation again. The schema creating scripts did not contain the fix for v6.10.
        new com.openexchange.groupware.update.tasks.AlterUidCollation2(),

        // +++++++++++++++++++++++++++++++++ Version 6.12 starts here. +++++++++++++++++++++++++++++++++

        // Version 70
        // New config parameters to set the default confirmation status of newly created appointments
        // for participants in private an public folders
        new com.openexchange.groupware.update.tasks.DefaultConfirmStatusUpdateTask(),
        // Version 72
        // Creates necessary tables for virtual folder tree
        new com.openexchange.folderstorage.virtual.VirtualTreeCreateTableTask(),
        // Version 74
        // Creates indexes on tables "prg_contacts" and "del_contacts" to improve auto-complete search
        new com.openexchange.groupware.update.tasks.ContactsAddIndex4AutoCompleteSearch(),
        // Version 76
        // Drops incorrect admin permission on top level infostore folder
        new com.openexchange.groupware.update.tasks.RemoveAdminPermissionOnInfostoreTask(),

        // +++++++++++++++++++++++++++++++++ Version 6.14 starts here. +++++++++++++++++++++++++++++++++

        // Version 90
        // Creates the table replicationMonitor for monitoring if slaves are completely replicated.
        new com.openexchange.groupware.update.tasks.CreateReplicationTableTask(),
        // Version 92
        // Config parameter for en-/disabling contact collection on incoming and outgoing mails.
        new com.openexchange.groupware.update.tasks.ContactCollectOnIncomingAndOutgoingMailUpdateTask(),
        // Version 94
        // Resolves Global Address Book's group permission to individual user permissions.
        new com.openexchange.groupware.update.tasks.GlobalAddressBookPermissionsResolverTask(),
        // Version 96
        // Adds "personal" column to to mail/transport account table.
        new com.openexchange.groupware.update.tasks.MailAccountAddPersonalTask(),
        // Version 98
        // Removes duplicate contact collector folders
        new com.openexchange.groupware.update.tasks.DuplicateContactCollectFolderRemoverTask(),
        // Version 100
        // Adds necessary indexes to improve shared folder search for a user
        new com.openexchange.groupware.update.tasks.FolderAddIndex4SharedFolderSearch(),

        // +++++++++++++++++++++++++++++++++ Version 6.16 starts here. +++++++++++++++++++++++++++++++++

        // Version 200
        // This is the last update task with a database schema version. All newer update task must use the new update task interface
        // UpdateTaskV2.
        new com.openexchange.groupware.update.tasks.LastVersionedUpdateTask()
        // All following update tasks must be added to the list below.
    };

    /**
     * All this tasks should extend {@link UpdateTaskAdapter} to fulfill the prerequisites to be sorted among their dependencies.
     */
    private static final UpdateTaskV2[] TASKS = new UpdateTaskV2[] {
        // Renames "Unified INBOX" to "Unified Mail"
        new com.openexchange.groupware.update.tasks.UnifiedINBOXRenamerTask(),

        // Creates necessary tables for mail header cache
        new com.openexchange.groupware.update.tasks.HeaderCacheCreateTableTask(),

        // Extends the calendar tables and creates table to store the confirmation data for external participants.
        new com.openexchange.groupware.update.tasks.ExtendCalendarForIMIPHandlingTask(),

        // Enables the bit for the contact collector feature for every user because that bit was not checked before 6.16.
        new com.openexchange.groupware.update.tasks.ContactCollectorReEnabler(),

        // +++++++++++++++++++++++++++++++++ Version 6.18 starts here. +++++++++++++++++++++++++++++++++

        // Adds a column to the table user_setting_server named folderTree to store the selected folder tree.
        new com.openexchange.groupware.update.tasks.FolderTreeSelectionTask(),

        // Repairs appointments where the number of attachments does not match the real amount of attachments.
        new com.openexchange.groupware.update.tasks.AttachmentCountUpdateTask(),

        // Creates an initial empty filestore usage entry for every context that currently did not uploaded anything.
        new com.openexchange.groupware.update.tasks.AddInitialFilestoreUsage(),

        // Currently users contacts are created with the display name attribute filed. Outlook primarily uses the fileAs attribute. This
        // task copies the display name to fileAs if that is empty.
        new com.openexchange.groupware.update.tasks.AddFileAsForUserContacts(),

        // Extend field "reason" for participants.
        new com.openexchange.groupware.update.tasks.ParticipantCommentFieldLength(),

        // New table for linking several appointments (from different sources) together that represent the same person.
        new com.openexchange.groupware.update.tasks.AggregatingContactTableService(),

        // Creates new table for multi-purpose ID generation
        new com.openexchange.groupware.update.tasks.IDCreateTableTask(),

        // TODO: Enable virtual folder tree update task when needed
        // Migrates existing folder data to new outlook-like folder tree structure
        // new com.openexchange.folderstorage.virtual.VirtualTreeMigrationTask()

        // +++++++++++++++++++++++++++++++++ Version 6.20 starts here. +++++++++++++++++++++++++++++++++

        // Transforms the "info" field to a TEXT field. This fields seems not to be used anywhere.
        new com.openexchange.groupware.update.tasks.ContactInfoField2Text(),

        // Creates new Contact fields (First Name, Last Name, Company) for Kana based search in japanese environments.
        new com.openexchange.groupware.update.tasks.ContactFieldsForJapaneseKanaSearch(),

        // Remove facebook subscriptions to force use of new oauth
        new com.openexchange.groupware.update.tasks.FacebookCrawlerSubscriptionRemoverTask(),

        // Remove linkedin subscriptions to force use of new oauth
        new com.openexchange.groupware.update.tasks.LinkedInCrawlerSubscriptionsRemoverTask(),

        // Remove yahoo subscriptions to force use of new oauth
        new com.openexchange.groupware.update.tasks.DeleteOldYahooSubscriptions(),

        // Switch the column type of 'value' in contextAttribute to TEXT
        new AllowTextInValuesOfDynamicContextAttributesTask(),

        // Switch the column type of 'value' in user_attribute to TEXT
        new AllowTextInValuesOfDynamicUserAttributesTask(),

        // Recreate the index on the context attributes table
        new CreateIndexOnContextAttributesTask(),

        // Recreate the index on the user attributes table for alias lookup
        new CreateIndexOnUserAttributesForAliasLookupTask(),

        // Correct the attachment count in the dates table
        new CorrectAttachmentCountInAppointments(),

        // Corrects the organizer in appointments. When exporting iCal and importing it again the organizer gets value 'null' instead of SQL
        // NULL. This task corrects this.
        new CorrectOrganizerInAppointments(),

        // Corrects field90 aka fileAs in contacts to have proper contact names in card view of Outlook OXtender 2.
        new CorrectFileAsInContacts(),

        // Add "sortNum" column to virtual folder table.
        new VirtualFolderAddSortNumTask(),

        // +++++++++++++++++++++++++++++++++ Version 6.20.1 starts here. +++++++++++++++++++++++++++++++++

        // Restores the initial permissions on the public root folder.
        new com.openexchange.groupware.update.tasks.DropIndividualUserPermissionsOnPublicFolderTask(),

        // Adds Outlook address fields to contact tables
        new com.openexchange.groupware.update.tasks.ContactAddOutlookAddressFieldsTask(),

        // Add UID field to contact tables.
        new com.openexchange.groupware.update.tasks.ContactAddUIDFieldTask(),

        // Add UIDs to contacts if missing
        new com.openexchange.groupware.update.tasks.ContactAddUIDValueTask(),

        // Adds UIDs to tasks.
        new com.openexchange.groupware.update.tasks.TasksAddUidColumnTask(),

        // Adds UID indexes.
        new com.openexchange.groupware.update.tasks.CalendarAddUIDIndexTask(),

        // Drops rather needless foreign keys
        new com.openexchange.groupware.update.tasks.DropFKTask(),

        // Adds 'organizerId', 'principal' and 'principalId' to prg_dates and del_dates
        new com.openexchange.groupware.update.tasks.AppointmentAddOrganizerIdPrincipalPrincipalIdColumnsTask(),

        // Adds index to prg_dates_members and del_dates_members
        new com.openexchange.groupware.update.tasks.CalendarAddIndex2DatesMembers(),

        // Adds index to oxfolder_tree and del_oxfolder_tree
        new com.openexchange.groupware.update.tasks.FolderAddIndex2LastModified(),

        // Checks for missing folder 'public_infostore' (15) in any available context
        new com.openexchange.groupware.update.tasks.CheckForPublicInfostoreFolderTask(),

        // Drops useless foreign keys from 'malPollHash' table
        new com.openexchange.groupware.update.tasks.MALPollDropConstraintsTask(),

        // +++++++++++++++++++++++++++++++++ Version 6.20.3 starts here. +++++++++++++++++++++++++++++++++

        // Extends dn fields in calendar tables to 320 chars.
        new com.openexchange.groupware.update.tasks.CalendarExtendDNColumnTaskV2(),

        // +++++++++++++++++++++++++++++++++ Version 6.20.5 starts here. +++++++++++++++++++++++++++++++++

        // Creates indexes on tables "prg_dlist" and "del_dlist" to improve look-up.
        new com.openexchange.groupware.update.tasks.DListAddIndexForLookup(),

        // +++++++++++++++++++++++++++++++++ Version 6.20.7 starts here. +++++++++++++++++++++++++++++++++

        // Another attempt: Adds 'organizerId', 'principal' and 'principalId' to prg_dates and del_dates
        new com.openexchange.groupware.update.tasks.AppointmentAddOrganizerIdPrincipalPrincipalIdColumnsTask2(),

        // Add UIDs to appointments if missing.
        new com.openexchange.groupware.update.tasks.CalendarAddUIDValueTask(),

        // +++++++++++++++++++++++++++++++++ Version 6.22.0 starts here. +++++++++++++++++++++++++++++++++

        // Add "replyTo" column to mail/transport account table
        new MailAccountAddReplyToTask(),

        // Migrate "replyTo" information from properties table to account tables
        new com.openexchange.groupware.update.tasks.MailAccountMigrateReplyToTask(),

        // Add 'filename' column to appointment tables.
        new com.openexchange.groupware.update.tasks.AppointmentAddFilenameColumnTask(),

        // Add 'filename' column to task tables.
        new com.openexchange.groupware.update.tasks.TasksAddFilenameColumnTask(),

        // Removes unnecessary indexes from certain tables (see Bug #21882)
        new com.openexchange.groupware.update.tasks.RemoveUnnecessaryIndexes(),
        new com.openexchange.groupware.update.tasks.RemoveUnnecessaryIndexes2(),

        // +++++++++++++++++++++++++++++++++ Version 6.22.1 starts here. +++++++++++++++++++++++++++++++++
        new com.openexchange.groupware.update.tasks.ContactFixUserDistListReferencesTask(),

        // +++++++++++++++++++++++++++++++++ Version 7.0.0 starts here. +++++++++++++++++++++++++++++++++

        // Extends the resources' description field
        new com.openexchange.groupware.update.tasks.EnlargeResourceDescription(),

        // Extends the UID field
        new com.openexchange.groupware.update.tasks.EnlargeCalendarUid(),

        // Sets the changing date once for users with a different defaultSendAddress
        new com.openexchange.groupware.update.tasks.ContactAdjustLastModifiedForChangedSenderAddress(),

        // Drop foreign key constraints from obsolete tables
        new com.openexchange.groupware.update.tasks.HeaderCacheDropFKTask(),

    };
}
