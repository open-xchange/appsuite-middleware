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

package com.openexchange.share.handler.download.limiter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.responseRenderers.RenderListener;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.share.limit.limiter.FilesDownloadLimiter;
import com.openexchange.share.limit.limiter.GuestDownloadLimiter;
import com.openexchange.share.limit.limiter.InfostoreDownloadLimiter;
import com.openexchange.share.limit.limiter.exceptions.DownloadLimitedExceptionCode;
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
    public void onBeforeWrite(AJAXRequestData request) throws OXException {
        try {
            super.onRequestInitialized(request);
        } catch (OXException oxException) {
            if (oxException.similarTo(DownloadLimitedExceptionCode.COUNT_EXCEEDED) || oxException.similarTo(DownloadLimitedExceptionCode.LIMIT_EXCEEDED)) {
                throw new RateLimitedException("429 Download Limits Exceeded", 0);
            }
            throw oxException;
        }
    }

    @Override
    public void onAfterWrite(AJAXRequestData request, AJAXRequestResult result) {
        super.onRequestPerformed(request, result, null);
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
