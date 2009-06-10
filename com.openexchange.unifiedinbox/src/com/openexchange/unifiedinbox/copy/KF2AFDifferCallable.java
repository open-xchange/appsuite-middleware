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

package com.openexchange.unifiedinbox.copy;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.session.Session;

final class KF2AFDifferCallable implements Callable<Object> {

    private final int accountId;

    private final int destAccountId;

    private final Session session;

    private final String destFullname;

    private final String[] toFill;

    private final List<String> idList;

    private final List<String> srcFullnameList;

    private final List<Integer> indexList;

    private final boolean fast;

    private final boolean move;

    KF2AFDifferCallable(final int accountId, final int destAccountId, final String destFullname, final boolean fast, final boolean move, final String[] toFill, final Session session) {
        super();
        this.accountId = accountId;
        this.destAccountId = destAccountId;
        this.session = session;
        this.destFullname = destFullname;
        this.toFill = toFill;
        this.fast = fast;
        this.move = move;
        idList = new ArrayList<String>();
        srcFullnameList = new ArrayList<String>();
        indexList = new ArrayList<Integer>();
    }

    void addIdAndFullnameAndIndex(final String id, final String srcFullname, final Integer index) {
        idList.add(id);
        srcFullnameList.add(srcFullname);
        indexList.add(index);
    }

    public Object call() throws Exception {
        final MailAccess<?, ?> sourceMailAccess = MailAccess.getInstance(session, accountId);
        sourceMailAccess.connect();
        try {
            final MailAccess<?, ?> destMailAccess = MailAccess.getInstance(session, destAccountId);
            destMailAccess.connect();
            try {
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
                for (int j = 0; j < results.length; j++) {
                    toFill[indexList.get(j).intValue()] = results[j];
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
                destMailAccess.close(true);
            }
        } finally {
            sourceMailAccess.close(true);
        }
        return null;
    }

}
