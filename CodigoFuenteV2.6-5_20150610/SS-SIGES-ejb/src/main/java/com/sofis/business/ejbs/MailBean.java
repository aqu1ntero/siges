package com.sofis.business.ejbs;

import com.sofis.business.interceptors.LoggedInterceptor;
import com.sofis.business.properties.LabelsEJB;
import com.sofis.business.utils.MailsTemplateUtils;
import com.sofis.entities.constantes.ConstantesEstandares;
import com.sofis.entities.constantes.MensajesNegocio;
import com.sofis.entities.data.Configuracion;
import com.sofis.entities.data.Estados;
import com.sofis.entities.data.MailsTemplate;
import com.sofis.entities.data.Organismos;
import com.sofis.entities.data.Programas;
import com.sofis.entities.data.Proyectos;
import com.sofis.exceptions.BusinessException;
import com.sofis.exceptions.GeneralException;
import com.sofis.exceptions.MailException;
import com.sofis.generico.utils.generalutils.EmailValidator;
import com.sofis.generico.utils.generalutils.StringsUtils;
import static java.lang.ProcessBuilder.Redirect.from;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
import javax.interceptor.Interceptors;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.naming.InitialContext;

/**
 *
 * @author Usuario
 */
@Named
@Stateless(name = "MailBean")
@LocalBean
@Interceptors({LoggedInterceptor.class})
public class MailBean {

    private static final Logger logger = Logger.getLogger(ConstantesEstandares.LOGGER);

    @Inject
    private ConfiguracionBean cnfBean;
    @Inject
    private MailsTemplateBean mailsTemplateBean;
    @Inject
    private OrganismoBean organismoBean;
    private Session mailSession = null;

    public MailBean() {
    }

    private boolean conCorreo(Integer orgPk) {
        boolean conCorreo = true;

        if (orgPk != null) {
            try {
                Configuracion cnfCorreo = cnfBean.obtenerCnfPorCodigoYOrg(ConstantesEstandares.CON_CORREO, orgPk);
                conCorreo = Boolean.parseBoolean(cnfCorreo.getCnfValor());
            } catch (Exception ex) {
                logger.log(Level.WARNING, "Error al obtener la configuracion 'CON_CORREO'.");
//                conCorreo = false;
            }
        }
        return conCorreo;
    }

    public boolean enviarMail(String subject, String from, String[] recipients, String message, Integer orgPk) {
        return enviarMail(subject, from, null, null, recipients, message, orgPk);
    }

