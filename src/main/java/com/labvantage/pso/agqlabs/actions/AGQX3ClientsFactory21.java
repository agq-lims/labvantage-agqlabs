package com.labvantage.pso.agqlabs.actions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

import java.util.Map;

/**
 * Autor: Jhon Carlos Solís Ochoa
 * Empresa: AGQ Labs
 * Descripción: Clase principal AGQX3Clients que gestiona la integración de datos de clientes
 * en el sistema Labvantage. Permite agregar, editar o inactivar registros de clientes
 * y procesar entidades relacionadas como direcciones y contactos.
 */
public class AGQX3ClientsFactory21 extends BaseAction {

    // Constantes utilizadas en la clase
    public static final String FALSE = "false";
    public static final String KEYID_1 = "keyid1";
    public static final String KEYID_2 = "keyid2";
    public static final String CUSTOMERCONTACT = "Customercontact";
    public static final String ADDRESSTYPE = "addresstype";
    public static final String COUNTRY = "country";
    public static final String ADD_SDI = "AddSDI";
    public static final String SDCID = "sdcid";
    public static final String ADDRESS = "Address";
    public static final String CUSTOMER = "Customer";

    /**
     * Método principal para procesar la acción requerida en función de los datos proporcionados.
     * Determina si se debe agregar, editar o inactivar un cliente y ejecuta la acción correspondiente.
     *
     * @param pl Objeto PropertyList que contiene los datos del cliente.
     * @throws SapphireException Si ocurre un error durante el procesamiento.
     */
    @Override
    public void processAction(PropertyList pl) throws SapphireException {

        logger.info("Inicia el procesamiento de la clase: " + this.getClass().getCanonicalName());


        String message = pl.getProperty("message");
        this.logger.info("Message: " + message);

        try {
            JSONObject joRequest = new JSONObject(message);
            String codCliente = joRequest.optString("CodCliente", "");
            logger.info("Inicia el registro del cliente con CodCliente: " + codCliente);
            String addressId = fetchAddressId(codCliente);
            String actionType = determineAction(joRequest, addressId);
            logger.info("Ejecutara la acción: " + actionType);
            ActionStrategy actionStrategy = ActionStrategyFactory.getStrategy(actionType);
            actionStrategy.execute(this, joRequest, addressId);

        } catch (SapphireException | JSONException e) {
            handleException(e, "Error procesando JSON");
        } catch (Exception e) {
            handleException(e, "Error inesperado");
        }
    }

    /**
     * Busca el ID de dirección asociado a un cliente utilizando su código único.
     *
     * @param codCliente Código único del cliente.
     * @return ID de dirección si existe, de lo contrario null.
     */
    private String fetchAddressId(String codCliente) {
        logger.info("Inicio el procesamiento de la función: fetchAddressId(" + codCliente + ")");
        SafeSQL safeSQL = new SafeSQL();
        safeSQL.reset();

        String sqlValidateClientCode = "SELECT addressid, u_customerid FROM address ad WHERE ad.addressid =  '" + codCliente + "';";
        DataSet datasetCodCliente = super.getQueryProcessor().getPreparedSqlDataSet(sqlValidateClientCode, safeSQL.getValues());
        return datasetCodCliente.getRowCount() > 0 ? datasetCodCliente.getString(0, "addressid", "") : null;
    }

    /**
     * Determina la acción a realizar (Agregar, Editar o Inactivar) en función de los datos del cliente.
     *
     * @param joRequest Objeto JSON con los datos del cliente.
     * @param addressId ID de dirección asociado al cliente.
     * @return Tipo de acción como cadena de texto ("Add", "Edit", "Inactivate").
     */
    private String determineAction(JSONObject joRequest, String addressId) {
        logger.info("Ejecutando método determineAction: " + addressId + " - " + joRequest.optString("Activo", ""));
        if (FALSE.equals(joRequest.optString("Activo", ""))) return "Inactivate";
        if (addressId == null) return "Add";
        return "Edit";
    }

