import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

public class DnsSrv {

    String getDomain(String name) throws NamingException {
        DirContext context;
        NameNotFoundException ret = null;

        context = new InitialDirContext();
        for ( ;; ) {
            try {
                Attributes attributes = context.getAttributes(
                    "dns:/_ldap._tcp.dc._msdcs." + name,
                    new String[] { "SRV" }
                );
                return name;
            } catch (NameNotFoundException nnfe) {
                ret = nnfe;
            }
            int dot = name.indexOf('.');
            if (dot == -1) {
                break;
            }
            name = name.substring(dot + 1);
        }

        throw ret != null ? ret : new NamingException("invalid name");
    }

    public static void main(String argv[]) throws Exception {
        DnsSrv dnsSrv = new DnsSrv();
        System.out.println(dnsSrv.getDomain(argv[0]));
    }
}

