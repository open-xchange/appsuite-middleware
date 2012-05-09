package com.openexchange.mobile.configuration.generator;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.mobile.configuration.generator.configuration.ConfigurationException;
import com.openexchange.mobile.configuration.generator.configuration.MobileConfigProperties;
import com.openexchange.mobile.configuration.generator.configuration.Property;
import com.openexchange.mobile.configuration.generator.osgi.Activator;
import com.openexchange.mobile.configuration.generator.services.MobileConfigServiceRegistry;
import com.openexchange.templating.OXTemplate;
import com.openexchange.templating.TemplateService;
import com.openexchange.tools.servlet.http.Tools;


public class MobileConfigServlet extends HttpServlet {


    private static final String PARAMETER_MAIL = "m";

    private static final String PARAMETER_LOGIN = "l";

    private enum Device {
        iPhone,
        winMob;
    }

    private static enum ErrorMessage {
        MSG_INTERNAL_ERROR("Ein interner Fehler ist aufgetreten, bitte versuchen Sie es später noch einmal.", "An internal error occurred. Please try again later."),
        MSG_NO_SUPPORTED_DEVICE_FOUND("Ihr Gerät wird nicht unterstützt", "Your device is not supported."),
        MSG_PARAMETER_LOGIN_IS_MISSING("Der Parameter \"l\" fehlt", "The \"l\" parameter is missing"),
        MSG_UNSECURE_ACCESS("Unsicherer Zugriff mit http ist nicht erlaubt. Bitte https benutzen.", "Unsecured http access is not allowed. Use https instead."),
        MSG_INVALID_ERROR_PARAMETER("Der übergebene \"error\"-Parameter ist ungültig.", "Invalid \"error\" parameter.");

        private final String english;

        private final String german;

        private static Map<Integer, ErrorMessage> members = new ConcurrentHashMap<Integer, ErrorMessage>();

        private ErrorMessage(final String german, final String english) {
            this.german = german;
            this.english = english;
        }


        public String getEnglish() {
            return english;
        }


        public String getGerman() {
            return german;
        }

        static {
            for (final ErrorMessage errmsg : ErrorMessage.values()) {
                members.put(errmsg.ordinal(), errmsg);
            }
        }

        public static ErrorMessage getErrorMessageByNumber(final int value) {
            return members.get(Integer.valueOf(value));
        }

    }

