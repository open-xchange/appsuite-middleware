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

package com.openexchange.drive.events.internal;

import com.openexchange.drive.restricted.loader.StringsProvider;

/**
 * {@link DriveStringsProvider} provides the necessary property and file names for loading Drive GCM secrets and APN certificates.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 * @since v7.10.2
 */
public class DriveStringsProvider implements StringsProvider {

    public DriveStringsProvider() {
        super();
    }

    @Override
    public String getPropertiesFilename() {
        return "drive.properties";
    }

    @Override
    public String getGCMSecretPropertyName() {
        return "com.openexchange.drive.events.gcm.key";
    }

    @Override
    public String getGCMSecretEnabledPropertyName() {
        return "com.openexchange.drive.events.gcm.enabled";
    }

    @Override
    public String getAPNiOSEnabledPropertyName() {
        return "com.openexchange.drive.events.apn.ios.enabled";
    }

    @Override
    public String getAPNiOSKeystorePropertyName() {
        return "com.openexchange.drive.events.apn.ios.keystore";
    }

    @Override
    public String getAPNiOSPasswordPropertyName() {
        return "com.openexchange.drive.events.apn.ios.password";
    }

    @Override
    public String getAPNiOSProductionPropertyName() {
        return "com.openexchange.drive.events.apn.ios.production";
    }

    @Override
    public String getAPNmacOSEnabledPropertyName() {
        return "com.openexchange.drive.events.apn.macos.enabled";
    }

    @Override
    public String getAPNmacOSKeystorePropertyName() {
        return "com.openexchange.drive.events.apn.macos.keystore";
    }

    @Override
    public String getAPNmacOSPasswordPropertyName() {
        return "com.openexchange.drive.events.apn.macos.password";
    }

    @Override
    public String getAPNmacOSProductionPropertyName() {
        return "com.openexchange.drive.events.apn.macos.production";
    }
}
