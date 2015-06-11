package com.sofis.business.ejbs;

import com.sofis.business.interceptors.LoggedInterceptor;
import com.sofis.business.utils.EntregablesUtils;
import com.sofis.data.daos.EntregablesDAO;
import com.sofis.entities.constantes.ConstanteApp;
import com.sofis.entities.constantes.MensajesNegocio;
import com.sofis.entities.data.Cronogramas;
import com.sofis.entities.data.Entregables;
import com.sofis.entities.data.ProdMes;
import com.sofis.entities.data.Productos;
import com.sofis.entities.data.Proyectos;
import com.sofis.entities.data.SsUsuario;
import com.sofis.entities.tipos.FiltroMisTareasTO;
import com.sofis.entities.tipos.MisTareasTO;
import com.sofis.entities.tipos.ReporteAcumuladoMesTO;
import com.sofis.entities.utils.SsUsuariosUtils;
import com.sofis.exceptions.BusinessException;
import com.sofis.generico.utils.generalutils.CollectionsUtils;
import com.sofis.generico.utils.generalutils.DatesUtils;
import com.sofis.persistence.dao.exceptions.DAOGeneralException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
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
@Stateless(name = "EntregablesBean")
@LocalBean
@Interceptors({LoggedInterceptor.class})
public class EntregablesBean {

    @PersistenceContext(unitName = ConstanteApp.PERSISTENCE_CONTEXT_UNIT_NAME)
    private EntityManager em;
    private static final Logger logger = Logger.getLogger(ConstanteApp.LOGGER_NAME);
    @EJB
    private ProductosBean productosBean;
    @EJB
    private ProyectosBean proyectosBean;

    public Entregables obtenerEntPorPk(Integer entPk) {
        return obtenerEntPorPk(entPk, false, false, false, false, false);
    }

    public Entregables obtenerEntPorPk(Integer entPk, boolean cargarProd, boolean cargarHistLB, boolean cargarDoc, boolean cargarPart, boolean cargarRegHoras) {
        EntregablesDAO dao = new EntregablesDAO(em);
        try {
            Entregables result = dao.findById(Entregables.class, entPk);

            if (cargarProd) {
                result.getEntProductosSet().isEmpty();
            }
            if (cargarHistLB) {
                result.getEntHistLBSet().isEmpty();
            }
            if (cargarDoc) {
                result.getEntDocumentosSet().isEmpty();
            }
            if (cargarPart) {
                result.getEntParticipantesSet().isEmpty();
            }
            if (cargarRegHoras) {
                result.getEntRegistrosHorasSet().isEmpty();
            }

            return result;
        } catch (DAOGeneralException ex) {
            Logger.getLogger(EntregablesBean.class.getName()).log(Level.SEVERE, null, ex);
            BusinessException be = new BusinessException();
            be.addError(MensajesNegocio.ERROR_ENTREGABLES_OBTENER);
            throw be;
        }
    }

    public List<Entregables> obtenerEntPorProyPk(Integer proyPk) {
        EntregablesDAO dao = new EntregablesDAO(em);
        return dao.obtenerEntPorProyPk(proyPk);
    }

    /**
     * Controla si está siendo usado por RegistrosHoras, Pagos, Participantes,
     * Productos, Documentos, Riesgos, Calidad o Interesados.
     *
     * @param entPk
     * @return boolean
     */
    public boolean tieneDependencias(Integer entPk) {
        EntregablesDAO dao = new EntregablesDAO(em);
        return dao.tieneDependencias(entPk);
    }

