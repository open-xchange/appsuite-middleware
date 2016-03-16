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

package com.openexchange.imap.command;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import javax.mail.MessagingException;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.protocol.FetchResponse;
import com.sun.mail.imap.protocol.UID;

/**
 * {@link SeqNumIMAPCommand}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SeqNumIMAPCommand extends AbstractIMAPCommand<int[]> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SeqNumIMAPCommand.class);

    private static final long[] L1 = new long[0];

    private final long[] uids;

    private final int length;

    private final String[] args;

    private final TIntList sia;

    private int fetchRespIndex;

    /**
     * @param imapFolder
     */
    public SeqNumIMAPCommand(final IMAPFolder imapFolder, final long[] uids, final boolean isSequential) {
        super(imapFolder);
        this.uids = uids == null ? L1 : uids;
        returnDefaultValue = (this.uids.length == 0);
        length = this.uids.length;
        args = length == 0 ? ARGS_EMPTY : (isSequential ? new String[] { new StringBuilder(64).append(this.uids[0]).append(':').append(
            this.uids[this.uids.length - 1]).toString() } : IMAPNumArgSplitter.splitUIDArg(this.uids, true, -1));
        sia = new TIntArrayList(length);
    }

    @Override
    protected boolean addLoopCondition() {
        return (fetchRespIndex < length);
    }

    @Override
    protected String[] getArgs() {
        return args;
    }

    @Override
    protected String getCommand(final int argsIndex) {
        final StringBuilder sb = new StringBuilder(args[argsIndex].length() + 64);
        sb.append("UID FETCH ");
        sb.append(args[argsIndex]);
        sb.append(" (UID)");
        return sb.toString();
    }

    private static final int[] EMPTY_ARR = new int[0];

    @Override
    protected int[] getDefaultValue() {
        return EMPTY_ARR;
    }

    @Override
    protected int[] getReturnVal() {
        return sia.toArray();
    }

    @Override
    protected boolean handleResponse(final Response response) throws MessagingException {
        if (!(response instanceof FetchResponse)) {
            return false;
        }
        final FetchResponse f = (FetchResponse) response;
        /*
         * Check if response's uid matches corresponding uid
         */
        final long currentUID = ((UID) f.getItem(0)).uid;
        final long correspondingUID = uids[fetchRespIndex++];
        if (correspondingUID != currentUID) {
            LOG.warn("IMAPUtils.getSequenceNumbers(): UID mismatch");
            return false;
        }
        sia.add(f.getNumber());
        return true;
    }

}
