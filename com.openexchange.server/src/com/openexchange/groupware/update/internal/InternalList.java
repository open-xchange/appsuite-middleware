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

package com.openexchange.groupware.update.internal;

import java.util.ArrayList;
import java.util.List;

import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.groupware.update.UpdateTaskV2;
import com.openexchange.groupware.update.tasks.AddOAuthColumnToMailAccountTableTask;
import com.openexchange.groupware.update.tasks.AddPrimaryKeyVcardIdsTask;
import com.openexchange.groupware.update.tasks.AddPrimaryKeyVcardPrincipalTask;
import com.openexchange.groupware.update.tasks.AddSnippetAttachmentPrimaryKeyUpdateTask;
import com.openexchange.groupware.update.tasks.AddStartTLSColumnForMailAccountTablesTask;
import com.openexchange.groupware.update.tasks.AddUUIDForDListTables;
import com.openexchange.groupware.update.tasks.AddUUIDForInfostoreReservedPaths;
import com.openexchange.groupware.update.tasks.AddUUIDForUpdateTaskTable;
import com.openexchange.groupware.update.tasks.AddUUIDForUserAttributeTable;
import com.openexchange.groupware.update.tasks.AllowTextInValuesOfDynamicContextAttributesTask;
import com.openexchange.groupware.update.tasks.AllowTextInValuesOfDynamicUserAttributesTask;
import com.openexchange.groupware.update.tasks.CorrectAttachmentCountInAppointments;
import com.openexchange.groupware.update.tasks.CorrectFileAsInContacts;
import com.openexchange.groupware.update.tasks.CorrectOrganizerInAppointments;
import com.openexchange.groupware.update.tasks.CreateIcalIdsPrimaryKeyTask;
import com.openexchange.groupware.update.tasks.CreateIcalPrincipalPrimaryKeyTask;
import com.openexchange.groupware.update.tasks.CreateIndexOnContextAttributesTask;
import com.openexchange.groupware.update.tasks.CreateIndexOnUserAttributesForAliasLookupTask;
import com.openexchange.groupware.update.tasks.DateExternalCreateForeignKeyUpdateTask;
import com.openexchange.groupware.update.tasks.DateExternalDropForeignKeyUpdateTask;
import com.openexchange.groupware.update.tasks.DelDateExternalCreateForeignKeyUpdateTask;
import com.openexchange.groupware.update.tasks.DelDateExternalDropForeignKeyUpdateTask;
import com.openexchange.groupware.update.tasks.DelDatesMembersPrimaryKeyUpdateTask;
import com.openexchange.groupware.update.tasks.DelDatesPrimaryKeyUpdateTask;
import com.openexchange.groupware.update.tasks.DelInfostorePrimaryKeyUpdateTask;
import com.openexchange.groupware.update.tasks.DropDuplicateEntryFromUpdateTaskTable;
import com.openexchange.groupware.update.tasks.DropVersionTableTask;
import com.openexchange.groupware.update.tasks.GenconfAttributesBoolsAddPrimaryKey;
import com.openexchange.groupware.update.tasks.GenconfAttributesBoolsAddUuidUpdateTask;
import com.openexchange.groupware.update.tasks.GenconfAttributesStringsAddPrimaryKey;
import com.openexchange.groupware.update.tasks.GenconfAttributesStringsAddUuidUpdateTask;
import com.openexchange.groupware.update.tasks.InfostoreClearDelTablesTask;
import com.openexchange.groupware.update.tasks.InfostoreDocumentCreateForeignKeyUpdateTask;
import com.openexchange.groupware.update.tasks.InfostoreDocumentDropForeignKeyUpdateTask;
import com.openexchange.groupware.update.tasks.InfostorePrimaryKeyUpdateTask;
import com.openexchange.groupware.update.tasks.MailAccountAddReplyToTask;
import com.openexchange.groupware.update.tasks.MakeFolderIdPrimaryForDelContactsTable;
import com.openexchange.groupware.update.tasks.MakeUUIDPrimaryForDListTables;
import com.openexchange.groupware.update.tasks.MakeUUIDPrimaryForDListTablesV2;
import com.openexchange.groupware.update.tasks.MakeUUIDPrimaryForInfostoreReservedPaths;
import com.openexchange.groupware.update.tasks.MakeUUIDPrimaryForUpdateTaskTable;
import com.openexchange.groupware.update.tasks.MakeUUIDPrimaryForUserAttributeTable;
import com.openexchange.groupware.update.tasks.MigrateUUIDsForUserAliasTable;
import com.openexchange.groupware.update.tasks.PrgContactsLinkageAddPrimaryKeyUpdateTask;
import com.openexchange.groupware.update.tasks.PrgContactsLinkageAddUuidUpdateTask;
import com.openexchange.groupware.update.tasks.PrgDatesMembersPrimaryKeyUpdateTask;
import com.openexchange.groupware.update.tasks.PrgDatesPrimaryKeyUpdateTask;
import com.openexchange.groupware.update.tasks.PrgLinksAddPrimaryKeyUpdateTask;
import com.openexchange.groupware.update.tasks.PrgLinksAddUuidUpdateTask;
import com.openexchange.groupware.update.tasks.Release781UpdateTask;
import com.openexchange.groupware.update.tasks.RemoveAliasInUserAttributesTable;
import com.openexchange.groupware.update.tasks.RemoveRedundantKeysForBug26913UpdateTask;
import com.openexchange.groupware.update.tasks.ResourceClearDelTablesTask;
import com.openexchange.groupware.update.tasks.UserClearDelTablesTask;
import com.openexchange.groupware.update.tasks.UserSettingServerAddPrimaryKeyUpdateTask;
import com.openexchange.groupware.update.tasks.UserSettingServerAddUuidUpdateTask;
import com.openexchange.groupware.update.tasks.VirtualFolderAddSortNumTask;
import com.openexchange.groupware.update.tasks.objectusagecount.CreateObjectUseCountTableTask;

