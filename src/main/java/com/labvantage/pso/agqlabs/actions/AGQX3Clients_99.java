package com.labvantage.pso.agqlabs.actions;

import org.json.JSONArray;
import sapphire.accessor.ActionException;
import sapphire.action.BaseAction;
import sapphire.xml.PropertyList;

import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.action.AddSDI;
import sapphire.action.EditSDIAttribute;
import sapphire.action.EditSDI;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

public class AGQX3Clients_99 extends BaseAction {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Override
    public void processAction(PropertyList pl) throws SapphireException {
   
        //logger.info("Start Process");
        String message = pl.getProperty("message");
        logger.info(message);
        logger.info("Iniciando integracion ProcessActionAGQX3Clients Version - 12052023");

        try {            
            JSONObject joRequest= new JSONObject(message);
            String codCliente = "";
            if(joRequest.has("CodCliente")){
                codCliente = joRequest.getString("CodCliente");
            }

            logger.info("Se registra el codigo del cliente: " + codCliente);
            String SQLValidateClientCode = "SELECT addressid, u_customerid FROM address ad WHERE ad.u_customerid = '" + codCliente + "';";
            
            SafeSQL safeSQL = new SafeSQL();
            safeSQL.reset();

            DataSet datasetCodCliente = super.getQueryProcessor().getPreparedSqlDataSet(SQLValidateClientCode, safeSQL.getValues());
            
            if (datasetCodCliente.getRowCount() <= 0) {
                logger.info("Case: AddClient");
                addEditClient(joRequest,"Add", codCliente);
            } else {
                if (pl.getProperty("Activo", "").equals("false")){
                    logger.info("Case: InactivacionCLiente");
                    this.inactiveClient(codCliente, "Customer");
                } else{
                    logger.info("Case: EdicionCliente, ingresa por aca");
                    addEditClient(joRequest,"Edit", codCliente);
                }
            }
        }catch (JSONException | SapphireException e) {
            this.logger.error(" Error   -->" + e.getMessage());
            setError("Error processing JSON" + e.getMessage()); 
           // pl.put("error", e);
        }catch (Exception  e) {
            this.logger.error(" Error   -->" + e.getMessage());
            setError("Error processing JSON" + e.getMessage());
            //pl.put("error", e);
        }
       
    }
    
