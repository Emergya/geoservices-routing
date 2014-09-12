
package com.emergya.geoservices.routing.wsdl;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para findShortestPathResponse complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="findShortestPathResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="headers">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="header" type="{http://sigem.sitep.com/mrk}Header" maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="startPoint" type="{http://sigem.sitep.com/mrk}ResponsePoint"/>
 *         &lt;element name="targetPoint" type="{http://sigem.sitep.com/mrk}ResponsePoint"/>
 *         &lt;element name="path">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="step" type="{http://sigem.sitep.com/mrk}Step" maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="messages">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="message" type="{http://sigem.sitep.com/mrk}Message" maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "findShortestPathResponse", propOrder = {
    "headers",
    "startPoint",
    "targetPoint",
    "path",
    "messages"
})
public class FindShortestPathResponse {

    @XmlElement(required = true)
    protected FindShortestPathResponse.Headers headers;
    @XmlElement(required = true)
    protected ResponsePoint startPoint;
    @XmlElement(required = true)
    protected ResponsePoint targetPoint;
    @XmlElement(required = true)
    protected FindShortestPathResponse.Path path;
    @XmlElement(required = true)
    protected FindShortestPathResponse.Messages messages;

    /**
     * Obtiene el valor de la propiedad headers.
     * 
     * @return
     *     possible object is
     *     {@link FindShortestPathResponse.Headers }
     *     
     */
    public FindShortestPathResponse.Headers getHeaders() {
        return headers;
    }

    /**
     * Define el valor de la propiedad headers.
     * 
     * @param value
     *     allowed object is
     *     {@link FindShortestPathResponse.Headers }
     *     
     */
    public void setHeaders(FindShortestPathResponse.Headers value) {
        this.headers = value;
    }

    /**
     * Obtiene el valor de la propiedad startPoint.
     * 
     * @return
     *     possible object is
     *     {@link ResponsePoint }
     *     
     */
    public ResponsePoint getStartPoint() {
        return startPoint;
    }

    /**
     * Define el valor de la propiedad startPoint.
     * 
     * @param value
     *     allowed object is
     *     {@link ResponsePoint }
     *     
     */
    public void setStartPoint(ResponsePoint value) {
        this.startPoint = value;
    }

    /**
     * Obtiene el valor de la propiedad targetPoint.
     * 
     * @return
     *     possible object is
     *     {@link ResponsePoint }
     *     
     */
    public ResponsePoint getTargetPoint() {
        return targetPoint;
    }

    /**
     * Define el valor de la propiedad targetPoint.
     * 
     * @param value
     *     allowed object is
     *     {@link ResponsePoint }
     *     
     */
    public void setTargetPoint(ResponsePoint value) {
        this.targetPoint = value;
    }

    /**
     * Obtiene el valor de la propiedad path.
     * 
     * @return
     *     possible object is
     *     {@link FindShortestPathResponse.Path }
     *     
     */
    public FindShortestPathResponse.Path getPath() {
        return path;
    }

    /**
     * Define el valor de la propiedad path.
     * 
     * @param value
     *     allowed object is
     *     {@link FindShortestPathResponse.Path }
     *     
     */
    public void setPath(FindShortestPathResponse.Path value) {
        this.path = value;
    }

    /**
     * Obtiene el valor de la propiedad messages.
     * 
     * @return
     *     possible object is
     *     {@link FindShortestPathResponse.Messages }
     *     
     */
    public FindShortestPathResponse.Messages getMessages() {
        return messages;
    }

    /**
     * Define el valor de la propiedad messages.
     * 
     * @param value
     *     allowed object is
     *     {@link FindShortestPathResponse.Messages }
     *     
     */
    public void setMessages(FindShortestPathResponse.Messages value) {
        this.messages = value;
    }


    /**
     * <p>Clase Java para anonymous complex type.
     * 
     * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="header" type="{http://sigem.sitep.com/mrk}Header" maxOccurs="unbounded" minOccurs="0"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "header"
    })
    public static class Headers {

        protected List<Header> header;

        /**
         * Gets the value of the header property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the header property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getHeader().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Header }
         * 
         * 
         */
        public List<Header> getHeader() {
            if (header == null) {
                header = new ArrayList<Header>();
            }
            return this.header;
        }

    }


    /**
     * <p>Clase Java para anonymous complex type.
     * 
     * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="message" type="{http://sigem.sitep.com/mrk}Message" maxOccurs="unbounded" minOccurs="0"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "message"
    })
    public static class Messages {

        protected List<Message> message;

        /**
         * Gets the value of the message property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the message property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getMessage().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Message }
         * 
         * 
         */
        public List<Message> getMessage() {
            if (message == null) {
                message = new ArrayList<Message>();
            }
            return this.message;
        }

    }


    /**
     * <p>Clase Java para anonymous complex type.
     * 
     * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="step" type="{http://sigem.sitep.com/mrk}Step" maxOccurs="unbounded" minOccurs="0"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "step"
    })
    public static class Path {

        protected List<Step> step;

        /**
         * Gets the value of the step property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the step property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getStep().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Step }
         * 
         * 
         */
        public List<Step> getStep() {
            if (step == null) {
                step = new ArrayList<Step>();
            }
            return this.step;
        }

    }

}
