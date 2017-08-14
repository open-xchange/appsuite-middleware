
package com.openexchange.oidc.hz;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.openexchange.hazelcast.serialization.AbstractCustomPortable;
import com.openexchange.hazelcast.serialization.CustomPortable;
import com.openexchange.oidc.state.AuthenticationRequestInfo;
import com.openexchange.oidc.state.DefaultAuthenticationRequestInfo;
import com.openexchange.oidc.state.DefaultAuthenticationRequestInfo.Builder;

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
        String[] array = new String[map.size()];
        int counter = 0;
        for (Entry<String, String> clientInformation : map.entrySet()) {
            array[counter] = clientInformation.getKey() + EQUAL_SIGN + clientInformation.getValue();
            counter++;
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
        Map<String, String> result = new HashMap<>();
        for (String uiInformation : readUTFArray) {
            String key = uiInformation.substring(0, uiInformation.indexOf(EQUAL_SIGN));
            String value = uiInformation.replace(key+EQUAL_SIGN, "");
            result.put(key, value);
        }
        return result;
    }

}
