
package org.agesic.siges.visualizador.web.ws;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.2.8
 * Generated source version: 2.0
 * 
 */
@WebService(name = "PublicarProyecto", targetNamespace = "http://ws.web.visualizador.siges.agesic.org/")
public interface PublicarProyecto {


    /**
     * 
     * @param token
     * @return
     *     returns org.agesic.siges.visualizador.web.ws.CategoriaProyectosResponse
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "obtenerCategoriasXml", targetNamespace = "http://ws.web.visualizador.siges.agesic.org/", className = "org.agesic.siges.visualizador.web.ws.ObtenerCategoriasXml")
    @ResponseWrapper(localName = "obtenerCategoriasXmlResponse", targetNamespace = "http://ws.web.visualizador.siges.agesic.org/", className = "org.agesic.siges.visualizador.web.ws.ObtenerCategoriasXmlResponse")
    public CategoriaProyectosResponse obtenerCategoriasXml(
        @WebParam(name = "token", targetNamespace = "")
        String token);

    /**
     * 
     * @param proyecto
     * @return
     *     returns java.lang.String
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "publicarProyecto", targetNamespace = "http://ws.web.visualizador.siges.agesic.org/", className = "org.agesic.siges.visualizador.web.ws.PublicarProyecto_Type")
    @ResponseWrapper(localName = "publicarProyectoResponse", targetNamespace = "http://ws.web.visualizador.siges.agesic.org/", className = "org.agesic.siges.visualizador.web.ws.PublicarProyectoResponse")
    public String publicarProyecto(
        @WebParam(name = "proyecto", targetNamespace = "")
        ProyectoImportado proyecto);

}
