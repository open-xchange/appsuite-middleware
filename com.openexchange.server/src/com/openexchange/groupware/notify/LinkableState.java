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

package com.openexchange.groupware.notify;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Pattern;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.configuration.ServerConfig.Property;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.notify.NotificationConfig.NotificationProperty;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.i18n.tools.RenderMap;
import com.openexchange.i18n.tools.StringTemplate;
import com.openexchange.i18n.tools.Template;
import com.openexchange.i18n.tools.TemplateToken;
import com.openexchange.i18n.tools.replacement.ModuleReplacement;
import com.openexchange.i18n.tools.replacement.StringReplacement;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * {@link LinkableState} - Enhances {@link State} interface by possibility to add an object link.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class LinkableState implements State {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(LinkableState.class);

    protected static volatile Template object_link_template;

    private static final String hostname;

    private static final UnknownHostException warnSpam;

    static {
        UnknownHostException uhe = null;
        String hn;
        try {
            hn = InetAddress.getLocalHost().getCanonicalHostName();
        } catch (UnknownHostException e) {
            hn = "localhost";
            uhe = e;
        }
        hostname = hn;
        warnSpam = uhe;
    }

    @Override
    public void addSpecial(final CalendarObject obj, final CalendarObject oldObj, final RenderMap renderMap,
            final EmailableParticipant p) {
        renderMap.put(new StringReplacement(TemplateToken.LINK, generateLink(obj, p)).setChanged(true));
    }

    /**
     * Generates the object link using {@link HostnameService} if available.
     *
     * @param obj
     *            The calendar object
     * @param p
     *            The participant to notify
     * @return The object link
     */
    public String generateLink(final CalendarObject obj, final EmailableParticipant p) {
        if (object_link_template == null) {
            loadTemplate();
        }

        final RenderMap subst = new RenderMap();
        switch (getModule()) {
        case Types.APPOINTMENT:
            subst.put(new ModuleReplacement(ModuleReplacement.MODULE_CALENDAR));
            break;
        case Types.TASK:
            subst.put(new ModuleReplacement(ModuleReplacement.MODULE_TASK));
            break;
        default:
            subst.put(new ModuleReplacement(ModuleReplacement.MODULE_UNKNOWN));
            break;
        }

        int folder = p.folderId;
        if (folder == -1) {
            folder = obj.getParentFolderID();
        }

        subst.put(new StringReplacement(TemplateToken.FOLDER_ID, Integer.toString(folder)));
        subst.put(new StringReplacement(TemplateToken.OBJECT_ID, Integer.toString(obj.getObjectID())));
        subst.put(new StringReplacement(TemplateToken.UI_WEB_PATH, ServerConfig.getProperty(Property.UI_WEB_PATH)));
        final HostnameService hostnameService = ServerServiceRegistry.getInstance().getService(HostnameService.class);
        final String hostnameStr;
        if (hostnameService == null || (hostnameStr = hostnameService.getHostname(p.id, p.cid)) == null) {
            LOG.debug("No host name service available or returned host name from service is null; using local host name as fallback.");
            if (warnSpam != null) {
                LOG.error("Can't resolve my own hostname, using 'localhost' instead, which is certainly not what you want!", warnSpam);
            }
            subst.put(new StringReplacement(TemplateToken.HOSTNAME, hostname));
        } else {
            subst.put(new StringReplacement(TemplateToken.HOSTNAME, hostnameStr));
        }

        return object_link_template.render(p.getLocale(), subst);
    }

    /**
     * Loads the template
     */
    public void loadTemplate() {
        synchronized (LinkableState.class) {
            final Pattern patternSlashFixer = Pattern.compile("^//+|[^:]//+");
            final String property = patternSlashFixer.matcher(NotificationConfig.getProperty(NotificationProperty.OBJECT_LINK, "")).replaceAll("/");
            object_link_template = new StringTemplate(property);
        }
    }
}
