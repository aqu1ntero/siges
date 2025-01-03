package com.sofis.business.validations;

import com.sofis.entities.constantes.MensajesNegocio;
import com.sofis.entities.data.Interesados;
import com.sofis.exceptions.BusinessException;
import com.sofis.generico.utils.generalutils.EmailValidator;
import com.sofis.generico.utils.generalutils.StringsUtils;
import java.util.Set;

public class InteresadosValidacion {

	public static boolean validar(Set<Interesados> interesados) throws BusinessException {
		if (interesados != null) {
			for (Interesados i : interesados) {
				validar(i);
			}
		}
		return true;
	}

	public static boolean validar(Interesados interesado) throws BusinessException {

		BusinessException be = new BusinessException();

		if (interesado == null) {
			be.addError(MensajesNegocio.ERROR_INTERESADO_NULL);
		} else {

			if (interesado.getIntOrgaFk() == null) {
				be.addError(MensajesNegocio.ERROR_INTERESADO_ORGANIZACION);
			}

			if (interesado.getTipo() == null) {
				be.addError(MensajesNegocio.ERROR_INTERESADO_TIPO);
			} else {
				switch (interesado.getTipo()) {
					case INTERNO:
						validarInterno(interesado, be);
						break;
					case EXTERNO:
						validarExterno(interesado, be);
						break;
				}
			}

			if (interesado.getIntPersonaFk() == null
					|| StringsUtils.isEmpty(interesado.getIntPersonaFk().getPersCargo())) {

				be.addError(MensajesNegocio.ERROR_INTERESADO_CARGO);
			}
			if (interesado.getIntRolintFk() == null) {
				be.addError(MensajesNegocio.ERROR_INTERESADO_ROL);
			}
		}

		if (be.getErrores().size() > 0) {
			throw be;
		}

		return true;
	}

	private static void validarExterno(Interesados interesado, BusinessException be) {

		if (interesado.getIntPersonaFk() == null
				|| StringsUtils.isEmpty(interesado.getIntPersonaFk().getPersNombre())) {

			be.addError(MensajesNegocio.ERROR_INTERESADO_NOMBRE);
		}
		if (interesado.getIntPersonaFk() == null
				|| StringsUtils.isEmpty(interesado.getIntPersonaFk().getPersMail())) {

			be.addError(MensajesNegocio.ERROR_INTERESADO_CORREO);
		}
		if (interesado.getIntPersonaFk() != null && interesado.getIntPersonaFk().getPersMail() != null
				&& !EmailValidator.validateEmail(interesado.getIntPersonaFk().getPersMail())) {

			be.addError(MensajesNegocio.ERROR_INTERESADO_CORREO_INVALIDO);
		}
	}

	private static void validarInterno(Interesados interesado, BusinessException be) {

		if (interesado.getUsuario() == null) {
			be.addError(MensajesNegocio.ERROR_INTERESADO_USUARIO);
		}
	}

}
