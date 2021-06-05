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

package com.openexchange.oauth.dropbox.v2;

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
    public static void main(String[] args) throws DbxException {
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
