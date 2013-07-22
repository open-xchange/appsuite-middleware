
package osgi;

import org.osgi.service.http.HttpService;
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
        service.registerServlet("/servlet/TestServlet", new TestServlet(), null, null);
        service.registerServlet("/servlet/Ping", new PingServlet(), null, null);
    }

}
