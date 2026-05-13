package com.labvantage.pso.agqlabs.actions;

import org.json.JSONArray;
import sapphire.accessor.ActionException;
import sapphire.action.BaseAction;
import sapphire.xml.PropertyList;

import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.action.AddSDI;
import sapphire.action.EditSDI;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;

import java.util.HashMap;
import java.util.Map;

public class AGQX3Clients extends BaseAction {

    // Mapa para almacenar los contactos indexados por CodContacto
    private Map<String, JSONObject> contactosMap;

    @Override
    public void processAction(PropertyList pl) throws SapphireException {

        // Validar que el message no sea null
        String message = pl.getProperty("message");

        // Log para depuración
        logger.info("Message recibido: " + (message != null ? message.substring(0, Math.min(message.length(), 100)) + "..." : "NULL"));

        if (message == null || message.trim().isEmpty()) {
            logger.error("Error: El mensaje es null o vacío");
            setError("Error: No se recibió el mensaje JSON");
            return;
        }

        logger.info("Iniciando integracion ProcessActionAGQX3Clients Version - 12052023");

        try {
            JSONObject joRequest = new JSONObject(message);

            // Construir el mapa de contactos ANTES de procesar direcciones
            this.contactosMap = buildContactosMap(joRequest);

            String codCliente = "";
            if(joRequest.has("CodCliente")){
                codCliente = joRequest.getString("CodCliente");
            }

            // Validar que CodCliente no esté vacío
            if (codCliente.trim().isEmpty()) {
                logger.error("Error: CodCliente está vacío");
                setError("Error: CodCliente es requerido");
                return;
            }

            String SQLValidateClientCode = "SELECT addressid, u_customerid FROM address ad WHERE ad.u_customerid = '" + codCliente + "';";

            SafeSQL safeSQL = new SafeSQL();
            safeSQL.reset();

            DataSet datasetCodCliente = super.getQueryProcessor().getPreparedSqlDataSet(SQLValidateClientCode, safeSQL.getValues());

            if (datasetCodCliente.getRowCount() <= 0) {
                logger.info("Case: AddClient");
                addEditClient(joRequest, "Add", "");
            } else {
                // Corregir: validar el campo Activo del JSON, no del PropertyList
                String activo = joRequest.optString("Activo", "true");
                if ("false".equalsIgnoreCase(activo)) {
                    logger.info("Case: InactivacionCLiente");
                    this.inactiveClient(datasetCodCliente.getString(0, "addressid", ""), "Customer");
                } else {
                    logger.info("Case: EdicionCliente");
                    addEditClient(joRequest, "Edit", datasetCodCliente.getString(0, "addressid", ""));
                }
            }
        } catch (JSONException e) {
            this.logger.error("Error JSON -->" + e.getMessage());
            setError("Error procesando JSON: " + e.getMessage());
        } catch (SapphireException e) {
            this.logger.error("Error Sapphire -->" + e.getMessage());
            setError("Error en procesamiento: " + e.getMessage());
        } catch (Exception e) {
            this.logger.error("Error inesperado -->" + e.getMessage());
            e.printStackTrace();
            setError("Error inesperado: " + e.getMessage());
        }
    }

