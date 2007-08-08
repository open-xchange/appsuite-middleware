package com.openexchange.admin.console;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.RuntimeMBeanException;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import com.openexchange.admin.console.AdminParser.NeededQuadState;
import com.openexchange.admin.console.CmdLineParser.IllegalOptionValueException;
import com.openexchange.admin.console.CmdLineParser.Option;
import com.openexchange.admin.console.CmdLineParser.UnknownOptionException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.MissingOptionException;

public class StatisticTools extends BasicCommandlineOptions {

    private static final String JMX_SERVER_PORT = "9999";

    private static final String JMX_ADMIN_PORT = "9998";

    private static final char OPT_HOST_SHORT = 'H';

    private static final String OPT_HOST_LONG = "host";

    private static final char OPT_STATS_SHORT = 'x';

    private static final String OPT_STATS_LONG = "xchangestats";

    private static final char OPT_RUNTIME_STATS_SHORT = 'r';

    private static final String OPT_RUNTIME_STATS_LONG = "runtimestats";

    private static final char OPT_OS_STATS_SHORT = 'o';

    private static final String OPT_OS_STATS_LONG = "osstats";

    private static final char OPT_THREADING_STATS_SHORT = 't';

    private static final String OPT_THREADING_STATS_LONG = "threadingstats";

    private static final char OPT_ALL_STATS_SHORT = 'a';

    private static final String OPT_ALL_STATS_LONG = "allstats";

    private static final char OPT_ADMINDAEMON_STATS_SHORT = 'A';

    private static final String OPT_ADMINDAEMON_STATS_LONG = "admindaemonstats";

    private static final char OPT_SHOWOPERATIONS_STATS_SHORT = 's';

    private static final String OPT_SHOWOPERATIONS_STATS_LONG = "showoperations";

    private static final char OPT_DOOPERATIONS_STATS_SHORT = 'd';

    private static final String OPT_DOOPERATIONS_STATS_LONG = "dooperation";

    private String JMX_HOST = "localhost";

    private String ox_jmx_url = null;

    JMXConnector c = null;

    private Option host = null;

    private Option xchangestats = null;

    private Option runtimestats = null;

    private Option osstats = null;

    private Option threadingstats = null;

    private Option allstats = null;

    private Option admindaemonstats;

    private Option showoperation;

    private Option dooperation;

    /**
     * This method is called after a hostname change and input parsing, because
     * the url depends on both steps
     */
    private void updatejmxurl(final boolean showAdminStats) {
        final String jmxPort = showAdminStats ? JMX_ADMIN_PORT : JMX_SERVER_PORT;
        this.ox_jmx_url = new StringBuilder("service:jmx:rmi:///jndi/rmi://").append(JMX_HOST).append(':').append(jmxPort).append("/server").toString();
    }

    public static void main(final String args[]) {
        final StatisticTools st = new StatisticTools();
        st.start(args);
    }