    public Entregables guardar(Entregables ent) {
        EntregablesDAO dao = new EntregablesDAO(em);
        try {
            return dao.update(ent);
        } catch (DAOGeneralException ex) {
            Logger.getLogger(EntregablesBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public List<Entregables> obtenerEntPorCoord(Integer proyPk, Integer coord) {
        if (proyPk != null) {
            EntregablesDAO dao = new EntregablesDAO(em);
            dao.obtenerEntregablesPorCoord(proyPk, coord);
        }
        return null;
    }

    public Double calcularAvanceEntPorProd(Integer entPk) {
        if (entPk != null) {
            List<Productos> listProd = productosBean.obtenerProdPorEnt(entPk);
            if (CollectionsUtils.isNotEmpty(listProd)) {
                Calendar cal = new GregorianCalendar();
                int pesoPlanTotal = 0;
                double avancePesoTotal = 0;
                for (Productos prod : listProd) {
                    double totalPlan = 0;
                    double avanceReal = 0;
                    for (ProdMes prodMes : prod.getProdMesList()) {
                        if (prodMes.getProdmesReal() != null) {
                            if (prodMes.getProdmesAnio() < cal.get(Calendar.YEAR)
                                    || (prodMes.getProdmesAnio() == cal.get(Calendar.YEAR)
                                    && prodMes.getProdmesMes() <= cal.get(Calendar.MONTH) + 1)) {
                                avanceReal += prodMes.getProdmesReal();
                            }
                        }
                        totalPlan += prodMes.getProdmesPlan();
                    }
                    pesoPlanTotal += totalPlan * prod.getProdPeso();
                    avancePesoTotal += avanceReal * prod.getProdPeso();
                }

                return pesoPlanTotal > 0 ? avancePesoTotal * 100 / pesoPlanTotal : 0d;
            }
        }
        return null;
    }

    public void guardarAvanceReal(Integer entPk) {
        Entregables ent = obtenerEntPorPk(entPk);
        if (ent != null) {
            Double avance = calcularAvanceEntPorProd(entPk);
            if (avance != null) {
                int progreso = (int) Math.round(avance);
                progreso = progreso > 100 ? 100 : progreso;
                ent.setEntProgreso(progreso);
                guardar(ent);
            }
        }
    }

    public Set<Entregables> copiarProyEntregables(Set<Entregables> entregablesSet, Cronogramas nvoCro, int desfasajeDias) {
        if (CollectionsUtils.isNotEmpty(entregablesSet) && nvoCro != null) {
            Set<Entregables> result = new HashSet<>();
            for (Entregables ent : entregablesSet) {
                Entregables nvoEnt = new Entregables();
                nvoEnt.setCoordinadorUsuFk(ent.getCoordinadorUsuFk());
                nvoEnt.setEntAssigs(ent.getEntAssigs());
                nvoEnt.setEntCodigo(ent.getEntCodigo());
                nvoEnt.setEntCollapsed(ent.getEntCollapsed());
                nvoEnt.setEntCroFk(nvoCro);
                nvoEnt.setEntDescripcion(ent.getEntDescripcion());
                nvoEnt.setEntDuracion(ent.getEntDuracion());
                nvoEnt.setEntDuracionLineaBase(ent.getEntDuracionLineaBase());
                nvoEnt.setEntEsfuerzo(ent.getEntEsfuerzo());
                nvoEnt.setEntFinEsHito(ent.getEntFinEsHito());
                nvoEnt.setEntHorasEstimadas(ent.getEntHorasEstimadas());
                nvoEnt.setEntId(ent.getEntId());
                nvoEnt.setEntInicioEsHito(ent.getEntInicioEsHito());
                nvoEnt.setEntNivel(ent.getEntNivel());
                nvoEnt.setEntNombre(ent.getEntNombre());
                nvoEnt.setEntPredecesorDias(ent.getEntPredecesorDias());
                nvoEnt.setEntPredecesorFk(ent.getEntPredecesorFk());
                nvoEnt.setEntProductosSet(productosBean.copiarProyProductos(ent, nvoEnt, desfasajeDias));
                nvoEnt.setEntProgreso(0);
                nvoEnt.setEntStatus(ent.getEntStatus());

                Date date;
                if (ent.getEntFin() != null && ent.getEntFin() > 0) {
                    date = DatesUtils.incrementarDias(ent.getEntFinDate(), desfasajeDias);
                    nvoEnt.setEntFin(date.getTime());
                }
                if (ent.getEntFinLineaBase() != null && ent.getEntFinLineaBase() > 0) {
                    date = DatesUtils.incrementarDias(ent.getEntFinLineaBaseDate(), desfasajeDias);
                    nvoEnt.setEntFinLineaBase(date.getTime());
                }
                if (ent.getEntInicio() != null && ent.getEntInicio() > 0) {
                    date = DatesUtils.incrementarDias(ent.getEntInicioDate(), desfasajeDias);
                    nvoEnt.setEntInicio(date.getTime());
                }
                if (ent.getEntInicioLineaBase() != null && ent.getEntInicioLineaBase() > 0) {
                    date = DatesUtils.incrementarDias(ent.getEntInicioLineaBaseDate(), desfasajeDias);
                    nvoEnt.setEntInicioLineaBase(date.getTime());
                }

                result.add(nvoEnt);
            }
            return result;
        }
        return null;
    }

    public Date obtenerPrimeraFecha(Collection<Entregables> entSet) {
        Date result = null;
        if (CollectionsUtils.isNotEmpty(entSet)) {
            for (Entregables e : entSet) {
                if (e.getEntInicioDate() != null
                        && (result == null || DatesUtils.esMayor(result, e.getEntInicioDate()))) {
                    result = e.getEntInicioDate();

                }
                if (e.getEntInicioLineaBaseDate() != null
                        && (result == null || DatesUtils.esMayor(result, e.getEntInicioLineaBaseDate()))) {
                    result = e.getEntInicioLineaBaseDate();
                }
            }
        }
        return result;
    }

    public Date obtenerUltimaFecha(Collection<Entregables> entregablesSet) {
        Date result = null;
        if (CollectionsUtils.isNotEmpty(entregablesSet)) {
            for (Entregables e : entregablesSet) {
                if (e.getEntFinDate() != null
                        && (result == null || DatesUtils.esMayor(e.getEntFinDate(), result))) {
                    result = e.getEntFinDate();
                }
                if (e.getEntFinLineaBaseDate() != null
                        && (result == null || DatesUtils.esMayor(e.getEntFinLineaBaseDate(), result))) {
                    result = e.getEntFinLineaBaseDate();
                }
            }
        }
        return result;
    }

    public Entregables actualizarAvance(Integer proyPk, Integer entPk, Integer avance, SsUsuario usuario) {
        if (entPk != null && avance != null) {
            Entregables ent = obtenerEntPorPk(entPk);
            if (ent != null) {
                Proyectos proy = proyectosBean.obtenerProyPorId(proyPk);
                boolean isGerente = SsUsuariosUtils.isUsuarioGerenteOAdjuntoFicha(proy, usuario);
                if (isGerente || (ent.getCoordinadorUsuFk() != null && ent.getCoordinadorUsuFk().equals(usuario))) {
                    ent.setEntProgreso(avance);
                    return guardar(ent);
                } else {
                    BusinessException be = new BusinessException();
                    be.addError(MensajesNegocio.ERROR_ENTREGABLE_AVANCE_COORD);
                    throw be;
                }
            }
        }
        return null;
    }

    /**
     *
     * @param listEnt
     * @return Map cuya clave es 5-2015, o sea mes-aÃ±o.
     */
    public Map<String, ReporteAcumuladoMesTO> obtenerAcumuladoMapMes(List<Entregables> listEnt) {
        Map<String, ReporteAcumuladoMesTO> result = new HashMap<>();

        List<ReporteAcumuladoMesTO> acuMes = obtenerAcumuladoCroPorMes(listEnt);
        for (ReporteAcumuladoMesTO acu : acuMes) {
            String clave = acu.getMes() + "-" + acu.getAnio();
            result.put(clave, acu);
        }

        return result;
    }

    /**
     *
     * @param listEnt
     * @return List
     */
    public List<ReporteAcumuladoMesTO> obtenerAcumuladoCroPorMes(List<Entregables> listEnt) {
        if (CollectionsUtils.isNotEmpty(listEnt)) {
            List<ReporteAcumuladoMesTO> result = new ArrayList<>();
            Map<String, Double> entPorMes = new HashMap<>();
            Calendar primera = new GregorianCalendar();
            Calendar ultima = new GregorianCalendar();
            Calendar cal;
            int mes;
            int anio;
            Double entEsfProg;
            Double entEsfProgTotal;
            Double entEsf;

            List<Entregables> listE = EntregablesUtils.entregablesSinPadres(listEnt);

            double esfuerzoTotal = 0;
            boolean sinLineaBase = false;
            for (Entregables ent : listE) {
                esfuerzoTotal += ent.getEntEsfuerzo();
                if (!sinLineaBase && (ent.getEntInicioLineaBase() == null || ent.getEntFinLineaBase() == null
                        || ent.getEntInicioLineaBase() <= 0 || ent.getEntFinLineaBase() <= 0)) {
                    //Para saber si todos tienen linea base.
                    sinLineaBase = true;
                }
            }

            for (Entregables ent : listE) {
                cal = null;
                entEsfProg = 0D;
                entEsfProgTotal = 0D;
                entEsf = 0D;
                if (ent.getEntEsfuerzo() != null && ent.getEntProgreso() != null) {
                    entEsfProg = ent.getEntEsfuerzo() * ent.getEntProgreso() / esfuerzoTotal;
                    entEsfProgTotal = ent.getEntEsfuerzo() * 100 / esfuerzoTotal;
                    entEsf = ent.getEntEsfuerzo() * 100 / esfuerzoTotal;
                }

                if (sinLineaBase) {
                    if (ent.getEntFinDate() != null) {
                        cal = new GregorianCalendar();
                        cal.setTime(ent.getEntFinDate());
                    }
                } else {
                    if (ent.getEntFinLineaBaseDate() != null) {
                        cal = new GregorianCalendar();
                        cal.setTime(ent.getEntFinLineaBaseDate());
                    }
                }

                //Calculo lo Planificado
                if (cal != null) {
                    mes = cal.get(Calendar.MONTH) + 1;
                    anio = cal.get(Calendar.YEAR);

                    if (DatesUtils.esMayor(primera.getTime(), cal.getTime())) {
                        primera = cal;
                    }
                    if (DatesUtils.esMayor(cal.getTime(), ultima.getTime())) {
                        ultima = cal;
                    }

                    String claveBase = "0-" + mes + "-" + anio;
                    if (entPorMes.containsKey(claveBase)) {
                        entPorMes.put(claveBase, entPorMes.get(claveBase) + entEsf);
                    } else {
                        entPorMes.put(claveBase, entEsf);
                    }
                }

                //Calculo lo real y lo proyectado.
                if (ent.getEntInicioDate() != null && ent.getEntFinDate() != null) {
                    cal.setTime(ent.getEntFinDate());
                    mes = cal.get(Calendar.MONTH) + 1;
                    anio = cal.get(Calendar.YEAR);

                    if (primera == null || DatesUtils.esMayor(primera.getTime(), cal.getTime())) {
                        primera = cal;
                    }
                    if (ultima == null || DatesUtils.esMayor(cal.getTime(), ultima.getTime())) {
                        ultima = cal;
                    }

                    if (ent.getEntProgreso() >= 100) {
                        String claveReal = "1-" + mes + "-" + anio;
                        if (entPorMes.containsKey(claveReal)) {
                            entPorMes.put(claveReal, entPorMes.get(claveReal) + entEsfProg);
                        } else {
                            entPorMes.put(claveReal, entEsfProg);
                        }
                    } else {
                        String claveProy = "2-" + mes + "-" + anio;
                        if (entPorMes.containsKey(claveProy)) {
                            entPorMes.put(claveProy, entPorMes.get(claveProy) + entEsfProgTotal);
                        } else {
                            entPorMes.put(claveProy, entEsfProgTotal);
                        }
                    }
                }
            }

            double baseAcu = 0;
            double realAcu = 0;
            double proyAcu = 0;
            Double baseMes;
            Double realMes;
            Double proyMes;
            String claveBase;
            String claveReal;
            String claveProy;

            cal = new GregorianCalendar();
            int mesHoy = cal.get(Calendar.MONTH) + 1;
            int anioHoy = cal.get(Calendar.YEAR);

            int mesPrimera = primera.get(Calendar.MONTH);
            int anioPrimera = primera.get(Calendar.YEAR);
            int mesUltima = ultima.get(Calendar.MONTH);
            int anioUltima = ultima.get(Calendar.YEAR);

            while (anioUltima > anioPrimera || (anioUltima == anioPrimera && mesUltima >= mesPrimera)) {
                mes = mesPrimera + 1;
                anio = anioPrimera;

                claveBase = "0-" + mes + "-" + anio;
                claveReal = "1-" + mes + "-" + anio;
                claveProy = "2-" + mes + "-" + anio;

                ReporteAcumuladoMesTO repAcu = new ReporteAcumuladoMesTO((short) anio, (short) mes);

                baseMes = entPorMes.get(claveBase);
                baseAcu += (baseMes != null ? baseMes : 0);
                repAcu.setValorPlan(baseAcu);

                realMes = entPorMes.get(claveReal);
                realAcu += (realMes != null ? realMes : 0);
                if (anio < anioHoy || (anio == anioHoy && mes <= mesHoy)) {
                    repAcu.setValorReal(realAcu);
                }

                proyMes = entPorMes.get(claveProy);
                proyAcu += (proyMes != null ? proyMes : 0);
                if (anio < anioHoy || (anio == anioHoy && mes <= mesHoy)) {
                    repAcu.setValorProyectado(proyAcu);
                } else {
                    repAcu.setValorProyectado(proyAcu + realAcu);
                }

                result.add(repAcu);

                primera.add(Calendar.MONTH, 1);
                mesPrimera = primera.get(Calendar.MONTH);
                anioPrimera = primera.get(Calendar.YEAR);
            }

            return result;
        }
        return null;
    }

    public List<MisTareasTO> obtenerMisTareasPorFiltro(FiltroMisTareasTO filtro, Integer orgPk) {
        EntregablesDAO dao = new EntregablesDAO(em);
        return dao.obtenerMisTareasPorFiltro(filtro, orgPk);
    }

    public Double obtenerEsfuerzoTotal(Integer proyPk) {
        EntregablesDAO dao = new EntregablesDAO(em);
        return dao.obtenerEsfuerzoTotal(proyPk, null);
    }

    public Double obtenerEsfuerzoTerminado(Integer proyPk) {
        EntregablesDAO dao = new EntregablesDAO(em);
        return dao.obtenerEsfuerzoTotal(proyPk, 100);
    }
}