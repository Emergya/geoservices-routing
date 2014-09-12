
package com.emergya.geoservices.routing.wsdl;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.emergya.geoservices.routing.wsdl package. 
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

    private final static QName _FindShortestPath_QNAME = new QName("http://sigem.sitep.com/mrk", "findShortestPath");
    private final static QName _FindShortestPathResponse_QNAME = new QName("http://sigem.sitep.com/mrk", "findShortestPathResponse");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.emergya.geoservices.routing.wsdl
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ResponsePoint }
     * 
     */
    public ResponsePoint createResponsePoint() {
        return new ResponsePoint();
    }

    /**
     * Create an instance of {@link FindShortestPathResponse }
     * 
     */
    public FindShortestPathResponse createFindShortestPathResponse() {
        return new FindShortestPathResponse();
    }

    /**
     * Create an instance of {@link FindShortestPath }
     * 
     */
    public FindShortestPath createFindShortestPath() {
        return new FindShortestPath();
    }

    /**
     * Create an instance of {@link Step }
     * 
     */
    public Step createStep() {
        return new Step();
    }

    /**
     * Create an instance of {@link RequestPoint }
     * 
     */
    public RequestPoint createRequestPoint() {
        return new RequestPoint();
    }

    /**
     * Create an instance of {@link Message }
     * 
     */
    public Message createMessage() {
        return new Message();
    }

    /**
     * Create an instance of {@link Header }
     * 
     */
    public Header createHeader() {
        return new Header();
    }

    /**
     * Create an instance of {@link com.emergya.geoservices.routing.wsdl.Edge }
     * 
     */
    public com.emergya.geoservices.routing.wsdl.Edge createEdge() {
        return new com.emergya.geoservices.routing.wsdl.Edge();
    }

    /**
     * Create an instance of {@link Point }
     * 
     */
    public Point createPoint() {
        return new Point();
    }

    /**
     * Create an instance of {@link ResponsePoint.Edge }
     * 
     */
    public ResponsePoint.Edge createResponsePointEdge() {
        return new ResponsePoint.Edge();
    }

    /**
     * Create an instance of {@link FindShortestPathResponse.Headers }
     * 
     */
    public FindShortestPathResponse.Headers createFindShortestPathResponseHeaders() {
        return new FindShortestPathResponse.Headers();
    }

    /**
     * Create an instance of {@link FindShortestPathResponse.Path }
     * 
     */
    public FindShortestPathResponse.Path createFindShortestPathResponsePath() {
        return new FindShortestPathResponse.Path();
    }

    /**
     * Create an instance of {@link FindShortestPathResponse.Messages }
     * 
     */
    public FindShortestPathResponse.Messages createFindShortestPathResponseMessages() {
        return new FindShortestPathResponse.Messages();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FindShortestPath }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://sigem.sitep.com/mrk", name = "findShortestPath")
    public JAXBElement<FindShortestPath> createFindShortestPath(FindShortestPath value) {
        return new JAXBElement<FindShortestPath>(_FindShortestPath_QNAME, FindShortestPath.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FindShortestPathResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://sigem.sitep.com/mrk", name = "findShortestPathResponse")
    public JAXBElement<FindShortestPathResponse> createFindShortestPathResponse(FindShortestPathResponse value) {
        return new JAXBElement<FindShortestPathResponse>(_FindShortestPathResponse_QNAME, FindShortestPathResponse.class, null, value);
    }

}
