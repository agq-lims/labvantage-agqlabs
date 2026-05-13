package com.labvantage.pso.agqlabs.actions;


import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.xml.PropertyList;

public class validateServiceConnection extends BaseAction {


    public void processAction(PropertyList propertyList) throws SapphireException {

        PropertyList sm = new PropertyList();

        String url = sm.getProperty("url", "");
        String method = sm.getProperty("method", "GET");
        String body = sm.getProperty("body", "");
        String authorization = sm.getProperty("authorization", "");
        String cookie = sm.getProperty("cookie", "");


        executeService(url, method, body, authorization, cookie);

    }


    private void executeService(String urlService, String method, String body, String authorization, String cookie) throws SapphireException {
        JSONObject respuestaPost = new JSONObject();

        try {
            respuestaPost = (JSONObject) WSUtils.getAnswer(
                    urlService,
                    method,
                    body,
                    authorization,
                    cookie
            );

            String messageProcess = "Se procesa la subida del archivo adecuadamente..." + respuestaPost;
            logger.info(messageProcess);
            throw new SapphireException(messageProcess);


        }catch (Exception e){
            String messageProcess = "Error al tratar de subir el archivo al repositorio: " + respuestaPost.toString() + " " + e.getMessage();
            logger.error(messageProcess);
            throw new SapphireException(messageProcess);
        }
    }


}
