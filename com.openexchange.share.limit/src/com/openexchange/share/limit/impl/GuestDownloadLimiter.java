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

package com.openexchange.share.limit.impl;

import java.sql.Connection;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.ActionBoundDispatcherListener;
import com.openexchange.ajax.requesthandler.DispatcherListener;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.java.Strings;
import com.openexchange.share.limit.FileAccess;
import com.openexchange.share.limit.exceptions.custom.DownloadLimitedExceptionCode;
import com.openexchange.share.limit.exceptions.custom.DownloadLimitedExceptionMessages;
import com.openexchange.share.limit.storage.RdbFileAccessStorage;
import com.openexchange.share.limit.util.LimitConfig;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link GuestDownloadLimiter} Implementation of {@link DispatcherListener} to limit download actions.
 * <p>
 * When registered this limiter is responsible for limiting document/zipdocuments/zipfolder action in files/infostore module (see com.openexchange.share.limit.DocumentLimiter.handles(String, String)).
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.2
 */
public abstract class GuestDownloadLimiter extends ActionBoundDispatcherListener {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(GuestDownloadLimiter.class);

    private final ConfigViewFactory configViewFactory;

    protected GuestDownloadLimiter(ConfigViewFactory configView) {
        this.configViewFactory = configView;
    }

    protected void throwIfExceeded(FileAccess limit, FileAccess used) throws OXException {
        if (FileAccess.isCountExceeded(limit, used)) {
            String message = "User " + limit.getUserId() + " in context " + limit.getContextId() + " exceeded the defined count limit of " + limit.getCount() + ". The download will be denied.";
            throw new DownloadLimitedExceptionCode(message, DownloadLimitedExceptionMessages.DOWNLOAD_DENIED_EXCEPTION_MESSAGE, Category.CATEGORY_ERROR, 1).create();
        }
        if (FileAccess.isSizeExceeded(limit, used)) {
            String message = "User " + limit.getUserId() + " in context " + limit.getContextId() + " exceeded the defined size limit of " + limit.getSize() + " with " + used.getSize() + " bytes. The download will be denied.";
            throw new DownloadLimitedExceptionCode(message, DownloadLimitedExceptionMessages.DOWNLOAD_DENIED_EXCEPTION_MESSAGE, Category.CATEGORY_ERROR, 1).create();
        }
    }

    /**
     * Drops all database entries for the given user (based on the provided {@link ServerSession}) that have become obsolete based on the server/context defined time frame
     *
     * @param contextId The context id the user is assigned to
     * @param userId The id of the user in the context
     * @throws OXException
     */
    protected void dropObsoleteAccesses(User user, int contextId) throws OXException {
        if (!user.isGuest()) {
            return;
        }
        Long start = null;
        ConfigView view = this.configViewFactory.getView(0, contextId);
        if (Strings.isEmpty(user.getMail())) { // anonymous guest
            Integer timeWindow = view.opt(LimitConfig.TIME_FRAME_LINKS, Integer.class, LimitConfig.timeFrameLinks());
            long now = new Date().getTime();
            start = now - timeWindow;
        } else {
            Integer timeWindow = view.opt(LimitConfig.TIME_FRAME_GUESTS, Integer.class, LimitConfig.timeFrameGuests());
            long now = new Date().getTime();
            start = now - timeWindow;
        }
        ConnectionHelper connectionHelper = new ConnectionHelper(contextId);
        try {
            RdbFileAccessStorage.getInstance().removeAccesses(contextId, user.getId(), start, connectionHelper.getWritable());
            connectionHelper.commit();
        } finally {
            connectionHelper.backWritable();
        }
    }

    /**
     * Returns the defined limits (by configuration) for the given user or <code>null</code> if no limit can be found
     *
     * @param user The user
     * @param contextId The context id the user is assigned to
     * @return {@link FileAccess} with desired information
     */
    protected FileAccess getLimit(User user, int contextId) {
        if (!user.isGuest()) {
            return null;
        }
        try {
            ConfigView view = configViewFactory.getView(0, contextId);
            Boolean enabled = view.opt(LimitConfig.LIMIT_ENABLED, Boolean.class, Boolean.FALSE);
            if (!enabled) {
                return null;
            }
            if (Strings.isEmpty(user.getMail())) { // anonymous guest
                Long userSizeLimit = view.opt(LimitConfig.SIZE_LIMIT_LINKS, Long.class, LimitConfig.sizeLimitLinks());
                Integer userCountLimit = view.opt(LimitConfig.COUNT_LIMIT_LINKS, Integer.class, LimitConfig.countLimitLinks());
                Integer userLimitTimeFrame = view.opt(LimitConfig.TIME_FRAME_LINKS, Integer.class, LimitConfig.timeFrameLinks());
                long now = new Date().getTime();
                long start = now - userLimitTimeFrame;

                return new FileAccess(contextId, user.getId(), start, now, userCountLimit, userSizeLimit);
            } else {
                Long userSizeLimit = view.opt(LimitConfig.SIZE_LIMIT_GUESTS, Long.class, LimitConfig.sizeLimitGuests());
                Integer userCountLimit = view.opt(LimitConfig.COUNT_LIMIT_GUESTS, Integer.class, LimitConfig.countLimitGuests());
                Integer userLimitTimeFrame = view.opt(LimitConfig.TIME_FRAME_GUESTS, Integer.class, LimitConfig.timeFrameGuests());
                long now = new Date().getTime();
                long start = now - userLimitTimeFrame;

                return new FileAccess(contextId, user.getId(), start, now, userCountLimit, userSizeLimit);
            }
        } catch (OXException e) {
            LOG.warn("Unable to retrieve configured limits for user {} in context {}: {}", user.getId(), contextId, e.getMessage());
        }
        return null;
    }