    public void addEditClient(JSONObject client, String type, String addressid) throws JSONException, SapphireException{
        logger.info("Metthod: addEditClient");
        PropertyList plClient = new PropertyList();
        plClient.put(AddSDI.PROPERTY_SDCID, "Address");
        plClient.put("addresstype","Customer");

        if(type.equals("Add")){
            logger.info("Entra en el action para la creacion del cliente: " + addressid);
            plClient.put("addressid",addressid);
        }
        
        if(client.has("CodCliente")){
            plClient.put("u_customerid",client.getString("CodCliente")==null?"":client.getString("CodCliente"));
        }
        if(client.has("RazonSocial")){
            plClient.put("addressdesc",client.getString("RazonSocial")==null?"":client.getString("RazonSocial"));
        }
        if(client.has("RazonSocialCorta")){
            plClient.put("u_razon_social_corta",client.getString("RazonSocialCorta")==null?"":client.getString("RazonSocialCorta"));
        }
        if(client.has("CodDivisa")){
            plClient.put("u_coddivisa",client.getString("CodDivisa")==null?"":client.getString("CodDivisa"));
        }
        if(client.has("CodPais")){
            plClient.put("country",client.getString("CodPais")==null?"":client.getString("CodPais"));
        }
        if(client.has("NIF")){
            plClient.put("u_cif",client.getString("NIF")==null?"":client.getString("NIF"));
        }
        if(client.has("TipoCliente")){
            plClient.put("u_clasificacion",client.getString("TipoCliente")==null?"":client.getString("TipoCliente"));
        }
//        if(client.has("NIF")){
//            plClient.put("u_cif",client.getString("NIF")==null?"":client.getString("NIF"));
//        }
        if(client.has("Activo")){
            plClient.put("addressstatus",client.getString("Activo")==null?"":client.getString("Activo").equals("false")?"Inactivo":"Activo");
        }
        if(client.has("CodClienteGrupo")){
            plClient.put("u_codclientgrup",client.getString("CodClienteGrupo")==null?"":client.getString("CodClienteGrupo"));
        }
        if(client.has("CodIdioma")){
            plClient.put("inv_languageid",client.getString("CodIdioma")==null?"":client.getString("CodIdioma"));
        }
        if(client.has("CategoriaCliente")){
            plClient.put("u_catclient",client.getString("CategoriaCliente")==null?"":client.getString("CategoriaCliente"));
        }
        if(client.has("CodClienteFactura")){
            plClient.put("u_codclientinv",client.getString("CodClienteFactura")==null?"":client.getString("CodClienteFactura"));
        }
        if(client.has("CategoriaABC")){
            plClient.put("u_catabc",client.getString("CategoriaABC")==null?"":client.getString("CategoriaABC"));
        }
        if(client.has("CodRepresentante")){
            plClient.put("u_codrepresent",client.getString("CodRepresentante")==null?"":client.getString("CodRepresentante"));
        }
        if(client.has("Representante")){
            plClient.put("u_represent",client.getString("Representante")==null?"":client.getString("Representante"));
        }
        if(client.has("CodUnidadNegocio")){
            plClient.put("u_codunidadneg",client.getString("CodUnidadNegocio")==null?"":client.getString("CodUnidadNegocio"));
        }
        if(client.has("CodSector")){
            plClient.put("u_codsector",client.getString("CodSector")==null?"":client.getString("CodSector"));
        }
        if(client.has("UnidadNegocio")){
            plClient.put("u_unidadneg",client.getString("UnidadNegocio")==null?"":client.getString("UnidadNegocio"));
        }
        if(client.has("Sector")){
            plClient.put("u_sector",client.getString("Sector")==null?"":client.getString("Sector"));
        }
        
        if(client.has("CodCondicionPago")){
            plClient.put("u_formadepago",client.getString("CodCondicionPago")==null?"":client.getString("CodCondicionPago"));
        }
        if(client.has("Pedido")){
            plClient.put("u_numpedido",client.getString("Pedido")==null?"":CheckValue(client.getString("Pedido")));
        }
        if(client.has("BeSafer")){
            plClient.put("u_besafer",client.getString("BeSafer")==null?"":CheckValue(client.getString("BeSafer")));
        }
        if(client.has("AcuseRecibo")){
            plClient.put("u_acuserecibo",client.getString("AcuseRecibo")==null?"":CheckValue(client.getString("AcuseRecibo")));
        }
        //Atributo
        if(client.has("APEAM")){
            plClient.put("u_apeam",client.getString("APEAM")==null?"":CheckValue(client.getString("APEAM")));
        }
        if(client.has("IdZonaMensajeria")){
            plClient.put("u_idzonamensaj",client.getString("IdZonaMensajeria")==null?"":client.getString("IdZonaMensajeria"));
        }
        if(client.has("CobrarMensajeria")){
            plClient.put("u_cobrarmensaj",client.getString("CobrarMensajeria")==null?"":CheckValue(client.getString("CobrarMensajeria")));
        }
        if(client.has("CobrarMensajeriaMinimo")){
            plClient.put("u_cobrarmensajmin",client.getString("CobrarMensajeriaMinimo")==null?"":CheckValue(client.getString("CobrarMensajeriaMinimo")));
        }
        if(client.has("ValorMinimo")){
            plClient.put("u_valormin",client.getString("ValorMinimo")==null?"":client.getString("ValorMinimo"));
        }
        if(client.has("AvisarSuperaLMR")){
            plClient.put("u_alertalmr",client.getString("AvisarSuperaLMR")==null?"":CheckValue(client.getString("AvisarSuperaLMR")));
        }
        if(client.has("AvisarSuperaCMA")){
            plClient.put("u_alertacma",client.getString("AvisarSuperaCMA")==null?"":CheckValue(client.getString("AvisarSuperaCMA")));
        }
        if(client.has("TipoNombre")){
            plClient.put("u_tiponombre",client.getString("TipoNombre")==null?"":client.getString("TipoNombre"));
        }
        if(client.has("Impagado")){
            plClient.put("u_impagado",client.getString("Impagado")==null?"":CheckValue(client.getString("Impagado")));
        }
        if(client.has("Imprimible")){
            plClient.put("u_imprimible",client.getString("Imprimible")==null?"":CheckValue(client.getString("Imprimible")));
        }
        if(client.has("EsGrupo")){
            plClient.put("u_esgrupo",client.getString("EsGrupo")==null?"":CheckValue(client.getString("EsGrupo")));
        }
        if(client.has("ARFD")){
            plClient.put("u_arfd",client.getString("ARFD")==null?"":CheckValue(client.getString("ARFD")));
        }
        if(client.has("FormasEnvio")){
            plClient.put("u_tipoenvio",client.getString("FormasEnvio")==null?"":client.getString("FormasEnvio"));
        }
        if(client.has("Entidad de inspeccion si/no")){
            plClient.put("u_entidadinspeccion",client.getString("Entidad de inspeccion si/no")==null?"":CheckValue(client.getString("Entidad de inspeccion si/no")));
        }
        if(client.has("Direccion oferta si/no")){
            plClient.put("u_direccoferta",client.getString("Direccion oferta si/no")==null?"":CheckValue(client.getString("Direccion oferta si/no")));
        }
        if(client.has("Mascara ID")){
            plClient.put("u_mascara",client.getString("Mascara ID")==null?"":client.getString("Mascara ID"));
        }
        if(client.has("Envio parcial si/no")){
            plClient.put("u_envioparcial",client.getString("Envio parcial si/no")==null?"":CheckValue(client.getString("Envio parcial si/no")));
        }
        /*adiciondos------NO FRS>*/
        if(client.has("CalculoRapido")){
            plClient.put("u_calculorapido",client.getString("CalculoRapido")==null?"":CheckValue(client.getString("CalculoRapido")));
        }
        if(client.has("RegularizarAlbaran")){
            plClient.put("u_regularizaralbaran",client.getString("RegularizarAlbaran")==null?"":CheckValue(client.getString("RegularizarAlbaran")));
        }
        if(client.has("NumMuestrasHist")){
            plClient.put("u_nummuestrashist",client.getString("NumMuestrasHist")==null?"":client.get("NumMuestrasHist"));
        }
        ////atributos a columnas
        if(client.has("InformeLMRCorto")){
            plClient.put("u_lmrcorto",client.getString("InformeLMRCorto")==null?"":CheckValue(client.getString("InformeLMRCorto")));
        }
        if(client.has("InformeCMACorto")){
            plClient.put("u_cmacorto",client.getString("InformeCMACorto")==null?"":CheckValue(client.getString("InformeCMACorto")));
        }     
        if(client.has("NombreXML")){
            plClient.put("u_nombrexml",client.getString("NombreXML")==null?"":client.get("NombreXML"));
        }
        if(client.has("IdUnidadMedida")){
            plClient.put("u_idunidadmedida",client.getString("IdUnidadMedida")==null?"":client.get("IdUnidadMedida"));
        }
        if(client.has("FraseAviso")){
            plClient.put("u_fraseaviso",client.getString("FraseAviso")==null?"":client.get("FraseAviso"));
        }
        if(client.has("InformeLMRColLMR")){
            plClient.put("u_informelmrcollmr",client.getString("InformeLMRColLMR")==null?"":CheckValue(client.getString("InformeLMRColLMR")));
        }
        if(client.has("InformeCMAColFOT")){
            plClient.put("u_informecmacolfot",client.getString("InformeCMAColFOT")==null?"":CheckValue(client.getString("InformeCMAColFOT")));
        }
        if(client.has("InformeCMAColLD")){
            plClient.put("u_informecmacolld",client.getString("InformeCMAColLD")==null?"":CheckValue(client.getString("InformeCMAColLD")));
        }
        if(client.has("InformeCMAColCMA")){
            plClient.put("u_informecmacolcma",client.getString("InformeCMAColCMA")==null?"":CheckValue(client.getString("InformeCMAColCMA")));
        }
        if(client.has("InformeCMA1Pag")){
            plClient.put("u_informecma1pag",client.getString("InformeCMA1Pag")==null?"":CheckValue(client.getString("InformeCMA1Pag")));
        }
        if(client.has("CamposAPEAM")){
            plClient.put("u_camposapeam",client.getString("CamposAPEAM")==null?"":client.get("CamposAPEAM"));
        }
        if(client.has("Comparar todos los LMR si/no")){
            plClient.put("u_comparartodoslmr",client.getString("Comparar todos los LMR si/no")==null?"":CheckValue(client.getString("Comparar todos los LMR si/no")));
        }
        if(client.has("RUT")){
            plClient.put("u_rut",client.getString("RUT")==null?"":client.get("RUT"));
        }
/*------NO FRS>*/

        try {
            if(type.equals("Add")){
                logger.info("Ejecutara el processAction de nuevo registro AddSDI");
                this.getActionProcessor().processAction(AddSDI.ID, AddSDI.VERSIONID, plClient);

            } else {
                logger.info("Ejecutara el processAction de editare registro AddSDI");
                plClient.put("keyid1", addressid);
                plClient.put("keyid2","Customer");
                this.getActionProcessor().processAction(EditSDI.ID, EditSDI.VERSIONID, plClient);
                // Eliminar Direcciones
                this.deleteAddressSdi(addressid, "Customercontact");
                // Eliminar Contactos
                this.deleteAddressSdi(addressid, "Site");
            }

//            if(client.has("InformeLMRCorto")){
//                this.EditAttributeSdi(newKeyId1, "Customer", "LMRCorto", client.getString("InformeLMRCorto")==null?"":CheckValue(client.getString("InformeLMRCorto")));
//            }
//            if(client.has("InformeCMACorto")){
//                this.EditAttributeSdi(newKeyId1, "Customer", "CMACorto", client.getString("InformeCMACorto")==null?"":CheckValue(client.getString("InformeCMACorto")));
//            }
//            if(client.has("NombreXML")){
//                this.EditAttributeSdi(newKeyId1, "Customer", "NombreXML", client.getString("NombreXML")==null?"":client.getString("NombreXML"));
//            }
//            if(client.has("IdUnidadMedida")){
//                this.EditAttributeSdi(newKeyId1, "Customer", "IDUnidadMedida", client.getString("IdUnidadMedida")==null?"":client.getString("IdUnidadMedida"));
//            }
//            if(client.has("FraseAviso")){
//                this.EditAttributeSdi(newKeyId1, "Customer", "Frasedeaviso", client.getString("FraseAviso")==null?"":client.getString("FraseAviso"));
//            }
//            if(client.has("InformeLMRColLMR")){
//                this.EditAttributeSdi(newKeyId1, "Customer", "InformeLMRColLMR", client.getString("InformeLMRColLMR")==null?"":CheckValue(client.getString("InformeLMRColLMR")));
//            }
//            if(client.has("InformeCMAColFOT")){
//                this.EditAttributeSdi(newKeyId1, "Customer", "InformeCMAColFOT", client.getString("InformeCMAColFOT")==null?"":CheckValue(client.getString("InformeCMAColFOT")));
//            }
//            if(client.has("InformeCMAColLD")){
//                this.EditAttributeSdi(newKeyId1, "Customer", "InformeCMAColLD", client.getString("InformeCMAColLD")==null?"":CheckValue(client.getString("InformeCMAColLD")));
//            }
//            if(client.has("InformeCMAColCMA")){
//                this.EditAttributeSdi(newKeyId1, "Customer", "InformeCMAColCMA", client.getString("InformeCMAColCMA")==null?"":CheckValue(client.getString("InformeCMAColCMA")));
//            }
//            if(client.has("InformeCMA1Pag")){                                      
//               this.EditAttributeSdi(newKeyId1, "Customer", "InformeCMA1pag", client.getString("InformeCMA1Pag")==null?"":CheckValue(client.getString("InformeCMA1Pag")));
//            }
//            if(client.has("APEAM")){
//              this.EditAttributeSdi(newKeyId1, "Customer", "APEAMReport",client.getString("APEAM")==null?"":CheckValue(client.getString("APEAM")));
//            }
//            if(client.has("CamposAPEAM")){                                             
//               this.EditAttributeSdi(newKeyId1, "Customer", "InformeAPEAM", client.getString("CamposAPEAM")==null?"":CheckValue(client.getString("CamposAPEAM")));
//            }
//            if(client.has("Comparar todos los LMR si/no")){
//                this.EditAttributeSdi(newKeyId1, "Customer", "CompararTodosLMR", client.getString("Comparar todos los LMR si/no")==null?"":client.getString("Comparar todos los LMR si/no"));
//            }
//            if(client.has("RUT")){
//                this.EditAttributeSdi(newKeyId1, "Customer", "RUT", client.getString("RUT")==null?"":client.getString("RUT"));
//            }
            
            if(client.has("Direcciones")){
                logger.info("Start Add Direcciones");
                //logger.info(client.getString("Direcciones"));
                JSONArray jsDireccion = new JSONArray(client.getString("Direcciones"));
                logger.info("::newKeyId1 "+addressid);
                addDirection(jsDireccion,addressid);
                
            }
            if(client.has("Contactos")){
                logger.info("Start Add Contactos");
                JSONArray jsContacto = new JSONArray(client.getString("Contactos"));
                addContact(jsContacto,addressid);
            }
        } catch (ActionException | JSONException e) {
            this.logger.error(" Error inserting Client  -->" + e.getMessage());
            e.getMessage();
        }
    }  
    