    public boolean enviarMail(String subject, String from, String[] recipientsTO, String[] recipientsCC, String[] recipientsBCC, String message, Integer orgPk) {

        if (conCorreo(orgPk)) {
            try {
                InitialContext ic = new InitialContext();
                mailSession = (Session) ic.lookup("java:jboss/mail/SigesMail");
                ic.close();

//                mailSession.getProperties().setProperty("mail.smtp.ssl.enable", ""); //true
//                mailSession.getProperties().setProperty("mail.transport.protocol", "smtp");
//                mailSession.getProperties().setProperty("mail.smtp.host", ""); //smtp.gmail.com
//                mailSession.getProperties().setProperty("mail.smtp.debug", "false");
//                mailSession.getProperties().setProperty("mail.debug", "false");
//                mailSession.getProperties().setProperty("mail.smtp.starttls.enable", ""); //true
//                mailSession.getProperties().setProperty("mail.smtp.port", ""); //465
//                mailSession.getProperties().setProperty("mail.smtp.auth", ""); //true
//                mailSession.getProperties().setProperty("mail.smtp.user", ""); //sofis.pruebas2@gmail.com
                /*
                mailSession.getProperties().setProperty("mail.smtp.ssl.enable", "false"); //true
                mailSession.getProperties().setProperty("mail.transport.protocol", "smtp");
                mailSession.getProperties().setProperty("mail.smtp.host", "adinet.com.uy"); //smtp.gmail.com
                mailSession.getProperties().setProperty("mail.smtp.debug", "false");
                mailSession.getProperties().setProperty("mail.debug", "false");
                mailSession.getProperties().setProperty("mail.smtp.starttls.enable", "false"); //true
                mailSession.getProperties().setProperty("mail.smtp.port", "25"); //465
                mailSession.getProperties().setProperty("mail.smtp.auth", "true"); //true
                mailSession.getProperties().setProperty("mail.smtp.user", "sofis.pruebas@adinet.com.uy"); //sofis.pruebas2@gmail.com
                mailSession.getProperties().setProperty("mail.smtp.password", "sofis123");
*/
                boolean hasRecipient = false;

                MimeMessage msg = new MimeMessage(mailSession);

                msg.setSubject(subject);

                if (StringsUtils.isEmpty(from)) {
                    String fromName = "SIGES";
                    if (orgPk != null) {
                        Configuracion cnf = cnfBean.obtenerCnfPorCodigoYOrg("MAIL_FROM", orgPk);
                        InternetAddress addressFrom = new InternetAddress();
                        addressFrom.setPersonal(fromName);
                        addressFrom.setAddress(cnf.getCnfValor());
                        msg.setFrom(addressFrom);
                    } else {
                        msg.setFrom();
                    }

                } else {
                    InternetAddress addressFrom = new InternetAddress();
                    addressFrom.setPersonal(from);
                    addressFrom.setAddress(from);
                    msg.setFrom(addressFrom);
                }

                List<InternetAddress> listAddress = null;
                if (recipientsTO != null) {
                    listAddress = new ArrayList<>();
                    loadRecipients(listAddress, recipientsTO);

                    InternetAddress[] addressTO = listAddress.toArray(new InternetAddress[listAddress.size()]);
                    msg.setRecipients(Message.RecipientType.TO, addressTO);
                    if (addressTO != null && addressTO.length > 0) {
                        hasRecipient = true;
                    }
                }
                if (recipientsCC != null) {
                    listAddress = new ArrayList<>();
                    loadRecipients(listAddress, recipientsCC);

                    InternetAddress[] addressCC = listAddress.toArray(new InternetAddress[listAddress.size()]);
                    msg.setRecipients(Message.RecipientType.CC, addressCC);
                    if (addressCC != null && addressCC.length > 0) {
                        hasRecipient = true;
                    }
                }
                if (recipientsBCC
                        != null) {
                    listAddress = new ArrayList<>();
                    loadRecipients(listAddress, recipientsBCC);

                    InternetAddress[] addressBCC = listAddress.toArray(new InternetAddress[listAddress.size()]);
                    msg.setRecipients(Message.RecipientType.BCC, addressBCC);
                    if (addressBCC != null && addressBCC.length > 0) {
                        hasRecipient = true;
                    }
                }

                if (!hasRecipient) {
                    MailException me = new MailException();
                    me.addError(MensajesNegocio.ERROR_MAIL_RECIPIENT);
                    throw me;
                }

                String encoding = "utf8";

                try {
                    Configuracion cnfEncoding = cnfBean.obtenerCnfPorCodigoYOrg("MAIL_ENCODING", orgPk);
                    if (cnfEncoding != null) {
                        encoding = cnfEncoding.getCnfValor();
                    }
                } catch (GeneralException ge) {
                    logger.log(Level.WARNING, "No se pudo obtener \"MAIL_ENCODING\" de las configuraciones. Se toma por defecto: " + encoding);
                }

                msg.setText(message, encoding);

                msg.setHeader("Content-Type", "text/html; charset=\"" + encoding + "\"");
                msg.setHeader("Content-Transfer-Encoding", "quoted-printable");
                //transport.connect(this.serverDir, Integer.valueOf(this.serverPort).intValue(), this.from, this.serverPass);

                msg.saveChanges();

                Transport.send(msg);
                logger.log(Level.INFO,"## Mail enviado correctamente.");

                return true;
            } catch (MailException me) {
                logger.log(Level.WARNING, "Error al enviar el msg: \"" + message + "\"");
                logger.log(Level.WARNING, "Una de las siguientes direcciones falla: "
                        + (recipientsTO != null ? "(TO:" + recipientsTO.length + ")" + StringsUtils.concat(recipientsTO) : "") + ","
                        + (recipientsCC != null ? "(CC:" + recipientsCC.length + ")" + StringsUtils.concat(recipientsCC) : "") + ","
                        + (recipientsBCC != null ? "(BCC:" + recipientsBCC.length + ")" + StringsUtils.concat(recipientsBCC) : "") + "\"");
                me.addError(MensajesNegocio.ERROR_MAIL_ENVIO);
                throw me;
            } catch (Exception ex) {
                logger.log(Level.WARNING, null, ex);
                MailException me = new MailException();
                me.addError(MensajesNegocio.ERROR_MAIL_ENVIO);
                throw me;
            }
        } else {
            String to = (recipientsTO != null ? "(TO:" + recipientsTO.length + ")" + StringsUtils.concat(recipientsTO) : "") + ","
                    + (recipientsCC != null ? "(CC:" + recipientsCC.length + ")" + StringsUtils.concat(recipientsCC) : "") + ","
                    + (recipientsBCC != null ? "(BCC:" + recipientsBCC.length + ")" + StringsUtils.concat(recipientsBCC) : "");
            logger.log(Level.INFO, "[Mail deshabilitado] No se envia correo: {0}, mensaje: {1}, " + to, new Object[]{subject, message});
            BusinessException be = new BusinessException();
            be.addError(MensajesNegocio.ERROR_MAIL_DISABLED);
            throw be;
        }
    }

