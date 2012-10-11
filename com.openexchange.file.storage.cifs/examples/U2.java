import jcifs.dcerpc.UUID;

public class U2 {

    public static void main( String[] argv ) throws Exception {
        UUID uuid = new UUID(argv[0]);
        System.out.println(uuid.toString());
    }
}
