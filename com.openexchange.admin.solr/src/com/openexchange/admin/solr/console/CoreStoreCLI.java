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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.admin.solr.console;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Formatter;
import java.util.List;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.NeededQuadState;
import com.openexchange.admin.console.CLIOption;
import com.openexchange.admin.console.ObjectNamingAbstraction;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.solr.SolrCoreStore;
import com.openexchange.solr.SolrMBean;

/**
 * {@link CoreStoreCLI}
 * 
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class CoreStoreCLI extends ObjectNamingAbstraction {

    private static final String JMX_URL = "service:jmx:rmi:///jndi/rmi://localhost:9999/server";

    private static final String ACTION_REGISTER = "registercorestore";

    private static final String ACTION_UNREGISTER = "unregistercorestore";

    private static final String ACTION_LIST = "listcorestore";

    private static final String ACTION_CHANGE = "changecorestore";

    private static final char OPT_STORE_ID_SHORT = 'i';

    private static final String OPT_STORE_ID_LONG = "id";

    private static final String OPT_STORE_ID_DESCRIPTION = "The core store id.";

    private static final char OPT_NUM_CORES_SHORT = 'c';

    private static final String OPT_NUM_CORES_LONG = "cores";

    private static final String OPT_NUM_CORES_DESCRIPTION = "The maximal number of cores that this core store can handle.";

    private static final char OPT_URI_SHORT = 'u';

    private static final String OPT_URI_LONG = "uri";

    private static final String OPT_URI_DESCRIPTION = "An absolute URI (rfc2396 compliant) that points to the directory where the core store is mounted. E.g. file:/path/to/mount/point.";

    private CLIOption idOption;

    private CLIOption coresOption;

    private CLIOption uriOption;

    private AdminParser parser;

    public static void main(String[] args) {
        CoreStoreCLI newCoreStoreCLI = new CoreStoreCLI();
        newCoreStoreCLI.start(args);
    }

    public void start(String[] args) {
        if (args == null || args.length == 0) {
            System.out.println("Missing argument 'action'.");
            System.exit(1);
        }

        String[] actionArgs;
        if (args.length > 1) {
            actionArgs = new String[args.length - 1];
            System.arraycopy(args, 1, actionArgs, 0, actionArgs.length);
        } else {
            actionArgs = new String[0];
        }

        String action = args[0];
        if (action.indexOf('/') >= 0) {
            action = action.substring(action.lastIndexOf('/') + 1);
        }

        if (action.equals(ACTION_REGISTER)) {
            register(actionArgs);
        } else if (action.equals(ACTION_UNREGISTER)) {
            unregister(actionArgs);
        } else if (action.equals(ACTION_LIST)) {
            list(actionArgs);
        } else if (action.equals(ACTION_CHANGE)) {
            change(actionArgs);
        } else {
            System.out.println("Unknown action '" + action + "'.");
            System.exit(1);
        }
    }

    private void register(String[] args) {
        parser = new AdminParser(ACTION_REGISTER);
        setDefaultCommandLineOptionsWithoutContextID(parser);
        coresOption = setShortLongOpt(
            parser,
            OPT_NUM_CORES_SHORT,
            OPT_NUM_CORES_LONG,
            OPT_NUM_CORES_DESCRIPTION,
            true,
            NeededQuadState.needed);
        uriOption = setShortLongOpt(parser, OPT_URI_SHORT, OPT_URI_LONG, OPT_URI_DESCRIPTION, true, NeededQuadState.needed);
        try {
            parser.ownparse(args);
            Credentials credentials = credentialsparsing(parser);
            int maxCores = parseCores();
            URI uri = parseUri();

            JMXServiceURL url = new JMXServiceURL(JMX_URL);
            JMXConnector jmxConnector = JMXConnectorFactory.connect(url, null);
            try {
                MBeanServerConnection mbsc = jmxConnector.getMBeanServerConnection();
                SolrMBean solrMBean = solrMBeanProxy(mbsc);
                int id = solrMBean.registerCoreStore(credentials, uri, maxCores);

                System.out.println("Core store '" + id + "' was successfully registered.");
                sysexit(0);
            } catch (MBeanException e) {
                Exception targetException = e.getTargetException();
                if (targetException == null) {
                    throw e;
                }

                throw targetException;
            } finally {
                try {
                    jmxConnector.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        } catch (Exception e) {
            printErrors(null, null, e, parser);
            sysexit(1);
        }
    }

    private void unregister(String[] args) {
        parser = new AdminParser(ACTION_UNREGISTER);
        setDefaultCommandLineOptionsWithoutContextID(parser);
        idOption = setShortLongOpt(parser, OPT_STORE_ID_SHORT, OPT_STORE_ID_LONG, OPT_STORE_ID_DESCRIPTION, true, NeededQuadState.needed);
        try {
            parser.ownparse(args);
            Credentials credentials = credentialsparsing(parser);
            int id = parseId();

            JMXServiceURL url = new JMXServiceURL(JMX_URL);
            JMXConnector jmxConnector = JMXConnectorFactory.connect(url, null);
            try {
                MBeanServerConnection mbsc = jmxConnector.getMBeanServerConnection();
                SolrMBean solrMBean = solrMBeanProxy(mbsc);
                solrMBean.unregisterCoreStore(credentials, id);

                System.out.println("Core store '" + id + "' was successfully unregistered.");
                sysexit(0);
            } catch (MBeanException e) {
                Exception targetException = e.getTargetException();
                if (targetException == null) {
                    throw e;
                }

                throw targetException;
            } finally {
                try {
                    jmxConnector.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        } catch (Exception e) {
            printErrors(null, null, e, parser);
            sysexit(1);
        }
    }

    private void change(String[] args) {
        parser = new AdminParser(ACTION_CHANGE);
        setDefaultCommandLineOptionsWithoutContextID(parser);
        idOption = setShortLongOpt(parser, OPT_STORE_ID_SHORT, OPT_STORE_ID_LONG, OPT_STORE_ID_DESCRIPTION, true, NeededQuadState.needed);
        coresOption = setShortLongOpt(parser, OPT_NUM_CORES_SHORT, OPT_NUM_CORES_LONG, OPT_NUM_CORES_DESCRIPTION, true, NeededQuadState.eitheror);
        uriOption = setShortLongOpt(parser, OPT_URI_SHORT, OPT_URI_LONG, OPT_URI_DESCRIPTION, true, NeededQuadState.eitheror);
        try {
            parser.ownparse(args);
            Credentials credentials = credentialsparsing(parser);
            int id = parseId();
            int maxCores = optCores();
            URI uri = optUri();

            JMXServiceURL url = new JMXServiceURL(JMX_URL);
            JMXConnector jmxConnector = JMXConnectorFactory.connect(url, null);
            try {
                MBeanServerConnection mbsc = jmxConnector.getMBeanServerConnection();
                SolrMBean solrMBean = solrMBeanProxy(mbsc);
                SolrCoreStore coreStore = solrMBean.getCoreStore(id);
                SolrCoreStore modified = new SolrCoreStore();
                modified.setUri(uri == null ? coreStore.getUri() : uri);
                modified.setMaxCores(maxCores < 0 ? coreStore.getMaxCores() : maxCores);
                modified.setNumCores(coreStore.getNumCores());
                solrMBean.modifyCoreStore(credentials, id, modified.getUri(), modified.getMaxCores());

                System.out.println("Core store '" + id + "' was successfully modified.");
                sysexit(0);
            } catch (MBeanException e) {
                Exception targetException = e.getTargetException();
                if (targetException == null) {
                    throw e;
                }

                throw targetException;
            } finally {
                try {
                    jmxConnector.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        } catch (Exception e) {
            printErrors(null, null, e, parser);
            sysexit(1);
        }
    }

    private void list(String[] args) {
        parser = new AdminParser(ACTION_LIST);
        setDefaultCommandLineOptionsWithoutContextID(parser);
        try {
            parser.ownparse(args);
            Credentials credentials = credentialsparsing(parser);

            JMXServiceURL url = new JMXServiceURL(JMX_URL);
            JMXConnector jmxConnector = JMXConnectorFactory.connect(url, null);
            try {
                MBeanServerConnection mbsc = jmxConnector.getMBeanServerConnection();
                SolrMBean solrMBean = solrMBeanProxy(mbsc);
                List<SolrCoreStore> stores = solrMBean.getAllStores(credentials);
                Formatter formatter = new Formatter();
                formatter.format("%1$8s | %2$64s | %3$10s | %4$10s", "id", "uri", "max. cores", "cores");
                formatter.flush();
                System.out.println(formatter.toString());
                System.out.println("-----------------------------------------------------------------------------------------------------");
                for (SolrCoreStore store : stores) {
                    int id = store.getId();
                    int maxCores = store.getMaxCores();
                    int numCores = store.getNumCores();
                    String uri = store.getUri().toString();

                    formatter = new Formatter();
                    formatter.format("%1$8d | %2$64s | %3$10d | %4$10d", id, uri, maxCores, numCores);
                    formatter.flush();
                    System.out.println(formatter.toString());
                }

                sysexit(0);
            } catch (MBeanException e) {
                Exception targetException = e.getTargetException();
                if (targetException == null) {
                    throw e;
                }

                throw targetException;
            } finally {
                try {
                    jmxConnector.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        } catch (Exception e) {
            printErrors(null, null, e, parser);
            sysexit(1);
        }
    }

    private int parseId() {
        Object optionValue = parser.getOptionValue(idOption);
        if (optionValue == null) {
            throw new IllegalArgumentException("Option '" + OPT_STORE_ID_LONG + "' must be a valid number.");
        }

        try {
            return Integer.parseInt(optionValue.toString());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Option '" + OPT_STORE_ID_LONG + "' must be a valid number.");
        }
    }

    private int parseCores() {
        Object optionValue = parser.getOptionValue(coresOption);
        if (optionValue == null) {
            throw new IllegalArgumentException("Option '" + OPT_NUM_CORES_LONG + "' must be a valid number.");
        }

        try {
            return Integer.parseInt(optionValue.toString());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Option '" + OPT_NUM_CORES_LONG + "' must be a valid number.");
        }
    }

    private URI parseUri() {
        Object optionValue = parser.getOptionValue(uriOption);
        if (optionValue == null) {
            throw new IllegalArgumentException("Option '" + OPT_URI_LONG + "' must be a valid URI.");
        }

        try {
            return new URI(optionValue.toString());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Option '" + OPT_URI_LONG + "' must be a valid URI.");
        }
    }

    private int optCores() {
        Object optionValue = parser.getOptionValue(coresOption);
        if (optionValue == null) {
            return -1;
        }

        try {
            return Integer.parseInt(optionValue.toString());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Option '" + OPT_NUM_CORES_LONG + "' must be a valid number.");
        }
    }

    private URI optUri() {
        Object optionValue = parser.getOptionValue(uriOption);
        if (optionValue == null) {
            return null;
        }

        try {
            return new URI(optionValue.toString());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Option '" + OPT_URI_LONG + "' must be a valid URI.");
        }
    }

    private SolrMBean solrMBeanProxy(MBeanServerConnection mbsc) throws MalformedObjectNameException {
        SolrMBean solrMBean = MBeanServerInvocationHandler.newProxyInstance(mbsc, new ObjectName(
            SolrMBean.DOMAIN,
            SolrMBean.KEY,
            SolrMBean.VALUE), SolrMBean.class, false);

        return solrMBean;
    }

    @Override
    protected String getObjectName() {
        return "solr core";
    }

}
