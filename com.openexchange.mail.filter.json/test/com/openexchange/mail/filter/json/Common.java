
package com.openexchange.mail.filter.json;

import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.config.SimConfigurationService;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.config.lean.SimLeanConfigurationService;
import com.openexchange.mailfilter.json.osgi.Services;
import com.openexchange.mailfilter.properties.MailFilterProperty;
import com.openexchange.server.ServiceLookup;

public class Common {

    public static SimLeanConfigurationService simMailFilterConfigurationService;

    public static void prepare(String passwordSource, String masterPassword) {
        SimConfigurationService simConfigurationService = new SimConfigurationService() {

            @Override
            public Properties getFile(String fileName) {
                final Properties properties = new Properties();
                properties.putAll(stringProperties);
                return properties;
            }
        };

        Common.simMailFilterConfigurationService = new SimLeanConfigurationService(simConfigurationService);
        simConfigurationService.stringProperties.put(MailFilterProperty.credentialSource.getFQPropertyName(), "imapLogin");
        simConfigurationService.stringProperties.put(MailFilterProperty.loginType.getFQPropertyName(), "user");
        simConfigurationService.stringProperties.put(MailFilterProperty.server.getFQPropertyName(), "localhost");
        simConfigurationService.stringProperties.put(MailFilterProperty.port.getFQPropertyName(), "2000");
        simConfigurationService.stringProperties.put(MailFilterProperty.scriptName.getFQPropertyName(), "Open-Xchange");
        simConfigurationService.stringProperties.put(MailFilterProperty.authenticationEncoding.getFQPropertyName(), "UTF-8");
        simConfigurationService.stringProperties.put(MailFilterProperty.nonRFCCompliantTLSRegex.getFQPropertyName(), "^Cyrus.*v([0-1]\\.[0-9].*|2\\.[0-2].*|2\\.3\\.[0-9]|2\\.3\\.[0-9][^0-9].*)$");
        simConfigurationService.stringProperties.put(MailFilterProperty.tls.getFQPropertyName(), "false");
        simConfigurationService.stringProperties.put(MailFilterProperty.vacationDomains.getFQPropertyName(), "");
        simConfigurationService.stringProperties.put(MailFilterProperty.connectionTimeout.getFQPropertyName(), "30000");
        if (null != passwordSource) {
            simConfigurationService.stringProperties.put(MailFilterProperty.passwordSource.getFQPropertyName(), passwordSource);
        }
        if (null != masterPassword) {
            simConfigurationService.stringProperties.put(MailFilterProperty.masterPassword.getFQPropertyName(), masterPassword);
        }

        final ConcurrentMap<Class<?>, Object> services = new ConcurrentHashMap<Class<?>, Object>(2, 0.9f, 1);
        services.put(LeanConfigurationService.class, simMailFilterConfigurationService);
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
