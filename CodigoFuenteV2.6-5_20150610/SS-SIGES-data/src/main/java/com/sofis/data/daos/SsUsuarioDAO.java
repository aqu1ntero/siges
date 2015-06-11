package com.sofis.data.daos;

import com.sofis.entities.data.Organismos;
import com.sofis.entities.data.SsUsuario;
import com.sofis.persistence.dao.exceptions.DAOGeneralException;
import com.sofis.persistence.dao.imp.hibernate.HibernateJpaDAOImp;
import java.io.Serializable;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 *
 * @author SSGenerador
 */
public class SsUsuarioDAO extends HibernateJpaDAOImp<SsUsuario, Integer> implements Serializable {

    private static final long serialVersionUID = 1L;

    public SsUsuarioDAO(EntityManager em) {
        super(em);
    }

    @Deprecated
    public List<SsUsuario> obtenerHabilitados() throws DAOGeneralException {
        CodigueraDAO cdao = new CodigueraDAO(super.getEm());
        return cdao.obtenerHabilitados(SsUsuario.class, "usuVigente", "usuDescripcion");
    }

    public List<Organismos> obtenerOrganismosPermitidos(Integer usuPk) {
        if (usuPk != null) {
            String nativeQuery = "SELECT * FROM organismos WHERE org_pk=(SELECT usu_ofi_roles_oficina FROM ss_usu_ofi_roles WHERE usu_ofi_roles_usuario = :usuPk GROUP BY usu_ofi_roles_oficina)";
            Query query = super.getEm().createNativeQuery(nativeQuery);
            query.setParameter("usuPk", usuPk);

            List<Organismos> result = query.getResultList();
            return result;
        }
        return null;
    }
    
    public List<SsUsuario> obtenerSsUsuariosCoordEnt(Integer orgPk) {
        String queryStr = "SELECT e.coordinadorUsuFk"
                + " FROM Entregables e"
                + " WHERE e.entCroFk.proyecto.proyOrgFk.orgPk = :orgPk"
                + " GROUP BY e.coordinadorUsuFk"
                + " ORDER BY e.coordinadorUsuFk.usuPrimerNombre, e.coordinadorUsuFk.usuPrimerApellido";

        Query query = getEm().createQuery(queryStr);
        query.setParameter("orgPk", orgPk);
        List<SsUsuario> result = query.getResultList();
        return result;
    }
}