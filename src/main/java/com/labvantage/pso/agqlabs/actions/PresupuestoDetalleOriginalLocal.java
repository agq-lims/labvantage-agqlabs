package com.labvantage.pso.agqlabs.actions;


import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.xml.PropertyList;

import java.util.HashMap;
import java.util.Map;

/**
 * Autor: Jhon Carlos Solís Ochoa
 * Empresa: AGQ Labs
 * Descripción: Clase para la lectura del servicio de presupuesto detalle..
 */

public class PresupuestoDetalleOriginalLocal extends BaseAction {


    public static final String GET = "GET";
    public static final String POST = "POST";

    /**
     * Procesa una acción que realiza la consulta del presupuesto detalle basado en el codigo del mismo.
     *
     * @param propertyList Objeto que contiene las propiedades del contexto.
     * @throws SapphireException Si ocurre un error durante el proceso.
     */
    public void processAction(PropertyList propertyList) throws SapphireException {

        logger.info("Inicia el procesamiento del presupuesto con CodPresupuesto: " + propertyList.getProperty("codPresupuesto"));
        PropertyList pl = new PropertyList();

        String cookie =
                "client.id=02557d50-48c7-4fbe-9be9-92785cbfb8ab; " +
                        "syracuse.sid.8124=cee92674-7f0f-4036-a79a-b60f00e512c2";

        try {
            Map<String, String> config = obtenerConfiguracionBase();
            String urlService = config.get("URL Base") +  config.get("URL Presupuesto Detalle") + "/" + propertyList.getProperty("codPresupuesto");
            String authorization = "Basic " + config.get("authorization");
            JSONObject joWSAnswer = (JSONObject)WSUtils.getAnswer(urlService, GET, null, authorization, cookie);

            String nombreServicio = "presupuestomodificacion";

            if(!validarExistenciaPresupuesto(propertyList.getProperty("codPresupuesto"))){
                nombreServicio = "presupuestoalta";
            }

            ejecutarPresupuesto(nombreServicio, config.get("URL Labvantage"), joWSAnswer);


        } catch (Exception e) {
            this.logger.error("Failed to process method: " + e.getClass().getName() + " -> " + e.getMessage());
        }
    }


    public void ejecutarPresupuesto(String nombreServicio, String url, JSONObject joWSAnswer) throws Exception {
        logger.info("Ingresamos al metodo para el procesamiento del presupuesto...");

        String token = "Token " + getToken();
        String servicio = url + "/" + nombreServicio;
        String jsonBodyPost = joWSAnswer.toString();


        JSONObject respuestaPost = (JSONObject) WSUtils.getAnswer(
                servicio,
                POST,
                jsonBodyPost,
                token,
                null
        );

        logger.info("Se procesa el presupuesto..." + respuestaPost.toString());

    }

    public Map<String, String> obtenerConfiguracionBase() throws Exception {

        Map<String, String> configuracion = new HashMap<>();

        String sql =
                "select dp.value_string, dp.value_name " +
                        "from u_detparam_sys dp " +
                        "where dp.parameter_sysid = 'Sage X3' " +
                        "and dp.value_name in ('URL Base','authorization', 'URL Presupuesto Detalle', 'URL Labvantage')";

        //DataSet resultQuery = this.getQueryProcessor().getSqlDataSet(sql);
        MockDataSet resultQuery = new MockDataSet();

        resultQuery.addRow("http://esb2023-dev.agqlabs.com:8290/", "URL Base");
        resultQuery.addRow("bGFidmFudGFnZV9kZXY6PmswP3Q1RTRkc1w4", "authorization");
        resultQuery.addRow("ventasAPI/ventas/presupuestos", "URL Presupuesto Detalle");
        resultQuery.addRow("http://10.1.70.10:8080/labvantage/rest/actions", "URL Labvantage");

        if (resultQuery != null && resultQuery.getRowCount() > 0) {

            for (int i = 1; i <= resultQuery.getRowCount(); i++) {

                //String key   = resultQuery.getString(i, "value_name");
                //String value = resultQuery.getString(i, "value_string");
                String key   = resultQuery.getString(i, "value_name");
                String value = resultQuery.getString(i, "value_string");

                if (key != null) {
                    configuracion.put(key, value != null ? value : "");
                }
            }
        }

        return configuracion;
    }

    public Boolean validarExistenciaPresupuesto(String codPresupuesto){
        Boolean existe = true;
        String sql =
                "select count(*) presupuesto\n" +
                        "from u_oferta cp\n" +
                        "where cp.cod_presupuesto  = '" + codPresupuesto + "'";

        //DataSet resultQuery = this.getQueryProcessor().getSqlDataSet(sql);
        MockDataSet resultQuery = new MockDataSet();
        resultQuery.addRow("0", "0");
        logger.info("Valor: " + resultQuery.getString(1, "value_string"));
        logger.info("Valor: " + resultQuery.getString(1, "value_name"));
        //DataSet resultQuery = this.getQueryProcessor().getSqlDataSet(sql);
        if (resultQuery != null && resultQuery.getRowCount() > 0
                && Integer.parseInt(resultQuery.getString(1, "value_string")) == 0){
            existe = false;
        }

         return existe;
    }


    public String getToken(){
        String sql =
                "select tokenvalue\n" +
                        "from authtoken\n" +
                        "where tokenstatus = 'Active'";
        //DataSet resultQuery = this.getQueryProcessor().getSqlDataSet(sql);
        MockDataSet resultQuery = new MockDataSet();
        resultQuery.addRow("0okw-ykdu-kbt[-cty?-ku]T-EXU7-IHFH-TH7X-7H7T-Q", "Token");
        logger.info("Tokem: " + resultQuery.getString(1, "value_string"));
        return resultQuery.getString(1, "value_string");

    }



}
