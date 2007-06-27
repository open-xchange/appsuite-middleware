
package com.openexchange.admin.adminConsole;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

public class StatisticTools {

    private String JMX_HOST = "localhost";

    private static final String JMX_SERVER_PORT = "9999";

    private static final String JMX_ADMIN_PORT = "9998";

    private boolean showOXStats = false;

    private boolean showRuntimeStats = false;

    private boolean showOperatingSystemStats = false;

    private boolean showAllStats = false;

    private boolean showAdminStats = false;

    private boolean showHelp = false;

    private boolean showThreadStats = false;

    private final boolean args_ok = true;

    private String ox_jmx_url = null;

    MBeanServerConnection mbc = null;

    JMXConnector c = null;

    /**
     * This method is called after a hostname change and input parsing, because
     * the url depends on both steps
     */
    private void updatejmxurl() {
        final String jmxPort = showAdminStats ? JMX_ADMIN_PORT : JMX_SERVER_PORT;
        this.ox_jmx_url = "service:jmx:rmi:///jndi/rmi://" + JMX_HOST + ':' + jmxPort + "/server";
    }

    public static void main(final String args[]) {
        final StatisticTools st = new StatisticTools();
        st.start(args);
    }

    public void start(final String args[]) {
        parseInput(args);
        updatejmxurl();
        if (showHelp) {
            showUsage();
        } else if (args_ok) {
            try {
                initConnection();
                fetchData();
            } catch (final java.io.IOException sve) {
                p2c("Can't connect to \"" + ox_jmx_url + "\"");
                System.exit(1);
            } catch (final Exception ex) {
                ex.printStackTrace();
                System.exit(1);
            }

            closeConnection();
        } else {
            showUsage();
        }
    }

    public void p2c(final Object obj) {
        System.out.println("" + obj);
    }

