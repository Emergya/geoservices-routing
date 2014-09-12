
package com.emergya.geoservices.routing.wsdl;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para findShortestPath complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="findShortestPath">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="startPoint" type="{http://sigem.sitep.com/mrk}RequestPoint"/>
 *         &lt;element name="targetPoint" type="{http://sigem.sitep.com/mrk}RequestPoint"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "findShortestPath", propOrder = {
    "startPoint",
    "targetPoint"
})
public class FindShortestPath {

    @XmlElement(required = true)
    protected RequestPoint startPoint;
    @XmlElement(required = true)
    protected RequestPoint targetPoint;

    /**
     * Obtiene el valor de la propiedad startPoint.
     * 
     * @return
     *     possible object is
     *     {@link RequestPoint }
     *     
     */
    public RequestPoint getStartPoint() {
        return startPoint;
    }

    /**
     * Define el valor de la propiedad startPoint.
     * 
     * @param value
     *     allowed object is
     *     {@link RequestPoint }
     *     
     */
    public void setStartPoint(RequestPoint value) {
        this.startPoint = value;
    }

    /**
     * Obtiene el valor de la propiedad targetPoint.
     * 
     * @return
     *     possible object is
     *     {@link RequestPoint }
     *     
     */
    public RequestPoint getTargetPoint() {
        return targetPoint;
    }

    /**
     * Define el valor de la propiedad targetPoint.
     * 
     * @param value
     *     allowed object is
     *     {@link RequestPoint }
     *     
     */
    public void setTargetPoint(RequestPoint value) {
        this.targetPoint = value;
    }

}
