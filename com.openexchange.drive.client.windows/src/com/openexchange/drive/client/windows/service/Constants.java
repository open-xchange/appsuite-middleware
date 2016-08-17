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

package com.openexchange.drive.client.windows.service;


/**
 * {@link Constants} provides several constants for the windows drive updater
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.1
 */
public class Constants {

    /**
     * The updater template configuration key
     */
    public static final String TMPL_UPDATER_CONFIG = "com.openexchange.drive.updater.tmpl";
    /**
     * The updater template default value
     */
    public static final String TMPL_UPDATER_DEFAULT = "oxdrive_update.tmpl";
    /**
     * The address of the download servlet
     */
    public static final String DOWNLOAD_SERVLET = "drive/client/windows/download";
    /**
     * The address of the update servlet
     */
    public static final String UPDATE_SERVLET = "drive/client/windows/v1/update.xml";
    /**
     * The address of the install servlet
     */
    public static final String INSTALL_SERVLET = "drive/client/windows/install";

    /**
     * The configuration key of the regex expression for binary '.exe' files.
     */
    public static final String PROP_BINARY_REGEX_EXE = "com.openexchange.drive.windows.binaryRegex.exe";
    /**
     * The configuration key of the regex expression for binary '.msi' files.
     */
    public static final String PROP_BINARY_REGEX_MSI = "com.openexchange.drive.windows.binaryRegex.msi";

    public static final String AUTO_EXTRACTER = "7zS.sfx";

    /**
     * The path to the branding configurations.
     */
    public static final String BRANDINGS_PATH = "com.openexchange.drive.updater.path";
    /**
     * The branding configuration key.
     */
    public static final String BRANDING_CONF = "com.openexchange.drive.update.branding";

    /**
     * The name of the branding id field
     */
    public static final String BRANDING_ID = "id";
    /**
     * The name of the branding name field
     */
    public static final String BRANDING_NAME = "name";
    /**
     * The name of the branding version field
     */
    public static final String BRANDING_VERSION = "version";
    /**
     * The name of the branding minimum version field
     */
    public static final String BRANDING_MINIMUM_VERSION = "minimumVersion";
    /**
     * The name of the branding release date field
     */
    public static final String BRANDING_RELEASE = "release";
}
