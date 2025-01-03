/*
 *  Clase desarrollada por Sofis Solutions
 *  
 */
package com.sofis.sofisform.to;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import org.jdom.Element;

/**
 * Clase desarrollada por Sofis Solutions
 *  key="CursoEdit"
renderedaspopup="true"
saveonparent="true"
image="../css/img/16x16_editar_ap.gif"
 * @author Sofis Solutions
 */
public class InputPopupTO extends BindingComponentTO implements Serializable{

   
    //el formulario asociado a la accion, en caso de edit o new es el formularios
    //al cual se debe de navegar
    private String form;
    //en caso de new o edit si se quiere renderizar como un popup el formularios form
    private boolean renderaspopup;
    //NO en USO, implica si una entidad de una coleccion de la entidad principal se
    //quiere almacenar en el momento que se almacena la entidad principal o en el momento
    //que se guarda ella
    private boolean saveonparent = true;
    //la imagen asociada al boton
    private String image;
    //el tooltip del boton
    private String tooltip;
    //en caso de NAVIGATE o de CANCEL donde se quiere navegar
    private String pageCase;
    //si el boton es user_defined en true implica que antes de invocar al bean y metodo asociado
    //al user defined igualmente se desea ejecutar la validacion definida en el modelo
    private boolean validateOnUserdefined = false;
    //en caso de ser user_defined el metodo que se invoca y contiene la logica del mismo
    //en otro caso si estan presentes antes de ejecutar la accion se invocan estos metodos
    //en caso de user_defined debe de devolver el case de navegacion
    private String bean;
    private String beanMethodName;
    //implica si antes de ejecutar la accion asociada se quiere perdir confirmación, si tiene un valor
    //cargado este String es la clave para mostrar en el mensaje javascript de confirmacion
    private String confirmActionKey;
    //el metodo del bean a ejecutarse luego de que se ejecuta la accion
    private String beanAfter;
    private String beanMethodNameAfter;

    //Si es true genera el delete button para eliminar el objeto asociado al componente.
    private boolean delete;

    public boolean isDelete() {
        return delete;
    }

    public void setDelete(boolean delete) {
        this.delete = delete;
    }

    public String getBeanAfter() {
        return beanAfter;
    }

    public void setBeanAfter(String beanAfter) {
        this.beanAfter = beanAfter;
    }

    public String getBeanMethodNameAfter() {
        return beanMethodNameAfter;
    }

    public void setBeanMethodNameAfter(String beanMethodNameAfter) {
        this.beanMethodNameAfter = beanMethodNameAfter;
    }

    public boolean isValidateOnUserdefined() {
        return validateOnUserdefined;
    }

    public void setValidateOnUserdefined(boolean validateOnUserdefined) {
        this.validateOnUserdefined = validateOnUserdefined;
    }

    public String getPageCase() {
        return pageCase;
    }

    public void setPageCase(String pageCase) {
        this.pageCase = pageCase;
    }

    public String getBean() {
        return bean;
    }

    public void setBean(String bean) {
        this.bean = bean;
    }

    public String getBeanMethodName() {
        return beanMethodName;
    }

    public void setBeanMethodName(String beanMethodName) {
        this.beanMethodName = beanMethodName;
    }

  

    public String getForm() {
        return form;
    }

    public void setForm(String form) {
        this.form = form;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public boolean isRenderaspopup() {
        return renderaspopup;
    }

    public void setRenderaspopup(boolean renderaspopup) {
        this.renderaspopup = renderaspopup;
    }

    public boolean isSaveonparent() {
        return saveonparent;
    }

    public void setSaveonparent(boolean saveonparent) {
        this.saveonparent = saveonparent;
    }

    public String getTooltip() {
        return tooltip;
    }

    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
    }

    public String getConfirmActionKey() {
        return confirmActionKey;
    }

    public void setConfirmActionKey(String confirmActionKey) {
        this.confirmActionKey = confirmActionKey;
    }

    public org.jdom.Element toXML() {
        Element toReturn = new Element("inputpopup");
        toReturn = this.toXMLMetadata(toReturn);
        toReturn.setAttribute("saveonparent", this.isSaveonparent() + "");

        toReturn.setAttribute("renderedaspopup", this.isRenderaspopup() + "");
        
        if (this.getBean() == null) {
            toReturn.setAttribute("bean", "");
        } else {
            toReturn.setAttribute("bean", this.getBean());
        }
        if (this.getBeanMethodName() == null) {
            toReturn.setAttribute("beanMethod", "");
        } else {
            toReturn.setAttribute("beanMethod", this.getBeanMethodName());
        }

        if (this.getBeanAfter() == null) {
            toReturn.setAttribute("beanAfter", "");
        } else {
            toReturn.setAttribute("beanAfter", this.getBeanAfter());
        }

        if (this.getBeanMethodNameAfter() == null) {
            toReturn.setAttribute("beanMethodAfter", "");
        } else {
            toReturn.setAttribute("beanMethodAfter", this.getBeanMethodNameAfter());
        }

        toReturn.setAttribute("validateOnUserdefined", this.isValidateOnUserdefined() + "");

        if (this.getPageCase() == null) {
            toReturn.setAttribute("pageCase", "");
        } else {
            toReturn.setAttribute("pageCase", this.getPageCase());
        }
        if (this.getForm() == null){
            toReturn.setAttribute("form", "");
        }else{
            toReturn.setAttribute("form", this.getForm() +"");
        }

        if (this.getImage() == null){
            toReturn.setAttribute("image", "");
        }else{
            toReturn.setAttribute("image", this.getImage() +"");
        }

        if (this.getTooltip() == null){
            toReturn.setAttribute("tooltip", "");
        }else{
            toReturn.setAttribute("tooltip", this.getTooltip() +"");
        }
        
        if (this.getConfirmActionKey() == null){
            toReturn.setAttribute("confirmActionKey", "");
        }else{
            toReturn.setAttribute("confirmActionKey", this.getConfirmActionKey() +"");
        }

        toReturn.setAttribute("delete", this.isDelete() +"");
        return toReturn;
    }
}
