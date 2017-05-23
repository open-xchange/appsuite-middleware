package com.openexchange.mobile.configuration.generator.test;

import org.junit.Assert;
import org.junit.Test;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.SimConfigurationService;
import com.openexchange.mobile.configuration.generator.MobileConfigServlet;
import com.openexchange.mobile.configuration.generator.configuration.ConfigurationException;
import com.openexchange.mobile.configuration.generator.configuration.Property;
import com.openexchange.mobile.configuration.generator.osgi.Services;
import com.openexchange.server.SimpleServiceLookup;


@SuppressWarnings("serial")
public class MobileConfigServletTest extends MobileConfigServlet  {

     @Test
     public void testSplitUsernameAndDomain() throws ConfigurationException {
        final SimConfigurationService service = new SimConfigurationService();
        service.stringProperties.put(Property.DomainUser.getName(), "$USER@$DOMAIN");

        SimpleServiceLookup serviceLookup = new SimpleServiceLookup();
        serviceLookup.add(ConfigurationService.class, service);
        Services.setServiceLookup(serviceLookup);

        final String[] splitUsernameAndDomain = splitUsernameAndDomain("seppel@ox.de");
        Assert.assertEquals("Value at index 0 wrong", "seppel", splitUsernameAndDomain[0]);
        Assert.assertEquals("Value at index 1 wrong", "ox.de", splitUsernameAndDomain[1]);
    }

     @Test
     public void testSplitUsernameAndDomain2() throws ConfigurationException {
        final SimConfigurationService service = new SimConfigurationService();
        service.stringProperties.put(Property.DomainUser.getName(), "$DOMAIN@$USER");

        SimpleServiceLookup serviceLookup = new SimpleServiceLookup();
        serviceLookup.add(ConfigurationService.class, service);
        Services.setServiceLookup(serviceLookup);

        final String[] splitUsernameAndDomain = splitUsernameAndDomain("seppel@ox.de");
        Assert.assertEquals("Value at index 0 wrong", "ox.de", splitUsernameAndDomain[0]);
        Assert.assertEquals("Value at index 1 wrong", "seppel", splitUsernameAndDomain[1]);
    }

     @Test
     public void testSplitUsernameAndDomain3() throws ConfigurationException {
        final SimConfigurationService service = new SimConfigurationService();
        service.stringProperties.put(Property.DomainUser.getName(), "$DOMAIN|$USER");

        SimpleServiceLookup serviceLookup = new SimpleServiceLookup();
        serviceLookup.add(ConfigurationService.class, service);
        Services.setServiceLookup(serviceLookup);

        final String[] splitUsernameAndDomain = splitUsernameAndDomain("seppel@ox.de");
        Assert.assertEquals("Value at index 0 wrong", "seppel@ox.de", splitUsernameAndDomain[0]);
        Assert.assertEquals("Value at index 1 wrong", "defaultcontext", splitUsernameAndDomain[1]);
    }

     @Test
     public void testSplitUsernameAndDomain4() throws ConfigurationException {
        final SimConfigurationService service = new SimConfigurationService();
        service.stringProperties.put(Property.DomainUser.getName(), "$DOMAIN|$USER");

        SimpleServiceLookup serviceLookup = new SimpleServiceLookup();
        serviceLookup.add(ConfigurationService.class, service);
        Services.setServiceLookup(serviceLookup);

        final String[] splitUsernameAndDomain = splitUsernameAndDomain("seppel|ox.de");
        Assert.assertEquals("Value at index 0 wrong", "ox.de", splitUsernameAndDomain[0]);
        Assert.assertEquals("Value at index 1 wrong", "seppel", splitUsernameAndDomain[1]);
    }

     @Test
     public void testSplitUsernameAndDomain5() throws ConfigurationException {
        final SimConfigurationService service = new SimConfigurationService();
        service.stringProperties.put(Property.DomainUser.getName(), "$USER@$DOMAIN");

        SimpleServiceLookup serviceLookup = new SimpleServiceLookup();
        serviceLookup.add(ConfigurationService.class, service);
        Services.setServiceLookup(serviceLookup);

        final String[] splitUsernameAndDomain = splitUsernameAndDomain("seppel");
        Assert.assertEquals("Value at index 0 wrong", "seppel", splitUsernameAndDomain[0]);
        Assert.assertEquals("Value at index 1 wrong", "defaultcontext", splitUsernameAndDomain[1]);
    }

}
