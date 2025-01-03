package com.sofis.web.mb;

import com.sofis.business.utils.GastosUtils;
import com.sofis.business.utils.NumbersUtils;
import com.sofis.entities.constantes.ConstanteApp;
import com.sofis.entities.data.Gastos;
import com.sofis.entities.data.Participantes;
import com.sofis.entities.data.SsUsuario;
import com.sofis.entities.data.TipoGasto;
import com.sofis.entities.tipos.ComboItemTO;
import com.sofis.entities.tipos.MonedaImporteTO;
import com.sofis.exceptions.BusinessException;
import com.sofis.web.delegates.GastosDelegate;
import com.sofis.web.delegates.TipoGastoDelegate;
import com.sofis.web.properties.Labels;
import com.sofis.web.utils.JSFUtils;
import com.sofis.web.utils.SofisCombo;
import com.sofis.web.utils.SofisComboG;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.event.PhaseId;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Inject;

/**
 *
 * @author Usuario
 */
@ManagedBean(name = "revisionGastosMB")
@ViewScoped
public class RevisionGastosMB implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(RevisionGastosMB.class.getName());
    private static final String PARTICIPANTE_MSG_ID = "participantesMsg";
    private static final String PARTICIPANTE_GASTOS_MSG_ID = "partGastosMsg";
    private static final String PARTICIPANTE_POPUP_MSG_ID = "participantesPopupMsg";
    private static final String FILTRO_BUSQUEDA_GASTOS_MSG_ID = "filtroBusquedaGastosMsg";
    
    @ManagedProperty("#{inicioMB}")
    private InicioMB inicioMB;
    @ManagedProperty("#{fichaMB}")
    private FichaMB fichaMB;
    @ManagedProperty("#{revisionHorasMB}")
    private RevisionHorasMB revisionHorasMB;
    @Inject
    private GastosDelegate gastosDelegate;
    @Inject
    private TipoGastoDelegate tipoGastoDelegate;
    
    //Atributos
    private List<Gastos> revisionGastosListado;
    private int cantElementosPorPagina = 25;
    private Boolean todoAprobado;
    //Para el filtro
    private Integer filtroProyPk;
    private Date filtroFechaDesde;
    private Date filtroFechaHasta;
    private List<ComboItemTO> listaAprobItems;
    private SofisComboG<ComboItemTO> listaAprobCombo;
    private List<TipoGasto> listaTipoGasto;
    private SofisCombo listaTipoGastoCombo;

    public RevisionGastosMB() {
//        participante = new Participantes();
    }

    public void setInicioMB(InicioMB inicioMB) {
        this.inicioMB = inicioMB;
    }

    public void setFichaMB(FichaMB fichaMB) {
        this.fichaMB = fichaMB;
    }

    public void setRevisionHorasMB(RevisionHorasMB revisionHorasMB) {
        this.revisionHorasMB = revisionHorasMB;
    }

    public List<Gastos> getRevisionGastosListado() {
        return revisionGastosListado;
    }

    public void setRevisionGastosListado(List<Gastos> revisionGastosListado) {
        this.revisionGastosListado = revisionGastosListado;
    }

