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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.file.storage.xox.subscription;

import static com.openexchange.file.storage.xox.XOXStorageConstants.PASSWORD;
import static com.openexchange.file.storage.xox.XOXStorageConstants.SHARE_URL;
import java.util.HashMap;
import java.util.Map;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageService;
import com.openexchange.file.storage.ServiceAware;
import com.openexchange.file.storage.xox.XOXStorageConstants;
import com.openexchange.java.Strings;

/**
 * {@link XOXFileStorageAccount}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.5
 */
public class XOXFileStorageAccount implements FileStorageAccount, ServiceAware {

    private static final long serialVersionUID = -8020641623608831630L;

    private final FileStorageService fileStorageService;
    private final String shareLink;
    private final String password;
    private final String displayName;
    private final String id;

    /**
     * Initializes a new {@link XOXFileStorageAccount}.
     * 
     * @param other Other account to clone
     */
    public XOXFileStorageAccount(FileStorageAccount other) {
        this(other.getFileStorageService(), // @formatter:off
            (String) other.getConfiguration().get(SHARE_URL),
            (String) other.getConfiguration().get(PASSWORD),
            other.getDisplayName(),
            other.getId());// @formatter:on
    }

    /**
     * Initializes a new {@link XOXFileStorageAccount}.
     * 
     * @param other Other account to clone
     * @param password The new password for that account
     */
    public XOXFileStorageAccount(FileStorageAccount other, String password) {
        this(other.getFileStorageService(), // @formatter:off
            (String) other.getConfiguration().get(SHARE_URL),
            password,
            other.getDisplayName(),
            other.getId());// @formatter:on
    }

    /**
     * Initializes a new {@link XOXFileStorageAccount}.
     * 
     * @param other Other account to clone
     * @param displayName The new display name to use
     * @param password The new password for that account
     */
    public XOXFileStorageAccount(FileStorageAccount other, String displayName, String password) {
        this(other.getFileStorageService(), // @formatter:off
            (String) other.getConfiguration().get(SHARE_URL),
            password,
            Strings.isEmpty(displayName) ? other.getDisplayName() : displayName,
            other.getId());// @formatter:on
    }

    /**
     * Initializes a new {@link XOXFileStorageAccount}.
     * 
     * @param fileStorageService The storage the account belongs to
     * @param shareLink The share link the account accesses
     * @param password The password for the share or <code>null</code>
     * @param displayName The optional display name of the account
     * @param id The ID of the account or <code>null</code>
     */
    public XOXFileStorageAccount(FileStorageService fileStorageService, String shareLink, String password, String displayName, String id) {
        super();
        this.fileStorageService = fileStorageService;
        this.shareLink = shareLink;
        this.password = Strings.isEmpty(password) ? null : password;
        this.displayName = Strings.isEmpty(displayName) ? XOXStorageConstants.DISPLAY_NAME : displayName;
        this.id = id;
    }

    @Override
    public String getServiceId() {
        return XOXStorageConstants.ID;
    }

    @Override
    public Map<String, Object> getConfiguration() {
        HashMap<String, Object> config = new HashMap<>(3, 0.9f);
        if (null != shareLink) {
            config.put(SHARE_URL, shareLink);
        }
        if (null != password) {
            config.put(PASSWORD, password);
        }
        return config;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public FileStorageService getFileStorageService() {
        return fileStorageService;
    }

    @Override
    public String toString() {
        return "XOXFileStorageAccount [fileStorageService=" + fileStorageService + ", displayName=" + displayName + ", id=" + id + ", shareLink=" + shareLink + "]";
    }

}
