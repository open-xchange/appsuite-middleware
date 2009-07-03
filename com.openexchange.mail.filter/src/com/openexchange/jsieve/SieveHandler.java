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
package com.openexchange.jsieve;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.jsieve.exceptions.OXSieveHandlerException;
import com.openexchange.jsieve.exceptions.OXSieveHandlerInvalidCredentialsException;

/**
 * This class is used to deal with the communication with sieve. For a
 * description of the communication system to sieve see {@see <a
 * href="http://www.ietf.org/internet-drafts/draft-martin-managesieve-07.txt"
 * >http://www.ietf.org/internet-drafts/draft-martin-managesieve-07.txt</a>}
 * 
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 */
public class SieveHandler {

    private final static String CRLF = "\r\n";

    private final static String SIEVE_OK = "OK";

    private final static String SIEVE_NO = "NO";

    private final static String SIEVE_AUTH = "AUTHENTICATE ";

    private final static String SIEVE_AUTH_FAILD = "NO \"Authentication Error\"";

    private final static String SIEVE_AUTH_LOGIN_USERNAME = "{12}" + CRLF + "VXNlcm5hbWU6";

    private final static String SIEVE_AUTH_LOGIN_PASSWORD = "{12}" + CRLF + "UGFzc3dvcmQ6";

    private final static String SIEVE_PUT = "PUTSCRIPT ";

    private final static String SIEVE_ACTIVE = "SETACTIVE ";

    private final static String SIEVE_DEACTIVE = "SETACTIVE \"\"" + CRLF;

    private final static String SIEVE_DELETE = "DELETESCRIPT ";

    private final static String SIEVE_LIST = "LISTSCRIPTS" + CRLF;

    private final static String SIEVE_GET_SCRIPT = "GETSCRIPT ";

    private final static String SIEVE_LOGOUT = "LOGOUT" + CRLF;

    /*-
     * Member section
     */

    private boolean AUTH = false;

    private String sieve_user = null;

    private String sieve_auth = null;

    private String sieve_auth_passwd = null;

    private String sieve_host = "127.0.0.1";

    private int sieve_host_port = 2000;

    private Capabilities capa = null;

    private Socket s_sieve = null;

    private BufferedReader bis_sieve = null;

    private BufferedOutputStream bos_sieve = null;

    private static Log log = LogFactory.getLog(SieveHandler.class);
    
    /**
     * SieveHandler use socket-connection to manage sieve-scripts.<br>
     * <br>
     * Important: Don't forget to close the SieveHandler!
     * 
     * @param userName
     * @param passwd
     * @param host
     * @param port
     */
    public SieveHandler(final String userName, final String passwd, final String host, final int port) {
        sieve_user = userName;
        sieve_auth = userName;
        sieve_auth_passwd = passwd;
        sieve_host = host;
        sieve_host_port = port;
    }


    public SieveHandler(final String userName, final String authUserName, final String authUserPasswd, final String host, final int port) {
        sieve_user = userName;
        sieve_auth = authUserName;
        sieve_auth_passwd = authUserPasswd;
        sieve_host = host;
        sieve_host_port = port;

    }

    private long mStart;
    private long mEnd;
    private void measureStart() {
        this.mStart = System.currentTimeMillis();
    }
    
    private void measureEnd(final String method) {
        this.mEnd = System.currentTimeMillis();
        if( log.isDebugEnabled() ) {
            log.debug("SieveHandler."+ method + "() took " + (this.mEnd-this.mStart) + "ms to perform");
        }
    }
    
