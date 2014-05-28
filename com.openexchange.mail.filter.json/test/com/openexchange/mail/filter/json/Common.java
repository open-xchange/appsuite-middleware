package com.openexchange.mail.filter.json;

import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.SimConfigurationService;
import com.openexchange.mailfilter.MailFilterProperties;
import com.openexchange.mailfilter.json.osgi.Services;
import com.openexchange.server.ServiceLookup;


public class Common {

    public static SimConfigurationService simConfigurationService;

    public static void prepare(String passwordSource, String masterPassword) {
        SimConfigurationService simConfigurationService = new SimConfigurationService() {
            @Override
            public Properties getFile(String fileName) {
                final Properties properties = new Properties();
                properties.putAll(stringProperties);
                return properties;
            }
        };
        Common.simConfigurationService = simConfigurationService;
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

        final ConcurrentMap<Class<?>, Object> services = new ConcurrentHashMap<Class<?>, Object>(2);
        services.put(ConfigurationService.class, simConfigurationService);
        Services.setServiceLookup(new ServiceLookup() {

            @Override
            public <S> S getService(Class<? extends S> clazz) {
                return (S) services.get(clazz);
            }

            @Override
            public <S> S getOptionalService(Class<? extends S> clazz) {
                return (S) services.get(clazz);
            }
        });
    }

}