//    public Participantes getParticipante() {
//        return participante;
//    }
//
//    public void setParticipante(Participantes participante) {
//        this.participante = participante;
//    }
    public int getCantElementosPorPagina() {
        return cantElementosPorPagina;
    }

    public void setCantElementosPorPagina(int cantElementosPorPagina) {
        this.cantElementosPorPagina = cantElementosPorPagina;
    }

    public Boolean getTodoAprobado() {
        return todoAprobado;
    }

    public void setTodoAprobado(Boolean todoAprobado) {
        this.todoAprobado = todoAprobado;
    }

    public Integer getFiltroProyPk() {
        return filtroProyPk;
    }

    public void setFiltroProyPk(Integer filtroProyPk) {
        this.filtroProyPk = filtroProyPk;
    }

    public Date getFiltroFechaDesde() {
        return filtroFechaDesde;
    }

    public void setFiltroFechaDesde(Date filtroFechaDesde) {
        this.filtroFechaDesde = filtroFechaDesde;
    }

    public Date getFiltroFechaHasta() {
        return filtroFechaHasta;
    }

    public void setFiltroFechaHasta(Date filtroFechaHasta) {
        this.filtroFechaHasta = filtroFechaHasta;
    }

    public List<ComboItemTO> getListaAprobItems() {
        return listaAprobItems;
    }

    public void setListaAprobItems(List<ComboItemTO> listaAprobItems) {
        this.listaAprobItems = listaAprobItems;
    }

    public SofisComboG<ComboItemTO> getListaAprobCombo() {
        return listaAprobCombo;
    }

    public void setListaAprobCombo(SofisComboG<ComboItemTO> listaAprobCombo) {
        this.listaAprobCombo = listaAprobCombo;
    }

    public List<TipoGasto> getListaTipoGasto() {
        return listaTipoGasto;
    }

    public void setListaTipoGasto(List<TipoGasto> listaTipoGasto) {
        this.listaTipoGasto = listaTipoGasto;
    }

    public SofisCombo getListaTipoGastoCombo() {
        return listaTipoGastoCombo;
    }

    public void setListaTipoGastoCombo(SofisCombo listaTipoGastoCombo) {
        this.listaTipoGastoCombo = listaTipoGastoCombo;
    }

    @PostConstruct
    public void init() {
        inicializarRevisionGastos();
    }

    private void inicializarRevisionGastos() {
        revisionHorasMB.setParticipante(new Participantes());
        revisionHorasMB.getParticipante().setPartProyectoFk(null);
        revisionHorasMB.getParticipante().setPartUsuarioFk(new SsUsuario(0));

        filtroProyPk = fichaMB.getFichaTO().getFichaFk();
        filtroFechaDesde = null;
        filtroFechaHasta = null;

        listaAprobItems = new ArrayList<>();
        listaAprobItems.add(new ComboItemTO(0, Labels.getValue("revisionHoras_pendiente")));
        listaAprobItems.add(new ComboItemTO(1, Labels.getValue("revisionHoras_aprobado")));
        listaAprobCombo = new SofisComboG(listaAprobItems, "itemNombre");
        listaAprobCombo.addEmptyItem(Labels.getValue("comboTodos"));

        listaTipoGasto = tipoGastoDelegate.obtenerTipoGastoPorOrg(inicioMB.getOrganismo().getOrgPk());
        if (listaTipoGasto != null) {
            listaTipoGastoCombo = new SofisCombo((List) listaTipoGasto, "tipogasNombre");
            listaTipoGastoCombo.addEmptyItem(Labels.getValue("comboTodos"));
        }
    }

    public String buscarConFiltro() {
        TipoGasto tipoGasto = (TipoGasto) listaTipoGastoCombo.getSelectedObject();
        ComboItemTO aprob = listaAprobCombo.getSelectedT();
        revisionGastosListado = gastosDelegate.obtenerRegistrosGastos(revisionHorasMB.getParticipante().getPartUsuarioFk().getUsuId(),
                revisionHorasMB.getParticipante().getPartProyectoFk().getProyPk(),
                filtroFechaDesde, filtroFechaHasta, tipoGasto, null, null,
                (aprob != null ? (Integer) aprob.getItemObject() : null), null);
        revisionGastosListado = GastosUtils.sortByFecha(revisionGastosListado, true);
        return null;
    }

    public String buscarSinFiltro() {
        //Aunque sea sin filtro, siempre incluye el usuario y el proyecto
        filtroFechaDesde = null;
        filtroFechaHasta = null;
        return buscarConFiltro();
    }

    public String guardar() {
        gastosDelegate.guardarGastos(revisionGastosListado, inicioMB.getOrganismo().getOrgPk());

        revisionHorasMB.setRenderedHorasGastos(0);
        revisionHorasMB.limpiarParticipante();
        fichaMB.actualizarFichaTO(null);
        fichaMB.cargarResumenParticipantes();
        return null;
    }

    public void limpiarFiltro() {
        listaAprobCombo.setSelected(-1);
        listaTipoGastoCombo.setSelected(-1);
        filtroFechaDesde = null;
        filtroFechaHasta = null;
    }

    /**
     *
     * @param part
     * @param aprobado null=todos, 0=no aprobados, 1=aprobados.
     * @return
     */
    public String cambiarUsuDetalleGastos(Participantes part, Long aprobado) {
        filtroFechaDesde = null;
        filtroFechaHasta = null;

        if (part != null) {
            revisionHorasMB.setRenderedHorasGastos(2);
            revisionHorasMB.setParticipante(part);
            Integer aprob = (aprobado != null ? aprobado.intValue() : null);

            listaAprobCombo.setSelected(aprob);

            revisionGastosListado = gastosDelegate.obtenerRegistrosGastos(revisionHorasMB.getParticipante().getPartUsuarioFk().getUsuId(),
                    revisionHorasMB.getParticipante().getPartProyectoFk().getProyPk(),
                    filtroFechaDesde, filtroFechaHasta, null, null, null,
                    (aprob != null ? aprob : null), null);
            revisionGastosListado = GastosUtils.sortByFecha(revisionGastosListado, true);

        } else {
            revisionHorasMB.setRenderedHorasGastos(0);

            fichaMB.cargarResumenParticipantes();
        }

        return null;
    }

    public void aprobarTodo(ValueChangeEvent evt) {
        PhaseId phaseId = evt.getPhaseId();
        if (phaseId.equals(PhaseId.ANY_PHASE)) {
            evt.setPhaseId(PhaseId.UPDATE_MODEL_VALUES);
            evt.queue();
        } else if (phaseId.equals(PhaseId.UPDATE_MODEL_VALUES)) {
            boolean aprobar = false;
            if (todoAprobado != null && todoAprobado.equals(Boolean.TRUE)) {
                aprobar = true;
            }
            if (revisionGastosListado != null) {
                for (Gastos gasto : revisionGastosListado) {
                    gasto.setGasAprobado(aprobar);
                }
            }
        }
    }

    public String monedaImporteTxt(MonedaImporteTO[] monImp) {
        if (monImp != null && monImp.length > 0) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < monImp.length; i++) {
                if (monImp[i].getImporte() != null) {
                    if (i > 0) {
                        sb.append("<br/>");
                    }
                    sb.append(monImp[i].getMoneda().getMonSigno())
                            .append(" ")
                            .append(NumbersUtils.formatImporte(monImp[i].getImporte()));
                }
            }
            return sb.toString();
        }
        return "";
    }

    public String marcarGastos() {
        if (todoAprobado == null || todoAprobado.equals(Boolean.FALSE)) {
            todoAprobado = true;
        } else {
            todoAprobado = false;
        }
        if (revisionGastosListado != null) {
            for (Gastos gasto : revisionGastosListado) {
                gasto.setGasAprobado(todoAprobado);
            }
        }

        return null;
    }

    public String eliminarGastosAction(Integer gastoPk) {
        try {
            gastosDelegate.eliminarGastos(gastoPk);
            for (Gastos gasto : revisionGastosListado) {
                if (gasto.getGasPk().equals(gastoPk)) {
                    revisionGastosListado.remove(gasto);
                    break;
                }
            }
        } catch (BusinessException be) {
            logger.log(Level.SEVERE, be.getMessage(), be);
            
            /*
            *  18-06-2018 Inspección de código.
            */

            //JSFUtils.agregarMsgs(PARTICIPANTE_MSG_ID, be.getErrores());

            for(String iterStr : be.getErrores()){
                JSFUtils.agregarMsgError(PARTICIPANTE_MSG_ID, Labels.getValue(iterStr), null);                
            }              

        }
        return null;
    }
}