    public void EditAttributeSdi(String addressid, String addrestype, String attribute, String value) throws ActionException, SapphireException{
        
        value = "N".equals(value)?"No":"S".equals(value)?"Si":value;
        
        logger.info("Metthod: EditAttributeSdi >>>>" + attribute);
        PropertyList parametersListAddress = new PropertyList();
        parametersListAddress.setProperty("sdcid", "Address");        //sdcid=Address
        parametersListAddress.setProperty("keyid1", addressid);       //keyidl=Customer_00081
        parametersListAddress.setProperty("keyid2", addrestype);      //keyid2=Customer 
        parametersListAddress.setProperty("keyid3", "(null)");        //keyid3=(null)
        parametersListAddress.setProperty("attributeid", attribute);  //attributeid=LMRCorto
        parametersListAddress.setProperty("attributesdcid", "Address");//attributesdcid=Address
        parametersListAddress.setProperty("attributeinstance", "1");  // attributeinstance=l         
        parametersListAddress.setProperty("value", value);                  //value=No 
        try{
            // this.getActionProcessor().processAction(EditSDIAttribute.ID, EditSDIAttribute.VERSIONID, parametersListAddress);
            this.getActionProcessor().processAction(EditSDIAttribute.ID, EditSDIAttribute.VERSIONID, parametersListAddress);
         } catch (SapphireException e) {
            this.logger.error(" Error  Edit Attribute -->" + attribute +", "+ e.getMessage());
        }
       
    }

