package com.labvantage.pso.agqlabs;

import com.labvantage.pso.agqlabs.actions.AddReporteBesafer;
import com.labvantage.pso.agqlabs.actions.ArticulosComercializables;
import com.labvantage.pso.agqlabs.actions.validateServiceConnection;
import org.apache.log4j.BasicConfigurator;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public class Main {
    public static void main(String[] args) throws SapphireException {

        BasicConfigurator.configure();

        /*StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"acreditaciondesc\": \"Esta es una nueva opcion\",\n");
        json.append("  \"empresa\": \"ES20\",\n");
        json.append("  \"estado\": \"Activo\",\n");
        json.append("  \"marca\": \"15\",\n");
        json.append("  \"no_marca\": \"*\",\n");
        json.append("  \"porcen_acreditacion\": \"60\",\n");
        json.append("  \"leyendas\": [\n");
        json.append("    {\n");
        json.append("      \"tacreditacion\": \"Una Acreditación\",\n");
        json.append("      \"idioma\": \"Spanish\",\n");
        json.append("      \"lnoacreditada\": \"Los parámetros marcados con asterisco (*) no están incluidos en el Alcance de Acreditación\",\n");
        json.append("      \"lacreditada\": \"(1) Ensayos cubiertos por la Acreditación ENAC Nº 305 LE1322, LE1323\"\n");
        json.append("    },\n");
        json.append("    {\n");
        json.append("      \"tacreditacion\": \"Una Acreditación\",\n");
        json.append("      \"idioma\": \"English\",\n");
        json.append("      \"lnoacreditada\": \"(*) Parameter Not accredited by ENAC\",\n");
        json.append("      \"lacreditada\": \"(1) Parameter accredited by ENAC Nº 305 LE1322, LE1323\"\n");
        json.append("    }\n");
        json.append("  ]\n");
        json.append("}");

        AcreditacionAgqLabs labs = new AcreditacionAgqLabs();

        PropertyList pl = new PropertyList();
        String strJson = json.toString();

        pl.put("json", strJson);

        labs.processAction(pl);*/


        ArticulosComercializables ar = new ArticulosComercializables();


        PropertyList pl = new PropertyList();
        //pl.setProperty("CodArticulo","T00052");
        //pl.setProperty("CodCategoriaArticulo","PTEC");
        //pl.setProperty("idEstado","1");

//        pl.setProperty("CodArticulo","P000056");
//        pl.setProperty("CodCategoriaArticulo","PNT");
//        pl.setProperty("idEstado","1");
//
//        ar.processAction(pl);


        /*String jsonString = "{"
                + "\"message\": {"
                + "\"CodCliente\": \"ME05-999514810\","
                + "\"RazonSocial\": \"9999_INSPIRATE S.A \","
                + "\"RazonSocialCorta\": \"GLO\","
                + "\"CodDivisa\": \"MXN\","
                + "\"CodPais\": \"MX\","
                + "\"NIF\": \"UNI\","
                + "\"TipoCliente\": \"1\","
                + "\"Activo\": true,"
                + "\"CodClienteGrupo\": \"ME01-00051162\","
                + "\"CodIdioma\": \"SPA\","
                + "\"CategoriaCliente\": \"ME01\","
                + "\"CodClienteFactura\": \"00051162\","
                + "\"CategoriaABC\": \"A\","
                + "\"CodRepresentante\": \"3203\","
                + "\"Representante\": \"Fennifer Guadalupe Verga Lopez\","
                + "\"CodUnidadNegocio\": \"04\","
                + "\"CodSector\": \"0401\","
                + "\"UnidadNegocio\": \"RECURSOS E INDUSTRIA\","
                + "\"Sector\": \"INDUSTRIA Y SERVICIOS\","
                + "\"CodCondicionPago\": \"CON\","
                + "\"Pedido\": false,"
                + "\"BeSafer\": false,"
                + "\"CalculoRapido\": false,"
                + "\"InformeLMRCorto\": true,"
                + "\"InformeCMACorto\": false,"
                + "\"NombreXML\": \"\","
                + "\"IdUnidadMedida\": \"\","
                + "\"FraseAviso\": \"loading\","
                + "\"RegularizarAlbaran\": false,"
                + "\"InformeLMRColLMR\": false,"
                + "\"InformeCMAColFOT\": false,"
                + "\"InformeCMAColLD\": false,"
                + "\"InformeCMAColCMA\": true,"
                + "\"AcuseRecibo\": false,"
                + "\"InformeCMA1Pag\": false,"
                + "\"APEAM\": false,"
                + "\"IdZonaMensajeria\": 15,"
                + "\"CobrarMensajeria\": false,"
                + "\"CobrarMensajeriaMinimo\": false,"
                + "\"ValorMinimo\": 1000.0,"
                + "\"AvisarSuperaLMR\": false,"
                + "\"AvisarSuperaCMA\": false,"
                + "\"TipoNombre\": \"entidad\","
                + "\"NumMuestrasHist\": 0,"
                + "\"Impagado\": false,"
                + "\"Imprimible\": true,"
                + "\"EsGrupo\": false,"
                + "\"FormasEnvio\": [2, 6],"
                + "\"CamposAPEAM\": \"\","
                + "\"Direcciones\": [{"
                + "\"CodDireccion\": \"\","
                + "\"Descripcion\": \"FISCAL G PATRIA\","
                + "\"Direccion\": \"AV PATRIA 1201 LOMAS DEL VALLE\","
                + "\"CodPostal\": \"45129\","
                + "\"Ciudad\": \"Ibague\","
                + "\"Provincia\": \"\","
                + "\"CodPais\": \"MX\","
                + "\"Telefono\": \"\","
                + "\"Fax\": \"\","
                + "\"Email\": \"marit@edu.uag.mx\","
                + "\"PorDefecto\": true,"
                + "\"CodContacto1\": \"MARITZA QUEZADA OLVERA\","
                + "\"CodContacto2\": \"JHON SOLIS\","
                + "\"CodContacto3\": \"ANDREA SOLIS\","
                + "\"CodContacto4\": \"\","
                + "\"CodContacto5\": \"\","
                + "\"CodContacto6\": \"\","
                + "\"CodContacto7\": \"\","
                + "\"CodContacto8\": \"\","
                + "\"CodContacto9\": \"\","
                + "\"CodContacto10\": \"\","
                + "\"CodContacto11\": \"\","
                + "\"CodContacto12\": \"\""
                + "}],"
                + "\"Contactos\": [{"
                + "\"CodContacto\": \"99900000176387\","
                + "\"Nombre\": \"MARITZA A.\","
                + "\"Apellidos\": \"QUEZADA OLVERA\","
                + "\"Funcion\": \"Financiero\","
                + "\"Telefono\": \"3314415200\","
                + "\"Pais\": \"MX\","
                + "\"Email\": \"mlquezada@edu.uag.mx\","
                + "\"EnviarMail\": true,"
                + "\"EnviarFax\": false,"
                + "\"EnviarFTP\": false,"
                + "\"UsuarioFTP\": \"\","
                + "\"ClaveFTP\": \"\","
                + "\"Direccion\": \"\","
                + "\"Observaciones\": \"ok\","
                + "\"AdministradorBesafer\": false,"
                + "\"RutNif\": \"SNF\""
                + "}, {"
                + "\"CodContacto\": \"7921005\","
                + "\"Nombre\": \"JHON CARLOS\","
                + "\"Apellidos\": \"SOLIS OCHOA\","
                + "\"Funcion\": \"Ingenieria\","
                + "\"Telefono\": \"3173763731\","
                + "\"Pais\": \"CO\","
                + "\"Email\": \"jsolis@edu.uag.mx\","
                + "\"EnviarMail\": true,"
                + "\"EnviarFax\": false,"
                + "\"EnviarFTP\": false,"
                + "\"UsuarioFTP\": \"\","
                + "\"ClaveFTP\": \"\","
                + "\"Direccion\": \"OTRA DIRECCION DIFERENTE\","
                + "\"Observaciones\": \"ok\","
                + "\"AdministradorBesafer\": false,"
                + "\"RutNif\": \"SNF\""
                + "}]"
                + "}"
                + "}";


        pl.setProperty("message", jsonString);

        AGQX3Clients agqx3Clients = new AGQX3Clients();
        agqx3Clients.processAction(pl);*/


        //EpiActionService ep = new EpiActionService();
        //ep.processAction(pl);
        //pl.setProperty("menu", "false");
        //ProductCodeGenerator codeGenerator = new ProductCodeGenerator();
        //codeGenerator.processAction(pl);
//        pl.setProperty("codPresupuesto", "QMT-ES260100041");
//        PresupuestoDetalleOriginalLocal pd = new PresupuestoDetalleOriginalLocal();
//        pd.processAction(pl);

//        pl.setProperty("CodMuestra", "AT-26/000092");
//        MuestraExtendida me = new MuestraExtendida();
//        me.processAction(pl);
        //String requestJson ="{\"sdcid\":\"Request\",\"requestdt\":\"28/01/2026\",\"submitbydepartmentid\":\"ES\",\"u_fechallegada\":\"28/01/2026 14:23:54\",\"u_agencias\":\"CLIENTE\",\"u_cod_albaran\":\"Albarán Interno Entregada Cliente\",\"inv_payeraddressid\":\"ES20-00037396\",\"u_nombrecliente\":\"ARGANO ASESORES, S.L.\",\"u_fechadentrega\":\"28/01/2026\",\"u_tipodeenvase\":\"STE\",\"u_envase\":\"Sin Tipo Envase\",\"u_nbultos\":\"0\",\"u_portespagados\":\"Y\",\"u_fechadeenvio\":\"28/01/2026\",\"u_precio\":\"0\",\"u_idmoneda\":\"Euros\",\"u_idorigen\":\"0\",\"u_iddestino\":\"ES\",\"u_pesototal\":\"0\",\"u_volumetrico\":\"0\",\"u_alto\":\"0\",\"u_ancho\":\"0\",\"u_largo\":\"0\",\"u_idincidenciatransporte\":\"0\",\"u_tipo\":\"2\",\"u_facturarincidencia\":\"N\",\"u_motivoincidencia\":\"Sin Incidencia\",\"u_tarifado\":\"N\",\"notes\":\"\",\"items\":[{\"sdcid\":\"LV_RequestItem\",\"productid\":\"AT-ES-0001\",\"requestid\":\"R-20260128-00008\",\"productversionid\":\"1\",\"templatesdcid\":\"Sample\",\"itemcount\":1,\"appliedflag\":\"Y\",\"auditsequence\":6,\"requesttext\":\"Prueba 28 Enero 26.\",\"requestitemstatus\":\"Pending\",\"shippinglocationdepartmentid\":\"ES\",\"notes\":\"notes a mano\",\"activeflag\":\"Y\",\"u_sampletype\":\"001710\",\"u_muestreador\":\"por Cliente\",\"u_horamuestreo\":\"2026-01-27T00:00:00\",\"u_albaranesdetransporte\":\"ZZZZZ\",\"u_prioridad\":\"N\",\"u_centro_produccion\":\"ES20\",\"u_cod_familia\":\"AT\",\"u_descripcion\":\"Prueba 28 Enero 26.\",\"u_prioridad_cliente\":\"N\",\"u_ofertaid\":\"QSP-ES250100021\",\"u_autorecibir\":\"Y\",\"u_snu\":\"N\",\"u_campana\":\"2026\",\"u_facturarmuestreo\":\"N\",\"u_seguimiento\":\"N\",\"u_puntoseco\":\"N\",\"u_refrendo\":\"N\",\"u_trazas\":\"N\",\"u_enviartrazasmail\":\"Y\",\"u_trazasadjuntas\":\"N\",\"u_informe_no_seguimiento\":\"Y\",\"u_generainformeweb\":\"N\",\"u_receiveddt\":\"2026-01-28T14:23:39.1176682+01:00\",\"u_inv_payeraddressid\":\"00100\",\"u_inv_payeraddresstype\":\"Customer\",\"u_direccionid\":\"001\",\"u_conditionlabel\":\"EnTransito\",\"u_nombresampletype\":\"FILTROS (Sop. Em.)\",\"u_leadtime\":\"2026-01-28T00:00:00+01:00\",\"u_facturada\":\"N\",\"u_pagada\":\"Y\"}]}";
        String json =  "";


        pl.setProperty("nombredocumento", "AT-26_051641.pdf");
        pl.setProperty("idtipodocumento", "1");
        pl.setProperty("codsociedad", "ES");
        pl.setProperty("codplanta", "ES20");
        pl.setProperty("codmuestra", "AT-26/051642");
        pl.setProperty("codcliente", "ES20-00033056");
        pl.setProperty("idestudio", "");
        pl.setProperty("idparcela", "0");
        pl.setProperty("reporte", "C:/Users/jhon.solis/Downloads/AT-26_051641.pdf");

        AddReporteBesafer rb = new AddReporteBesafer();
        rb.processAction(pl);

        //validateServiceConnection ys = new validateServiceConnection();

        //ys.processAction(pl);
    }
}