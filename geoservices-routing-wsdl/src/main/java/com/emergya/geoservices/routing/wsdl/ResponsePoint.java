
package com.emergya.geoservices.routing.wsdl;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para ResponsePoint complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="ResponsePoint">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="edge">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *                   &lt;element name="distFrom" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *                   &lt;element name="distTo" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *                   &lt;element name="direction" type="{http://www.w3.org/2001/XMLSchema}string"/>
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
@XmlType(name = "ResponsePoint", propOrder = {
    "edge"
})
public class ResponsePoint {

    @XmlElement(required = true)
    protected ResponsePoint.Edge edge;

    /**
     * Obtiene el valor de la propiedad edge.
     * 
     * @return
     *     possible object is
     *     {@link ResponsePoint.Edge }
     *     
     */
    public ResponsePoint.Edge getEdge() {
        return edge;
    }

    /**
     * Define el valor de la propiedad edge.
     * 
     * @param value
     *     allowed object is
     *     {@link ResponsePoint.Edge }
     *     
     */
    public void setEdge(ResponsePoint.Edge value) {
        this.edge = value;
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
     *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}int"/>
     *         &lt;element name="distFrom" type="{http://www.w3.org/2001/XMLSchema}double"/>
     *         &lt;element name="distTo" type="{http://www.w3.org/2001/XMLSchema}double"/>
     *         &lt;element name="direction" type="{http://www.w3.org/2001/XMLSchema}string"/>
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
        "id",
        "distFrom",
        "distTo",
        "direction"
    })
    public static class Edge {

        protected int id;
        protected double distFrom;
        protected double distTo;
        @XmlElement(required = true)
        protected String direction;

        /**
         * Obtiene el valor de la propiedad id.
         * 
         */
        public int getId() {
            return id;
        }

        /**
         * Define el valor de la propiedad id.
         * 
         */
        public void setId(int value) {
            this.id = value;
        }

        /**
         * Obtiene el valor de la propiedad distFrom.
         * 
         */
        public double getDistFrom() {
            return distFrom;
        }

        /**
         * Define el valor de la propiedad distFrom.
         * 
         */
        public void setDistFrom(double value) {
            this.distFrom = value;
        }

        /**
         * Obtiene el valor de la propiedad distTo.
         * 
         */
        public double getDistTo() {
            return distTo;
        }

        /**
         * Define el valor de la propiedad distTo.
         * 
         */
        public void setDistTo(double value) {
            this.distTo = value;
        }

        /**
         * Obtiene el valor de la propiedad direction.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getDirection() {
            return direction;
        }

        /**
         * Define el valor de la propiedad direction.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setDirection(String value) {
            this.direction = value;
        }

    }

}
