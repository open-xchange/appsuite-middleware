import java.io.InterruptedIOException;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFileInputStream;
import jcifs.util.transport.TransportException;

public class InterruptTest extends Thread {

    String url;

    public InterruptTest(final String url) {
        this.url = url;
    }
    @Override
    public void run() {
        for (int i = 0; i < 100; i++) {
            try {
                final SmbFileInputStream in = new SmbFileInputStream(url);

                final byte[] b = new byte[10];
                while(in.read( b ) > 0) {
                    ;
                }

                in.close();
            } catch(final InterruptedIOException iioe) {
                System.out.println("InterruptedIOException");
                continue;
            } catch(final SmbException se) {
                Throwable t = se.getRootCause();
                if (t instanceof TransportException) {
                    final TransportException te = (TransportException)t;
                    t = te.getRootCause();
                    if (t instanceof InterruptedException) {
                        System.out.println("InterruptedException in constructor");
                        continue;
                    }
                }
                se.printStackTrace();
                try { Thread.sleep(500); } catch(final InterruptedException ie) {Thread.currentThread().interrupt();}
            } catch(final Exception e) {
                e.printStackTrace();
                break;
            }
        }
    }

    public static void main( final String argv[] ) throws Exception {
        final InterruptTest it = new InterruptTest(argv[0]);
        it.start();
        for (int i = 0; i < 20; i++) {
            Thread.sleep(200);
            it.interrupt();
        }
    }
}

