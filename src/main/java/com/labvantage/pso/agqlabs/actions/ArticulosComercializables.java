package com.labvantage.pso.agqlabs.actions;

import sapphire.accessor.ActionException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;
import java.util.HashMap;
import java.util.Map;

/**

 *
 * Autor: Jhon Carlos Solís Ochoa
 * Empresa: AGQ Labs
 * Descripción:  Esta clase es responsable de modificar el estado de los artículos en el sistema Labvantage.
 *  La clase valida las propiedades necesarias, ejecuta las consultas SQL y actualiza el estado
 *  de los artículos basándose en la lógica definida.
 */
public class ArticulosComercializables extends BaseAction {

    private static final String ERROR = "ERROR";
    private static final String COMPLETE = "COMPLETE";
    public static final String SDCID = "sdcid";
    private final String className;
    private boolean statusFlag;
    private String output;
    private String processLog;
    private final String url;
    private int status;

    /**
     * Constructor de la clase ArticulosComercializables.
     * Inicializa las propiedades predeterminadas.
     */
    public ArticulosComercializables()  {
        className = this.getClass().getName();
        statusFlag = false;
        output = "";
        processLog = "";
        url = "";
    }

    /**
     * Obtiene el estado de la clase.
     *
     * @return el estado actual
     */
    public int getStatus() {
        return status;
    }

    /**
     * Establece el estado de la clase.
     *
     * @param status el nuevo estado
     */
    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * Método principal que procesa la acción, recibe las propiedades y las valida.
     *
     * @param props las propiedades que contienen los datos a procesar
     * @throws ActionException si ocurre un error durante el procesamiento de la acción
     */
    @Override
    public void processAction(PropertyList props) throws ActionException {
        logInfo(className + "-BEGIN");
        logInfo(className + " props = " + props.toJSONString());

        // Obtener información desde el objeto PropertyList
        String codArticulo = props.getProperty("CodArticulo", "");
        String codCategoriaArticulo = props.getProperty("CodCategoriaArticulo", "");
        String estado =  props.getProperty("idEstado", "");
        props.setProperty("status", "OK");

        // Validar propiedades
        Map<String, String> propertiesToValidate = new HashMap<>();
        propertiesToValidate.put("CodArticulo", codArticulo);
        propertiesToValidate.put("CodCategoriaArticulo", codCategoriaArticulo);

        logInfo(String.format("%s CodArticulo= %s CodCategoriaArticulo= %s idEstado= %s",
                className, codArticulo, codCategoriaArticulo, estado));

        validateProperties(propertiesToValidate, props);

        String firstLetter = codArticulo.substring(0, 1);

        if (!"R".equalsIgnoreCase(firstLetter)) {
            String columnId = getColumnId(firstLetter);
            String sql = buildPrimaryQuery(codCategoriaArticulo, columnId);
            logInfo("Generated SQL: " + sql);

            DataSet dsTemp = this.getQueryProcessor().getSqlDataSet(sql);
            if (dsTemp.getRowCount() > 0) {
                processMatchingRows(dsTemp, codArticulo, estado);
            }
        }
    }

    /**
     * Procesa las filas que coinciden con los resultados obtenidos de la consulta SQL primaria.
     *
     * @param dsTemp el conjunto de datos con los resultados de la consulta primaria
     * @param codArticulo el código del artículo a procesar
     * @param estado el estado a aplicar al artículo
     * @throws ActionException si ocurre un error durante el procesamiento de las filas
     */
    private void processMatchingRows(DataSet dsTemp, String codArticulo, String estado) throws ActionException {
        logInfo("Procedimiento processMatchingRows");
        String asdcid = dsTemp.getString(0, SDCID);
        String sql = buildKeyQuery(dsTemp, codArticulo);
        logInfo("Generated Key Query: " + sql + " " + asdcid);

        DataSet keyResults = this.getQueryProcessor().getSqlDataSet(sql);

        if (keyResults.getRowCount() > 0) {
            editSDI(asdcid, keyResults, estado);
        }
    }

