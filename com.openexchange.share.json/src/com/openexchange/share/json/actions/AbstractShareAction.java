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

package com.openexchange.share.json.actions;

import static com.openexchange.osgi.Tools.requireService;
import java.util.TimeZone;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.i18n.Translator;
import com.openexchange.i18n.TranslatorFactory;
import com.openexchange.server.ServiceLookup;
import com.openexchange.share.ShareService;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.groupware.ModuleSupport;
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
    private final ShareJSONParser parser;

    /**
     * Initializes a new {@link AbstractShareAction}.
     *
     * @param services The service lookup reference
     */
    public AbstractShareAction(ServiceLookup services) {
        super();
        this.services = services;
        this.parser = new ShareJSONParser(services);
    }

    /**
     * Gets the JSON parser.
     *
     * @return The parser
     */
    protected ShareJSONParser getParser() {
        return parser;
    }

    /**
     * Gets the share service.
     *
     * @return The share service
     * @throws OXException if the service is unavailable
     */
    protected ShareService getShareService() throws OXException {
        return requireService(ShareService.class, services);
    }

    /**
     * Gets the module support service.
     *
     * @return The module support service
     * @throws OXException if the service is unavailable
     */
    protected ModuleSupport getModuleSupport() throws OXException {
        return requireService(ModuleSupport.class, services);
    }

    /**
     * Gets the {@link ShareNotificationService}.
     *
     * @return The {@link ShareNotificationService}.
     * @throws OXException if the service is unavailable
     */
    protected ShareNotificationService getNotificationService() throws OXException {
        return requireService(ShareNotificationService.class, services);
    }

    /**
     * Gets the {@link UserService}.
     * @return The {@link UserService}.
     * @throws OXException if the service is unavailable
     */
    protected UserService getUserService() throws OXException {
        return requireService(UserService.class, services);
    }

    /**
     * Gets the {@link ContextService}
     * @return The {@link ContextService}
     * @throws OXException if the service is unavailable
     */
    protected ContextService getContextService() throws OXException {
        return requireService(ContextService.class, services);
    }

    /**
     * Gets a {@link Translator} for the session users locale.
     * @param session The session
     * @return The translator
     * @throws OXException
     */
    protected Translator getTranslator(ServerSession session) throws OXException {
        TranslatorFactory translatorFactory = requireService(TranslatorFactory.class, services);
        return translatorFactory.translatorFor(session.getUser().getLocale());
    }

    protected static TimeZone getTimeZone(AJAXRequestData requestData, ServerSession session) {
        String timeZoneID = requestData.getParameter("timezone");
        if (null == timeZoneID) {
            timeZoneID = session.getUser().getTimeZone();
        }
        TimeZone timeZone = TimeZone.getTimeZone(timeZoneID);
        return timeZone;
    }

    /**
     * Gets the string representation of a share targets module identifier.
     *
     * @param target The share target
     * @return The module string
     */
    protected String moduleFor(ShareTarget target) throws OXException {
        return getModuleSupport().getShareModule(target.getModule());
    }

}
