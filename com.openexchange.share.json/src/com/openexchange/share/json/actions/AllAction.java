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

package com.openexchange.share.json.actions;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.java.Autoboxing;
import com.openexchange.server.ServiceLookup;
import com.openexchange.share.AuthenticationMode;
import com.openexchange.share.Share;
import com.openexchange.share.ShareCryptoService;
import com.openexchange.share.ShareService;
import com.openexchange.share.json.GuestShare;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AllAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.0
 */
public class AllAction extends AbstractShareAction {

    /**
     * Initializes a new {@link AllAction}.
     *
     * @param services The service lookup
     * @param translatorFactory
     */
    public AllAction(ServiceLookup services) {
        super(services);
    }

    @Override
    public AJAXRequestResult perform(AJAXRequestData requestData, ServerSession session) throws OXException {
        ShareService shareService = getShareService();
        List<Share> shares = shareService.getAllShares(session);
        if (null == shares || 0 == shares.size()) {
            return new AJAXRequestResult(new JSONArray());
        }
        List<String> shareURLs = generateShareURLs(session.getContextId(), shares, requestData);
        Date lastModified = null;
        Set<Integer> guestIDs = new HashSet<Integer>();
        for (Share share : shares) {
            Date shareLastModified = share.getModified();
            if (null == lastModified || null != shareLastModified && shareLastModified.after(lastModified)) {
                lastModified = shareLastModified;
            }
            guestIDs.add(Integer.valueOf(share.getGuest()));
        }
        User[] guestUsers = getUserService().getUser(session.getContext(), Autoboxing.I2i(guestIDs));
        Map<Integer, GuestInfo> guestUsersByID = new HashMap<Integer, GuestInfo>();
        for (User user : guestUsers) {
            AuthenticationMode authMode = shareService.getAuthenticationMode(session.getContextId(), user.getId());
            guestUsersByID.put(Integer.valueOf(user.getId()), new GuestInfo(user, authMode));
        }
        ShareCryptoService cryptoService = services.getService(ShareCryptoService.class);
        List<GuestShare> guestShares = new ArrayList<GuestShare>(shares.size());
        for (int i = 0; i < shares.size(); i++) {
            Share share = shares.get(i);
            String shareURL = shareURLs.get(i);
            GuestInfo guestInfo = guestUsersByID.get(Integer.valueOf(share.getGuest()));
            User guest = guestInfo.getGuest();

            String guestPassword = AuthenticationMode.ANONYMOUS_PASSWORD == guestInfo.getAuthMode() ? cryptoService.decrypt(guest.getUserPassword()) : null;
            guestShares.add(new GuestShare(share, guest, guestPassword, shareURL));
        }
        return new AJAXRequestResult(guestShares, lastModified, "guestshare");
    }

    private static final class GuestInfo {

        private final User guest;

        private final AuthenticationMode authMode;

        public GuestInfo(User guest, AuthenticationMode authMode) {
            super();
            this.guest = guest;
            this.authMode = authMode;
        }

        public User getGuest() {
            return guest;
        }

        public AuthenticationMode getAuthMode() {
            return authMode;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((authMode == null) ? 0 : authMode.hashCode());
            result = prime * result + ((guest == null) ? 0 : guest.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            GuestInfo other = (GuestInfo) obj;
            if (authMode != other.authMode)
                return false;
            if (guest == null) {
                if (other.guest != null)
                    return false;
            } else if (!guest.equals(other.guest))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return "GuestInfo [guest=" + guest + ", authMode=" + authMode + "]";
        }

    }

}
