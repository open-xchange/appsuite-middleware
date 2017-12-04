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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.mail.authenticity.impl.handler.core;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.mail.FullnameArgument;
import com.openexchange.mail.MailAttributation;
import com.openexchange.mail.MailFetchArguments;
import com.openexchange.mail.MailFetchListener;
import com.openexchange.mail.MailFetchListenerResult;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailFields;
import com.openexchange.mail.authenticity.MailAuthenticityHandler;
import com.openexchange.mail.authenticity.MailAuthenticityHandlerRegistry;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.session.Session;

/**
 * {@link MailAuthenticityFetchListener}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class MailAuthenticityFetchListener implements MailFetchListener {

    private static final String STATE_PARAM_HANDLER = "mail.authenticity.handler";

    private final MailAuthenticityHandlerRegistry handlerRegistry;

    /**
     * Initializes a new {@link MailAuthenticityFetchListener}.
     */
    public MailAuthenticityFetchListener(MailAuthenticityHandlerRegistry handlerRegistry) {
        super();
        this.handlerRegistry = handlerRegistry;
    }

    private boolean isNotApplicableFor(MailFetchArguments fetchArguments) {
        return false == isApplicableFor(fetchArguments);
    }

    private boolean isApplicableFor(MailFetchArguments fetchArguments) {
        FullnameArgument folder = fetchArguments.getFolder();
        return (null != folder && MailAccount.DEFAULT_ID == folder.getAccountId()) && new MailFields(fetchArguments.getFields()).contains(MailField.AUTHENTICATION_OVERALL_RESULT);
    }

    @Override
    public boolean accept(MailMessage[] mailsFromCache, MailFetchArguments fetchArguments, Session session) throws OXException {
        if (isNotApplicableFor(fetchArguments)) {
            return true;
        }

        MailAuthenticityHandler handler = handlerRegistry.getHighestRankedHandlerFor(session);
        if (null == handler) {
            return true;
        }

        long threshold = handlerRegistry.getDateThreshold(session);
        for (MailMessage mail : mailsFromCache) {
            if (false == mail.hasAuthenticityResult() && (threshold <= 0 || mail.getReceivedDate().getTime() >= threshold)) {
                // Should hold an authenticity result
                return false;
            }
        }

        return true;
    }

    @Override
    public MailAttributation onBeforeFetch(MailFetchArguments fetchArguments, Session session, Map<String, Object> state) throws OXException {
        if (isNotApplicableFor(fetchArguments)) {
            // Special field not contained
            return MailAttributation.NOT_APPLICABLE;
        }

        MailAuthenticityHandler handler = handlerRegistry.getHighestRankedHandlerFor(session);
        if (null == handler) {
            return MailAttributation.NOT_APPLICABLE;
        }

        MailFields fields = null == fetchArguments.getFields() ? new MailFields() : new MailFields(fetchArguments.getFields());
        fields.add(MailField.RECEIVED_DATE);
        Set<String> headerNames = null == fetchArguments.getHeaderNames() ? new LinkedHashSet<>() : new LinkedHashSet<>(Arrays.asList(fetchArguments.getHeaderNames()));
        {
            Collection<MailField> requiredFields = handler.getRequiredFields();
            if (null != requiredFields && !requiredFields.isEmpty()) {
                for (MailField requiredField : requiredFields) {
                    fields.add(requiredField);
                }
            }
            Collection<String> requiredHeaders = handler.getRequiredHeaders();
            if (null != requiredHeaders && !requiredHeaders.isEmpty()) {
                for (String requiredHeader : requiredHeaders) {
                    headerNames.add(requiredHeader);
                }
            }
        }

        state.put(STATE_PARAM_HANDLER, handler);
        return MailAttributation.builder(fields.isEmpty() ? null : fields.toArray(), headerNames.isEmpty() ? null : headerNames.toArray(new String[headerNames.size()])).build();
    }

    @Override
    public MailFetchListenerResult onAfterFetch(MailMessage[] mails, boolean cacheable, Session session, Map<String, Object> state) throws OXException {
        MailAuthenticityHandler handler = (MailAuthenticityHandler) state.get(STATE_PARAM_HANDLER);
        if (null == handler) {
            return MailFetchListenerResult.neutral(mails, cacheable);
        }

        for (MailMessage mail : mails) {
            handler.handle(session, mail);
        }
        return MailFetchListenerResult.neutral(mails, cacheable);
    }

}
