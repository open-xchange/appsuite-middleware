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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.admin.contextrestore.dataobjects;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@link UpdateTaskInformation} - Update task information.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UpdateTaskInformation {

    private final static String REGEX_VALUE = "([^\\),]*)";
    private final static Pattern insertIntoUpdateTaskValues = Pattern.compile("\\((?:" + REGEX_VALUE + ",)(?:" + REGEX_VALUE + ",)(?:" + REGEX_VALUE + ",)" + REGEX_VALUE + "\\)");

    private static UpdateTaskInformation searchAndCheckUpdateTask(final BufferedReader in, final UpdateTaskInformation updateTaskInformation) throws IOException {
        StringBuilder insert = null;
        String line;
        boolean eoi = false;
        while (!eoi && (line = in.readLine()) != null && !line.startsWith("--")) {
            if (null != insert) {
                insert.append(line);
                if (line.endsWith(");")) {
                    eoi = true;
                }
            } else {
                if (line.startsWith("INSERT INTO `updateTask` VALUES ")) {
                    // Start collecting lines
                    insert = new StringBuilder(2048);
                    insert.append(line);
                }
            }
        }
        if (null != insert) {
            final Matcher matcher = insertIntoUpdateTaskValues.matcher(insert.substring(32));
            insert = null;
            while (matcher.find()) {
                final UpdateTaskEntry updateTaskEntry = new UpdateTaskEntry();
                updateTaskEntry.setContextId(Integer.parseInt(matcher.group(1)));
                updateTaskEntry.setTaskName(matcher.group(2));
                updateTaskEntry.setSuccessful((Integer.parseInt(matcher.group(3)) > 0));
                updateTaskEntry.setLastModified(Long.parseLong(matcher.group(4)));
                updateTaskInformation.add(updateTaskEntry);
            }
        }
        return updateTaskInformation;
    }

    public static void main(String[] args) throws Exception {
        String s = "INSERT INTO `updateTask` VALUES (0,'com.openexchange.groupware.calendar.update.AlterChangeExceptionFieldLength',1,1261475506251),(0,'com.openexchange.groupware.update.tasks.FolderAddPermColumnUpdateTask',1,1261475506252),(0,'com.openexchange.groupware.calendar.update.AlterDeleteExceptionFieldLength',1,1261475506253),(0,'com.openexchange.groupware.update.tasks.CreateReplicationTableTask',1,1261475506254),(0,'com.openexchange.groupware.update.tasks.UnboundFolderReplacementUpdateTask',1,1261475506255),(0,'com.openexchange.groupware.update.tasks.MailAccountMigrationTask',1,1261475506256),(0,'com.openexchange.groupware.update.tasks.ContactsChangedFromUpdateTask',1,1261475506257),(0,'com.openexchange.groupware.update.tasks.EnlargeTaskTitle',1,1261475506258),(0,'com.openexchange.groupware.calendar.update.AlterMailAddressLength',1,1261475506260),(0,'com.openexchange.groupware.update.tasks.CreateSubscribeTableTask',1,1261475506261),(0,'com.openexchange.groupware.update.tasks.AlterUidCollation',1,1261475506262),(0,'com.openexchange.groupware.update.tasks.CorrectCharsetAndCollationTask',1,1261475506263),(0,'com.openexchange.groupware.update.tasks.ContactCollectOnIncomingAndOutgoingMailUpdateTask',1,1261475506265),(0,'com.openexchange.groupware.update.tasks.AppointmentRepairRecurrenceString',1,1261475506266),(0,'com.openexchange.groupware.update.tasks.AppointmentChangedFromZeroTask',1,1261475506268),(0,'com.openexchange.groupware.update.tasks.SpamUpdateTask',1,1261475506269),(0,'com.openexchange.groupware.update.tasks.InfostoreLongerURLFieldTask',1,1261475506270),(0,'com.openexchange.groupware.update.tasks.GlobalAddressBookPermissionsResolverTask',1,1261475506272),(0,'com.openexchange.groupware.calendar.update.AlterCreatingDate',1,1261475506274),(0,'com.openexchange.groupware.update.tasks.AppointmentRepairRecurrenceDatePosition',1,1261475506275),(0,'com.openexchange.groupware.update.tasks.RenameGroupTask',1,1261475506277),(0,'com.openexchange.groupware.update.tasks.CorrectIndexes',1,1261475506280),(0,'com.openexchange.groupware.update.tasks.MALPollCreateTableTask',1,1261475506282),(0,'com.openexchange.groupware.update.tasks.ClearLeftoverAttachmentsUpdateTask',1,1261475506289),(0,'com.openexchange.groupware.update.tasks.CreatePublicationTablesTask',1,1261475506291),(0,'com.openexchange.groupware.update.tasks.AddAppointmentParticipantsIndexTask',1,1261475506292),(0,'com.openexchange.groupware.update.tasks.MailAccountCreateTablesTask',1,1261475506294),(0,'com.openexchange.groupware.update.tasks.ClearOrphanedInfostoreDocuments',1,1261475506296),(0,'com.openexchange.groupware.update.tasks.RemoveBrokenReminder',1,1261475506298),(0,'com.openexchange.groupware.update.tasks.CorrectIndexes6_10',1,1261475506300),(0,'com.openexchange.groupware.calendar.update.UpdateFolderIdInReminder',1,1261475506301),(0,'com.openexchange.groupware.update.tasks.InfostoreRenamePersonalInfostoreFolders',1,1261475506303),(0,'com.openexchange.groupware.update.tasks.TaskCreateUserSettingServer',1,1262709696079),(0,'com.openexchange.groupware.update.tasks.SpellCheckUserDictTableTask',1,1261475506307),(0,'com.openexchange.groupware.update.tasks.ContactsRepairLinksAttachments',1,1261475506309),(0,'com.openexchange.groupware.update.tasks.MailAccountAddPersonalTask',1,1261475506311),(0,'com.openexchange.groupware.update.tasks.CorrectWrongAppointmentFolder',1,1261475506313),(0,'com.openexchange.groupware.update.tasks.FolderAddIndex4SharedFolderSearch',1,1261475506315),(0,'com.openexchange.groupware.update.tasks.MailUploadQuotaUpdateTask',1,1261475506317),(0,'com.openexchange.groupware.update.tasks.TaskModifiedByNotNull',1,1261475506319),(0,'com.openexchange.groupware.update.tasks.InfostoreResolveFolderNameCollisions',1,1261475506321),(0,'com.openexchange.groupware.update.tasks.ContactsRepairLinksAttachments2',1,1261475506324),(0,'com.openexchange.groupware.update.tasks.DefaultConfirmStatusUpdateTask',1,1261475506326),(0,'com.openexchange.groupware.update.tasks.ContactsAddIndex4AutoCompleteSearch',1,1275895321071),(0,'com.openexchange.groupware.update.tasks.ContactsAddUseCountColumnUpdateTask',1,1261475506330),(0,'com.openexchange.groupware.update.tasks.DuplicateContactCollectFolderRemoverTask',1,1261475506332),(0,'com.openexchange.groupware.update.tasks.ContactsGlobalMoveUpdateTask',1,1261475506334),(0,'com.openexchange.folderstorage.virtual.VirtualTreeCreateTableTask',1,1261475506336),(0,'com.openexchange.groupware.update.tasks.AlterUidCollation2',1,1261475506339),(0,'com.openexchange.groupware.update.tasks.NewAdminExtensionsUpdateTask',1,1261475506341),(0,'com.openexchange.groupware.update.tasks.CreateGenconfTablesTask',1,1261475506343),(0,'com.openexchange.groupware.update.tasks.DelFolderTreeTableUpdateTask',1,1261475506346),(0,'com.openexchange.groupware.update.tasks.NewInfostoreFolderTreeUpdateTask',1,1261475506348),(0,'com.openexchange.groupware.update.tasks.POP3CreateTableTask',1,1261475506350),(0,'com.openexchange.groupware.update.tasks.TaskReminderFolderZero',1,1261475506352),(0,'com.openexchange.groupware.update.tasks.PasswordMechUpdateTask',1,1261475506355),(0,'com.openexchange.groupware.update.tasks.ContactsFieldSizeUpdateTask',1,1261475506357),(0,'com.openexchange.groupware.update.tasks.CalendarExtendDNColumnTask',1,1261475506359),(0,'com.openexchange.groupware.update.tasks.AppointmentExceptionRemoveDuplicateDatePosition',1,1261475506363),(0,'com.openexchange.groupware.update.tasks.RemoveAdminPermissionOnInfostoreTask',1,1261475506365),(0,'com.openexchange.groupware.calendar.update.RepairRecurrencePatternNullValue',1,1261475506367),(0,'com.openexchange.groupware.update.tasks.CreateTableVersion',1,1261475506370),(0,'com.openexchange.groupware.update.tasks.LastVersionedUpdateTask',1,1261475506383),(0,'com.openexchange.groupware.update.tasks.HeaderCacheCreateTableTask',1,1263380362075),(0,'com.openexchange.groupware.update.tasks.MALPollModifyTableTask',0,1263926266436),(0,'com.openexchange.messaging.generic.groupware.MessagingGenericCreateTableTask',1,1264425281393),(0,'com.openexchange.groupware.update.tasks.ExtendCalendarForIMIPHandlingTask',1,1266312063286),(0,'com.openexchange.push.malpoll.MALPollCreateTableTask',1,1267017031262),(0,'com.openexchange.push.malpoll.MALPollModifyTableTask',1,1267017031600),(0,'com.openexchange.groupware.update.tasks.ContactCollectorReEnabler',1,1267520506609),(0,'com.openexchange.groupware.infostore.database.impl.InfostoreFilenameReservationsCreateTableTask',1,1268323254217),(0,'com.openexchange.usm.database.ox.USMTablesUpdateTask',1,1269858829673),(0,'com.openexchange.usm.database.ox.USMClearTablesUpdateTask',1,1269858829713),(0,'com.openexchange.usm.database.ox.USMUUIDTablesUpdateTask',1,1269858829741),(0,'com.openexchange.usm.database.ox.USMClearTablesUpdateTask93',1,1269858829769),(0,'com.openexchange.groupware.update.tasks.FolderTreeSelectionTask',1,1271066670392),(0,'com.openexchange.publish.database.PublicationWithUsernameAndPasswordUpdateTask',1,1272280215357),(0,'com.openexchange.groupware.update.tasks.AttachmentCountUpdateTask',1,1277390407512),(0,'com.openexchange.groupware.update.tasks.AddInitialFilestoreUsage',1,1278594057681),(0,'com.openexchange.groupware.update.tasks.AddFileAsForUserContacts',1,1283411255129),(0,'com.openexchange.subscribe.database.EnabledColumn',1,1283411308360),(0,'com.openexchange.publish.database.EnabledColumn',1,1279534389409),(0,'com.openexchange.groupware.update.tasks.ParticipantCommentFieldLength',1,1279627483841),(0,'com.openexchange.groupware.update.tasks.AggregatingContactTableService',1,1280734661735),(0,'com.openexchange.publish.database.FixPublicationTablePrimaryKey',1,1283339531120),(0,'com.openexchange.publish.database.PublicationWithUsernameAndPasswordUpdateTaskRetry',1,1283339531586),(0,'com.openexchange.subscribe.database.FixSubscriptionTablePrimaryKey',1,1283410903610),(0,'com.openexchange.publish.database.PublicationUsersCreatedAndLastModifiedColumn',1,1283410904191),(0,'com.openexchange.subscribe.database.SubscriptionsCreatedAndLastModifiedColumn',1,1283410904940),(0,'com.openexchange.publish.database.PublicationsCreatedAndLastModifiedColumn',1,1283411235176),(0,'com.openexchange.file.storage.generic.groupware.FileStorageGenericCreateTableTask',1,1285599478023),(0,'com.openexchange.usm.database.ox.update.ChangeCollationUpdateTask',1,1286532257674),(0,'com.openexchange.usm.database.ox.update.USMTablesUpdateTaskV2',1,1290703533843),(0,'com.openexchange.usm.database.ox.update.USMUUIDTablesUpdateTaskV2',1,1290703533939),(0,'com.openexchange.usm.database.ox.update.USMClearTablesUpdateTaskV2',1,1290703534067),(0,'com.openexchange.oauth.internal.groupware.OAuthCreateTableTask',1,1294046949279),(0,'com.openexchange.groupware.update.tasks.IDCreateTableTask',1,1294047336162),(0,'com.openexchange.groupware.contexts.impl.sql.ContextAttributeTableUpdateTask',1,1295373115925),(0,'com.openexchange.groupware.update.tasks.ContactInfoField2Text',1,1296480597816),(0,'com.openexchange.groupware.update.tasks.ContactFieldsForJapaneseKanaSearch',1,1296480623991),(0,'com.openexchange.messaging.facebook.groupware.FacebookDropObsoleteAccountsTask',1,1298374157067),(0,'com.openexchange.groupware.update.tasks.SubscriptionRemoverTask',0,1298458163072),(0,'com.openexchange.frontend.uwa.internal.groupware.CreateWidgetTableTask',1,1298465723030),(0,'com.openexchange.frontend.uwa.internal.groupware.CreatePositionsTableTask',1,1298465723436),(0,'com.openexchange.groupware.update.tasks.FacebookCrawlerSubscriptionRemoverTask',1,1298552307502),(0,'com.openexchange.groupware.update.tasks.LinkedInCrawlerSubscriptionsRemoverTask',1,1298552330520),(0,'com.openexchange.groupware.update.tasks.AllowTextInValuesOfDynamicContextAttributesTask',1,1301932784962),(0,'com.openexchange.groupware.update.tasks.AllowTextInValuesOfDynamicUserAttributesTask',1,1301932786994),(0,'com.openexchange.groupware.update.tasks.CreateIndexOnContextAttributesTask',1,1301932787623),(0,'com.openexchange.groupware.update.tasks.CreateIndexOnUserAttributesForAliasLookupTask',1,1301932788753),(0,'com.openexchange.oauth.internal.groupware.OAuthCreateTableTask2',1,1303299234744),(0,'com.openexchange.subscribe.yahoo.update.DeleteOldYahooSubscriptions',1,1308655837255),(0,'com.openexchange.groupware.update.tasks.DeleteOldYahooSubscriptions',1,1308729823095),(0,'com.openexchange.groupware.update.tasks.CorrectAttachmentCountInAppointments',1,1310561956315),(0,'com.openexchange.groupware.update.tasks.CorrectOrganizerInAppointments',1,1311168286240),(0,'com.openexchange.mail.smal.internal.tasks.SMALCreateTableTask',1,1313757652161),(0,'com.openexchange.appstore.internal.CreateAppStoreTables',1,1314018322482),(0,'com.openexchange.mail.smal.internal.tasks.SMALCheckTableTask',1,1314107556159),(0,'com.openexchange.groupware.update.tasks.CorrectFileAsInContacts',1,1315826922471),(0,'com.openexchange.groupware.update.tasks.VirtualFolderAddSortNumTask',1,1316449978011),(0,'com.openexchange.chat.db.groupware.DBChatCreateTableTask',1,1323425828619),(0,'com.openexchange.groupware.update.tasks.DropIndividualUserPermissionsOnPublicFolderTask',1,1324028313232),(0,'com.openexchange.groupware.update.tasks.ContactAddOutlookAddressFieldsTask',1,1323877135069),(0,'com.openexchange.groupware.update.tasks.ContactAddUIDFieldTask',1,1324300138115),(0,'com.openexchange.groupware.update.tasks.TasksAddUidColumnTask',1,1325496510306),(0,'com.openexchange.groupware.update.tasks.CalendarAddUIDIndexTask',1,1324642423203),(0,'com.openexchange.groupware.update.tasks.ContactAddUIDValueTask',1,1325495324620),(0,'com.openexchange.groupware.update.tasks.DropFKTask',1,1k',1,1325496510306),(0,'com.openexchange.groupware.update.tasks.CalendarAddUIDIndexTask',1,1324642423203),(0,'com.openexchang\n" +
        		"e.groupware.update.tasks.ContactAddUIDValueTask',1,1325495324620),(0,'com.openexchange.groupware.update.tasks.DropFKTask',1,1\n" +
        		"326128153186),(0,'com.openexchange.groupware.update.tasks.AppointmentAddOrganizerIdPrincipalPrincipalIdColumnsTask',1,1326805614941),(0,'com.openexchange.groupware.update.tasks.CalendarAddIndex2DatesMembers',1,1326818534281),(0,'com.openexchange.groupware.update.tasks.FolderAddIndex2LastModified',1,1327080610436),(0,'com.openexchange.jslob.storage.db.groupware.DBJSlobCreateTableTask',1,1328792629133),(0,'com.openexchange.groupware.update.tasks.CheckForPublicInfostoreFolderTask',1,1329469734340),(0,'com.openexchange.groupware.update.tasks.MailAccountFixReplyToMessupTask',1,1330513051506),(0,'com.openexchange.groupware.update.tasks.MailAccountAddReplyToTask',1,1330701062876),(0,'com.openexchange.groupware.update.tasks.MailAccountMigrateReplyToTask',1,1330701062996),(0,'com.openexchange.groupware.update.tasks.MALPollDropConstraintsTask',1,1331132513492),(0,'com.openexchange.mail.smal.impl.internal.tasks.SMALCreateTableTask',1,1332840164346),(0,'com.openexchange.mail.smal.impl.internal.tasks.SMALCheckTableTask',1,1332840164378),(0,'com.openexchange.groupware.update.tasks.CalendarExtendDNColumnTaskV2',1,1334929241408),(0,'com.openexchange.oauth.provider.groupware.OAuthProviderCreateTableTask',1,1337344130102),(0,'com.openexchange.oauth.provider.groupware.OAuth2ProviderCreateTableTask',1,1337689996972),(0,'com.openexchange.json.cache.impl.osgi.JsonCacheCreateTableTask',1,1337794102662),(0,'com.openexchange.json.cache.impl.osgi.JsonCacheAddInProgressFieldTask',1,1337856933270),(0,'com.openexchange.json.cache.impl.osgi.JsonCacheMediumTextTask',1,1337876977699),(0,'com.openexchange.groupware.update.tasks.DListAddIndexForLookup',1,1338800908099),(0,'com.openexchange.groupware.update.tasks.AppointmentAddFilenameColumnTask',1,1339682828670),(0,'com.openexchange.groupware.update.tasks.UnifiedINBOXRenamerTask',1,1261475506508),(0,'com.openexchange.contact.storage.rdb.sql.AddFilenameColumnTask',1,1340021931290),(0,'com.openexchange.groupware.update.tasks.UnifiedInboxRenamerTask',1,1340021931683),(0,'com.openexchange.json.cache.impl.osgi.JsonCacheAddOtherFieldsTask',1,1340091921485),(0,'com.openexchange.groupware.update.tasks.TasksAddFilenameColumnTask',1,1341563176214),(0,'com.openexchange.contact.storage.rdb.sql.CorrectNumberOfImagesTask',1,1342697504903),(0,'com.openexchange.groupware.update.tasks.AppointmentAddOrganizerIdPrincipalPrincipalIdColumnsTask2',1,1343116824159),(0,'com.openexchange.groupware.update.tasks.CalendarAddUIDValueTask',1,1343836345033),(0,'com.openexchange.contact.storage.ldap.database.LdapCreateTableTask',1,1343974277524),(0,'com.openexchange.snippet.rdb.groupware.RdbSnippetCreateTableTask',1,1346240888936),(0,'com.openexchange.snippet.mime.groupware.MimeSnippetCreateTableTask',1,1346316975593),(0,'com.openexchange.file.storage.rdb.groupware.FileStorageRdbCreateTableTask',1,1347968196359),(0,'com.openexchange.groupware.update.tasks.RemoveUnnecessaryIndexes',1,1348567521646),(0,'com.openexchange.realtime.presence.subscribe.database.CreatePresenceSubscriptionDB',1,1348585084303),(0,'com.openexchange.groupware.update.tasks.RemoveUnnecessaryIndexes2',1,1348737000552);";


        UpdateTaskInformation uti = searchAndCheckUpdateTask(new BufferedReader(new StringReader(s)), new UpdateTaskInformation());

        System.out.println(uti);

    }

    private final Map<String, List<UpdateTaskEntry>> entries;

    /**
     * Initializes a new {@link UpdateTaskInformation}.
     */
    public UpdateTaskInformation() {
        super();
        entries = new HashMap<String, List<UpdateTaskEntry>>();
    }

    /**
     * Initializes a new {@link UpdateTaskInformation}.
     */
    public UpdateTaskInformation(final int capacity) {
        super();
        entries = new HashMap<String, List<UpdateTaskEntry>>(capacity);
    }

    /**
     * Gets the size of the update task collection.
     *
     * @return The size
     */
    public int size() {
        return entries.size();
    }

    /**
     * Checks whether the update task collection is empty.
     *
     * @return <code>true</code> if empty; otherwise <code>false</code>
     */
    public boolean isEmpty() {
        return entries.isEmpty();
    }

    /**
     * Checks if the update task collection contains specified element.
     *
     * @param e The element possibly contained
     * @return <code>true</code> if contained; otherwise <code>false</code>
     */
    public boolean contains(final UpdateTaskEntry e) {
        return entries.containsKey(e.getTaskName());
    }

    /**
     * Adds specified element.
     *
     * @param e The element to add
     */
    public void add(final UpdateTaskEntry e) {
        if (null == e) {
            return;
        }
        final String taskName = e.getTaskName();
        List<UpdateTaskEntry> list = entries.get(taskName);
        if (null == list) {
            list = new LinkedList<UpdateTaskEntry>();
            entries.put(taskName, list);
        }
        list.add(e);
    }

    /**
     * Removes specified element.
     *
     * @param e The element to remove
     */
    public void remove(final UpdateTaskEntry e) {
        if (null == e) {
            return;
        }
        final String taskName = e.getTaskName();
        List<UpdateTaskEntry> list = entries.get(taskName);
        if (null != list && list.remove(e) && list.isEmpty()) {
            entries.remove(taskName);
        }
    }

    /**
     * Clears the update task collection.
     */
    public void clear() {
        entries.clear();
    }

    /**
     * Checks equality to given update task collection.
     * 
     * @param other The other update task collection.
     * @return <code>true</code> if equal; otherwise <code>false</code>
     */
    public boolean equalTo(final UpdateTaskInformation other) {
        if (null == other) {
            return false;
        }
        final Map<String, List<UpdateTaskEntry>> m1 = this.entries;
        final Map<String, List<UpdateTaskEntry>> m2 = other.entries;
        // Check by size
        {
            final int size1 = m1.size();
            final int size2 = m2.size();
            if (size1 != size2) {
                return false;
            }
        }
        for (final Entry<String, List<UpdateTaskEntry>> entry : m1.entrySet()) {
            final String taskName = entry.getKey();
            final List<UpdateTaskEntry> list2 = m2.get(taskName);
            if (null == list2) {
                return false;
            }
            final List<UpdateTaskEntry> list1 = entry.getValue();
            if (list1.size() != list2.size()) {
                return false;
            }
            for (final UpdateTaskEntry updateTaskEntry : list1) {
                if (!contained(updateTaskEntry, list1)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean contained(final UpdateTaskEntry e, final List<UpdateTaskEntry> list) {
        final String taskName = e.getTaskName();
        final boolean successful = e.isSuccessful();
        for (final UpdateTaskEntry cur : list) {
            if (taskName.equals(cur.getTaskName()) && successful == cur.isSuccessful()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return entries.toString();
    }

}
