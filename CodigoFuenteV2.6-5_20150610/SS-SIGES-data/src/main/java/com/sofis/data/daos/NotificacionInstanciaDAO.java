package com.sofis.data.daos;

import com.sofis.entities.constantes.MensajesNegocio;
import com.sofis.entities.data.Notificacion;
import com.sofis.entities.data.NotificacionInstancia;
import com.sofis.exceptions.TechnicalException;
import com.sofis.persistence.dao.exceptions.DAOGeneralException;
import com.sofis.persistence.dao.imp.hibernate.HibernateJpaDAOImp;
import java.io.Serializable;
import java.util.List;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 *
 * @author Usuario
 */
public class NotificacionInstanciaDAO extends HibernateJpaDAOImp<NotificacionInstancia, Integer> implements Serializable {

    private static final long serialVersionUID = 1L;

    public NotificacionInstanciaDAO(EntityManager em) {
        super(em);
    }

    public List<NotificacionInstancia> obtenerNotificacionInstPorProyId(Integer proyId, Integer orgPk) throws DAOGeneralException {
        if (proyId != null && orgPk != null) {
            try {
                String query = "SELECT ni FROM NotificacionInstancia ni "
                        + " WHERE ni.notinstProyFk.proyPk = :proyId"
                        + " AND ni.notinstProyFk.proyOrgFk.orgPk = :orgPk";
                Query q = super.getEm().createQuery(query);
                q.setParameter("proyId", proyId);
                q.setParameter("orgPk", orgPk);
                List<NotificacionInstancia> result = q.getResultList();
                return result;
            } catch (Exception w) {
                TechnicalException te = new TechnicalException();
                te.addError(MensajesNegocio.ERROR_NOTIFICACION_INST_OBTENER);
                throw te;
            }
        }
        return null;
    }

    public NotificacionInstancia obtenerNotificacionInstPorCod(String cod, Integer proyPk) {
        if (proyPk != null) {
            try {
                String query = "SELECT ni FROM NotificacionInstancia ni "
                        + " WHERE ni.notinstProyFk.proyPk = :proyPk"
                        + " AND ni.notinstNotFk.notCod = :cod";
                Query q = super.getEm().createQuery(query);
                q.setParameter("proyPk", proyPk);
                q.setParameter("cod", cod);
                return (NotificacionInstancia) q.getSingleResult();
            } catch (Exception w) {
                TechnicalException te = new TechnicalException();
                te.addError(MensajesNegocio.ERROR_NOTIFICACION_INST_OBTENER);
                throw te;
            }
        }
        return null;
    }

    public List<Notificacion> obtenerNotificacionesSinInstanciaPorProyId(Integer proyPk, Integer orgId) throws DAOGeneralException {
        if (proyPk != null) {
            try {
                String query = "SELECT n FROM Notificacion n "
                        + " WHERE n.notOrgFk.orgPk = :orgPk "
                        + " AND n.notPk NOT IN ( "
                        + " SELECT ni.notinstNotFk.notPk "
                        + " FROM NotificacionInstancia ni "
                        + " WHERE ni.notinstProyFk.proyPk = :proyId )";
                Query q = super.getEm().createQuery(query);
                q.setParameter("proyId", proyPk);
                q.setParameter("orgPk", orgId);
                return q.getResultList();
            } catch (Exception w) {
                TechnicalException te = new TechnicalException();
                te.addError(MensajesNegocio.ERROR_NOTIFICACION_INST_OBTENER);
                throw te;
            }
        }
        return null;
    }
}