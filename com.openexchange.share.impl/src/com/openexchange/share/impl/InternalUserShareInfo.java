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

package com.openexchange.share.impl;

import java.util.Date;
import java.util.Locale;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.notify.hostname.HostData;
import com.openexchange.share.AuthenticationMode;
import com.openexchange.share.GuestInfo;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.ShareTargetPath;
import com.openexchange.share.core.tools.ShareLinks;
import com.openexchange.share.recipient.RecipientType;

/**
 * {@link InternalUserShareInfo}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.0
 */
public class InternalUserShareInfo extends AbstractShareInfo {

    private final int contextID;
    private final User user;

    /**
     * Initializes a new {@link InternalUserShareInfo}.
     *
     * @param contextID The context identifier
     * @param user The user
     * @param srcTarget The share target from the sharing users point of view
     * @param dstTarget The share target from the recipients point of view
     */
    public InternalUserShareInfo(int contextID, User user, ShareTarget srcTarget, ShareTarget dstTarget) {
        super(srcTarget, dstTarget);
        this.contextID = contextID;
        this.user = user;
    }

    @Override
    public String getShareURL(HostData hostData) throws OXException {
        return ShareLinks.generateInternal(hostData, getDestinationTarget());
    }

    @Override
    public GuestInfo getGuest() {
        /*
         * use special guest info for internal user
         */
        return new GuestInfo() {

            @Override
            public RecipientType getRecipientType() {
                return RecipientType.USER;
            }

            @Override
            public String getPassword() {
                return null;
            }

            @Override
            public Locale getLocale() {
                return user.getLocale();
            }

            @Override
            public int getGuestID() {
                return user.getId();
            }

            @Override
            public String getEmailAddress() {
                return user.getMail();
            }

            @Override
            public String getDisplayName() {
                return user.getDisplayName();
            }

            @Override
            public int getCreatedBy() {
                return 0;
            }

            @Override
            public int getContextID() {
                return contextID;
            }

            @Override
            public String getBaseToken() {
                return null;
            }

            @Override
            public AuthenticationMode getAuthentication() {
                return null;
            }

            @Override
            public ShareTarget getLinkTarget() {
                return null;
            }

            @Override
            public Date getExpiryDate() {
                return null;
            }

            @Override
            public String generateLink(HostData hostData, ShareTargetPath targetPath) throws OXException {
                return ShareLinks.generateInternal(hostData, getDestinationTarget());
            }
        };
    }

}
