package com.labvantage.pso.agqlabs.actions;


import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Autor: Jhon Carlos Solís Ochoa
 * Empresa: AGQ Labs
 * Descripción: Servicio para compartir el reporte y este disponible en BeSafer.
 */

public class AddReporteBesafer extends BaseAction {

    private static final String POST = "POST";
    private boolean error = false;
    private String messageProcess = "";

    /**
     * Procesa una acción que realiza el envio del reporte.
     *
     * @param propertyList Objeto que contiene las propiedades del contexto.
     * @throws SapphireException Si ocurre un error durante el proceso.
     */

    public void processAction(PropertyList propertyList) throws SapphireException {

           logger.info("Inicia el proceso de construcción del reporte con destino a Besafer");
           String jsonFinal = "";

           try {


               logger.info("Leyendo mensaje de entrada...");

               String nombreDocumento = propertyList.getProperty("nombredocumento");
               String idTipoDocumento = propertyList.getProperty("idtipodocumento");
               String codSociedad = propertyList.getProperty("codsociedad");
               String codPlanta = propertyList.getProperty("codplanta");
               String codMuestra = propertyList.getProperty("codmuestra");
               String codCliente = propertyList.getProperty("codcliente");
               String codEstudio = propertyList.getProperty("idestudio");
               String codParcela = propertyList.getProperty("idparcela");
               String archivoReporte = propertyList.getProperty("reporte");

               logger.info("Documento: " + nombreDocumento);
               logger.info("codsociedad: " + codSociedad);
               logger.info("Ruta del PDF: " + archivoReporte);

               archivoReporte = archivoReporte.trim();

               // Leer archivo PDF
               File file = new File(archivoReporte);

               logger.info("Verificando archivo: " + file.getAbsolutePath());

               if (!file.exists()) {
                   throw new SapphireException("Archivo no encontrado: " + file.getAbsolutePath());
               }

               byte[] pdfBytes = Files.readAllBytes(file.toPath());

               // Convertir a Base64
               String documentoBase64 =  Base64.getEncoder()
                       .encodeToString(pdfBytes)
                       .replace("\r", "")
                       .replace("\n", "");;

               logger.info("PDF convertido a Base64 correctamente.");

               JSONObject metadataInterno = new JSONObject();
               metadataInterno.put("Seguimiento", "0");
               metadataInterno.put("CodSociedad", codSociedad);
               metadataInterno.put("CodPlanta", codPlanta);
               metadataInterno.put("Trazas", "0");
               metadataInterno.put("CodCliente", codCliente);
               metadataInterno.put("CodMuestra", codMuestra);
               metadataInterno.put("idEstudio", codEstudio);
               metadataInterno.put("idParcela", codParcela);

               JSONObject metadatos = new JSONObject();
               metadatos.put("Metadatos", metadataInterno);

               // JSON principal
               JSONObject jsonDocumento = new JSONObject();
               jsonDocumento.put("idDocumento", 0);
               jsonDocumento.put("nombreDocumento", nombreDocumento);
               jsonDocumento.put("idTipoDocumento", Integer.parseInt(idTipoDocumento));
               jsonDocumento.put("metadatos", metadatos);
               jsonDocumento.put("documento", documentoBase64);

               jsonFinal = jsonDocumento.toString();
               logger.info("Reporte con destino a Besafer: " + jsonFinal);

               subirReporteAzure(obtenerConfiguracionBase(), jsonFinal, null);

               setFinalResponse(propertyList);

           }catch (Exception e){

               logger.error("Error generando el JSON para Besafer: " + (jsonFinal.length() == 0 ? "JSON no generado" : jsonFinal) , e);
               throw new SapphireException("Error construyendo documento para Besafer: " + e.getMessage());
           }






    }


    public Map<String, String> obtenerConfiguracionBase() {
        logger.info("Ejecuta el método obtenerConfiguracionBase()");
        Map<String, String> configuracion = new HashMap<>();

        String sql =
                "select dp.value_string, dp.value_name " +
                        "from u_detparam_sys dp " +
                        "where dp.parameter_sysid = 'Sage X3' " +
                        "and dp.value_name in ('URL Base','authorization', 'URL Servicio Subida Documento')";

        DataSet resultQuery = this.getQueryProcessor().getSqlDataSet(sql);

        if (resultQuery != null && resultQuery.getRowCount() > 0) {

            for (int i = 0; i < resultQuery.getRowCount(); i++) {

                String key   = resultQuery.getString(i, "value_name");
                String value = resultQuery.getString(i, "value_string");

                if (key != null) {
                    configuracion.put(key, value != null ? value : "");
                }
            }
        }

        return configuracion;
    }

    public void subirReporteAzure(Map<String, String> config, String body, String cookie) throws Exception {
            logger.info("Ingresamos al metodo subirReporteAzure...");


        String urlService = config.get("URL Base") +  config.get("URL Servicio Subida Documento");
        String authorization = "Basic " + config.get("authorization");
        logger.info("Authorization: " + authorization);
        logger.info("urlService: " + urlService);

        JSONObject respuestaPost = new JSONObject();

        try {
            respuestaPost = (JSONObject) WSUtils.getNewAnswer(
                    urlService,
                    POST,
                    body,
                    authorization,
                    cookie
            );

            messageProcess = "Se procesa la subida del archivo adecuadamente..." + respuestaPost;
            logger.info(messageProcess);


        }catch (Exception e){
            messageProcess = "Error al tratar de subir el archivo al repositorio: " + respuestaPost.toString() + " " + e.getMessage();
            logger.error(messageProcess);
            error = true;
        }


    }

    /**
     * Establece la respuesta final basada en el estado de error.
     */
    private void setFinalResponse(PropertyList propertyList) {
        if (this.error) {
            setErrorResponse(propertyList, this.messageProcess.toString());
        } else {
            setSuccessResponse(propertyList);
        }
    }

    /**
     * Establece respuesta de error en el PropertyList.
     */
    private void setErrorResponse(PropertyList propertyList, String message) {
        propertyList.setProperty("status", "ERROR");
        propertyList.setProperty("code", "400");
        propertyList.setProperty("outputmessage", message);

    }

    /**
     * Establece respuesta exitosa en el PropertyList.
     */
    private void setSuccessResponse(PropertyList propertyList) {
        propertyList.setProperty("status", "OK");
        propertyList.setProperty("code", "200");
        propertyList.setProperty("outputmessage", "OK");

    }





}
