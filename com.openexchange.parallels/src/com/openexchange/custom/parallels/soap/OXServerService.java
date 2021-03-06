package com.openexchange.custom.parallels.soap;

import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;

/**
 * This class was generated by Apache CXF 2.6.1
 * 2012-07-11T15:32:34.778+02:00
 * Generated source version: 2.6.1
 * 
 */
@WebServiceClient(name = "OXServerService", 
                  // wsdlLocation = "null",
                  targetNamespace = "http://soap.parallels.custom.openexchange.com") 
public class OXServerService extends Service {

    public final static URL WSDL_LOCATION;

    public final static QName SERVICE = new QName("http://soap.parallels.custom.openexchange.com", "OXServerService");
    public final static QName OXServerServiceHttpsEndpoint = new QName("http://soap.parallels.custom.openexchange.com", "OXServerServiceHttpsEndpoint");
    public final static QName OXServerServiceHttpsSoap12Endpoint = new QName("http://soap.parallels.custom.openexchange.com", "OXServerServiceHttpsSoap12Endpoint");
    public final static QName OXServerServiceHttpSoap11Endpoint = new QName("http://soap.parallels.custom.openexchange.com", "OXServerServiceHttpSoap11Endpoint");
    public final static QName OXServerServiceHttpsSoap11Endpoint = new QName("http://soap.parallels.custom.openexchange.com", "OXServerServiceHttpsSoap11Endpoint");
    public final static QName OXServerServiceHttpEndpoint = new QName("http://soap.parallels.custom.openexchange.com", "OXServerServiceHttpEndpoint");
    public final static QName OXServerServiceHttpSoap12Endpoint = new QName("http://soap.parallels.custom.openexchange.com", "OXServerServiceHttpSoap12Endpoint");
    static {
        WSDL_LOCATION = null;
    }

    public OXServerService(URL wsdlLocation) {
        super(wsdlLocation, SERVICE);
    }

    public OXServerService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public OXServerService() {
        super(WSDL_LOCATION, SERVICE);
    }
    

    /**
     *
     * @return
     *     returns OXServerServicePortType
     */
    @WebEndpoint(name = "OXServerServiceHttpsEndpoint")
    public OXServerServicePortType getOXServerServiceHttpsEndpoint() {
        return super.getPort(OXServerServiceHttpsEndpoint, OXServerServicePortType.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns OXServerServicePortType
     */
    @WebEndpoint(name = "OXServerServiceHttpsEndpoint")
    public OXServerServicePortType getOXServerServiceHttpsEndpoint(WebServiceFeature... features) {
        return super.getPort(OXServerServiceHttpsEndpoint, OXServerServicePortType.class, features);
    }
    /**
     *
     * @return
     *     returns OXServerServicePortType
     */
    @WebEndpoint(name = "OXServerServiceHttpsSoap12Endpoint")
    public OXServerServicePortType getOXServerServiceHttpsSoap12Endpoint() {
        return super.getPort(OXServerServiceHttpsSoap12Endpoint, OXServerServicePortType.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns OXServerServicePortType
     */
    @WebEndpoint(name = "OXServerServiceHttpsSoap12Endpoint")
    public OXServerServicePortType getOXServerServiceHttpsSoap12Endpoint(WebServiceFeature... features) {
        return super.getPort(OXServerServiceHttpsSoap12Endpoint, OXServerServicePortType.class, features);
    }
    /**
     *
     * @return
     *     returns OXServerServicePortType
     */
    @WebEndpoint(name = "OXServerServiceHttpSoap11Endpoint")
    public OXServerServicePortType getOXServerServiceHttpSoap11Endpoint() {
        return super.getPort(OXServerServiceHttpSoap11Endpoint, OXServerServicePortType.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns OXServerServicePortType
     */
    @WebEndpoint(name = "OXServerServiceHttpSoap11Endpoint")
    public OXServerServicePortType getOXServerServiceHttpSoap11Endpoint(WebServiceFeature... features) {
        return super.getPort(OXServerServiceHttpSoap11Endpoint, OXServerServicePortType.class, features);
    }
    /**
     *
     * @return
     *     returns OXServerServicePortType
     */
    @WebEndpoint(name = "OXServerServiceHttpsSoap11Endpoint")
    public OXServerServicePortType getOXServerServiceHttpsSoap11Endpoint() {
        return super.getPort(OXServerServiceHttpsSoap11Endpoint, OXServerServicePortType.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns OXServerServicePortType
     */
    @WebEndpoint(name = "OXServerServiceHttpsSoap11Endpoint")
    public OXServerServicePortType getOXServerServiceHttpsSoap11Endpoint(WebServiceFeature... features) {
        return super.getPort(OXServerServiceHttpsSoap11Endpoint, OXServerServicePortType.class, features);
    }
    /**
     *
     * @return
     *     returns OXServerServicePortType
     */
    @WebEndpoint(name = "OXServerServiceHttpEndpoint")
    public OXServerServicePortType getOXServerServiceHttpEndpoint() {
        return super.getPort(OXServerServiceHttpEndpoint, OXServerServicePortType.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns OXServerServicePortType
     */
    @WebEndpoint(name = "OXServerServiceHttpEndpoint")
    public OXServerServicePortType getOXServerServiceHttpEndpoint(WebServiceFeature... features) {
        return super.getPort(OXServerServiceHttpEndpoint, OXServerServicePortType.class, features);
    }
    /**
     *
     * @return
     *     returns OXServerServicePortType
     */
    @WebEndpoint(name = "OXServerServiceHttpSoap12Endpoint")
    public OXServerServicePortType getOXServerServiceHttpSoap12Endpoint() {
        return super.getPort(OXServerServiceHttpSoap12Endpoint, OXServerServicePortType.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns OXServerServicePortType
     */
    @WebEndpoint(name = "OXServerServiceHttpSoap12Endpoint")
    public OXServerServicePortType getOXServerServiceHttpSoap12Endpoint(WebServiceFeature... features) {
        return super.getPort(OXServerServiceHttpSoap12Endpoint, OXServerServicePortType.class, features);
    }

}
