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

package com.openexchange.ajax.config.actions;

/**
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public enum Tree {

    /** A string containing GUI-specific settings (currently, it is a huge JSON object). */
    GUI("/gui"),

    /** [inside GUI] "infostore/split" or "infostore/list" */
    InfostoreView("/gui/infostore/view"),
    
    /** [inside GUI] "true" or "false" */
    MailNewMailOptionsBccEnabled("/gui/mail/newmail_options/bcc"),
    
    /** [inside GUI] "true" or "false" */
    MailNewMailOptionsFromEnabled("/gui/mail/newmail_options/from"),
    
    /** [inside GUI] "true" or "false" */
    MailNewMailOptionsCcEnabled("/gui/mail/newmail_options/cc"),
    
    /** [inside GUI] "true" or "false" */
    MailNewMailOptionsOptionsEnabled("/gui/mail/newmail_options/options"),
    
    /** [inside GUI] "true" or "false" */
    MailAllowHtmlMails("/gui/mail/htmlmessage"),
    
    /** [inside GUI] json array */
    //TODO: [{"position":"below","signature_text":"wefwefwe","signature_name":"efwefwef","signature_default":true}],
    //MailSignatures("/gui/mail/signatures"),
    
    /** [inside GUI] "mail/list/unthreaded" or ? */
    MailViewSpam("/gui/mail/view_spam"),
    
    /** [inside GUI] "mail/hsplit/unthreaded" or ? */
    MailView("/gui/mail/view"),
    
    /** [inside GUI] "true" or "false" */
    MailAutoComplete("/gui/mail/autocomplete"),
    
    /** [inside GUI] "TEXT/PLAIN" or "TEXT/HTML" or "ALTERNATIVE" */
    MailFormatMessages("/gui/mail/formatmessage"),
    
    /** [inside GUI] "true" or "false" */
    MailFullHeader("/gui/mail/fullmailheader"),

    /** [inside GUI] "true" or "false" */
    MailNotifyOnReadAcknowledgment("/gui/mail/notifyacknoledge"),
    
    //TODO: Portal settings
    
    /** [inside GUI] string array */
    PrivateCategories("/gui/private_categories"),
    
    /** [inside GUI] "true" or "false" */
    EnableConfirmationPopup("/gui/global/confirmpopup"),
    
    /** [inside GUI] "1" (means "Yes") or "0" (means "No") or "2" (means "Ask") */
    SaveConfigOnLogout("/gui/global/save"),
    
    /** [inside GUI] */
    AutoRefresh("/gui/global/autorefresh"),
    
    /** [inside GUI] */
    PanelRows("/gui/menu/menuiteration"),
    
    /** [inside GUI] */
    ThemeID("/gui/theme/id"),
    
    /** [inside GUI] */
    ThemeName("/gui/theme/name"),
    
    /** [inside GUI] */
    ThemePath("/gui/theme/path"),
    
    //TODO: effects
    
    /** [inside GUI] */
    TasksReminderInterval("/gui/tasks/interval"),
    
    /** [inside GUI] "asc" or ? */
    TasksGridSort("/gui/tasks/gridsort"),
    
    /** [inside GUI] "tasks/split" or "tasks/list" */
    TasksView("/gui/tasks/view"),
    
    //TODO: FolderTreeState
    
    /** [inside GUI] "asc" or ? */
    ContactsGridSort("/gui/contacts/gridsort"),
    
    /** [inside GUI] "auto" or ? */
    ContactsCardsPerColumn("/gui/contacts/cardsToViewPerColumn"),
    
    /** [inside GUI] "contacts/cards" or ? */
    ContactsView("/gui/contacts/view"),
    
    /** [inside GUI] */
    CalendarEndWorkingTime("/gui/calendar/endtime"),
    
    /** [inside GUI] "true" or "false" */
    CalendarShowAppointmetsOfAllFolders("/gui/calendar/allfolders"),
    
    /** [inside GUI] */
    CalendarStartWorkingTime("/gui/calendar/starttime"),
    
    /** [inside GUI] */
    CalendarInterval("/gui/calendar/interval"),
    
    /** [inside GUI] "calendar/calendar/day" or ? */
    CalendarView("/gui/calendar/view"),
    
    //TODO: more gui settings
    
    
    /** A string containing GUI-specific settings. This is a JSON object that must be kept small for performance. */
    FastGUI("/fastgui"),
    
    /** the unique identifier of the context (read-only, added 2008-01-28). */
    ContextID("/context_id"),

    /** the unique identifier of the user (read-only). */
    Identifier("/identifier"),
    
    /** the unique identifier of the contact data of the user (read-only). */
    ContactID("/contact_id"),
    
    /** the configured language of the user. */
    Language("/language"),

    /** the configured timezone of the user. */
    TimeZone("/timezone"),

    /** send a mail notification for appointments */
    CalendarNotification("/calendarnotification"),
    
    /** send a mail notification for tasks */
    TaskNotification("/tasknotification"),
    
    /** Selectable times for GUI reload */
    ReloadTimes("/reloadTimes"),
    
    /** Version string of the server. */
    ServerVersion("/serverVersion"),
    
    /** User timezone specific long of the current server time. */
    CurrentTime("/currentTime"),
    
    /** Timeout after that idle uploads are deleted. */
    MaxUploadIdleTimeout("/maxUploadIdleTimeout"),

    /** the standard task folder (read-only) */
    PrivateTaskFolder("/folder/tasks"),

    /** the standard calendar folder (read-only) */
    PrivateAppointmentFolder("/folder/calendar"),
    
    /** the standard contacts folder (read-only) */
    PrivateContactFolder("/folder/contacts"),
    
    /** the private infostore folder (read-only) */
    PrivateInfostoreFolder("/folder/infostore"),
    
    /** If external participants without email should be shown. */
    ShowWithoutEmail("/participants/showWithoutEmail"),
    
    /** Enables participant selection dialog for appointments and tasks. (read-only, added 2008-04-30/SP4) */
    ShowParticipantDialog("/participants/showDialog"),

    /** all email addresses of the user including the primary address (read-only, added 2008-02-25) */
    MailAddresses("/modules//mail/addresses"),

    /** (added 2008-02-25) */
    AppendMailText("/modules/mail/appendmailtext"),
    
    /** Alters default setting whether external images contained in HTML content are allowed or not (added 2008-05-27) */
    AllowHtmlImages("/modules/mail/allowhtmlimages"),
    
    /** color quoted lines (added 2008-02-25) */
    ColorQuoted("/modules/mail/colorquoted"),
    
    /** primary email address of the user (read-only, added 2008-02-25) */
    DefaultAddress("/modules/mail/defaultaddress"),
    
    /** delete emails or move to trash (added 2008-02-25) */
    DeleteMail("/modules/mail/deletemail"),
    
    /** display emoticons as graphics (added 2008-02-25) */
    Emoticons("/modules/mail/emoticons"),
    
    /** identifier of the folder with the mail drafts (read-only, added 2008-02-25) */
    DraftsFolder("/modules/mail/defaultFolder/drafts"),
    
    /** identifier of the folder that gets all incoming mails (read-only, added 2008-02-25) */
    InboxFolder("/modules/mail/defaultFolder/inbox"),
    
    /** identifier of the folder with the sent mails (read-only, added 2008-02-25) */
    SentFolder("/modules/mail/defaultFolder/sent"),
    
    /** identifier of the folder with the spam mails (read-only, added 2008-02-25) */
    SpamFolder("/modules/mail/defaultFolder/spam"),
    
    /** identifier of the folder with the deleted mails (read-only, added 2008-02-25) */
    TrashFolder("/modules/mail/defaultFolder/trash"),
    
    /** forward messages as inline or attachment (added 2008-02-25) */
    ForwardMessage("/modules/mail/forwardmessage"),
    
    /** activate inlining of HTML attachments (added 2008-02-25) */
    InlineAttachments("/modules/mail/forwardmessage/inlineattachments"),
    
    /** (added 2008-02-25) */
    LineWrap("/modules/mail/linewrap"),
    
    /** if mail module is enabled or not (added 2008-02-25) */
    MailEnabled("/modules/mail/module"),
    
    /** one email address out of the addresses list that are email sent with (added 2008-02-25) */
    SendAddress("/modules//mail/sendaddress"),
    
    /** Spam Button should be displayed in GUI or not (added 2008-02-25) */
    SpamButton("/modules/mail/spambutton"),
    
    /** attach vcard when sending mails (added 2008-02-25) */
    AppendVcard("/modules/mail/vcard"),
    
    /** header(s) identifying phishing headers (added 2008-05-27) */
    PhishingHeaders("/modules/mail/phishingheaders"),
    
    /** */
    CalendarEnabled("/modules/calendar/module"),
    
    /** */
    CalendarConflict("/modules/calendar/calendar_conflict"),
    
    /** */
    CalendarFreeBusy("/modules/calendar/calendar_freebusy"),

    /** */
    CalendarTeamview("/modules/calendar/calendar_teamview"),
    
    /** */
    ContactsEnabled("/modules/contacts/module"),
    
    /** */
    TasksEnabled("/modules/tasks/module"),
    
    /** */
    DelegateTasks("/modules/tasks/delegate_tasks"),
    
    /** */
    InfostoreEnabled("/modules/infostore/module"),
    
    /** */
    ICal("/modules/interfaces/ical"),
    
    /** */
    VCard("/modules/interfaces/vcard"),
    
    /** */
    SyncML("/modules/interfaces/syncml"),
    
    /** */
    PublicFolders("/modules/folder/public_folders"),
    
    /** */
    ReadCreateSharedFolders("/modules/folder/read_create_shared_folders"),
    
    /** Extras link in the configuration (read only, added 2008-04-29) */
    Extras("/modules/com.openexchange.extras/module"),
    
    AvailableModules("/availableModules"),

    MailFilter("/modules/mailfilter"),
    ;

    private final String path;

    /**
     * Default constructor.
     */
    private Tree(final String path) {
        this.path = path;
    }

    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }
}
