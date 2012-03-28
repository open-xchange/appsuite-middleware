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

package com.openexchange.messaging.smslmms;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.datatypes.genericonf.ReadOnlyDynamicFormDescription;
import com.openexchange.exception.OXException;
import com.openexchange.messaging.ConfigProvidingMessagingService;
import com.openexchange.messaging.MessagingAccountAccess;
import com.openexchange.messaging.MessagingAccountManager;
import com.openexchange.messaging.MessagingAccountTransport;
import com.openexchange.messaging.MessagingAction;
import com.openexchange.messaging.MessagingPermission;
import com.openexchange.messaging.smslmms.api.SMSAccess;
import com.openexchange.messaging.smslmms.api.SMSConfiguration;
import com.openexchange.messaging.smslmms.api.SMSService;
import com.openexchange.messaging.smslmms.api.SMSTransport;
import com.openexchange.session.Session;

/**
 * {@link SMSMessagingService} - The messaging service for SMS/MMS.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class SMSMessagingService implements ConfigProvidingMessagingService, SMSService {

    private static final List<MessagingAction> ACTIONS = Collections.unmodifiableList(Arrays.asList(
        new MessagingAction(SMSMessagingConstants.TYPE_REPLY_TO, MessagingAction.Type.STORAGE, SMSMessagingConstants.TYPE_NEW),
        new MessagingAction(SMSMessagingConstants.TYPE_DELETE, MessagingAction.Type.STORAGE),
        new MessagingAction(SMSMessagingConstants.TYPE_OPEN, MessagingAction.Type.STORAGE),
        new MessagingAction(SMSMessagingConstants.TYPE_NEW, MessagingAction.Type.MESSAGE)));

    private final SMSService smsService;

    private final DynamicFormDescription formDescription;

    /**
     * Initializes a new {@link SMSMessagingService}.
     * 
     * @param smsService The SMS/MMS service
     */
    public SMSMessagingService(final SMSService smsService) {
        super();
        this.smsService = smsService;
        final DynamicFormDescription tmpDescription = new DynamicFormDescription();
        formDescription = new ReadOnlyDynamicFormDescription(tmpDescription);
    }

    @Override
    public Map<String, Object> getConfiguration(final int accountId, final Session session) throws OXException {
        final SMSConfiguration smsConfiguration = smsService.getSMSConfiguration(accountId, session);
        final Map<String, Object> map = new HashMap<String, Object>(16);

        map.put(SMSConfiguration.PROP_ADDRESSES, smsConfiguration.getAddresses());
        map.put(SMSConfiguration.PROP_DISPLAY_STRING, smsConfiguration.getDisplayString());
        map.put(SMSConfiguration.PROP_ENABLED, Boolean.valueOf(smsConfiguration.isEnabled()));
        map.put(SMSConfiguration.PROP_FOLDER_STORAGE, Boolean.valueOf(smsConfiguration.supportsFolderStorage()));
        map.put(SMSConfiguration.PROP_LENGTH, Integer.valueOf(smsConfiguration.getLength()));
        map.put(SMSConfiguration.PROP_MMS, Boolean.valueOf(smsConfiguration.isMMS()));
        map.put(SMSConfiguration.PROP_MULTI_SMS, Boolean.valueOf(smsConfiguration.getMultiSMS()));
        map.put(SMSConfiguration.PROP_SMS_ACCESS, Boolean.valueOf(smsConfiguration.supportsAccess()));
        map.put(SMSConfiguration.PROP_UPSELL_LINK, smsConfiguration.getUpsellLink());

        for (final Entry<String,Object> entry : smsConfiguration.getConfiguration().entrySet()) {
            final String propName = entry.getKey();
            if (!map.containsKey(propName)) {
                map.put(propName, entry.getValue());
            }
        }

        return map;
    }

    @Override
    public SMSConfiguration getSMSConfiguration(final int accountId, final Session session) throws OXException {
        return smsService.getSMSConfiguration(accountId, session);
    }

    @Override
    public String getId() {
        return SMSService.SERVICE_ID;
    }

    @Override
    public List<MessagingAction> getMessageActions() {
        return ACTIONS;
    }

    @Override
    public String getDisplayName() {
        return SMSService.DISPLAY_NAME;
    }

    @Override
    public DynamicFormDescription getFormDescription() {
        return formDescription;
    }

    @Override
    public Set<String> getSecretProperties() {
        return Collections.emptySet();
    }

    @Override
    public int[] getStaticRootPermissions() {
        return new int[] {MessagingPermission.READ_FOLDER,
            MessagingPermission.READ_ALL_OBJECTS,
            MessagingPermission.NO_PERMISSIONS,
            MessagingPermission.DELETE_OWN_OBJECTS};
    }

    @Override
    public MessagingAccountManager getAccountManager() {
        return smsService.getAccountManager();
    }

    @Override
    public SMSAccess getSMSAccess(final int accountId, final Session session) throws OXException {
        return smsService.getSMSAccess(accountId, session);
    }

    @Override
    public SMSTransport getSMSTransport(final int accountId, final Session session) throws OXException {
        return smsService.getSMSTransport(accountId, session);
    }

    @Override
    public MessagingAccountAccess getAccountAccess(final int accountId, final Session session) throws OXException {
        final SMSConfiguration configuration = smsService.getSMSConfiguration(accountId, session);
        return new SMSAccountAccess(configuration, smsService.getSMSAccess(accountId, session));
    }

    @Override
    public MessagingAccountTransport getAccountTransport(final int accountId, final Session session) throws OXException {
        return new SMSTransportAccess(smsService.getSMSTransport(accountId, session));
    }

}
