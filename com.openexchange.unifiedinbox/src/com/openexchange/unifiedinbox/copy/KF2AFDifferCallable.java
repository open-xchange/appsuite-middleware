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

package com.openexchange.unifiedinbox.copy;

import java.util.ArrayList;
import java.util.List;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mailaccount.UnifiedInboxUID;
import com.openexchange.session.Session;
import com.openexchange.threadpool.Task;
import com.openexchange.threadpool.ThreadRenamer;
import gnu.trove.list.TIntList;
import gnu.trove.list.linked.TIntLinkedList;

final class KF2AFDifferCallable implements Task<Object> {

    private final int accountId;

    private final int destAccountId;

    private final Session session;

    private final String destFullname;

    private final String[] toFill;

    private final List<String> idList;

    private final List<String> srcFullnameList;

    private final TIntList indexList;

    private final boolean move;

    KF2AFDifferCallable(final int accountId, final int destAccountId, final String destFullname, final boolean move, final String[] toFill, final Session session) {
        super();
        this.accountId = accountId;
        this.destAccountId = destAccountId;
        this.session = session;
        this.destFullname = destFullname;
        this.toFill = toFill;
        this.move = move;
        idList = new ArrayList<String>();
        srcFullnameList = new ArrayList<String>();
        indexList = new TIntLinkedList();
    }

    void addIdAndFullnameAndIndex(final String id, final String srcFullname, final int index) {
        idList.add(id);
        srcFullnameList.add(srcFullname);
        indexList.add(index);
    }

    @Override
    public Object call() throws Exception {
        MailAccess<?, ?> sourceMailAccess = null;
        try {
            sourceMailAccess = MailAccess.getInstance(session, accountId);
            sourceMailAccess.connect();
            MailAccess<?, ?> destMailAccess = null;
            try {
                destMailAccess = MailAccess.getInstance(session, destAccountId);
                destMailAccess.connect();
                final int size = idList.size();
                final MailMessage[] mails = new MailMessage[size];
                // Gather mails
                for (int j = 0; j < size; j++) {
                    final String srcFullname = srcFullnameList.get(j);
                    final String mailId = idList.get(j);
                    mails[j] = sourceMailAccess.getMessageStorage().getMessage(srcFullname, mailId, false);
                }
                // Append gathered messages
                final String[] results = destMailAccess.getMessageStorage().appendMessages(destFullname, mails);
                UnifiedInboxUID helper = new UnifiedInboxUID();
                for (int j = 0; j < results.length; j++) {
                    toFill[indexList.get(j)] = helper.setUID(destAccountId, destFullname, results[j]).toString();
                }
                // Delete on move
                if (move) {
                    final String[] sa = new String[1];
                    for (int j = 0; j < size; j++) {
                        final String srcFullname = srcFullnameList.get(j);
                        sa[0] = idList.get(j);
                        sourceMailAccess.getMessageStorage().deleteMessages(srcFullname, sa, true);
                    }
                }
            } finally {
                if (null != destMailAccess) {
                    destMailAccess.close(true);
                }
            }
        } finally {
            if (null != sourceMailAccess) {
                sourceMailAccess.close(true);
            }
        }
        return null;
    }

    @Override
    public void afterExecute(final Throwable t) {
        // NOP
    }

    @Override
    public void beforeExecute(final Thread t) {
        // NOP
    }

    @Override
    public void setThreadName(final ThreadRenamer threadRenamer) {
        // NOP
    }

}
