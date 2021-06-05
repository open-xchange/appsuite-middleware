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

package com.openexchange.share.handler.download.limiter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.responseRenderers.RenderListener;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.download.limit.limiter.FilesDownloadLimiter;
import com.openexchange.download.limit.limiter.GuestDownloadLimiter;
import com.openexchange.download.limit.limiter.InfostoreDownloadLimiter;
import com.openexchange.download.limit.limiter.exceptions.DownloadLimitedExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.tools.servlet.ratelimit.RateLimitedException;

/**
 * {@link ShareDownloadLimiter} - A {@link RenderListener} that is responsible for 'GET' actions in the 'share' module which are invoked directly via ShareHandler from the ShareServlet.
 * <p>
 * As this {@link RenderListener} is mostly similar to existing limiters it extends the {@link GuestDownloadLimiter} as it provides almost all functionality.
 *
 * @see {@link FilesDownloadLimiter}
 * @see {@link InfostoreDownloadLimiter}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.2
 */
public class ShareDownloadLimiter extends GuestDownloadLimiter implements RenderListener {

    public ShareDownloadLimiter(ConfigViewFactory configView) {
        super(configView);
    }

    @Override
    public boolean handles(AJAXRequestData request) {
        return applicable(request);
    }

    @Override
    public void onBeforeWrite(AJAXRequestData request, AJAXRequestResult result, HttpServletRequest req, HttpServletResponse resp) throws OXException {
        try {
            super.onRequestInitialized(request);
        } catch (OXException oxException) {
            if (DownloadLimitedExceptionCode.COUNT_EXCEEDED.equals(oxException) || DownloadLimitedExceptionCode.LIMIT_EXCEEDED.equals(oxException)) {
                throw new RateLimitedException("429 Download Limits Exceeded", 0);
            }
            throw oxException;
        }
    }

    @Override
    public void onAfterWrite(AJAXRequestData request, AJAXRequestResult result, Exception writeException) {
        super.onRequestPerformed(request, result, writeException);
    }

    @Override
    public String getModule() {
        return "share";
    }

    @Override
    public Set<String> getActions() {
        return new HashSet<>(Arrays.asList("GET"));
    }
}
