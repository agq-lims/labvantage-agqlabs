package com.labvantage.pso.agqlabs.actions;



import org.json.JSONArray;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

import java.util.HashMap;
import java.util.Map;

/**
 * Autor: Jhon Carlos Solís Ochoa
 * Empresa: AGQ Labs
 * Descripción: Servicio para el registro de request..
 */

public class AddRequestService extends BaseAction {

    public static final int CANTIDAD_CARACTERES = 6;
    private boolean error = false;
    private final StringBuilder errorMessage = new StringBuilder();
    private final Map<String, FieldProcessor> itemFieldProcessors = new HashMap<>();
    private String request = "";
    private String codPresupuesto = "";


    /**
     * Procesa una acción que realiza el registro de request desde sistemas externos (SIL).
     *
     * @param propertyList Objeto que contiene las propiedades del contexto.
     * @throws SapphireException Si ocurre un error durante el proceso.
     */

    public void processAction(PropertyList propertyList) throws SapphireException {

        logger.info("Inicia el proceso de registro de request en el sistema");
        initItemFieldProcessors();

        // Determinar y ejecutar la estrategia apropiada
        request = resolveRequestId(propertyList);

        if (request.isEmpty()) {
            setErrorResponse(propertyList, "No se pudo determinar el request ID");
            return;
        }

        logger.info("Request a utilizar: " + request);

        addRequestItem(propertyList,request);


        setFinalResponse(propertyList);

    }

    /**
     * Determina y resuelve el request ID basado en la estrategia apropiada.
     * Reemplaza el if-else if-else con lógica declarativa.
     */
    private String resolveRequestId(PropertyList propertyList) {
        String requestIdFromInput = propertyList.getProperty("requestid", "").trim();

        // Estrategia 1: No viene requestid → Crear nuevo
        if (requestIdFromInput.isEmpty()) {
            logger.info("No se proporcionó requestid, creando nuevo request");
            return addRequest(propertyList);
        }

        // Estrategia 2: Viene requestid y existe → Usar existente
        if (existeRequest(requestIdFromInput)) {
            logger.info("Request existente encontrado: " + requestIdFromInput);
            return requestIdFromInput;
        }

        // Estrategia 3: Viene requestid pero no existe → Crear nuevo (ignorar el proporcionado)
        logger.warn("El requestid " + requestIdFromInput + " no existe, creando nuevo request");
        return addRequest(propertyList);
    }


    /**
     * Valida si un request existe en la base de datos.
     */
    private boolean existeRequest(String requestId) {
        logger.info("Validando existencia del request: " + requestId);

        String sql = "select count(1) as total from s_request sr where sr.s_requestid = '" + requestId + "'";

        try {
            DataSet ds = this.getQueryProcessor().getSqlDataSet(sql);

            if (ds != null && ds.getRowCount() > 0) {
                int count = Integer.parseInt(ds.getValue(0, "total", "0"));
                boolean exists = count > 0;
                logger.info("Request " + requestId + " existe: " + exists);
                return exists;
            }
        } catch (Exception e) {
            logger.error("Error validando existencia del request " + requestId + ": " + e.getMessage());
        }

        return false;
    }


    /**
     * Establece la respuesta final basada en el estado de error.
     */
    private void setFinalResponse(PropertyList propertyList) {
        if (this.error) {
            setErrorResponse(propertyList, this.errorMessage.toString());
        } else {
            setSuccessResponse(propertyList, request);
        }
    }

    /**
     * Establece respuesta de error en el PropertyList.
     */
    private void setErrorResponse(PropertyList propertyList, String message) {
        propertyList.setProperty("status", "ERROR");
        propertyList.setProperty("code", "400");
        propertyList.setProperty("outputmessage", message);
        propertyList.setProperty("requestId", request);
    }

    /**
     * Establece respuesta exitosa en el PropertyList.
     */
    private void setSuccessResponse(PropertyList propertyList, String requestId) {
        propertyList.setProperty("status", "OK");
        propertyList.setProperty("code", "200");
        propertyList.setProperty("outputmessage", "OK");
        propertyList.setProperty("requestId", requestId);
    }



