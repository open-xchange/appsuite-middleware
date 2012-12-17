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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.file.storage.dropbox;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.scribe.model.Token;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.session.WebAuthSession;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.dropbox.session.DropboxOAuthAccess;
import com.openexchange.session.Session;

/**
 * {@link AbstractDropboxAccess}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractDropboxAccess {

    protected final DropboxOAuthAccess dropboxOAuthAccess;
    protected final Session session;
    protected final FileStorageAccount account;
    protected final long dropboxUserId;
    protected final String dropboxUserName;
    protected final org.scribe.oauth.OAuthService dropboxOAuthService;
    protected final Token dropboxAccessToken;
    protected final DropboxAPI<WebAuthSession> dropboxAPI;

    /**
     * Initializes a new {@link AbstractDropboxAccess}.
     */
    protected AbstractDropboxAccess(final DropboxOAuthAccess dropboxOAuthAccess, final FileStorageAccount account, final Session session) {
        super();
        this.dropboxOAuthAccess = dropboxOAuthAccess;
        this.account = account;
        this.session = session;
        // Other fields
        this.dropboxUserId = dropboxOAuthAccess.getDropboxUserId();
        this.dropboxUserName = dropboxOAuthAccess.getDropboxUserName();
        this.dropboxOAuthService = dropboxOAuthAccess.getDropboxOAuthService();
        this.dropboxAccessToken = dropboxOAuthAccess.getDropboxAccessToken();
        this.dropboxAPI = dropboxOAuthAccess.getDropboxAPI();
    }

    public String getDropboxUserName() {
        return dropboxUserName;
    }

    /**
     * URL-encodes specified string.
     *
     * @param string The string
     * @return The URL-encoded string
     */
    protected static String encode(final String string) {
        try {
            return URLEncoder.encode(string, "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            return string;
        }
    }

}
