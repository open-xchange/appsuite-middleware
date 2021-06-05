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