    private void addRequestItem(PropertyList props, String requestId)  {
        logger.info("Inicia el registro del requestitem " + requestId);

        // 2. Obtener el arreglo de items
        String itemsStr = props.getProperty("items");

        if (itemsStr == null || itemsStr.isEmpty()) {
            String message = "El JSON no contiene items para procesar";
            logger.warn(message);
        }


        JSONArray itemsArray = new JSONArray(itemsStr);


        // 2. Campos permitidos del item
        String[] itemFields = {
                "u_ofertaid",
                "productid",
                "requestitemstatus",
                "shippinglocationdepartmentid",
                "sitedepartmentid",
                "u_receiveddt",
                "activeflag",
                "u_sampletype",
                "u_samplesubtype",
                "u_horamuestreo",
                "u_puntodemuestreo",
                "u_direccionid",
                "u_requestdt",
                "u_proyectoid",
                "u_pntmuestreo",
                "u_proyectoetapa",
                "u_conditionlabel",
                "requestitemstatus_dup",
                "u_muestreador",
                "u_lote",
                "u_npedidocliente",
                "u_clientetercero",
                "u_direcciontercero",
                "u_observaciones_muestra",
                "u_observaciones_informe",
                "u_leadtime",
                "u_clientesecundario",
                "u_descripcion",
                "u_ofertaatid",
                "u_entregadacliente"
        };

        // 3. Iterar cada item
        for (int i = 0; i < itemsArray.length(); i++) {

            JSONObject itemJson = itemsArray.getJSONObject(i);

            // 4. Crear PropertyList por item
            PropertyList itemPL = new PropertyList();

            // 5. Campos fijos requeridos por LabVantage
            itemPL.setProperty("sdcid", "LV_RequestItem");
            itemPL.setProperty("templatesdcid", "Sample");
            itemPL.setProperty("requestid", requestId);
            itemPL.setProperty("appliedflag", "N");
            itemPL.setProperty("u_autorecibir", "Y");
            itemPL.setProperty("itemcount", "1");
            itemPL.setProperty("usersequence", "1");

            // 6. Cargar dinámicamente los campos del JSON
            for (int f = 0; f < itemFields.length; f++) {

                String field = itemFields[f];
                String value = itemJson.optString(field, "");

                FieldProcessor processor = itemFieldProcessors.get(field);


                if(processor != null){
                    try {
                        processor.process(value, itemJson, itemPL);
                    } catch (SapphireException e) {
                        error=true;
                        String errorMsg = "Error generado al procesar el registro: " + e.getMessage();
                        errorMessage.append(errorMsg);
                        logger.error(errorMsg);

                    }
                }else {
                    itemPL.setProperty(
                            field.toLowerCase(),
                            value
                    );
                }

            }

            // 7. Ejecutar AddSDI para el item
            try {
                getActionProcessor().processAction("AddSDI", "1", itemPL);
            } catch (ActionException e) {
                error=true;
                String errorMsg = "Error generado al procesar el registro: " + e.getMessage();
                errorMessage.append(errorMsg);
                logger.error(errorMsg);
            }

            // 8. Limpiar PropertyList para evitar solapes
            itemPL.clear();
        }
    }


