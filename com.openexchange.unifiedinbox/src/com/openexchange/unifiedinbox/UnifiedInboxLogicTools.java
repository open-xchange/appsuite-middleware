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

package com.openexchange.unifiedinbox;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import com.openexchange.exception.OXException;
import com.openexchange.mail.api.MailLogicTools;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.session.Session;


/**
 * {@link UnifiedInboxLogicTools}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UnifiedInboxLogicTools extends MailLogicTools {

    /**
     * Initializes a new {@link UnifiedInboxLogicTools}.
     * 
     * @param session
     * @param accountId
     */
    public UnifiedInboxLogicTools(final Session session, final int accountId) {
        super(session, accountId);
    }

    @Override
    public MailMessage getFowardMessage(final MailMessage[] originalMails) throws OXException {
        final MailMessage mail = super.getFowardMessage(originalMails);
        try {
            final TIntList accountIds = new TIntArrayList(originalMails.length);
            final UnifiedInboxUID uid = new UnifiedInboxUID();
            for (final MailMessage originalMail : originalMails) {
                uid.setUIDString(originalMail.getMailId());
                final int aid = uid.getAccountId();
                if (!accountIds.contains(aid)) {
                    accountIds.add(aid);
                }
            }
            if (1 == accountIds.size()) {
                mail.setAccountId(accountIds.get(0));
            }
        } catch (final Exception e) {
            // Ignore
        }
        return mail;
    }
    
    @Override
    public MailMessage getFowardMessage(final MailMessage[] originalMails, final UserSettingMail usm) throws OXException {
        final MailMessage mail = super.getFowardMessage(originalMails, usm);
        try {
            final TIntList accountIds = new TIntArrayList(originalMails.length);
            final UnifiedInboxUID uid = new UnifiedInboxUID();
            for (final MailMessage originalMail : originalMails) {
                uid.setUIDString(originalMail.getMailId());
                final int aid = uid.getAccountId();
                if (!accountIds.contains(aid)) {
                    accountIds.add(aid);
                }
            }
            if (1 == accountIds.size()) {
                mail.setAccountId(accountIds.get(0));
            }
        } catch (final Exception e) {
            // Ignore
        }
        return mail;
    }
    
    @Override
    public MailMessage getReplyMessage(final MailMessage originalMail, final boolean replyAll) throws OXException {
        final MailMessage mail = super.getReplyMessage(originalMail, replyAll);
        try {
            final UnifiedInboxUID uid = new UnifiedInboxUID(originalMail.getMailId());
            mail.setAccountId(uid.getAccountId());
        } catch (final Exception e) {
            // Ignore
        }
        return mail;
    }
    
    @Override
    public MailMessage getReplyMessage(final MailMessage originalMail, final boolean replyAll, final UserSettingMail usm) throws OXException {
        final MailMessage mail = super.getReplyMessage(originalMail, replyAll, usm);
        try {
            final UnifiedInboxUID uid = new UnifiedInboxUID(originalMail.getMailId());
            mail.setAccountId(uid.getAccountId());
        } catch (final Exception e) {
            // Ignore
        }
        return mail;
    }

}