    private void initConnection() throws InterruptedException, IOException {
        // Set timeout here, it is given in ms
        final long timeout = 1000;
        final JMXServiceURL serviceurl = new JMXServiceURL(ox_jmx_url);
        // If server needs auth
        // Hashtable h = new Hashtable();
        // String[] credentials = new String[] {"username" ,"password" };
        // h.put("jmx.remote.credentials", credentials);
        // c = JMXConnectorFactory.connect(u,h);
        final IOException[] exc = new IOException[1];
        final Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    c = JMXConnectorFactory.connect(serviceurl);
                } catch (IOException e) {
                    exc[0] = e;
                }
            }
        };
        t.start();
        t.join(timeout);
        if (t.isAlive()) {
            t.interrupt();
            throw new InterruptedIOException("Connection timed out");
        }
        if (exc[0] != null) {
            throw exc[0];
        }
        mbc = c.getMBeanServerConnection();
    }

    private void closeConnection() {

        if (c != null) {
            try {
                c.close();
            } catch (final IOException ex) {
                ex.printStackTrace();
            }
        }

    }

    private void parseInput(final String args[]) {
        String param = null;

        for (int a = 0; a < args.length; a++) {
            param = args[a];
            try {
                for (int i = 1; i < args.length; i++) {
                    if (!args[a + i].startsWith("-")) {
                        param += " " + args[a + i];
                    } else {
                        break;
                    }
                }
            } catch (final Exception e) {
                // nothing
            }
            if (param.startsWith("-host=")) {
                final StringTokenizer st = new StringTokenizer(param, "=");
                if (st.countTokens() == 2) {
                    st.nextToken();
                    JMX_HOST = st.nextToken().toLowerCase();
                    updatejmxurl();
                }
            }
            if (param.equals("-x")) {
                showOXStats = true;
            }
            if (param.equals("-r")) {
                showRuntimeStats = true;
            }
            if (param.equals("-o")) {
                showOperatingSystemStats = true;
            }
            if (param.equals("-a")) {
                showAllStats = true;
            }
            if (param.equals("-t")) {
                showThreadStats = true;
            }
            if (param.equals("-A")) {
                showAdminStats = true;
            }
            /*
             * We allow both GNU Style (--help) and POSIX (-h)
             */
            if (param.equals("--help") || param.equals("-h")) {
                showHelp = true;
            }
        }
    }

    private void showUsage() {
        final StringBuffer sb = new StringBuffer();
        sb.append("\n");
        sb.append("Options: \n");
        sb.append("\t -host=localhost\n");
        sb.append("\t -x Shows Open-Xchange Stats\n");
        sb.append("\t -r Shows Java Runtime Stats\n");
        sb.append("\t -o Shows Operating System Stats\n");
        sb.append("\t -t Shows Threading Stats\n");
        sb.append("\t -a Shows All Stats\n");
        sb.append("\t -A Shows stats of Admindaemon instead of OX Server");

        p2c(sb.toString());
    }

    private void showStats(final Set data_set, final String class_name) throws IOException, InstanceNotFoundException, MBeanException, AttributeNotFoundException, ReflectionException, IntrospectionException {
        final Iterator itr = data_set.iterator();
        while (itr.hasNext()) {
            final ObjectInstance oin = (ObjectInstance) itr.next();

            final ObjectName obj = oin.getObjectName();
            final MBeanInfo info = mbc.getMBeanInfo(obj);
            // p2c("# "+obj.getCanonicalName() +" ->"+info.getClassName());
            if (info.getClassName().equals(class_name)) {
                // p2c("# "+obj.getCanonicalName());
                final String ocname = obj.getCanonicalName();
                final MBeanAttributeInfo[] attrs = info.getAttributes();
                if (attrs.length > 0) {
                    for (final MBeanAttributeInfo element : attrs) {
                        try {
                            final Object o = mbc.getAttribute(obj, element.getName());
                            if (o != null) {
                                if (o instanceof CompositeDataSupport) {
                                    final CompositeDataSupport c = (CompositeDataSupport) o;
                                    p2c(ocname + "," + element.getName() + " = [init=" + c.get("init") + ",max=" + c.get("max") + ",committed=" + c.get("committed") + ",used=" + c.get("used") + "]");
                                } else {
                                    if (o instanceof String[]) {
                                        final String[] c = (String[]) o;
                                        p2c(ocname + "," + element.getName() + " = " + Arrays.toString(c));
                                    } else if (o instanceof long[]) {
                                        final long[] l = (long[]) o;
                                        p2c(ocname + "," + element.getName() + " = " + Arrays.toString(l));
                                    } else {
                                        p2c(ocname + "," + "" + element.getName() + " = " + o.toString());
                                    }
                                }
                            }
                        } catch (final javax.management.RuntimeMBeanException rexp) {

                        }
                    }
                }
            }
        }
    }

    private void showMemoryPoolData(final Set data_set) throws InstanceNotFoundException, AttributeNotFoundException, IntrospectionException, MBeanException, ReflectionException, IOException {
        showStats(data_set, "sun.management.MemoryPoolImpl");
    }

    private void showSysThreadingData(final Set data_set) throws InstanceNotFoundException, AttributeNotFoundException, IntrospectionException, MBeanException, ReflectionException, IOException {
        showStats(data_set, "sun.management.ThreadImpl");
    }

    private void showOXData(final Set data_set) throws InstanceNotFoundException, AttributeNotFoundException, IntrospectionException, MBeanException, ReflectionException, IOException {
        showStats(data_set, "com.openexchange.tools.ajp13.monitoring.AJPv13ServerThreadsMonitor");
        showStats(data_set, "com.openexchange.tools.ajp13.monitoring.AJPv13ListenerMonitor");
        showStats(data_set, "com.openexchange.monitoring.GeneralMonitor");
        showStats(data_set, "com.openexchange.api2.MailInterfaceMonitor");
        showStats(data_set, "com.openexchange.database.ConnectionPool");
    }

    private void showAdminData(final Set data_set) throws InstanceNotFoundException, AttributeNotFoundException, IntrospectionException, MBeanException, ReflectionException, IOException {
        showStats(data_set, "com.openexchange.admin.tools.monitoring.Monitor");
    }

    private void fetchData() throws IOException, InstanceNotFoundException, AttributeNotFoundException, IntrospectionException, MBeanException, ReflectionException {

        final Set data_set = mbc.queryMBeans(null, null);
        if (showOXStats) {
            if (showAdminStats) {
                showAdminData(data_set);
            } else {
                showOXData(data_set);
            }
        } else if (showOperatingSystemStats) {
            showStats(data_set, "com.sun.management.UnixOperatingSystem");
        } else if (showRuntimeStats) {
            showStats(data_set, "sun.management.RuntimeImpl");
            showMemoryPoolData(data_set);
        } else if (showAllStats) {
            showOXData(data_set);
            showStats(data_set, "com.sun.management.UnixOperatingSystem");
            showStats(data_set, "sun.management.RuntimeImpl");
            showMemoryPoolData(data_set);
            showSysThreadingData(data_set);
        } else if (showThreadStats) {
            showSysThreadingData(data_set);
        } else {
            showOXData(data_set);
        }
    }

}