    private String addRequest(PropertyList props)  {
        logger.info("Se inicia el registro del request");
        // 2. Crear PropertyList para el encabezado
        PropertyList requestPL = new PropertyList();
        requestPL.setProperty("sdcid", "request");
        requestPL.setProperty("requesttype", "Routine");
        requestPL.setProperty("autoreleaseflag", "Y");
        requestPL.setProperty("requestclass", "Submission");
        requestPL.setProperty("creationrule", "OnAcceptance");

        // 2. Copiar campos desde props (EXCEPTO items)
        String[] fields = {
                "requestdt",
                "submitbydepartmentid",
                "u_fechallegada",
                "u_agencias",
                "u_cod_albaran",
                "inv_payeraddressid",
                "u_fechadentrega",
                "u_tipodeenvase",
                "u_envase",
                "u_nbultos",
                "u_portespagados",
                "u_fechadeenvio",
                "u_precio",
                "u_idmoneda",
                "u_idorigen",
                "u_iddestino",
                "u_pesototal",
                "u_volumetrico",
                "u_alto",
                "u_ancho",
                "u_largo",
                "u_idincidenciatransporte",
                "u_tipo",
                "u_facturarincidencia",
                "u_motivoincidencia",
                "u_tarifado",
                "notes"
        };

        // 3. Recorrer el JSON y cargar el PropertyList
        for (String field : fields) {

                String value = getPropertyOrEmpty(props, field);
                FieldProcessor processor = itemFieldProcessors.get(field);

                if (processor != null) {
                    try {
                        processor.process(value, null, requestPL);
                    } catch (SapphireException e) {
                        error=true;
                        String errorMsg = "Error generado al procesar el registro: " + e.getMessage();
                        errorMessage.append(errorMsg);
                        logger.error(errorMsg);
                    }
                } else {
                    requestPL.setProperty(
                            field.toLowerCase(),
                            value
                    );
                }

        }


        try {
            getActionProcessor().processAction("AddSDI", "1", requestPL);
        } catch (ActionException e) {
            error=true;
            String errorMsg = "Error generado al procesar el registro: " + e.getMessage();
            errorMessage.append(errorMsg);
            logger.error(errorMsg);
        }
        return requestPL.getProperty("newkeyid1");
        //Adicionar el registro y cargar los datos con el AddSDI


    }



    public void logPropertyList(PropertyList pl) {

        if (pl == null || pl.isEmpty()) {
            logger.info("PropertyList vacío o nulo");
            return;
        }

        for (Object keyObj : pl.keySet()) {

            String key = keyObj.toString();
            String value = pl.getProperty(key);

            logger.info(key + ": " + value);
        }
    }


    private DataSet obtenerAddressIdCliente(String customerId) {

        logger.info("Entro a procesar el cliente: " + customerId);
        String sql =
                "select ad.addressid, ad.addressdesc " +
                        "from address ad " +
                        "where ad.addresstype = 'Customer' " +
                        "and ad.u_customerid = '" + customerId + "'";


       DataSet ds = this.getQueryProcessor().getSqlDataSet(sql);

        if (ds != null || ds.getRowCount() > 0) {
            return ds;
        }

        String errorMsg = "No se encontraron datos para el cliente: " + customerId;
        error = true;
        errorMessage.append(errorMessage);
        logger.error(errorMsg);
        return new DataSet();

    }



