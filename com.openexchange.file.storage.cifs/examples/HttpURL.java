import java.net.URL;

public class HttpURL {

    public static void main( String[] args ) throws Exception {
        jcifs.Config.registerSmbURLHandler();

        URL u = new URL( new URL( args[0] ), args[1] );
        System.out.println( u );
    }
}
