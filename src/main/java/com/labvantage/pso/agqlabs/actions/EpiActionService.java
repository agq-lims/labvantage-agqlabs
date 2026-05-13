package com.labvantage.pso.agqlabs.actions;

import org.json.JSONArray;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

/**
 * Autor: Jhon Carlos Solís Ochoa
 * Empresa: AGQ Labs
 * Descripción: Clase que proporciona servicios relacionados con los Elementos de Protección Individual (EPI).
 * Convierte los datos obtenidos de una consulta SQL en un formato JSON específico.
 */
public class EpiActionService extends BaseAction {

    /**
     * Procesa una acción que genera un resultado JSON a partir de la información de los EPI.
     *
     * @param propertyList Objeto que contiene las propiedades del contexto.
     * @throws SapphireException Si ocurre un error durante el proceso.
     */
    public void processAction(PropertyList propertyList) throws SapphireException {
        // Obtiene el resultado en formato JSON
        JSONArray epi = getEpiResult();
        // Agrega el JSON resultante a las propiedades
        propertyList.setProperty("message", epi.toString());
        // Registra el resultado en los logs
        this.logger.info("EPI JSON Result: " + propertyList.getProperty("message"));
    }

    /**
     * Realiza la consulta SQL para obtener la información de los EPI y la convierte en JSON.
     *
     * @return Un JSONArray con los datos de los EPI.
     * @throws SapphireException Si ocurre un error durante la ejecución de la consulta o procesamiento de los datos.
     */
    private JSONArray getEpiResult() throws SapphireException {
        try {
            // Consulta SQL para obtener los datos
            String strSql = "SELECT e.u_epiid, e.epidesc, sa.thumbnailimage " +
                    "FROM u_epi e " +
                    "LEFT JOIN sdiattachment sa " +
                    "ON e.u_epiid = sa.keyid1 " +
                    "AND sa.sdcid = 'epi' " +
                    "ORDER BY CAST(e.u_epiid AS INT)";

            logger.info(strSql);

            // Ejecuta la consulta y obtiene un DataSet
            DataSet resulQuery = this.getQueryProcessor().getSqlDataSet(strSql);
            // Convierte el DataSet a JSON
            return convertDataSetToJson(resulQuery);

        } catch (Exception e) {
            // Manejo de errores
            String errorMessage = "Se ha presentado un error consultando los EPI -> " + e.getMessage();
            this.logger.error(errorMessage);
            throw new SapphireException(errorMessage);
        }
    }

    /**
     * Convierte un DataSet en un JSONArray con una estructura específica.
     *
     * @param dataSet Objeto DataSet con los datos obtenidos de la consulta SQL.
     * @return Un JSONArray con los datos convertidos.
     * @throws SapphireException Si ocurre un error durante la conversión.
     */
    private JSONArray convertDataSetToJson(DataSet dataSet) throws SapphireException {
        logger.info("Executed method convertDataSetToJson");
        JSONArray jsonArray = new JSONArray();

        try {
            // Obtiene la cantidad de filas en el DataSet
            int rowCount = dataSet.getRowCount();
            logger.info("Cantidad de datos: " + rowCount);

            // Itera sobre las filas del DataSet
            for (int i = 0; i < rowCount; i++) {
                JSONObject jsonObject = new JSONObject();

                // Extrae los valores de cada columna
                jsonObject.put("idEpi", dataSet.getString(i, "u_epiid"));
                jsonObject.put("Epi", dataSet.getString(i, "epidesc", ""));

                // Extrae el CLOB (thumbnailimage) y lo convierte a String
                String clobValue = dataSet.getClob(i, "thumbnailimage", "");
                if (clobValue == null || clobValue.isEmpty()) {
                    jsonObject.put("ImagenEpi", "");
                } else {
                    // Elimina datos innecesarios del CLOB
                    clobValue = clobValue.substring(clobValue.indexOf(",") + 1);
                    jsonObject.put("ImagenEpi", clobValue);
                }

                // Agrega el objeto JSON a la lista
                jsonArray.put(jsonObject);
            }

        } catch (Exception e) {
            // Manejo de errores durante la conversión
            String errorMessage = "Error al procesar el DataSet a JSON -> " + e.getMessage();
            this.logger.error(errorMessage);
            throw new SapphireException(errorMessage);
        }

        return jsonArray;
    }
}

