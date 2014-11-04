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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.share.impl.groupware;

import java.util.Collections;
import java.util.List;
import org.apache.commons.lang.Validate;
import com.openexchange.config.cascade.ConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.quota.AccountQuota;
import com.openexchange.quota.DefaultAccountQuota;
import com.openexchange.quota.Quota;
import com.openexchange.quota.QuotaProvider;
import com.openexchange.quota.QuotaType;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.share.impl.DefaultShareService;

/**
 * {@link ShareQuotaProvider}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */
public class ShareQuotaProvider implements QuotaProvider {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ShareQuotaProvider.class);

    private static final String MODULE_ID = "share";

    private final DefaultShareService shareService;

    private final ServiceLookup services;

    public ShareQuotaProvider(ServiceLookup services, DefaultShareService shareService) {
        super();

        Validate.notNull(services, "ServiceLookup might not be null!");
        Validate.notNull(shareService, "ShareService might not be null!");
        this.services = services;
        this.shareService = shareService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getModuleID() {
        return MODULE_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDisplayName() {
        return "Shares";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AccountQuota> getFor(Session session) throws OXException {
        return Collections.singletonList(getFor(session, Integer.toString(session.getUserId())));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AccountQuota getFor(Session session, String accountID) throws OXException {
        ConfigViewFactory viewFactory = services.getService(ConfigViewFactory.class);
        if (viewFactory == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(ConfigViewFactory.class.getName());
        }

        final ConfigView configView = viewFactory.getView(session.getUserId(), session.getContextId());
        long limit = getQuota(configView);
        if (limit == Quota.UNLIMITED) {
            return new DefaultAccountQuota(accountID, getDisplayName()).addQuota(Quota.UNLIMITED_AMOUNT);
        }

        int usedQuota = shareService.getUsedQuota(session.getContextId(), Integer.parseInt(accountID));

        return new DefaultAccountQuota(accountID, getDisplayName()).addQuota(new Quota(QuotaType.AMOUNT, limit, usedQuota));
    }

    /**
     * Returns the defined quota limit for defined for the user.
     *
     * @param configView - view for the given user.
     * @return long - quota limit for the given user
     * @throws OXException
     */
    protected long getQuota(final ConfigView configView) throws OXException {
        ConfigProperty<String> property = configView.property("com.openexchange.quota.share", String.class);
        if (!property.isDefined()) {
            LOG.warn("Property 'com.openxchange.quota.share' not found. Will use default value 150 as quota. Please define the property to change the value.");
            return 150;
        }
        try {
            return Long.parseLong(property.get().trim());
        } catch (final RuntimeException e) {
            LOG.warn("Could not parse value found for property 'com.openxchange.quota.share'. Will use default value 150 as quota.", e);
            return 150;
        }
    }
}
