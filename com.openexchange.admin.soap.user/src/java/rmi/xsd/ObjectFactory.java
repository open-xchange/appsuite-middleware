
package java.rmi.xsd;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the java.rmi.xsd package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _RemoteExceptionMessage_QNAME = new QName("http://rmi.java/xsd", "message");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: java.rmi.xsd
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link RemoteException }
     * 
     */
    public RemoteException createRemoteException() {
        return new RemoteException();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://rmi.java/xsd", name = "message", scope = RemoteException.class)
    public JAXBElement<String> createRemoteExceptionMessage(String value) {
        return new JAXBElement<String>(_RemoteExceptionMessage_QNAME, String.class, RemoteException.class, value);
    }

}