    /**
     * Use this function to initialize the connection. It will get the welcome messages from the
     * server, parse the capabilities and login the user.
     * 
     * @throws IOException
     * @throws UnsupportedEncodingException
     * @throws OXSieveHandlerException
     * @throws OXSieveHandlerInvalidCredentialsException 
     */
    public void initializeConnection() throws IOException, OXSieveHandlerException, UnsupportedEncodingException, OXSieveHandlerInvalidCredentialsException {
        measureStart();
        s_sieve = new Socket();
        /*
         * Connect with a connect-timeout of 30sec
         */
        s_sieve.connect(new InetSocketAddress(sieve_host, sieve_host_port), 30000);
        /*
         * Set timeout to 30sec
         */
        s_sieve.setSoTimeout(30000);
        bis_sieve = new BufferedReader(new InputStreamReader(s_sieve.getInputStream(),"UTF-8"));
        bos_sieve = new BufferedOutputStream(s_sieve.getOutputStream());

        if (getServerWelcome()) {
            log.debug("Got welcome from sieve");
        } else {
            throw new OXSieveHandlerException("No welcome from server", sieve_host, sieve_host_port);
        }
        measureEnd("getServerWelcome");
        
        measureStart();
        final ArrayList<String> temp = capa.getSasl();
        measureEnd("capa.getSasl");
        
        if (null != temp && temp.contains("PLAIN")) {
            measureStart();
            if (selectAuth("PLAIN")) {
                log.debug("Authentication to sieve successful");
            } else {
                throw new OXSieveHandlerInvalidCredentialsException("Authentication failed");
            }
            measureEnd("selectAuth");
        } else {
            throw new OXSieveHandlerException("The server doesn't suppport PLAIN authentication", sieve_host, sieve_host_port);
        }
    }

    /**
     * Upload this byte[] as sieve script
     * 
     * @param script_name
     * @param script
     * @throws OXSieveHandlerException
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    public void setScript(final String script_name, final byte[] script) throws OXSieveHandlerException, IOException, UnsupportedEncodingException {
        if (AUTH == false) {
            throw new OXSieveHandlerException("Script upload not possible. Auth first.", sieve_host, sieve_host_port);
        }

        if (script == null) {
            throw new OXSieveHandlerException("Script upload not possible. No Script", sieve_host, sieve_host_port);
        }

        final String put = SIEVE_PUT + '\"' + script_name + "\" {" + script.length + "+}" + CRLF;
        bos_sieve.write(put.getBytes("UTF-8"));
        bos_sieve.write(script);

        bos_sieve.write(CRLF.getBytes("UTF-8"));
        bos_sieve.flush();

        final StringBuilder sb = new StringBuilder();
        final String actualline = bis_sieve.readLine();
        if (null != actualline && actualline.startsWith(SIEVE_OK)) {
            return;
        } else if (null != actualline && actualline.startsWith("NO ")) {
            final String answer = actualline.substring(3);
            final Pattern p = Pattern.compile("^\\{([^\\}]*)\\}.*$");
            final Matcher matcher = p.matcher(answer);
            if (matcher.matches()) {
                final String group = matcher.group(1);
                final int octetsToRead = Integer.parseInt(group);
                final char[] buf = new char[octetsToRead];
                final int octetsRead = bis_sieve.read(buf, 0, octetsToRead);
                if (octetsRead == octetsToRead) {
                    sb.append(buf);
                } else {
                    sb.append(buf, 0, octetsRead);
                }
                sb.append(CRLF);
            } else {
                sb.append(answer);
                sb.append(CRLF);
            }
            throw new OXSieveHandlerException(sb.toString(), sieve_host, sieve_host_port);
        } else {
            throw new OXSieveHandlerException("Unknown error occured", sieve_host, sieve_host_port);
        }
    }

    /**
     * Activate/Deactivate sieve script. Is status is true, activate this
     * script.
     * 
     * @param script_name
     * @param status
     * @throws IOException
     * @throws UnsupportedEncodingException
     * @throws OXSieveHandlerException
     */
    public void setScriptStatus(final String script_name, final boolean status) throws OXSieveHandlerException, UnsupportedEncodingException, IOException {
        if (status) {
            activate(script_name);
        } else {
            deactivate(script_name);
        }
    }

