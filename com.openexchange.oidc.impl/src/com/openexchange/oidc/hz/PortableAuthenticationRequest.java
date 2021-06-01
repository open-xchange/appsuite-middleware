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

package com.openexchange.oidc.hz;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.openexchange.hazelcast.serialization.AbstractCustomPortable;
import com.openexchange.hazelcast.serialization.CustomPortable;
import com.openexchange.oidc.state.AuthenticationRequestInfo;
import com.openexchange.oidc.state.impl.DefaultAuthenticationRequestInfo;
import com.openexchange.oidc.state.impl.DefaultAuthenticationRequestInfo.Builder;

/**
 * {@link PortableAuthenticationRequest} Contains all needed information to load an
 * {@link AuthenticationRequestInfo} identified by its state.
 *
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 * @since v7.10.0
 */
public class PortableAuthenticationRequest extends AbstractCustomPortable {

    private static final String STATE = "state";
    private static final String DOMAINNAME = "domainname";
    private static final String DEEPLINK = "deeplink";
    private static final String NONCE = "nonce";
    private static final String UI_CLIENT_INFORMATION = "ui_client_information";
    private static final String UI_CLIENT_ID = "ui_client_id";
    private static final String EQUAL_SIGN = "&=&";

    private AuthenticationRequestInfo delegate;

    public PortableAuthenticationRequest() {
        super();
    }

    public PortableAuthenticationRequest(AuthenticationRequestInfo delegate) {
        super();
        this.delegate = delegate;
    }

    public void setDelegate(AuthenticationRequestInfo delegate) {
        this.delegate = delegate;
    }

    public AuthenticationRequestInfo getDelegate() {
        return this.delegate;
    }

    @Override
    public int getClassId() {
        return CustomPortable.PORTABLE_OIDC_AUTHN_REQUEST_INFO;
    }

    @Override
    public void writePortable(PortableWriter writer) throws IOException {
        writer.writeUTF(STATE, delegate.getState());
        writer.writeUTF(DOMAINNAME, delegate.getDomainName());
        writer.writeUTF(DEEPLINK, delegate.getDeepLink());
        writer.writeUTF(NONCE, delegate.getNonce());
        writer.writeUTF(UI_CLIENT_ID, delegate.getUiClientID());
        writer.writeUTFArray(UI_CLIENT_INFORMATION, getArrayFromMap(delegate.getAdditionalClientInformation()));
    }

    private String[] getArrayFromMap(Map<String, String> map) {
        if (null == map) {
            return new String[0];
        }

        String[] array = new String[map.size()];
        int counter = 0;
        for (Map.Entry<String, String> clientInformation : map.entrySet()) {
            array[counter++] = clientInformation.getKey() + EQUAL_SIGN + clientInformation.getValue();
        }
        return array;
    }

    @Override
    public void readPortable(PortableReader reader) throws IOException {
        DefaultAuthenticationRequestInfo authenticationInfo = new Builder(reader.readUTF(STATE))
        .domainName(reader.readUTF(DOMAINNAME))
        .deepLink(reader.readUTF(DEEPLINK))
        .nonce(reader.readUTF(NONCE))
        .additionalClientInformation(this.getMapFromArray(reader.readUTFArray(UI_CLIENT_INFORMATION)))
        .uiClientID(reader.readUTF(UI_CLIENT_ID))
        .build();
        this.setDelegate(authenticationInfo);
    }

    private Map<String, String> getMapFromArray(String[] readUTFArray) {
        if (null == readUTFArray) {
            return new HashMap<>(1);
        }

        Map<String, String> result = new HashMap<>(readUTFArray.length);
        for (String uiInformation : readUTFArray) {
            int pos = uiInformation.indexOf(EQUAL_SIGN);
            if (pos > 0) {
                String key = uiInformation.substring(0, pos);
                String value = uiInformation.substring(pos + EQUAL_SIGN.length());
                result.put(key, value);
            }
        }
        return result;
    }

}
