package com.labvantage.pso.agqlabs.actions;


import org.json.JSONArray;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.action.BaseAction;
import sapphire.xml.PropertyList;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class AcreditacionAgqLabs extends BaseAction {

    private final PropertyList acreditacion;


    public AcreditacionAgqLabs() {

        acreditacion = new PropertyList();

    }

    @Override
    public void processAction(PropertyList pl) throws SapphireException {
        //Se reciben los parametros y s obtienen los datos, si llega el campo ID es una modificación en caso contrario es una creación

        String acreditacionjson = pl.get("json").toString();
        parseJSON(acreditacionjson);

    }



    private void parseJSON(String jsonString) throws SapphireException {


        String codigoAcreditacion = "";

        JSONObject jsonObject = new JSONObject(jsonString);


        ActionProcessor actionProcessor = getActionProcessor();

        String createUser = this.connectionInfo.getSysuserId();
        String posicion = "Posición 1";
        LocalDateTime now = LocalDateTime.now();


        // Rellenar las propiedades de "acreditacion"
        acreditacion.setAttribute("sdcid","Acreditacion");
        acreditacion.setAttribute("createby", createUser);
        acreditacion.setAttribute("createdt", now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        acreditacion.setAttribute("createtool", "AddSDI");
        acreditacion.setAttribute("acreditaciondesc", jsonObject.getString("acreditaciondesc"));
        acreditacion.setAttribute("empresa", jsonObject.getString("empresa"));
        acreditacion.setAttribute("estado", jsonObject.getString("estado"));
        acreditacion.setAttribute("marca", jsonObject.getString("marca"));
        acreditacion.setAttribute("no_marca", jsonObject.getString("no_marca"));
        acreditacion.setAttribute("porcen_acreditacion", jsonObject.getString("porcen_acreditacion"));
        acreditacion.setAttribute("posicion", posicion);


        try{
            logger.info("Se inicia el registro de datos de acreditación...");
            actionProcessor.processAction("AddSDI", "1", acreditacion);
            codigoAcreditacion = acreditacion.get("newkeyid1").toString();

        }catch (Exception e){
            String errMsg = String.format("Se ha generado error al almacenar la acreditción: %s, %s", e.getMessage(), e.getCause().toString());
            logger.error(errMsg);
            throw new SapphireException(errMsg);
        }


        // Rellenar las propiedades de "leyenda"
        JSONArray leyendasArray = jsonObject.getJSONArray("leyendas");
        for (int i = 0; i < leyendasArray.length(); i++) {
            JSONObject leyendaObject = leyendasArray.getJSONObject(i);
            PropertyList leyendaProps = new PropertyList();

            // Guardar cada campo en `leyendaProps`
            leyendaProps.setAttribute("u_acreditacionid",codigoAcreditacion);
            leyendaProps.setAttribute("tipo", leyendaObject.getString("tacreditacion"));
            leyendaProps.setAttribute("idioma", leyendaObject.getString("idioma"));
            leyendaProps.setAttribute("leyendanoacreditada", leyendaObject.getString("lnoacreditada"));
            leyendaProps.setAttribute("leyendaacreditada", leyendaObject.getString("lacreditada"));
            leyendaProps.setAttribute("codacreditacion", codigoAcreditacion);
            leyendaProps.setAttribute("posicion", posicion);

            // Agregar las propiedades de esta leyenda a la lista
            try{
                actionProcessor.processAction("AddSDIDetail","1", leyendaProps);
            }catch (Exception e){
                String errMsg = "Error en el registro de detalles de acreditación: " + e.getMessage() + ": " + e.getCause().toString();
                logger.error(errMsg);
                throw new SapphireException(errMsg);
            }
        }
    }




}
