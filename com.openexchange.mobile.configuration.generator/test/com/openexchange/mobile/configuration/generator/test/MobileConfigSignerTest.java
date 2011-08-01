package com.openexchange.mobile.configuration.generator.test;

import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.SimConfigurationService;
import com.openexchange.mobile.configuration.generator.MobileConfigSigner;
import com.openexchange.mobile.configuration.generator.configuration.ConfigurationException;
import com.openexchange.mobile.configuration.generator.configuration.Property;
import com.openexchange.mobile.configuration.generator.services.MobileConfigServiceRegistry;
import com.openexchange.threadpool.SimThreadPoolService;
import com.openexchange.threadpool.ThreadPoolService;


public class MobileConfigSignerTest {

    private static final String SIGNEDJAVA_MOBILECONFIG = "testdata/signedjava.mobileconfig";
    private static final String EAS_MOBILECONFIG = "testdata/eas.mobileconfig";

    @Before
    public void setUp(){
        final SimConfigurationService service = new SimConfigurationService();
        service.stringProperties.put(Property.OpensslBinary.getName(), "/usr/bin/openssl");
        service.stringProperties.put(Property.CertFile.getName(), "define");
        service.stringProperties.put(Property.KeyFile.getName(), "define");
        service.stringProperties.put(Property.OpensslTimeout.getName(), "2000");
        MobileConfigServiceRegistry.getServiceRegistry().addService(ConfigurationService.class, service);
        MobileConfigServiceRegistry.getServiceRegistry().addService(ThreadPoolService.class, new SimThreadPoolService());
    }

    @Test
    public void signerTest() throws IOException, InterruptedException, ConfigurationException {
        final FileReader fileReader = new FileReader(EAS_MOBILECONFIG);
        final FileOutputStream fileWriter = new FileOutputStream(SIGNEDJAVA_MOBILECONFIG);
        final MobileConfigSigner mobileConfigSigner = new MobileConfigSigner(fileWriter);
        final char[] buf = new char[1024];
        int read;
        while (-1 != (read = fileReader.read(buf))) {
            mobileConfigSigner.write(buf, 0, read);
        }
        mobileConfigSigner.close();
        fileReader.close();
    }

    @Test
    public void signerTestTooLong() throws IOException, InterruptedException, ConfigurationException {
        final FileReader fileReader = new FileReader(EAS_MOBILECONFIG);
        final FileOutputStream fileWriter = new FileOutputStream(SIGNEDJAVA_MOBILECONFIG);
        final MobileConfigSigner mobileConfigSigner = new MobileConfigSigner(fileWriter) {
            @Override
            protected String[] getCommand() throws ConfigurationException {
                return new String[]{ "sleep", "10" };
            }
        };
        final char[] buf = new char[1024];
        int read;
        while (-1 != (read = fileReader.read(buf))) {
            mobileConfigSigner.write(buf, 0, read);
        }
        try {
            mobileConfigSigner.close();
            Assert.fail("No exception thrown");
        } catch (final IOException e) {
            Assert.assertTrue("Wrong exception message: " + e.getMessage(), MobileConfigSigner.OPENSSL_DIDN_T_RETURN_IN_A_TIMELY_MANNER_KILLED_PROCESS.equals(e.getMessage()));
        }

        fileReader.close();
    }

    @Test
    public void signerTestWrongResult() throws IOException, InterruptedException, ConfigurationException {
        final FileReader fileReader = new FileReader(EAS_MOBILECONFIG);
        final FileOutputStream fileWriter = new FileOutputStream(SIGNEDJAVA_MOBILECONFIG);
        final MobileConfigSigner mobileConfigSigner = new MobileConfigSigner(fileWriter) {
            @Override
            protected String[] getCommand() throws ConfigurationException {
                return new String[]{ "/bin/false" };
            }
        };
        final char[] buf = new char[1024];
        int read;
        read = fileReader.read(buf);
        mobileConfigSigner.write(buf, 0, read);
        try {
            mobileConfigSigner.close();
            Assert.fail("No exception thrown");
        } catch (final IOException e) {
            Assert.assertTrue("Wrong exception message: " + e.getMessage(), (MobileConfigSigner.OPENSSL_EXITED_UNEXPECTEDLY_WITH + 1).equals(e.getMessage()));
        }
    }
}
