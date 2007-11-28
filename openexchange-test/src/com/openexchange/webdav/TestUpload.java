
package com.openexchange.webdav;

import java.util.Properties;

import junit.framework.TestCase;

import com.openexchange.groupware.configuration.AbstractConfigWrapper;
import com.openexchange.test.WebdavInit;

public class TestUpload extends TestCase {

    private Properties webdavProps;

    private String login;

    private String password;

    private String hostname;
    
    /**
     * Default constructor.
     * @param arg0
     */
    public TestUpload(final String arg0) {
        super(arg0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        webdavProps = WebdavInit.getWebdavProperties();
        login = AbstractConfigWrapper.parseProperty(webdavProps, "login", "");
        password = AbstractConfigWrapper.parseProperty(webdavProps, "password", "");
        hostname = AbstractConfigWrapper.parseProperty(webdavProps, "hostname", "localhost");
    }

    private static final int PAKETS = 10;
    
    public void testUpload() throws Throwable {
        /*
        final HttpClient client = new HttpClient();
        // TODO read home infostore dir
        final String uri = "http://" + hostname + "/servlet/webdav.infostore/Martin%20Braun/testfile";
        final PutMethod put = new PutMethod(uri);

        final HttpMethodParams params = new HttpMethodParams(HttpMethodParams
            .getDefaultParams());
        params.setVersion(HttpVersion.HTTP_1_1);
        put.setParams(params);

        final RequestEntity entity = new RequestEntity() {
            public long getContentLength() {
                return -1;
            }
            public String getContentType() {
                return "application/octet-stream";
            }
            public boolean isRepeatable() {
                return true;
            }
            public void writeRequest(final OutputStream out)
                throws IOException {
                final byte[] bytes = new byte[1024];
                for (int i = 0; i < bytes.length; i++) {
                    bytes[i] = 0;
                }
                for (int i = 0; i < PAKETS; i++) {
                    out.write(bytes);
                }
            }
        };
        put.setRequestEntity(entity);

        final Credentials creds = new UsernamePasswordCredentials(login, password);
        client.getState().setCredentials(AuthScope.ANY, creds);

        client.executeMethod(put);
        assertEquals(201, put.getStatusCode());
        */
    }
    
}
