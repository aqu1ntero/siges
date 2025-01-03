package com.sofis.data.daos;

import com.sofis.entities.data.Moneda;
import com.sofis.persistence.dao.imp.hibernate.HibernateJpaDAOImp;
import java.io.Serializable;
import java.util.logging.Logger;
import javax.persistence.EntityManager;

/**
 *
 * @author Usuario
 */
public class MonedaDAO extends HibernateJpaDAOImp<Moneda, Integer> implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(MonedaDAO.class.getName());      

    public MonedaDAO(EntityManager em) {
        super(em);
    }
}