    /**
     * Get the sieveScript, if a script doesn't exists a byte[] with a size of 0 is returned
     * 
     * @param script_name
     * @return the read script
     * @throws OXSieveHandlerException
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    public String getScript(final String script_name) throws OXSieveHandlerException, UnsupportedEncodingException, IOException {
        if (!AUTH) {
            throw new OXSieveHandlerException("Get script not possible. Auth first.", sieve_host, sieve_host_port);
        }
        final String get = SIEVE_GET_SCRIPT + "\"" + script_name + "\"" + CRLF;
        bos_sieve.write(get.getBytes("UTF-8"));
        bos_sieve.flush();
        final StringBuilder sb = new StringBuilder();
        boolean firstread = true;
        while (true) {
            final String temp = bis_sieve.readLine();
            if (temp.startsWith(SIEVE_OK)) {
                // We have to strip off the last trailing CRLF...
                return sb.substring(0, sb.length() - 2);
            } else if (temp.startsWith(SIEVE_NO)) {
                return "";
            }
            // The first line contains the length of the following byte set, we don't need this
            // information here and so strip it off...
            if (firstread) {
                firstread = false;
            } else {
                sb.append(temp);
                sb.append(CRLF);
            }
        }
    }

    /**
     * Get the list of sieveScripts
     * 
     * @return List of scripts
     * @throws IOException
     * @throws UnsupportedEncodingException
     * @throws OXSieveHandlerException
     */
    public ArrayList<String> getScriptList() throws OXSieveHandlerException, UnsupportedEncodingException, IOException {
        if (AUTH == false) {
            throw new OXSieveHandlerException("List scripts not possible. Auth first.", sieve_host, sieve_host_port);
        }

        final String active = SIEVE_LIST;
        bos_sieve.write(active.getBytes("UTF-8"));
        bos_sieve.flush();

        final ArrayList<String> list = new ArrayList<String>();
        while (true) {
            final String temp = bis_sieve.readLine();
            if (temp.startsWith(SIEVE_OK)) {
                return list;
            }
            if (temp.startsWith(SIEVE_NO)) {
                throw new OXSieveHandlerException("Sieve has no script list", sieve_host, sieve_host_port);
            }
            // Here we strip off the leading and trailing " and the ACTIVE at the
            // end if it occurs. We want a list of the script names only
            final String scriptname = temp.substring(temp.indexOf('\"') + 1, temp.lastIndexOf('\"'));
            list.add(scriptname);
        }

    }

    /**
     * Get the list of active sieve scripts
     * 
     * @return List of scripts
     * @throws IOException
     * @throws UnsupportedEncodingException
     * @throws OXSieveHandlerException
     */
    public String getActiveScript() throws OXSieveHandlerException, UnsupportedEncodingException, IOException {
        if (AUTH == false) {
            throw new OXSieveHandlerException("List scripts not possible. Auth first.", sieve_host, sieve_host_port);
        }

        final String active = SIEVE_LIST;
        bos_sieve.write(active.getBytes("UTF-8"));
        bos_sieve.flush();

        String scriptname = null;
        while (true) {
            final String temp = bis_sieve.readLine();
            if (temp.startsWith(SIEVE_OK)) {
                return scriptname;
            }
            if (temp.startsWith(SIEVE_NO)) {
                throw new OXSieveHandlerException("Sieve has no script list", sieve_host, sieve_host_port);
            }
            
            if (temp.matches(".*ACTIVE")) {
                scriptname = temp.substring(temp.indexOf('\"') + 1, temp.lastIndexOf('\"'));
            }
        }

    }

    
    /**
     * Remove the sieve script. If the script is active it is deactivated before removing
     * 
     * @param script_name
     * @throws IOException
     * @throws UnsupportedEncodingException
     * @throws OXSieveHandlerException
     */
    public void remove(final String script_name) throws OXSieveHandlerException, UnsupportedEncodingException, IOException {
        if (AUTH == false) {
            throw new OXSieveHandlerException("Delete a script not possible. Auth first.", sieve_host, sieve_host_port);
        }
        if (null == script_name) {
            throw new OXSieveHandlerException("Script can't be removed", sieve_host, sieve_host_port);
        }
        
        setScriptStatus(script_name, false);
        
        final String delete = SIEVE_DELETE + "\"" + script_name + "\"" + CRLF;
        bos_sieve.write(delete.getBytes("UTF-8"));
        bos_sieve.flush();

        while (true) {
            final String temp = bis_sieve.readLine();
            if (temp.startsWith(SIEVE_OK)) {
                return;
            } else if (temp.startsWith(SIEVE_NO)) {
                throw new OXSieveHandlerException("Script can't be removed", sieve_host, sieve_host_port);
            }
        }
    }

