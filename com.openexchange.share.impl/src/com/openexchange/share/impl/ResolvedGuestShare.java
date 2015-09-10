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

package com.openexchange.share.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.server.ServiceLookup;
import com.openexchange.share.GuestInfo;
import com.openexchange.share.GuestShare;
import com.openexchange.share.PersonalizedShareTarget;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.core.tools.ShareTool;
import com.openexchange.share.groupware.ModuleSupport;

/**
 * {@link ResolvedGuestShare}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.0
 */
public class ResolvedGuestShare implements GuestShare {

    protected final DefaultGuestInfo guestInfo;
    protected final ServiceLookup services;
    protected final Date expiryDate;
    protected final Map<PersonalizedShareTarget, ShareTarget> targetMap;

    /**
     * Initializes a new {@link ResolvedGuestShare}, performing optional personalizations of the share targets for the session's user
     * implicitly.
     *
     * @param services A service lookup reference
     * @param contextID The context ID
     * @param guestUser The guest user
     * @param targets The share targets
     * @param personalizedTargets The list of personalized (for the guest user) share targets. Must be in the same order as <code>shares</code>.
     * @throws OXException
     */
    public ResolvedGuestShare(ServiceLookup services, int contextID, User guestUser, List<ShareTarget> targets, List<PersonalizedShareTarget> personalizedTargets) throws OXException {
        super();
        this.services = services;
        if (ShareTool.isAnonymousGuest(guestUser)) {
            this.guestInfo = new DefaultGuestInfo(services, contextID, guestUser, targets.get(0));
        } else {
            this.guestInfo = new DefaultGuestInfo(services, contextID, guestUser, null);
        }
        expiryDate = guestInfo.getExpiryDate();
        this.targetMap = new HashMap<>(targets.size() * 2);
        services.getService(ModuleSupport.class);
        Iterator<ShareTarget> sit = targets.iterator();
        Iterator<PersonalizedShareTarget> pit = personalizedTargets.iterator();
        while (sit.hasNext()) {
            ShareTarget target = sit.next();
            PersonalizedShareTarget personalizedTarget = pit.next();
            targetMap.put(personalizedTarget, target);
        }
    }

    @Override
    public GuestInfo getGuest() {
        return guestInfo;
    }

    @Override
    public List<ShareTarget> getTargets() {
        return new ArrayList<>(targetMap.values());
    }

    @Override
    public List<PersonalizedShareTarget> getPersonalizedTargets() {
        return new ArrayList<>(targetMap.keySet());
    }

    @Override
    public int getCommonModule() {
        Collection<ShareTarget> targets = targetMap.values();
        if (null == targets || 0 == targets.size()) {
            return 0;
        } else {
            Iterator<ShareTarget> it = targets.iterator();
            int module = it.next().getModule();
            while (it.hasNext()) {
                if (module != it.next().getModule()) {
                    return 0;
                }
            }
            return module;
        }
    }

    @Override
    public String getCommonFolder() {
        Collection<ShareTarget> targets = targetMap.values();
        if (null == targets || 0 == targets.size()) {
            return null;
        } else {
            Iterator<ShareTarget> it = targets.iterator();
            String folder = it.next().getFolder();
            if (null == folder) {
                return null;
            }

            while (it.hasNext()) {
                if (false == folder.equals(it.next().getFolder())) {
                    return null;
                }
            }
            return folder;
        }
    }

    @Override
    public PersonalizedShareTarget resolvePersonalizedTarget(String path) {
        if (path == null) {
            return null;
        }

        if (null != targetMap && 0 < targetMap.size() && null != path) {
            for (Entry<PersonalizedShareTarget, ShareTarget> entry : targetMap.entrySet()) {
                if (path.equals(entry.getValue().getPath())) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }

    @Override
    public ShareTarget resolveTarget(PersonalizedShareTarget personalizedTarget) {
        return targetMap.get(personalizedTarget);
    }

    @Override
    public boolean isMultiTarget() {
        return null != targetMap && 1 < targetMap.size();
    }

    @Override
    public ShareTarget getSingleTarget() {
        return null != targetMap && 1 == targetMap.size() ? targetMap.values().iterator().next() : null;
    }

    @Override
    public Date getExpiryDate() {
        return expiryDate;
    }

    @Override
    public String toString() {
        return "ResolvedGuestShare [contextID=" + guestInfo.getContextID() + ", guestUser=" + guestInfo.getGuestID() + ", targets=" + targetMap.values() + "]";
    }

}
