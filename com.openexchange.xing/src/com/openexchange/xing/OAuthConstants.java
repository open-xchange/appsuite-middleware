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

/*
Copyright 2010 Pablo Fernandez

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package com.openexchange.xing;

/**
 * This class contains OAuth constants, used project-wide
 *
 * @author Pablo Fernandez
 */
public class OAuthConstants
{
  public static final String TIMESTAMP = "oauth_timestamp";
  public static final String SIGN_METHOD = "oauth_signature_method";
  public static final String SIGNATURE = "oauth_signature";
  public static final String CONSUMER_SECRET = "oauth_consumer_secret";
  public static final String CONSUMER_KEY = "oauth_consumer_key";
  public static final String CALLBACK = "oauth_callback";
  public static final String VERSION = "oauth_version";
  public static final String NONCE = "oauth_nonce";
  public static final String PARAM_PREFIX = "oauth_";
  public static final String TOKEN = "oauth_token";
  public static final String TOKEN_SECRET = "oauth_token_secret";
  public static final String OUT_OF_BAND = "oob";
  public static final String VERIFIER = "oauth_verifier";
  public static final String HEADER = "Authorization";
  public static final String SCOPE = "scope";

  //OAuth 2.0
  public static final String ACCESS_TOKEN = "access_token";
  public static final String CLIENT_ID = "client_id";
  public static final String CLIENT_SECRET = "client_secret";
  public static final String REDIRECT_URI = "redirect_uri";
  public static final String CODE = "code";

}