    public void start(final String args[]) {
        final AdminParser parser = new AdminParser("statistictools");

        setOptions(parser);

        try {
            parser.ownparse(args);

            boolean admin = false;
            final String host = (String) parser.getOptionValue(this.host);
            if (null != host) {
                JMX_HOST = host;
            }
            if (null != parser.getOptionValue(this.admindaemonstats)) {
                admin = true;
            }
            int count = 0;
            if (null != parser.getOptionValue(this.xchangestats)) {
                final MBeanServerConnection initConnection = initConnection(admin);
                showOXData(initConnection, admin);
                count++;
            }
            if (null != parser.getOptionValue(this.runtimestats)) {
                if (0 == count) {
                    final MBeanServerConnection initConnection = initConnection(admin);
                    showStats(initConnection, "sun.management.RuntimeImpl");
                    showMemoryPoolData(initConnection);
                }
                count++;
            }
            if (null != parser.getOptionValue(this.osstats)) {
                if (0 == count) {
                    final MBeanServerConnection initConnection = initConnection(admin);
                    showStats(initConnection, "com.sun.management.UnixOperatingSystem");
                }
                count++;

            }
            if (null != parser.getOptionValue(this.threadingstats)) {
                if (0 == count) {
                    final MBeanServerConnection initConnection = initConnection(admin);
                    showSysThreadingData(initConnection);
                }
                count++;

            }
            if (null != parser.getOptionValue(this.allstats)) {
                if (0 == count) {
                    final MBeanServerConnection initConnection = initConnection(admin);
                    showOXData(initConnection, admin);
                    showStats(initConnection, "com.sun.management.UnixOperatingSystem");
                    showStats(initConnection, "sun.management.RuntimeImpl");
                    showMemoryPoolData(initConnection);
                    showSysThreadingData(initConnection);
                }
                count++;

            }
            if (null != parser.getOptionValue(this.showoperation)) {
                if (0 == count) {
                    final MBeanServerConnection initConnection = initConnection(admin);
                    showOperations(initConnection);
                }
                count++;

            }
            final String operation = (String) parser.getOptionValue(this.dooperation);
            if (null != operation) {
                if (0 == count) {
                    final MBeanServerConnection initConnection = initConnection(admin);
                    final Object result = doOperation(initConnection, operation);
                    if (null != result) {
                        System.out.println(result);
                    }
                }
                count++;
                System.out.println("Done");
            }
            if (0 == count) {
                System.err.println(new StringBuilder("No option selected (").append(OPT_STATS_LONG).append(", ")
                        .append(OPT_RUNTIME_STATS_LONG).append(", ").append(OPT_OS_STATS_LONG).append(", ")
                        .append(OPT_THREADING_STATS_LONG).append(", ").append(OPT_ALL_STATS_LONG).append(")"));
                parser.printUsage();
            } else if (count > 1) {
                System.err.println("More than one of the stat options given. Using the first one only");
            }
        } catch (final IllegalOptionValueException e) {
            printError("Illegal option value : " + e.getMessage(), parser);
            parser.printUsage();
            sysexit(SYSEXIT_ILLEGAL_OPTION_VALUE);
        } catch (final UnknownOptionException e) {
            printError("Unrecognized options on the command line: " + e.getMessage(), parser);
            parser.printUsage();
            sysexit(SYSEXIT_UNKNOWN_OPTION);
        } catch (final MissingOptionException e) {
            printError(e.getMessage(), parser);
            parser.printUsage();
            sysexit(SYSEXIT_MISSING_OPTION);
        } catch (final IOException e) {
            printServerException(e, parser);
            sysexit(1);
        } catch (final InstanceNotFoundException e) {
            printServerException(e, parser);
            sysexit(1);
        } catch (final AttributeNotFoundException e) {
            printServerException(e, parser);
            sysexit(1);
        } catch (final IntrospectionException e) {
            printServerException(e, parser);
            sysexit(1);
        } catch (final MBeanException e) {
            printServerException(e, parser);
            sysexit(1);
        } catch (final ReflectionException e) {
            printServerException(e, parser);
            sysexit(1);
        } catch (final InterruptedException e) {
            printServerException(e, parser);
            sysexit(1);
        } catch (final MalformedObjectNameException e) {
            printServerException(e, parser);
            sysexit(1);
        } catch (final NullPointerException e) {
            printServerException(e, parser);
            sysexit(1);
        } catch (final InvalidDataException e) {
            printServerException(e, parser);
            sysexit(1);
        } finally {
            closeConnection();
        }
    }

    private void setOptions(AdminParser parser) {
        this.host = setShortLongOpt(parser, OPT_HOST_SHORT, OPT_HOST_LONG, "host", "specifies the host", false);
        this.xchangestats = setShortLongOpt(parser, OPT_STATS_SHORT, OPT_STATS_LONG, "shows Open-Xchange stats", false, NeededQuadState.notneeded);
        this.runtimestats = setShortLongOpt(parser, OPT_RUNTIME_STATS_SHORT, OPT_RUNTIME_STATS_LONG, "shows Java runtime stats", false, NeededQuadState.notneeded);
        this.osstats = setShortLongOpt(parser, OPT_OS_STATS_SHORT, OPT_OS_STATS_LONG, "shows operating system stats", false, NeededQuadState.notneeded);
        this.threadingstats = setShortLongOpt(parser, OPT_THREADING_STATS_SHORT, OPT_THREADING_STATS_LONG, "shows threading stats", false, NeededQuadState.notneeded);
        this.allstats = setShortLongOpt(parser, OPT_ALL_STATS_SHORT, OPT_ALL_STATS_LONG, "shows all stats", false, NeededQuadState.notneeded);
        this.admindaemonstats = setShortLongOpt(parser, OPT_ADMINDAEMON_STATS_SHORT, OPT_ADMINDAEMON_STATS_LONG, "shows stats for the admin instead of the groupware", false, NeededQuadState.notneeded);
        this.showoperation = setShortLongOpt(parser, OPT_SHOWOPERATIONS_STATS_SHORT, OPT_SHOWOPERATIONS_STATS_LONG, "shows the operations for the registered beans", false, NeededQuadState.notneeded);
        this.dooperation = setShortLongOpt(parser, OPT_DOOPERATIONS_STATS_SHORT, OPT_DOOPERATIONS_STATS_LONG, "operation", "Syntax is <canonical object name (the first part from showoperatons)>!<operationname>", false);
    }

