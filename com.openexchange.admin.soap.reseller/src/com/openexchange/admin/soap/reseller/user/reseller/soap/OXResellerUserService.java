package com.openexchange.admin.soap.reseller.user.reseller.soap;

import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.Service;

/**
 * This class was generated by Apache CXF 2.6.0
 * 2012-06-06T11:07:58.240+02:00
 * Generated source version: 2.6.0
 *
 */
@WebServiceClient(name = "OXResellerUserService",

                  targetNamespace = "http://soap.reseller.admin.openexchange.com")
public class OXResellerUserService extends Service {

    public final static URL WSDL_LOCATION;

    public final static QName SERVICE = new QName("http://soap.reseller.admin.openexchange.com", "OXResellerUserService");
    public final static QName OXResellerUserServiceHttpSoap12Endpoint = new QName("http://soap.reseller.admin.openexchange.com", "OXResellerUserServiceHttpSoap12Endpoint");
    public final static QName OXResellerUserServiceHttpsSoap11Endpoint = new QName("http://soap.reseller.admin.openexchange.com", "OXResellerUserServiceHttpsSoap11Endpoint");
    public final static QName OXResellerUserServiceHttpsEndpoint = new QName("http://soap.reseller.admin.openexchange.com", "OXResellerUserServiceHttpsEndpoint");
    public final static QName OXResellerUserServiceHttpsSoap12Endpoint = new QName("http://soap.reseller.admin.openexchange.com", "OXResellerUserServiceHttpsSoap12Endpoint");
    public final static QName OXResellerUserServiceHttpEndpoint = new QName("http://soap.reseller.admin.openexchange.com", "OXResellerUserServiceHttpEndpoint");
    public final static QName OXResellerUserServiceHttpSoap11Endpoint = new QName("http://soap.reseller.admin.openexchange.com", "OXResellerUserServiceHttpSoap11Endpoint");
    static {
        WSDL_LOCATION = null;
    }

    public OXResellerUserService(URL wsdlLocation) {
        super(wsdlLocation, SERVICE);
    }

    public OXResellerUserService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public OXResellerUserService() {
        super(WSDL_LOCATION, SERVICE);
    }


    /**
     *
     * @return
     *     returns OXResellerUserServicePortType
     */
    @WebEndpoint(name = "OXResellerUserServiceHttpSoap12Endpoint")
    public OXResellerUserServicePortType getOXResellerUserServiceHttpSoap12Endpoint() {
        return super.getPort(OXResellerUserServiceHttpSoap12Endpoint, OXResellerUserServicePortType.class);
    }

    /**
     *
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns OXResellerUserServicePortType
     */
    @WebEndpoint(name = "OXResellerUserServiceHttpSoap12Endpoint")
    public OXResellerUserServicePortType getOXResellerUserServiceHttpSoap12Endpoint(WebServiceFeature... features) {
        return super.getPort(OXResellerUserServiceHttpSoap12Endpoint, OXResellerUserServicePortType.class, features);
    }
    /**
     *
     * @return
     *     returns OXResellerUserServicePortType
     */
    @WebEndpoint(name = "OXResellerUserServiceHttpsSoap11Endpoint")
    public OXResellerUserServicePortType getOXResellerUserServiceHttpsSoap11Endpoint() {
        return super.getPort(OXResellerUserServiceHttpsSoap11Endpoint, OXResellerUserServicePortType.class);
    }

    /**
     *
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns OXResellerUserServicePortType
     */
    @WebEndpoint(name = "OXResellerUserServiceHttpsSoap11Endpoint")
    public OXResellerUserServicePortType getOXResellerUserServiceHttpsSoap11Endpoint(WebServiceFeature... features) {
        return super.getPort(OXResellerUserServiceHttpsSoap11Endpoint, OXResellerUserServicePortType.class, features);
    }
    /**
     *
     * @return
     *     returns OXResellerUserServicePortType
     */
    @WebEndpoint(name = "OXResellerUserServiceHttpsEndpoint")
    public OXResellerUserServicePortType getOXResellerUserServiceHttpsEndpoint() {
        return super.getPort(OXResellerUserServiceHttpsEndpoint, OXResellerUserServicePortType.class);
    }

    /**
     *
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns OXResellerUserServicePortType
     */
    @WebEndpoint(name = "OXResellerUserServiceHttpsEndpoint")
    public OXResellerUserServicePortType getOXResellerUserServiceHttpsEndpoint(WebServiceFeature... features) {
        return super.getPort(OXResellerUserServiceHttpsEndpoint, OXResellerUserServicePortType.class, features);
    }
    /**
     *
     * @return
     *     returns OXResellerUserServicePortType
     */
    @WebEndpoint(name = "OXResellerUserServiceHttpsSoap12Endpoint")
    public OXResellerUserServicePortType getOXResellerUserServiceHttpsSoap12Endpoint() {
        return super.getPort(OXResellerUserServiceHttpsSoap12Endpoint, OXResellerUserServicePortType.class);
    }

    /**
     *
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns OXResellerUserServicePortType
     */
    @WebEndpoint(name = "OXResellerUserServiceHttpsSoap12Endpoint")
    public OXResellerUserServicePortType getOXResellerUserServiceHttpsSoap12Endpoint(WebServiceFeature... features) {
        return super.getPort(OXResellerUserServiceHttpsSoap12Endpoint, OXResellerUserServicePortType.class, features);
    }
    /**
     *
     * @return
     *     returns OXResellerUserServicePortType
     */
    @WebEndpoint(name = "OXResellerUserServiceHttpEndpoint")
    public OXResellerUserServicePortType getOXResellerUserServiceHttpEndpoint() {
        return super.getPort(OXResellerUserServiceHttpEndpoint, OXResellerUserServicePortType.class);
    }

    /**
     *
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns OXResellerUserServicePortType
     */
    @WebEndpoint(name = "OXResellerUserServiceHttpEndpoint")
    public OXResellerUserServicePortType getOXResellerUserServiceHttpEndpoint(WebServiceFeature... features) {
        return super.getPort(OXResellerUserServiceHttpEndpoint, OXResellerUserServicePortType.class, features);
    }
    /**
     *
     * @return
     *     returns OXResellerUserServicePortType
     */
    @WebEndpoint(name = "OXResellerUserServiceHttpSoap11Endpoint")
    public OXResellerUserServicePortType getOXResellerUserServiceHttpSoap11Endpoint() {
        return super.getPort(OXResellerUserServiceHttpSoap11Endpoint, OXResellerUserServicePortType.class);
    }

    /**
     *
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns OXResellerUserServicePortType
     */
    @WebEndpoint(name = "OXResellerUserServiceHttpSoap11Endpoint")
    public OXResellerUserServicePortType getOXResellerUserServiceHttpSoap11Endpoint(WebServiceFeature... features) {
        return super.getPort(OXResellerUserServiceHttpSoap11Endpoint, OXResellerUserServicePortType.class, features);
    }

}
