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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.report.client.jmx;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

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
import javax.management.RuntimeMBeanException;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.BasicCommandlineOptions;
import com.openexchange.admin.console.AdminParser.NeededQuadState;
import com.openexchange.admin.console.CmdLineParser.Option;

public abstract class AbstractJMXTools extends BasicCommandlineOptions {

	static final String JMX_SERVER_PORT = "9999";
	static final String JMX_ADMIN_PORT = "9998";
	protected static final char OPT_HOST_SHORT = 'H';
	protected static final String OPT_HOST_LONG = "host";
	protected static final String OPT_JMX_AUTH_USER_LONG = "jmxauthuser";
	protected static final char OPT_JMX_AUTH_USER_SHORT = 'J';
	protected static final String OPT_JMX_AUTH_PASSWORD_LONG = "jmxauthpassword";
	protected static final char OPT_JMX_AUTH_PASSWORD_SHORT = 'P';
	protected String JMX_HOST = "localhost";
	protected Option host = null;
	protected Option jmxuser = null;
	protected Option jmxpass = null;
	String ox_jmx_url = null;
	JMXConnector c = null;

	/**
	 * This method is called after a hostname change and input parsing, because
	 * the url depends on both steps
	 */
	protected void updatejmxurl(final boolean showAdminStats) {
	    final String jmxPort = showAdminStats ? JMX_ADMIN_PORT : JMX_SERVER_PORT;
	    this.ox_jmx_url = new StringBuilder("service:jmx:rmi:///jndi/rmi://").append(JMX_HOST).append(':').append(jmxPort).append("/server").toString();
	}

	protected MBeanServerConnection initConnection(final boolean adminstats, final HashMap<String, String[]> env)
			throws InterruptedException, IOException {
			    updatejmxurl(adminstats);
			    // Set timeout here, it is given in ms
			    final long timeout = 2000;
			    final JMXServiceURL serviceurl = new JMXServiceURL(ox_jmx_url);
			    final IOException[] exc = new IOException[1];
			    final RuntimeException[] excr = new RuntimeException[1];
			    final Thread t = new Thread() {
			        @Override
			        public void run() {
			            try {
			                c = JMXConnectorFactory.connect(serviceurl,env);
			            } catch (IOException e) {
			                exc[0] = e;
			            } catch (RuntimeException e) {
			                excr[0] = e;
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
			    if (excr[0] != null) {
			        throw excr[0];
			    }
			    return c.getMBeanServerConnection();
			}

	protected void closeConnection() {
	    if (c != null) {
	        try {
	            c.close();
	        } catch (final IOException ex) {
	            ex.printStackTrace();
	        }
	    }
	}
	
	protected void setOptionsAbstract(AdminParser parser) {
		this.host = setShortLongOpt(parser, OPT_HOST_SHORT, OPT_HOST_LONG, "host", "Specifies the host", false);
        this.jmxuser = setShortLongOpt(parser, OPT_JMX_AUTH_USER_SHORT, OPT_JMX_AUTH_USER_LONG, "JMX username (required when jmx authentication enabled)", true, NeededQuadState.notneeded);
        this.jmxpass = setShortLongOpt(parser, OPT_JMX_AUTH_PASSWORD_SHORT, OPT_JMX_AUTH_PASSWORD_LONG, "JMX username (required when jmx authentication enabled)", true, NeededQuadState.notneeded);
	}

	@SuppressWarnings("unchecked")
	protected StringBuffer getStats(final MBeanServerConnection mbc, final String class_name) throws IOException,
			InstanceNotFoundException, MBeanException,
			AttributeNotFoundException, ReflectionException,
			IntrospectionException {
			    StringBuffer retval = new StringBuffer();
				
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
			                                retval.append(sb.toString());
			                            } else {
			                                if (o instanceof String[]) {
			                                    final String[] c = (String[]) o;
			                                    retval.append(sb.append(Arrays.toString(c)).toString());
			                                } else if (o instanceof long[]) {
			                                    final long[] l = (long[]) o;
			                                    retval.append(sb.append(Arrays.toString(l)).toString());
			                                } else {
			                                	retval.append(sb.append(o.toString()).toString());
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
			    
			    return retval;
			}

}