    private String generateCode(String codigo){
        String datePart = LocalDateTime.now().format(DATE_FMT);
        AtomicInteger SEQ = new AtomicInteger(0);
        int seqPart = SEQ.updateAndGet(v -> (v >= 9999 ? 0 : v +1));
        //int randomPart = ThreadLocalRandom.current().nextInt(100, 999);

        return String.format(
                "%s_%04d%03d",
                codigo,
                datePart,
                seqPart
        );
    }
    
    public void addDirection(JSONArray jsDireccion, String newKeyId1) throws JSONException, ActionException, SapphireException{
            logger.info("Metthod: addDirection");
        PropertyList plDireccion = new PropertyList();
        plDireccion.put(AddSDI.PROPERTY_SDCID, "Address");
        plDireccion.put("addresstype","Customercontact");
        
        for (int i=0; i<jsDireccion.length();i++){
            plDireccion.put("u_contactcustomerid", newKeyId1);
            if(jsDireccion.getJSONObject(i).has("CodDireccion")){
                String codDireccion = jsDireccion.getJSONObject(i).getString("CodDireccion");
                plDireccion.put("u_siteid", codDireccion);
                plDireccion.put("addressid", codDireccion);
            }
            if(jsDireccion.getJSONObject(i).has("Descripcion")){
                plDireccion.put("addressdesc", jsDireccion.getJSONObject(i).getString("Descripcion"));
            }
            if(jsDireccion.getJSONObject(i).has("Direccion")){
                plDireccion.put("address1", jsDireccion.getJSONObject(i).getString("Direccion"));
            }
            if(jsDireccion.getJSONObject(i).has("CodPostal")){
                plDireccion.put("postalcode", jsDireccion.getJSONObject(i).getString("CodPostal"));
            }
            if(jsDireccion.getJSONObject(i).has("Ciudad")){
                plDireccion.put("city", jsDireccion.getJSONObject(i).getString("Ciudad"));
            }
            if(jsDireccion.getJSONObject(i).has("Provincia")){
                plDireccion.put("state", jsDireccion.getJSONObject(i).getString("Provincia"));
            }
            if(jsDireccion.getJSONObject(i).has("CodPais")){
                plDireccion.put("country", jsDireccion.getJSONObject(i).getString("CodPais"));
            }
            if(jsDireccion.getJSONObject(i).has("Telefono")){
                plDireccion.put("phone", jsDireccion.getJSONObject(i).getString("Telefono"));
            }
            if(jsDireccion.getJSONObject(i).has("Fax")){
                plDireccion.put("fax", jsDireccion.getJSONObject(i).getString("Fax"));
            }
            if(jsDireccion.getJSONObject(i).has("Email")){
                plDireccion.put("email", jsDireccion.getJSONObject(i).getString("Email"));
            }
            if(jsDireccion.getJSONObject(i).has("PorDefecto")){
                plDireccion.put("u_pordefecto", CheckValue(jsDireccion.getJSONObject(i).getString("PorDefecto")));
            }
            this.getActionProcessor().processAction(AddSDI.ID, AddSDI.VERSIONID, plDireccion);
            
            String newKeyIdUno = plDireccion.getProperty("newkeyid1", "");
            String newKeyIdDos = plDireccion.getProperty("newkeyid2", "");
            
            if(jsDireccion.getJSONObject(i).has("CodContacto1")){
                if(jsDireccion.getJSONObject(i).getString("CodContacto1").length()>0)
                    this.AddAddressContact(newKeyIdUno, newKeyIdDos, jsDireccion.getJSONObject(i).getString("CodContacto1"));
            }
            if(jsDireccion.getJSONObject(i).has("CodContacto2")){
                if(jsDireccion.getJSONObject(i).getString("CodContacto2").length()>0)
                    this.AddAddressContact(newKeyIdUno, newKeyIdDos, jsDireccion.getJSONObject(i).getString("CodContacto2"));
            }
            if(jsDireccion.getJSONObject(i).has("CodContacto3")){
                if(jsDireccion.getJSONObject(i).getString("CodContacto3").length()>0)
                    this.AddAddressContact(newKeyIdUno, newKeyIdDos, jsDireccion.getJSONObject(i).getString("CodContacto3"));
            }
            if(jsDireccion.getJSONObject(i).has("CodContacto4")){
                if(jsDireccion.getJSONObject(i).getString("CodContacto4").length()>0)
                    this.AddAddressContact(newKeyIdUno, newKeyIdDos, jsDireccion.getJSONObject(i).getString("CodContacto4"));
            }
            if(jsDireccion.getJSONObject(i).has("CodContacto5")){
                if(jsDireccion.getJSONObject(i).getString("CodContacto5").length()>0)
                    this.AddAddressContact(newKeyIdUno, newKeyIdDos, jsDireccion.getJSONObject(i).getString("CodContacto5"));
            }
            if(jsDireccion.getJSONObject(i).has("CodContacto6")){
                if(jsDireccion.getJSONObject(i).getString("CodContacto6").length()>0)
                    this.AddAddressContact(newKeyIdUno, newKeyIdDos, jsDireccion.getJSONObject(i).getString("CodContacto6"));
            }
            if(jsDireccion.getJSONObject(i).has("CodContacto7")){
                if(jsDireccion.getJSONObject(i).getString("CodContacto7").length()>0)
                    this.AddAddressContact(newKeyIdUno, newKeyIdDos, jsDireccion.getJSONObject(i).getString("CodContacto7"));
            }
            if(jsDireccion.getJSONObject(i).has("CodContacto8")){
                if(jsDireccion.getJSONObject(i).getString("CodContacto8").length()>0)
                    this.AddAddressContact(newKeyIdUno, newKeyIdDos, jsDireccion.getJSONObject(i).getString("CodContacto8"));
            }
            if(jsDireccion.getJSONObject(i).has("CodContacto9")){
                if(jsDireccion.getJSONObject(i).getString("CodContacto9").length()>0)
                    this.AddAddressContact(newKeyIdUno, newKeyIdDos, jsDireccion.getJSONObject(i).getString("CodContacto9"));
            }
            if(jsDireccion.getJSONObject(i).has("CodContacto10")){
                if(jsDireccion.getJSONObject(i).getString("CodContacto10").length()>0)
                    this.AddAddressContact(newKeyIdUno, newKeyIdDos, jsDireccion.getJSONObject(i).getString("CodContacto10"));
            }
            if(jsDireccion.getJSONObject(i).has("CodContacto11")){
                if(jsDireccion.getJSONObject(i).getString("CodContacto11").length()>0)
                    this.AddAddressContact(newKeyIdUno, newKeyIdDos, jsDireccion.getJSONObject(i).getString("CodContacto11"));
            }
            if(jsDireccion.getJSONObject(i).has("CodContacto12")){
                if(jsDireccion.getJSONObject(i).getString("CodContacto12").length()>0)
                    this.AddAddressContact(newKeyIdUno, newKeyIdDos, jsDireccion.getJSONObject(i).getString("CodContacto12"));
            }
            
            logger.info("Se creó una dirección");
        }
    }
    
