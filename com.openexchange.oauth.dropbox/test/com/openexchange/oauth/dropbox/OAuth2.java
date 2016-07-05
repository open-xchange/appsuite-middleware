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

package com.openexchange.oauth.dropbox;

import java.io.IOException;
import java.util.Scanner;
import com.dropbox.core.DbxAppInfo;
import com.dropbox.core.DbxAuthFinish;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxWebAuth;
import com.dropbox.core.DbxWebAuth.Request;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.users.FullAccount;

/**
 * {@link OAuth2} tests the OAuth 2.0 authorisation workflow
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class OAuth2 {

    /**
     * Simple OAuth 2.0 test with V2 API
     */
    public static void main(String[] args) throws IOException, DbxException {
        if (args.length == 0) {
            System.err.println("You must specify the API key and API secret");
            System.exit(-1);
        }
        String apiKey = args[0];
        String apiSecret = args[1];

        // Setup
        DbxAppInfo appInfo = new DbxAppInfo(apiKey, apiSecret);
        DbxRequestConfig config = new DbxRequestConfig("Deucalion");

        // Request authorisation
        DbxWebAuth webAuth = new DbxWebAuth(config, appInfo);
        String authorize = webAuth.authorize(Request.newBuilder().withNoRedirect().build());
        System.out.println(authorize);

        // Wait for the authorisation code
        Scanner in = new Scanner(System.in);
        System.out.println("Enter the authorisation code:");
        String authCode = in.nextLine();
        in.close();

        // Finish authorisation
        DbxAuthFinish finish = webAuth.finishFromCode(authCode);
        String accessToken = finish.getAccessToken();
        DbxClientV2 client = new DbxClientV2(config, accessToken);

        // Perform a simple API call
        FullAccount currentAccount = client.users().getCurrentAccount();
        System.out.println(currentAccount.getEmail());
    }
}