    /**
     * Construye la consulta SQL para obtener las claves primarias basadas en los resultados anteriores.
     *
     * @param dsTemp los datos que contienen los resultados de la consulta
     * @param codArticulo el código del artículo
     * @return la consulta SQL generada
     */
    private String buildKeyQuery(DataSet dsTemp, String codArticulo) {
        StringBuilder query = new StringBuilder("SELECT ");
        int rowCount = dsTemp.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            query.append(dsTemp.getString(i, "columnid")).append(" AS keyid").append(i + 1);
            if (i < rowCount - 1) {
                query.append(", ");
            }
        }
        query.append(" FROM ").append(dsTemp.getString(0, "tableid"))
                .append(" WHERE ").append(dsTemp.getString(0, "columnaid")).append("='").append(codArticulo).append("'");
        return query.toString();
    }

    /**
     * Edita el estado del artículo en la base de datos utilizando la acción "EditSDI".
     *
     * @param asdcid el id del artículo
     * @param keyResults los resultados obtenidos de la consulta SQL
     * @param estado el nuevo estado a aplicar
     * @throws ActionException si ocurre un error durante la edición
     */
    private void editSDI(String asdcid, DataSet keyResults, String estado) throws ActionException {
        PropertyList plEditSDI = new PropertyList();
        plEditSDI.setProperty(SDCID, asdcid);
        plEditSDI.setProperty("keyid1", keyResults.getString(0, "keyid1"));
        plEditSDI.setProperty("keyid2", keyResults.getString(0, "keyid2", ""));
        plEditSDI.setProperty("keyid3", keyResults.getString(0, "keyid3", ""));
        plEditSDI.setProperty("u_estado", "1".equalsIgnoreCase(estado) ? "Y" : "N");
        logInfo("EditSDI Payload: " + plEditSDI.toJSONString());
        this.getActionProcessor().processAction("EditSDI", "1", plEditSDI);
    }

    /**
     * Construye la consulta SQL primaria basada en el código de categoría del artículo.
     *
     * @param codCategoriaArticulo el código de categoría del artículo
     * @param columnId el identificador de la columna
     * @return la consulta SQL generada
     */
    private String buildPrimaryQuery(String codCategoriaArticulo, String columnId) {
        return String.format(
                "SELECT sc.columnid, sc.tableid, '%s' AS columnaid, " +
                        "(SELECT s2.sdcid FROM sdc s2 WHERE s2.tableid=sc.tableid) AS sdcid " +
                        "FROM syscolumn sc " +
                        "WHERE sc.tableid=(SELECT sd.tableid FROM sdc sd " +
                        "WHERE sd.sdcid=(SELECT rv.refdisplayvalue FROM refvalue rv " +
                        "WHERE rv.reftypeid='cod_categoria' AND rv.refvalueid='%s')) " +
                        "AND sc.pkflag='Y' ORDER BY sc.columnsequence",
                columnId, codCategoriaArticulo
        );
    }

    /**
     * Obtiene el nombre de la columna a utilizar basado en la primera letra del código de artículo.
     *
     * @param firstLetter la primera letra del código de artículo
     * @return el nombre de la columna correspondiente
     */
    private String getColumnId(String firstLetter) {
        switch (firstLetter) {
            case "T":
                return "paramlistid";
            case "P":
                return "workitemid";
            case "K":
                return "u_paramlistdetailagqid";
            case "A":
                return "s_productid";
            default:
                return "u_codarticulo"; // Valor por defecto para otros casos
        }
    }

    /**
     * Valida las propiedades de entrada y si alguna es inválida, registra el error y agrega el mensaje.
     *
     * @param properties las propiedades que deben ser validadas
     * @param props el objeto PropertyList que contiene los detalles de la acción
     * @throws ActionException si alguna propiedad es inválida
     */
    private void validateProperties(Map<String, String> properties, PropertyList props) throws ActionException {
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            String propertyName = entry.getKey();
            String propertyValue = entry.getValue();

            if (isInvalidProperty(propertyName, propertyValue)) {
                logAndAddError(propertyName, props);
            }
        }
    }

    /**
     * Verifica si una propiedad es inválida (vacía o nula).
     *
     * @param propertyName el nombre de la propiedad
     * @param value el valor de la propiedad
     * @return verdadero si la propiedad es inválida, falso en caso contrario
     */
    private boolean isInvalidProperty(String propertyName, String value) {
        if (value.isBlank()) {
            this.statusFlag = true;
            this.output = propertyName + " not found or empty";
            return true;
        }
        return false;
    }

    /**
     * Registra un error y agrega un mensaje al log.
     *
     * @param propertyName el nombre de la propiedad con error
     * @param props las propiedades a las que se debe agregar el error
     * @throws ActionException si ocurre un error durante el registro del mensaje
     */
    private void logAndAddError(String propertyName, PropertyList props) throws ActionException {
        logInfo("Error in property: " + propertyName);
        addMessageLog("", props);
        props.setProperty("status", ERROR);
        props.setProperty("outputmessage", this.output);

        throw new ActionException("Processing terminated due to error in property: " + propertyName);
    }

    /**
     * Agrega un mensaje al log del proceso.
     *
     * @param jsonObject el objeto JSON asociado al mensaje
     * @param properties las propiedades relacionadas al mensaje
     * @throws ActionException si ocurre un error durante el registro del mensaje
     */
    private void addMessageLog(String jsonObject, PropertyList properties) throws ActionException {
        String processedBy = this.connectionInfo.getSysuserId();
        PropertyList plAddMessageLog = new PropertyList();

        plAddMessageLog.setProperty(SDCID, "LV_MessageLog");
        plAddMessageLog.setProperty("messagetypeid", "articuloscomercializables");
        plAddMessageLog.setProperty("messagetag", this.url);
        plAddMessageLog.setProperty("directionflag", "I");
        plAddMessageLog.setProperty("processedby", processedBy);
        plAddMessageLog.setProperty("processeddt", "N");
        plAddMessageLog.setProperty("messagebody", jsonObject);
        plAddMessageLog.setProperty("propertylist", properties.toXMLString());
        plAddMessageLog.setProperty("processstatus", this.statusFlag ? ERROR : COMPLETE);
        plAddMessageLog.setProperty("processnotes", this.output);
        plAddMessageLog.setProperty("processlog", this.processLog);

        this.getActionProcessor().processAction("AddSDI", "1", plAddMessageLog);
    }

    /**
     * Registra un mensaje informativo en el log.
     *
     * @param message el mensaje a registrar
     */
    private void logInfo(String message) {
        logger.info(message);
        this.processLog += "\n" + message;
    }
}

