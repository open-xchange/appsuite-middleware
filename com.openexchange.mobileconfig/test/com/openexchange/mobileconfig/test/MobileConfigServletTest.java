package com.openexchange.mobileconfig.test;

import junit.framework.Assert;
import org.junit.Test;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.SimConfigurationService;
import com.openexchange.mobileconfig.MobileConfigServlet;
import com.openexchange.mobileconfig.configuration.ConfigurationException;
import com.openexchange.mobileconfig.services.MobileConfigServiceRegistry;


@SuppressWarnings("serial")
public class MobileConfigServletTest extends MobileConfigServlet  {

    @Test
    public void testSplitUsernameAndDomain() throws ConfigurationException {
        final SimConfigurationService service = new SimConfigurationService();
        service.stringProperties.put("com.openexchange.usm.eas.login_pattern.domain_user", "$USER@$DOMAIN");
        MobileConfigServiceRegistry.getServiceRegistry().addService(ConfigurationService.class, service);
        
        final String[] splitUsernameAndDomain = splitUsernameAndDomain("seppel@ox.de");
        Assert.assertEquals("Value at index 0 wrong", "seppel", splitUsernameAndDomain[0]);
        Assert.assertEquals("Value at index 1 wrong", "ox.de", splitUsernameAndDomain[1]);
    }

    @Test
    public void testSplitUsernameAndDomain2() throws ConfigurationException {
        final SimConfigurationService service = new SimConfigurationService();
        service.stringProperties.put("com.openexchange.usm.eas.login_pattern.domain_user", "$DOMAIN@$USER");
        MobileConfigServiceRegistry.getServiceRegistry().addService(ConfigurationService.class, service);
        
        final String[] splitUsernameAndDomain = splitUsernameAndDomain("seppel@ox.de");
        Assert.assertEquals("Value at index 0 wrong", "ox.de", splitUsernameAndDomain[0]);
        Assert.assertEquals("Value at index 1 wrong", "seppel", splitUsernameAndDomain[1]);
    }

    @Test
    public void testSplitUsernameAndDomain3() throws ConfigurationException {
        final SimConfigurationService service = new SimConfigurationService();
        service.stringProperties.put("com.openexchange.usm.eas.login_pattern.domain_user", "$DOMAIN|$USER");
        MobileConfigServiceRegistry.getServiceRegistry().addService(ConfigurationService.class, service);
        
        try {
            splitUsernameAndDomain("seppel@ox.de");
        } catch (final ConfigurationException e) {
            Assert.assertTrue(e.getMessage().startsWith("Splitting of login"));
            return;
        }
        Assert.fail("No exception occurred");
    }

    @Test
    public void testSplitUsernameAndDomain4() throws ConfigurationException {
        final SimConfigurationService service = new SimConfigurationService();
        service.stringProperties.put("com.openexchange.usm.eas.login_pattern.domain_user", "$DOMAIN|$USER");
        MobileConfigServiceRegistry.getServiceRegistry().addService(ConfigurationService.class, service);
        
        final String[] splitUsernameAndDomain = splitUsernameAndDomain("seppel|ox.de");
        Assert.assertEquals("Value at index 0 wrong", "ox.de", splitUsernameAndDomain[0]);
        Assert.assertEquals("Value at index 1 wrong", "seppel", splitUsernameAndDomain[1]);
    }
}
