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
        
        Map<String, String> result = new HashMap<>();
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
