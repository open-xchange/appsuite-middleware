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

package com.openexchange.mail.api;

import com.openexchange.exception.OXException;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.spamhandler.SpamHandler;

/**
 * {@link IMailMessageStorageBatch} - Extends {@link IMailMessageStorage} for mail systems which support to request single header names.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface IMailMessageStorageBatch extends IMailMessageStorage {

    /**
     * An <b>optional</b> method that updates the color label of all messages located in given folder.
     * <p>
     * If no mail could be found for a given mail ID, it is treated as a no-op.
     * <p>
     * The underlying mailing system needs to support some kind of user-definable flags to support this method. Otherwise this method should
     * be left unchanged with an empty body.
     * <p>
     * The color labels are user flags with the common prefix <code>"cl_"</code> and its numeric color code appended (currently numbers 0 to
     * 10).
     *
     * @param fullName The folder full name
     * @param colorLabel The color label to apply
     * @throws OXException If color label cannot be updated
     */
    public void updateMessageColorLabel(String fullName, int colorLabel) throws OXException;

    /**
     * Updates the flags of all messages located in given folder. If parameter <code>set</code> is
     * <code>true</code> the affected flags denoted by <code>flags</code> are added; otherwise removed.
     * <p>
     * If no mail could be found for a given mail ID, it is treated as a no-op.
     * <p>
     * System flags are:
     * <ul>
     * <li>ANSWERED - This flag is set by clients to indicate that this message has been answered to.</li>
     * <li>DELETED - Clients set this flag to mark a message as deleted. The expunge operation on a folder removes all messages in that
     * folder that are marked for deletion.</li>
     * <li>DRAFT - This flag is set by clients to indicate that the message is a draft message.</li>
     * <li>FLAGGED - No semantic is defined for this flag. Clients alter this flag.</li>
     * <li>RECENT - Folder implementations set this flag to indicate that this message is new to this folder, that is, it has arrived since
     * the last time this folder was opened.</li>
     * <li>SEEN - This flag is implicitly set by the implementation when the this Message's content is returned to the client in some
     * form.Clients can alter this flag.</li>
     * <li>USER - A special flag that indicates that this folder supports user defined flags.</li>
     * </ul>
     * <p>
     * If mail folder in question supports user flags (storing individual strings per message) the virtual flags can also be updated through
     * this routine; e.g. {@link MailMessage#FLAG_FORWARDED}.
     * <p>
     * Moreover this routine checks for any spam related actions; meaning the {@link MailMessage#FLAG_SPAM} shall be enabled/disabled. Thus
     * the {@link SpamHandler#handleSpam(String, String[], boolean, MailAccess)}/
     * {@link SpamHandler#handleHam(String, String[], boolean, MailAccess)} methods needs to be executed.
     *
     * @param fullName The folder full name
     * @param flags The bit pattern for the flags to alter
     * @param set <code>true</code> to enable the flags; otherwise <code>false</code>
     * @throws OXException If system flags cannot be updated
     */
    public void updateMessageFlags(String fullName, int flags, boolean set) throws OXException;

    /**
     * Like {@link #updateMessageFlags(String, int, boolean)} but also updates user flags
     *
     * @param fullName The folder full name
     * @param flags The bit pattern for the flags to alter
     * @param userFlags An array of user flags
     * @param set <code>true</code> to enable the flags; otherwise <code>false</code>
     * @throws OXException If system flags cannot be updated
     */
    public void updateMessageFlags(final String fullName, final int flagsArg, String[] userFlags, final boolean set) throws OXException;

}
