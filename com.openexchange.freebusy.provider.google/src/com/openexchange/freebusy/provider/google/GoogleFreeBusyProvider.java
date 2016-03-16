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

package com.openexchange.freebusy.provider.google;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.mail.internet.AddressException;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.freebusy.FreeBusyData;
import com.openexchange.freebusy.FreeBusyExceptionCodes;
import com.openexchange.freebusy.provider.FreeBusyProvider;
import com.openexchange.freebusy.provider.google.client.JsonClient;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.session.Session;

/**
 * {@link GoogleFreeBusyProvider}
 *
 * Provider of free/busy information.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class GoogleFreeBusyProvider implements FreeBusyProvider {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(GoogleFreeBusyProvider.class);

    private final JsonClient client;
    private String[] emailSuffixes;
    private Boolean validEmailsOnly;

    /**
     * Initializes a new {@link GoogleFreeBusyProvider}.
     *
     * @throws OXException
     */
    public GoogleFreeBusyProvider() throws OXException {
        super();
        ConfigurationService configService = GoogleFreeBusyProviderLookup.getService(ConfigurationService.class);
        this.client = new JsonClient(
            configService.getProperty("com.openexchange.freebusy.provider.google.apiEndpoint", "https://www.googleapis.com/calendar/v3"),
            configService.getProperty("com.openexchange.freebusy.provider.google.apiKey", "{YOUR_API_KEY}"));
    }

    @Override
    public Map<String, FreeBusyData> getFreeBusy(Session session, List<String> participants, Date from, Date until) {
        /*
         * prepare participant's free/busy data
         */
        Map<String, FreeBusyData> freeBusyInformation = new HashMap<String, FreeBusyData>();
        List<String> filteredParticipants = new ArrayList<String>();
        for (String participant : participants) {
            FreeBusyData freeBusyData = new FreeBusyData(participant, from, until);
            freeBusyInformation.put(participant, freeBusyData);
            if (hasAllowedEmailSuffix(participant) && isValidEmail(participant)) {
                filteredParticipants.add(participant);
            } else {
                freeBusyData.addWarning(FreeBusyExceptionCodes.DATA_NOT_AVAILABLE.create(participant));
            }
        }
        /*
         * query free/busy information in chunks
         */
        if (0 < filteredParticipants.size()) {
            try {
                /*
                 * add data from all filtered participants
                 */
                List<FreeBusyData> freeBusyDataList = client.getFreeBusy(filteredParticipants, from, until);
                for (FreeBusyData freeBusyData : freeBusyDataList) {
                    if (false == freeBusyInformation.containsKey(freeBusyData.getParticipant())) {
                        LOG.warn("Got data for non-requested participant '{}', skipping.", freeBusyData.getParticipant());
                        continue;
                    }
                    freeBusyInformation.get(freeBusyData.getParticipant()).add(freeBusyData);
                }
            } catch (OXException e) {
                // add exception for all participants
                for (String participant : filteredParticipants) {
                    freeBusyInformation.get(participant).addWarning(e);
                }
            }
        }
        return freeBusyInformation;
    }

    private boolean hasAllowedEmailSuffix(String participant) {
        String[] emailSuffixes = getEmailSuffixes();
        if (null != emailSuffixes && 0 < emailSuffixes.length) {
            for (String suffix : emailSuffixes) {
                if (participant.endsWith(suffix)) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    private boolean isValidEmail(String participant) {
        if (isValidEmailsOnly()) {
            try {
                new QuotedInternetAddress(participant).validate();
            } catch (AddressException e) {
                return false;
            }
        }
        return true;
    }

    private String[] getEmailSuffixes() {
        if (null == this.emailSuffixes) {
            String value = null;
            try {
                value = GoogleFreeBusyProviderLookup.getService(ConfigurationService.class).getProperty(
                    "com.openexchange.freebusy.provider.google.emailSuffixes");
            } catch (OXException e) {
                LOG.warn("error reading 'com.openexchange.freebusy.provider.google.emailSuffixes'", e);
            }
            this.emailSuffixes = null == value || 0 == value.trim().length() ? new String[0] : value.trim().split(",");
        }
        return this.emailSuffixes;
    }

    private boolean isValidEmailsOnly() {
        if (null == this.validEmailsOnly) {
            boolean value = true;
            try {
                value = GoogleFreeBusyProviderLookup.getService(ConfigurationService.class).getBoolProperty(
                    "com.openexchange.freebusy.provider.google.validEmailsOnly", true);
            } catch (OXException e) {
                LOG.warn("error reading 'com.openexchange.freebusy.provider.google.validEmailsOnly'", e);
            }
            this.validEmailsOnly = Boolean.valueOf(value);
        }
        return this.validEmailsOnly.booleanValue();
    }

}