    /**
     * Maneja excepciones y registra los errores asociados.
     *
     * @param e       Excepción capturada.
     * @param message Mensaje de error a registrar.
     */
    protected void handleException(Exception e, String message) {
        this.logger.error("Error --> " + e.getMessage());
        this.setError(message + ": " + e.getMessage());
    }



    /**
     * Inactiva un cliente existente en el sistema.
     *
     * @param addressId ID de dirección del cliente a inactivar.
     * @param type Tipo de cliente a inactivar (por ejemplo, "Customer").
     * @throws SapphireException Si ocurre un error durante la inactivación del cliente.
     */
    protected void inactiveClient(String addressId, String type) throws SapphireException {
        this.logger.info("Ejecutando método inactiveClient");

        PropertyList parametersListAddress = new PropertyList();
        parametersListAddress.setProperty(SDCID, ADDRESS);
        parametersListAddress.setProperty(KEYID_1, addressId);
        parametersListAddress.setProperty(KEYID_2, type);
        parametersListAddress.setProperty("addressstatus", "Inactivo");

        this.getActionProcessor().processAction("EditSDI", "1", parametersListAddress);
    }


    /**
     * Agrega un nuevo cliente al sistema y procesa entidades relacionadas (direcciones y contactos).
     *
     * @param plClient Objeto PropertyList con los datos del cliente a agregar.
     * @param joRequest Objeto JSON que contiene los datos completos del cliente.
     * @throws SapphireException Si ocurre un error al agregar el cliente o al procesar las entidades relacionadas.
     */
    protected void addClient(PropertyList plClient, JSONObject joRequest) throws SapphireException {
        this.logger.info("Ejecutando método addClient");
        this.getActionProcessor().processAction(ADD_SDI, "1", plClient);
        processRelatedEntities(joRequest, plClient.getProperty("newkeyid1", ""));
    }

    /**
     * Edita los datos de un cliente existente en el sistema, actualiza entidades relacionadas
     * y elimina contactos y sitios antiguos antes de procesar nuevas entidades relacionadas.
     *
     * @param plClient Objeto PropertyList con los datos del cliente a editar.
     * @param addressId ID de dirección del cliente a editar.
     * @param joRequest Objeto JSON que contiene los datos completos del cliente.
     * @throws SapphireException Si ocurre un error al editar el cliente o al procesar las entidades relacionadas.
     */
    protected void editClient(PropertyList plClient, String addressId, JSONObject joRequest) throws SapphireException {
        this.logger.info("Ejecutando método editClient");

        plClient.setProperty(KEYID_1, addressId);
        plClient.setProperty(KEYID_2, CUSTOMER);
        this.getActionProcessor().processAction("EditSDI", "1", plClient);

        // Eliminar contactos y sitios asociados
        deleteAddressSdi(addressId, CUSTOMERCONTACT);
        deleteAddressSdi(addressId, "Site");

        // Procesar entidades relacionadas
        processRelatedEntities(joRequest, addressId);
    }

    /**
     * Procesa las entidades relacionadas con el cliente, como direcciones y contactos.
     *
     * @param client Objeto JSON que contiene los datos completos del cliente.
     * @param keyId ID clave del cliente para asociar las entidades relacionadas.
     * @throws JSONException Si ocurre un error al manipular el JSON.
     * @throws SapphireException Si ocurre un error al procesar las entidades relacionadas.
     */
    protected void processRelatedEntities(JSONObject client, String keyId) throws JSONException, SapphireException {
        // Procesar direcciones
        if (client.has("Direcciones")) {
            JSONArray jsDirections = client.getJSONArray("Direcciones");
            logger.info("Inicio del procesamiento de direcciones");
            addDirection(jsDirections, keyId);
        }

        // Procesar contactos
        if (client.has("Contactos")) {
            JSONArray jsContacts = client.getJSONArray("Contactos");
            logger.info("Inicio del procesamiento de contactos");
            addContact(jsContacts, keyId);
        }
    }