    public void addEditClient(JSONObject client, String type, String addressid) throws JSONException, SapphireException {
        logger.info("Metthod: addEditClient - Tipo: " + type);
        PropertyList plClient = new PropertyList();
        plClient.put(AddSDI.PROPERTY_SDCID, "Address");
        plClient.put("addresstype", "Customer");

        // ... (resto del código de mapeo de campos permanece igual) ...
        // [Mantener todo el código de mapeo desde CodCliente hasta RUT]

        if(client.has("CodCliente")){
            plClient.put("u_customerid", client.optString("CodCliente", ""));
        }
        if(client.has("RazonSocial")){
            plClient.put("addressdesc", client.optString("RazonSocial", ""));
        }
        if(client.has("RazonSocialCorta")){
            plClient.put("u_razon_social_corta", client.optString("RazonSocialCorta", ""));
        }
        if(client.has("CodDivisa")){
            plClient.put("u_coddivisa", client.optString("CodDivisa", ""));
        }
        if(client.has("CodPais")){
            plClient.put("country", client.optString("CodPais", ""));
        }
        if(client.has("NIF")){
            plClient.put("u_cif", client.optString("NIF", ""));
        }
        if(client.has("TipoCliente")){
            plClient.put("u_clasificacion", client.optString("TipoCliente", ""));
        }
        if(client.has("Activo")){
            String activo = client.optString("Activo", "true");
            plClient.put("addressstatus", "false".equalsIgnoreCase(activo) ? "Inactivo" : "Activo");
        }
        if(client.has("CodClienteGrupo")){
            plClient.put("u_codclientgrup", client.optString("CodClienteGrupo", ""));
        }
        if(client.has("CodIdioma")){
            plClient.put("inv_languageid", obtenerLenguaje(client.optString("CodIdioma", "")));
        }
        if(client.has("CategoriaCliente")){
            plClient.put("u_catclient", client.optString("CategoriaCliente", ""));
        }
        if(client.has("CodClienteFactura")){
            plClient.put("u_codclientinv", client.optString("CodClienteFactura", ""));
        }
        if(client.has("CategoriaABC")){
            plClient.put("u_catabc", client.optString("CategoriaABC", ""));
        }
        if(client.has("CodRepresentante")){
            plClient.put("u_codrepresent", client.optString("CodRepresentante", ""));
        }
        if(client.has("Representante")){
            plClient.put("u_represent", client.optString("Representante", ""));
        }
        if(client.has("CodUnidadNegocio")){
            plClient.put("u_codunidadneg", client.optString("CodUnidadNegocio", ""));
        }
        if(client.has("CodSector")){
            plClient.put("u_codsector", client.optString("CodSector", ""));
        }
        if(client.has("UnidadNegocio")){
            plClient.put("u_unidadneg", client.optString("UnidadNegocio", ""));
        }
        if(client.has("Sector")){
            plClient.put("u_sector", client.optString("Sector", ""));
        }
        if(client.has("CodCondicionPago")){
            plClient.put("u_formadepago", client.optString("CodCondicionPago", ""));
        }
        if(client.has("Pedido")){
            plClient.put("u_numpedido", CheckValue(client.optString("Pedido", "false")));
        }
        if(client.has("BeSafer")){
            plClient.put("u_besafer", CheckValue(client.optString("BeSafer", "false")));
        }
        if(client.has("AcuseRecibo")){
            plClient.put("u_acuserecibo", CheckValue(client.optString("AcuseRecibo", "false")));
        }
        if(client.has("APEAM")){
            plClient.put("u_apeam", CheckValue(client.optString("APEAM", "false")));
        }
        if(client.has("IdZonaMensajeria")){
            plClient.put("u_idzonamensaj", client.optString("IdZonaMensajeria", ""));
        }
        if(client.has("CobrarMensajeria")){
            plClient.put("u_cobrarmensaj", CheckValue(client.optString("CobrarMensajeria", "false")));
        }
        if(client.has("CobrarMensajeriaMinimo")){
            plClient.put("u_cobrarmensajmin", CheckValue(client.optString("CobrarMensajeriaMinimo", "false")));
        }
        if(client.has("ValorMinimo")){
            plClient.put("u_valormin", client.optString("ValorMinimo", ""));
        }
        if(client.has("AvisarSuperaLMR")){
            plClient.put("u_alertalmr", CheckValue(client.optString("AvisarSuperaLMR", "false")));
        }
        if(client.has("AvisarSuperaCMA")){
            plClient.put("u_alertacma", CheckValue(client.optString("AvisarSuperaCMA", "false")));
        }
        if(client.has("TipoNombre")){
            plClient.put("u_tiponombre", client.optString("TipoNombre", ""));
        }
        if(client.has("Impagado")){
            plClient.put("u_impagado", CheckValue(client.optString("Impagado", "false")));
        }
        if(client.has("Imprimible")){
            plClient.put("u_imprimible", CheckValue(client.optString("Imprimible", "false")));
        }
        if(client.has("EsGrupo")){
            plClient.put("u_esgrupo", CheckValue(client.optString("EsGrupo", "false")));
        }
        if(client.has("ARFD")){
            plClient.put("u_arfd", CheckValue(client.optString("ARFD", "false")));
        }
        if(client.has("FormasEnvio")){
            plClient.put("u_tipoenvio", client.optString("FormasEnvio", ""));
        }
        if(client.has("Entidad de inspeccion si/no")){
            plClient.put("u_entidadinspeccion", CheckValue(client.optString("Entidad de inspeccion si/no", "false")));
        }
        if(client.has("Direccion oferta si/no")){
            plClient.put("u_direccoferta", CheckValue(client.optString("Direccion oferta si/no", "false")));
        }
        if(client.has("Mascara ID")){
            plClient.put("u_mascara", client.optString("Mascara ID", ""));
        }
        if(client.has("Envio parcial si/no")){
            plClient.put("u_envioparcial", CheckValue(client.optString("Envio parcial si/no", "false")));
        }
        if(client.has("CalculoRapido")){
            plClient.put("u_calculorapido", CheckValue(client.optString("CalculoRapido", "false")));
        }
        if(client.has("RegularizarAlbaran")){
            plClient.put("u_regularizaralbaran", CheckValue(client.optString("RegularizarAlbaran", "false")));
        }
        if(client.has("NumMuestrasHist")){
            plClient.put("u_nummuestrashist", client.optString("NumMuestrasHist", "0"));
        }
        if(client.has("InformeLMRCorto")){
            plClient.put("u_lmrcorto", CheckValue(client.optString("InformeLMRCorto", "false")));
        }
        if(client.has("InformeCMACorto")){
            plClient.put("u_cmacorto", CheckValue(client.optString("InformeCMACorto", "false")));
        }
        if(client.has("NombreXML")){
            plClient.put("u_nombrexml", client.optString("NombreXML", ""));
        }
        if(client.has("IdUnidadMedida")){
            plClient.put("u_idunidadmedida", client.optString("IdUnidadMedida", ""));
        }
        if(client.has("FraseAviso")){
            plClient.put("u_fraseaviso", client.optString("FraseAviso", ""));
        }
        if(client.has("InformeLMRColLMR")){
            plClient.put("u_informelmrcollmr", CheckValue(client.optString("InformeLMRColLMR", "false")));
        }
        if(client.has("InformeCMAColFOT")){
            plClient.put("u_informecmacolfot", CheckValue(client.optString("InformeCMAColFOT", "false")));
        }
        if(client.has("InformeCMAColLD")){
            plClient.put("u_informecmacolld", CheckValue(client.optString("InformeCMAColLD", "false")));
        }
        if(client.has("InformeCMAColCMA")){
            plClient.put("u_informecmacolcma", CheckValue(client.optString("InformeCMAColCMA", "false")));
        }
        if(client.has("InformeCMA1Pag")){
            plClient.put("u_informecma1pag", CheckValue(client.optString("InformeCMA1Pag", "false")));
        }
        if(client.has("CamposAPEAM")){
            plClient.put("u_camposapeam", client.optString("CamposAPEAM", ""));
        }
        if(client.has("Comparar todos los LMR si/no")){
            plClient.put("u_comparartodoslmr", CheckValue(client.optString("Comparar todos los LMR si/no", "false")));
        }
        if(client.has("RUT")){
            plClient.put("u_rut", client.optString("RUT", ""));
        }

        try {
            if ("Add".equals(type)) {
                this.getActionProcessor().processAction(AddSDI.ID, AddSDI.VERSIONID, plClient);
            } else {
                plClient.put("keyid1", addressid);
                plClient.put("keyid2", "Customer");
                this.getActionProcessor().processAction(EditSDI.ID, EditSDI.VERSIONID, plClient);

                // Eliminar Direcciones y Contactos solo en edición
                this.deleteAddressSdi(addressid, "Customercontact");
                this.deleteAddressSdi(addressid, "Site");
            }

            String newKeyId1 = "Add".equals(type) ? plClient.getProperty("newkeyid1", "") : addressid;

            logger.info("newKeyId1 obtenido: " + newKeyId1);

            if (client.has("Direcciones")) {
                logger.info("Start Add Direcciones");
                JSONArray jsDireccion = client.getJSONArray("Direcciones");
                logger.info("::newKeyId1 " + newKeyId1);
                addDirection(jsDireccion, newKeyId1);
            }

            if (client.has("Contactos")) {
                logger.info("Start Add Contactos");
                JSONArray jsContacto = client.getJSONArray("Contactos");
                addContact(jsContacto, newKeyId1);
            }

        } catch (ActionException | JSONException e) {
            this.logger.error("Error inserting Client -->" + e.getMessage());
            throw new SapphireException("Error al procesar cliente: " + e.getMessage());
        }
    }

