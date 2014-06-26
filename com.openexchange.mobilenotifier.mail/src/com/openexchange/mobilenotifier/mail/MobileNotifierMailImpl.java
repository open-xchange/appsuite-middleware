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

package com.openexchange.mobilenotifier.mail;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import javax.mail.internet.InternetAddress;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.service.MailService;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.mobilenotifier.AbstractMobileNotifierService;
import com.openexchange.mobilenotifier.MobileNotifierProviders;
import com.openexchange.mobilenotifier.NotifyItem;
import com.openexchange.mobilenotifier.NotifyTemplate;
import com.openexchange.mobilenotifier.utility.LocaleAndTimeZone;
import com.openexchange.mobilenotifier.utility.LocalizationUtility;
import com.openexchange.mobilenotifier.utility.MobileNotifierFileUtility;
import com.openexchange.server.ExceptionOnAbsenceServiceLookup;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;

/**
 * {@link MobileNotifierMailImpl}
 *
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 */
public class MobileNotifierMailImpl extends AbstractMobileNotifierService {

    private final ServiceLookup services;

    /**
     * Initializes a new {@link MobileNotifierMailImpl}.
     * 
     * @param services The service look-up
     */
    public MobileNotifierMailImpl(final ServiceLookup services) {
        super();
        this.services = ExceptionOnAbsenceServiceLookup.valueOf(services);
    }

    @Override
    public String getProviderName() {
        return MobileNotifierProviders.MAIL.getProviderName();
    }

    @Override
    public String getFrontendName() {
        return MobileNotifierProviders.MAIL.getFrontendName();
    }

    @Override
    public List<List<NotifyItem>> getItems(final Session session) throws OXException {
        final MailService mailService = services.getService(MailService.class);
        final List<List<NotifyItem>> notifyItems = new ArrayList<List<NotifyItem>>();
        final MailField[] requestedFields = new MailField[] {
            MailField.ID, MailField.FOLDER_ID, MailField.FROM, MailField.RECEIVED_DATE, MailField.SUBJECT, MailField.FLAGS,
            MailField.CONTENT_TYPE };
        final List<MailMessage> messages = new LinkedList<MailMessage>();

        MailAccount[] userMailAccounts;
        {
            final MailAccountStorageService mailAccountService = services.getService(MailAccountStorageService.class);
            userMailAccounts = new MailAccount[] { mailAccountService.getDefaultMailAccount(session.getUserId(), session.getContextId()) };
        }

        for (final MailAccount mailAccount : userMailAccounts) {
            MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
            try {
                mailAccess = mailService.getMailAccess(session, mailAccount.getId());
                mailAccess.connect();
                final List<MailMessage> mailMessages = Arrays.asList(mailAccess.getMessageStorage().getUnreadMessages(
                    "INBOX",
                    MailSortField.RECEIVED_DATE,
                    OrderDirection.DESC,
                    requestedFields,
                    25));
                for (MailMessage mailMessage : mailMessages) {
                    final String folderArg = mailMessage.getMailPath().getFolderArgument();
                    final List<NotifyItem> notifyItem = new ArrayList<NotifyItem>();
                    final InternetAddress[] inetAddr = mailMessage.getFrom();
                    final String subject = mailMessage.getSubject();
                    final boolean attachments = mailMessage.hasAttachment();
                    final int flag = mailMessage.getFlags();
                    final String id = mailMessage.getMailId();
                    final String teaser = getTeaser(mailMessage.getFolder(), id, mailAccess);
                    final String localizedReceivedDate = getLocalizedDateOrTime(mailMessage.getReceivedDate(), session);

                    notifyItem.add(new NotifyItem("folder", folderArg));
                    notifyItem.add(new NotifyItem("id", id));
                    notifyItem.add(new NotifyItem("from", inetAddr[0]));
                    notifyItem.add(new NotifyItem("received_date", localizedReceivedDate));
                    notifyItem.add(new NotifyItem("subject", subject));
                    notifyItem.add(new NotifyItem("attachments", attachments));
                    notifyItem.add(new NotifyItem("flags", flag));
                    notifyItem.add(new NotifyItem("teaser", teaser));
                    messages.add(mailMessage);
                    notifyItems.add(notifyItem);
                }
            } finally {
                if (mailAccess != null) {
                    mailAccess.close(true);
                }
            }
        }
        return notifyItems;
    }

    @Override
    public NotifyTemplate getTemplate() throws OXException {
        final String template = MobileNotifierFileUtility.getTemplateFileContent(MobileNotifierProviders.MAIL.getTemplateFileName());
        final String title = MobileNotifierProviders.MAIL.getTitle();
        return new NotifyTemplate(title, template, true, MobileNotifierProviders.MAIL.getIndex());
    }

    @Override
    public void putTemplate(String changedTemplate) throws OXException {
        MobileNotifierFileUtility.writeTemplateFileContent(MobileNotifierProviders.MAIL.getTemplateFileName(), changedTemplate);
    }

    /**
     * Cuts the primary mail content after 200 character, removes control character
     * 
     * @param folder The folder
     * @param mailId The id of the mail message
     * @param mailAccess The mail access
     * @return String of the mail content
     * @throws OXException
     */
    private String getTeaser(final String folder, final String mailId, final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess) throws OXException {
        final String[] idAsArr = new String[1];
        idAsArr[0] = mailId;
        final ConfigurationService configurationService = services.getService(ConfigurationService.class);
        final int cutIndex = configurationService.getIntProperty("com.openexchange.mobilenotifier.mail.maxContentSize", 200);

        final String[] message = mailAccess.getMessageStorage().getPrimaryContents(folder, idAsArr);
        String teaser = message[0];
        // removes control character
        teaser = teaser.replaceAll("[\\u0000-\\u001f]", "");
        // cut mail message if message greater than cut index
        if (teaser.length() > cutIndex) {
            teaser = message[0].substring(0, cutIndex);
        }
        return teaser;
    }

    /**
     * Converts date to localized string. If date is current date, only the time will be displayed otherwise only the date is shown
     * 
     * @param date The date which should be localized
     * @param session The session of the user
     * @return localized date string
     * @throws OXException
     */
    private String getLocalizedDateOrTime(Date date, Session session) throws OXException {
        final User user = UserStorage.getInstance().getUser(session.getUserId(), session.getContextId());
        final Locale locale = user.getLocale();
        final LocaleAndTimeZone ltz = new LocaleAndTimeZone(locale, user.getTimeZone());

        String localizedReceivedDate = LocalizationUtility.getFormattedDate(date,
            DateFormat.LONG,
            ltz.getLocale(),
            ltz.getTimeZone());
        // checks if date is current date, if true show only the time
        final Date currentDate = new Date();
        final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        if (dateFormat.format(currentDate).equals(dateFormat.format(date))) {
            localizedReceivedDate = LocalizationUtility.getFormattedTime(date, DateFormat.SHORT, ltz.getLocale(), ltz.getTimeZone());
        }
        return localizedReceivedDate;
    }
}