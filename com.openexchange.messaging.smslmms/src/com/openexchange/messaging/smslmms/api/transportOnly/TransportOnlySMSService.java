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

package com.openexchange.messaging.smslmms.api.transportOnly;

import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.messaging.MessagingAccountManager;
import com.openexchange.messaging.smslmms.api.AbstractSMSService;
import com.openexchange.messaging.smslmms.api.SMSAccess;
import com.openexchange.messaging.smslmms.api.SMSConfiguration;
import com.openexchange.messaging.smslmms.api.SMSService;
import com.openexchange.session.Session;

/**
 * {@link TransportOnlySMSService} - The transport-only {@link SMSService}.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class TransportOnlySMSService extends AbstractSMSService {

    /**
     * Initializes a new {@link TransportOnlySMSService}.
     */
    public TransportOnlySMSService() {
        super();
    }

    @Override
    public MessagingAccountManager getAccountManager() {
        return EmptySMSAccountManager.getInstance();
    }

    @Override
    protected SMSAccess getSMSAccessInternal(final int accountId, final Session session) throws OXException {
        return new EmptySMSAccess(accountId, session.getUserId(), session.getContextId());
    }

    @Override
    public SMSConfiguration getSMSConfiguration(final int accountId, final Session session) throws OXException {
        return new AccessLessSMSConfiguration(getSMSConfigurationInternal(accountId, session));
    }

    /**
     * Gets the SMS/MMS configuration for the user associated with specified session.
     * 
     * @param accountId The account identifier
     * @param session The session providing user data
     * @return The SMS/MMS configuration
     * @throws OXException If configuration cannot be returned
     */
    protected abstract SMSConfiguration getSMSConfigurationInternal(int accountId, Session session) throws OXException;

    private static final class AccessLessSMSConfiguration implements SMSConfiguration {

        private final SMSConfiguration smsConfiguration;

        protected AccessLessSMSConfiguration(final SMSConfiguration smsConfiguration) {
            super();
            this.smsConfiguration = smsConfiguration;
        }

        @Override
        public Map<String, Object> getConfiguration() {
            return smsConfiguration.getConfiguration();
        }

        @Override
        public List<String> getAddresses() {
            return smsConfiguration.getAddresses();
        }

        @Override
        public String getDisplayString() {
            return smsConfiguration.getDisplayString();
        }

        @Override
        public int getLength() {
            return smsConfiguration.getLength();
        }

        @Override
        public boolean isEnabled() {
            return smsConfiguration.isEnabled();
        }

        @Override
        public boolean isCaptcha() {
            return smsConfiguration.isCaptcha();
        }

        @Override
        public boolean getMultiSMS() {
            return smsConfiguration.getMultiSMS();
        }

        @Override
        public boolean isMMS() {
            return smsConfiguration.isMMS();
        }

        @Override
        public boolean supportsFolderStorage() {
            return false;
        }

        @Override
        public boolean supportsAccess() {
            return false;
        }

        @Override
        public String getUpsellLink() {
            return smsConfiguration.getUpsellLink();
        }

    }

}