    /**
     * CORREGIDO: Método addDirection con bucle correctamente anidado
     */
    public void addDirection(JSONArray jsDireccion, String newKeyId1) throws JSONException, ActionException, SapphireException {
        logger.info("Metthod: addDirection - Total direcciones: " + jsDireccion.length());

        for (int i = 0; i < jsDireccion.length(); i++) {
            // Crear NUEVO PropertyList para cada dirección
            PropertyList plDireccion = new PropertyList();
            plDireccion.put(AddSDI.PROPERTY_SDCID, "Address");
            plDireccion.put("addresstype", "Customercontact");

            JSONObject direccion = jsDireccion.getJSONObject(i);

            plDireccion.put("u_contactcustomerid", newKeyId1);

            if (direccion.has("CodDireccion")) {
                plDireccion.put("u_siteid", direccion.optString("CodDireccion", ""));
            }
            if (direccion.has("Descripcion")) {
                plDireccion.put("addressdesc", direccion.optString("Descripcion", ""));
            }
            if (direccion.has("Direccion")) {
                plDireccion.put("address1", direccion.optString("Direccion", ""));
            }
            if (direccion.has("CodPostal")) {
                plDireccion.put("postalcode", direccion.optString("CodPostal", ""));
            }
            if (direccion.has("Ciudad")) {
                plDireccion.put("city", direccion.optString("Ciudad", ""));
            }
            if (direccion.has("Provincia")) {
                plDireccion.put("state", direccion.optString("Provincia", ""));
            }
            if (direccion.has("CodPais")) {
                plDireccion.put("country", direccion.optString("CodPais", ""));
            }
            if (direccion.has("Telefono")) {
                plDireccion.put("phone", direccion.optString("Telefono", ""));
            }
            if (direccion.has("Fax")) {
                plDireccion.put("fax", direccion.optString("Fax", ""));
            }
            if (direccion.has("Email")) {
                plDireccion.put("email", direccion.optString("Email", ""));
            }
            if (direccion.has("PorDefecto")) {
                plDireccion.put("u_pordefecto", CheckValue(direccion.optString("PorDefecto", "false")));
            }

            // Ejecutar AddSDI para crear la dirección
            this.getActionProcessor().processAction(AddSDI.ID, AddSDI.VERSIONID, plDireccion);

            String newKeyIdUno = plDireccion.getProperty("newkeyid1", "");
            String newKeyIdDos = plDireccion.getProperty("newkeyid2", "");

            logger.info("Dirección creada - newKeyId1: " + newKeyIdUno + ", newKeyId2: " + newKeyIdDos);

            // CORREGIDO: Bucle para procesar CodContacto1 a CodContacto12
            for (int j = 1; j <= 12; j++) {
                String codContactoKey = "CodContacto" + j;

                if (direccion.has(codContactoKey)) {
                    String codContacto = direccion.optString(codContactoKey, "");
                    JSONObject contacto = this.contactosMap.get(codContacto);

                    if (!codContacto.isEmpty()) {
                        // Obtener el nombre completo en lugar del código
                        String nombreCompleto = getNombreCompletoContacto(codContacto);
                        String nombre = contacto.optString("Nombre", "");
                        String apellidos = contacto.optString("Apellidos", "");
                        String email = contacto.optString("Email", "");
                        String envioEmail = CheckValueYN(contacto.optString("EnviarMail", ""));
                        logger.info("Asociando contacto " + codContactoKey + ": " + codContacto + " -> " + nombreCompleto);
                        this.AddAddressContact(newKeyIdUno, newKeyIdDos, nombre, apellidos, email, envioEmail);
                    }
                }
            }

            logger.info("Se creó una dirección #" + (i + 1));
        }
    }

