package com.openexchange.mobileconfig;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Arrays;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.config.ConfigurationService;
import com.openexchange.mobileconfig.configuration.ConfigurationException;
import com.openexchange.mobileconfig.configuration.MobileConfigProperties;
import com.openexchange.mobileconfig.osgi.Activator;
import com.openexchange.mobileconfig.services.MobileConfigServiceRegistry;


public class MobileConfigServlet extends HttpServlet {

    private enum Device {
        iPhone,
        winMob;
    }
    
    private static final String CRLF = "\r\n";
    
    private static final transient Log LOG = LogFactory.getLog(MobileConfigServlet.class);

    /**
     * 
     */
    private static final long serialVersionUID = 7913468326542861986L;
    
    public static String write(final String host, final String username, final String domain) throws ConfigurationException {
        
        final StringBuilder sb = new StringBuilder();
        sb.append("<wap-provisioningdoc>" + CRLF);
        sb.append("   <characteristic type=\"Sync\">" + CRLF);
        sb.append("        <characteristic type=\"Settings\">" + CRLF);
        sb.append("        <parm name=\"SyncWhenRoaming\" value=\"1\"/>" + CRLF);
        sb.append("        </characteristic>" + CRLF);
        sb.append("        <characteristic type=\"Connection\">" + CRLF);
        sb.append("            <parm name=\"Domain\" value=\"" + domain + "\"/>" + CRLF);
        sb.append("            <parm name=\"Server\" value=\"" + host + "\"/>" + CRLF);
        sb.append("            <parm name=\"User\" value=\"" + username + "\"/>" + CRLF);
        sb.append("\t    <parm name=\"SavePassword\" value=\"1\"/>" + CRLF);
        sb.append("            <parm name=\"URI\" value=\"Microsoft-Server-ActiveSync\"/>" + CRLF);
        sb.append("        <parm name=\"UseSSL\" value=\"1\"/>" + CRLF);
        sb.append("        </characteristic>" + CRLF);
        sb.append("        <characteristic type=\"Mail\">" + CRLF);
        sb.append("            <parm name=\"Enabled\" value=\"1\"/>" + CRLF);
        sb.append("        <parm name=\"EmailAgeFilter\" value=\"3\"/>" + CRLF);
        sb.append("        </characteristic>" + CRLF);
        sb.append("        <characteristic type=\"Calendar\">" + CRLF);
        sb.append("            <parm name=\"Enabled\" value=\"1\"/>" + CRLF);
        sb.append("        <parm name=\"CalendarAgeFilter\" value=\"5\"/>" + CRLF);
        sb.append("        </characteristic>" + CRLF);
        sb.append("        <characteristic type=\"Contacts\">" + CRLF);
        sb.append("            <parm name=\"Enabled\" value=\"1\"/>" + CRLF);
        sb.append("        </characteristic>" + CRLF);
        sb.append("   </characteristic>" + CRLF);
        sb.append("   <characteristic type='BrowserFavorite'>" + CRLF);
        sb.append("    <characteristic type='Open Xchange'>" + CRLF);
        sb.append("        <parm name='URL' value='http://www.open-xchange.com'/>" + CRLF);
        sb.append("    </characteristic>" + CRLF);
        sb.append("</characteristic>  " + CRLF);
        sb.append("</wap-provisioningdoc>" + CRLF);
        return sb.toString();
    }

    private static void writeMobileConfigWinMob(final OutputStream out, final String host, final String username, final String domain) throws IOException, ConfigurationException {
        CabUtil.writeCabFile(new DataOutputStream(new BufferedOutputStream(out)), write(host, username, domain));
    }