    /**
     * Close socket-connection to sieve
     * 
     * @throws IOException
     * @throws UnsupportedEncodingException
     */
    public void close() throws IOException, UnsupportedEncodingException {
        if (null != bos_sieve) {
            bos_sieve.write(SIEVE_LOGOUT.getBytes("UTF-8"));
            bos_sieve.flush();
        }
        if (null != s_sieve) {
            s_sieve.close();
        }
    }

    private boolean getServerWelcome() throws UnknownHostException, IOException {
        capa = new Capabilities();

        while (true) {
            final String test = bis_sieve.readLine();
            if (test.startsWith(SIEVE_OK)) {
                return true;
            } else if (test.startsWith(SIEVE_NO)) {
                AUTH = false;
                return false;
            } else {
                parseCAPA(test);
            }
        }
    }

    private boolean authPLAIN() throws IOException, UnsupportedEncodingException {
        final String to64 = sieve_user + '\0' + sieve_auth + '\0' + sieve_auth_passwd;
        final String user_auth_pass_64 = convertStringToBase64(to64) + CRLF;
        final String auth_mech_string = SIEVE_AUTH + "\"PLAIN\" ";
        final String user_size = "{" + (user_auth_pass_64.length() - 2) + "+}" + CRLF;

        // We don't need to specify an encoding here because all strings contain only ASCII Text
        bos_sieve.write(auth_mech_string.getBytes());
        bos_sieve.write(user_size.getBytes());
        bos_sieve.write(user_auth_pass_64.getBytes());
        bos_sieve.flush();

        while (true) {
            final String temp = bis_sieve.readLine();
            if (null != temp) {
                if (temp.startsWith(SIEVE_OK)) {
                    AUTH = true;
                    return true;
                } else if (temp.startsWith(SIEVE_NO)) {
                    AUTH = false;
                    return false;
                }
            } else {
                AUTH = false;
                return false;
            }
        }
    }

    // FIXME: Not tested yet
    private boolean authLOGIN() throws IOException, OXSieveHandlerException, UnsupportedEncodingException {

        final String auth_mech_string = SIEVE_AUTH + "\"LOGIN\"" + CRLF;
        bos_sieve.write(auth_mech_string.getBytes("UTF-8"));
        bos_sieve.flush();

        while (true) {
            final String temp = bis_sieve.readLine();
            if (temp.endsWith(SIEVE_AUTH_LOGIN_USERNAME)) {
                break;
            } else if (temp.endsWith(SIEVE_AUTH_FAILD)) {
                throw new OXSieveHandlerException("can't auth to SIEVE ", sieve_host, sieve_host_port);
            }
        }

        final String user64 = convertStringToBase64(sieve_auth) + CRLF;
        final String user_size = '{' + (user64.length() - 2) + "+}" + CRLF;
        bos_sieve.write(user_size.getBytes("UTF-8"));
        bos_sieve.write(user64.getBytes("UTF-8"));
        bos_sieve.flush();

        while (true) {
            final String temp = bis_sieve.readLine();
            if (temp.endsWith(SIEVE_AUTH_LOGIN_PASSWORD)) {
                break;
            } else if (temp.endsWith(SIEVE_AUTH_FAILD)) {
                throw new OXSieveHandlerException("can't auth to SIEVE ", sieve_host, sieve_host_port);
            }
        }

        final String pass64 = convertStringToBase64(sieve_auth_passwd) + CRLF;
        final String pass_size = '{' + (pass64.length() - 2) + "+}" + CRLF;
        bos_sieve.write(pass_size.getBytes("UTF-8"));
        bos_sieve.write(pass64.getBytes("UTF-8"));
        bos_sieve.flush();

        while (true) {
            final String temp = bis_sieve.readLine();
            if (temp.startsWith(SIEVE_OK)) {
                AUTH = true;
                return true;
            } else if (temp.startsWith(SIEVE_AUTH_FAILD)) {
                throw new OXSieveHandlerException("can't auth to SIEVE ", sieve_host, sieve_host_port);
            }
        }
    }

