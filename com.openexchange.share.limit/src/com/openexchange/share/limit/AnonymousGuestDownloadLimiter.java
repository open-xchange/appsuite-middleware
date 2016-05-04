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

package com.openexchange.share.limit;

import java.sql.Connection;
import java.util.Date;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.share.limit.impl.ConnectionHelper;
import com.openexchange.share.limit.internal.Services;
import com.openexchange.share.limit.storage.RdbFileAccessStorage;
import com.openexchange.share.limit.util.LimitConfig;
import com.openexchange.tools.servlet.limit.ActionLimiter;
import com.openexchange.tools.servlet.ratelimit.RateLimitedException;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.UserService;

/**
 * {@link AnonymousGuestDownloadLimiter} Implementation of {@link ActionLimiter} to limit download actions.
 * <p>
 * When registered this limiter is responsible for limiting document/zipdocuments/zipfolder action in files/infostore module (see com.openexchange.share.limit.DocumentLimiter.handles(String, String)).
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.2
 */
public class AnonymousGuestDownloadLimiter implements ActionLimiter {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AnonymousGuestDownloadLimiter.class);

    private final ConfigViewFactory configView;
    private final long sizeLimit;
    private final int countLimit;
    private final int limitTimeFrame;

    public AnonymousGuestDownloadLimiter(ConfigViewFactory configView) {
        this.configView = configView;

        this.countLimit = LimitConfig.countLimit();
        this.sizeLimit = LimitConfig.sizeLimit();
        this.limitTimeFrame = LimitConfig.timeFrame();
    }

    @Override
    public void check(AJAXRequestData request) throws OXException {
        ServerSession session = request.getSession();
        int contextId = session.getContextId();
        int userId = session.getUserId();

        dropObsoleteAccesses(contextId, userId);

        FileAccess limit = getLimit(contextId, userId);
        boolean enabled = isEnabled(limit);
        if (!enabled) {
            return;
        }

        FileAccess used = getUsed(limit);

        throwIfExceeded(limit, used);
    }

    protected void throwIfExceeded(FileAccess limit, FileAccess used) {
        Long timeFrameAsLong = limit.getTimeOfEndInMillis() - limit.getTimeOfStartInMillis();
        int timeFrame = timeFrameAsLong.intValue();

        if (FileAccess.isCountExceeded(limit, used)) {
            throw new RateLimitedException("429 Too Many Requests", timeFrame / 1000);
        }
        if (FileAccess.isSizeExceeded(limit, used)) {
            throw new RateLimitedException("429 Too Large Content Requested", timeFrame / 1000);
        }
    }

    /**
     * Drops all database entries for the given user (based on the provided {@link ServerSession}) that have become obsolete based on the server/context defined time frame
     * 
     * @param contextId The context id the user is assigned to
     * @param userId The id of the user in the context
     * @throws OXException
     */
    protected void dropObsoleteAccesses(int contextId, int userId) throws OXException {
        ConfigView view = this.configView.getView(0, contextId);
        Integer timeWindow = view.opt(LimitConfig.TIME_FRAME, Integer.class, this.limitTimeFrame);
        long now = new Date().getTime();
        long start = now - timeWindow;

        ConnectionHelper connectionHelper = new ConnectionHelper(contextId);
        try {
            RdbFileAccessStorage.getInstance().removeAccesses(contextId, userId, start, connectionHelper.getWritable());
            connectionHelper.commit();
        } finally {
            connectionHelper.backWritable();
        }
    }

    /**
     * Returns the defined limits (by configuration) for the given user
     * 
     * @param contextId The context id the user is assigned to
     * @param userId The id of the user in the context
     * @return {@link FileAccess} with desired information
     * @throws OXException
     */
    protected FileAccess getLimit(int contextId, int userId) throws OXException {
        ConfigView view = configView.getView(0, contextId);
        Long userSizeLimit = view.opt(LimitConfig.SIZE_LIMIT, Long.class, this.sizeLimit);
        Integer userCountLimit = view.opt(LimitConfig.COUNT_LIMIT, Integer.class, this.countLimit);
        Integer userLimitTimeFrame = view.opt(LimitConfig.TIME_FRAME, Integer.class, this.limitTimeFrame);
        long now = new Date().getTime();
        long start = now - userLimitTimeFrame;

        return new FileAccess(contextId, userId, start, now, userCountLimit, userSizeLimit);
    }

    /**
     * Returns if the feature is enabled by checking the configuration retrieved by calling com.openexchange.share.limit.DocumentLimiter.getLimits(int, int).
     * <p>
     * If both, size and count are smaller or equal zero, the feature is disabled.
     * 
     * @param allowedLimits {@link FileAccess} to check if enabled
     * @return
     */
    protected boolean isEnabled(FileAccess allowedLimits) {
        if (allowedLimits.getTimeOfStartInMillis() == allowedLimits.getTimeOfEndInMillis()) {
            return false;
        }
        if (allowedLimits.getCount() <= 0 && allowedLimits.getSize() <= 0) {
            return false;
        }
        return true;
    }

    /**
     * Sets the given {@link FileAccess}es as default and checks the persisted one against it.
     * 
     * @param limits The {@link FileAccess}es to check against
     * @return <code>true</code>, if one of the limits (size or count) is exceeded; otherwise <code>false</code>
     * @throws OXException
     */
    protected FileAccess getUsed(FileAccess limits) throws OXException {
        ConnectionHelper connectionHelper = new ConnectionHelper(limits.getContextId());

        long start = limits.getTimeOfStartInMillis();
        try {
            return RdbFileAccessStorage.getInstance().getUsage(limits.getContextId(), limits.getUserId(), start, connectionHelper.getReadOnly());
        } finally {
            connectionHelper.backReadOnly();
        }
    }

    @Override
    public boolean handles(String module, String action) throws OXException {
        if (Strings.isEmpty(module) || Strings.isEmpty(action)) {
            return false;
        }
        
        if (!module.toLowerCase().startsWith("files") && !module.toLowerCase().startsWith("infostore")) {
            return false;
        }
        if (!action.equalsIgnoreCase("document") && !action.equalsIgnoreCase("zipdocuments") && !action.equalsIgnoreCase("zipfolder")) {
            return false;
        }
        return true;
    }

    @Override
    public boolean handles(int contextId, int userId) throws OXException {
        UserService userService = Services.getService(UserService.class);

        User user = userService.getUser(userId, contextId);
        if (user != null) {
            return (user.isGuest() && Strings.isEmpty(user.getMail()));
        }
        return false;
    }

    @Override
    public void after(AJAXRequestData request, AJAXRequestResult result) throws OXException {
        ServerSession session = request.getSession();
        int contextId = session.getContextId();
        int userId = session.getUserId();

        FileAccess limits = getLimit(contextId, userId);
        boolean enabled = isEnabled(limits);
        if (!enabled) {
            return;
        }

        Object resultObject = result.getResultObject();

        long length = -1;
        if (AJAXRequestResult.ResultType.DIRECT == result.getType()) {
            Object object = result.getResponseProperties().get("X-Content-Size");
            if (object instanceof Long) {
                length = ((Long) object).longValue();
            }
            result.setResponseProperty("X-Content-Size", null);
        } else if (resultObject instanceof IFileHolder) {
            IFileHolder file = null;
            try {
                file = (IFileHolder) resultObject;
                length = file.getLength();
            } finally {
                Streams.close(file);
            }
        }
        if (length != -1) {
            ConnectionHelper connectionHelper = new ConnectionHelper(contextId);
            try {
                postCheck(limits, length, connectionHelper.getReadOnly());

                Connection writable = connectionHelper.getWritable();
                RdbFileAccessStorage.getInstance().addAccess(contextId, userId, length, writable);
                connectionHelper.commit();
            } finally {
                connectionHelper.back();
            }
        } else {
            LOG.warn("Unable to retrieve size for request. Cannot add file access!");
        }
    }

    /**
     * Checks if the processed file access will exceed the defined limits and prevents download if so.
     * 
     * @param limit The limit to check against
     * @param length The consumed size
     * @param readOnly The read only connection
     * @throws OXException
     */
    protected void postCheck(FileAccess limit, long length, Connection readOnly) throws OXException {
        FileAccess used = RdbFileAccessStorage.getInstance().getUsage(limit.getContextId(), limit.getUserId(), limit.getTimeOfStartInMillis(), readOnly);

        long size = used.getSize();
        used.setSize(size + length);
        int count = used.getCount();
        used.setCount(count++);

        throwIfExceeded(limit, used);
    }
}
