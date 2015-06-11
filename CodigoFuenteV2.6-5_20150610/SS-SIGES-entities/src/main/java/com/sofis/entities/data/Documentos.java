package com.sofis.entities.data;

import com.sofis.entities.constantes.ConstantesEstandares;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

/**
 *
 * @author Usuario
 */
@Entity
@Table(name = "documentos")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Documentos.findAll", query = "SELECT d FROM Documentos d")})
public class Documentos implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "docs_pk")
    private Integer docsPk;
    @JoinColumn(name = "docs_tipodoc_fk", referencedColumnName = "tipodoc_inst_pk")
    @ManyToOne(optional = false)
    @Fetch(FetchMode.SELECT)
    private TipoDocumentoInstancia docsTipo;
    @Column(name = "docs_nombre")
    private String docsNombre;
    @Column(name = "docs_fecha")
    @Temporal(TemporalType.DATE)
    private Date docsFecha;
    @Column(name = "docs_privado")
    private Boolean docsPrivado;
    @Column(name = "docs_estado")
    private Double docsEstado;
    @JoinColumn(name = "docs_entregable_fk", referencedColumnName = "ent_pk")
    @ManyToOne(optional = true)
    @Fetch(FetchMode.SELECT)
    private Entregables docsEntregable;
    @Transient
    private String docsEstadoColor;
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "docfileDocFk")
    @Fetch(FetchMode.SELECT)
    private List<DocFile> docFile;
    @Column(name = "docs_aprobado")
    private Boolean docsAprobado;

    @JoinColumn(name = "docs_pago_fk", referencedColumnName = "pag_pk")
    @ManyToOne(optional = true)
    @Fetch(FetchMode.SELECT)
    private Pagos docsPagoFk;

    public Documentos() {
    }

    public Documentos(Integer docsPk) {
        this.docsPk = docsPk;
    }

    public Integer getDocsPk() {
        return docsPk;
    }

    public void setDocsPk(Integer docsPk) {
        this.docsPk = docsPk;
    }

    public TipoDocumentoInstancia getDocsTipo() {
        return docsTipo;
    }

    public void setDocsTipo(TipoDocumentoInstancia docsTipo) {
        this.docsTipo = docsTipo;
    }

    public String getDocsNombre() {
        return docsNombre;
    }

    public void setDocsNombre(String docsNombre) {
        this.docsNombre = docsNombre;
    }

    public Date getDocsFecha() {
        return docsFecha;
    }

    public void setDocsFecha(Date docsFecha) {
        this.docsFecha = docsFecha;
    }

    public Boolean getDocsPrivado() {
        return docsPrivado;
    }

    public boolean isDocPrivado() {
        return docsPrivado != null ? docsPrivado : false;
    }

    public void setDocsPrivado(Boolean docsPrivado) {
        this.docsPrivado = docsPrivado;
    }

    public Double getDocsEstado() {
        return docsEstado;
    }

    public void setDocsEstado(Double docsEstado) {
        this.docsEstado = docsEstado;
    }

    public Entregables getDocsEntregable() {
        return docsEntregable;
    }

    public void setDocsEntregable(Entregables docsEntregable) {
        this.docsEntregable = docsEntregable;
    }

    public DocFile getDocFile() {
        return docFile != null ? docFile.get(0) : null;
    }

    public void setDocFile(DocFile docFile) {
        this.docFile = new ArrayList<DocFile>();
        this.docFile.add(docFile);
    }

    public Boolean getDocsAprobado() {
        return docsAprobado;
    }

    public void setDocsAprobado(Boolean docsAprobado) {
        this.docsAprobado = docsAprobado;
    }

    public boolean isDocsAprobado() {
        return docsAprobado != null ? docsAprobado : false;
    }

    public Pagos getDocsPagoFk() {
        return docsPagoFk;
    }

    public void setDocsPagoFk(Pagos docsPagoFk) {
        this.docsPagoFk = docsPagoFk;
    }

    public String getDocsEstadoColor() {
        return docsEstadoColor;
    }

    public void setDocsEstadoColor(String docsEstadoColor) {
        this.docsEstadoColor = docsEstadoColor;
    }

    public byte[] getDocfileFile() {
        return (this.getDocFile() != null ? this.getDocFile().getDocfileFile() : null);
    }

    public String getDocfileNombre() {
        return (this.getDocFile() != null ? this.getDocFile().getDocfileNombre() : null);
    }

    public boolean getTieneArchivo() {
        return this.docFile != null
                && !this.docFile.isEmpty()
                && this.docFile.get(0) != null;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (docsPk != null ? docsPk.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Documentos)) {
            return false;
        }
        Documentos other = (Documentos) object;
        if (docsPk == null || other.docsPk == null) {
            return this == object;
        }

        if ((this.docsPk == null && other.docsPk != null) || (this.docsPk != null && !this.docsPk.equals(other.docsPk))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.sofis.entities.data.Documentos[ docsPk=" + docsPk + " ]";
    }

    public String getEstadoColor() {
        //Si no es exigido no se pinta el semaforo.
        if (docsTipo != null && docsTipo.getTipodocExigidoDesde() == null) {
            return ConstantesEstandares.COLOR_TRANSPARENT;
        }

        if (docsEstado != null && docsEstado.equals(0D)) {
            return ConstantesEstandares.SEMAFORO_ROJO;
        } else if (docsEstado != null && docsEstado.equals(0.5D)) {
            return ConstantesEstandares.SEMAFORO_AMARILLO;
        } else if (docsEstado != null && docsEstado.equals(1D)) {
            return ConstantesEstandares.SEMAFORO_VERDE;
        }
        return ConstantesEstandares.COLOR_TRANSPARENT;
    }

    public void toSystemOut() {
        System.out.println("-- Documentos --");
        System.out.println("Pk:" + this.docsPk);
        System.out.println("nombre:" + this.docsNombre);
        System.out.println("tipo:" + this.getDocsTipo());
        System.out.println("estado:" + this.docsEstado);
        System.out.println("fecha:" + this.docsFecha.toString());
        System.out.println("privado:" + this.docsPrivado);
    }
}