/**
 * Lists all update tasks of the com.openexchange.server bundle.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class InternalList {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(InternalList.class);

    private static final InternalList SINGLETON = new InternalList();

    /**
     * Gets the {@link InternalList} instance
     *
     * @return The instance
     */
    public static final InternalList getInstance() {
        return SINGLETON;
    }

    private InternalList() {
        super();
    }

    /**
     * Starts the internal list.
     */
    public void start() {
        final DynamicList registry = DynamicList.getInstance();
        TASKS = genTaskList();
        for (final UpdateTaskV2 task : TASKS) {
            if (!registry.addUpdateTask(task)) {
                LOG.error("Internal update task \"{}\" could not be registered.", task.getClass().getName(), new Exception());
            }
        }
    }

    /**
     * Stops the internal list.
     */
    public void stop() {
        final DynamicList registry = DynamicList.getInstance();
        for (final UpdateTaskV2 task : TASKS) {
            registry.removeUpdateTask(task);
        }
    }

    /**
     * All this tasks should extend {@link UpdateTaskAdapter} to fulfill the prerequisites to be sorted among their dependencies.
     */
    private static UpdateTaskV2[] TASKS = null;

    private static UpdateTaskV2[] genTaskList() {
        List<UpdateTaskV2> list = new ArrayList<UpdateTaskV2>();

        // Renames "Unified INBOX" to "Unified Mail"
        list.add(new com.openexchange.groupware.update.tasks.UnifiedINBOXRenamerTask());

        // Creates necessary tables for mail header cache
        list.add(new com.openexchange.groupware.update.tasks.HeaderCacheCreateTableTask());

        // Extends the calendar tables and creates table to store the confirmation data for external participants.
        list.add(new com.openexchange.groupware.update.tasks.ExtendCalendarForIMIPHandlingTask());

        // Enables the bit for the contact collector feature for every user because that bit was not checked before 6.16.
        list.add(new com.openexchange.groupware.update.tasks.ContactCollectorReEnabler());

        // +++++++++++++++++++++++++++++++++ Version 6.18 starts here. +++++++++++++++++++++++++++++++++

        // Adds a column to the table user_setting_server named folderTree to store the selected folder tree.
        list.add(new com.openexchange.groupware.update.tasks.FolderTreeSelectionTask());

        // Repairs appointments where the number of attachments does not match the real amount of attachments.
        list.add(new com.openexchange.groupware.update.tasks.AttachmentCountUpdateTask());

        // Creates an initial empty filestore usage entry for every context that currently did not uploaded anything.
        list.add(new com.openexchange.groupware.update.tasks.AddInitialFilestoreUsage());

        // Currently users contacts are created with the display name attribute filed. Outlook primarily uses the fileAs attribute. This
        // task copies the display name to fileAs if that is empty.
        list.add(new com.openexchange.groupware.update.tasks.AddFileAsForUserContacts());

        // Extend field "reason" for participants.
        list.add(new com.openexchange.groupware.update.tasks.ParticipantCommentFieldLength());

        // New table for linking several appointments (from different sources) together that represent the same person.
        list.add(new com.openexchange.groupware.update.tasks.AggregatingContactTableService());

        // Creates new table for multi-purpose ID generation
        list.add(new com.openexchange.groupware.update.tasks.IDCreateTableTask());

        // TODO: Enable virtual folder tree update task when needed
        // Migrates existing folder data to new outlook-like folder tree structure
        // new com.openexchange.folderstorage.virtual.VirtualTreeMigrationTask()

        // +++++++++++++++++++++++++++++++++ Version 6.20 starts here. +++++++++++++++++++++++++++++++++

        // Transforms the "info" field to a TEXT field. This fields seems not to be used anywhere.
        list.add(new com.openexchange.groupware.update.tasks.ContactInfoField2Text());

        // Creates new Contact fields (First Name); Last Name); Company) for Kana based search in japanese environments.
        list.add(new com.openexchange.groupware.update.tasks.ContactFieldsForJapaneseKanaSearch());

        // Remove linkedin subscriptions to force use of new oauth
        list.add(new com.openexchange.groupware.update.tasks.LinkedInCrawlerSubscriptionsRemoverTask());

        // Remove yahoo subscriptions to force use of new oauth
        list.add(new com.openexchange.groupware.update.tasks.DeleteOldYahooSubscriptions());

        // Switch the column type of 'value' in contextAttribute to TEXT
        list.add(new AllowTextInValuesOfDynamicContextAttributesTask());

        // Switch the column type of 'value' in user_attribute to TEXT
        list.add(new AllowTextInValuesOfDynamicUserAttributesTask());

        // Recreate the index on the context attributes table
        list.add(new CreateIndexOnContextAttributesTask());

        // Recreate the index on the user attributes table for alias lookup
        list.add(new CreateIndexOnUserAttributesForAliasLookupTask());

        // Correct the attachment count in the dates table
        list.add(new CorrectAttachmentCountInAppointments());

        // Corrects the organizer in appointments. When exporting iCal and importing it again the organizer gets value 'null' instead of SQL
        // NULL. This task corrects this.
        list.add(new CorrectOrganizerInAppointments());

        // Corrects field90 aka fileAs in contacts to have proper contact names in card view of Outlook OXtender 2.
        list.add(new CorrectFileAsInContacts());

        // Add "sortNum" column to virtual folder table.
        list.add(new VirtualFolderAddSortNumTask());

        // +++++++++++++++++++++++++++++++++ Version 6.20.1 starts here. +++++++++++++++++++++++++++++++++

        // Restores the initial permissions on the public root folder.
        list.add(new com.openexchange.groupware.update.tasks.DropIndividualUserPermissionsOnPublicFolderTask());

        // Adds Outlook address fields to contact tables
        list.add(new com.openexchange.groupware.update.tasks.ContactAddOutlookAddressFieldsTask());

        // Add UID field to contact tables.
        list.add(new com.openexchange.groupware.update.tasks.ContactAddUIDFieldTask());

        // Add UIDs to contacts if missing
        list.add(new com.openexchange.groupware.update.tasks.ContactAddUIDValueTask());

        // Adds UIDs to tasks.
        list.add(new com.openexchange.groupware.update.tasks.TasksAddUidColumnTask());

        // Adds UID indexes.
        list.add(new com.openexchange.groupware.update.tasks.CalendarAddUIDIndexTask());

        // Drops rather needless foreign keys
        list.add(new com.openexchange.groupware.update.tasks.DropFKTask());

        // Adds 'organizerId'); 'principal' and 'principalId' to prg_dates and del_dates
        list.add(new com.openexchange.groupware.update.tasks.AppointmentAddOrganizerIdPrincipalPrincipalIdColumnsTask());

        // Adds index to prg_dates_members and del_dates_members
        list.add(new com.openexchange.groupware.update.tasks.CalendarAddIndex2DatesMembers());

        // Adds index to oxfolder_tree and del_oxfolder_tree
        list.add(new com.openexchange.groupware.update.tasks.FolderAddIndex2LastModified());

        // Checks for missing folder 'public_infostore' (15) in any available context
        list.add(new com.openexchange.groupware.update.tasks.CheckForPublicInfostoreFolderTask());

        // Drops useless foreign keys from 'malPollHash' table
        list.add(new com.openexchange.groupware.update.tasks.MALPollDropConstraintsTask());

        // +++++++++++++++++++++++++++++++++ Version 6.20.3 starts here. +++++++++++++++++++++++++++++++++

        // Extends dn fields in calendar tables to 320 chars.
        list.add(new com.openexchange.groupware.update.tasks.CalendarExtendDNColumnTaskV2());

        // +++++++++++++++++++++++++++++++++ Version 6.20.5 starts here. +++++++++++++++++++++++++++++++++

        // Creates indexes on tables "prg_dlist" and "del_dlist" to improve look-up.
        list.add(new com.openexchange.groupware.update.tasks.DListAddIndexForLookup());

        // +++++++++++++++++++++++++++++++++ Version 6.20.7 starts here. +++++++++++++++++++++++++++++++++

        // Another attempt: Adds 'organizerId'); 'principal' and 'principalId' to prg_dates and del_dates
        list.add(new com.openexchange.groupware.update.tasks.AppointmentAddOrganizerIdPrincipalPrincipalIdColumnsTask2());

        // Add UIDs to appointments if missing.
        list.add(new com.openexchange.groupware.update.tasks.CalendarAddUIDValueTask());

        // +++++++++++++++++++++++++++++++++ Version 6.22.0 starts here. +++++++++++++++++++++++++++++++++

        // Add "replyTo" column to mail/transport account table
        list.add(new MailAccountAddReplyToTask());

        // Migrate "replyTo" information from properties table to account tables
        list.add(new com.openexchange.groupware.update.tasks.MailAccountMigrateReplyToTask());

        // Add 'filename' column to appointment tables.
        list.add(new com.openexchange.groupware.update.tasks.AppointmentAddFilenameColumnTask());

        // Add 'filename' column to task tables.
        list.add(new com.openexchange.groupware.update.tasks.TasksAddFilenameColumnTask());

        // Removes unnecessary indexes from certain tables (see Bug #21882)
        list.add(new com.openexchange.groupware.update.tasks.RemoveUnnecessaryIndexes());
        list.add(new com.openexchange.groupware.update.tasks.RemoveUnnecessaryIndexes2());

        // +++++++++++++++++++++++++++++++++ Version 6.22.1 starts here. +++++++++++++++++++++++++++++++++
        list.add(new com.openexchange.groupware.update.tasks.ContactFixUserDistListReferencesTask());

        // +++++++++++++++++++++++++++++++++ Version 7.0.0 starts here. +++++++++++++++++++++++++++++++++

        // Extends the resources' description field
        list.add(new com.openexchange.groupware.update.tasks.EnlargeResourceDescription());

        // Extends the UID field
        list.add(new com.openexchange.groupware.update.tasks.EnlargeCalendarUid());

        // Sets the changing date once for users with a different defaultSendAddress
        list.add(new com.openexchange.groupware.update.tasks.ContactAdjustLastModifiedForChangedSenderAddress());

        // Drop foreign key constraints from obsolete tables
        list.add(new com.openexchange.groupware.update.tasks.HeaderCacheDropFKTask());

        // +++++++++++++++++++++++++++++++++ Version 7.4.0 starts here. +++++++++++++++++++++++++++++++++

        // Add UUID column to genconf_attributes_strings table
        list.add(new GenconfAttributesStringsAddUuidUpdateTask());

        // Add UUID column to genconf_attributes_bools table
        list.add(new GenconfAttributesBoolsAddUuidUpdateTask());

        // Add UUID column to updatTask table
        list.add(new AddUUIDForUpdateTaskTable());

        //Add Uuid column to prg_links table
        list.add(new PrgLinksAddUuidUpdateTask());

        //Add Uuid column to prg_contacts_linkage table
        list.add(new PrgContactsLinkageAddUuidUpdateTask());

        //Add Uuid column to dlist tables
        list.add(new AddUUIDForDListTables());

        //Add primary key to ical_ids table
        list.add(new CreateIcalIdsPrimaryKeyTask());

        //Add primary key to ical_principal table
        list.add(new CreateIcalPrincipalPrimaryKeyTask());

        //Add primary key to vcard_ids table
        list.add(new AddPrimaryKeyVcardIdsTask());

        //Add primary key to vcard_principal table
        list.add(new AddPrimaryKeyVcardPrincipalTask());

        // Add UUID column to user_attribute table
        list.add(new AddUUIDForUserAttributeTable());

        //Add UUID column to infostoreReservedPaths table
        list.add(new AddUUIDForInfostoreReservedPaths());

        //Add UUID column to user_setting_server table
        list.add(new UserSettingServerAddUuidUpdateTask());

        //Drop foreign key from infostore_document table
        list.add(new InfostoreDocumentDropForeignKeyUpdateTask());

        //Add folder_id to primary key in infostore table
        list.add(new InfostorePrimaryKeyUpdateTask());

        //Add foreign key to infostore_document_table
        list.add(new InfostoreDocumentCreateForeignKeyUpdateTask());

        //Add folder_id to primary key in del_infostore table
        list.add(new DelInfostorePrimaryKeyUpdateTask());

        //Drop foreign key from dateExternal table
        list.add(new DateExternalDropForeignKeyUpdateTask());

        //Add folder_id to primary key in prg_dates
        list.add(new PrgDatesPrimaryKeyUpdateTask());

        //Create foreign key in dateExternal table
        list.add(new DateExternalCreateForeignKeyUpdateTask());

        //Drop foreign key from delDateExternal table
        list.add(new DelDateExternalDropForeignKeyUpdateTask());

        //Add folder_id to primary key in del_dates
        list.add(new DelDatesPrimaryKeyUpdateTask());

        //Create foreign key in delDateExternal table
        list.add(new DelDateExternalCreateForeignKeyUpdateTask());

        // Add folder_id to primary key in del_contacts
        list.add(new MakeFolderIdPrimaryForDelContactsTable());

        // Remove redundant keys (see bug 26913)
        list.add(new RemoveRedundantKeysForBug26913UpdateTask());

        // Add synthetic primary keys to tables without natural key if full primary key support is enabled
        {

            // Add primary key to genconf_attributes_strings table
            list.add(new GenconfAttributesStringsAddPrimaryKey());

            // Add primary key to genconf_attributes_bools table
            list.add(new GenconfAttributesBoolsAddPrimaryKey());

            // Add primary key to updateTask table
            list.add(new DropDuplicateEntryFromUpdateTaskTable());
            list.add(new MakeUUIDPrimaryForUpdateTaskTable());

            // Add primary key to user_attribute table
            list.add(new MakeUUIDPrimaryForUserAttributeTable());

            //Add primary key to prg_links table
            list.add(new PrgLinksAddPrimaryKeyUpdateTask());

            //Add primary key to prg_contacts_linkage table
            list.add(new PrgContactsLinkageAddPrimaryKeyUpdateTask());

            //Add primary key to dlist tables
            list.add(new MakeUUIDPrimaryForDListTables());

            //Add primary key to infostoreReservedPaths table;
            list.add(new MakeUUIDPrimaryForInfostoreReservedPaths());

            //Add primary key to user_setting_server table
            list.add(new UserSettingServerAddPrimaryKeyUpdateTask());

            //Add folder_id to primary key in prg_dates_members
            list.add(new PrgDatesMembersPrimaryKeyUpdateTask());

            //Add folder_id to primary key in del_dates_members
            list.add(new DelDatesMembersPrimaryKeyUpdateTask());

        }

        // Adds "archive" and "archive_fullname" columns to mail/transport account table
        list.add(new com.openexchange.groupware.update.tasks.MailAccountAddArchiveTask());

        // +++++++++++++++++++++++++++++++++ Version 7.4.1 starts here. +++++++++++++++++++++++++++++++++

        // Removes obsolete data from the 'del_contacts', 'del_dlist' and 'del_contacts_image' tables
        list.add(new com.openexchange.groupware.update.tasks.ContactClearDelTablesTasks());

        // Removes obsolete data from the 'del_task' table
        list.add(new com.openexchange.groupware.update.tasks.TaskClearDelTablesTasks());

        // Removes obsolete data from the 'del_dates' table
        list.add(new com.openexchange.groupware.update.tasks.AppointmentClearDelTablesTasks());

        // Removes obsolete data from the 'del_user' table
        list.add(new UserClearDelTablesTask());

        // Removes obsolete data from the 'del_resource' table
        list.add(new ResourceClearDelTablesTask());

        // Removes obsolete data from the 'del_infostore_document' table
        list.add(new InfostoreClearDelTablesTask());

        // Removes obsolete data from the 'del_oxfolder_tree', and 'virtualBackupTree' tables
        list.add(new com.openexchange.groupware.update.tasks.FolderClearDelTablesTasks());

        // Adds default values to the 'del_oxfolder_tree', and 'virtualBackupTree' tables.
        list.add(new com.openexchange.groupware.update.tasks.FolderDefaultValuesForDelTablesTasks());

        // Extends the sizes of the 'filename', 'title' and 'file_size' columns in the 'infostore_document' table
        list.add(new com.openexchange.groupware.update.tasks.InfostoreExtendFilenameTitleAndFilesizeTask());

        // Extends the size of the 'name' column in the 'infostoreReservedPaths' table.
        list.add(new com.openexchange.groupware.update.tasks.InfostoreExtendReservedPathsNameTask());

        // +++++++++++++++++++++++++++++++++ Version 7.4.2 starts here. +++++++++++++++++++++++++++++++++

        // Extends the size of the 'fname' column in the 'oxfolder_tree' table, as well as the 'name' column in the 'virtualTree' table.
        list.add(new com.openexchange.groupware.update.tasks.FolderExtendNameTask());

        // Extende folder tables by "meta" JSON BLOB
        list.add(new com.openexchange.groupware.update.tasks.AddMetaForOXFolderTable());

        // Add primary key to snippetAttachment table, fix for bug 30293
        list.add(new AddSnippetAttachmentPrimaryKeyUpdateTask());

        // Performs several adjustments to DB schema to get aligned to clean v7.4.1 installation
        list.add(new com.openexchange.groupware.update.tasks.DropFKTaskv2());

        // Adds/corrects user mail index: INDEX (mail) -> INDEX (cid, mail(255))
        list.add(new com.openexchange.groupware.update.tasks.UserAddMailIndexTask());

        // +++++++++++++++++++++++++++++++++ Version 7.6.0 starts here. +++++++++++++++++++++++++++++++++

        // Extends infostore document tables by "meta" JSON BLOB.
        list.add(new com.openexchange.groupware.update.tasks.AddMetaForInfostoreDocumentTable());

        // Extends infostore document tables by the (`cid`, `file_md5sum`) index.
        list.add(new com.openexchange.groupware.update.tasks.AddMD5SumIndexForInfostoreDocumentTable());

        // Adds (cid,changing_date) index to calendar tables if missing
        list.add(new com.openexchange.groupware.update.tasks.CalendarAddChangingDateIndexTask());

        // Checks and drops obsolete tables possibly created for managing POP3 accounts
        list.add(new com.openexchange.groupware.update.tasks.POP3CheckAndDropObsoleteTablesTask());

        // Ensures that each folder located below a user's default infostore trash folder is of type 16
        list.add(new com.openexchange.groupware.update.tasks.FolderInheritTrashFolderTypeTask());

        // +++++++++++++++++++++++++++++++++ Version 7.6.1 starts here. +++++++++++++++++++++++++++++++++

        // Removes invalid priority values from tasks
        list.add(new com.openexchange.groupware.update.tasks.TasksDeleteInvalidPriorityTask());

        // Corrects values in the 'changing_date' column that are set to {@link Long#MAX_VALUE}.
        list.add(new com.openexchange.groupware.update.tasks.FolderCorrectChangingDateTask());

        // (Re-)adds indexes in prg_contacts for "auto-complete" queries
        list.add(new com.openexchange.groupware.update.tasks.ContactsAddIndex4AutoCompleteSearchV2());

        // Check if foreign keys in date tables are dropped and drop them if necessary
        list.add(new com.openexchange.groupware.update.tasks.CheckAndDropDateExternalForeignKeysUpdateTask());

        // Adds the 'full_time' column to the tasks tables
        list.add(new com.openexchange.groupware.update.tasks.TasksAddFulltimeColumnTask());

        // Check for possibly preset message format preference in JSLob and aligns the DB value accordingly
        list.add(new com.openexchange.groupware.update.tasks.CheckForPresetMessageFormatInJSLob());

        // +++++++++++++++++++++++++++++++++ Version 7.8.0 starts here. +++++++++++++++++++++++++++++++++

        // Adds permissions to system- and root-folders for the virtual guest group.
        list.add(new com.openexchange.groupware.update.tasks.FolderPermissionAddGuestGroup());

        // Adds the column 'guestCreatedBy' to the tables 'user' and 'del_user'
        list.add(new com.openexchange.groupware.update.tasks.UserAddGuestCreatedByTask());

        // Create table for object permissions
        list.add(new com.openexchange.groupware.update.tasks.objectpermission.ObjectPermissionCreateTableTask());

        // Extends "user" table by the (`cid`, `guestCreatedBy`) index
        list.add(new com.openexchange.groupware.update.tasks.AddGuestCreatedByIndexForUserTable());

        // Drop redundant indices
        list.add(new com.openexchange.groupware.update.tasks.DropRendundantIndicesUpdateTask());

        // Migrates the user aliases from the user_attribute table to the user_alias table; but does not delete the entries in the user_attribute table.
        list.add(new com.openexchange.groupware.update.tasks.MigrateAliasUpdateTask());

        // Grants "read all" permissions for the user infostore folder
        list.add(new com.openexchange.groupware.update.tasks.FolderPermissionReadAllForUserInfostore());

        // Add vCardId column if missing
        list.add(new com.openexchange.groupware.update.tasks.ContactAddVCardIdTask());

        // Add vCardId column for del table if missing
        list.add(new com.openexchange.groupware.update.tasks.ContactAddVCardIdToDelTask());

        // Remove accounts related to facebook
        list.add(new com.openexchange.groupware.update.tasks.RemoveFacebookAccountsTask());

        // Add primary key to dlist tables
        list.add(new MakeUUIDPrimaryForDListTablesV2());

        // Creates indexes on tables "prg_contacts" and "del_contacts" to improve auto-complete
        list.add(new com.openexchange.groupware.update.tasks.CalendarAddIndex2DatesMembersV2());

        // +++++++++++++++++++++++++++++++++ Version 7.8.1 starts here. +++++++++++++++++++++++++++++++++

        list.add(new DropVersionTableTask());

        // Checks if the 'uuid' column exists in the 'user_alias' table. If absent, adds the column and migrates all UUIDs for each alias entry
        list.add(new MigrateUUIDsForUserAliasTable());

        // Removes the aliases from the user attributes table. They are stored in the table `user_alias` with version 7.8.0
        list.add(new RemoveAliasInUserAttributesTable());

        // Create object_use_count table
        list.add(new CreateObjectUseCountTableTask());

        // Re-executes PrgLinksAddPrimaryKeyUpdateTask --> Adds primary key to `prg_links` table
        list.add(new com.openexchange.groupware.update.tasks.PrgLinksAddPrimaryKeyUpdateTaskV2());

        // Corrects values in the 'created_from' column for folders nested below/underneath personal 'Trash' folder
        list.add(new com.openexchange.groupware.update.tasks.FolderCorrectOwnerTask());

        // Checks and drops obsolete tables possibly created for managing POP3 accounts
        list.add(new com.openexchange.groupware.update.tasks.POP3CheckAndDropObsoleteTablesTaskV2());

        // (Re-)adds department index in prg_contacts for "auto-complete" queries
        list.add(new com.openexchange.groupware.update.tasks.ContactsAddDepartmentIndex4AutoCompleteSearch());

        // +++++++++++++++++++++++++++++++++ Version 7.8.2 starts here. +++++++++++++++++++++++++++++++++

        list.add(new Release781UpdateTask());

        // Adds "starttls" column to "user_mail_account" and "user_transport_account" tables and attempts to set a reasonable default value for that column dependent on mail account data
        list.add(new AddStartTLSColumnForMailAccountTablesTask());

        // Applies MEDIUM TEXT to "user_setting" table.
        list.add(new com.openexchange.groupware.update.tasks.UserSettingMediumTextTask());

        // Adds "oauth" column to "user_transport_account" table
        list.add(new AddOAuthColumnToMailAccountTableTask());

        return list.toArray(new UpdateTaskV2[list.size()]);
    }

}