    /**
     * Splits the given login into a username and a domain part
     * @param username
     * @return An array. Index 0 is the username. Index 1 is the domain
     * @throws ConfigurationException
     */
    protected static String[] splitUsernameAndDomain(String username) throws ConfigurationException {
        final String domain_user = MobileConfigProperties.getProperty(MobileConfigServiceRegistry.getServiceRegistry().getService(ConfigurationService.class), MobileConfigProperties.Property.DomainUser);
        final String separator = domain_user.replaceAll("\\$USER|\\$DOMAIN", "");
        final String[] split = username.split(Pattern.quote(separator));
        if (split.length != 2) {
            throw new ConfigurationException("Splitting of login returned wrong length. Array is " + Arrays.toString(split));
        }
        if (domain_user.indexOf("$USER") < domain_user.indexOf("$DOMAIN")) {
            return split;
        } else {
            // change position in array...
            return new String[]{split[1], split[0]};
        }
    }

//    private void doLogout(final Session session) {
//        try {
//            LoginPerformer.getInstance().doLogout(session.getSessionID());
//        } catch (LoginException e) {
//            LOG.error(e.getMessage(), e);
//        }
//    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final String iphoneRegEx;
        final String winMobRegEx;
        try {
            iphoneRegEx = MobileConfigProperties.getProperty(MobileConfigServiceRegistry.getServiceRegistry().getService(ConfigurationService.class), MobileConfigProperties.Property.iPhoneRegex);
            winMobRegEx = MobileConfigProperties.getProperty(MobileConfigServiceRegistry.getServiceRegistry().getService(ConfigurationService.class), MobileConfigProperties.Property.WinMobRegex);
        } catch (final ConfigurationException e) {
            LOG.error("A configuration exception occurred, which should not happen: " + e.getMessage(), e);
            printError(resp, "An internal error occurred, please try again later...");
            return;
        }
        
