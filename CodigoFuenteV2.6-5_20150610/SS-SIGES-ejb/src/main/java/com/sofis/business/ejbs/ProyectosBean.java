package com.sofis.business.ejbs;

import com.sofis.business.utils.ProgProyUtils;
import com.sofis.business.validations.ProyectosValidacion;
import com.sofis.data.daos.EntHistLineaBaseDAO;
import com.sofis.data.daos.FichaProyectoDAO;
import com.sofis.data.daos.ProyIndicesDAO;
import com.sofis.data.daos.ProySitactHistoricoDAO;
import com.sofis.data.daos.ProyectosConCronogramaDAO;
import com.sofis.data.daos.ProyectosDAO;
import com.sofis.entities.codigueras.SsRolCodigos;
import com.sofis.entities.constantes.ConstanteApp;
import com.sofis.entities.constantes.ConstantesEstandares;
import com.sofis.entities.constantes.MensajesNegocio;
import com.sofis.entities.data.Adquisicion;
import com.sofis.entities.data.Areas;
import com.sofis.entities.data.AreasTags;
import com.sofis.entities.data.Cronogramas;
import com.sofis.entities.data.Devengado;
import com.sofis.entities.data.Documentos;
import com.sofis.entities.data.EntHistLineaBase;
import com.sofis.entities.data.Entregables;
import com.sofis.entities.data.Estados;
import com.sofis.entities.data.Interesados;
import com.sofis.entities.data.Pagos;
import com.sofis.entities.data.ProdMes;
import com.sofis.entities.data.Productos;
import com.sofis.entities.data.Programas;
import com.sofis.entities.data.ProyIndices;
import com.sofis.entities.data.ProyReplanificacion;
import com.sofis.entities.data.ProySitactHistorico;
import com.sofis.entities.data.Proyectos;
import com.sofis.entities.data.ProyectosConCronograma;
import com.sofis.entities.data.Riesgos;
import com.sofis.entities.data.SsUsuario;
import com.sofis.entities.enums.TipoFichaEnum;
import com.sofis.entities.tipos.FichaTO;
import com.sofis.entities.utils.SsUsuariosUtils;
import com.sofis.exceptions.BusinessException;
import com.sofis.exceptions.GeneralException;
import com.sofis.exceptions.MailException;
import com.sofis.exceptions.TechnicalException;
import com.sofis.generico.utils.generalutils.CollectionsUtils;
import com.sofis.generico.utils.generalutils.DatesUtils;
import com.sofis.generico.utils.generalutils.StringsUtils;
import com.sofis.persistence.dao.exceptions.DAOGeneralException;
import com.sofis.persistence.dao.reference.EntityReference;
import com.sofis.sofisform.to.CriteriaTO;
import com.sofis.sofisform.to.MatchCriteriaTO;
import com.sofis.utils.CriteriaTOUtils;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

/**
 *
 * @author Usuario
 */
@Named
@Stateless(name = "ProyectosBean")
@LocalBean
public class ProyectosBean {

    @PersistenceContext(unitName = ConstanteApp.PERSISTENCE_CONTEXT_UNIT_NAME)
    private EntityManager em;
    private static final Logger logger = Logger.getLogger(ConstanteApp.LOGGER_NAME);

    @Inject
    private ConsultaHistorico<Programas> ch;
    @Inject
    private DatosUsuario du;
    @EJB
    private ProgramasBean programasBean;
    @EJB
    private RiesgosBean riesgosBean;
    @EJB
    private DocumentosBean documentosBean;
    @EJB
    private MailBean mailBean;
    @EJB
    private SsUsuarioBean ssUsuarioBean;
    @EJB
    private ProgramasProyectosBean programasProyectosBean;
    @EJB
    private EntregablesBean entregablesBean;
    @EJB
    private ProyReplanificacionBean proyReplanificacionBean;
    @EJB
    private PresupuestoBean presupuestoBean;
    @EJB
    private AdquisicionBean adquisicionBean;
    @EJB
    private CronogramasBean cronogramasBean;
    @EJB
    private InteresadosBean interesadosBean;
    @EJB
    private TipoDocumentoInstanciaBean tipoDocumentoInstanciaBean;
    @EJB
    private NotificacionInstanciaBean notificacionInstanciaBean;
    @EJB
    private DevengadoBean devengadoBean;
    @EJB
    private RegistrosHorasBean registrosHorasBean;
    @EJB
    private GastosBean gastosBean;
    @EJB
    private AreaTematicaBean areaTematicaBean;
    @EJB
    private AreasBean areasBean;
    @EJB
    private ProductosBean productosBean;
    @EJB
    private ProdMesBean prodMesBean;
    @Inject
    private CalidadBean calidadBean;
    @Inject
    private ProyIndicesBean proyIndicesBean;

    private Proyectos guardar(Proyectos proy, SsUsuario usuario, Integer orgPk) throws GeneralException {
        logger.info("Guardar Proyecto.");
        ProyectosValidacion.validar(proy, usuario, orgPk);
        FichaProyectoDAO fpDao = new FichaProyectoDAO(em);

        try {
            //TODO para evitar el unsaved transient object, verificar porque no ocurre con estado y si con usuario
            SsUsuario usu;
            if (proy.getProyUsrAdjuntoFk() != null) {
                usu = em.find(SsUsuario.class, proy.getProyUsrAdjuntoFk().getUsuId());
                proy.setProyUsrAdjuntoFk(usu);
            }

            if (proy.getProyUsrGerenteFk() != null) {
                usu = em.find(SsUsuario.class, proy.getProyUsrGerenteFk().getUsuId());
                proy.setProyUsrGerenteFk(usu);
            }

            if (proy.getProyUsrPmofedFk() != null) {
                usu = em.find(SsUsuario.class, proy.getProyUsrPmofedFk().getUsuId());
                proy.setProyUsrPmofedFk(usu);
            }

            if (proy.getProyUsrSponsorFk() != null) {
                usu = em.find(SsUsuario.class, proy.getProyUsrSponsorFk().getUsuId());
                proy.setProyUsrSponsorFk(usu);
            }

            if (proy.getProyCroFk() != null && proy.getProyCroFk().getEntregablesSet() != null) {
                for (Entregables e : proy.getProyCroFk().getEntregablesSet()) {
                    if (e.getCoordinadorUsuFk() != null) {
                        usu = em.find(SsUsuario.class, e.getCoordinadorUsuFk().getUsuId());
                        e.setCoordinadorUsuFk(usu);
                    }
                }
            }

            proy = fpDao.update(proy, du.getCodigoUsuario(), du.getOrigen());

            if (proy != null) {
                if (proy.getProyEstFk().isEstado(Estados.ESTADOS.EJECUCION.estado_id)) {
                    proyReplanificacionBean.inactivarSolicitud(proy.getProyPk());
                }
                guardarIndicadoresYHermanos(proy.getProyPk(), orgPk);

                programasBean.actualizarProgramaPorProyectos(proy, usuario, orgPk);
            }

            return proy;

        } catch (BusinessException be) {
            logger.logp(Level.SEVERE, ProyectosBean.class.getName(), "guardar", be.getMessage(), be);
            throw be;
        } catch (Exception ex) {
            logger.logp(Level.SEVERE, ProyectosBean.class.getName(), "guardar", ex.getMessage(), ex);
            TechnicalException ge = new TechnicalException();
            ge.addError(ex.getMessage());
            throw ge;
        }
    }

