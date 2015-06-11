package com.sofis.web.utils;

import com.sofis.exceptions.TechnicalException;
import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import com.sofis.business.ejbs.ArchivoContenidoLocalLocal;

import com.sofis.business.ejbs.DatosUsuarioRemote;
import com.sofis.business.ejbs.EntityManagementBeanRemote;
import com.sofis.business.ejbs.UsuarioLocal;
import com.sofis.entities.constantes.ConstanteApp;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Usuario
 */
public class EJBUtils {

    private static final Logger logger = Logger.getLogger(ConstanteApp.LOGGER_NAME);

    private static Object lookup(String beanName, String viewClassFullName, boolean stateful) throws NamingException {
        final Hashtable jndiProperties = new Hashtable();
        jndiProperties.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
        final Context context = new InitialContext(jndiProperties);
        context.addToEnvironment("sistema", "SS-SIGES");
//        context.addToEnvironment("sistema", "SS-SIGES-web");
        // The app name is the application name of the deployed EJBs. This is typically the ear name
        // without the .ear suffix. However, the application name could be overridden in the application.xml of the
        // EJB deployment on the server.
        // Since we haven't deployed the application as a .ear, the app name for us will be an empty string
        final String appName = "SS-SIGES-ear-1.0-SNAPSHOT";
        // This is the module name of the deployed EJBs on the server. This is typically the jar name of the
        // EJB deployment, without the .jar suffix, but can be overridden via the ejb-jar.xml
        // In this example, we have deployed the EJBs in a jboss-as-ejb-remote-app.jar, so the module name is
        // jboss-as-ejb-remote-app
        final String moduleName = "SS-SIGES-ejb-1.0-SNAPSHOT";
        // AS7 allows each deployment to have an (optional) distinct name. We haven't specified a distinct name for
        // our EJB deployment, so this is an empty string
        final String distinctName = "";
        // The EJB name which by default is the simple class name of the bean implementation class
        // let's do the lookup
        String looks = "ejb:" + appName + "/" + moduleName + "/" + distinctName + "/" + beanName + "!" + viewClassFullName;
        if (stateful) {
            looks += "?stateful";
        }
        return (Object) context.lookup(looks);
    }

    public static EntityManagementBeanRemote getEntityManagement() throws TechnicalException {
        try {
            return (EntityManagementBeanRemote) lookup("EntityManagementBean", EntityManagementBeanRemote.class.getName(), false);
        } catch (NamingException ex) {
            TechnicalException te = new TechnicalException();
            te.addError(ex.getMessage());
            throw te;
        }
    }

    public static DatosUsuarioRemote getDatosUsuario() throws TechnicalException {
        try {
            return (DatosUsuarioRemote) lookup("DatosUsuario", DatosUsuarioRemote.class.getName(), true);
        } catch (NamingException ex) {
            TechnicalException te = new TechnicalException();
            te.addError(ex.getMessage());
            throw te;
        }
    }

    public static ArchivoContenidoLocalLocal getArchivoContenidoLocal() throws TechnicalException {
        try {
            return (ArchivoContenidoLocalLocal) lookup("ArchivoContenidoLocal", ArchivoContenidoLocalLocal.class.getName(), false);
        } catch (NamingException ex) {
            TechnicalException te = new TechnicalException();
            te.addError(ex.getMessage());
            throw te;
        }
    }

    public static UsuarioLocal getUsuarioLocal() throws TechnicalException {
        try {
            UsuarioLocal usuLoc = (UsuarioLocal) lookup("SsUsuarioBean", UsuarioLocal.class.getName(), false);
            return usuLoc;
        } catch (NamingException ex) {
            logger.log(Level.SEVERE, null, ex);
            TechnicalException te = new TechnicalException();
            te.addError(ex.getMessage());
            throw te;
        }
    }
}