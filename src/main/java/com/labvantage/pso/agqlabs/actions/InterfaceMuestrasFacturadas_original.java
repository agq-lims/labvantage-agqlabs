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
import java.net.HttpURLConnection;
import java.net.URL;
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

/**
 *
 * @author gustavo.rojas
 */
public class InterfaceMuestrasFacturadas_original extends BaseAction {

    private final String CLASSNAME = "InterfaceMuestrasFacturadas";
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
        String CodMuestra = props.getProperty("CodMuestra", "");
        String message = "";
        props.setProperty("status", "OK");
        //
        if ("".equalsIgnoreCase(CodMuestra)) {
            message = props.getProperty("message", "");
            message = message.trim();
            if (!"".equalsIgnoreCase(message)) {
                if (message.charAt(0) == '[') {
                    //message = message.substring(1, message.length() - 1);
                    meLogInfo(this.CLASSNAME + " message=" + message);
                    try {
                        JSONArray ja = new JSONArray(message);
                        for (int jj = 0; jj < ja.length(); jj++) {
                            JSONObject jo = ja.getJSONObject(jj);
                            if (jo.has("CodMuestra")) {
                                String temp = jo.getString("CodMuestra");
                                if (!"".equalsIgnoreCase(temp)) {
                                    CodMuestra += ";" + temp;
                                }
                            }
                        }
                        meLogInfo(this.CLASSNAME + " CodMuestra=" + CodMuestra);
                        if (CodMuestra.charAt(0) == ';') {
                            CodMuestra = CodMuestra.substring(1);
                        }
                    } catch (JSONException ex) {
                        java.util.logging.Logger.getLogger(InterfaceMuestrasFacturadas_original.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    meLogInfo(this.CLASSNAME + " message=" + props.toJSONString());
                    try {
                        JSONObject jo = new JSONObject(message);
                        if (jo.has("CodMuestra")) {
                            CodMuestra = jo.getString("CodMuestra");
                        }
                    } catch (JSONException ex) {
                        java.util.logging.Logger.getLogger(InterfaceMuestrasFacturadas_original.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
        //
        this.meLogInfo(this.CLASSNAME + " message=" + message);
        this.meLogInfo(this.CLASSNAME + " CodMuestra=" + CodMuestra);
        //
        if ("".equalsIgnoreCase(CodMuestra)) {
            this.error = true;
            this.output = "CodMuestra not found";
            addMessageLog("", props);
            props.setProperty("status", "ERROR");
            props.setProperty("outputmessage", this.output);
            return;
        }
        //
        String strSQL;
        if (CodMuestra.contains("[")) {
            CodMuestra = CodMuestra.replace("[", "");
            CodMuestra = CodMuestra.replace("]", "");
            CodMuestra = CodMuestra.replace("\"", "'");
            strSQL = "select ss.requestitemid from s_sample ss where ss.u_samplelabel in (" + CodMuestra.replaceAll("[;]", "','") + ") and ss.u_mode is null";
        } else {
            strSQL = "select ss.requestitemid from s_sample ss where ss.u_samplelabel in ('" + CodMuestra.replaceAll("[;]", "','") + "') and ss.u_mode is null";
        }
        //
        this.meLogInfo(this.CLASSNAME + " strSQL=" + strSQL);
        DataSet dsSamples = this.getQueryProcessor().getSqlDataSet(strSQL);
        if (dsSamples.getRowCount() > 0) {
            PropertyList plEditSDI = new PropertyList();
            plEditSDI.setProperty("sdcid", "LV_RequestItem");
            plEditSDI.setProperty("keyid1", dsSamples.getColumnValues("requestitemid", ";"));
            plEditSDI.setProperty("u_facturada", "Y");
            this.meLogInfo(this.CLASSNAME + " plEditSDI=" + plEditSDI.toJSONString());
            this.getActionProcessor().processAction(EditSDI.ID, EditSDI.VERSIONID, plEditSDI);
        }
        //
        this.addMessageLog(CodMuestra, props);
        this.meLogInfo(this.CLASSNAME + " props=" + props.toJSONString());
        this.meLogInfo(this.CLASSNAME + " - END");
        //
        props.setProperty("outputmessage", "CodMuestra=" + CodMuestra);
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
            HttpURLConnection connAnswer = (HttpURLConnection)urlAnswer.openConnection();
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
                this.meLogInfo("CONTENT: " + content);
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

    private void meLogInfo(String strInfo) {
        logger.info(strInfo);
        this.processlog += "\n" + strInfo;
    }

}
