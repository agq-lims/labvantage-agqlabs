package com.labvantage.pso.agqlabs.actions;


import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

import java.util.HashMap;
import java.util.Map;

/**
 * Autor: Jhon Carlos Solís Ochoa
 * Empresa: AGQ Labs
 * Descripción: Clase para la lectura del servicio de muestras extendidas..
 */

public class MuestraExtendida extends BaseAction {


    public static final String POST = "POST";
    public static final String PUT = "PUT";
    public static final String COD_MUESTRA = "CodMuestra";

    /**
     * Procesa una acción que realiza la consulta de las muestras extendidas.
     *
     * @param propertyList Objeto que contiene las propiedades del contexto.
     * @throws SapphireException Si ocurre un error durante el proceso.
     */
    @Override
    public void processAction(PropertyList propertyList) throws SapphireException {

        String codMuestra = propertyList.getProperty(COD_MUESTRA);
        logger.info("Inicia el procesamiento de las muestras extendidas: " + codMuestra);

        String cookie =
                "client.id=02557d50-48c7-4fbe-9be9-92785cbfb8ab; " +
                        "syracuse.sid.8124=cee92674-7f0f-4036-a79a-b60f00e512c2";

        try {
            Map<String, String> config = obtenerConfiguracionBase();
             //Consultar las muestras extendidas
            JSONObject joWSAnswer = obtenerMuestrasExtendidas(config, codMuestra, null);
            String jsonBodyPost = joWSAnswer.toString();
            logger.info("Se ha generado el JSON: " + jsonBodyPost);
            ejecutarMuestraExtendida(config,jsonBodyPost,cookie);


        } catch (Exception e) {
            this.logger.error("Failed to process method: " + e.getClass().getName() + " -> " + e.getMessage());
        }
    }


    private JSONObject obtenerMuestrasExtendidas(Map<String, String> config, String codMuestra, String cookie){
        logger.info("Ingresamos al método para consultar las muestras extendidas obtenerMuestrasExtendidas()... " + codMuestra);


        String urlService = config.get("URL Labvantage") + "/" + config.get("URL Consulta Muestras Extendidas");
        String authorization = "Token " + getToken();

        logger.info("URL Service: " + urlService);
        logger.info("Token: " + authorization);


        JSONObject json = new JSONObject();
        JSONObject joWSAnswer;
        json.put("CodMuestra", codMuestra);

        String body = json.toString();

        try {
            JSONObject root = (JSONObject)WSUtils.getAnswer(urlService, POST, body, authorization, cookie);
            logger.info("Se hizo la lectura de los datos: JSONObject root");
            JSONObject output = root.getJSONObject("output");
            joWSAnswer = new JSONObject(output.getString("responsemessage"));
        } catch (Exception e) {
            logger.error("Se ha presentado un error procesando el servicio "  + e.getMessage());
            throw new RuntimeException(e);
        }

        return joWSAnswer;


    }


    public void ejecutarMuestraExtendida(Map<String, String> config, String body, String cookie) throws Exception {
        logger.info("Ingresamos al metodo para el procesamiento de muestras extendidas ejecutarMuestraExtendida...");


        String urlService = config.get("URL Base") +  config.get("URL Muestras Extendido");
        String authorization = "Basic " + config.get("authorization");
        logger.info("Authorization: " + authorization);
        logger.info("urlService: " + urlService);

        JSONObject respuestaPost = (JSONObject) WSUtils.getAnswer(
                urlService,
                PUT,
                body,
                authorization,
                null
        );

        logger.info("Se procesan las muestras extendidas..." + respuestaPost.toString());

    }

    public Map<String, String> obtenerConfiguracionBase() {
        logger.info("Ejecuta el método obtenerConfiguracionBase()");
        Map<String, String> configuracion = new HashMap<>();

        String sql =
                "select dp.value_string, dp.value_name " +
                        "from u_detparam_sys dp " +
                        "where dp.parameter_sysid = 'Sage X3' " +
                        "and dp.value_name in ('URL Base','authorization', 'URL Muestras Extendido', 'URL Labvantage', 'URL Consulta Muestras Extendidas')";

        DataSet resultQuery = this.getQueryProcessor().getSqlDataSet(sql);

        if (resultQuery != null && resultQuery.getRowCount() > 0) {

            for (int i = 0; i <= resultQuery.getRowCount(); i++) {

                String key   = resultQuery.getString(i, "value_name");
                String value = resultQuery.getString(i, "value_string");

                if (key != null) {
                    configuracion.put(key, value != null ? value : "");
                }
            }
        }

        return configuracion;
    }



    public String getToken(){
        String sql =
                "select tokenvalue\n" +
                        "from authtoken\n" +
                        "where tokenstatus = 'Active'";
        DataSet resultQuery = this.getQueryProcessor().getSqlDataSet(sql);
        return resultQuery.getString(0, "tokenvalue");

    }


}
