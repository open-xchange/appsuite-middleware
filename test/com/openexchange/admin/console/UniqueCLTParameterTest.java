package com.openexchange.admin.console;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import junit.framework.JUnit4TestAdapter;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import com.openexchange.admin.tools.ShellExecutor;
import com.openexchange.admin.tools.ShellExecutor.ArrayOutput;

public class UniqueCLTParameterTest {

    private String prefix = "/opt/open-xchange/sbin/";
    
    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(UniqueCLTParameterTest.class);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testUniqueCLTParameter() throws IOException, InterruptedException {
        final String envPrefix = System.getenv("SBINPREFIX");
        if( envPrefix.length() > 0 ) {
            System.out.println("Using " + envPrefix + "/ as path to commands to test...");
            this.prefix = envPrefix;
        }
        final URL resource = this.getClass().getClassLoader().getResource("");
        final List<String> cltlist = FileUtils.readLines(new File(resource.getFile() + "../sbin/contextcltlist"));
        cltlist.addAll(FileUtils.readLines(new File(resource.getFile() + "../sbin/usercltlist")));
        final ShellExecutor se = new ShellExecutor();
        for (final String command :  cltlist) {
            System.out.println("Testing " + command + " ...");
            final ArrayOutput result = se.executeprocargs(new String[] { prefix + command, "--check" });
            assertTrue(command + " failed: " + result.errOutput, result.exitstatus==0);
        }
    }
    
}
