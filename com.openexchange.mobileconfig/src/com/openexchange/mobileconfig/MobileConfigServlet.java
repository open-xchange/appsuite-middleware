package com.openexchange.mobileconfig;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.login.Interface;
import com.openexchange.mobileconfig.services.MobileConfigServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.webdav.OXServlet;


public class MobileConfigServlet extends OXServlet {

    /**
     * 
     */
    private static final long serialVersionUID = 7913468326542861986L;

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final Session session = getSession(req);
        final PrintWriter writer = resp.getWriter();
        final User user = UserStorage.getStorageUser(session.getUserId(), session.getContextId());
        if (null != user) {
            writeMobileConfig(writer, user.getMail(), getHostname(session.getUserId(), session.getContextId()), "OX EAS", session.getLogin());
        }
    }

    private String getHostname(final int userId, final int contextId) throws UnknownHostException {
        final HostnameService service = MobileConfigServiceRegistry.getServiceRegistry().getService(HostnameService.class);
        final String canonicalHostName = InetAddress.getLocalHost().getCanonicalHostName();
        return ((null != service) && (null != service.getHostname(userId, contextId))) ? service.getHostname(userId, contextId) : canonicalHostName;
    }

    @Override
    protected void decrementRequests() {
        // Not used here
    }

    @Override
    protected void incrementRequests() {
        // Not used here
    }

    @Override
    protected Interface getInterface() {
        return Interface.HTTP_JSON;
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

}
