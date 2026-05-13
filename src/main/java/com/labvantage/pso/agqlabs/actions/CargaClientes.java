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

public class CargaClientes extends BaseAction {


    public static final String POST = "POST";
    public static final String PUT = "PUT";
    public static final String GET = "GET";


    /**
     * Procesa una acción que realiza la consulta de las muestras extendidas.
     *
     * @param propertyList Objeto que contiene las propiedades del contexto.
     * @throws SapphireException Si ocurre un error durante el proceso.
     */
    @Override
    public void processAction(PropertyList propertyList) throws SapphireException {

        String cantidad = propertyList.getProperty("cantidad");
        logger.info("Inicia el procesamiento de carga de los (" + cantidad + ") clientes: " );

        String cookie =
                "client.id=02557d50-48c7-4fbe-9be9-92785cbfb8ab; " +
                        "syracuse.sid.8124=cee92674-7f0f-4036-a79a-b60f00e512c2";

        try {

            getDataCustomer(Integer.parseInt(cantidad));

        } catch (Exception e) {
            this.logger.error("Failed to process method getDataCustomer: " + e.getClass().getName() + " -> " + e.getMessage());
        }
    }


    private JSONObject obtenerClientes(Map<String, String> config, String codCliente, String cookie){
        logger.info("Ingresamos al método para consultar los clientes faltantes obtenerClientes()... " + codCliente);


        String urlService = config.get("URL Base") + "clientesAPI/clientes/" + codCliente;
        String authorization = "Basic " + config.get("authorization");


        logger.info("URL Service: " + urlService);
        logger.info("Token: " + authorization);

        JSONObject joWSAnswer;

        try {
            JSONObject root = (JSONObject)WSUtils.getAnswer(urlService, GET, null, authorization, cookie);
            logger.info("Se hizo la lectura de los datos: JSONObject root");
            joWSAnswer = root;
        } catch (Exception e) {
            logger.error("Se ha presentado un error procesando el servicio "  + e.getMessage());
            throw new RuntimeException(e);
        }

        return joWSAnswer;


    }


    public void ejecutarCreacionClientes(Map<String, String> config, String body, String cookie) throws Exception {
        logger.info("Ingresamos al método para el procesamiento de creación de clientes ejecutarCeacionClientes...");


        String urlService = config.get("URL Labvantage") +  "/clientes";
        String authorization = "Token " + getToken();
        logger.info("Authorization: " + authorization);
        logger.info("urlService: " + urlService);

        JSONObject respuestaPost = (JSONObject) WSUtils.getAnswer(
                urlService,
                POST,
                body,
                authorization,
                null
        );

        logger.info("Se registra el cliente..." + respuestaPost.toString());

    }

    public void getDataCustomer(int cantidad){
        logger.info("Ejecuta el método getDataCustomer() y se obtienen los primeros N registros");

        Map<String, String> config = obtenerConfiguracionBase();

        String sql =
                "SELECT TOP (" + cantidad + ") t.codcliente\n" +
                        "FROM u_clientestmp t\n" +
                        "WHERE 1=1\n" +
                        "AND t.clientereal = '1'\n" +
                        "AND NOT EXISTS (\n" +
                        "    SELECT 1\n" +
                        "    FROM address a\n" +
                        "    WHERE a.u_customerid = t.codcliente\n" +
                        "      AND a.addresstype = 'Customer'\n" +
                        ")\n" +
                        "AND NOT (t.codcliente IS NULL\n" +
                        "   OR LTRIM(RTRIM(t.codcliente)) = ''\n" +
                        "   OR t.codcliente LIKE '%FALTA%'\n" +
                        "   OR t.codcliente NOT LIKE '%[^0-9]%'\n" +
                        ")\n" +
                        "ORDER BY \n" +
                        "    CASE \n" +
                        "        WHEN t.codcliente LIKE 'ES20%' THEN 0 \n" +
                        "        ELSE 1 \n" +
                        "    END, \n" +
                        "    t.codcliente";

        DataSet resultQuery = this.getQueryProcessor().getSqlDataSet(sql);

        if (resultQuery != null && resultQuery.getRowCount() > 0) {

            for (int i = 0; i < resultQuery.getRowCount(); i++) {

                String codCliente = resultQuery.getValue(i, "codcliente");
                logger.info("Se busca el cliente: " + codCliente);

                try {
                    JSONObject joWSAnswer = obtenerClientes(config, codCliente, null);

                    // 🔥 VALIDACIÓN CLAVE
                    if (joWSAnswer == null || joWSAnswer.length() == 0) {
                        logger.warn("El cliente no existe en el servicio externo: " + codCliente);
                        continue; // 👈 pasa al siguiente cliente
                    }

                    JSONObject wrapper = new JSONObject();
                    wrapper.put("message", joWSAnswer.toString());

                    String jsonBodyPost = wrapper.toString();
                    logger.info("Se ha generado el JSON: " + jsonBodyPost);

                    ejecutarCreacionClientes(config, jsonBodyPost, null);

                } catch (Exception e) {
                    logger.error("Error procesando cliente " + codCliente + ": " + e.getMessage());
                    // 👇 continúa con el siguiente cliente
                }
            }
        } else {
            logger.info("No se encontraron clientes para procesar.");
        }
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