    private static final transient Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(MobileConfigServlet.class));

    /**
     *
     */
    private static final long serialVersionUID = 7913468326542861986L;

    public static String write(final String email, final String host, final String username, final String domain) throws OXException {
            final TemplateService service = MobileConfigServiceRegistry.getServiceRegistry().getService(TemplateService.class);
            final OXTemplate loadTemplate = service.loadTemplate("winMobileTemplate.tmpl");
            final StringWriter writer = new StringWriter();
            loadTemplate.process(generateHashMap(email, host, username, domain), writer);
            return writer.toString();
    }

    /**
     * Splits the given login into a username and a domain part
     * @param username
     * @return An array. Index 0 is the username. Index 1 is the domain
     * @throws ConfigurationException
     */
    protected static String[] splitUsernameAndDomain(final String username) throws ConfigurationException {
        final String domain_user = MobileConfigProperties.getProperty(MobileConfigServiceRegistry.getServiceRegistry(), Property.DomainUser);
        final String separator = domain_user.replaceAll("\\$USER|\\$DOMAIN", "");
        final String[] split = username.split(Pattern.quote(separator));
        if (split.length > 2) {
            throw new ConfigurationException("Splitting of login returned wrong length. Array is " + Arrays.toString(split));
        }

        if (split.length == 1) {
            return new String[]{split[0], "defaultcontext"};
        } else {
            if (domain_user.indexOf("$USER") < domain_user.indexOf("$DOMAIN")) {
                return split;
            } else {
                // change position in array...
                return new String[]{split[1], split[0]};
            }
        }
    }

    private static HashMap<String, String> generateHashMap(final String email, final String host, final String username, final String domain) {
        final HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("email", email);
        hashMap.put("host", host);
        hashMap.put("username", username);
        hashMap.put("domain", domain);
        return hashMap;
    }

    private static void writeMobileConfigWinMob(final OutputStream out, final String email, final String host, final String username, final String domain) throws IOException, OXException {
        CabUtil.writeCabFile(new DataOutputStream(new BufferedOutputStream(out)), write(email, host, username, domain));
    }

    @Override
    public void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        resp.setCharacterEncoding("UTF-8");
        Tools.disableCaching(resp);
        final ConfigurationService service = MobileConfigServiceRegistry.getServiceRegistry().getService(ConfigurationService.class);
        if (null == service) {
            LOG.error("A configuration exception occurred, which should not happen: No configuration service found");
            printError(req, resp, ErrorMessage.MSG_INTERNAL_ERROR);
            return;
        }

        final String parameter = req.getParameter("error");
        if (null != parameter && parameter.length() != 0) {
            // Error output
            errorOutput(req, resp, parameter);
            return;
        }
        final String iphoneRegEx;
        final String winMobRegEx;
        try {
            iphoneRegEx = MobileConfigProperties.getProperty(service, Property.iPhoneRegex);
            winMobRegEx = MobileConfigProperties.getProperty(service, Property.WinMobRegex);
            final Boolean secureConnect = MobileConfigProperties.getProperty(service, Property.OnlySecureConnect);
            if (secureConnect) {
                if (!req.isSecure()) {
                    printError(req, resp, ErrorMessage.MSG_UNSECURE_ACCESS);
                    return;
                }
            }
        } catch (final ConfigurationException e) {
            LOG.error("A configuration exception occurred, which should not happen: " + e.getMessage(), e);
            printError(req, resp, ErrorMessage.MSG_INTERNAL_ERROR);
            return;
        }

        final Device device = detectDevice(req);
        final String login = req.getParameter(PARAMETER_LOGIN);
        if (null == device) {
            if (null == login) {
                printError(req, resp, ErrorMessage.MSG_PARAMETER_LOGIN_IS_MISSING);
                return;
            }
            String mailpart = "";
            final String mail = req.getParameter(PARAMETER_MAIL);
            if (null != mail) {
                mailpart = "&m=" + URLEncoder.encode(mail, "UTF-8");
            }

            final String header = req.getHeader("user-agent");
            if (null != header) {
                if (header.matches(iphoneRegEx)) {
                    // iPhone part
                    resp.sendRedirect(Activator.ALIAS + "/eas.mobileconfig?l=" + URLEncoder.encode(login,"UTF-8") + mailpart);
                    return;
                } else if (header.matches(winMobRegEx)) {
                    // WinMob part
                    resp.sendRedirect(Activator.ALIAS + "/ms.cab?l=" + URLEncoder.encode(login,"UTF-8") + mailpart);
                    return;
                } else {
                    printError(req, resp, ErrorMessage.MSG_NO_SUPPORTED_DEVICE_FOUND);
                    LOG.info("Unsupported device header: \"" + header + "\"");
                    return;
                }
            }
        } else {
            try {
                generateConfig(req, resp, login, device);
            } catch (final OXException e) {
                LOG.error("A template exception occurred, which should not happen: " + e.getMessage(), e);
                printError(req, resp, ErrorMessage.MSG_INTERNAL_ERROR);
                return;
            } catch (final IOException e) {
                LOG.error("A template exception occurred, which should not happen: " + e.getMessage(), e);
                printError(req, resp, ErrorMessage.MSG_INTERNAL_ERROR);
                return;
            }
        }
    }

    private Device detectDevice(final HttpServletRequest req) {
        final String pathInfo = req.getPathInfo();
        if ("/eas.mobileconfig".equals(pathInfo)) {
            return Device.iPhone;
        } else if ("/ms.cab".equals(pathInfo)) {
            return Device.winMob;
        } else {
            return null;
        }
    }

    /**
     * Reads the language from the header, returns either ENGLISH or GERMAN. No other value can be returned
     * @param req
     * @return
     */
    private Locale detectLanguage(final HttpServletRequest req) {
        final String parameter = req.getHeader("Accept-Language");
        if (null == parameter) {
            return Locale.ENGLISH;
        } else {
            if (parameter.startsWith("de")) {
                return Locale.GERMAN;
            } else {
                return Locale.ENGLISH;
            }
        }
    }

    private void errorOutput(final HttpServletRequest req, final HttpServletResponse resp, final String string) {
        final Locale locale = detectLanguage(req);
        ErrorMessage msg = null;
        try {
            msg = ErrorMessage.getErrorMessageByNumber(Integer.parseInt(string));
        } catch (final NumberFormatException e1) {
            msg = ErrorMessage.MSG_INVALID_ERROR_PARAMETER;
        }
        resp.setContentType("text/html; charset=UTF-8");
        PrintWriter writer;
        try {
            writer = getWriterFromOutputStream(resp.getOutputStream());
        } catch (final IOException e) {
            LOG.error("Unable to get output stream to write error message: " + e.getMessage(), e);
            return;
        }
        if (ErrorMessage.MSG_PARAMETER_LOGIN_IS_MISSING.equals(msg)) {
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
            writer.println("<form action=\"" + Activator.ALIAS + "\" method=\"get\" enctype=\"application/x-www-form-urlencoded; charset=UTF-8\" accept-charset=\"UTF-8\">");
            writer.println("<table>");
            writer.println("<tr><td>");
            if (Locale.ENGLISH.equals(locale)) {
                writer.println("<h1>" + "Enter your username for auto-configuring your device." + "</h1>");
            } else if (Locale.GERMAN.equals(locale)) {
                writer.println("<h1>" + "Geben Sie für die automatische Konfiguration Ihres Gerätes Ihren Benutzernamen ein." + "</h1>");
            }
            writer.println("<input name=\"" + PARAMETER_LOGIN +"\" type=\"text\" size=\"30\" maxlength=\"100\">");
            writer.println("<input type=\"submit\" value=\" Absenden \">");
            writer.println("</td>");
            writer.println("</tr>");
            writer.println("</table>");
            writer.println("</form>");
            writer.println("</body></html>");
            writer.close();
        } else {
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
            if (Locale.ENGLISH.equals(locale)) {
                writer.println("<td><h1>" + String.valueOf(msg.getEnglish()) + "</h1></td>");
            } else if (Locale.GERMAN.equals(locale)) {
                writer.println("<td><h1>" + String.valueOf(msg.getGerman()) + "</h1></td>");
            }
            writer.println("</tr>");
            writer.println("</table>");
            writer.println("</body></html>");
            writer.close();
        }

    }

    private void generateConfig(final HttpServletRequest req, final HttpServletResponse resp, final String login, final Device device) throws IOException, OXException {
        String mail = login;
        final String parameter = req.getParameter(PARAMETER_MAIL);
        if (null != parameter) {
            mail = parameter;
        }
        final String[] usernameAndDomain;
        try {
            usernameAndDomain = splitUsernameAndDomain(login);
        } catch (final ConfigurationException e) {
            throw new OXException(e);
        }
        if (Device.iPhone.equals(device)) {
            resp.setContentType("application/x-apple-aspen-config");
            final ServletOutputStream outputStream = resp.getOutputStream();
            final PrintWriter writer = getWriterFromOutputStream(outputStream);
            writeMobileConfig(writer, outputStream, mail, getHostname(req), usernameAndDomain[0], usernameAndDomain[1]);
            writer.close();
        } else if (Device.winMob.equals(device)) {
            final ServletOutputStream outputStream = resp.getOutputStream();
            writeMobileConfigWinMob(outputStream, mail, getHostname(req), usernameAndDomain[0], usernameAndDomain[1]);
            outputStream.close();
        }
    }

    private String getHostname(final HttpServletRequest req) {
        final String canonicalHostName = req.getServerName();
        return canonicalHostName;
    }

    private PrintWriter getWriterFromOutputStream(final ServletOutputStream outputStream) {
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

    private void printError(final HttpServletRequest req, final HttpServletResponse resp, final ErrorMessage string) throws IOException {
        resp.sendRedirect(Activator.ALIAS + "?error=" + URLEncoder.encode(String.valueOf(string.ordinal()),"UTF-8"));
    }

    private void writeMobileConfig(final PrintWriter printWriter, final OutputStream outStream, final String email, final String host, final String username, final String domain) throws IOException, OXException {
        try {
            final TemplateService service = MobileConfigServiceRegistry.getServiceRegistry().getService(TemplateService.class);
            final OXTemplate loadTemplate = service.loadTemplate("iPhoneTemplate.tmpl");
            final Boolean property = MobileConfigProperties.getProperty(MobileConfigServiceRegistry.getServiceRegistry().getService(ConfigurationService.class), Property.SignConfig);
            if (property) {
                final MobileConfigSigner writer = new MobileConfigSigner(outStream);
                try {
                    loadTemplate.process(generateHashMap(email, host, username, domain), writer);
                } finally {
                    writer.close();
                }
            } else {
                loadTemplate.process(generateHashMap(email, host, username, domain), printWriter);
            }
        } catch (final ConfigurationException e) {
            throw new OXException(e);
        } finally {
            printWriter.close();
        }
    }

}
