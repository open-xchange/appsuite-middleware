package com.openexchange.mail.filter;

import java.util.Properties;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.SimConfigurationService;
import com.openexchange.mailfilter.internal.MailFilterProperties;
import com.openexchange.mailfilter.services.MailFilterServletServiceRegistry;


public class Common {

    public static SimConfigurationService simConfigurationService;

    public static void prepare(String passwordSource, String masterPassword) {
        simConfigurationService = new SimConfigurationService() {
            @Override
            public Properties getFile(String fileName) {
                final Properties properties = new Properties();
                properties.putAll(stringProperties);
                return properties;
            }
        };
        simConfigurationService.stringProperties.put(MailFilterProperties.Values.SIEVE_CREDSRC.property, "imapLogin");
        simConfigurationService.stringProperties.put(MailFilterProperties.Values.SIEVE_LOGIN_TYPE.property, "user");
        simConfigurationService.stringProperties.put(MailFilterProperties.Values.SIEVE_SERVER.property, "localhost");
        simConfigurationService.stringProperties.put(MailFilterProperties.Values.SIEVE_PORT.property, "2000");
        simConfigurationService.stringProperties.put(MailFilterProperties.Values.SCRIPT_NAME.property, "Open-Xchange");
        simConfigurationService.stringProperties.put(MailFilterProperties.Values.SIEVE_AUTH_ENC.property, "UTF-8");
        simConfigurationService.stringProperties.put(MailFilterProperties.Values.NON_RFC_COMPLIANT_TLS_REGEX.property, "^Cyrus.*v([0-1]\\.[0-9].*|2\\.[0-2].*|2\\.3\\.[0-9]|2\\.3\\.[0-9][^0-9].*)$");
        simConfigurationService.stringProperties.put(MailFilterProperties.Values.TLS.property, "false");
        simConfigurationService.stringProperties.put(MailFilterProperties.Values.VACATION_DOMAINS.property, "");
        simConfigurationService.stringProperties.put(MailFilterProperties.Values.SIEVE_CONNECTION_TIMEOUT.property, "30000");
        if (null != passwordSource) {
            simConfigurationService.stringProperties.put(MailFilterProperties.Values.SIEVE_PASSWORDSRC.property, passwordSource);
        }
        if (null != masterPassword) {
            simConfigurationService.stringProperties.put(MailFilterProperties.Values.SIEVE_MASTERPASSWORD.property, masterPassword);
        }
        MailFilterServletServiceRegistry.getServiceRegistry().addService(ConfigurationService.class, simConfigurationService);
    }

}