    /**
     * Obtiene el nombre completo (Nombre + Apellidos) de un contacto dado su CodContacto
     */
    private String getNombreCompletoContacto(String codContacto) {
        if (codContacto == null || codContacto.isEmpty()) {
            return "";
        }

        // Verificar que el mapa esté inicializado
        if (this.contactosMap == null) {
            logger.error("Error: contactosMap no está inicializado");
            return codContacto;
        }

        JSONObject contacto = this.contactosMap.get(codContacto);

        if (contacto == null) {
            logger.warn("No se encontró contacto con CodContacto: " + codContacto);
            return codContacto; // Fallback: devolver el código si no se encuentra
        }

        String nombre = contacto.optString("Nombre", "");
        String apellidos = contacto.optString("Apellidos", "");

        // Concatenar Nombre + " " + Apellidos y limpiar espacios extra
        String nombreCompleto = (nombre + " " + apellidos).trim();

        logger.info("Contacto " + codContacto + " resuelto a: '" + nombreCompleto + "'");

        return nombreCompleto.isEmpty() ? codContacto : nombreCompleto;
    }

    /**
     * Construye un mapa de contactos indexados por CodContacto para búsqueda rápida
     */
    private Map<String, JSONObject> buildContactosMap(JSONObject joRequest) {
        Map<String, JSONObject> map = new HashMap<>();

        if (joRequest.has("Contactos")) {
            try {
                JSONArray jsContactos = joRequest.getJSONArray("Contactos");
                for (int i = 0; i < jsContactos.length(); i++) {
                    JSONObject contacto = jsContactos.getJSONObject(i);
                    String codContacto = contacto.optString("CodContacto", "");
                    if (!codContacto.isEmpty()) {
                        map.put(codContacto, contacto);
                        logger.info("Contacto indexado: " + codContacto);
                    }
                }
            } catch (JSONException e) {
                logger.error("Error construyendo mapa de contactos: " + e.getMessage());
            }
        }

        logger.info("Total contactos indexados: " + map.size());
        return map;
    }

