

package com.openexchange.custom.parallels.soap;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import com.openexchange.control.console.AbstractJMXHandler;


public class JMXHelper extends AbstractJMXHandler {

    public JMXHelper() throws MalformedObjectNameException, NullPointerException, IOException {
        super();
        // Username and password not support at the moment
        initJMX(DEFAULT_HOST, DEFAULT_PORT, null, null);
    }

    public Bundle[] listBundles() throws InstanceNotFoundException, MBeanException, ReflectionException, IOException {
        final ObjectName objectName = getObjectName();
        final MBeanServerConnection mBeanServerConnection = getMBeanServerConnection();
        final List<Map<String, String>> bundleList = (List<Map<String, String>>) mBeanServerConnection.invoke(
            objectName,
            "list",
            new Object[] {},
            new String[] {});
        final Bundle[] retval = new Bundle[bundleList.size()];
        for (int a = 0; a < bundleList.size(); a++) {
            final Map<String, String> data = bundleList.get(a);
            final Bundle bundle = new Bundle();
            bundle.setName(data.get("bundlename"));
            bundle.setStatus(data.get("status"));
            retval[a] = bundle;
        }
        return retval;
    }

    public String getServerVersion() throws InstanceNotFoundException, MBeanException, ReflectionException, IOException {
        final ObjectName objectName = getObjectName();
        final MBeanServerConnection mBeanServerConnection = getMBeanServerConnection();
        return (String) mBeanServerConnection.invoke(
            objectName,
            "version",
            new Object[] {},
            new String[] {});
    }
}
