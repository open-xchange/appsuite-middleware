package com.openexchange.config.internal;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.config.ConfigurationServices;

@SuppressWarnings("unchecked")
public class ConfigurationImplTest {
    
    String[] arr = {"./test/testfiles/"};
    
    private ConfigurationImpl configurationImpl = new ConfigurationImpl(arr, null);
    private static Map<String, Object> asConfig;

    
    @Before
    public void setUp() throws IOException {
        asConfig = (Map<String, Object>) ConfigurationServices.loadYamlFrom(getClass().getResourceAsStream("/testfiles/as-config.yml"));
    }
    
    @Test
    public void testLooksApplicable_host_not_matching() {
        Map<String, Object> host1Config = (Map<String, Object>) asConfig.get("host1.mycloud.net");
        assertFalse(configurationImpl.looksApplicable(host1Config, "host4.mycloud.net"));
    }

    @Test
    public void testLooksApplicable_host_matching() {
        Map<String, Object> host1Config = (Map<String, Object>) asConfig.get("host1.mycloud.net");
        assertTrue(configurationImpl.looksApplicable(host1Config, "host1.mycloud.net"));
    }

    @Test
    public void testLooksApplicable_hostregex_not_matching() {
        Map<String, Object> host1Config = (Map<String, Object>) asConfig.get("host*.mycloud.net");
        assertFalse(configurationImpl.looksApplicable(host1Config, "performance.mycloud.net"));
    }

    @Test
    public void testLooksApplicable_hostregex_matching() {
        Map<String, Object> host1Config = (Map<String, Object>) asConfig.get("host*.mycloud.net");
        assertTrue(configurationImpl.looksApplicable(host1Config, "host1.mycloud.net"));
    }

    @Test
    public void testLooksApplicable_NoConfig() {
        Map<String, Object> host1Config = (Map<String, Object>) asConfig.get("host*.mycloud.net");
        assertFalse(configurationImpl.looksApplicable(host1Config, null));
    }

    @Test
    public void testLooksApplicable_NoData() {
        Map<String, Object> host1Config = (Map<String, Object>) asConfig.get("host*.mycloud.net");
        assertFalse(configurationImpl.looksApplicable(host1Config, null));
    }
}