    private List<InternetAddress> loadRecipients(List<InternetAddress> listAddress, String[] recipients) {
        if (listAddress == null) {
            listAddress = new ArrayList<>();
        }
        for (String recipient : recipients) {
            if (validarEMail(recipient)) {
                try {
                    listAddress.add(new InternetAddress(recipient));
                } catch (AddressException ex) {
                    logger.log(Level.SEVERE, null, ex);
                }
            } else {
                logger.log(Level.FINE, "Se descarta la dirección de mail: '" + recipient + "'. La dirección no es correcta.");
            }
        }
        return listAddress;
    }

    /**
     * Valida el mail aportado.
     *
     * @param email
     * @return
     */
    public boolean validarEMail(String email) {
        return new EmailValidator().validate(email);
    }

    /**
     * Envía un comunicado informando la solicitud de una aprobacion para un
     * Programa o Proyecto.
     *
     * @param orgPk
     * @param destinatario
     */
    public void comunicarSolicitudAprobacion(Integer orgPk, Object obj, String... destinatario) throws GeneralException {
        MailsTemplate mt = mailsTemplateBean.obtenerMailTmpPorCodigo(MailsTemplateBean.MAIL_SOL_APROBACION, orgPk);

        if (conCorreo(orgPk) && mt != null) {
            String asunto = mt.getMailTmpAsunto();
            String mensaje = mt.getMailTmpMensaje();

            Organismos org = organismoBean.obtenerOrgPorId(orgPk);

            String tipo = "";
            Integer fichaPk = null;
            String nombre = "";
            if (obj instanceof Programas) {
                Programas p = (Programas) obj;
                tipo = "programa";
                fichaPk = p.getProgPk();
                nombre = p.getProgNombre();
            } else if (obj instanceof Proyectos) {
                Proyectos p = (Proyectos) obj;
                tipo = "proyecto";
                fichaPk = p.getProyPk();
                nombre = p.getProyNombre();
            }
            Map<String, String> valores = new HashMap<>();
            valores.put("TIPO_PROG_PROY", tipo);
            valores.put("ID_PROG_PROY", fichaPk.toString());
            valores.put("NOMBRE_PROG_PROY", nombre);
            valores.put("ORGANISMO_NOMBRE", org.getOrgNombre());
            valores.put("ORGANISMO_DIRECCION", org.getOrgDireccion());
            mensaje = MailsTemplateUtils.instanciarConHashMap(mensaje, valores);

            this.enviarMail(asunto, "", destinatario, mensaje, orgPk);
        }
    }

    /**
     * Envía un comunicado informando el cambio de estado para un Programa o
     * Proyecto.
     *
     * @param orgPk
     * @param obj Programa o Proyecto
     * @param destinatario
     * @throws GeneralException
     */
    public void comunicarCambioEstado(Integer orgPk, Object obj, String... destinatario) throws GeneralException {
        MailsTemplate mt = mailsTemplateBean.obtenerMailTmpPorCodigo(MailsTemplateBean.MAIL_CAMBIO_ESTADO, orgPk);

        if (conCorreo(orgPk) && mt != null) {
            String asunto = mt.getMailTmpAsunto();
            String mensaje = mt.getMailTmpMensaje();

            Organismos org = organismoBean.obtenerOrgPorId(orgPk);

            String tipo = "";
            Integer fichaPk = null;
            String nombre = "";
            Estados estado = null;
            if (obj instanceof Programas) {
                Programas p = (Programas) obj;
                tipo = LabelsEJB.getValue("programa");
                fichaPk = p.getProgPk();
                nombre = p.getProgNombre();
                estado = p.getProgEstFk();
            } else if (obj instanceof Proyectos) {
                Proyectos p = (Proyectos) obj;
                tipo = LabelsEJB.getValue("proyecto");
                fichaPk = p.getProyPk();
                nombre = p.getProyNombre();
                estado = p.getProyEstFk();
            }

            Map<String, String> valores = new HashMap<>();
            valores.put("TIPO_PROG_PROY", tipo);
            valores.put("ID_PROG_PROY", fichaPk.toString());
            valores.put("NOMBRE_PROG_PROY", nombre);
            valores.put("FASE_PROG_PROY", LabelsEJB.getValue("estado_" + estado.getEstPk()));
            valores.put("ORGANISMO_NOMBRE", org.getOrgNombre());
            valores.put("ORGANISMO_DIRECCION", org.getOrgDireccion());
            mensaje = MailsTemplateUtils.instanciarConHashMap(mensaje, valores);

            this.enviarMail(asunto, "", destinatario, mensaje, orgPk);
        }
    }