    private MBeanServerConnection initConnection(final boolean adminstats) throws InterruptedException, IOException {
        updatejmxurl(adminstats);
        // Set timeout here, it is given in ms
        final long timeout = 2000;
        final JMXServiceURL serviceurl = new JMXServiceURL(ox_jmx_url);
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
        return c.getMBeanServerConnection();
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

    private void showStats(final MBeanServerConnection mbc, final String class_name) throws IOException, InstanceNotFoundException, MBeanException, AttributeNotFoundException, ReflectionException, IntrospectionException {
        final Iterator<ObjectInstance> itr = mbc.queryMBeans(null, null).iterator();
        while (itr.hasNext()) {
            final ObjectInstance oin = (ObjectInstance) itr.next();

            final ObjectName obj = oin.getObjectName();
            final MBeanInfo info = mbc.getMBeanInfo(obj);
            if (info.getClassName().equals(class_name)) {
                final String ocname = obj.getCanonicalName();
                final MBeanAttributeInfo[] attrs = info.getAttributes();
                if (attrs.length > 0) {
                    for (final MBeanAttributeInfo element : attrs) {
                        try {
                            final Object o = mbc.getAttribute(obj, element.getName());
                            if (o != null) {
                                final StringBuilder sb = new StringBuilder(ocname).append(",").append(element.getName()).append(" = ");
                                if (o instanceof CompositeDataSupport) {
                                    final CompositeDataSupport c = (CompositeDataSupport) o;
                                    sb.append("[init=").append(c.get("init")).append(",max=").append(c.get("max")).append(",committed=").append(c.get("committed")).append(",used=").append(c.get("used")).append("]");
                                    System.out.println(sb.toString());
                                } else {
                                    if (o instanceof String[]) {
                                        final String[] c = (String[]) o;
                                        System.out.println(sb.append(Arrays.toString(c)).toString());
                                    } else if (o instanceof long[]) {
                                        final long[] l = (long[]) o;
                                        System.out.println(sb.append(Arrays.toString(l)).toString());
                                    } else {
                                        System.out.println(sb.append(o.toString()).toString());
                                    }
                                }
                            }
                        } catch (final RuntimeMBeanException e) {
                            // If there was an error getting the attribute we just omit that attribute
                        }                        
                    }
                }
            }
        }
    }

    private void showMemoryPoolData(final MBeanServerConnection mbc) throws InstanceNotFoundException, AttributeNotFoundException, IntrospectionException, MBeanException, ReflectionException, IOException {
        showStats(mbc, "sun.management.MemoryPoolImpl");
    }

    private void showSysThreadingData(final MBeanServerConnection mbc) throws InstanceNotFoundException, AttributeNotFoundException, IntrospectionException, MBeanException, ReflectionException, IOException {
        showStats(mbc, "sun.management.ThreadImpl");
    }

    private void showOXData(final MBeanServerConnection mbc, final boolean admin) throws InstanceNotFoundException, AttributeNotFoundException, IntrospectionException, MBeanException, ReflectionException, IOException {
        if (admin) {
            showStats(mbc, "com.openexchange.admin.tools.monitoring.Monitor");
        } else {
            showStats(mbc, "com.openexchange.tools.ajp13.monitoring.AJPv13ServerThreadsMonitor");
            showStats(mbc, "com.openexchange.tools.ajp13.monitoring.AJPv13ListenerMonitor");
            showStats(mbc, "com.openexchange.monitoring.GeneralMonitor");
            showStats(mbc, "com.openexchange.api2.MailInterfaceMonitor");
            showStats(mbc, "com.openexchange.database.ConnectionPool");
        }
    }
    
    private void showOperations(final MBeanServerConnection mbc) throws IOException, InstanceNotFoundException, IntrospectionException, ReflectionException {
        final Set<ObjectName> queryNames = mbc.queryNames(null, null);
        for (final ObjectName objname : queryNames) {
            final MBeanInfo beanInfo = mbc.getMBeanInfo(objname);
            final MBeanOperationInfo[] operations = beanInfo.getOperations();
            for (final MBeanOperationInfo operation : operations) {
                System.out.println(new StringBuilder(objname.getCanonicalName()).append(", operationname: ").append(operation.getName()).append(", desciption: ").append(operation.getDescription()));
            }
        }
    }
    
    private Object doOperation(final MBeanServerConnection mbc, final String fullqualifiedoperationname) throws MalformedObjectNameException, NullPointerException, IOException, InvalidDataException, InstanceNotFoundException, MBeanException, ReflectionException {
        final String[] split = fullqualifiedoperationname.split("!");
        if (2 == split.length) {
            final ObjectName objectName = new ObjectName(split[0]);
            final Object result = mbc.invoke(objectName, split[1], null, null);
            return result;
        } else {
            throw new InvalidDataException("The given operationname is not valid. It couldn't be split at \"!\"");
        }
    }
}
