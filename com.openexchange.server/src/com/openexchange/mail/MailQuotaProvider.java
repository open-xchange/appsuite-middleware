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

package com.openexchange.mail;

import java.util.LinkedList;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.service.MailService;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountExceptionCodes;
import com.openexchange.mailaccount.MailAccountFacade;
import com.openexchange.quota.AccountQuota;
import com.openexchange.quota.DefaultAccountQuota;
import com.openexchange.quota.QuotaExceptionCodes;
import com.openexchange.quota.QuotaProvider;
import com.openexchange.quota.QuotaType;
import com.openexchange.session.Session;


/**
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class MailQuotaProvider implements QuotaProvider {

    private final MailAccountFacade mailAccountService;

    private final MailService mailService;

    public MailQuotaProvider(MailAccountFacade mailAccountService, MailService mailService) {
        super();
        this.mailAccountService = mailAccountService;
        this.mailService = mailService;
    }

    @Override
    public String getModuleID() {
        return "mail";
    }

    @Override
    public String getDisplayName() {
        return "E-Mail";
    }

    @Override
    public AccountQuota getFor(Session session, String accountID) throws OXException {
        MailAccount mailAccount;
        try {
            mailAccount = mailAccountService.getMailAccount(Integer.parseInt(accountID), session.getUserId(), session.getContextId());
        } catch (OXException e) {
            if (MailAccountExceptionCodes.NOT_FOUND.equals(e)) {
                throw QuotaExceptionCodes.UNKNOWN_ACCOUNT.create(accountID, getModuleID());
            }

            throw e;
        } catch (NumberFormatException e) {
            throw QuotaExceptionCodes.UNKNOWN_ACCOUNT.create(accountID, getModuleID());
        }

        return getForMailAccount(mailAccount, session);
    }

    @Override
    public List<AccountQuota> getFor(Session session) throws OXException {
        List<AccountQuota> quotas = new LinkedList<AccountQuota>();
        MailAccount[] mailAccounts = mailAccountService.getUserMailAccounts(session.getUserId(), session.getContextId());
        for (MailAccount mailAccount : mailAccounts) {
            quotas.add(getForMailAccount(mailAccount, session));
        }

        return quotas;
    }

    private AccountQuota getForMailAccount(MailAccount mailAccount, Session session) throws OXException {
        MailAccess<? extends IMailFolderStorage,? extends IMailMessageStorage> mailAccess = mailService.getMailAccess(session, mailAccount.getId());
        mailAccess.connect();
        try {
            DefaultAccountQuota accountQuota = new DefaultAccountQuota(String.valueOf(mailAccount.getId()), mailAccount.getName());
            IMailFolderStorage folderStorage = mailAccess.getFolderStorage();
            Quota storageQuota = folderStorage.getStorageQuota("INBOX");
            if (storageQuota != null) {
                long limit = storageQuota.getLimit();
                if (limit == Quota.UNLIMITED) {
                    accountQuota.addQuota(com.openexchange.quota.Quota.UNLIMITED_SIZE);
                } else {
                    accountQuota.addQuota(QuotaType.SIZE, limit, storageQuota.getUsage());
                }
            }

            Quota messageQuota = folderStorage.getMessageQuota("INBOX");
            if (messageQuota != null) {
                long limit = messageQuota.getLimit();
                if (limit == Quota.UNLIMITED) {
                    accountQuota.addQuota(com.openexchange.quota.Quota.UNLIMITED_AMOUNT);
                } else {
                    accountQuota.addQuota(QuotaType.AMOUNT, limit, messageQuota.getUsage());
                }
            }

            return accountQuota;
        } finally {
            mailAccess.close();
        }
    }

}
