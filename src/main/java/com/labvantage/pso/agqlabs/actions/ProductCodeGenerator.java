package com.labvantage.pso.agqlabs.actions;

import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

/**
 * Autor: Jhon Carlos Solís Ochoa
 * Empresa: AGQ Labs
 * Descripción:  Generador de códigos para productos.
 *  Esta clase implementa la lógica para generar códigos únicos para productos
 *  con un prefijo específico, dependiendo de si es un menú o no.
 *
 *  <p>Los códigos generados tienen un formato específico:
 *   - Prefijo "T" para productos regulares.
 *   - Prefijo "K" para productos asociados a menús.</p>
 *
 */

public class ProductCodeGenerator extends BaseAction {


    /**
     * Método principal que ejecuta la acción para generar un código de producto.
     *
     * @param propertyList una lista de propiedades que contiene datos de entrada y salida.
     *                     - "menu" (opcional): Indica si el producto pertenece a un menú.
     *                     - "codeProduct": Propiedad donde se almacenará el código generado.
     */
    @Override
    public void processAction(PropertyList propertyList){
        String menu = propertyList.getProperty("menu", "false");

        String code = getProductCode(Boolean.valueOf(menu));
        propertyList.setProperty("codeProduct", code);
        logger.info("Codigo: " + propertyList.getProperty("codeProduct", ""));
    }

    /**
     * Genera un código único para un producto.
     *
     * @param isMenu un indicador booleano que especifica si el producto es parte de un menú.
     *               - true: El producto pertenece a un menú, el prefijo será "K".
     *               - false: Producto regular, el prefijo será "T".
     * @return el código generado en formato: "<prefijo><número_de_cinco_dígitos>".
     */
    private String getProductCode(Boolean isMenu){

        String prefix = "T";

        String strSql = "SELECT count(1) cantidad FROM s_product";
        DataSet resulQuery = this.getQueryProcessor().getSqlDataSet(strSql);

        int quantity = resulQuery.getInt(0, "cantidad") + 1;

        if(Boolean.TRUE.equals(isMenu)){
            prefix = "K";
        }

        // Retorna el código en el formato esperado.
        return prefix + String.format("%05d", quantity);

    }


}
