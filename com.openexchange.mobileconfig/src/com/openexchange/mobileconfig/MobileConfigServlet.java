package com.openexchange.mobileconfig;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.UnknownHostException;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class MobileConfigServlet extends HttpServlet {

    private static final transient Log LOG = LogFactory.getLog(MobileConfigServlet.class);
    
    /**
     * 
     */
    private static final long serialVersionUID = 7913468326542861986L;

    private static final String CRLF = "\r\n";
    
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final String login = req.getParameter("login");
        if (null == login) {
            final PrintWriter writer = resp.getWriter();
            writer.println("Parameter login is missing");
            writer.close();
        }
        generateConfig(req, resp, login);
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

    private void generateConfig(HttpServletRequest req, HttpServletResponse resp, final String login) throws UnknownHostException, IOException {
        final String pathInfo = req.getPathInfo();
        String mail = login;
        final String parameter = req.getParameter("mail");
        if (null != parameter) {
            mail = parameter;
        }
        if ("/eas.mobileconfig".equals(pathInfo)) {
            final PrintWriter writer = resp.getWriter();
            writeMobileConfig(writer, mail, getHostname(req), "OX EAS", login);
            writer.close();
        } else if ("/ms.cab".equals(pathInfo)) {
            final ServletOutputStream outputStream = resp.getOutputStream();
            writeMobileConfigWinMob(outputStream, mail, getHostname(req), "OX EAS", login);
            outputStream.close();
        } else {
            final PrintWriter writer = resp.getWriter();
            writer.println("Device parameter not known");
            writer.close();
        }
    }

//    private void doLogout(final Session session) {
//        try {
//            LoginPerformer.getInstance().doLogout(session.getSessionID());
//        } catch (LoginException e) {
//            LOG.error(e.getMessage(), e);
//        }
//    }

    private String getHostname(final HttpServletRequest req) throws UnknownHostException {
        //final HostnameService service = MobileConfigServiceRegistry.getServiceRegistry().getService(HostnameService.class);
        final String canonicalHostName = req.getServerName();
        //final int userId = session.getUserId();
        //final int contextId = session.getContextId()
        //return ((null != service) && (null != service.getHostname(userId, contextId))) ? service.getHostname(userId, contextId) : canonicalHostName;
        return canonicalHostName;
    }

    private void writeMobileConfig(final PrintWriter printWriter, final String email, final String host, final String displayname, final String username) {
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
            printWriter.println("           <key>PayloadDescription</key>");
            printWriter.println("           <string>Geräte für die Verwendung mit Microsoft Exchange-ActiveSync-Diensten konfigurieren.</string>");
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

    public static void writeMobileConfigWinMob(final OutputStream out, final String email, final String host, final String displayname, final String username) throws IOException {
        CabUtil.writeCabFile(new DataOutputStream(new BufferedOutputStream(out)), write(email, host, displayname, username));
    }

    public static String write(final String email, final String host, final String displayname, final String username) {
        final StringBuilder sb = new StringBuilder();
        sb.append("<wap-provisioningdoc>" + CRLF);
        sb.append("   <characteristic type=\"Sync\">" + CRLF);
        sb.append("        <characteristic type=\"Settings\">" + CRLF);
        sb.append("        <parm name=\"SyncWhenRoaming\" value=\"1\"/>" + CRLF);
        sb.append("        </characteristic>" + CRLF);
        sb.append("        <characteristic type=\"Connection\">" + CRLF);
        sb.append("            <parm name=\"Domain\" value=\"open-xchange.com\"/>" + CRLF);
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
}
