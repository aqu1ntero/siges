package com.sofis.business.ejbs;

import com.sofis.business.validations.TipoDocumentoValidacion;
import com.sofis.data.daos.TipoDocumentoDAO;
import com.sofis.data.utils.DAOUtils;
import com.sofis.entities.constantes.ConstanteApp;
import com.sofis.entities.constantes.MensajesNegocio;
import com.sofis.entities.data.TipoDocumento;
import com.sofis.exceptions.BusinessException;
import com.sofis.exceptions.TechnicalException;
import com.sofis.generico.utils.generalutils.CollectionsUtils;
import com.sofis.generico.utils.generalutils.StringsUtils;
import com.sofis.persistence.dao.exceptions.DAOGeneralException;
import com.sofis.sofisform.to.CriteriaTO;
import com.sofis.sofisform.to.MatchCriteriaTO;
import com.sofis.utils.CriteriaTOUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author Usuario
 */
@Named
@Stateless(name = "TipoDocumentoBean")
@LocalBean
public class TipoDocumentoBean {

    @PersistenceContext(unitName = ConstanteApp.PERSISTENCE_CONTEXT_UNIT_NAME)
    private EntityManager em;
    private static final Logger logger = Logger.getLogger(TipoDocumentoBean.class.getName());
    @Inject
    private DatosUsuario du;
    
    
    //private String usuario;
    //private String origen;
    
    @PostConstruct
    public void init(){
        //usuario = du.getCodigoUsuario();
        //origen = du.getOrigen();
    }
    

    public TipoDocumento obtenerTipoDocPorId(Integer tipoDocPk) {
	TipoDocumentoDAO tipoDocDao = new TipoDocumentoDAO(em);
	try {
	    return tipoDocDao.findById(TipoDocumento.class, tipoDocPk);

	} catch (DAOGeneralException ex) {
	    logger.logp(Level.SEVERE, TipoDocumentoBean.class.getName(), "obtenerTipoDocPorId", ex.getMessage(), ex);
	    TechnicalException te = new TechnicalException(ex);
	    te.addError(ex.getMessage());
	    throw te;
	}
    }

    public List<TipoDocumento> obtenerTodos(Integer orgPk) {
	TipoDocumentoDAO tipoDocDao = new TipoDocumentoDAO(em);
	try {
	    List<TipoDocumento> tiposDocumentos = tipoDocDao.findByOneProperty(TipoDocumento.class, "tipoDocOrgFk.orgPk", orgPk);
	    return tiposDocumentos;
	} catch (Exception ex) {
	    logger.logp(Level.SEVERE, TipoDocumentoBean.class.getName(), "obtenerTodos", ex.getMessage(), ex);
	    TechnicalException te = new TechnicalException(ex);
	    te.addError(ex.getMessage());
	    throw te;
	}
    }

    public void eliminarTipoDoc(Integer tipoDocPk) {
	if (tipoDocPk != null) {
	    TipoDocumentoDAO dao = new TipoDocumentoDAO(em);
	    try {
		TipoDocumento td = obtenerTipoDocPorId(tipoDocPk);
		dao.delete(td);
		checkResumenEjecutivo(null, td.getTipoDocOrgFk().getOrgPk());

	    } catch (DAOGeneralException ex) {
		logger.log(Level.SEVERE, null, ex);

		BusinessException be = new BusinessException();
		be.addError(MensajesNegocio.ERROR_TIPO_DOC_ELIMINAR);
		if (ex.getCause() instanceof javax.persistence.PersistenceException
			&& ex.getCause().getCause() instanceof org.hibernate.exception.ConstraintViolationException) {
		    be.addError(MensajesNegocio.ERROR_TIPO_DOC_CONST_VIOLATION);
		}
		throw be;
	    }
	}
    }

    public List<TipoDocumento> busquedaTipoDocFiltro(Map<String, Object> mapFiltro, String elementoOrdenacion, int ascendente, Integer orgPk) {
	List<CriteriaTO> criterios = new ArrayList<>();

	if (orgPk != null) {
	    MatchCriteriaTO criterio = CriteriaTOUtils.createMatchCriteriaTO(
		    MatchCriteriaTO.types.EQUALS, "tipoDocOrgFk.orgPk", orgPk);
	    criterios.add(criterio);
	}

	String nombre = mapFiltro != null ? (String) mapFiltro.get("tipodocNombre") : null;
	if (!StringsUtils.isEmpty(nombre)) {
//	    MatchCriteriaTO criterio = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.CONTAINS, "tipodocNombre", nombre);
	    CriteriaTO criterio = DAOUtils.createMatchCriteriaTOString("tipodocNombre", nombre);
	    criterios.add(criterio);
	}

	Integer estPk = mapFiltro != null ? (Integer) mapFiltro.get("tipodocExigidoDesde") : null;
	if (estPk != null && estPk != -1) {
	    MatchCriteriaTO criterio = CriteriaTOUtils.createMatchCriteriaTO(
		    MatchCriteriaTO.types.EQUALS, "tipodocExigidoDesde", estPk);
	    criterios.add(criterio);
	}

	CriteriaTO condicion;
	if (CollectionsUtils.isNotEmpty(criterios)) {
	    if (criterios.size() == 1) {
		condicion = criterios.get(0);
	    } else {
		condicion = CriteriaTOUtils.createANDTO(criterios.toArray(new CriteriaTO[0]));
	    }
	} else {
	    condicion = CriteriaTOUtils.createMatchCriteriaTO(
		    MatchCriteriaTO.types.NOT_NULL, "tipodocNombre", 1);
	}

	String[] orderBy = {};
	boolean[] asc = {};
	if (!StringsUtils.isEmpty(elementoOrdenacion)) {
	    orderBy = new String[]{elementoOrdenacion};
	    asc = new boolean[]{(ascendente == 1)};
	}

	TipoDocumentoDAO dao = new TipoDocumentoDAO(em);

	try {
	    return dao.findEntityByCriteria(TipoDocumento.class, condicion, orderBy, asc, null, null);
	} catch (DAOGeneralException ex) {
	    logger.log(Level.SEVERE, null, ex);
	    BusinessException be = new BusinessException();
	    be.addError(MensajesNegocio.ERROR_TIPO_DOC_OBTENER);
	    throw be;
	}
    }