    public void addContact(JSONArray jsContacto, String newKeyId1) throws JSONException, ActionException {
        logger.info("Metthod: addContact - Total contactos: " + jsContacto.length());

        for (int i = 0; i < jsContacto.length(); i++) {
            PropertyList plContacto = new PropertyList();
            plContacto.put(AddSDI.PROPERTY_SDCID, "Address");
            plContacto.put("addresstype", "Site");

            JSONObject contacto = jsContacto.getJSONObject(i);

            plContacto.put("u_customerid", newKeyId1);

            if (contacto.has("CodContacto")) {
                plContacto.put("u_contactcustomerid", contacto.optString("CodContacto", ""));
            }
            if (contacto.has("Nombre")) {
                plContacto.put("firstname", contacto.optString("Nombre", ""));
            }
            if (contacto.has("Apellidos")) {
                plContacto.put("lastname", contacto.optString("Apellidos", ""));
            }
            if (contacto.has("Funcion")) {
                plContacto.put("u_funcion", contacto.optString("Funcion", ""));
            }
            if (contacto.has("Telefono")) {
                plContacto.put("phone", contacto.optString("Telefono", ""));
            }
            if (contacto.has("Pais")) {
                plContacto.put("country", contacto.optString("Pais", ""));
            }
            if (contacto.has("Email")) {
                plContacto.put("email", contacto.optString("Email", ""));
            }
            if (contacto.has("EnviarMail")) {
                plContacto.put("u_enviomail", CheckValue(contacto.optString("EnviarMail", "false")));
            }
            if (contacto.has("EnviarFax")) {
                plContacto.put("u_enviofax", CheckValue(contacto.optString("EnviarFax", "false")));
            }
            if (contacto.has("EnviarFTP")) {
                plContacto.put("u_envioftp", CheckValue(contacto.optString("EnviarFTP", "false")));
            }
            if (contacto.has("UsuarioFTP")) {
                plContacto.put("u_usuarioftp", contacto.optString("UsuarioFTP", ""));
            }
            if (contacto.has("ClaveFTP")) {
                plContacto.put("u_claveftp", contacto.optString("ClaveFTP", ""));
            }
            if (contacto.has("Direccion")) {
                plContacto.put("u_contactaddress", contacto.optString("Direccion", ""));
            }
            if (contacto.has("Observaciones")) {
                plContacto.put("u_contactobs", contacto.optString("Observaciones", ""));
            }
            if (contacto.has("AdministradorBesafer")) {
                plContacto.put("u_adminbesafer", CheckValue(contacto.optString("AdministradorBesafer", "false")));
            }
            if (contacto.has("RutNif")) {
                plContacto.put("u_rutnif", contacto.optString("RutNif", ""));
            }

            this.getActionProcessor().processAction(AddSDI.ID, AddSDI.VERSIONID, plContacto);

            logger.info("Se creó un contacto #" + (i + 1));
        }
    }

