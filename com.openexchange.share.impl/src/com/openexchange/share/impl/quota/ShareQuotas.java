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

package com.openexchange.share.impl.quota;

import static com.openexchange.java.Autoboxing.L;
import java.util.Collection;
import com.openexchange.exception.OXException;
import com.openexchange.quota.AccountQuota;
import com.openexchange.quota.Quota;
import com.openexchange.quota.QuotaExceptionCodes;
import com.openexchange.quota.QuotaProvider;
import com.openexchange.quota.QuotaService;
import com.openexchange.quota.QuotaType;
import com.openexchange.session.Session;
import com.openexchange.share.GuestInfo;
import com.openexchange.share.recipient.RecipientType;

/**
 * {@link ShareQuotas}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since 7.10.5
 */
public class ShareQuotas {

    private final Quota shareLinksQuota;
    private final Quota inviteGuestsQuota;

    /**
     * Initializes a new {@link ShareQuotas} and gets the current quotas for <code>share_links</code> and <code>invite_guests</code>.
     *
     * @param quotaService The quota service to use
     * @param session The session of the user the quota will be checked for
     */
    public ShareQuotas(QuotaService quotaService, Session session) throws OXException {
        super();
        this.shareLinksQuota = getAmountQuota(quotaService, session, "share_links", "0");
        this.inviteGuestsQuota = getAmountQuota(quotaService, session, "invite_guests", "0");
    }

    /**
     * Checks that the quota is not exceeded if the supplied new shares would be created.
     *
     * @param guestInfos The new shares that are about to be created
     * @throws OXException If quota would be exceeded
     */
    public void checkAllowsNewShares(Collection<GuestInfo> guestInfos) throws OXException {
        if (null != guestInfos && 0 < guestInfos.size()) {
            int numLinks = 0;
            int numGuests = 0;
            for (GuestInfo guestInfo : guestInfos) {
                if (RecipientType.ANONYMOUS.equals(guestInfo.getRecipientType())) {
                    numLinks++;
                } else if (RecipientType.GUEST.equals(guestInfo.getRecipientType())) {
                    numGuests++;
                }
            }
            checkAllowsNewGuests(numGuests);
            checkAllowsNewLinks(numLinks);
        }
    }

    /**
     * Checks that the quota is not exceeded if the given number of new share links would be created.
     *
     * @param numLinks The number of new share links being created
     * @throws OXException If quota would be exceeded
     */
    public void checkAllowsNewLinks(int numLinks) throws OXException {
        if (0 < numLinks && null != shareLinksQuota && (shareLinksQuota.isExceeded() || shareLinksQuota.willExceed(numLinks))) {
            throw QuotaExceptionCodes.QUOTA_EXCEEDED_SHARE_LINKS.create(L(shareLinksQuota.getUsage()), L(shareLinksQuota.getLimit()));
        }
    }

    /**
     * Checks that the quota is not exceeded if the given number of new guest users would be created.
     *
     * @param numLinks The number of new guest users being created
     * @throws OXException If quota would be exceeded
     */
    public void checkAllowsNewGuests(int numGuests) throws OXException {
        if (0 < numGuests && null != inviteGuestsQuota && (inviteGuestsQuota.isExceeded() || inviteGuestsQuota.willExceed(numGuests))) {
            throw QuotaExceptionCodes.QUOTA_EXCEEDED_INVITE_GUESTS.create(L(inviteGuestsQuota.getUsage()), L(inviteGuestsQuota.getLimit()));
        }
    }

    private Quota getAmountQuota(QuotaService quotaService, Session session, String provider, String accountId) throws OXException {
        QuotaProvider quotaProvider = quotaService.getProvider(provider);
        if (null != quotaProvider) {
            AccountQuota accountQuota = quotaProvider.getFor(session, "0");
            if (null != accountQuota && accountQuota.hasQuota(QuotaType.AMOUNT)) {
                return accountQuota.getQuota(QuotaType.AMOUNT);
            }
        }
        return null;
    }

}