    /**
     * Envía un comunicado informando que queda pendiente de aprobación.
     *
     * @param orgPk
     * @param obj Programa o Proyecto
     * @param destinatario
     * @throws GeneralException
     */
    public void comunicarProgProyPendiente(Integer orgPk, Object obj, String... destinatario) throws MailException {
        MailsTemplate mt = mailsTemplateBean.obtenerMailTmpPorCodigo(MailsTemplateBean.MAIL_PROG_PROY_PENDIENTE, orgPk);

        if (conCorreo(orgPk) && mt != null) {
            String asunto = mt.getMailTmpAsunto();
            String mensaje = mt.getMailTmpMensaje();

            Organismos org = organismoBean.obtenerOrgPorId(orgPk);

            String tipo = "";
            Integer fichaPk = null;
            String nombre = "";
            if (obj instanceof Programas) {
                Programas p = (Programas) obj;
                tipo = LabelsEJB.getValue("programa");
                fichaPk = p.getProgPk();
                nombre = p.getProgNombre();
            } else if (obj instanceof Proyectos) {
                Proyectos p = (Proyectos) obj;
                tipo = LabelsEJB.getValue("proyecto");
                fichaPk = p.getProyPk();
                nombre = p.getProyNombre();
            }

            Map<String, String> valores = new HashMap<>();
            valores.put("TIPO_PROG_PROY", tipo);
            valores.put("ID_PROG_PROY", fichaPk.toString());
            valores.put("NOMBRE_PROG_PROY", nombre);
            valores.put("ORGANISMO_NOMBRE", org.getOrgNombre());
            valores.put("ORGANISMO_DIRECCION", org.getOrgDireccion());
            mensaje = MailsTemplateUtils.instanciarConHashMap(mensaje, valores);

            this.enviarMail(asunto, "", null, null, destinatario, mensaje, orgPk);
        }
    }

    /**
     * Envía un mail con la nueva constraseña generada para el usuario
     *
     * @param orgPk
     * @param destinatario
     */
    public boolean comunicarNuevaContrasenia(Integer orgPk, String nombre, String contrasenia, String email) throws GeneralException {
        MailsTemplate mt = mailsTemplateBean.obtenerMailTmpPorCodigo(MailsTemplateBean.MAIL_CAMBIO_CONTRASENIA, null);

        if (mt != null) {
            String asunto = mt.getMailTmpAsunto();
            String mensaje = mt.getMailTmpMensaje();

            Map<String, String> valores = new HashMap<>();
            valores.put("NOMBRE", nombre);
            valores.put("CONTRASENIA", contrasenia);
            if (orgPk != null) {
                Organismos org = organismoBean.obtenerOrgPorId(orgPk);
                valores.put("ORGANISMO_NOMBRE", org.getOrgNombre());
                valores.put("ORGANISMO_DIRECCION", org.getOrgDireccion());
            }
            mensaje = MailsTemplateUtils.instanciarConHashMap(mensaje, valores);

            String[] dests = new String[1];
            dests[0] = email;

            return this.enviarMail(asunto, "", dests, mensaje, orgPk);
        }
        return false;
    }

    public void comunicarNuevoUsuario(Integer orgPk, String mail, String clave) {
        MailsTemplate mt = mailsTemplateBean.obtenerMailTmpPorCodigo(MailsTemplateBean.MAIL_NVO_USUARIO, null);

        if (mt != null) {
            String asunto = mt.getMailTmpAsunto();
            String mensaje = mt.getMailTmpMensaje();

            Map<String, String> valores = new HashMap<>();
            valores.put("USU_MAIL", mail);
            valores.put("USU_PASSWORD", clave);
            Organismos org = organismoBean.obtenerOrgPorId(orgPk);
            valores.put("ORGANISMO_NOMBRE", org.getOrgNombre());
            valores.put("ORGANISMO_DIRECCION", org.getOrgDireccion());
            mensaje = MailsTemplateUtils.instanciarConHashMap(mensaje, valores);

            String[] dests = new String[1];
            dests[0] = mail;

            this.enviarMail(asunto, "", dests, mensaje, orgPk);
        }
    }
}