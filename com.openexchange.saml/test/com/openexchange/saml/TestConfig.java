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

package com.openexchange.saml;

import java.util.Set;
import com.google.common.collect.ImmutableSet;

public class TestConfig implements SAMLConfig {


    TestConfig() {
        super();
    }

    @Override
    public String getProviderName() {
        return "OX App Suite";
    }

    @Override
    public String getEntityID() {
        return "http://webmail.example.com";
    }

    @Override
    public String getAssertionConsumerServiceURL() {
        return "https://webmail.example.com/appsuite/api/saml/acs";
    }

    @Override
    public String getSingleLogoutServiceURL() {
        return "https://webmail.example.com/appsuite/api/saml/sls";
    }

    @Override
    public String getIdentityProviderEntityID() {
        return "http://idp.example.com";
    }

    @Override
    public Binding getLogoutResponseBinding() {
        return Binding.HTTP_REDIRECT;
    }

    @Override
    public String getIdentityProviderAuthnURL() {
        return "https://idp.example.com/sso/login";
    }

    @Override
    public boolean singleLogoutEnabled() {
        return true;
    }

    @Override
    public String getIdentityProviderLogoutURL() {
        return "https://idp.example.com/sso/logout";
    }

    @Override
    public boolean enableMetadataService() {
        return true;
    }

    @Override
    public String getLogoutResponseTemplate() {
        return null;
    }

    @Override
    public boolean isAutoLoginEnabled() {
        return true;
    }

    @Override
    public boolean isAllowUnsolicitedResponses() {
        return true;
    }

    @Override
    public boolean isSessionIndexAutoLoginEnabled() {
        return true;
    }

    @Override
    public Set<String> getHosts() {
        return ImmutableSet.of("all");
    }
}