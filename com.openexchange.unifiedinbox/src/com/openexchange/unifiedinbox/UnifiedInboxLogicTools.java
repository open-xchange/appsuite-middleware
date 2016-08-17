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

package com.openexchange.unifiedinbox;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import com.openexchange.exception.OXException;
import com.openexchange.mail.FullnameArgument;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.api.MailLogicTools;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.mailaccount.UnifiedInboxUID;
import com.openexchange.session.Session;
import com.openexchange.unifiedinbox.utility.UnifiedInboxUtility;


/**
 * {@link UnifiedInboxLogicTools}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UnifiedInboxLogicTools extends MailLogicTools {

    /**
     * Initializes a new {@link UnifiedInboxLogicTools}.
     *
     * @param session The associated session
     * @param accountId The account identifier
     */
    public UnifiedInboxLogicTools(Session session, int accountId) {
        super(session, accountId);
    }

    private UserSettingMail getUserSettingMail() throws OXException {
        return UserSettingMailStorage.getInstance().getUserSettingMail(session);
    }

    @Override
    public MailMessage getFowardMessage(MailMessage[] originalMails, boolean setFrom) throws OXException {
        return getFowardMessage(originalMails, getUserSettingMail(), setFrom);
    }

    @Override
    public MailMessage getFowardMessage(MailMessage[] originalMails, UserSettingMail usm, boolean setFrom) throws OXException {
        MailMessage mail = super.getFowardMessage(originalMails, usm, setFrom);
        try {
            TIntList accountIds = new TIntArrayList(originalMails.length);
            UnifiedInboxUID uid = new UnifiedInboxUID();
            for (MailMessage originalMail : originalMails) {
                uid.setUIDString(originalMail.getMailId());
                int aid = uid.getAccountId();
                if (!accountIds.contains(aid)) {
                    accountIds.add(aid);
                }
            }
            if (1 == accountIds.size()) {
                mail.setAccountId(accountIds.get(0));
            }

            MailPath msgref = mail.getMsgref();
            if (null != msgref && accountId == msgref.getAccountId()) {
                uid.setUIDString(msgref.toString());
                FullnameArgument fa = UnifiedInboxUtility.parseNestedFullName(uid.getFullName());
                mail.setMsgref(new MailPath(fa.getAccountId(), fa.getFullname(), uid.getId()));
            }

            {
                int count = mail.getEnclosedCount();
                for (int i = 0; i < count; i++) {
                    MailPart mp = mail.getEnclosedMailPart(i);
                    Object content = mp.getContent();
                    if (content instanceof MailMessage) {
                        MailMessage nestedMail = (MailMessage) content;
                        MailPath msgref2 = nestedMail.getMsgref();
                        if (null != msgref2 && accountId == msgref2.getAccountId()) {
                            uid.setUIDString(msgref2.toString());
                            FullnameArgument fa = UnifiedInboxUtility.parseNestedFullName(uid.getFullName());
                            nestedMail.setMsgref(new MailPath(fa.getAccountId(), fa.getFullname(), uid.getId()));
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Ignore
            MailPath msgref = mail.getMsgref();
            if (null != msgref && accountId == msgref.getAccountId()) {
                FullnameArgument fa = UnifiedInboxUtility.parseNestedFullName(msgref.getFolder());
                mail.setMsgref(new MailPath(fa.getAccountId(), fa.getFullname(), msgref.getMailID()));
            }

            {
                int count = mail.getEnclosedCount();
                for (int i = 0; i < count; i++) {
                    MailPart mp = mail.getEnclosedMailPart(i);
                    Object content = mp.getContent();
                    if (content instanceof MailMessage) {
                        MailMessage nestedMail = (MailMessage) content;
                        MailPath msgref2 = nestedMail.getMsgref();
                        if (null != msgref2 && accountId == msgref2.getAccountId()) {
                            FullnameArgument fa = UnifiedInboxUtility.parseNestedFullName(msgref2.getFolder());
                            nestedMail.setMsgref(new MailPath(fa.getAccountId(), fa.getFullname(), msgref2.getMailID()));
                        }
                    }
                }
            }
        }
        return mail;
    }

    @Override
    public MailMessage getReplyMessage(MailMessage originalMail, boolean replyAll, boolean setFrom) throws OXException {
        return getReplyMessage(originalMail, replyAll, getUserSettingMail(), setFrom);
    }

    @Override
    public MailMessage getReplyMessage(MailMessage originalMail, boolean replyAll, UserSettingMail usm, boolean setFrom) throws OXException {
        MailMessage mail = super.getReplyMessage(originalMail, replyAll, usm, setFrom);
        try {
            UnifiedInboxUID uid = new UnifiedInboxUID(originalMail.getMailId());
            mail.setAccountId(uid.getAccountId());

            MailPath msgref = mail.getMsgref();
            if (null != msgref && accountId == msgref.getAccountId()) {
                uid.setUIDString(msgref.toString());
                FullnameArgument fa = UnifiedInboxUtility.parseNestedFullName(uid.getFullName());
                mail.setMsgref(new MailPath(fa.getAccountId(), fa.getFullname(), uid.getId()));
            }
        } catch (Exception e) {
            // Ignore
            MailPath msgref = mail.getMsgref();
            if (null != msgref && accountId == msgref.getAccountId()) {
                FullnameArgument fa = UnifiedInboxUtility.parseNestedFullName(msgref.getFolder());
                mail.setMsgref(new MailPath(fa.getAccountId(), fa.getFullname(), msgref.getMailID()));
            }
        }
        return mail;
    }

}
