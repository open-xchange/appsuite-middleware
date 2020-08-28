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

package com.openexchange.share.impl.federated;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.osgi.framework.BundleContext;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.osgi.RankingAwareNearRegistryServiceTracker;
import com.openexchange.session.Session;
import com.openexchange.share.ShareTargetPath;
import com.openexchange.share.core.tools.ShareTool;
import com.openexchange.share.federated.FederatedShareLinkExceptions;
import com.openexchange.share.federated.FederatedShareLinkService;
import com.openexchange.share.federated.ShareLinkAnalyzeResult;
import com.openexchange.share.federated.ShareLinkManager;
import com.openexchange.share.federated.ShareLinkState;

/**
 * {@link FederatedShareLinkServiceImpl}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.5
 */
public class FederatedShareLinkServiceImpl extends RankingAwareNearRegistryServiceTracker<ShareLinkManager> implements FederatedShareLinkService {

    /**
     * Initializes a new {@link FederatedShareLinkServiceImpl}.
     * 
     * @param context The bundle context
     */
    public FederatedShareLinkServiceImpl(BundleContext context) {
        super(context, ShareLinkManager.class, 100);
    }

    @Override
    public ShareLinkAnalyzeResult analyzeLink(Session session, String shareLink) throws OXException {
        checkLink(shareLink);
        for (ShareLinkManager manager : getManagers(shareLink)) {
            ShareLinkState state = manager.analyzeLink(session, shareLink);
            if (null != state) {
                return new ShareLinkAnalyzeResult(manager.getId(), state);
            }
        }
        throw FederatedShareLinkExceptions.NOT_USABLE.create(shareLink);
    }

    @Override
    public String bindShare(Session session, String shareLink, String shareName, String password) throws OXException {
        checkLink(shareLink);
        for (ShareLinkManager manager : getManagers(shareLink)) {
            ShareLinkState state = manager.analyzeLink(session, shareLink);
            if (null != state) {
                if (ShareLinkState.ADDABLE_WITH_PASSWORD.equals(state)) {
                    checkPassword(password);
                    return manager.bindShare(session, shareLink, shareName, password);
                }
                if (ShareLinkState.ADDABLE.equals(state)) {
                    return manager.bindShare(session, shareLink, shareName, password);
                }
                break;
            }
        }
        throw FederatedShareLinkExceptions.NOT_USABLE.create(shareLink);
    }

    @Override
    public void update(Session session, String shareLink, String password) throws OXException {
        checkLink(shareLink);
        for (ShareLinkManager manager : getManagers(shareLink)) {
            ShareLinkState state = manager.analyzeLink(session, shareLink);
            if (null != state) {
                if (ShareLinkState.CREDENTIALS_REFRESH.equals(state)) {
                    checkPassword(password);
                    manager.updateShare(session, shareLink, password);
                    return;
                }
                break;
            }
        }
        throw FederatedShareLinkExceptions.NOT_USABLE.create(shareLink);
    }

    @Override
    public void unbindShare(Session session, String shareLink) throws OXException {
        checkLink(shareLink);
        for (ShareLinkManager manager : getManagers(shareLink)) {
            ShareLinkState state = manager.analyzeLink(session, shareLink);
            if (null != state) {
                if (ShareLinkState.SUBSCRIBED.equals(state)) {
                    manager.unbindShare(session, shareLink);
                    return;
                }
                break;
            }
        }
        throw FederatedShareLinkExceptions.NOT_USABLE.create(shareLink);
    }

    /*
     * ============================== HELPERS ==============================
     */

    private void checkLink(String shareLink) throws OXException {
        if (Strings.isEmpty(shareLink)) {
            throw FederatedShareLinkExceptions.MISSING_LINK.create();
        }
        if (false == ShareTool.isShare(shareLink)) {
            throw FederatedShareLinkExceptions.NOT_USABLE.create(shareLink);
        }
    }

    private void checkPassword(String password) throws OXException {
        if (Strings.isEmpty(password)) {
            throw FederatedShareLinkExceptions.MISSING_PASSWORD.create();
        }
    }

    /**
     * Returns a list with managers for the module in the link
     *
     * @param shareLink The share link
     * @return A list of managers in order or an empty list
     */
    private List<ShareLinkManager> getManagers(String shareLink) {
        if (false == ShareTool.isShare(shareLink)) {
            return Collections.emptyList();
        }

        ShareTargetPath shareTarget = ShareTool.getShareTarget(shareLink);
        if (null == shareTarget || shareTarget.getModule() < 1) {
            return Collections.emptyList();
        }

        LinkedList<ShareLinkManager> managers = new LinkedList<>();
        int module = shareTarget.getModule();
        for (Iterator<ShareLinkManager> iterator = iterator(); iterator.hasNext();) {
            ShareLinkManager manager = iterator.next();
            if (module == manager.getSupportedModule()) {
                managers.add(manager);
            }
        }
        return managers;
    }

}
