
package osgi;

import org.osgi.service.http.HttpService;
import com.openexchange.http.testservlet.DiagnosticServlet;
import com.openexchange.http.testservlet.PingServlet;
import com.openexchange.http.testservlet.TestServlet;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.server.ServiceExceptionCode;

/**
 * {@link TestServletActivator} - Registers <code>TestServlet</code> and <code>PingServlet</code>.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class TestServletActivator extends HousekeepingActivator {

    /**
     * Path to diagnostic
     */
    private static final String STATS_DIAGNOSTIC = "/stats/diagnostic";

    /**
     * Path to Ping
     */
    private static final String SERVLET_PING = "/servlet/Ping";

    /**
     * Path to TestServlet
     */
    private static final String SERVLET_TEST_SERVLET = "/servlet/TestServlet";

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { HttpService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        HttpService service = getService(HttpService.class);
        if (service == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(HttpService.class.getName());
        }
        service.registerServlet(SERVLET_TEST_SERVLET, new TestServlet(), null, null);
        service.registerServlet(SERVLET_PING, new PingServlet(), null, null);
        service.registerServlet(STATS_DIAGNOSTIC, new DiagnosticServlet(), null, null);
    }

    @Override
    protected void stopBundle() throws Exception {
        HttpService service = getService(HttpService.class);
        if (service != null) {
            service.unregister(SERVLET_TEST_SERVLET);
            service.unregister(SERVLET_PING);
            service.unregister(STATS_DIAGNOSTIC);
        }
        super.stopBundle();
    }

}
