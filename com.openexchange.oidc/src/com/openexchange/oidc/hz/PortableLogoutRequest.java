package com.openexchange.oidc.hz;

import java.io.IOException;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.openexchange.hazelcast.serialization.AbstractCustomPortable;
import com.openexchange.hazelcast.serialization.CustomPortable;
import com.openexchange.oidc.state.DefaultLogoutRequestInfo;
import com.openexchange.oidc.state.LogoutRequestInfo;

public class PortableLogoutRequest extends AbstractCustomPortable {

    private static final String STATE = "state";
    private static final String DOMAINNAME = "domainname";
    private static final String IDTOKEN = "idtoken";
    
    private LogoutRequestInfo delegate;
    
    
    public PortableLogoutRequest() {
        super();
    }
    
    public PortableLogoutRequest(LogoutRequestInfo delegate) {
        super();
        this.delegate = delegate;
    }
    
    public LogoutRequestInfo getDelegate() {
        return delegate;
    }
    
    
    public void setDelegate(LogoutRequestInfo delegate) {
        this.delegate = delegate;
    }
    
    @Override
    public int getClassId() {
        return CustomPortable.PORTABLE_OIDC_LOGOUT_REQUEST_INFO;
    }
    
    @Override
    public void writePortable(PortableWriter writer) throws IOException {
        writer.writeUTF(STATE, delegate.getState());
        writer.writeUTF(DOMAINNAME, delegate.getDomainName());
        writer.writeUTF(IDTOKEN, delegate.getIDToken());
    }
    
    @Override
    public void readPortable(PortableReader reader) throws IOException {
        DefaultLogoutRequestInfo logoutRequestInfo = new DefaultLogoutRequestInfo(reader.readUTF(STATE), reader.readUTF(DOMAINNAME), reader.readUTF(IDTOKEN));
        this.setDelegate(logoutRequestInfo);
    }
}
