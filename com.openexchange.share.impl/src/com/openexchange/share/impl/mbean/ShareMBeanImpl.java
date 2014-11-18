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

package com.openexchange.share.impl.mbean;

import java.util.Collections;
import java.util.List;
import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;
import com.openexchange.exception.OXException;
import com.openexchange.share.Share;
import com.openexchange.share.ShareInfo;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.impl.DefaultShareService;

/**
 * {@link ShareMBeanImpl}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8.0
 */
public class ShareMBeanImpl extends StandardMBean implements ShareMBean {

    private final DefaultShareService shareService;

    public ShareMBeanImpl(Class<?> mbeanInterface, DefaultShareService shareService) throws NotCompliantMBeanException {
        super(mbeanInterface);
        this.shareService = shareService;
    }

    @Override
    public String listShares(int contextId) throws OXException {
        return formatForCLT(shareService.getAllShares(contextId));
    }

    @Override
    public String listShares(int contextId, int userId) throws OXException {
        return formatForCLT(shareService.getAllShares(contextId, userId));
    }

    @Override
    public int removeShare(String token, String path) throws OXException {
        if (null != path && !path.isEmpty() && !"".equals(path)) {
            token = token + "/" + path;
        }
        return shareService.removeShares(Collections.singletonList(token));
    }

    @Override
    public int removeShare(String token, String path, int contextId) throws OXException {
        if (null != path && !path.isEmpty() && !"".equals(path)) {
            token = token + "/" + path;
        }
        return shareService.removeShares(Collections.singletonList(token), contextId);
    }

    @Override
    public int removeShares(int contextId) throws OXException {
        return shareService.removeShares(contextId);
    }

    @Override
    public int removeShares(int contextId, int userId) throws OXException {
        return shareService.removeShares(contextId, userId);
    }

    private static String formatForCLT(List<ShareInfo> shareInfo) throws OXException {
        StringBuilder sb = new StringBuilder();
        for (ShareInfo info : shareInfo) {
            sb.append("Token: ").append(info.getToken()).append(" (");
            Share share = info.getShare();
            ShareTarget target = share.getTarget();
            sb.append("Share [created by ").append(share.getCreatedBy()).append(", guest=").append(share.getGuest())
              .append(", target=").append("ShareTarget [module=").append(target.getModule()).append(", folder=").append(target.getFolder())
              .append((null != target.getItem() ? (", item=" + target.getItem()) : "") + "]").append("]").append(")");
            sb.append("\n");
        }
        return sb.toString();
    }

}
