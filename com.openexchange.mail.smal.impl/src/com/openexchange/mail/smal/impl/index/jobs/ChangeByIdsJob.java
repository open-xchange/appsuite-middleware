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

package com.openexchange.mail.smal.impl.index.jobs;

import java.util.Arrays;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.attach.index.Attachment;
import com.openexchange.index.IndexAccess;
import com.openexchange.index.IndexFacadeService;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.smal.impl.SmalMailAccess;
import com.openexchange.mail.smal.impl.SmalServiceLookup;
import com.openexchange.mail.smal.impl.index.FakeSession;
import com.openexchange.service.indexing.JobInfo;


/**
 * {@link ChangeByIdsJob}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class ChangeByIdsJob extends AbstractMailJob {

    @Override
    public void execute(JobInfo jobInfo) throws OXException {
        try {
            if (!(jobInfo instanceof MailJobInfo)) {
                throw new IllegalArgumentException("Job info must be an instance of MailJobInfo.");
            }

            MailJobInfo info = (MailJobInfo) jobInfo;
            long start = System.currentTimeMillis();
            if (LOG.isDebugEnabled()) {
                LOG.debug(this.getClass().getSimpleName() + " started performing. " + info.toString());
            }

            checkJobInfo();
            IndexFacadeService indexFacade = SmalServiceLookup.getServiceStatic(IndexFacadeService.class);
            final IndexAccess<MailMessage> mailIndex = indexFacade.acquireIndexAccess(Types.EMAIL, info.userId, info.contextId);
            final IndexAccess<Attachment> attachmentIndex = indexFacade.acquireIndexAccess(Types.ATTACHMENT, info.userId, info.contextId);
            FakeSession fakeSession = new FakeSession(info.primaryPassword, info.userId, info.contextId);
            MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = SmalMailAccess.getUnwrappedInstance(fakeSession, info.accountId);
            try {
                mailAccess.connect();
                IMailFolderStorage folderStorage = mailAccess.getFolderStorage();
                if (folderStorage.exists(info.folder)) {
                    String[] ids = (String[]) info.getProperty(IDS);
                    final List<String> toChange = Arrays.asList(ids);
                    changeMails(info, toChange, mailAccess.getMessageStorage(), mailIndex, attachmentIndex);
                }
            } finally {
                SmalMailAccess.closeUnwrappedInstance(mailAccess);
                closeIndexAccess(mailIndex);
                closeIndexAccess(attachmentIndex);

                if (LOG.isDebugEnabled()) {
                    long diff = System.currentTimeMillis() - start;
                    LOG.debug(this.getClass().getSimpleName() + " lasted " + diff + "ms. " + info.toString());
                }
            }
        } catch (Exception e) {
            throw new OXException(e);
        }
    }

    private void checkJobInfo() {
        // Nothing to do

    }

}
