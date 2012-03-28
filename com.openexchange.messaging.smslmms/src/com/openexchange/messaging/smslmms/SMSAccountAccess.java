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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.messaging.smslmms;

import com.openexchange.exception.OXException;
import com.openexchange.messaging.MessagingAccountAccess;
import com.openexchange.messaging.MessagingExceptionCodes;
import com.openexchange.messaging.MessagingFolder;
import com.openexchange.messaging.MessagingFolderAccess;
import com.openexchange.messaging.MessagingMessageAccess;
import com.openexchange.messaging.smslmms.api.SMSAccess;
import com.openexchange.messaging.smslmms.api.SMSConfiguration;
import com.openexchange.messaging.smslmms.api.SMSMessageAccess;
import com.openexchange.messaging.smslmms.api.SMSService;


/**
 * {@link SMSAccountAccess} - The SMS/MMS messaging-based account access.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class SMSAccountAccess implements MessagingAccountAccess, SMSAccess {

    protected boolean connected;

    protected final SMSAccess smsAccess;

    protected final SMSConfiguration configuration;

    /**
     * Initializes a new {@link SMSAccountAccess}.
     * 
     * @param smsAccess The SMS/MMS access
     */
    public SMSAccountAccess(final SMSConfiguration configuration, final SMSAccess smsAccess) {
        super();
        this.configuration = configuration;
        this.smsAccess = smsAccess;
    }

    @Override
    public int getAccountId() {
        return smsAccess.getAccountId();
    }

    @Override
    public SMSMessageAccess getSMSMessageAccess() throws OXException {
        return smsAccess.getSMSMessageAccess();
    }

    @Override
    public MessagingMessageAccess getMessageAccess() throws OXException {
        return new SMSMessagingMessageAccess(smsAccess.getSMSMessageAccess());
    }

    @Override
    public MessagingFolderAccess getFolderAccess() throws OXException {
        if (!configuration.supportsFolderStorage()) {
            throw MessagingExceptionCodes.OPERATION_NOT_SUPPORTED.create(SMSService.DISPLAY_NAME);
        }
        return smsAccess.getFolderAccess();
    }

    @Override
    public MessagingFolder getRootFolder() throws OXException {
        return smsAccess.getRootFolder();
    }

    @Override
    public void connectAccess() throws OXException {
        smsAccess.connectAccess();
    }

    @Override
    public void closeAccess() {
        smsAccess.closeAccess();
    }

    @Override
    public void connect() throws OXException {
        if (connected) {
            return;
        }
        smsAccess.connectAccess();
        connected = true;
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public void close() {
        if (!connected) {
            return;
        }
        try {
            smsAccess.closeAccess();
        } catch (final Exception e) {
            // Ignore
        }
        connected = false;
    }

    @Override
    public boolean ping() throws OXException {
        return true;
    }

    @Override
    public boolean cacheable() {
        return false;
    }

}
