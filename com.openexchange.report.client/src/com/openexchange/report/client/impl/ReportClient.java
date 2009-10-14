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

package com.openexchange.report.client.impl;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.net.ssl.HttpsURLConnection;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.NeededQuadState;
import com.openexchange.admin.console.CmdLineParser.IllegalOptionValueException;
import com.openexchange.admin.console.CmdLineParser.Option;
import com.openexchange.admin.console.CmdLineParser.UnknownOptionException;
import com.openexchange.admin.rmi.exceptions.MissingOptionException;
import com.openexchange.report.client.jmx.AbstractJMXTools;

public class ReportClient extends AbstractJMXTools {
		
	private static final String REPORT_SERVER_URL = "https://activation.open-xchange.com";
	
	private static final String POST_LICENSE_KEYS_KEY = "license_keys";
	
	private static final String POST_METADATA_KEY = "client_information";
	
	private static final String URL_ENCODING = "UTF-8";
	
	private static final char OPT_SEND_ONLY_SHORT = 's';
	
    private static final String OPT_SEND_ONLY_LONG = "sendonly";
    
	private static final char OPT_DISPLAY_ONLY_SHORT = 'd';
	
    private static final String OPT_DISPLAY_ONLY_LONG = "displayonly";
        
    private Option displayonly = null;

    private Option sendonly = null;

    public static void main(final String args[]) {
        final ReportClient t = new ReportClient();
        t.start(args);
    }
    
    public void start(final String args[]) {
        final AdminParser parser = new AdminParser("report");
        
        setOptions(parser);

        try {
            parser.ownparse(args);

            final String jmxuser = (String)parser.getOptionValue(this.jmxuser);
            final String jmxpass = (String)parser.getOptionValue(this.jmxpass);
            HashMap<String, String[]> env = null;
            
            if( jmxuser != null && jmxuser.trim().length() > 0 ) {
                if( jmxpass == null ) {
                    throw new IllegalOptionValueException(this.jmxpass,null);
                }
                env = new HashMap<String, String[]>();
                String[] creds = new String[]{ jmxuser, jmxpass };
                env.put(JMXConnector.CREDENTIALS, creds);
            }

            final String host = (String) parser.getOptionValue(this.host);
            if (null != host) {
                JMX_HOST = host;
            }
            
            if ((null != parser.getOptionValue(this.sendonly) && (null != parser.getOptionValue(this.displayonly)))) {
                System.err.println("More than one of the stat options given. Using the default one one only (display and send)");
                
                final MBeanServerConnection initConnection = initConnection(false, env);
                String metadata = sendReport(initConnection);
                System.out.println(metadata);
            } else {
                int count = 0;
                if (null != parser.getOptionValue(this.sendonly)) {
                    final MBeanServerConnection initConnection = initConnection(false, env);
                    sendReport(initConnection);
                    count++;
                }
                if (null != parser.getOptionValue(this.displayonly)) {
                    if (0 == count) {
                        final MBeanServerConnection initConnection = initConnection(false, env);
                        System.out.println(getMetadata(initConnection));
                    }
                    count++;
                }
                if (0 == count) {
                	System.err.println("No option selected. Using the default one one (display and send)");
                	
                    final MBeanServerConnection initConnection = initConnection(false, env);
                    String metadata = sendReport(initConnection);
                    System.out.println(metadata);
                }
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
        } catch (final MalformedURLException e) {
            printServerException(e, parser);
            sysexit(1);
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
        } catch (final NullPointerException e) {
            printServerException(e, parser);
            sysexit(1);
        } finally {
            closeConnection();
        }
    }
    
	private void setOptions(AdminParser parser) {
		super.setOptionsAbstract(parser);
		this.sendonly = setShortLongOpt(parser, OPT_SEND_ONLY_SHORT, OPT_SEND_ONLY_LONG, "Send report without displaying it (Disables default)", false, NeededQuadState.notneeded);
		this.displayonly = setShortLongOpt(parser, OPT_DISPLAY_ONLY_SHORT, OPT_DISPLAY_ONLY_LONG, "Display report without sending it (Disables default)", false, NeededQuadState.notneeded);
	}
    
	private String sendReport(final MBeanServerConnection mbc) throws MalformedURLException, IOException, InstanceNotFoundException, AttributeNotFoundException, IntrospectionException, MBeanException, ReflectionException {
		String metadata = getMetadata(mbc);

		StringBuffer report = new StringBuffer();
		report.append(POST_LICENSE_KEYS_KEY);
		report.append("=");
		report.append(URLEncoder.encode(getLicenseKey(), URL_ENCODING));
		report.append("&");
		report.append(POST_METADATA_KEY);
		report.append("=");
		report.append(URLEncoder.encode(metadata, URL_ENCODING));

		HttpsURLConnection httpsURLConnection= (HttpsURLConnection) new URL(REPORT_SERVER_URL).openConnection();
		httpsURLConnection.setUseCaches(false);
		httpsURLConnection.setDoOutput(true);
		httpsURLConnection.setDoInput(true);
		httpsURLConnection.setRequestMethod("POST");
		httpsURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

		DataOutputStream stream = new DataOutputStream(httpsURLConnection.getOutputStream ());
		stream.writeBytes(report.toString());
		stream.flush();
		stream.close();

		if (httpsURLConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
			throw new MalformedURLException("Problem contacting report server: " + httpsURLConnection.getResponseCode());
		} else {
			BufferedReader in = new BufferedReader(new InputStreamReader(httpsURLConnection.getInputStream()));
			String buffer = "";
			while ((buffer = in.readLine()) != null) {
				System.out.println(buffer);
			}
			in.close();
		}

		return metadata;
	}
    
    private String getLicenseKey() {
    	return "OX-AS-MK-123456-987";
    }
    
    private String getMetadata(final MBeanServerConnection mbc) throws InstanceNotFoundException, AttributeNotFoundException, IntrospectionException, MBeanException, ReflectionException, IOException {
    	StringBuffer report = new StringBuffer();
    	
    	report.append(getStats(mbc, "com.openexchange.ajp13.monitoring.AJPv13ServerThreadsMonitor").toString());
    	report.append(getStats(mbc, "com.openexchange.ajp13.najp.AJPv13ListenerMonitor"));
    	report.append(getStats(mbc, "com.openexchange.monitoring.internal.GeneralMonitor"));
    	report.append(getStats(mbc, "com.openexchange.api2.MailInterfaceMonitor"));
    	report.append(getStats(mbc, "com.openexchange.database.internal.ConnectionPool"));
        
    	report.append(getStats(mbc, "com.sun.management.UnixOperatingSystem"));
    	report.append(getStats(mbc, "sun.management.RuntimeImpl"));
        
    	report.append(getStats(mbc, "sun.management.MemoryPoolImpl"));
    	report.append(getStats(mbc, "sun.management.ThreadImpl"));
    	
    	return report.toString();
    }
    
}
