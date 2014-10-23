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

package com.openexchange.share.groupware;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import com.openexchange.share.Share;

/**
 * {@link ShareTargetDiff}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class ShareTargetDiff {

    private final List<Share> added;

    private final List<Share> removed;

    private final List<Share> modified;

    public ShareTargetDiff(List<Share> oldTargets, List<Share> newTargets) {
        super();
        added = new ArrayList<Share>(newTargets);
        added.removeAll(oldTargets);
        removed = new ArrayList<Share>(oldTargets);
        removed.removeAll(newTargets);
        modified = new ArrayList<Share>(newTargets.size());
        for (Share target : newTargets) {
            int index = oldTargets.indexOf(target);
            if (index >= 0) {
                Share oldTarget = oldTargets.remove(index);
                if (expiryDateChanged(target, oldTarget) || metaChanged(target, oldTarget)) {
                    Share updatedTarget = new Share(oldTarget.getModule(), oldTarget.getFolder(), oldTarget.getItem());
                    updatedTarget.setExpiryDate(target.getExpiryDate());
                    updatedTarget.setMeta(target.getMeta());
                    modified.add(updatedTarget);
                }
            }
        }
    }


    /**
     * Gets the added
     *
     * @return The added
     */
    public List<Share> getAdded() {
        return added;
    }


    /**
     * Gets the removed
     *
     * @return The removed
     */
    public List<Share> getRemoved() {
        return removed;
    }


    /**
     * Gets the modified
     *
     * @return The modified
     */
    public List<Share> getModified() {
        return modified;
    }

    private static boolean metaChanged(Share t1, Share t2) {
        Map<String, Object> m1 = t1.getMeta();
        Map<String, Object> m2 = t2.getMeta();
        if (m1 == null) {
            if (m2 == null) {
                return false;
            }

            return true;
        }

        if (m2 == null) {
            return true;
        }

        return !m1.equals(m2);
    }

    private static boolean expiryDateChanged(Share t1, Share t2) {
        Date d1 = t1.getExpiryDate();
        Date d2 = t2.getExpiryDate();
        if (d1 == null) {
            if (d2 == null) {
                return false;
            }

            return true;
        }

        if (d2 == null) {
            return true;
        }

        return !d1.equals(d2);
    }

}