        final Device device = detectDevice(req);
        final String login = req.getParameter("login");
        if (null == device) {
            if (null == login) {
                printError(resp, "Parameter login is missing");
                return;
            }
            String mailpart = "";
            final String mail = req.getParameter("mail");
            if (null != mail) {
                mailpart = "&mail=" + mail;
            }

            final String header = req.getHeader("user-agent");
            if (null != header) {
                if (header.matches(iphoneRegEx)) {
                    // iPhone part
                    resp.sendRedirect(Activator.ALIAS + "/eas.mobileconfig?login=" + req.getParameter("login") + mailpart);
                    return;
                } else if (header.matches(winMobRegEx)) {
                    // WinMob part
                    resp.sendRedirect(Activator.ALIAS + "/ms.cab?login=" + req.getParameter("login") + mailpart);
                    return;
                } else {
                    printError(resp, "No supported device found from header");
                    return;
                }
            }
        } else {
            try {
                generateConfig(req, resp, login, device);
            } catch (final ConfigurationException e) {
                LOG.error("A configuration exception occurred, which should not happen: " + e.getMessage(), e);
                printError(resp, "An internal error occurred, please try again later...");
                return;
            }
        }
//        final Session session = getSessionObject(req);
//        Tools.disableCaching(resp);
//        Tools.deleteCookies(req, resp);
//        if (!req.isSecure()) {
//            final PrintWriter writer = writer;
//            writer.println("This page can only be accessed over a secure connection");
//            writer.close();
//            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
//            if (null != session) {
//                doLogout(session);
//            }
//        } else {
//            if (null == session) {
//                final PrintWriter writer = writer;
//                writer.println("No session found");
//                writer.close();
//            } else {
//                final User user = UserStorage.getStorageUser(session.getUserId(), session.getContextId());
//                if (null != user) {
//                }
//                doLogout(session);
//            }
//        }
    }

    private Device detectDevice(HttpServletRequest req) {
        final String pathInfo = req.getPathInfo();
        if ("/eas.mobileconfig".equals(pathInfo)) {
            return Device.iPhone;
        } else if ("/ms.cab".equals(pathInfo)) {
            return Device.winMob;
        } else {
            return null;
        }
    }

    private void generateConfig(HttpServletRequest req, HttpServletResponse resp, final String login, final Device device) throws UnknownHostException, IOException, ConfigurationException {
        String mail = login;
        final String parameter = req.getParameter("mail");
        if (null != parameter) {
            mail = parameter;
        }
        final String[] usernameAndDomain = splitUsernameAndDomain(login);
        if (Device.iPhone.equals(device)) {
            final PrintWriter writer = getWriterFromOutputStream(resp.getOutputStream());
            writeMobileConfig(writer, mail, getHostname(req), "OX EAS", usernameAndDomain[0], usernameAndDomain[1]);
            writer.close();
        } else if (Device.winMob.equals(device)) {
            final ServletOutputStream outputStream = resp.getOutputStream();
            writeMobileConfigWinMob(outputStream, getHostname(req), usernameAndDomain[0], usernameAndDomain[1]);
            outputStream.close();
        }
    }

    private String getHostname(final HttpServletRequest req) throws UnknownHostException {
        //final HostnameService service = MobileConfigServiceRegistry.getServiceRegistry().getService(HostnameService.class);
        final String canonicalHostName = req.getServerName();
        //final int userId = session.getUserId();
        //final int contextId = session.getContextId()
        //return ((null != service) && (null != service.getHostname(userId, contextId))) ? service.getHostname(userId, contextId) : canonicalHostName;
        return canonicalHostName;
    }

    private PrintWriter getWriterFromOutputStream(ServletOutputStream outputStream) {
        try {
            return new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(outputStream), Charset.forName("UTF-8")));
        } catch (final IllegalCharsetNameException e) {
            LOG.error(e.getMessage(), e);
            throw e;
        } catch (final UnsupportedCharsetException e) {
            LOG.error(e.getMessage(), e);
            throw e;
        }
    }

    private void printError(final HttpServletResponse resp, final String string) throws IOException {
        resp.setContentType("text/html");
        final PrintWriter writer = getWriterFromOutputStream(resp.getOutputStream());
        writer.println("<html><head>");
        writer.println("<meta name=\"viewport\" content=\"width=320\" />");
        writer.println("<meta name=\"mobileoptimized\" content=\"0\" />");
        writer.println("<title>Error</title>");
        writer.println("<style type=\"text/css\">");
        writer.println("table { height: 100%; width:100% }");
        writer.println("td { text-align:center; vertical-align:middle; }");
        writer.println("</style>");
        writer.println("</head>");
        writer.println("<body>");
        writer.println("<table>");
        writer.println("<tr>");
        writer.println("<td><h1>" + string + "</h1></td>");
        writer.println("</tr>");
        writer.println("</table>");
        writer.println("</body></html>");
        writer.close();
    }

    private void writeMobileConfig(final PrintWriter printWriter, final String email, final String host, final String displayname, final String username, String domain) {
        try {
            printWriter.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            printWriter.println("<!DOCTYPE plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">");
            printWriter.println("<plist version=\"1.0\">");
            printWriter.println("<dict>");
            printWriter.println("   <key>PayloadContent</key>");
            printWriter.println("   <array>");
            printWriter.println("       <dict>");
            printWriter.println("           <key>EmailAddress</key>");
            printWriter.println("           <string>" + email + "</string>");
            printWriter.println("           <key>Host</key>");
            printWriter.println("           <string>" + host + "</string>");
//            printWriter.println("           <key>Password</key>");
//            printWriter.println("           <string>" + password + "</string>");
            //printWriter.println("           <key>PayloadDescription</key>");
            //printWriter.println("           <string>Geräte für die Verwendung mit Microsoft Exchange-ActiveSync-Diensten konfigurieren.</string>");
            printWriter.println("           <key>PayloadDisplayName</key>");
            printWriter.println("           <string>" + displayname + "</string>");
            printWriter.println("           <key>PayloadIdentifier</key>");
            printWriter.println("           <string>eas.profile.eas</string>");
            printWriter.println("           <key>PayloadOrganization</key>");
            printWriter.println("           <string>OX</string>");
            printWriter.println("           <key>PayloadType</key>");
            printWriter.println("           <string>com.apple.eas.account</string>");
            printWriter.println("           <key>PayloadUUID</key>");
            printWriter.println("           <string>077E36E4-9579-47F0-BACF-62A55FEA11E8</string>");
            printWriter.println("           <key>PayloadVersion</key>");
            printWriter.println("           <integer>1</integer>");
            printWriter.println("           <key>SSL</key>");
            printWriter.println("           <true/>");
            printWriter.println("           <key>UserName</key>");
            printWriter.println("           <string>" + username + "</string>");
            printWriter.println("       </dict>");
            printWriter.println("   </array>");
            printWriter.println("   <key>PayloadDescription</key>");
            printWriter.println("   <string>Profilbeschreibung</string>");
            printWriter.println("   <key>PayloadDisplayName</key>");
            printWriter.println("   <string>EAS</string>");
            printWriter.println("   <key>PayloadIdentifier</key>");
            printWriter.println("   <string>eas.profile</string>");
            printWriter.println("   <key>PayloadOrganization</key>");
            printWriter.println("   <string>OX</string>");
            printWriter.println("   <key>PayloadRemovalDisallowed</key>");
            printWriter.println("   <false/>");
            printWriter.println("   <key>PayloadType</key>");
            printWriter.println("   <string>Configuration</string>");
            printWriter.println("   <key>PayloadUUID</key>");
            printWriter.println("   <string>E4DB69E7-EC59-418E-A883-14944D5BC48F</string>");
            printWriter.println("   <key>PayloadVersion</key>");
            printWriter.println("   <integer>1</integer>");
            printWriter.println("</dict>");
            printWriter.println("</plist>");
        } finally {
            printWriter.close();
        }
    }
}
