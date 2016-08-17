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

package com.openexchange.mail.autoconfig.sources;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.mail.autoconfig.Autoconfig;
import com.openexchange.mail.autoconfig.xmlparser.AutoconfigParser;
import com.openexchange.mail.autoconfig.xmlparser.ClientConfig;
import com.openexchange.server.ServiceLookup;

/**
 * {@link ConfigurationFile}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class ConfigurationFile extends AbstractConfigSource {

    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ConfigurationFile.class);

    private static final String locationProperty = "com.openexchange.mail.autoconfig.path";

    private final ServiceLookup services;

    public ConfigurationFile(ServiceLookup services) {
        this.services = services;
    }

    @Override
    public Autoconfig getAutoconfig(String emailLocalPart, final String emailDomain, String password, User user, Context context) throws OXException {
        return getAutoconfig(emailLocalPart, emailDomain, password, user, context, true);
    }

    @Override
    public Autoconfig getAutoconfig(String emailLocalPart, final String emailDomain, String password, User user, Context context, boolean forceSecure) throws OXException {
        ConfigViewFactory configViewFactory = services.getService(ConfigViewFactory.class);
        ConfigView view = configViewFactory.getView(user.getId(), context.getContextId());
        String fileLocation = view.get(locationProperty, String.class);
        if (fileLocation == null) {
            return null;
        }
        File configFolder = new File(fileLocation);

        File[] files = configFolder.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return name.equalsIgnoreCase(emailDomain + ".xml");
            }
        });

        if (files == null || files.length == 0) {
            return null;
        }

        FileInputStream fis;
        try {
            fis = new FileInputStream(files[0]);
        } catch (FileNotFoundException e) {
            LOG.warn("Unable to find file: {}", files[0], e);
            return null;
        }
        ClientConfig clientConfig = new AutoconfigParser().getConfig(fis);

        Autoconfig autoconfig = getBestConfiguration(clientConfig, emailDomain);
        replaceUsername(autoconfig, emailLocalPart, emailDomain);
        autoconfig.setMailStartTls(forceSecure);
        autoconfig.setTransportStartTls(forceSecure);
        return autoconfig;
    }

}
