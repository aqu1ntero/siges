package com.sofis.business.validations;

import com.sofis.business.utils.Utils;
import com.sofis.entities.constantes.ConstanteApp;
import com.sofis.entities.constantes.MensajesNegocio;
import com.sofis.entities.data.Adquisicion;
import com.sofis.exceptions.BusinessException;
import com.sofis.generico.utils.generalutils.StringsUtils;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Usuario
 */
public class AdquisicionValidacion {

    private static final Logger logger = Logger.getLogger(ConstanteApp.LOGGER_NAME);

    public static boolean validar(Adquisicion adq) throws BusinessException {
        logger.finest("Validar Adquisicion.");
        BusinessException be = new BusinessException();

        if (adq == null) {
            be.addError(MensajesNegocio.ERROR_ADQISICION_NULL);
        } else {
            if (StringsUtils.isEmpty(adq.getAdqNombre())) {
                be.addError(MensajesNegocio.ERROR_ADQISICION_NOMBRE);
            } else if (adq.getAdqNombre().length() > Adquisicion.NOMBRE_LENGHT) {
                be.addError(Utils.mensajeLargoCampo("adquisicion_nombre", Adquisicion.NOMBRE_LENGHT));
            }

            if (adq.getAdqFuente() == null) {
                be.addError(MensajesNegocio.ERROR_ADQISICION_FUENTE);
            }

            if (adq.getAdqMoneda() == null) {
                be.addError(MensajesNegocio.ERROR_ADQISICION_MONEDAS);
            }
        }

        if (be.getErrores().size() > 0) {
            logger.logp(Level.WARNING, AdquisicionValidacion.class.getName(), "validar", be.getErrores().toString(), be);
            throw be;
        }

        return true;
    }
}