/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.mobile.configuration.generator;

import static com.openexchange.java.Autoboxing.I;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import com.openexchange.config.ConfigurationService;
import com.openexchange.java.Strings;
import com.openexchange.mobile.configuration.generator.configuration.ConfigurationException;
import com.openexchange.mobile.configuration.generator.configuration.MobileConfigProperties;
import com.openexchange.mobile.configuration.generator.configuration.Property;
import com.openexchange.mobile.configuration.generator.osgi.Services;
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
    Process process;
    private Writer output;
    private InputStream input;

    public MobileConfigSigner(OutputStream writer) throws ConfigurationException {
        this.writer = writer;
        List<String> command = getCommand();
        this.pb = new ProcessBuilder(command);
    }

    protected List<String> getCommand() throws ConfigurationException {
        ConfigurationService service = Services.optService(ConfigurationService.class);
        if (null == service) {
            throw new ConfigurationException("No configuration service found");
        }

        String opensslBinary = MobileConfigProperties.getProperty(service, Property.OpensslBinary);
        String certFile = MobileConfigProperties.getProperty(service, Property.CertFile);
        String keyFile = MobileConfigProperties.getProperty(service, Property.KeyFile);
        String pemFile = MobileConfigProperties.getProperty(service, Property.PemFile);

        List<String> l = new LinkedList<String>();
        l.add(opensslBinary);
        l.add("smime");
        l.add("-sign");
        l.add("-signer");
        l.add(certFile);
        l.add("-inkey");
        l.add(keyFile);
        l.add("-outform");
        l.add("der");
        l.add("-nodetach");
        if (Strings.isNotEmpty(pemFile)) {
            l.add("-certfile");
            l.add(pemFile);
        }

        return l;
    }

    private void init() throws IOException {
        this.process = pb.start();
        this.input = new BufferedInputStream(this.process.getInputStream(), 65536);
        this.output = new OutputStreamWriter(new BufferedOutputStream(this.process.getOutputStream()));

    }

    @Override
    public void close() throws IOException {
        this.output.close();
        final ThreadPoolService service = ThreadPools.getThreadPool();
        final Future<Integer> submit = service.submit(new AbstractTask<Integer>() {

            @Override
            public Integer call() throws Exception {
                final int waitFor;
                try {
                    waitFor = process.waitFor();
                } catch (InterruptedException e) {
                    // Restore the interrupted status; see http://www.ibm.com/developerworks/java/library/j-jtp05236/index.html
                    Thread.currentThread().interrupt();
                    throw new IOException("openssl process was interrupted");
                }
                if (waitFor != 0) {
                    throw new IOException(OPENSSL_EXITED_UNEXPECTEDLY_WITH + waitFor);
                }
                return I(waitFor);
            }

        });
        try {
            Integer property = MobileConfigProperties.getProperty(Property.OpensslTimeout);
            if (property == null) {
                submit.get(3000, TimeUnit.MILLISECONDS);
            } else {
                submit.get(property.intValue(), TimeUnit.MILLISECONDS);
            }
        } catch (InterruptedException e) {
            // Restore the interrupted status; see http://www.ibm.com/developerworks/java/library/j-jtp05236/index.html
            Thread.currentThread().interrupt();
            throw new IOException("openssl process was interrupted");
        } catch (ExecutionException e) {
            throw new IOException(e.getCause().getMessage());
        } catch (TimeoutException e) {
            this.process.destroy();
            throw new IOException(OPENSSL_DIDN_T_RETURN_IN_A_TIMELY_MANNER_KILLED_PROCESS);
        } catch (ConfigurationException e) {
            throw new IOException("An configuration error occured: " + e.getMessage());
        }
        final byte[] buf = new byte[1024];
        int read;
        while ((read = this.input.read(buf)) > 0) {
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
