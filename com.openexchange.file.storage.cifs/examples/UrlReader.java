import java.io.InputStream;
import java.net.URL;

public class UrlReader extends Thread {

    URL url;
    byte[] buf;

    public UrlReader( String u, int bufsiz ) throws Exception {
        url = new URL( u );
        buf = new byte[bufsiz];
    }

    @Override
    public void run() {
        try {
            InputStream in = url.openStream();
            int n;
            while ((n = in.read( buf )) > 0) {
                System.out.write( buf, 0, n );
            }
            in.close();
            System.err.println( url + " read complete" );
        } catch( Exception ex ) {
            ex.printStackTrace( System.err );
        }
    }

    public static void main( String[] args ) throws Exception {
        UrlReader[] readers = new UrlReader[args.length];

        jcifs.Config.registerSmbURLHandler();

        int i;
        for( i = 0; i < args.length; i++) {
            readers[i] = new UrlReader( args[i], (i + 1) * 128 );
        }
        for( i = 0; i < args.length; i++) {
            readers[i].start();
        }
    }
}
