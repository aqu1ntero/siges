package com.sofis.web.mb;

import com.sofis.entities.constantes.ConstanteApp;
import com.sofis.entities.data.Organismos;
import com.sofis.entities.data.PlantillaCronograma;
import com.sofis.entities.data.PlantillaEntregables;
import com.sofis.entities.tipos.FiltroPlantillaCronogramaTO;
import com.sofis.exceptions.BusinessException;
import com.sofis.web.delegates.PlantillaCronogramaDelegate;
import com.sofis.web.delegates.ProgramasDelegate;
import com.sofis.web.delegates.ProyectosDelegate;
import com.sofis.web.enums.FieldAttributeEnum;
import com.sofis.web.properties.Labels;
import com.sofis.web.utils.JSFUtils;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;

/**
 *
 * @author Usuario
 */
@ManagedBean(name = "PlantillaCronogramaMB")
@ViewScoped
public class PlantillaCronogramaMB implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(ConstanteApp.LOGGER_NAME);
    private final static String LECC_APRENDIDAS_MSG = "leccAprendidasMsg";
    private final static String LECC_APRENDIDAS_FORM_MSG = "leccAprendidasMsgForm";
    
    @ManagedProperty("#{inicioMB}")
    private InicioMB inicioMB;
    @Inject
    private ProgramasDelegate programasDelegate;
    @Inject
    private ProyectosDelegate proyectosDelegate;
    @Inject
    private PlantillaCronogramaDelegate plantillaCroDelegate;
    
    private List<PlantillaCronograma> plantillaCronogramaResultado;
    private int cantElementosPorPagina = 20;
    private boolean filtroRender;
    private FiltroPlantillaCronogramaTO filtro;
    // Form Alta/Modificacion
    private Boolean formDataExpanded;
    private PlantillaCronograma plantillaCronograma;
    //el mapeo del numero lofgico de cada entregable con el objeto
    private HashMap<Integer, PlantillaEntregables> mapeo = new HashMap();

    public List<PlantillaCronograma> getPlantillaCronogramaResultado() {
        return plantillaCronogramaResultado;
    }

    public void setPlantillaCronogramaResultado(List<PlantillaCronograma> plantillaCronogramaResultado) {
        this.plantillaCronogramaResultado = plantillaCronogramaResultado;
    }

    public PlantillaCronogramaMB() {
        filtroRender = false;
        filtro = new FiltroPlantillaCronogramaTO();
        formDataExpanded = false;
        plantillaCronogramaResultado = new ArrayList<>();
        plantillaCronograma = new PlantillaCronograma();
        mapeo = new HashMap();
    }

    public void setInicioMB(InicioMB inicioMB) {
        this.inicioMB = inicioMB;
    }

    public int getCantElementosPorPagina() {
        return cantElementosPorPagina;
    }

    public void setCantElementosPorPagina(int cantElementosPorPagina) {
        this.cantElementosPorPagina = cantElementosPorPagina;
    }

    public FiltroPlantillaCronogramaTO getFiltro() {
        return filtro;
    }

    public void setFiltro(FiltroPlantillaCronogramaTO filtro) {
        this.filtro = filtro;
    }

    public boolean isFiltroRender() {
        return filtroRender;
    }

    public void setFiltroRender(boolean filtroRender) {
        this.filtroRender = filtroRender;
    }

    public PlantillaCronograma getPlantillaCronograma() {
        return plantillaCronograma;
    }

    public void setPlantillaCronograma(PlantillaCronograma plantillaCronograma) {
        this.plantillaCronograma = plantillaCronograma;
    }

    public Boolean getFormDataExpanded() {
        return formDataExpanded;
    }

    public void setFormDataExpanded(Boolean formDataExpanded) {
        this.formDataExpanded = formDataExpanded;
    }

    @PostConstruct
    public void init() {
        inicioMB.cargarOrganismoSeleccionado();
        buscarAction();
    }

    public void renderizarFiltro() {
        if (filtroRender) {
            filtroRender = false;
        } else {
            filtroRender = true;
        }
    }

    public String limpiar() {
        this.plantillaCronograma = new PlantillaCronograma();
        mapeo = new HashMap();
        return null;
    }

    public void guardar() {
        for (PlantillaEntregables e : plantillaCronograma.getPlantillaEntregablesSet()) {
            if (e.getPentregablesNumeroAnt() != null && e.getPentregablesNumeroAnt().equals(-1)) {
                e.setPentregableAntFk(null);
            } else {
                e.setPentregableAntFk(mapeo.get(e.getPentregablesNumeroAnt()));
            }
        }

        final Integer orgPk = inicioMB.getOrganismo().getOrgPk();
        plantillaCronograma.setOrgFk(new Organismos(orgPk));
        try {
            plantillaCronograma = this.plantillaCroDelegate.guardarPlantillaCronograma(plantillaCronograma);
        } catch (BusinessException be) {
            logger.log(Level.SEVERE, null, be);
            JSFUtils.agregarMsgs("", be.getErrores());
        }

        //vuelvo a generar el mapeo y el anterior
        recargarMapeo();
        buscarAction();
    }

    private void recargarMapeo() {
        for (PlantillaEntregables e : plantillaCronograma.getPlantillaEntregablesSet()) {
            mapeo.put(e.getPentregablesNumero(), e);

            if (e.getPentregableAntFk() != null) {
                e.setPentregablesNumeroAnt(e.getPentregableAntFk().getPentregablesNumero());
            }
        }

        Collections.sort(plantillaCronograma.getPlantillaEntregablesSet(), new Comparator<PlantillaEntregables>() {
            @Override
            public int compare(PlantillaEntregables t, PlantillaEntregables t1) {
                return t.getPentregablesNumero().compareTo(t1.getPentregablesNumero());
            }
        });
    }

    public void agregar() {
        this.plantillaCronograma = new PlantillaCronograma();
        this.plantillaCronograma.setActivo(true);
        this.plantillaCronograma.setPlantillaEntregablesSet(new ArrayList());

    }

    public void agregarLinea() {
        PlantillaEntregables e = new PlantillaEntregables();
        e.setPentregablesNombre("");
        e.setNuevo(true);

        e.setPentregablePCroFk(plantillaCronograma);
        e.setPentregableAntFk(new PlantillaEntregables());

        if (this.plantillaCronograma.getPlantillaEntregablesSet() == null) {
            this.plantillaCronograma.setPlantillaEntregablesSet(new ArrayList());
        }

        this.plantillaCronograma.getPlantillaEntregablesSet().add(e);
        e.setPentregablesNumero(this.plantillaCronograma.getPlantillaEntregablesSet().size());
        mapeo.put(e.getPentregablesNumero(), e);
    }

    public void cerrarFormCollapsable() {
        limpiar();
        formDataExpanded = false;
    }

    public String buscarAction() {
        if (plantillaCronogramaResultado != null) {
            plantillaCronogramaResultado.clear();
        }

        final Integer orgPk = inicioMB.getOrganismo().getOrgPk();
        plantillaCronogramaResultado = plantillaCroDelegate.buscarPorFiltro(filtro, orgPk);
        return null;
    }

    public String limpiarFiltro() {
        filtro = new FiltroPlantillaCronogramaTO();

        return null;
    }

    public String eliminarPlantilla(Integer pCronPk) {
        try {

            plantillaCroDelegate.eliminarPlantillaCronograma(pCronPk);
            buscarAction();
        } catch (BusinessException be) {
            logger.log(Level.SEVERE, be.getMessage());
            JSFUtils.agregarMsgInfo(LECC_APRENDIDAS_FORM_MSG, Labels.getValue("error_lecc_aprendidas_baja"), null);
        }
        return null;
    }

    public String editarPlantilla(Integer pk) {
        limpiar();
        plantillaCronograma = plantillaCroDelegate.obtenerPlantillaCronogramaPorPk(pk);
        if (plantillaCronograma != null) {
            //carago el hashMap del mapeo
            recargarMapeo();
            formDataExpanded = true;
        } else {
            JSFUtils.agregarMsgError(LECC_APRENDIDAS_FORM_MSG, Labels.getValue("error_lecc_aprendidas_null"), null);
        }

        return null;
    }

    //metodos de visualización
    /**
     * Retorna un booolean si el fieldName aportado debe estar deshabilitado
     * para usar.
     *
     * @param fieldName
     * @return
     */
    public boolean fieldDisabled(String fieldName) {
        //dependiendo del usuario, estado etc es si esta habilitado o no
        return fieldAttribute(fieldName, FieldAttributeEnum.DISABLED);
    }

    /**
     * Retorna un booolean si el fieldName aportado debe ser desplegado en
     * pantalla.
     *
     * @param fieldName
     * @return
     */
    public boolean fieldRendered(String fieldName) {
        //dependiendo del usuario, estado etc es si esta habilitado o no
        return fieldAttribute(fieldName, FieldAttributeEnum.RENDERED);
    }

    private boolean fieldAttribute(String fieldName, FieldAttributeEnum field) {

        boolean checkDisabled = field == FieldAttributeEnum.DISABLED;
        boolean checkRendered = field == FieldAttributeEnum.RENDERED;

        boolean disabled = false;
        boolean rendered = true;

        boolean isPMOT = inicioMB.isUsuarioOrgaPMOT();
        boolean isPMOF = inicioMB.isUsuarioOrgaPMOF();

        if (fieldName.equalsIgnoreCase("agregarLeccApre")
                || fieldName.equalsIgnoreCase("editarLeccion")
                || fieldName.equalsIgnoreCase("eliminarLeccion")) {
            if (!(isPMOF || isPMOT)) {
                rendered = false;
            }
        }

        if (fieldName.equalsIgnoreCase("AreaTematicaTree")) {
            if (!(isPMOF || isPMOT)) {
//                rendered = false;
            }
        }

        if (fieldName.equalsIgnoreCase("AreaConocimientoTree")) {
            if (!(isPMOF || isPMOT)) {
//                rendered = false;
            }
        }

        if (checkDisabled) {
            return disabled;
        } else if (checkRendered) {
            return rendered;
        } else {
            return false;
        }
    }
}