    public void addContact(JSONArray jsContacto, String newKeyId1) throws JSONException, ActionException{
        logger.info("Metthod: addContact");
        PropertyList plContacto = new PropertyList();
        plContacto.put(AddSDI.PROPERTY_SDCID, "Address");
        plContacto.put("addresstype","Site");
        
        for (int i=0; i<jsContacto.length();i++){
            plContacto.put("u_customerid", newKeyId1);
            if(jsContacto.getJSONObject(i).has("CodContacto")){
                String codContacto = jsContacto.getJSONObject(i).getString("CodContacto");
                plContacto.put("u_contactcustomerid", codContacto);
                plContacto.put("addressid", codContacto);
            }
            if(jsContacto.getJSONObject(i).has("Nombre")){
                plContacto.put("firstname", jsContacto.getJSONObject(i).getString("Nombre"));
            }
            if(jsContacto.getJSONObject(i).has("Apellidos")){
                plContacto.put("lastname", jsContacto.getJSONObject(i).getString("Apellidos"));
            }
            if(jsContacto.getJSONObject(i).has("Funcion")){
                plContacto.put("u_funcion", jsContacto.getJSONObject(i).getString("Funcion"));
            }
            if(jsContacto.getJSONObject(i).has("Telefono")){
                plContacto.put("phone", jsContacto.getJSONObject(i).getString("Telefono"));
            }
            if(jsContacto.getJSONObject(i).has("Pais")){
                plContacto.put("country", jsContacto.getJSONObject(i).getString("Pais"));
            }
            if(jsContacto.getJSONObject(i).has("Email")){
                plContacto.put("email", jsContacto.getJSONObject(i).getString("Email"));
            }
            if(jsContacto.getJSONObject(i).has("EnviarMail")){
                plContacto.put("u_enviomail", CheckValue(jsContacto.getJSONObject(i).getString("EnviarMail")));
            }
            if(jsContacto.getJSONObject(i).has("EnviarFax")){
                plContacto.put("u_enviofax", CheckValue(jsContacto.getJSONObject(i).getString("EnviarFax")));
            }
            if(jsContacto.getJSONObject(i).has("EnviarFTP")){
                plContacto.put("u_envioftp", CheckValue(jsContacto.getJSONObject(i).getString("EnviarFTP")));
            }
            if(jsContacto.getJSONObject(i).has("UsuarioFTP")){
                plContacto.put("u_usuarioftp", jsContacto.getJSONObject(i).getString("UsuarioFTP"));
            }
            if(jsContacto.getJSONObject(i).has("ClaveFTP")){
                plContacto.put("u_claveftp", jsContacto.getJSONObject(i).getString("ClaveFTP"));
            }
            if(jsContacto.getJSONObject(i).has("Direccion")){
                plContacto.put("u_contactaddress", jsContacto.getJSONObject(i).getString("Direccion"));
            }
            if(jsContacto.getJSONObject(i).has("Observaciones")){
                plContacto.put("u_contactobs", jsContacto.getJSONObject(i).getString("Observaciones"));
            }
            if(jsContacto.getJSONObject(i).has("AdministradorBesafer")){
                plContacto.put("u_adminbesafer", CheckValue(jsContacto.getJSONObject(i).getString("AdministradorBesafer")));
            }
            if(jsContacto.getJSONObject(i).has("RutNif")){
                plContacto.put("u_rutnif", jsContacto.getJSONObject(i).getString("RutNif"));
            }
            this.getActionProcessor().processAction(AddSDI.ID, AddSDI.VERSIONID, plContacto);
            
            logger.info("Se creó un contacto");
        }
    }
    
