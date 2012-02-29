package com.openexchange.mobile.configuration.generator;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import com.openexchange.config.ConfigurationService;
import com.openexchange.mobile.configuration.generator.configuration.ConfigurationException;
import com.openexchange.mobile.configuration.generator.configuration.MobileConfigProperties;
import com.openexchange.mobile.configuration.generator.configuration.Property;
import com.openexchange.mobile.configuration.generator.services.MobileConfigServiceRegistry;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadPools;



public class MobileConfigSigner extends Writer {

    // Public to test this
    public  static final String OPENSSL_DIDN_T_RETURN_IN_A_TIMELY_MANNER_KILLED_PROCESS = "openssl didn't return in a timely manner, killed process";

    // Public to test this
    public static final String OPENSSL_EXITED_UNEXPECTEDLY_WITH = "openssl exited unexpectedly with ";

    private final OutputStream writer;
    private final ProcessBuilder pb;
    private Process process;
    private Writer output;
    private InputStream input;

    public MobileConfigSigner(final OutputStream writer) throws ConfigurationException {
        this.writer = writer;
        final String[] command = getCommand();
        this.pb = new ProcessBuilder(command);
    }

    protected String[] getCommand() throws ConfigurationException {
        final ConfigurationService service = MobileConfigServiceRegistry.getServiceRegistry().getService(ConfigurationService.class);
        if (null == service) {
            throw new ConfigurationException("No configuration service found");
        }

        final String opensslBinary = MobileConfigProperties.getProperty(service, Property.OpensslBinary);
        final String certFile = MobileConfigProperties.getProperty(service, Property.CertFile);
        final String keyFile = MobileConfigProperties.getProperty(service, Property.KeyFile);
        final String[] command = new String[]{ opensslBinary, "smime", "-sign", "-signer", certFile, "-inkey",
            keyFile, "-outform", "der", "-nodetach"};
        return command;
    }

    private void init() throws IOException {
        this.process = pb.start();
        this.input = new BufferedInputStream(this.process.getInputStream());
        this.output = new OutputStreamWriter(new BufferedOutputStream(this.process.getOutputStream()));

    }

    @Override
    public void close() throws IOException {
        this.output.close();
        final ThreadPoolService service = ThreadPools.getThreadPool();
        final Future<Integer> submit = service.submit(new AbstractTask<Integer>() {

            public Integer call() throws Exception {
                final int waitFor;
                try {
                    waitFor = process.waitFor();
                } catch (final InterruptedException e) {
                    // Restore the interrupted status; see http://www.ibm.com/developerworks/java/library/j-jtp05236/index.html
                    Thread.currentThread().interrupt();
                    throw new IOException("openssl process was interrupted");
                }
                if (waitFor != 0) {
                    throw new IOException(OPENSSL_EXITED_UNEXPECTEDLY_WITH + waitFor);
                }
                return waitFor;
            }

        });
        try {
            final Integer property = MobileConfigProperties.getProperty(MobileConfigServiceRegistry.getServiceRegistry(), Property.OpensslTimeout);
            submit.get(property, TimeUnit.MILLISECONDS);
        } catch (final InterruptedException e) {
            // Restore the interrupted status; see http://www.ibm.com/developerworks/java/library/j-jtp05236/index.html
            Thread.currentThread().interrupt();
            throw new IOException("openssl process was interrupted");
        } catch (final ExecutionException e) {
            throw new IOException(e.getCause().getMessage());
        } catch (final TimeoutException e) {
            this.process.destroy();
            throw new IOException(OPENSSL_DIDN_T_RETURN_IN_A_TIMELY_MANNER_KILLED_PROCESS);
        } catch (final ConfigurationException e) {
            throw new IOException("An configuration error occured: " + e.getMessage());
        }
        final byte[] buf = new byte[1024];
        int read;
        while (-1 != (read = this.input.read(buf))) {
            this.writer.write(buf, 0, read);
        }
        this.writer.close();
        this.input.close();
    }

    @Override
    public void flush() throws IOException {
        this.output.flush();
    }

    @Override
    public void write(final char[] cbuf, final int off, final int len) throws IOException {
        if (null == process) {
            // First run, init
            init();
        }
        this.output.write(cbuf, off, len);
    }
}