    private void initItemFieldProcessors() {

        // Cliente principal → afecta 2 campos
        itemFieldProcessors.put("inv_payeraddressid", (value, itemJson, pl) -> {

            DataSet ds = obtenerAddressIdCliente(value);

            if (ds != null && ds.getRowCount() > 0) {
                pl.setProperty("inv_payeraddressid", ds.getValue(0, "addressid"));
                pl.setProperty("u_nombrecliente", ds.getValue(0, "addressdesc"));
            }
        });

        // Cliente secundario
        itemFieldProcessors.put("u_clientesecundario", (value, itemJson, pl) -> {

            DataSet ds = obtenerAddressIdCliente(value);

            if (ds != null && ds.getRowCount() > 0) {
                pl.setProperty("u_clientesecundario", ds.getValue(0, "addressid"));
            }
        });

        itemFieldProcessors.put("u_samplesubtype", (value, itemJson, pl) -> {
            pl.setProperty("u_samplesubtype", formatearIdTipoMuestra(value));
        });

        itemFieldProcessors.put("productid", (value, itemJson, pl) -> {

            DataSet pr = obtenerProductoPorAnalisisTipo(value);

            if (pr != null && pr.getRowCount() > 0) {
                pl.setProperty("productid", pr.getValue(0, "productid"));
                pl.setProperty("productversionid", pr.getValue(0, "productversionid"));
                pl.setProperty("u_centro_produccion", pr.getValue(0, "sitedepartmentid"));
                pl.setProperty("u_cod_familia", pr.getValue(0, "u_familia_cod"));
            }


        });

        itemFieldProcessors.put("u_sampletype", (value, itemJson, pl) -> {

            String tipoMuestra = formatearIdTipoMuestra(value);
            DataSet st = obtenerTipoMuestra(tipoMuestra);

            if (st != null && st.getRowCount() > 0) {
                pl.setProperty("u_sampletype", st.getValue(0, "sampletypeid"));
                pl.setProperty("u_nombresampletype", st.getValue(0, "sampletypedesc"));
            }

        });

        itemFieldProcessors.put("u_ofertaid", (value, itemJson, pl) -> {
            codPresupuesto = getOfertaId(value);
            pl.setProperty("u_ofertaid", codPresupuesto);
        });

        itemFieldProcessors.put("submitbydepartmentid", (value, itemJson, pl) -> {
            pl.setProperty("submitbydepartmentid", value);
            pl.setProperty("sitedepartmentid", value);
        });

        itemFieldProcessors.put("u_direccionid", (value, itemJson, pl) -> {

            DataSet pr = getAddressContact(request);
            pl.setProperty("u_direccionid", pr.getValue(0, "addressid"));

        });


        itemFieldProcessors.put("u_ofertaatid", (value, itemJson, pl) -> {
            pl.setProperty("u_ofertaatid", getIdOfertaAT(value, codPresupuesto));
        });


        itemFieldProcessors.put("u_agencias", (value, itemJson, pl) -> {
            pl.setProperty("u_agencias", getAgencia(value));
        });

        itemFieldProcessors.put("u_entregadacliente", (value, itemJson, pl) -> {
            pl.setProperty("u_entregadacliente", yesNoData(value));
        });

    }


    private String getPropertyOrEmpty(PropertyList props, String propertyName) {
        logger.info("Ejecutando método getPropertyOrEmpty... " + propertyName);
        if (props == null || propertyName == null) {
            return "";
        }

        String value = props.getProperty(propertyName);

        if (value == null) {
            return "";
        }

        value = value.trim();

        if (value.isEmpty() || "null".equalsIgnoreCase(value)) {
            return "";
        }

        return value;
    }

    private String formatearIdTipoMuestra(String valor) {
        logger.info("Se formatea el idTipoMuestra: " + valor);
        if (valor == null || valor.trim().isEmpty()) {
            return "";
        }

        valor = valor.trim();

        // Si tiene 6 o más caracteres, se deja tal cual
        if (valor.length() >= CANTIDAD_CARACTERES) {
            return valor;
        }

        // Rellenar con ceros a la izquierda hasta 6
        String idTipoMuestra = String.format("%06d", Integer.parseInt(valor));
        logger.info("IdTipoMuestra: " + idTipoMuestra);
        return idTipoMuestra;
    }



    private DataSet obtenerTipoMuestra(String idTipoMuestra) {
        logger.info("Ejecutando método obtenerTipoMuestra... " + idTipoMuestra);

        if (idTipoMuestra == null || idTipoMuestra.trim().isEmpty()) {
            String errorMsg = "No se recibe el tipo de muestra" ;
            error = true;
            errorMessage.append(errorMsg);
            logger.error(errorMsg);
            return new DataSet();
        }


        String sql =
                "select s_sampletypeid as sampletypeid, sampletypedesc\n" +
                        "from s_sampletype ss \n" +
                        "where s_sampletypeid = '" + idTipoMuestra + "'" ;

        DataSet ds = this.getQueryProcessor().getSqlDataSet(sql);

        if (ds.getRowCount() > 0) {
            return ds;
        }

        String errorMsg = "No esta registrado el tipo de muestra " + idTipoMuestra ;
        error = true;
        errorMessage.append(errorMsg);
        logger.error(errorMsg);

        return new DataSet();

    }