    /**
     * Sets the given {@link FileAccess}es as default and checks the persisted one against it.
     *
     * @param limit The {@link FileAccess}es to check against
     * @return <code>true</code>, if one of the limits (size or count) is exceeded; otherwise <code>false</code>
     */
    protected FileAccess getUsed(FileAccess limit) {
        int contextId = limit.getContextId();
        int userId = limit.getUserId();
        try {
            ConnectionHelper connectionHelper = new ConnectionHelper(contextId);

            long start = limit.getTimeOfStartInMillis();
            try {
                return RdbFileAccessStorage.getInstance().getUsage(contextId, userId, start, connectionHelper.getReadOnly());
            } finally {
                connectionHelper.backReadOnly();
            }
        } catch (OXException e) {
            LOG.warn("Unable to retrieve usage for user {} in context {}: {}", userId, contextId, e.getMessage());
        }
        return null;
    }

    @Override
    public boolean applicable(AJAXRequestData requestData) {
        ServerSession session = requestData.getSession();
        if (null == session || session.isAnonymous()) {
            return false;
        }
        if (!session.getUser().isGuest()) { // if not a guest, skip
            return false;
        }

        if (!super.applicable(requestData)) {
            return false;
        }

        // TODO: Check for download explicitly for "document" action
        return "download".equals(requestData.getParameter("view"));
    }

    @Override
    public void onRequestInitialized(AJAXRequestData requestData) throws OXException {
        ServerSession session = requestData.getSession();
        if (null == session || session.isAnonymous()) {
            return;
        }
        int contextId = session.getContextId();

        removeOldAccesses(session, contextId);

        User user = session.getUser();
        FileAccess limit = getLimit(user, contextId);
        if ((limit == null) || (FileAccess.isDisabled(limit))) {
            return;
        }

        FileAccess used = getUsed(limit);
        if (used == null) {
            return;
        }

        throwIfExceeded(limit, used);
    }

    protected void removeOldAccesses(ServerSession session, int contextId) {
        try {
            dropObsoleteAccesses(session.getUser(), contextId);
        } catch (OXException e) {
            int userId = session.getUserId();
            LOG.info("Unable to delete obsolete entries for user {} in context {}. As this is just for cleanup reasons these entries won't be considered within further processings.", userId, contextId, e);
        }
    }

    @Override
    public void onRequestPerformed(AJAXRequestData requestData, AJAXRequestResult requestResult, Exception e) {
        if (e != null) { // aborted request due to limit exception
            return;
        }
        ServerSession session = requestData.getSession();
        if (null == session || session.isAnonymous()) {
            return;
        }

        int contextId = session.getContextId();

        FileAccess limit = getLimit(session.getUser(), contextId);
        if ((limit == null) || (FileAccess.isDisabled(limit))) {
            return;
        }

        Object resultObject = requestResult.getResultObject();

        long length = -1;
        if (AJAXRequestResult.ResultType.DIRECT == requestResult.getType()) {
            Object object = requestResult.getResponseProperties().get("X-Content-Size");
            if (object instanceof Long) {
                length = ((Long) object).longValue();
            }
            requestResult.setResponseProperty("X-Content-Size", null);
        } else if (resultObject instanceof IFileHolder) {
            IFileHolder file = (IFileHolder) resultObject;
            length = file.getLength();
        }
        if (length != -1) {
            ConnectionHelper connectionHelper = null;
            int userId = session.getUserId();
            try {
                connectionHelper = new ConnectionHelper(contextId);

                Connection writable = connectionHelper.getWritable();
                RdbFileAccessStorage.getInstance().addAccess(contextId, userId, length, writable);
                connectionHelper.commit();
            } catch (OXException oxException) {
                LOG.error("Unable to execute post execution check and add access for user {} in context {}: {}", userId, contextId, oxException.getMessage());
            } finally {
                if (connectionHelper != null) {
                    connectionHelper.back();
                }
            }
        } else {
            LOG.warn("Unable to retrieve size for request. Cannot add file access!");
        }
    }

    @Override
    public void onResultReturned(AJAXRequestData requestData, AJAXRequestResult requestResult, Exception e) {
        // nothing to do as com.openexchange.share.limit.AnonymousGuestDownloadLimiter.onRequestPerformed(AJAXRequestData, AJAXRequestResult, Exception) shouldn't be invoked
    }

    @Override
    public Set<String> getActions() {
        return new HashSet<String>(Arrays.asList("document", "zipdocuments", "zipfolder"));
    }

    @Override
    public abstract String getModule();
}