    public void AddAddressContact(String addressid , String addresstypeid, String name) throws ActionException, SapphireException{
        logger.info("Metthod: AddAddressContact   "+addressid+"  "+name);
        PropertyList parametersListAddress = new PropertyList();
        parametersListAddress.setProperty("sdcid", "Address");
        parametersListAddress.setProperty("keyid1", addressid);
        parametersListAddress.setProperty("keyid2", addresstypeid);        
        parametersListAddress.setProperty("name", name);  
        parametersListAddress.setProperty("linkid", "sdiaddress_link"); 
        
        this.getActionProcessor().processAction("AddDetailSDI", "1", parametersListAddress);
    }
    
    public void inactiveClient(String addressid, String addresstype) throws ActionException, SapphireException{
        logger.info("Metthod: inactiveClient");
        PropertyList parametersListAddress = new PropertyList();
        parametersListAddress.setProperty("sdcid", "Address");
        parametersListAddress.setProperty("keyid1", addressid);
        parametersListAddress.setProperty("keyid2", addresstype);        
        parametersListAddress.setProperty("addressstatus", "Inactivo");
        
        this.getActionProcessor().processAction(EditSDI.ID, EditSDI.VERSIONID, parametersListAddress);                
    }
    
    public void deleteAddressSdi(String addressid, String deletetype) throws ActionException, SapphireException{
        logger.info("Metthod: deleteAddressSdi, " + deletetype);
        PropertyList parametersListAddress = new PropertyList();
        parametersListAddress.setProperty("sdcid", "Address");
        parametersListAddress.setProperty("columnid", "createdt");
        parametersListAddress.setProperty("processactionid", "DeleteSDI");        
        parametersListAddress.setProperty("processactionversionid", "1");
        
        if(deletetype.equals("Customercontact")){
            parametersListAddress.setProperty("whereclause", "u_contactcustomerid = '"+addressid+"' and addresstype = 'Customercontact'");  
        } else if(deletetype.equals("Site")) {
            parametersListAddress.setProperty("whereclause", "u_customerid = '"+addressid+"' and addresstype = 'Site'"); 
        }  
                
        parametersListAddress.setProperty("batch", "n");        
        parametersListAddress.setProperty("asynchronous", "n");
        
        this.getActionProcessor().processAction("CheckDates", "1", parametersListAddress);
    }
    public String CheckValue(String valor){
        return "false".equals(valor)?"N":"S";
    }
}