    private DataSet obtenerProductoPorAnalisisTipo(String idAnalisisTipo) throws SapphireException {
        logger.info("Ejecutando método obtenerProductoPorAnalisisTipo... " + idAnalisisTipo);
        String errorMsg;

        if (idAnalisisTipo == null || idAnalisisTipo.trim().isEmpty()) {
            errorMsg = "No se recibio un ID de analisis tipo ";
            errorMessage.append(errorMsg);
            error = true;
            logger.error(errorMsg);
            throw new SapphireException(errorMsg);
        }


        String sql =
                "select TOP 1 sp.s_productid as productid, s_productversionid as productversionid, sitedepartmentid, u_familia_cod " +
                        "from s_product sp " +
                        "where sp.u_id_analisis_tipo = '" + idAnalisisTipo + "' ORDER BY s_productversionid DESC" ;

        DataSet ds = this.getQueryProcessor().getSqlDataSet(sql);
        logger.info("Cantidad: " + ds.getRowCount() + " Valor: " + ds.getValue(0, "productid"));
        if (ds.getRowCount() > 0) {

            return ds;
        }

        errorMsg = "No esta registrado el análisis tipo de código: " + idAnalisisTipo;
        errorMessage.append(errorMsg);
        error = true;
        logger.error(errorMsg);

        throw new SapphireException(errorMsg);


    }


    private String getOfertaId(String pCodPresupuesto) {
        logger.info("Ejecutando método getOfertaId... " + pCodPresupuesto);
        String errorMsg;


        String strSQL = "select u_ofertaid from u_oferta where cod_presupuesto='" + pCodPresupuesto + "'";
        DataSet dsTemp = getQueryProcessor().getSqlDataSet(strSQL);
        if (dsTemp.getRowCount() > 0) {
            return dsTemp.getString(0, "u_ofertaid", "");
        }

        errorMsg = "No existe una oferta registrada con el código:  " + pCodPresupuesto;
        errorMessage.append(errorMsg);
        error = true;
        logger.error(errorMsg);
        return "";
    }


    private DataSet getAddressContact(String requestId){
        String sql = "select top 1 *\n" +
                "from address a \n" +
                "where a.addresstype = 'Customercontact'\n" +
                "and EXISTS ( select  s_requestid from s_request sr where sr.s_requestid ='" + requestId + "' and a.u_contactcustomerid =  sr.inv_payeraddressid )";

        DataSet ds = this.getQueryProcessor().getSqlDataSet(sql);

        if (ds.getRowCount() > 0) {

            return ds;
        }

        String errorMsg = "El cliente no posee contactos registrados ";
        errorMessage.append(errorMsg);
        error = true;
        logger.error(errorMsg);

        return new DataSet();

    }


    private String getIdOfertaAT(String codLinea, String codOferta){
        String sql = "select uo2.u_ofertaatid from u_oferta uo " +
                "inner join u_ofertaat uo2 " +
                "on uo.u_ofertaid = uo2.ofertaid " +
                "where uo2.codlinea = '" + codLinea + "' and uo.u_ofertaid = '" + codOferta + "'";

        DataSet ds = this.getQueryProcessor().getSqlDataSet(sql);

        if (ds.getRowCount() > 0) {

            return ds.getValue(0, "u_ofertaatid");
        }

        String errorMsg = "No existe la linea " + codLinea + " registrada, en la oferta de código: " + codOferta;
        errorMessage.append(errorMsg);
        error = true;
        logger.error(errorMsg);

        return "";

    }

    private String getAgencia(String codAgencia){
        String sql = "select agenciasdesc from u_agencias where cod_agencia = '" + codAgencia + "'";

        DataSet ds = this.getQueryProcessor().getSqlDataSet(sql);

        if (ds.getRowCount() > 0) {

            return ds.getValue(0, "agenciasdesc");
        }

        String errorMsg = "No existe la agencia: " + codAgencia;
        errorMessage.append(errorMsg);
        //error = true;
        logger.error(errorMsg);

        return codAgencia;

    }

    private String yesNoData(String data){
        String result = "N";
        if(!data.isBlank() || data.equals("true")){
            result = "Y";
        }
        return result;
    }



}