    /**
     * Construye los property list de referencia que evitaran la carga y validacion individual de las propiedades.
     *
     * @param client Objeto JSON que contiene los datos completos del cliente.
     * @throws JSONException Si ocurre un error durante la validacion de los datos.
     */
    protected PropertyList buildClientPropertyList(JSONObject client) throws JSONException {
        PropertyList plClient = new PropertyList();
        plClient.setProperty(SDCID, ADDRESS);
        plClient.setProperty(ADDRESSTYPE, CUSTOMER);

        // Campos comunes mapeados
        Map<String, String> fieldMappings = Map.ofEntries(
                Map.entry("CodCliente", "addressid"),
                Map.entry("RazonSocial", "addressdesc"),
                Map.entry("RazonSocialCorta", "u_razon_social_corta"),
                Map.entry("CodDivisa", "u_coddivisa"),
                Map.entry("CodPais", COUNTRY),
                Map.entry("NIF", "u_cif"),
                Map.entry("TipoCliente", "u_clasificacion"),
                Map.entry("CodClienteGrupo", "u_codclientgrup"),
                Map.entry("CodIdioma", "inv_languageid"),
                Map.entry("CategoriaCliente", "u_catclient"),
                Map.entry("CodClienteFactura", "u_codclientinv"),
                Map.entry("CategoriaABC", "u_catabc"),
                Map.entry("CodRepresentante", "u_codrepresent"),
                Map.entry("Representante", "u_represent"),
                Map.entry("CodUnidadNegocio", "u_codunidadneg"),
                Map.entry("CodSector", "u_codsector"),
                Map.entry("UnidadNegocio", "u_unidadneg"),
                Map.entry("Sector", "u_sector"),
                Map.entry("CodCondicionPago", "u_formadepago"),
                Map.entry("TipoNombre", "u_tiponombre"),
                Map.entry("ValorMinimo", "u_valormin"),
                Map.entry("IdZonaMensajeria", "u_idzonamensaj"),
                Map.entry("Mascara ID", "u_mascara")
        );

        // Agregar propiedades comunes
        for (Map.Entry<String, String> entry : fieldMappings.entrySet()) {
            addPropertyIfExists(client, plClient, entry.getKey(), entry.getValue());
        }

        // Campos booleanos mapeados
        Map<String, String> booleanMappings = Map.ofEntries(
                Map.entry("Activo", "addressstatus"),
                Map.entry("Pedido", "u_numpedido"),
                Map.entry("BeSafer", "u_besafer"),
                Map.entry("AcuseRecibo", "u_acuserecibo"),
                Map.entry("APEAM", "u_apeam"),
                Map.entry("CobrarMensajeria", "u_cobrarmensaj"),
                Map.entry("CobrarMensajeriaMinimo", "u_cobrarmensajmin"),
                Map.entry("AvisarSuperaLMR", "u_alertalmr"),
                Map.entry("AvisarSuperaCMA", "u_alertacma"),
                Map.entry("Impagado", "u_impagado"),
                Map.entry("Imprimible", "u_imprimible"),
                Map.entry("EsGrupo", "u_esgrupo"),
                Map.entry("Envio parcial si/no", "u_envioparcial"),
                Map.entry("CalculoRapido", "u_calculorapido"),
                Map.entry("RegularizarAlbaran", "u_regularizaralbaran"),
                Map.entry("EnviarMail", "u_enviomail"),
                Map.entry("EnviarFax", "u_enviofax"),
                Map.entry("EnviarFTP", "u_envioftp"),
                Map.entry("EnviarFTP", "u_envioftp")
        );

        // Agregar propiedades booleanas
        for (Map.Entry<String, String> entry : booleanMappings.entrySet()) {
            addBooleanPropertyIfExists(client, plClient, entry.getKey(), entry.getValue());
        }


        return plClient;
    }

