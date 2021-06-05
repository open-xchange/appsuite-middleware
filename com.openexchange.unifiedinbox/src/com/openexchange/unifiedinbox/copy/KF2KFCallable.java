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
import com.openexchange.mailaccount.UnifiedInboxUID;
import com.openexchange.session.Session;
import com.openexchange.threadpool.Task;
import com.openexchange.threadpool.ThreadRenamer;
import com.openexchange.unifiedinbox.utility.UnifiedInboxUtility;
import gnu.trove.list.TIntList;
import gnu.trove.list.linked.TIntLinkedList;

final class KF2KFCallable implements Task<Object> {

    private final int accountId;

    private final Session session;

    private final String sourceFolder;

    private final String destFolder;

    private final String[] toFill;

    private final List<String> idList;

    private final TIntList indexList;

    private final boolean fast;

    private final boolean move;

    KF2KFCallable(final String sourceFolder, final String destFolder, final boolean fast, final boolean move, final String[] toFill, final int accountId, final Session session) {
        super();
        this.accountId = accountId;
        this.session = session;
        this.destFolder = destFolder;
        this.sourceFolder = sourceFolder;
        this.toFill = toFill;
        this.fast = fast;
        this.move = move;
        idList = new ArrayList<String>();
        indexList = new TIntLinkedList();
    }

    void addIdAndIndex(final String id, final int index) {
        idList.add(id);
        indexList.add(index);
    }

    @Override
    public Object call() throws Exception {
        MailAccess<?, ?> mailAccess = null;
        try {
            mailAccess = MailAccess.getInstance(session, accountId);
            mailAccess.connect();
            final String realSource = UnifiedInboxUtility.determineAccountFullName(mailAccess, sourceFolder);
            final String realDest = UnifiedInboxUtility.determineAccountFullName(mailAccess, destFolder);
            final String[] results;
            if (move) {
                results = mailAccess.getMessageStorage().moveMessages(realSource, realDest, idList.toArray(new String[idList.size()]), fast);
            } else {
                results = mailAccess.getMessageStorage().copyMessages(realSource, realDest, idList.toArray(new String[idList.size()]), fast);
            }
            UnifiedInboxUID helper = new UnifiedInboxUID();
            for (int j = 0; j < results.length; j++) {
                toFill[indexList.get(j)] = helper.setUID(accountId, realDest, results[j]).toString();
            }
        } finally {
            if (null != mailAccess) {
                mailAccess.close(true);
            }
        }
        return null;
    }

    @Override
    public void afterExecute(final Throwable t) {
        // Nothing to do
    }

    @Override
    public void beforeExecute(final Thread t) {
        // Nothing to do
    }

    @Override
    public void setThreadName(final ThreadRenamer threadRenamer) {
        // Nothing to do
    }

}