    public Proyectos obtenerProyPorId(Integer id, Boolean loadAreasTem, Boolean loadAreasRestr, Boolean loadDocs, Boolean loadInteresados, Boolean loadRiesgos, Boolean loadCalidad) throws GeneralException {
        Proyectos proy = obtenerProyPorId(id);

        if (loadAreasTem != null && loadAreasTem) {
            proy.getAreasTematicasSet().isEmpty();
        }
        if (loadAreasRestr != null && loadAreasRestr) {
            proy.getAreasRestringidasSet().isEmpty();
        }
        if (loadDocs != null && loadDocs) {
            proy.getDocumentosSet().isEmpty();
        }
        if (loadInteresados != null && loadInteresados) {
            proy.getInteresadosList().isEmpty();
        }
        if (loadRiesgos != null && loadRiesgos) {
            proy.getRiesgosList().isEmpty();
        }
        if (loadCalidad != null && loadCalidad) {
            proy.getCalidadList().isEmpty();
        }

        return proy;
    }

    public Proyectos obtenerProyPorId(Integer id) throws GeneralException {
        if (id != null) {
            ProyectosDAO proyDao = new ProyectosDAO(em);
            try {
                Proyectos proy = proyDao.findById(Proyectos.class, id);
                return proy;
            } catch (NoResultException e) {
                logger.log(Level.SEVERE, null, e);
                return null;
            } catch (Exception ex) {
                //Las demás excepciones se consideran técnicas
                logger.log(Level.SEVERE, null, ex);
                TechnicalException ge = new TechnicalException();
                ge.addError(ex.getMessage());
                throw ge;
            }
        }
        return null;
    }

    public Proyectos guardarProyecto(FichaTO fichaTO, SsUsuario usuario, Integer orgPk) throws GeneralException {
        Proyectos proy = (Proyectos) ProgProyUtils.fichaTOToProgProy(fichaTO);
        return guardarProyecto(proy, usuario, orgPk);
    }