    /**
     * Valida las propiedades de tipo diferente de booleanos.
     *
     * @param client Objeto JSON que contiene los datos completos del cliente.
     * @param plClient Objeto PropertyList con los datos del cliente a validar.
     * @param jsonKey ID clave del JSON a validar.
     * @param plKey ID clave del PropertyList a validar.
     * @throws JSONException Si ocurre un error durante la validacion de los datos.
     */
    private void addPropertyIfExists(JSONObject client, PropertyList plClient, String jsonKey, String plKey) throws JSONException {
        if (client.has(jsonKey)) {
            plClient.setProperty(plKey, client.optString(jsonKey, ""));
        }
    }

    /**
     * Valida las propiedades de tipo booleanos.
     *
     * @param client Objeto JSON que contiene los datos completos del cliente.
     * @param plClient Objeto PropertyList con los datos del cliente a validar.
     * @param jsonKey ID clave del JSON a validar.
     * @param plKey ID clave del PropertyList a validar.
     * @throws JSONException Si ocurre un error durante la validacion de los datos.
     */
    private void addBooleanPropertyIfExists(JSONObject client, PropertyList plClient, String jsonKey, String plKey) throws JSONException {
        if (client.has(jsonKey)) {
            String value = client.optString(jsonKey, FALSE);
            plClient.setProperty(plKey, "true".equalsIgnoreCase(value) ? "S" : "N");
        }
    }

    /**
     * Elimina entidades relacionadas con un cliente (como contactos o sitios) según el tipo especificado.
     *
     * @param addressId ID de dirección del cliente.
     * @param deleteType Tipo de entidad a eliminar ("Customercontact" o "Site").
     * @throws SapphireException Si ocurre un error durante la eliminación.
     */
    private void deleteAddressSdi(String addressId, String deleteType) throws SapphireException {
        this.logger.info("Ejecutando método deleteAddressSdi para el tipo: " + deleteType);

        PropertyList parametersListAddress = new PropertyList();
        parametersListAddress.setProperty(SDCID, ADDRESS);
        parametersListAddress.setProperty("columnid", "createdt");
        parametersListAddress.setProperty("processactionid", "DeleteSDI");
        parametersListAddress.setProperty("processactionversionid", "1");

        if (deleteType.equals(CUSTOMERCONTACT)) {
            parametersListAddress.setProperty("whereclause", "u_contactcustomerid = '" + addressId + "' and addresstype = 'Customercontact'");
        }
        if (deleteType.equals("Site")) {
            parametersListAddress.setProperty("whereclause", "u_customerid = '" + addressId + "' and addresstype = 'Site'");
        }

        parametersListAddress.setProperty("batch", "n");
        parametersListAddress.setProperty("asynchronous", "n");
        this.getActionProcessor().processAction("CheckDates", "1", parametersListAddress);
    }


    private void addDirection(JSONArray jsDireccion, String newKeyId1) throws JSONException, SapphireException {
        logger.info("Metthod: addDirection");

        Map<String, String> fieldMappings = Map.ofEntries(
                Map.entry("CodDireccion", "u_siteid"),
                Map.entry("Descripcion", "addressdesc"),
                Map.entry("Direccion", "address1"),
                Map.entry("CodPostal", "postalcode"),
                Map.entry("Ciudad", "city"),
                Map.entry("Provincia", "state"),
                Map.entry("CodPais", COUNTRY),
                Map.entry("Telefono", "phone"),
                Map.entry("Fax", "fax"),
                Map.entry("Email", "email"),
                Map.entry("PorDefecto", "u_pordefecto")
        );

        for (int i = 0; i < jsDireccion.length(); i++) {
            JSONObject direccion = jsDireccion.getJSONObject(i);



            PropertyList plDireccion = new PropertyList();
            plDireccion.setProperty(SDCID, ADDRESS);
            plDireccion.setProperty(ADDRESSTYPE, CUSTOMERCONTACT);
            plDireccion.setProperty("u_contactcustomerid", newKeyId1);

            addPropertiesToList(direccion, plDireccion, fieldMappings);

            this.getActionProcessor().processAction(ADD_SDI, "1", plDireccion);

            String newKeyIdUno = plDireccion.getProperty("newkeyid1", "");
            String newKeyIdDos = plDireccion.getProperty("newkeyid2", "");

            addAddressContacts(direccion, newKeyIdUno, newKeyIdDos);


        }
    }