    public TipoDocumento guardar(TipoDocumento tipoDoc) {
	if (tipoDoc != null) {
	    TipoDocumentoValidacion.validar(tipoDoc);
	    validarDuplicado(tipoDoc);

	    TipoDocumentoDAO dao = new TipoDocumentoDAO(em);
	    try {
		tipoDoc = dao.update(tipoDoc, du.getCodigoUsuario(),du.getOrigen());
		checkResumenEjecutivo(tipoDoc, tipoDoc.getTipoDocOrgFk().getOrgPk());
		return tipoDoc;
	    } catch (DAOGeneralException ex) {
		logger.log(Level.SEVERE, ex.getMessage(), ex);
		BusinessException be = new BusinessException();
		be.addError(MensajesNegocio.ERROR_AREAS_GUARDAR);
		throw be;
	    }
	}
	return null;
    }

    /**
     * Controla que solo haya un Tipo de Documento como resument ejecutivo. Si
     * no hay ninguno, marca uno por defecto.
     *
     * @param tipoDoc
     * @param orgPk
     */
    private void checkResumenEjecutivo(TipoDocumento tipoDoc, Integer orgPk) {
	if (orgPk != null) {
	    if (tipoDoc != null) {
		if (tipoDoc.getTipodocResumenEjecutivo()) {
		    List<TipoDocumento> listTipoDoc = obtenerTodos(orgPk);
		    for (TipoDocumento td : listTipoDoc) {

                        if (td.getTipodocResumenEjecutivo() != null 
                                && td.getTipodocResumenEjecutivo()
				&& !td.getTipdocPk().equals(tipoDoc.getTipdocPk())) {

                            td.setTipodocResumenEjecutivo(Boolean.FALSE);
			    guardar(td);
			}
		    }
		}
	    } else {
		List<TipoDocumento> listTipoDoc = obtenerTodos(orgPk);
		TipoDocumento td = null;
		boolean check = false;
		for (TipoDocumento tDoc : listTipoDoc) {
		    if (td == null) {
			td = tDoc;
		    }
		    if (tDoc.getTipodocResumenEjecutivo()) {
			check = true;
			break;
		    }
		}

		if (!check) {
		    td.setTipodocResumenEjecutivo(Boolean.TRUE);
		    guardar(td);
		}
	    }
	}
    }

    /**
     * Valida que no existan Tipos de Documentos con el mismo nombre. La
     * validación es dentro del organismo al cual pertenece el tipo de doc.
     * aportado.
     *
     * @param tipoDoc
     */
    private void validarDuplicado(TipoDocumento tipoDoc) {
	List<TipoDocumento> list = obtenerTodos(tipoDoc.getTipoDocOrgFk().getOrgPk());
	if (CollectionsUtils.isNotEmpty(list)) {
	    for (TipoDocumento td : list) {
		if (!td.getTipdocPk().equals(tipoDoc.getTipdocPk())
			&& td.getTipodocNombre().equals(tipoDoc.getTipodocNombre())) {
		    BusinessException be = new BusinessException();
		    be.addError(MensajesNegocio.ERROR_TIPO_DOC_NOMBRE_DUPLICADO);
		    throw be;
		}
	    }
	}
    }

    /**
     * Obtener todos los Tipo de Documentos de un organismo dado.
     *
     * @param orgPk
     * @return Lista de TipoDocumento.
     */
    public List<TipoDocumento> obtenerTipoDocPorOrgPk(Integer orgPk) {
	TipoDocumentoDAO tipoDocDao = new TipoDocumentoDAO(em);
	try {
	    return tipoDocDao.findByOneProperty(TipoDocumento.class, "tipoDocOrgFk.orgPk", orgPk);

	} catch (DAOGeneralException ex) {
	    logger.log(Level.SEVERE, ex.getMessage(), ex);
	    BusinessException be = new BusinessException();
	    be.addError(ex.getMessage());
	    throw be;
	}
    }
}