    public Proyectos guardarProyecto(Proyectos proy, SsUsuario usuario, Integer orgPk) throws GeneralException {
        if (proy != null) {
            guardarHistoricoSitAct(proy, usuario);
            guardarLineaBaseYHistorico(proy);

            if (proy.getProyectoOriginal() != null
                    && proy.getProyectoOriginal().getProyEstFk().isEstado(Estados.ESTADOS.PLANIFICACION.estado_id)
                    && proy.getProyEstFk().isEstado(Estados.ESTADOS.EJECUCION.estado_id)) {
                List<Adquisicion> adqList = adquisicionBean.obtenerAdquisicionPorProy(proy.getProyPk());

                if (adqList != null) {
                    for (Adquisicion adq : adqList) {
                        if (adq.getPagosSet() != null) {
                            for (Pagos pago : adq.getPagosSet()) {
                                if (!pago.isPagConfirmado()) {
                                    pago.setPagFechaReal(pago.getPagFechaPlanificada());
                                    pago.setPagImporteReal(pago.getPagImportePlanificado());
                                }
                            }
                        }

                        if (CollectionsUtils.isNotEmpty(adq.getDevengadoList())) {
                            for (Devengado dev : adq.getDevengadoList()) {
                                dev.setDevReal(dev.getDevPlan());
                            }
                        }
                    }
                    if (proy.getProyPreFk() != null) {
                        proy.getProyPreFk().setAdquisicionSet(new HashSet<>(adqList));
                    }
                }
            }

            boolean cambioEstado = false;

            if (proy.getProyPk() == null) {
                proy.setProyFechaCrea(new Date());
                proy.setProyFechaAct(new Date());

                if (proy.getProyUsrPmofedFk() != null
                        && proy.getProyUsrPmofedFk().equals(usuario)) {
                    proy.setProyEstFk(new Estados(Estados.ESTADOS.INICIO.estado_id));
                    proy.setProyEstPendienteFk(null);

                } else {
                    if (usuario.isUsuarioPMOT(orgPk)) {
                        proy.setProyEstFk(new Estados(Estados.ESTADOS.PENDIENTE_PMOF.estado_id));
                        proy.setProyEstPendienteFk(new Estados(Estados.ESTADOS.PENDIENTE_PMOF.estado_id));
                    } else {
                        proy.setProyEstFk(new Estados(Estados.ESTADOS.PENDIENTE_PMOT.estado_id));
                        proy.setProyEstPendienteFk(new Estados(Estados.ESTADOS.PENDIENTE_PMOT.estado_id));
                    }
                }
            } else {
                Proyectos proyPersistido = obtenerProyPorId(proy.getProyPk());
                if (!proyPersistido.getProyEstFk().equals(proy.getProyEstFk())) {
                    proy.setProyFechaEstadoAct(new Date());
                    cambioEstado = true;
                    proy.setProyFechaAct(new Date());
                }

                if (isUsuarioGerenteOAdjuntoProy(proy, usuario)) {
                    if (!tieneCambiosConfPMOT(proy, proyPersistido)) {
                        proy.setProyFechaAct(new Date());
                    }
                }
            }

            if (proy.getActivo() == null) {
                proy.setActivo(true);
            }

            boolean hasDoc = false;
            try {
                hasDoc = CollectionsUtils.isNotEmpty(proy.getDocumentosSet());
            } catch (Exception e) {
            }
            if (hasDoc) {
                for (Documentos doc : proy.getDocumentosSet()) {
                    if (doc.getDocsFecha() == null) {
                        doc.setDocsFecha(new Date());
                    }
                }
            }

            if (proy.getActivo() == null) {
                proy.setActivo(true);
            }

            proy = guardar(proy, usuario, orgPk);

            if (proy != null) {
                mailPostGuardar(proy, cambioEstado, orgPk);
            }
        }
        return proy;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    private void mailPostGuardar(Proyectos proy, boolean cambioEstado, Integer orgPk) {
        if (proy != null) {
            if (cambioEstado) {
                List<SsUsuario> usuariosDest = new ArrayList<>();
                usuariosDest.add(proy.getProyUsrGerenteFk());
                usuariosDest.add(proy.getProyUsrAdjuntoFk());
                String[] destinatarios = SsUsuariosUtils.arrayMailsUsuarios(usuariosDest);
                try {
                    mailBean.comunicarCambioEstado(orgPk, proy, destinatarios);
                } catch (MailException me) {
                    logger.log(Level.SEVERE, me.getMessage(), me);
                }
            }

            if (proy.getProyEstFk().isPendientes()) {
                List<SsUsuario> usuariosDest = new ArrayList<>();
                if (proy.getProyEstFk().isEstado(Estados.ESTADOS.PENDIENTE_PMOT.estado_id)) {
                    String[] ordenUsuarios = new String[]{"usuPrimerNombre", "usuSegundoNombre", "usuPrimerApellido", "usuSegundoApellido"};
                    boolean[] ascUsuarios = new boolean[]{true, true, true, true};
                    usuariosDest.addAll(ssUsuarioBean.obtenerUsuariosPorRol(SsRolCodigos.PMO_TRANSVERSAL, orgPk, ordenUsuarios, ascUsuarios));
                } else if (proy.getProyEstFk().isEstado(Estados.ESTADOS.PENDIENTE_PMOF.estado_id)) {
                    usuariosDest.add(proy.getProyUsrPmofedFk());
                }
                String[] destinatarios = SsUsuariosUtils.arrayMailsUsuarios(usuariosDest);
                try {
                    mailBean.comunicarProgProyPendiente(orgPk, proy, destinatarios);
                } catch (MailException me) {
                    logger.log(Level.SEVERE, me.getMessage(), me);
                }
            }
        }
    }

    public void guardarHistoricoSitAct(Proyectos proy, SsUsuario usuario) {
        if (proy != null
                && proy.getProySituacionActual() != null
                && proy.getProyectoOriginal() != null) {

            int eq = proy.getProyectoOriginal().getProySituacionActual() == null ? -1 : proy.getProySituacionActual().trim().toLowerCase().compareTo(proy.getProyectoOriginal().getProySituacionActual().trim().toLowerCase());
            if (eq != 0) {
                if (!StringsUtils.isEmpty(proy.getProySituacionActual())
                        && proy.getProySituacionActual().length() > 4000) {
                    BusinessException be = new BusinessException();
                    be.addError(MensajesNegocio.ERROR_FICHA_SITUACION_ACT_LARGO);
                    throw be;
                }

                ProySitactHistorico proySitActH = new ProySitactHistorico();
                proySitActH.setProySitactDesc(proy.getProySituacionActual());
                proySitActH.setProySitactFecha(new Date());
                proySitActH.setProySitactUsuario(usuario);
                proySitActH.setProyectoFk(new Proyectos(proy.getProyPk()));

                ProySitactHistoricoDAO dao = new ProySitactHistoricoDAO(em);
                try {
                    dao.persist(proySitActH, du.getCodigoUsuario(), du.getOrigen());
                } catch (DAOGeneralException ex) {
                    Logger.getLogger(ProyectosBean.class.getName()).log(Level.SEVERE, null, ex);
                    BusinessException be = new BusinessException();
                    be.addError(MensajesNegocio.ERROR_FICHA_HIST_SIT_ACT_GUARDAR);
                    throw be;
                }
            }
        }
    }

    public void guardarLineaBaseYHistorico(Proyectos proy) {
        if (proy != null && proy.getProyectoOriginal() != null
                && proy.getProyCroFk() != null
                && (!proy.getProyectoOriginal().getProyEstFk().isEstado(Estados.ESTADOS.EJECUCION.estado_id)
                && proy.getProyEstFk().isEstado(Estados.ESTADOS.EJECUCION.estado_id))) {

            setearLineaBaseCronograma(proy);
            ProyReplanificacion replan = proyReplanificacionBean.obtenerSolicitudActiva(proy.getProyPk());

            EntHistLineaBaseDAO dao = new EntHistLineaBaseDAO(em);
            Date date = new Date();
            for (Entregables ent : proy.getProyCroFk().getEntregablesSet()) {
                EntHistLineaBase entHistLineaBase = new EntHistLineaBase();
                entHistLineaBase.setEnthistFecha(date);
                entHistLineaBase.setEnthistEntregableFk(ent);
                entHistLineaBase.setEnthistInicioLineaBase(ent.getEntInicio());
                entHistLineaBase.setEnthistFinLineaBase(ent.getEntFin());
                entHistLineaBase.setEnthistDuracion(ent.getEntDuracion());

                if (replan != null && replan.getProyreplanHistorial() && replan.isProyreplanActivo()) {
                    entHistLineaBase.setEnthistReplanFk(replan);
                }

                try {
                    dao.update(entHistLineaBase, du.getCodigoUsuario(), du.getOrigen());
                } catch (DAOGeneralException ex) {
                    Logger.getLogger(ProyectosBean.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public Proyectos guardarProyectoAprobacion(FichaTO fichaTO, SsUsuario usuario, Integer orgPk) throws GeneralException {
        Proyectos proy = (Proyectos) ProgProyUtils.fichaTOToProgProy(fichaTO);
        return guardarProyectoAprobacion(proy, usuario, orgPk);
    }

    public Proyectos guardarProyectoAprobacion(Proyectos proy, SsUsuario usuario, Integer orgPk) throws GeneralException {
        proy.setProyectoOriginal((Proyectos) proy.clone());
        programasProyectosBean.solAprobacionCambioEstado(proy, usuario, orgPk);

        proy = guardarProyecto(proy, usuario, orgPk);

        if (proy != null) {
            if (proy.getProyEstPendienteFk() != null && !proy.getProyEstFk().isPendientes()) {
                String[] ordenUsuarios = new String[]{"usuPrimerNombre", "usuSegundoNombre", "usuPrimerApellido", "usuSegundoApellido"};
                boolean[] ascUsuarios = new boolean[]{true, true, true, true};
                List<SsUsuario> usuariosPMOT = ssUsuarioBean.obtenerUsuariosPorRol(SsRolCodigos.PMO_TRANSVERSAL, orgPk, ordenUsuarios, ascUsuarios);
                String[] destinatarios = SsUsuariosUtils.arrayMailsUsuarios(usuariosPMOT);
                try {
                    mailBean.comunicarSolicitudAprobacion(orgPk, proy, destinatarios);
                } catch (MailException me) {
                    logger.log(Level.SEVERE, me.getMessage(), me);
                }
            }
        }
        return proy;
    }

    private void setearLineaBaseCronograma(Proyectos proy) {
        if (proy.getProyCroFk() != null && proy.getProyCroFk().getEntregablesSet() != null) {
            Cronogramas cro = proy.getProyCroFk();
            for (Entregables ent : cro.getEntregablesSet()) {
                ent.setEntInicioLineaBase(ent.getEntInicio());
                ent.setEntFinLineaBase(ent.getEntFin());
                ent.setEntDuracionLineaBase(ent.getEntDuracion());
            }
        }
    }

    /**
     * Dado un id de un programa retorna todos los proyectos que contenga.
     *
     * @param progPk Id del programa.
     * @return Lista de proyectos.
     */
    public Set<Proyectos> obtenerProyPorProgId(Integer progPk) {
        ProyectosDAO proyDao = new ProyectosDAO(em);
        List<Proyectos> resultado = null;

        try {
            resultado = proyDao.findByOneProperty(Proyectos.class, "proyProgFk.progPk", progPk);

        } catch (BusinessException be) {
            logger.logp(Level.SEVERE, ProyectosBean.class.getName(), "obtenerProyPorProgId", be.getMessage(), be);
            throw be;
        } catch (Exception ex) {
            logger.logp(Level.SEVERE, ProyectosBean.class.getName(), "obtenerProyPorProgId", ex.getMessage(), ex);
            TechnicalException ge = new TechnicalException();
            ge.addError(ex.getMessage());
            throw ge;
        }

        return new HashSet<>(resultado);
    }

    /**
     * Retorna los proyectos cuyo gerente sea el del id aportado.
     *
     * @param usuPk
     * @param orgPk
     * @return List
     */
    public List<Proyectos> obtenerProyPorGerente(Integer usuPk, Integer orgPk) {
        if (usuPk != null && orgPk != null) {
            ProyectosDAO proyDao = new ProyectosDAO(em);

            MatchCriteriaTO criterioOrg = CriteriaTOUtils.createMatchCriteriaTO(
                    MatchCriteriaTO.types.EQUALS, "proyOrgFk.orgPk", orgPk);

            MatchCriteriaTO criterioGerente = CriteriaTOUtils.createMatchCriteriaTO(
                    MatchCriteriaTO.types.EQUALS, "proyUsrGerenteFk.usuId", usuPk);

            CriteriaTO condicion = CriteriaTOUtils.createANDTO(criterioOrg, criterioGerente);

            try {
                String[] orderBy = new String[]{"proyNombre"};
                boolean[] ascending = new boolean[]{true};
                return proyDao.findEntityByCriteria(Proyectos.class, condicion, orderBy, ascending, null, null);
            } catch (DAOGeneralException ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return null;
    }

    public Set<ProyectosConCronograma> obtenerProyectosConCronogramaPorProgId(Integer proyPk) {
        ProyectosConCronogramaDAO proyDao = new ProyectosConCronogramaDAO(em);

        List<ProyectosConCronograma> resultado = null;

        try {
            resultado = proyDao.findByOneProperty(ProyectosConCronograma.class, "proyProgFk", proyPk);

        } catch (BusinessException be) {
            logger.logp(Level.SEVERE, ProyectosBean.class.getName(), "obtenerProyectosConCronogramaPorProgId", be.getMessage(), be);
            throw be;
        } catch (Exception ex) {
            logger.logp(Level.SEVERE, ProyectosBean.class.getName(), "obtenerProyectosConCronogramaPorProgId", ex.getMessage(), ex);
            TechnicalException ge = new TechnicalException();
            ge.addError(ex.getMessage());
            throw ge;
        }

        return new HashSet<>(resultado);
    }

    public List<Proyectos> obtenerProyHermanos(Integer proyPk) {
        ProyectosDAO dao = new ProyectosDAO(em);
        List<Proyectos> list = dao.obtenerProyHermanos(proyPk);
        return list;
    }

    public Interesados guardarInteresado(Interesados i, Integer proyId) {
        Proyectos p = em.find(Proyectos.class, proyId);
        p.getInteresadosList().add(i);
        em.merge(p);

        return i;
    }

    public Proyectos darBajaProyecto(Integer proyId, SsUsuario usuario, Integer orgPk) throws GeneralException {
        Proyectos proy = em.find(Proyectos.class, proyId);

        if (ProgProyUtils.isUsuarioPMOF(proy, usuario, orgPk)) {
            proy.setProyEstPendienteFk(new Estados(Estados.ESTADOS.SOLICITUD_CANCELAR_PMOT.estado_id));
        } else if (usuario.isUsuarioPMOT(orgPk)) {
            proy.setProyEstPendienteFk(null);
            proy.setActivo(false);
        } else {
            throw new BusinessException(MensajesNegocio.ERROR_USUARIO_NO_PERMISO_ACCION);
        }

        try {
            proy = guardar(proy, usuario, orgPk);
            //TODO: Notificar al PM, Adjunto y PMOF
            return proy;
        } catch (GeneralException ge) {
            throw new BusinessException(MensajesNegocio.ERROR_ELIMINAR_PROYECTO);
        }
    }

    public ProyIndices obtenerIndicadores(Integer proyPk, Integer orgPk) {
        ProyIndices p = proyIndicesBean.obtenerIndicePorProyId(proyPk);
        if (p == null) {
            p = guardarIndicadores(proyPk, orgPk);
        }
        return p;
    }

    public ProyIndices guardarIndicadores(Integer proyPk, Integer orgPk) {
        return guardarIndicadores(proyPk, true, true, orgPk);
    }

    /**
     * Guarda los indicadores para el proyecto aportado y todos sus hermanos.
     * Retorna los indicadores del proyecto aportado.
     *
     * @param proyPk
     * @param orgPk
     * @return ProyIndices
     */
    public ProyIndices guardarIndicadoresYHermanos(Integer proyPk, Integer orgPk) {
        List<Proyectos> proyHermanos = obtenerProyHermanos(proyPk);
        ProyIndices pi = null;
        if (proyHermanos != null) {
            for (Proyectos proy : proyHermanos) {
                guardarIndicadores(proy.getProyPk(), orgPk);
            }
        }

        pi = guardarIndicadores(proyPk, orgPk);

        return pi;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void guardarIndicadoresSimple(Integer proyPk, boolean programas, boolean soloFaltantes, Integer orgPk) {
        guardarIndicadores(proyPk, programas, soloFaltantes, orgPk);
    }

    /**
     * Calcula los indicadores y los guarda.
     *
     * @param proyPk
     * @return
     */
    public ProyIndices guardarIndicadores(Integer proyPk, boolean programas, boolean recalcular, Integer orgPk) {
        logger.fine("guardar Indicadores Proyectos.");
        if (proyPk != null) {
            Proyectos p = obtenerProyPorId(proyPk);

            ProyIndices ind = proyIndicesBean.obtenerIndicePorProyId(proyPk);

            if (ind == null) {
                ind = new ProyIndices();
            }
            boolean change = false;

            if (ind.getProyindRiesgoAlto() == null || recalcular) {
                ind.setProyindRiesgoAlto(riesgosBean.obtenerCantRiesgosAltos(proyPk, orgPk));
                change = true;
            }
            if (ind.getProyindRiesgoExpo() == null || recalcular) {
                ind.setProyindRiesgoExpo(riesgosBean.obtenerExposicionRiesgo(proyPk));
                change = true;
            }
            if (ind.getProyindRiesgoUltact() == null || recalcular) {
                ind.setProyindRiesgoUltact(riesgosBean.obtenerDateUltimaActualizacion(proyPk));
                change = true;
            }
            if (ind.getProyindMetodologiaEstado() == null || recalcular) {
                ind.setProyindMetodologiaEstado(documentosBean.calcularIndiceEstadoMetodologiaProyecto(proyPk, p.getProyEstFk(), orgPk));
                change = true;
            }
            if (ind.getProyindMetodologiaSinAprobar() == null || recalcular) {
                ind.setProyindMetodologiaSinAprobar(documentosBean.calcularIndiceMetodologiaSinAprobar(proyPk));
                change = true;
            }
            if (ind.getProyindPeriodoInicio() == null || recalcular) {
                ind.setProyindPeriodoInicio(obtenerPrimeraFecha(p));
                change = true;
            }
            if (ind.getProyindPeriodoFin() == null || recalcular) {
                ind.setProyindPeriodoFin(obtenerUltimaFecha(p));
                change = true;
            }
            if (ind.getProyindPorcPesoTotal() == null || recalcular) {
                ind.setProyindPorcPesoTotal(this.porcentajePesoTotalProyecto(proyPk));
                change = true;
            }
            if (ind.getProyindCalInd() == null || recalcular) {
                ind.setProyindCalInd(calidadBean.calcularIndiceCalidad(proyPk, orgPk));
                change = true;
            }
            if (ind.getProyindPorcPesoTotal() == null || recalcular) {
                ind.setProyindCalPend(calidadBean.obtenerPendCalidad(proyPk));
                change = true;
            }

            try {
                if (change) {
                    ProyIndicesDAO dao = new ProyIndicesDAO(em);
                    ind = dao.update(ind);

                    ProyectosDAO pDao = new ProyectosDAO(em);
                    pDao.updateIndices(proyPk, ind);

                    if (p.getProyProgFk() != null && programas) {
                        programasBean.guardarIndicadores(p.getProyProgFk().getProgPk(), orgPk);
                    }
                }

                return ind;
            } catch (DAOGeneralException ex) {
                Logger.getLogger(ProyectosBean.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

    public Proyectos guardarProyectoRetrocederEstado(Integer proyPk, SsUsuario usuario, Integer orgPk, ProyReplanificacion replanificacion) throws GeneralException {
        Proyectos p = obtenerProyPorId(proyPk);
        Estados est = p.getProyEstFk();
        ProgProyUtils.retrocederEstado(p, usuario, orgPk, replanificacion);
        if (!est.equals(p.getProyEstFk())) {
            p.setProyFechaEstadoAct(new Date());

            List<SsUsuario> usuariosDest = new ArrayList<>();
            usuariosDest.add(p.getProyUsrGerenteFk());
            usuariosDest.add(p.getProyUsrAdjuntoFk());
            String[] destinatarios = SsUsuariosUtils.arrayMailsUsuarios(usuariosDest);
            try {
                mailBean.comunicarCambioEstado(orgPk, p, destinatarios);
            } catch (MailException me) {
                logger.log(Level.SEVERE, me.getMessage(), me);
            }
        }

        return guardar(p, usuario, orgPk);
    }

    public String obtenerUltimaActualizacionColor(Estados estado, Date fechaAct, Integer semaforoAmarillo, Integer semaforoRojo) {
        if (estado.isEstado(Estados.ESTADOS.FINALIZADO.estado_id)) {
            return ConstantesEstandares.SEMAFORO_AZUL;
        }

        if (fechaAct != null && semaforoAmarillo != null && semaforoRojo != null) {
            Calendar calFechaAct = Calendar.getInstance();
            calFechaAct.setTime(fechaAct);

            Calendar calAmarillo = Calendar.getInstance();
            calAmarillo.setTimeInMillis(calFechaAct.getTimeInMillis());
            calAmarillo.add(Calendar.DATE, semaforoAmarillo);

            Calendar calRojo = Calendar.getInstance();
            calRojo.setTimeInMillis(calFechaAct.getTimeInMillis());
            calRojo.add(Calendar.DATE, semaforoRojo);

            Calendar calNow = Calendar.getInstance();
            if (calNow.before(calAmarillo)) {
                return ConstantesEstandares.SEMAFORO_VERDE;
            }
            if (calNow.equals(calAmarillo) || (calNow.after(calAmarillo) && calNow.before(calRojo))) {
                return ConstantesEstandares.SEMAFORO_AMARILLO;
            }
            if (calNow.equals(calRojo) || calNow.after(calRojo)) {
                return ConstantesEstandares.SEMAFORO_ROJO;
            }
        }

        return ConstantesEstandares.COLOR_TRANSPARENT;
    }

    public Date obtenerUltimaActualizacionPrograma(Set<Proyectos> proyectosSet) {
        if (proyectosSet != null && !proyectosSet.isEmpty()) {
            Date result = null;
            boolean tieneProy = false;
            for (Proyectos p : proyectosSet) {
                if ((p.getProyEstFk().isEstado(Estados.ESTADOS.INICIO.estado_id)
                        || p.getProyEstFk().isEstado(Estados.ESTADOS.PLANIFICACION.estado_id)
                        || p.getProyEstFk().isEstado(Estados.ESTADOS.EJECUCION.estado_id))
                        && (result == null || DatesUtils.esMayor(result, p.getProyFechaAct()))) {
                    result = p.getProyFechaAct();
                    tieneProy = true;
                }
            }
            return tieneProy ? result : null;
        }
        return null;
    }

    public boolean isUsuarioGerenteOAdjuntoProy(Proyectos p, SsUsuario u) {
        return p != null && u != null
                && (p.getProyUsrGerenteFk().equals(u)
                || p.getProyUsrAdjuntoFk().equals(u));
    }

    /**
     * @see ProyectosDAO#obtenerPrimeraFecha()
     * @return Date
     */
    public Date obtenerPrimeraFecha() {
        ProyectosDAO dao = new ProyectosDAO(em);
        return dao.obtenerPrimeraFecha();
    }

    /**
     * @see ProyectosDAO#obtenerUltimaFecha()
     * @return Date
     */
    public Date obtenerUltimaFecha() {
        ProyectosDAO dao = new ProyectosDAO(em);
        return dao.obtenerUltimaFecha();
    }

    /**
     * Retorna la primer fecha de un proyecto dado.
     *
     * @param proy
     * @return Date
     */
    private Date obtenerPrimeraFecha(Proyectos proy) {
        Date result = null;
        if (proy != null) {
            result = proy.getProyFechaCrea();
            Date dateAct = proy.getProyFechaAct();
            result = result == null || dateAct != null && DatesUtils.esMayor(result, dateAct) ? dateAct : result;

            //Pagos
            Date prePrimera = presupuestoBean.obtenerPrimeraFechaPre(proy.getProyPreFk());
            result = result == null || prePrimera != null && DatesUtils.esMayor(result, prePrimera) ? prePrimera : result;

            //Riesgos
            if (CollectionsUtils.isNotEmpty(proy.getRiesgosList())) {
                for (Riesgos r : proy.getRiesgosList()) {
                    if (r.getRiskFechaActualizacion() != null
                            && (result == null || DatesUtils.esMayor(result, r.getRiskFechaActualizacion()))) {
                        result = r.getRiskFechaActualizacion();
                    }
                }
            }

            //Gantt
            Set<Entregables> entSet = proy.getProyCroFk() != null && proy.getProyCroFk().getEntregablesSet() != null ? proy.getProyCroFk().getEntregablesSet() : null;
            Date entPrimera = entregablesBean.obtenerPrimeraFecha(entSet);
            result = result == null || entPrimera != null && DatesUtils.esMayor(result, entPrimera) ? entPrimera : result;

            //Devengados
            List<Adquisicion> adqList = adquisicionBean.obtenerAdquisicionPorProy(proy.getProyPk());
            Date dateDev = devengadoBean.obtenerPrimeraFechaPorAdq(adqList, true);
            result = result == null || dateDev != null && DatesUtils.esMayor(result, dateDev) ? dateDev : result;

            //Carga de horas
            Date dateHoras = registrosHorasBean.obtenerPrimeraFecha(proy.getProyPk(), true);
            result = result == null || dateHoras != null && DatesUtils.esMayor(result, dateHoras) ? dateHoras : result;

            //Gastos
            Date dateGastos = gastosBean.obtenerPrimeraFecha(proy.getProyPk(), true);
            result = result == null || dateGastos != null && DatesUtils.esMayor(result, dateGastos) ? dateGastos : result;
        }
        return result;
    }

    /**
     * Retorna la última fecha de un proyecto dado.
     *
     * @param proy
     * @return Date
     */
    private Date obtenerUltimaFecha(Proyectos proy) {
        Date result = null;
        if (proy != null) {
            result = proy.getProyFechaCrea();
            Date dateAct = proy.getProyFechaAct();
            result = result == null || dateAct != null && DatesUtils.esMayor(dateAct, result) ? dateAct : result;

            //Pagos
            Date preUltima = presupuestoBean.obtenerUltimaFechaPre(proy.getProyPreFk());
            result = result == null || preUltima != null ? preUltima : result;

            //Riesgos
            if (CollectionsUtils.isNotEmpty(proy.getRiesgosList())) {
                for (Riesgos r : proy.getRiesgosList()) {
                    if (r.getRiskFechaActualizacion() != null
                            && (result == null || DatesUtils.esMayor(r.getRiskFechaActualizacion(), result))) {
                        result = r.getRiskFechaActualizacion();
                    }
                    if (r.getRiskFechaLimite() != null
                            && (result == null || DatesUtils.esMayor(r.getRiskFechaLimite(), result))) {
                        result = r.getRiskFechaLimite();
                    }
                }
            }

            //Gantt
            Set<Entregables> entSet = proy.getProyCroFk() != null && proy.getProyCroFk().getEntregablesSet() != null ? proy.getProyCroFk().getEntregablesSet() : null;
            Date entUltima = entregablesBean.obtenerUltimaFecha(entSet);
            result = result == null || entUltima != null ? entUltima : result;

            //Devengados
            List<Adquisicion> adqList = adquisicionBean.obtenerAdquisicionPorProy(proy.getProyPk());
            Date dateDev = devengadoBean.obtenerPrimeraFechaPorAdq(adqList, false);
            result = result == null || dateDev != null && DatesUtils.esMayor(dateDev, result) ? dateDev : result;

            //Carga de horas
            Date dateHoras = registrosHorasBean.obtenerPrimeraFecha(proy.getProyPk(), false);
            result = result == null || dateHoras != null && DatesUtils.esMayor(dateHoras, result) ? dateHoras : result;

            //Gastos
            Date dateGastos = gastosBean.obtenerPrimeraFecha(proy.getProyPk(), false);
            result = result == null || dateGastos != null && DatesUtils.esMayor(dateGastos, result) ? dateGastos : result;
        }

        return result;
    }

    public List<Proyectos> obtenerTodos() {
        ProyectosDAO proyDao = new ProyectosDAO(em);
        try {
            return proyDao.findAll(Proyectos.class);
        } catch (DAOGeneralException ex) {
            Logger.getLogger(ProyectosBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public Double porcentajePesoTotalProyecto(Integer proyPk) {
        if (proyPk != null) {
            Proyectos proyecto = this.obtenerProyPorId(proyPk);

            if (proyecto != null && proyecto.isActivo()
                    && (proyecto.getProyEstFk().isEstado(Estados.ESTADOS.INICIO.estado_id)
                    || proyecto.getProyEstFk().isEstado(Estados.ESTADOS.PLANIFICACION.estado_id)
                    || proyecto.getProyEstFk().isEstado(Estados.ESTADOS.EJECUCION.estado_id))) {
                List<Proyectos> setProyectos = obtenerProyHermanos(proyPk);
                if (setProyectos != null && !setProyectos.isEmpty()) {
                    double pesoTotal = 0;
                    for (Proyectos proy : setProyectos) {
                        if (proy != null && proy.isActivo()
                                && proy.getProyPeso() != null
                                && (proy.getProyEstFk().isEstado(Estados.ESTADOS.INICIO.estado_id)
                                || proy.getProyEstFk().isEstado(Estados.ESTADOS.PLANIFICACION.estado_id)
                                || proy.getProyEstFk().isEstado(Estados.ESTADOS.EJECUCION.estado_id))) {
                            pesoTotal += proy.getProyPeso();
                        }
                    }

                    return pesoTotal > 0 ? (proyecto.getProyPeso().floatValue() * 100 / pesoTotal) : 0d;
                }
            } else {
                return 0d;
            }
        }
        return null;
    }

    public List<Integer> obtenerIdsProyPorOrg(Integer orgPk) throws GeneralException {
        ProyectosDAO proyDao = new ProyectosDAO(em);
        List<Integer> proyIds = new ArrayList<>();
        try {

            CriteriaTO criteria = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "proyOrgFk.orgPk", orgPk);

            List<EntityReference<Integer>> proyectosResult = proyDao.findEntityReferenceByCriteria(Proyectos.class, criteria, new String[]{}, new boolean[]{}, null, null, "proyPk");
            for (EntityReference<Integer> proy : proyectosResult) {
                if (proy.getPropertyMap().get("proyPk") != null) {
                    proyIds.add((Integer) proy.getPropertyMap().get("proyPk"));
                }
            }
        } catch (Exception ex) {
            //Las demás excepciones se consideran técnicas
            logger.logp(Level.SEVERE, ProyectosBean.class.getName(), "obtenerProyPorId", ex.getMessage(), ex);
            TechnicalException ge = new TechnicalException();
            ge.addError(ex.getMessage());
            throw ge;
        }

        return proyIds;
    }

    public List<Proyectos> obtenerActivosPorOrg(Integer orgPk) {
        ProyectosDAO proyDao = new ProyectosDAO(em);

        MatchCriteriaTO criterioOrg = CriteriaTOUtils.createMatchCriteriaTO(
                MatchCriteriaTO.types.EQUALS, "proyOrgFk.orgPk", orgPk);

        MatchCriteriaTO criterioAct1 = CriteriaTOUtils.createMatchCriteriaTO(
                MatchCriteriaTO.types.EQUALS, "activo", Boolean.TRUE);

        MatchCriteriaTO criterioAct2 = CriteriaTOUtils.createMatchCriteriaTO(
                MatchCriteriaTO.types.NULL, "activo", 1);

        CriteriaTO criterioAct = CriteriaTOUtils.createORTO(criterioAct1, criterioAct2);

        CriteriaTO condicion = CriteriaTOUtils.createANDTO(criterioOrg, criterioAct);

        try {
            String[] orderBy = new String[]{"proyNombre"};
            boolean[] ascending = new boolean[]{true};
            return proyDao.findEntityByCriteria(Proyectos.class, condicion, orderBy, ascending, null, null);
        } catch (DAOGeneralException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }
        return null;
    }

    public List<Proyectos> obtenerProyComboPorOrg(Integer orgPk) {
        ProyectosDAO proyDao = new ProyectosDAO(em);
        return proyDao.obtenerProyComboPorOrg(orgPk);
    }

    public void controlarEntregables(Integer proyPk, boolean resetLineaBase) {
        Proyectos proy = obtenerProyPorId(proyPk);
        if (proy != null && proy.getProyCroFk() != null && CollectionsUtils.isNotEmpty(proy.getProyCroFk().getEntregablesSet())) {
            List<Entregables> entResult = recalcularEntregablesPadres(proy.getProyCroFk().getEntregablesSet());

            for (Entregables ent : entResult) {
                if (proy.getProyEstFk().isEstado(Estados.ESTADOS.EJECUCION.estado_id)
                        || proy.getProyEstFk().isEstado(Estados.ESTADOS.FINALIZADO.estado_id)) {
                    if (ent.getEntInicioLineaBase() == null || ent.getEntInicioLineaBase() <= 0) {
                        ent.setEntInicioLineaBase(ent.getEntInicio());
                    }
                    if (ent.getEntFinLineaBase() == null || ent.getEntFinLineaBase() <= 0) {
                        ent.setEntFinLineaBase(ent.getEntFin());
                    }
                    if (ent.getEntFinLineaBase().equals(ent.getEntInicioLineaBase())) {
                        ent.setEntDuracionLineaBase(1);
                    } else if (ent.getEntFinLineaBase() > ent.getEntInicioLineaBase()) {
                        int duraLineaBase = Math.round((ent.getEntFinLineaBase() - ent.getEntInicioLineaBase()) / 86400000);
                        ent.setEntDuracionLineaBase(duraLineaBase);
                    }

                    if (resetLineaBase) {
                        ent.setEntInicioLineaBase(ent.getEntInicio());
                        ent.setEntFinLineaBase(ent.getEntFin());
                        ent.setEntDuracionLineaBase(ent.getEntDuracion());
                    }
                } else {
                    //si esta en otros estado entonces la linea base se limpia
                    ent.setEntInicioLineaBase(0l);
                    ent.setEntFinLineaBase(0l);
                    ent.setEntDuracionLineaBase(0);
                }

                if (ent.getEntInicio() != null && ent.getEntInicio() > 0
                        && ent.getEntFin() != null && ent.getEntFin() > 0) {
                    ent.setEntDuracion(DatesUtils.diasEntreFechas(ent.getEntInicioDate(), ent.getEntFinDate()));
                }

                if (productosBean.tieneProdPorEnt(ent.getEntPk())) {
                    Double avance = entregablesBean.calcularAvanceEntPorProd(ent.getEntPk());
                    if (avance != null) {
                        Long progreso = Math.round(avance);
                        progreso = progreso > 100 ? 100 : progreso;
                        ent.setEntProgreso(progreso.intValue());
                    }
                }

                entregablesBean.guardar(ent);
            }
        }
    }

    public List<Entregables> recalcularEntregablesPadres(Set<Entregables> entSet) {
        List<Entregables> entResult = new ArrayList<>();

        if (CollectionsUtils.isNotEmpty(entSet)) {
            List<Entregables> entList = new ArrayList<>(entSet);
            Entregables[] entArr = entSet.toArray(new Entregables[entSet.size()]);

            int nivelMax = 0;
            for (Entregables ent : entList) {
                if (ent.getEntNivel() > nivelMax) {
                    nivelMax = ent.getEntNivel();
                }
            }

            //Se ordena por id.
            for (int i = 0; i < entArr.length; i++) {
                for (int j = i + 1; j < entArr.length; j++) {
                    if (entArr[i].getEntId() > entArr[j].getEntId()) {
                        Entregables ent = entArr[j];
                        entArr[j] = entArr[i];
                        entArr[i] = ent;
                    }
                }
            }

            for (int a = nivelMax; a >= 0; a--) {
                for (int b = 0; b < entArr.length; b++) {
                    if (entArr[b].getEntNivel().equals(a - 1)) {
                        boolean seguir = true;
                        boolean padre = false;
                        long fechaMenor = 0;
                        long fechaMayor = 0;
                        for (int c = b + 1; seguir && c < entArr.length; c++) {
                            if (entArr[c].getEntNivel().equals(entArr[b].getEntNivel() + 1)
                                    || entArr[c].getEntNivel() > entArr[b].getEntNivel()) {
                                if (fechaMenor == 0 || entArr[c].getEntInicio() < fechaMenor) {
                                    fechaMenor = entArr[c].getEntInicio();
                                }
                                if (entArr[c].getEntFin() > fechaMayor) {
                                    fechaMayor = entArr[c].getEntFin();
                                }
                                padre = true;
                            } else {
                                seguir = false;
                            }
                        }

                        if (padre) {
                            if (fechaMenor > 0) {
                                entArr[b].setEntInicio(fechaMenor);
                            }
                            if (fechaMayor > 0) {
                                entArr[b].setEntFin(fechaMayor);
                            }
                            entArr[b].setEntEsfuerzo(0);
                            entArr[b].setEntProgreso(0);
                        }
                    }
                }
            }

            for (int i = 0; i < entArr.length; i++) {
                Integer difDias = DatesUtils.diasEntreFechas(entArr[i].getEntInicioDate(), entArr[i].getEntFinDate());
                if (difDias != null) {
                    entArr[i].setEntDuracion(difDias);
                }
            }
            Collections.addAll(entResult, entArr);
        }
        return entResult;
    }

    public Proyectos guardarCopiaProyecto(Integer proyPk, String nombre, Date fechaComienzoProyCopia, SsUsuario usu, Integer orgPk) {
        if (StringsUtils.isEmpty(nombre)) {
            BusinessException be = new BusinessException();
            be.addError(MensajesNegocio.ERROR_COPIA_PROY_NOMBRE);
            throw be;
        }
        if (fechaComienzoProyCopia == null) {
            BusinessException be = new BusinessException();
            be.addError(MensajesNegocio.ERROR_COPIA_PROY_FECHA);
            throw be;
        }
        if (orgPk == null) {
            BusinessException be = new BusinessException();
            be.addError(MensajesNegocio.ERROR_COPIA_PROY_ORG);
            throw be;
        }

        Proyectos p = obtenerProyPorId(proyPk);
        if (p == null) {
            BusinessException be = new BusinessException();
            be.addError(MensajesNegocio.ERROR_COPIA_PROY_NULL);
            throw be;
        }

        Proyectos copiaProy = copiarProyecto(p, nombre, fechaComienzoProyCopia);
        copiaProy = guardar(copiaProy, usu, orgPk);

        //Metodología
        tipoDocumentoInstanciaBean.guardarCopiaTDIProyecto(proyPk, copiaProy.getProyPk(), orgPk);

        //Notificaciones
        notificacionInstanciaBean.guardarCopiaNIProyecto(proyPk, copiaProy.getProyPk(), orgPk);

        //Participantes
        return copiaProy;
    }

    public Proyectos copiarProyecto(Proyectos proy, String nombre, Date fechaComienzoProyCopia) {
        if (proy != null) {
            Integer difFechas = DatesUtils.diasEntreFechas(proy.getProyFechaCrea(), fechaComienzoProyCopia);
            int desfasajeDias = difFechas != null ? difFechas : 0;

            Proyectos nvoProy = (Proyectos) proy.clone();

            nvoProy.setProyPk(null);
            nvoProy.setProyNombre(nombre);
            nvoProy.setProyEstFk(new Estados(Estados.ESTADOS.INICIO.estado_id));
            nvoProy.setProyEstPendienteFk(null);
            Date date = new Date();
            nvoProy.setProyFechaCrea(date);
            nvoProy.setProyFechaAct(date);
            nvoProy.setProyFechaEstadoAct(null);
            nvoProy.setActivo(Boolean.TRUE);
            nvoProy.setProySituacionActual(null);
            nvoProy.setProyGrp(null);
            nvoProy.setProyIndices(null);

            //Audit
            nvoProy.setProyUltUsuario(null);
            nvoProy.setProyUltMod(null);
            nvoProy.setProyUltOrigen(null);
            nvoProy.setProyVersion(0);

            //Colecciones
            nvoProy.setAreasTematicasSet(proy.getAreasTematicasSet());
            nvoProy.setAreasRestringidasSet(proy.getAreasRestringidasSet());

            nvoProy.setDocumentosSet(null);

            nvoProy.setInteresadosList(interesadosBean.copiarProyInteresados(proy.getInteresadosList()));
            nvoProy.setRiesgosList(riesgosBean.copiarProyRiesgos(proy.getRiesgosList(), nvoProy, desfasajeDias));

            Cronogramas cro = proy.getProyCroFk();
            Date entComienzo = entregablesBean.obtenerPrimeraFecha(cro != null ? cro.getEntregablesSet() : null);
            Integer difFechaEnt = DatesUtils.diasEntreFechas(entComienzo, fechaComienzoProyCopia);
            int desfasajeDiasEnt = difFechaEnt != null ? difFechaEnt : 0;
            nvoProy.setProyCroFk(cronogramasBean.copiarProyCronograma(cro, desfasajeDiasEnt));
            Cronogramas croNvo = nvoProy.getProyCroFk();
            nvoProy.setProyPreFk(presupuestoBean.copiarProyPresupuesto(proy.getProyPreFk(), (croNvo != null ? croNvo.getEntregablesSet() : null), desfasajeDias));

            return nvoProy;
        }
        return null;
    }

    /**
     * Retorna true si el proyecto tiene solicitud de cambio de estado.
     *
     * @param progPk
     * @return Boolean
     */
    public boolean cambioEstadoPorProg(Integer progPk) {
        ProyectosDAO dao = new ProyectosDAO(em);
        return dao.cambioEstadoPorProg(progPk);
    }

    private boolean tieneCambiosConfPMOT(Proyectos proy, Proyectos proyPersistido) {
        boolean tieneCambios = false;

        if (proy != null && proyPersistido != null) {
            tieneCambios = tieneCambios ? tieneCambios : proyPersistido.getProyAreaFk() != null && proy.getProyAreaFk() != null && !proyPersistido.getProyAreaFk().equals(proy.getProyAreaFk());
            tieneCambios = tieneCambios ? tieneCambios : proyPersistido.getProyProgFk() != null && proy.getProyProgFk() != null && !proyPersistido.getProyProgFk().equals(proy.getProyProgFk());
            tieneCambios = tieneCambios ? tieneCambios : proyPersistido.getProyPeso() != null && proy.getProyPeso() != null && !proyPersistido.getProyPeso().equals(proy.getProyPeso());
            tieneCambios = tieneCambios ? tieneCambios : proyPersistido.getProyUsrGerenteFk() != null && proy.getProyUsrGerenteFk() != null && !proyPersistido.getProyUsrGerenteFk().equals(proy.getProyUsrGerenteFk());
            tieneCambios = tieneCambios ? tieneCambios : proyPersistido.getProyUsrAdjuntoFk() != null && proy.getProyUsrAdjuntoFk() != null && !proyPersistido.getProyUsrAdjuntoFk().equals(proy.getProyUsrAdjuntoFk());
            tieneCambios = tieneCambios ? tieneCambios : proyPersistido.getProyUsrSponsorFk() != null && proy.getProyUsrSponsorFk() != null && !proyPersistido.getProyUsrSponsorFk().equals(proy.getProyUsrSponsorFk());
            tieneCambios = tieneCambios ? tieneCambios : proyPersistido.getProyUsrPmofedFk() != null && proy.getProyUsrPmofedFk() != null && !proyPersistido.getProyUsrPmofedFk().equals(proy.getProyUsrPmofedFk());
            tieneCambios = tieneCambios ? tieneCambios : proyPersistido.getProySemaforoAmarillo() != null && proy.getProySemaforoAmarillo() != null && !proyPersistido.getProySemaforoAmarillo().equals(proy.getProySemaforoAmarillo());
            tieneCambios = tieneCambios ? tieneCambios : proyPersistido.getProySemaforoRojo() != null && proy.getProySemaforoRojo() != null && !proyPersistido.getProySemaforoRojo().equals(proy.getProySemaforoRojo());

            //Area tematica
            List<AreasTags> listATproy = areaTematicaBean.obtenerAreasTematicasPorFichaPk(proy.getProyPk(), TipoFichaEnum.PROYECTO.id);
            List<AreasTags> listATproyPersit = areaTematicaBean.obtenerAreasTematicasPorFichaPk(proyPersistido.getProyPk(), TipoFichaEnum.PROYECTO.id);
            if (listATproyPersit != null
                    && listATproy != null
                    && (listATproyPersit.size() != listATproy.size()
                    || !listATproyPersit.containsAll(listATproy))) {
                return true;
            }

            //Lectura
            List<Areas> listAreaProy = areasBean.obtenerAreasRestringidasPorFichaPk(proy.getProyPk(), TipoFichaEnum.PROYECTO.id);
            List<Areas> listAreaProyPrestist = areasBean.obtenerAreasRestringidasPorFichaPk(proyPersistido.getProyPk(), TipoFichaEnum.PROYECTO.id);
            if (listAreaProyPrestist != null
                    && listAreaProy != null
                    && (listAreaProyPrestist.size() != listAreaProy.size()
                    || !listAreaProyPrestist.containsAll(listAreaProy))) {
                return true;
            }
            //Metodología y Notificaciones no es necesario porque se guardan independientemente del proyecto.
        }
        return tieneCambios;
    }

    public Integer porcentajeAvanceEnTiempo(Integer proyPk) {
        ProyIndices proyInd = proyIndicesBean.obtenerIndicePorProyId(proyPk);

        if (proyInd.getProyindPeriodoInicio() != null && proyInd.getProyindPeriodoFin() != null) {
            Calendar inicio = new GregorianCalendar();
            inicio.setTime(proyInd.getProyindPeriodoInicio());
            Calendar fin = new GregorianCalendar();
            fin.setTime(proyInd.getProyindPeriodoFin());
            Calendar calNow = new GregorianCalendar();

            if (DatesUtils.esMayor(inicio.getTime(), fin.getTime())) {
                return null;
            }
            if (calNow.before(inicio)) {
                return 0;
            } else if (calNow.after(fin)) {
                return 100;
            }

            if (DatesUtils.fechasIguales(inicio.getTime(), fin.getTime()) && DatesUtils.fechasIguales(calNow.getTime(), inicio.getTime())) {
                return 50;
            }

            Integer diffProy = DatesUtils.diasEntreFechas(inicio.getTime(), fin.getTime());
            Integer diffNow = DatesUtils.diasEntreFechas(inicio.getTime(), calNow.getTime());
            return diffNow * 100 / diffProy;

        }
        return null;
    }

    public List<Proyectos> obtenerPorUsuarioParticipanteActivo(Integer usuId, Integer orgPk) {
        ProyectosDAO dao = new ProyectosDAO(em);

        MatchCriteriaTO criteriaOrg = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "proyOrgFk.orgPk", orgPk);
        MatchCriteriaTO criteriaUsu = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "participantesList.partUsuarioFk.usuId", usuId);
        MatchCriteriaTO criteriaActivo = CriteriaTOUtils.createMatchCriteriaTO(MatchCriteriaTO.types.EQUALS, "participantesList.partActivo", Boolean.TRUE);
        CriteriaTO criteria = CriteriaTOUtils.createANDTO(criteriaOrg, criteriaUsu, criteriaActivo);

        String[] orderBy = {};
        boolean[] asc = {};

        try {
            List<Proyectos> listPart = dao.findEntityByCriteria(Proyectos.class, criteria, orderBy, asc, null, null);
            return listPart;
        } catch (BusinessException be) {
            throw be;
        } catch (DAOGeneralException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            BusinessException be = new BusinessException();
            be.addError(ex.getMessage());
            throw be;
        }
    }

    public void controlarProdAcumulados(Integer proyPk) {
        List<Productos> listProd = productosBean.obtenerProdPorProyPk(proyPk);
        for (Productos prod : listProd) {
            if (prod.isProdAcu()) {
                if (prod.getProdMesList() != null) {
                    boolean primero = true;
                    Double plan = 0D;
                    Double real = 0D;
                    double planAnterior = 0;
                    double realAnterior = 0;
                    List<ProdMes> listProdMes = prodMesBean.obtenerOrdenadoPorFecha(prod.getProdPk());
                    for (ProdMes prodMes : listProdMes) {
                        if (primero) {
                            planAnterior = prodMes.getProdmesAcuPlan();
                            realAnterior = prodMes.getProdmesAcuReal();
                            prodMes.setProdmesPlan(planAnterior);
                            prodMes.setProdmesReal(realAnterior);
                            primero = false;
                        } else {
                            plan = prodMes.getProdmesAcuPlan() - planAnterior;
                            real = prodMes.getProdmesAcuReal() - realAnterior;
                            plan = plan < 0 ? 0 : plan;
                            real = real < 0 ? 0 : real;
                            prodMes.setProdmesPlan(plan);
                            prodMes.setProdmesReal(real);
                            planAnterior += plan;
                            realAnterior += real;
                        }
                    }
                }
            } else {
                prod = productosBean.calcularAcumulados(prod);
            }
            productosBean.guardarProducto(prod);
        }
    }
}