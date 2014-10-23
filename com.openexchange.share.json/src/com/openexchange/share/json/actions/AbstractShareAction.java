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

package com.openexchange.share.json.actions;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.exception.OXException;
import com.openexchange.i18n.Translator;
import com.openexchange.i18n.TranslatorFactory;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.share.ShareList;
import com.openexchange.share.ShareService;
import com.openexchange.share.groupware.ModuleHandler;
import com.openexchange.share.groupware.ModuleHandlerProvider;
import com.openexchange.share.notification.ShareNotificationService;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.UserService;

/**
 * {@link AbstractShareAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.0
 */
public abstract class AbstractShareAction implements AJAXActionService {

    protected final ServiceLookup services;

    /**
     * Initializes a new {@link AbstractShareAction}.
     *
     * @param services The service lookup reference
     * @param translatorFactory
     */
    public AbstractShareAction(ServiceLookup services) {
        super();
        this.services = services;
    }

    /**
     * Gets the share service.
     *
     * @return The share service
     * @throws OXException if the service is unavailable
     */
    protected ShareService getShareService() throws OXException {
        ShareService service = services.getService(ShareService.class);
        if (service == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(ShareService.class.getName());
        }
        return service;
    }

    /**
     * Gets the {@link ShareNotificationService}.
     *
     * @return The {@link ShareNotificationService}.
     * @throws OXException if the service is unavailable
     */
    protected ShareNotificationService getNotificationService() throws OXException {
        ShareNotificationService service = services.getService(ShareNotificationService.class);
        if (service == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(ShareNotificationService.class.getName());
        }
        return service;
    }

    /**
     * Gets the {@link UserService}.
     * @return The {@link UserService}.
     * @throws OXException if the service is unavailable
     */
    protected UserService getUserService() throws OXException {
        UserService service = services.getService(UserService.class);
        if (service == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(UserService.class.getName());
        }
        return service;
    }

    /**
     * Gets a {@link Translator} for the session users locale.
     * @param session The session
     * @return The translator
     * @throws OXException
     */
    protected Translator getTranslator(ServerSession session) throws OXException {
        TranslatorFactory translatorFactory = services.getService(TranslatorFactory.class);
        if (translatorFactory == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(TranslatorFactory.class.getName());
        }
        return translatorFactory.translatorFor(session.getUser().getLocale());
    }

    /**
     * Gets the {@link ModuleHandler} for a given module.
     * @param module The module
     * @return The handler
     * @throws OXException if no handler is available
     */
    protected ModuleHandler getModuleHandler(int module) throws OXException {
        ModuleHandlerProvider provider = services.getService(ModuleHandlerProvider.class);
        if (provider == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(UserService.class.getName());
        }

        return provider.getHandler(module);
    }

    /**
     * Generates a URL for every share that is passed.
     *
     * @param shares A list of shares
     * @param requestData The request data
     * @return A list of URLs, one for every share. The URLs are guaranteed to be in the same order as their according shares.
     * @throws OXException
     */
    protected List<String> generateShareURLs(List<ShareList> shares, AJAXRequestData requestData) throws OXException {
        return getShareService().generateShareURLs(shares, determineProtocol(requestData), determineHostname(requestData));
    }

    protected static String determineProtocol(AJAXRequestData requestData) {
        HttpServletRequest servletRequest = requestData.optHttpServletRequest();
        if (null != servletRequest) {
            return com.openexchange.tools.servlet.http.Tools.getProtocol(servletRequest);
        } else {
            return requestData.isSecure() ? "https://" : "http://";
        }
    }

    protected static String determineHostname(AJAXRequestData requestData) {
        HttpServletRequest servletRequest = requestData.optHttpServletRequest();
        if (null != servletRequest) {
            return servletRequest.getServerName();
        } else {
            return requestData.getHostname();
        }
    }

}
