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

package com.openexchange.freebusy.provider.ews;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.mail.internet.AddressException;
import com.microsoft.schemas.exchange.services._2006.messages.FreeBusyResponseType;
import com.microsoft.schemas.exchange.services._2006.types.ExchangeVersionType;
import com.openexchange.config.ConfigurationService;
import com.openexchange.ews.EWSExceptionCodes;
import com.openexchange.ews.EWSFactoryService;
import com.openexchange.ews.ExchangeWebService;
import com.openexchange.exception.OXException;
import com.openexchange.freebusy.FreeBusyData;
import com.openexchange.freebusy.FreeBusyExceptionCodes;
import com.openexchange.freebusy.FreeBusyInterval;
import com.openexchange.freebusy.provider.FreeBusyProvider;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.session.Session;

/**
 * {@link EWSFreeBusyProvider}
 *
 * Provider of free/busy information.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class EWSFreeBusyProvider implements FreeBusyProvider {

    /**
     * The maximum value for this time period is 42 days. Any requests for user availability information beyond the maximum value will
     * return an error.
     * @see http://msdn.microsoft.com/en-us/library/exchangewebservices.freebusyviewoptionstype.timewindow.aspx
     */
    private static final int MAX_DAYS = 42;
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(EWSFreeBusyProvider.class);

    private final ExchangeWebService ews;
    private String[] emailSuffixes;
    private Boolean validEmailsOnly;
    private Boolean detailedData;

    /**
     * Initializes a new {@link EWSFreeBusyProvider}.
     *
     * @throws OXException
     */
    public EWSFreeBusyProvider() throws OXException {
        super();
        this.ews = createWebService();
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
            for (Date currentFrom = from; currentFrom.before(until); currentFrom = addDays(currentFrom, MAX_DAYS)) {
                Date maxUntil = addDays(currentFrom, MAX_DAYS);
                Date currentUntil = until.before(maxUntil) ? until : maxUntil;
                List<FreeBusyResponseType> freeBusyResponses = null;
                try {
                    freeBusyResponses = getFreeBusyResponses(filteredParticipants, currentFrom, currentUntil);
                    if (null == freeBusyResponses || 0 == freeBusyResponses.size()) {
                        throw EWSExceptionCodes.NO_RESPONSE.create();
                    } else if (freeBusyResponses.size() != filteredParticipants.size()) {
                        throw EWSExceptionCodes.UNEXPECTED_RESPONSE_COUNT.create(filteredParticipants.size(), freeBusyResponses.size());
                    }
                } catch (OXException e) {
                    for (int i = 0; i < filteredParticipants.size(); i++) {
                        String participant = filteredParticipants.get(i);
                        FreeBusyData freeBusyData = freeBusyInformation.get(participant);
                        freeBusyData.addWarning(e);
                    }
                    continue;
                }
                /*
                 * add data from all filtered participants
                 */
                for (int i = 0; i < filteredParticipants.size(); i++) {
                    String participant = filteredParticipants.get(i);
                    FreeBusyData freeBusyData = freeBusyInformation.get(participant);
                    FreeBusyResponseType freeBusyResponse = freeBusyResponses.get(i);
                    if (null != freeBusyResponse) {
                        OXException warning = Tools.getError(participant, freeBusyResponse.getResponseMessage());
                        if (null != warning) {
                            freeBusyData.addWarning(warning);
                        }
                        Collection<FreeBusyInterval> intervals = Tools.getFreeBusyIntervals(freeBusyResponse.getFreeBusyView());
                        if (null != intervals && 0 < intervals.size()) {
                            freeBusyData.addAll(intervals);
                        }
                    } else {
                        freeBusyData.addWarning(FreeBusyExceptionCodes.DATA_NOT_AVAILABLE.create(participant));
                    }
                }
            }
        }
        return freeBusyInformation;
    }

    private List<FreeBusyResponseType> getFreeBusyResponses(List<String> emailAddresses, Date from, Date until) throws OXException {
        return ews.getAvailability().getFreeBusy(emailAddresses, from, until, isDetailedData());
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
                value = EWSFreeBusyProviderLookup.getService(ConfigurationService.class).getProperty(
                    "com.openexchange.freebusy.provider.ews.emailSuffixes");
            } catch (OXException e) {
                LOG.warn("error reading 'com.openexchange.freebusy.provider.ews.emailSuffixes'", e);
            }
            this.emailSuffixes = null == value || 0 == value.trim().length() ? new String[0] : value.trim().split(",");
        }
        return this.emailSuffixes;
    }

    private boolean isValidEmailsOnly() {
        if (null == this.validEmailsOnly) {
            boolean value = true;
            try {
                value = EWSFreeBusyProviderLookup.getService(ConfigurationService.class).getBoolProperty(
                    "com.openexchange.freebusy.provider.ews.validEmailsOnly", true);
            } catch (OXException e) {
                LOG.warn("error reading 'com.openexchange.freebusy.provider.ews.validEmailsOnly'", e);
            }
            this.validEmailsOnly = Boolean.valueOf(value);
        }
        return this.validEmailsOnly.booleanValue();
    }

    private boolean isDetailedData() {
        if (null == this.detailedData) {
            boolean value = false;
            try {
                value = EWSFreeBusyProviderLookup.getService(ConfigurationService.class).getBoolProperty(
                    "com.openexchange.freebusy.provider.ews.detailed", false);
            } catch (OXException e) {
                LOG.warn("error reading 'com.openexchange.freebusy.provider.ews.detailed'", e);
            }
            this.detailedData = Boolean.valueOf(value);
        }
        return this.detailedData.booleanValue();
    }

    private static ExchangeWebService createWebService() throws OXException {
        ConfigurationService configService = EWSFreeBusyProviderLookup.getService(ConfigurationService.class);
        ExchangeWebService ews = EWSFreeBusyProviderLookup.getService(EWSFactoryService.class).create(
            configService.getProperty("com.openexchange.freebusy.provider.ews.url"),
            configService.getProperty("com.openexchange.freebusy.provider.ews.userName"),
            configService.getProperty("com.openexchange.freebusy.provider.ews.password"));
        ews.getConfig().setExchangeVersion(ExchangeVersionType.valueOf(ExchangeVersionType.class,
            configService.getProperty("com.openexchange.freebusy.provider.ews.exchangeVersion", "EXCHANGE_2010").toUpperCase()));
        ews.getConfig().setIgnoreHostnameValidation(configService.getBoolProperty(
            "com.openexchange.freebusy.provider.ews.skipHostVerification", false));
        ews.getConfig().setTrustAllCerts(configService.getBoolProperty("com.openexchange.freebusy.provider.ews.trustAllCerts", false));
        return ews;
    }

    private static Date addDays(Date date, int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, days);
        return calendar.getTime();
    }

}
