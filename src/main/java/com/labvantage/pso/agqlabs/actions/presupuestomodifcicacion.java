/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.labvantage.pso.agqlabs.actions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.action.BaseAction;
import sapphire.xml.PropertyList;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.action.AddSDI;
import sapphire.action.EditSDI;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.StringUtil;

/**
 *
 * @author gustavo.rojas
 */
public class presupuestomodifcicacion extends BaseAction {

    private String CLASSNAME = "proveedor";
    private Boolean error = false;
    private String output = "";
    private String processlog = "";
    private String messagetag = "";
    private String url = "";

    private int status;

    public void setStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

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
        //
        String pCodPresupuesto = props.getProperty("CodPresupuesto", "");
        String pEstado = props.getProperty("Estado", "");
        String pFechaUltimaAceptacion = props.getProperty("FechaUltimaAceptacion", "");
        String pVersion = props.getProperty("Version", "");
        //
        if (!"".equals(pCodPresupuesto)) {
            this.error = true;
            this.output = "No se encuentra un valor para CodPresupuesto.";
        } else if (!"".equals(pEstado)) {
            this.error = true;
            this.output = "No se encuentra un valor para Estado.";
        } else if (!"".equals(pFechaUltimaAceptacion)) {
            this.error = true;
            this.output = "No se encuentra un valor para FechaUltimaAceptacion.";
        } else if (!"".equals(pVersion)) {
            this.error = true;
            this.output = "No se encuentra un valor para Version.";
        }
        //
        if (!this.error) {
            String vOfertaId = getOfertaId(pCodPresupuesto);
            if (!"".equals(vOfertaId)) {
                PropertyList plData = new PropertyList();
                plData.setProperty("keyid1", vOfertaId);
                plData.setProperty("estado", pEstado);
                plData.setProperty("fecha_aceptacion", pFechaUltimaAceptacion.replace("T", " "));
                plData.setProperty("version", pVersion);
                plData.setProperty("sdcid", "Oferta");
                getActionProcessor().processAction("EditSDI", "1", plData);
            } else {
                this.error = true;
                this.output = "No se encuentra el presupuesto con dódigo: " + pCodPresupuesto;
            }
        }
        //
        if (this.error) {
            props.setProperty("status", "ERROR");
            props.setProperty("outputmessage", this.output);
        } else {
            props.setProperty("status", "OK");
            props.setProperty("outputmessage", "OK. Presupuesto " + pCodPresupuesto + " actualizado.");
        }
    }

    private String getCountry(String countryCod) {
        String result = "";
        String strSQL = "select u_countriesid idpais, codpais from u_countries where idpais='" + countryCod + "'";
        DataSet dsTemp = getQueryProcessor().getSqlDataSet(strSQL);
        if (dsTemp.getRowCount() > 0) {
            result = dsTemp.getString(0, "idpais") + "|" + dsTemp.getString(0, "codpais");
        }
        return result;
    }

    private String getOfertaId(String pCodPresupuesto) {
        String result = "";
        String strSQL = "select u_ofertaid from u_oferta where cod_presupuesto='" + pCodPresupuesto + "'";
        DataSet dsTemp = getQueryProcessor().getSqlDataSet(strSQL);
        if (dsTemp.getRowCount() > 0) {
            result = dsTemp.getString(0, "u_ofertaid", "");
        }
        return result;
    }

    private String fixValBool(String oldVal) {
        String newVal = oldVal;
        if ("true".equalsIgnoreCase(newVal)) {
            newVal = "Y";
        } else if ("false".equalsIgnoreCase(newVal)) {
            newVal = "N";
        }
        return newVal;
    }

    private Object getAnswer(String url, String method, String body, String authorization) throws Exception {
        String mContexLogger = "getAnswer";
        Logger.logInfo(" Start");
        JSONObject joAnswer = null;
        String sEndPointURL = url;
        Logger.logInfo(" sEndPointURL: " + url);
        Logger.logInfo(mContexLogger + " sRequestMethod: " + method);
        Logger.logInfo(mContexLogger + " body: " + body);
//        String encoding = Base64.getEncoder().encodeToString(authorization.getBytes());
        String aut = "Basic " + authorization;
        Logger.logInfo(" Authorization: " + aut);

        try {
            body = body == null ? "" : body;
            Logger.logInfo(" body: " + body);
            URL urlAnswer = new URL(sEndPointURL);
            Logger.logInfo(mContexLogger + " Pre Conection: " + urlAnswer);
            HttpURLConnection connAnswer = (HttpURLConnection) urlAnswer.openConnection();
            Logger.logInfo(mContexLogger + " Pos Conection: " + connAnswer);

            connAnswer.setConnectTimeout(5000);
            connAnswer.setReadTimeout(5000);
            connAnswer.setRequestProperty("Accept", "application/json");
            connAnswer.setRequestProperty("Content-Type", "application/json");
            connAnswer.setRequestProperty("Access-Control-Allow-Origin", "*");
            connAnswer.setRequestProperty("OData-Version", "4.0");
            connAnswer.setRequestProperty("Authorization", aut);
            connAnswer.setRequestProperty("Content-Length", String.valueOf(body.length()));
            connAnswer.setRequestMethod(method);
            connAnswer.setDoOutput(true);

            if (method.equalsIgnoreCase("GET")) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connAnswer.getInputStream()));
                String inputLine;
                StringBuffer content = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();
                meLogInfo("CONTENT: " + content);
                if (content.toString() != null) {
                    joAnswer = new JSONObject(content.toString());
                }

            } else if (method.equalsIgnoreCase("POST")) {

                Logger.logInfo(mContexLogger + " NEXT SETDoOUTPOU");
                Logger.logInfo(mContexLogger + " Body: " + body);

                byte[] postData = body.getBytes(StandardCharsets.UTF_8);
                OutputStream os = connAnswer.getOutputStream();
                os.write(postData);
                os.flush();
                os.close();
                InputStreamReader ins = new InputStreamReader(connAnswer.getInputStream());
                Logger.logInfo(mContexLogger + " ins: " + ins);
                StringBuilder sb = new StringBuilder();
                int ch;
                while ((ch = ins.read()) != -1) {
                    sb.append((char) ch);
                }

                Logger.logInfo(mContexLogger + " *******WS Answer: " + sb.toString());
                if (sb.toString() != null) {
                    joAnswer = new JSONObject(sb.toString());
                }

            }
            this.setStatus(connAnswer.getResponseCode());
            meLogInfo("STATUS: " + status);
            this.output = (this.error == true ? "ERROR! STATUS:" + status : "Message processed successfully.");

            return joAnswer;
        } catch (IOException | JSONException e) {
            Logger.logError(mContexLogger + " Exception getWSAnswer: " + e.getMessage());
            throw new Exception(e.getMessage());
        }
    }

    private void addMessageLog(String jsonObject, PropertyList properties) throws ActionException {
        String processedBy = this.connectionInfo.getSysuserId();
        PropertyList plAddMessageLog = new PropertyList();
        plAddMessageLog.setProperty(AddSDI.PROPERTY_SDCID, "LV_MessageLog");
        plAddMessageLog.setProperty("messagetypeid", this.CLASSNAME);
        plAddMessageLog.setProperty("messagetag", this.url);
        plAddMessageLog.setProperty("directionflag", "I");
        plAddMessageLog.setProperty("processedby", processedBy);
        plAddMessageLog.setProperty("processeddt", "N");
        plAddMessageLog.setProperty("messagebody", jsonObject);
        plAddMessageLog.setProperty("propertylist", properties.toXMLString());
        plAddMessageLog.setProperty("processstatus", (this.error == true ? "ERROR" : "COMPLETE"));
        plAddMessageLog.setProperty("processnotes", this.output);
        plAddMessageLog.setProperty("processlog", this.processlog);

        getActionProcessor().processAction(AddSDI.ID, AddSDI.VERSIONID, plAddMessageLog);
    }

    private String valueJsonString(JSONObject json, String key) {
        String value = "";
        try {
            if (json.has(key)) {
                value = json.getString(key);
                if ("null".equalsIgnoreCase(value)) {
                    value = "";
                }
                //logger.info(key + "  " + value);
            } else {
                value = "";
                //logger.info(key + " no xiste");
            }
        } catch (JSONException e) {
            //error += e;
        }
        return value;
    }

    private String valueJsonBoolean(JSONObject json, String key) {
        String value = valueJsonString(json, key);
        if (value.equalsIgnoreCase("true")) {
            value = "Y";
        } else if (value.equalsIgnoreCase("false")) {
            value = "N";
        }
        return value;
    }

    private void meLogInfo(String strInfo) {
        logger.info(strInfo);
        this.processlog += "\n" + strInfo;
    }

}