    private void activate(final String sieve_script_name) throws OXSieveHandlerException, UnsupportedEncodingException, IOException {
        if (AUTH == false) {
            throw new OXSieveHandlerException("Activate a script not possible. Auth first.", sieve_host, sieve_host_port);
        }

        final String active = SIEVE_ACTIVE + '\"' + sieve_script_name + '\"' + CRLF;
        bos_sieve.write(active.getBytes("UTF-8"));
        bos_sieve.flush();

        while (true) {
            final String temp = bis_sieve.readLine();
            if (temp.startsWith(SIEVE_OK)) {
                return;
            } else if (temp.startsWith(SIEVE_NO)) {
                throw new OXSieveHandlerException("Error while activating script: " + sieve_script_name, sieve_host, sieve_host_port);
            }
        }
    }

    private void deactivate(final String sieve_script_name) throws OXSieveHandlerException, UnsupportedEncodingException, IOException {
        if (AUTH == false) {
            throw new OXSieveHandlerException("Deactivate a script not possible. Auth first.", sieve_host, sieve_host_port);
        }
        
        boolean scriptactive = false;
        if (sieve_script_name.equals(getActiveScript())) {
            scriptactive = true;
        }

        if (scriptactive) {
            bos_sieve.write(SIEVE_DEACTIVE.getBytes("UTF-8"));
            bos_sieve.flush();
            
            while (true) {
                final String temp = bis_sieve.readLine();
                if (temp.startsWith(SIEVE_OK)) {
                    return;
                } else if (temp.startsWith(SIEVE_NO)) {
                    throw new OXSieveHandlerException("Error while deactivating script: " + sieve_script_name, sieve_host, sieve_host_port);
                }
            }
        }
    }

    /**
     * @param auth_mech
     * @return
     * @throws IOException
     * @throws UnsupportedEncodingException
     * @throws OXSieveHandlerException
     */
    private boolean selectAuth(final String auth_mech) throws IOException, UnsupportedEncodingException, OXSieveHandlerException {
        if (auth_mech.equals("PLAIN")) {
            return authPLAIN();
        } else if (auth_mech.equals("LOGIN")) {
            return authLOGIN();
        }
        return false;
    }

    private void parseCAPA(final String line) {
        final String starttls = "\"STARTTLS\"";
        final String implementation = "\"IMPLEMENTATION\"";
        final String sieve = "\"SIEVE\"";
        final String sasl = "\"SASL\"";

        
        String temp = line;

        if (temp.startsWith(starttls)) {
            temp = temp.substring(starttls.length());
            capa.setStarttls(Boolean.TRUE);
        } else if (temp.startsWith(implementation)) {
            temp = temp.substring(implementation.length());
            temp = temp.substring(temp.indexOf('\"') + 1);
            temp = temp.substring(0, temp.indexOf('\"'));

            capa.setImplementation(temp);
        } else if (temp.startsWith(sieve)) {
            temp = temp.substring(sieve.length());
            temp = temp.substring(temp.indexOf("\"") + 1);
            temp = temp.substring(0, temp.indexOf("\""));

            final StringTokenizer st = new StringTokenizer(temp);
            while (st.hasMoreTokens()) {
                capa.addSieve(st.nextToken());
            }
        } else if (temp.startsWith(sasl)) {
            temp = temp.substring(sasl.length());
            temp = temp.substring(temp.indexOf("\"") + 1);
            temp = temp.substring(0, temp.indexOf("\""));

            final StringTokenizer st = new StringTokenizer(temp);
            while (st.hasMoreTokens()) {
                capa.addSasl(st.nextToken().toUpperCase());
            }
        }
    }

    /**
     * 
     * @param toConvert
     * @return Base64String
     * @throws UnsupportedEncodingException
     */
    private String convertStringToBase64(final String toConvert) throws UnsupportedEncodingException {
        final String converted = com.openexchange.tools.encoding.Base64.encode(toConvert.getBytes("UTF-8")); 
        return converted.replaceAll("(\\r)?\\n", "");
    }

    public Capabilities getCapabilities() {
        return this.capa;
    }
}