    public void AddAddressContact(String addressid, String addresstypeid, String name, String lastname, String email, String envio) throws ActionException, SapphireException {
        logger.info("Metthod: AddAddressContact - addressid: " + addressid + ", name: " + name);
        PropertyList parametersListAddress = new PropertyList();
        parametersListAddress.setProperty("sdcid", "Address");
        parametersListAddress.setProperty("keyid1", addressid);
        parametersListAddress.setProperty("keyid2", addresstypeid);
        parametersListAddress.setProperty("name", name);
        parametersListAddress.setProperty("lastname", lastname);
        parametersListAddress.setProperty("enviomail", envio);
        parametersListAddress.setProperty("email", email);
        parametersListAddress.setProperty("linkid", "sdiaddress_link");

        this.getActionProcessor().processAction("AddDetailSDI", "1", parametersListAddress);
    }

    public void inactiveClient(String addressid, String addresstype) throws ActionException, SapphireException {
        logger.info("Metthod: inactiveClient");
        PropertyList parametersListAddress = new PropertyList();
        parametersListAddress.setProperty("sdcid", "Address");
        parametersListAddress.setProperty("keyid1", addressid);
        parametersListAddress.setProperty("keyid2", addresstype);
        parametersListAddress.setProperty("addressstatus", "Inactivo");

        this.getActionProcessor().processAction(EditSDI.ID, EditSDI.VERSIONID, parametersListAddress);
    }

    public void deleteAddressSdi(String addressid, String deletetype) throws ActionException, SapphireException {
        logger.info("Metthod: deleteAddressSdi, " + deletetype);
        PropertyList parametersListAddress = new PropertyList();
        parametersListAddress.setProperty("sdcid", "Address");
        parametersListAddress.setProperty("columnid", "createdt");
        parametersListAddress.setProperty("processactionid", "DeleteSDI");
        parametersListAddress.setProperty("processactionversionid", "1");

        if ("Customercontact".equals(deletetype)) {
            parametersListAddress.setProperty("whereclause", "u_contactcustomerid = '" + addressid + "' and addresstype = 'Customercontact'");
        } else if ("Site".equals(deletetype)) {
            parametersListAddress.setProperty("whereclause", "u_customerid = '" + addressid + "' and addresstype = 'Site'");
        }

        parametersListAddress.setProperty("batch", "n");
        parametersListAddress.setProperty("asynchronous", "n");

        this.getActionProcessor().processAction("CheckDates", "1", parametersListAddress);
    }

    public String CheckValue(String valor) {
        return "false".equalsIgnoreCase(valor) ? "N" : "S";
    }

    public String CheckValueYN(String valor) {
        return "false".equalsIgnoreCase(valor) ? "N" : "Y";
    }

    private String obtenerLenguaje(String lenguaje) {
        logger.info("Entro a procesar el metodo obtenerLenguaje: " + lenguaje);

        if (lenguaje == null || lenguaje.trim().isEmpty()) {
            logger.warn("Lenguaje vacío, retornando default");
            return "Spanish"; // ID default para español u otro idioma base
        }

        String sql = "select TOP 1 L.languageid from language l where l.u_codlanguage = '" + lenguaje + "'";

        try {
            DataSet ds = this.getQueryProcessor().getSqlDataSet(sql);

            if (ds != null && ds.getRowCount() > 0) {
                String languageId = ds.getValue(0, "languageid");
                logger.info("Lenguaje encontrado: " + languageId);
                return languageId;
            }
        } catch (Exception e) {
            logger.error("Error consultando lenguaje: " + e.getMessage());
        }

        String errorMsg = "No se encontraron datos para el lenguaje: " + lenguaje;
        logger.error(errorMsg);
        return lenguaje;
    }
}