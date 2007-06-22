package com.openexchange.admin.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class ShellExecutor {

    public class ArrayOutput {
        public final ArrayList<String> errOutput = new ArrayList<String>();

        public final ArrayList<String> stdOutput = new ArrayList<String>();
        
        public int exitstatus;
    }

    public final ArrayOutput executeprocargs(final String[] args) throws IOException, InterruptedException {
        final Process proc = Runtime.getRuntime().exec(args);
        final ArrayOutput retval = getoutputs(proc);
        retval.exitstatus = proc.waitFor();
        return retval;
    }

    private final ArrayOutput getoutputs(final Process proc) throws IOException {
        final InputStream err = proc.getErrorStream();
        final InputStream is = proc.getInputStream();
        final BufferedReader buf = new BufferedReader(new InputStreamReader(is));
        final BufferedReader errbuf = new BufferedReader(new InputStreamReader(err));
        String readBuffer = null;
        String errreadBuffer = null;
        final ArrayOutput retval = new ArrayOutput();

        while (null != (errreadBuffer = errbuf.readLine())) {
            retval.errOutput.add(errreadBuffer);
        }
        while (null != (readBuffer = buf.readLine())) {
            retval.stdOutput.add(readBuffer);
        }
        return retval;
    }

}
