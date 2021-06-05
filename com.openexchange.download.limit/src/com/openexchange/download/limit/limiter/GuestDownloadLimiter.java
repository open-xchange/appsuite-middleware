/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.download.limit.limiter;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
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
import com.openexchange.download.limit.FileAccess;
import com.openexchange.download.limit.internal.ConnectionHelper;
import com.openexchange.download.limit.limiter.exceptions.DownloadLimitedExceptionCode;
import com.openexchange.download.limit.storage.RdbFileAccessStorage;
import com.openexchange.download.limit.util.LimitConfig;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.User;

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
            throw DownloadLimitedExceptionCode.COUNT_EXCEEDED.create(I(limit.getUserId()), I(limit.getContextId()), I(limit.getCount()));
        }
        if (FileAccess.isSizeExceeded(limit, used)) {
            throw DownloadLimitedExceptionCode.LIMIT_EXCEEDED.create(I(limit.getUserId()), I(limit.getContextId()), L(limit.getSize()), L(used.getSize()));
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
        long start;
        ConfigView view = this.configViewFactory.getView(0, contextId);
        if (Strings.isEmpty(user.getMail())) { // anonymous guest
            Integer timeWindow = view.opt(LimitConfig.TIME_FRAME_LINKS, Integer.class, I(LimitConfig.getInstance().timeFrameLinks()));
            long now = new Date().getTime();
            start = now - timeWindow.intValue();
        } else {
            Integer timeWindow = view.opt(LimitConfig.TIME_FRAME_GUESTS, Integer.class, I(LimitConfig.getInstance().timeFrameGuests()));
            long now = new Date().getTime();
            start = now - timeWindow.intValue();
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
            boolean enabled = view.opt(LimitConfig.LIMIT_ENABLED, Boolean.class, Boolean.FALSE).booleanValue();
            if (!enabled) {
                return null;
            }
            if (Strings.isEmpty(user.getMail())) { // anonymous guest
                Long userSizeLimit = view.opt(LimitConfig.SIZE_LIMIT_LINKS, Long.class, L(LimitConfig.getInstance().sizeLimitLinks()));
                Integer userCountLimit = view.opt(LimitConfig.COUNT_LIMIT_LINKS, Integer.class, I(LimitConfig.getInstance().countLimitLinks()));
                Integer userLimitTimeFrame = view.opt(LimitConfig.TIME_FRAME_LINKS, Integer.class, I(LimitConfig.getInstance().timeFrameLinks()));
                long now = new Date().getTime();
                long start = now - userLimitTimeFrame.intValue();

                return new FileAccess(contextId, user.getId(), start, now, userCountLimit.intValue(), userSizeLimit.longValue());
            }
            Long userSizeLimit = view.opt(LimitConfig.SIZE_LIMIT_GUESTS, Long.class, L(LimitConfig.getInstance().sizeLimitGuests()));
            Integer userCountLimit = view.opt(LimitConfig.COUNT_LIMIT_GUESTS, Integer.class, I(LimitConfig.getInstance().countLimitGuests()));
            Integer userLimitTimeFrame = view.opt(LimitConfig.TIME_FRAME_GUESTS, Integer.class, I(LimitConfig.getInstance().timeFrameGuests()));
            long now = new Date().getTime();
            long start = now - userLimitTimeFrame.intValue();

            return new FileAccess(contextId, user.getId(), start, now, userCountLimit.intValue(), userSizeLimit.longValue());
        } catch (OXException e) {
            LOG.warn("Unable to retrieve configured limits for user {} in context {}: {}", I(user.getId()), I(contextId), e.getMessage());
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
            LOG.warn("Unable to retrieve usage for user {} in context {}: {}", I(userId), I(contextId), e.getMessage());
        }
        return null;
    }

    @Override
    public boolean applicable(AJAXRequestData requestData) {
        if (!LimitConfig.getInstance().isEnabled()) {
            return false;
        }
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

        if ("document".equalsIgnoreCase(requestData.getAction())) {
            return "download".equalsIgnoreCase(requestData.getParameter("delivery")) || isTrue(requestData.getParameter("dl"));
        }
        return true;
    }

    protected static boolean isTrue(String value) {
        return "1".equals(value) || "yes".equalsIgnoreCase(value) || Boolean.valueOf(value).booleanValue();
    }

    @Override
    public void onRequestInitialized(AJAXRequestData requestData) throws OXException {
        ServerSession session = requestData.getSession();
        if (session == null) {
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
            LOG.info("Unable to delete obsolete entries for user {} in context {}. As this is just for cleanup reasons these entries won't be considered within further processings.", I(userId), I(contextId), e);
        }
    }

    @Override
    public void onRequestPerformed(AJAXRequestData requestData, AJAXRequestResult requestResult, Exception e) {
        if (e != null) { // aborted request due to limit exception
            return;
        }
        ServerSession session = requestData.getSession();
        if (session == null) {
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
            IFileHolder file = IFileHolder.class.cast(resultObject);
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
                LOG.error("Unable to execute post execution check and add access for user {} in context {}: {}", I(userId), I(contextId), oxException.getMessage());
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
