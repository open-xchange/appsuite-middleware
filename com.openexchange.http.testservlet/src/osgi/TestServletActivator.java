
package osgi;

import org.osgi.service.http.HttpService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.http.testservlet.TestServlet;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.server.ServiceExceptionCode;

/**
 * {@link TestServletActivator} - Registers an instance of TestServlet at /servlet/TestServlet.
 * 
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class TestServletActivator extends HousekeepingActivator {

    // The alias for registering the test servlet in the url namespace
    private final String ALIAS = "/servlet/TestServlet";

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { HttpService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        trackService(HttpService.class);
        openTrackers();

        HttpService service = getService(HttpService.class);
        if (service == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(HttpService.class.getName());
        }
        service.registerServlet(ALIAS, new TestServlet(), null, null);
    }

}
