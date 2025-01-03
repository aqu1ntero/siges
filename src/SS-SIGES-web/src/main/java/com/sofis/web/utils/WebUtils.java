package com.sofis.web.utils;

import com.sofis.entities.data.AreaConocimiento;
import com.sofis.entities.data.AreasTags;
import com.sofis.web.properties.Labels;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.faces.context.FacesContext;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import org.icefaces.ace.model.tree.NodeState;
import org.icefaces.ace.model.tree.NodeStateMap;

public class WebUtils {

	public static final String AREAS_CONOCIMIENTO_STATE_MAP = "areasConocimientoStateMap";
	public static final String LISTA_AREAS_CON_TREE_NODE = "listaAreasConTreeNode";

	public static Map<String, Object> setNodosForAreaConocimiento(List<AreaConocimiento> listaAreasCon, List<MutableTreeNode> listaAreasConTreeNode, Set<AreaConocimiento> areasConocimiento, NodeStateMap areasConocimientoStateMap) {
		areasConocimientoStateMap = new NodeStateMap();
		for (AreaConocimiento aCon : listaAreasCon) {
			if (aCon.getAreaConPadreFk() == null || aCon.getAreaConPadreFk().getConPk() == null) {
				DefaultMutableTreeNode nodoPadre = new DefaultMutableTreeNode();
				nodoPadre.setAllowsChildren(true);
				nodoPadre.setUserObject(aCon);
				nodoPadre = setNodosHijosAreaConocimiento(listaAreasCon, nodoPadre, aCon, areasConocimiento, areasConocimientoStateMap);

				areasConocimientoStateMap = setAreasConStateMap(areasConocimiento, aCon, nodoPadre, areasConocimientoStateMap);
				listaAreasConTreeNode.add(nodoPadre);
			}
		}
		Map<String, Object> result = new HashMap<>();
		result.put(AREAS_CONOCIMIENTO_STATE_MAP, areasConocimientoStateMap);
		result.put(LISTA_AREAS_CON_TREE_NODE, listaAreasConTreeNode);
		return result;
	}

	private static DefaultMutableTreeNode setNodosHijosAreaConocimiento(List<AreaConocimiento> listaAreasCon, DefaultMutableTreeNode nodoPadre, AreaConocimiento areasConPadre, Set<AreaConocimiento> areasConocimiento, NodeStateMap areasConocimientoStateMap) {
		if (nodoPadre != null && areasConPadre != null) {
			for (AreaConocimiento aCon : listaAreasCon) {
				if (aCon.getConPk() != null
						&& aCon.getAreaConPadreFk() != null
						&& aCon.getAreaConPadreFk().getConPk().equals(areasConPadre.getConPk())) {
					DefaultMutableTreeNode nodoHijo = new DefaultMutableTreeNode();
					nodoHijo.setAllowsChildren(true);
					nodoHijo.setUserObject(aCon);
					setNodosHijosAreaConocimiento(listaAreasCon, nodoHijo, aCon, areasConocimiento, areasConocimientoStateMap);

					areasConocimientoStateMap = setAreasConStateMap(areasConocimiento, aCon, nodoHijo, areasConocimientoStateMap);
					nodoPadre.add(nodoHijo);
				}
			}
		}
		return nodoPadre;
	}

	private static NodeStateMap setAreasConStateMap(Set<AreaConocimiento> areasConocimiento, AreaConocimiento areaC, DefaultMutableTreeNode nodo, NodeStateMap areasConocimientoStateMap) {
		NodeState ns = new NodeState();
		if (areasConocimiento != null && !areasConocimiento.isEmpty() && areasConocimiento.contains(areaC)) {
			ns.setSelected(true);
		}

		areasConocimientoStateMap.put(nodo, ns);
		return areasConocimientoStateMap;
	}

	public static String mesToStr(Integer mes, Boolean abreviado) {
		if (mes != null && mes >= 1 && mes <= 12) {
			if (abreviado != null && abreviado) {
				return Labels.getValue("mes_abr_" + mes);
			} else {
				return Labels.getValue("mes_" + mes);
			}
		}
		return null;
	}
	//----------------------------------------------------------------------------------------------------------

	public static void putObjectOnExtCtx(String key, Object obj) {
		FacesContext.getCurrentInstance().getExternalContext().getFlash().put(key, obj);
	}

	public static Object getFlashContext(String attName) {
		return FacesContext.getCurrentInstance().getExternalContext().getFlash().get(attName);
	}

	public static String estadoMediaToStr(Integer cod) {
		if (cod != null) {
			return Labels.getValue("estado_media_proy_" + cod);
		}
		return null;
	}
}
