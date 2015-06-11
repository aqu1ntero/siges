package com.sofis.business.ejbs;

import com.sofis.business.interceptors.LoggedInterceptor;
import com.sofis.data.daos.TipoLeccionDAO;
import com.sofis.entities.constantes.ConstanteApp;
import com.sofis.entities.data.TipoLeccion;
import com.sofis.persistence.dao.exceptions.DAOGeneralException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Named;
import javax.interceptor.Interceptors;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author Usuario
 */
@Named
@Stateless(name = "TipoLeccionBean")
@LocalBean
@Interceptors({LoggedInterceptor.class})
public class TipoLeccionBean {

    @PersistenceContext(unitName = ConstanteApp.PERSISTENCE_CONTEXT_UNIT_NAME)
    private EntityManager em;
    private static final Logger logger = Logger.getLogger(ConstanteApp.LOGGER_NAME);

    public List<TipoLeccion> obtenerTodos() {
        TipoLeccionDAO dao = new TipoLeccionDAO(em);
        try {
            return dao.findAll(TipoLeccion.class);
        } catch (DAOGeneralException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }
        return null;
    }

}