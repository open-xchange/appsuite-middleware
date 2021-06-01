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

package com.openexchange.mail;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.service.MailService;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountExceptionCodes;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.mailaccount.UnifiedInboxManagement;
import com.openexchange.quota.AccountQuota;
import com.openexchange.quota.AccountQuotas;
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

    private final MailAccountStorageService mailAccountService;

    private final MailService mailService;

    public MailQuotaProvider(MailAccountStorageService mailAccountService, MailService mailService) {
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

        if (UnifiedInboxManagement.PROTOCOL_UNIFIED_INBOX.equals(mailAccount.getMailProtocol())) {
            throw QuotaExceptionCodes.UNKNOWN_ACCOUNT.create(accountID, getModuleID());
        }

        return getForMailAccount(mailAccount, session);
    }

    @Override
    public AccountQuotas getFor(Session session) throws OXException {
        MailAccount[] mailAccounts = mailAccountService.getUserMailAccounts(session.getUserId(), session.getContextId());
        List<AccountQuota> quotas = new ArrayList<AccountQuota>(mailAccounts.length);
        List<OXException> warnings = null;
        for (MailAccount mailAccount : mailAccounts) {
            if (false == UnifiedInboxManagement.PROTOCOL_UNIFIED_INBOX.equals(mailAccount.getMailProtocol())) {
                try {
                    quotas.add(getForMailAccount(mailAccount, session));
                } catch (OXException e) {
                    if (warnings == null) {
                        warnings = new LinkedList<>();
                    }
                    warnings.add(e);
                }
            }
        }

        return new AccountQuotas(quotas, warnings);
    }

    private AccountQuota getForMailAccount(MailAccount mailAccount, Session session) throws OXException {
        MailAccess<? extends IMailFolderStorage,? extends IMailMessageStorage> mailAccess = null;
        try {
            mailAccess = mailService.getMailAccess(session, mailAccount.getId());
            mailAccess.connect();
            DefaultAccountQuota accountQuota = new DefaultAccountQuota(String.valueOf(mailAccount.getId()), mailAccount.getName());
            IMailFolderStorage folderStorage = mailAccess.getFolderStorage();
            Quota storageQuota = folderStorage.getStorageQuota("INBOX");
            if (storageQuota != null) {
                long limit = storageQuota.getLimitBytes();
                if (limit == Quota.UNLIMITED) {
                    accountQuota.addQuota(com.openexchange.quota.Quota.UNLIMITED_SIZE);
                } else {
                    accountQuota.addQuota(QuotaType.SIZE, limit, storageQuota.getUsageBytes());
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
            if (mailAccess != null) {
                mailAccess.close();
            }
        }
    }

}
