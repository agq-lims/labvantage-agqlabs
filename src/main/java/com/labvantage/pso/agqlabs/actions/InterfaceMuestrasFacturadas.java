/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.labvantage.pso.agqlabs.actions;

import org.json.JSONArray;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.action.BaseAction;
import sapphire.action.EditSDI;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

/**
 *
 * @author gustavo.rojas
 * Modificado: Jhon Carlos Solis Ochoa
 * Descripción: Se modifica porque se requiere el cambio de estado
 */
public class InterfaceMuestrasFacturadas extends BaseAction {

    private final String CLASSNAME = "InterfaceMuestrasFacturadas";
    private Boolean error = false;
    private final String output = "";
    private String processlog = "";
    private final String messagetag = "";
    private final String url = "";

    /**
     *
     * @param props
     * @throws SapphireException
     */
    @Override
    public void processAction(PropertyList props) throws SapphireException {
        //
        meLogInfo(this.CLASSNAME + " - BEGIN");
        meLogInfo(this.CLASSNAME + " props=" + props.toJSONString());


        String message = props.getProperty("message", "");


        if(message == null && message.trim().isEmpty()){
            this.error = true;
            this.processlog += " ERROR: El campo message está vacío.";
        }

        if(!this.error) {
            //Se leen los datos del JSON


            JSONArray ja =  new JSONArray(message);

            for (int i = 0; i < ja.length(); i++) {

                logger.info("Se realizará el cambio de estado de la muestras");
                registrarEstadoFacturas(ja.getJSONObject(i));

            }

        }


        if(this.error){
            props.setProperty("status", "ERROR");
            props.setProperty("outputmessage", this.processlog);
        }else{
            props.setProperty("status", "OK");
            props.setProperty("outputmessage", this.processlog);
        }

    }


    private void registrarEstadoFacturas(JSONObject jo)  {
        //Registra las muestras
        logger.info("Se ejecuta el método registrarEstadoFacturas");

        String codMuestra = jo.optString("CodMuestra", "").trim();
        boolean facturadoBool = jo.optBoolean("Facturado", false);
        String valorFacturado = facturadoBool ? "Y" : "N";

        logger.info("Muestra: " + codMuestra + ", Estado: " + valorFacturado);
        if (!codMuestra.isEmpty()) {

            String strSQL =
                    "select ss.requestitemid " +
                            "from s_sample ss " +
                            "where ss.u_samplelabel = '" + codMuestra + "' " +
                            "and ss.u_mode is null";

            this.meLogInfo(this.CLASSNAME + " SQL=" + strSQL);

            DataSet dsSamples = this.getQueryProcessor().getSqlDataSet(strSQL);

            if (dsSamples != null && dsSamples.getRowCount() > 0) {

                PropertyList plEditSDI = new PropertyList();
                plEditSDI.setProperty("sdcid", "LV_RequestItem");
                plEditSDI.setProperty("keyid1",
                        dsSamples.getColumnValues("requestitemid", ";"));
                plEditSDI.setProperty("u_facturada", valorFacturado);

                this.meLogInfo(this.CLASSNAME +
                        " plEditSDI=" + plEditSDI.toJSONString());

                try {

                    this.getActionProcessor().processAction(
                            EditSDI.ID,
                            EditSDI.VERSIONID,
                            plEditSDI
                    );
                    this.processlog += (codMuestra + " ");

                } catch (ActionException e) {
                    this.error = true;
                    this.processlog +=  " ERROR procesando JSON principal -> " + e.getMessage();
                }

            }else{
                this.error = true;
                this.processlog +=  " ERROR: CodMuestra vacío  " + codMuestra;
            }

        }else{
            this.error = true;
            this.processlog +=  " ERROR: No se encontró requestitemid para CodMuestra=" + codMuestra;
        }

    }


    private void meLogInfo(String strInfo) {
        logger.info(strInfo);
        //this.processlog += "\n" + strInfo;
    }

}