    private void addContact(JSONArray jsContacto, String newKeyId1) throws JSONException, ActionException {
        logger.info("Executing method addContact");

        Map<String, String> fieldMappings = Map.ofEntries(
                Map.entry("CodContacto", "u_contactcustomerid"),
                Map.entry("Nombre", "firstname"),
                Map.entry("Apellidos", "lastname"),
                Map.entry("Funcion", "u_funcion"),
                Map.entry("Telefono", "phone"),
                Map.entry("Pais", COUNTRY),
                Map.entry("Email", "email"),
                Map.entry("EnviarMail", "u_enviomail"),
                Map.entry("EnviarFax", "u_enviofax"),
                Map.entry("EnviarFTP", "u_envioftp"),
                Map.entry("UsuarioFTP", "u_usuarioftp"),
                Map.entry("ClaveFTP", "u_claveftp"),
                Map.entry("Direccion", "u_contactaddress"),
                Map.entry("Observaciones", "u_contactobs"),
                Map.entry("AdministradorBesafer", "u_adminbesafer"),
                Map.entry("RutNif", "u_rutnif")
        );

        for (int i = 0; i < jsContacto.length(); i++) {
            JSONObject contacto = jsContacto.getJSONObject(i);
            PropertyList plContacto = new PropertyList();
            plContacto.setProperty(SDCID, ADDRESS);
            plContacto.setProperty(ADDRESSTYPE, "Site");
            plContacto.setProperty("u_customerid", newKeyId1);

            addPropertiesToList(contacto, plContacto, fieldMappings);
            this.getActionProcessor().processAction(ADD_SDI, "1", plContacto);

        }
    }

    private void addPropertiesToList(JSONObject jsonObject, PropertyList pl, Map<String, String> mappings) {
        for (Map.Entry<String, String> entry : mappings.entrySet()) {
            String jsonKey = entry.getKey();
            String plKey = entry.getValue();

            if (jsonObject.has(jsonKey)) {
                String value = jsonObject.optString(jsonKey);
                pl.setProperty(plKey, value.isEmpty() ? FALSE : value);
            }
        }
    }

    private void addAddressContacts(JSONObject direccion, String newKeyIdUno, String newKeyIdDos) throws SapphireException {
        logger.info("Metodo addAddressContacts");
        for (int j = 1; j <= 12; j++) {
            String contactKey = "CodContacto" + j;
            logger.info("contactKey: " + contactKey);
            if (direccion.has(contactKey) && !direccion.getString(contactKey).isEmpty()) {
                logger.info("Valida y creara la direccion del contacto");
                addAddressContact(newKeyIdUno, newKeyIdDos, direccion.getString(contactKey));
            }
        }
    }

    private void addAddressContact(String addressId, String addressTypeId, String name) throws SapphireException {
        this.logger.info("Method: AddAddressContact   " + addressId + "  " + name);

        PropertyList parametersListAddress = new PropertyList();

        parametersListAddress.setProperty(SDCID, ADDRESS);
        parametersListAddress.setProperty(KEYID_1, addressId);
        parametersListAddress.setProperty(KEYID_2, addressTypeId);
        parametersListAddress.setProperty("name", name);
        parametersListAddress.setProperty("linkid", "sdiaddress_link");

        this.getActionProcessor().processAction("AddDetailSDI", "1", parametersListAddress);

    }

}
