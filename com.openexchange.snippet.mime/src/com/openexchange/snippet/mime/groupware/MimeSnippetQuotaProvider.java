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

package com.openexchange.snippet.mime.groupware;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.quota.AccountQuota;
import com.openexchange.quota.DefaultAccountQuota;
import com.openexchange.quota.Quota;
import com.openexchange.quota.QuotaExceptionCodes;
import com.openexchange.quota.QuotaProvider;
import com.openexchange.quota.QuotaType;
import com.openexchange.quota.groupware.AmountQuotas;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.session.Session;
import com.openexchange.snippet.mime.MimeSnippetManagement;
import com.openexchange.snippet.mime.MimeSnippetService;
import com.openexchange.snippet.mime.Services;

/**
 * {@link MimeSnippetQuotaProvider}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class MimeSnippetQuotaProvider implements QuotaProvider {

    private static final String MODULE_ID = "mime_snippet";
    private static final String PROP_AMOUNT_LIMIT = "com.openexchange.snippet.quota.limit";

    private final AtomicReference<MimeSnippetService> snippetServiceRef;

    /**
     * Initializes a new {@link MimeSnippetQuotaProvider}.
     */
    public MimeSnippetQuotaProvider() {
        super();
        this.snippetServiceRef = new AtomicReference<MimeSnippetService>(null);
    }

    /**
     * Sets the associated snippet service
     *
     * @param quotaProvider The snippet service to set
     */
    public void setSnippetService(MimeSnippetService snippetService) {
        this.snippetServiceRef.set(snippetService);
    }

    @Override
    public String getModuleID() {
        return MODULE_ID;
    }

    @Override
    public String getDisplayName() {
        return "Snippet";
    }

    private Quota getAmountQuota(Session session) throws OXException {
        ConfigViewFactory viewFactory = Services.getService(ConfigViewFactory.class);
        if (viewFactory == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(ConfigViewFactory.class.getName());
        }

        long limit = AmountQuotas.getConfiguredLimitByPropertyName(session, PROP_AMOUNT_LIMIT, viewFactory);
        if (limit <= Quota.UNLIMITED) {
            return Quota.UNLIMITED_AMOUNT;
        }
        long usage = snippetServiceRef.get().getManagement(session).getOwnSnippetsCount();
        return new Quota(QuotaType.AMOUNT, limit, usage);
    }

    private Quota optSizeQuota(Session session) throws OXException {
        MimeSnippetManagement snippetManagement = snippetServiceRef.get().getManagement(session);
        if (false == snippetManagement.hasQuota()) {
            // No storage quota
            return null;
        }

        // Retrieve size limit
        long limit = snippetManagement.getLimit();
        if (limit <= Quota.UNLIMITED) {
            return Quota.UNLIMITED_SIZE;
        }

        // Retrieve usage as well and return
        return new Quota(QuotaType.SIZE, limit, snippetManagement.getUsage());
    }

    @Override
    public AccountQuota getFor(Session session, String accountID) throws OXException {
        if (!accountID.equals("0")) {
            throw QuotaExceptionCodes.UNKNOWN_ACCOUNT.create(accountID, MODULE_ID);
        }

        Quota amountQuota = getAmountQuota(session);
        DefaultAccountQuota accountQuota = new DefaultAccountQuota(accountID, getDisplayName()).addQuota(amountQuota);
        Quota sizeQuota = optSizeQuota(session);
        if (null != sizeQuota) {
            accountQuota.addQuota(sizeQuota);
        }
        return accountQuota;
    }

    @Override
    public List<AccountQuota> getFor(Session session) throws OXException {
        return Collections.singletonList(getFor(session, "0"));
    }

}
