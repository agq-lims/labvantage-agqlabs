package com.labvantage.pso.agqlabs.actions;


import sapphire.action.BaseAction;

/**
 * Clase de fábrica para obtener estrategias de acción según el tipo de acción requerida.
 */
public class ActionStrategyFactory extends BaseAction {

    /**
     * Devuelve una estrategia de acción basada en el tipo de acción especificado.
     *
     * @param actionType Tipo de acción (Add, Edit, Inactivate).
     * @return Implementación de ActionStrategy correspondiente.
     */
    public static ActionStrategy getStrategy(String actionType) {
        switch (actionType) {
            case "Add":
                return new AddClientStrategy();
            case "Inactivate":
                return new InactivateClientStrategy();
            case "Edit":
                return new EditClientStrategy();
            default:
                throw new IllegalArgumentException("Tipo de acción inválido: " + actionType);
        }
    }

}
