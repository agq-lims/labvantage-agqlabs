/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.labvantage.pso.agqlabs.actions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.action.AddSDI;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

/**
 *
 * @author gustavo.rojas
 */
public class presupuestomodificacion extends BaseAction {

    private String CLASSNAME = "presupuestomodificacion";
    public static final String ESTADO_VACIO = "4";
    private boolean error = false;
    private String output = "";
    private String processlog = "";
    private String messagetag = "";
    private String warning = "";
    private String url = "";
    private int status;
    SafeSQL safeSQL = new SafeSQL();


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
        String pTipoPresupuesto = props.getProperty("TipoPresupuesto", ""); // QMT QSP QCF QCA
        //
        String vOfertaId = getOfertaId(pCodPresupuesto);
        String strSeguimiento = "N";
        String strTemp = "";
        String addOferta = "EditSDI";
        String estado;
        //

        if (vOfertaId.isBlank()) {
            addOferta = "AddSDI";
            vOfertaId = pCodPresupuesto;
            logger.info("No se encuentra el presupuesto con código: " + pCodPresupuesto + " se adicionará como nuevo");
        }
        //
        PropertyList plData = new PropertyList();
            estado = getEstado(props.getProperty("Estado", ESTADO_VACIO));
            plData.setProperty("cod_planta_venta", props.getProperty("CodPlantaVenta", ""));
            plData.setProperty("tipo_presupuesto", pTipoPresupuesto);
            plData.setProperty("cod_presupuesto", pCodPresupuesto);
            plData.setProperty("num_revision", props.getProperty("NumRevision", ""));
            plData.setProperty("cod_referencia_cliente", getPropertyOrEmpty(props,"CodReferenciaCliente"));
            plData.setProperty("fecha_presupuesto", props.getProperty("FechaPresupuesto", "").replace("T", " "));
            plData.setProperty("clienteid", obtenerAddressIdCliente(props.getProperty("CodCliente", "")));
            plData.setProperty("moneda", props.getProperty("CodDivisa", ""));
            plData.setProperty("importe", props.getProperty("Importe", ""));
            plData.setProperty("importe_total", props.getProperty("ImporteTotal", ""));
            plData.setProperty("aplicaips", fixValBool(props.getProperty("AplicaIPC", "")));
            plData.setProperty("direccion", props.getProperty("CodDireccion", ""));
            plData.setProperty("cod_comercial", props.getProperty("CodComercial", ""));
            plData.setProperty("cod_regimen_impuestos", props.getProperty("CodRegimenImpuestos", ""));
            plData.setProperty("forma_pago", props.getProperty("CodCondicionPago", ""));
            plData.setProperty("estado", estado);
            plData.setProperty("fecha_aceptacion", props.getProperty("FechaMaxValidez", "").replace("T", " "));
            plData.setProperty("cliente_facturar", props.getProperty("CodClienteFactura", ""));
            plData.setProperty("fecha_inicio", props.getProperty("FechaVigenciaInicial", "").replace("T", " "));
            plData.setProperty("fecha_fin", props.getProperty("FechaVigenciaFinal", "").replace("T", " "));
            plData.setProperty("titulo", props.getProperty("Titulo", ""));
            plData.setProperty("subtitulo", props.getProperty("SubTitulo", ""));
            plData.setProperty("cod_contacto", props.getProperty("CodContacto", ""));
            plData.setProperty("notes", props.getProperty("Observaciones", ""));
            plData.setProperty("idcreador", props.getProperty("IdCreador", ""));
            plData.setProperty("idmodificador", props.getProperty("IdModificador", ""));
            plData.setProperty("incluir_mensajeria", fixValBool(props.getProperty("IncluirMensajeria", "")));
            plData.setProperty("fecha_ultima_aceptacion", props.getProperty("FechaUltimaAceptacion", "").replace("T", " "));
            plData.setProperty("version", props.getProperty("Version", ""));
            plData.setProperty("fecha_fin_presupuesto", props.getProperty("FechaFinPresupuesto", "").replace("T", " "));
            //
            String strProyecto = "N";
            if ("QCA".equalsIgnoreCase(pTipoPresupuesto)) {
                strProyecto = "Y";
            }
            plData.setProperty("proyecto", strProyecto);
            //
            strTemp = props.getProperty("Lineas", "");
            //
            try {
                JSONArray joa = new JSONArray(strTemp);
                for (int k = 0; k < joa.length(); k++) {
                    JSONObject jox = joa.getJSONObject(k);
                    Object ox = jox.get("ATanalitico");
                    if (ox.equals(true)) {
                        ox = jox.get("CodCategoriaServicio");
                        if (ox.equals("NAG") || ox.equals("EAG")) {
                            strSeguimiento = "Y";
//                        break;
                        }
                    }
                }
            } catch (JSONException ex) {
                this.error = true;
                this.warning += "Se ha presentado un error en el registro del presupuesto " + ex.getMessage();
                java.util.logging.Logger.getLogger(presupuestomodificacion.class.getName()).log(Level.SEVERE, null, ex);
            }

        //

            plData.setProperty("seguimiento", strSeguimiento);
            //
            plData.setProperty("sdcid", "Oferta");
            plData.setProperty("keyid1", vOfertaId);

            //
            meLogInfo(this.CLASSNAME + " plData=" + plData.toJSONString());
            getActionProcessor().processAction(addOferta, "1", plData);
            String ofertaid = vOfertaId;
            //
            try {
                JSONArray joa = new JSONArray(strTemp);
                String codArticulo;
                String idAnalisisTipo ;
                String strActionName;
                String vOfertaATId;
                String codLinea;
                for (int k = 0; k < joa.length(); k++) {
                    JSONObject jox = joa.getJSONObject(k);
                    JSONObject jol;
                    JSONArray joc;
                    Object ox = jox.get("ATanalitico");
                    if (ox.equals(true)) {
                        //
                        plData.clear();

                        idAnalisisTipo = valueJsonString(jox, "CodArticulo");
                        codArticulo = obtenerProductoPorAnalisisTipo(idAnalisisTipo.substring(1));

                        logger.info("codArticulo=",codArticulo);
                        if (codArticulo == null || codArticulo.trim().isEmpty()) {
                            logger.warn("No se encontró producto para idAnalisisTipo=" + idAnalisisTipo +
                                    " en la línea " + valueJsonString(jox, "CodLinea"));
                            continue; // salta este registro y pasa al siguiente del JSON
                        }

                        plData.setProperty("sdcid", "OfertaAT");
                        plData.setProperty("ofertaid", ofertaid);

                        strActionName = "AddSDI";


                        //Identificar si la linea existe para editar o para crear
                        codLinea = valueJsonString(jox, "CodLinea");
                        meLogInfo(this.CLASSNAME + " codLinea=" + codLinea + " codLinea=" + codLinea);
                        vOfertaATId = getOfertaATId(vOfertaId, codLinea);
                        meLogInfo(this.CLASSNAME + " vOfertaATId=" + vOfertaATId);
                        //
                        if (!vOfertaATId.isEmpty()) {
                            strActionName = "EditSDI";
                            plData.setProperty("keyid1", vOfertaATId);
                        }


                        plData.setProperty("at_analitico", "Y");
                        plData.setProperty("idcentroproduccion", valueJsonString(jox, "IdCentroProduccionSeleccionado"));
                        plData.setProperty("numlinea", valueJsonString(jox, "NumLinea"));
                        plData.setProperty("codlinea", codLinea);
                        plData.setProperty("cantidad", valueJsonString(jox, "Cantidad"));
                        plData.setProperty("codarticulo", idAnalisisTipo);
                        plData.setProperty("cod_unidad_venta", valueJsonString(jox, "CodUnidadVenta"));
                        plData.setProperty("preciobruto", valueJsonString(jox, "PrecioBruto"));
                        plData.setProperty("descuento", valueJsonString(jox, "Descuento"));
                        plData.setProperty("codareanegocio", valueJsonString(jox, "CodAreaNegocio"));
                        plData.setProperty("codpnt", valueJsonString(jox, "CodPnt"));
                        plData.setProperty("idfamilia", valueJsonString(jox, "IdFamilia"));
                        plData.setProperty("idsubfamilia", valueJsonString(jox, "IdSubFamilia"));
                        plData.setProperty("idtipomuestra", formatearIdTipoMuestra(valueJsonString(jox, "IdTipoMuestra")));
                        plData.setProperty("idlugarmuestreo", valueJsonString(jox, "IdLugarMuestreo"));
                        plData.setProperty("idpuntomuestreo", valueJsonString(jox, "IdPuntoMuestreo"));
                        plData.setProperty("categoriaservicio", valueJsonString(jox, "CodCategoriaServicio"));
                        plData.setProperty("cuota", valueJsonString(jox, "Cuota"));
                        plData.setProperty("version", valueJsonString(jox, "Version"));
                        //
                        plData.setProperty("idanalisistipo", codArticulo);
                        //
                        if ("QCAxxxxxxxxxx".equalsIgnoreCase(pTipoPresupuesto)) {
                            ox = jox.getString("DesglosePresupuestoAbierto");
                            if (!ox.equals("null")) {
                                ox = jox.getJSONArray("DesglosePresupuestoAbierto");
                                JSONArray ox2 = (JSONArray) ox;
                                if (ox2.length() > 0) {
                                    for (int jj = 0; jj < ox2.length(); jj++) {
                                        if (jj == 0) {
                                            logger.info("88888888888 jj=" + jj);
                                            plData.setProperty("codarticulo", valueJsonString(ox2.getJSONObject(jj), "CodComponente"));
                                            plData.setProperty("preciobruto", valueJsonString(ox2.getJSONObject(jj), "Precio"));
                                            plData.setProperty("codareanegocio", valueJsonString(ox2.getJSONObject(jj), "CodAreaNegocio"));
                                            plData.setProperty("categoriaservicio", valueJsonString(ox2.getJSONObject(jj), "CodCategoriaServicio"));
                                            plData.setProperty("codpnt", valueJsonString(ox2.getJSONObject(jj), "IdPnt"));
                                            plData.setProperty("id_empresa", valueJsonString(ox2.getJSONObject(jj), "IdEmpresa"));
                                            plData.setProperty("idfamilia", valueJsonString(ox2.getJSONObject(jj), "IdFamiliaTm"));
                                            plData.setProperty("idsubfamilia", valueJsonString(ox2.getJSONObject(jj), "IdSubFamiliaTm"));
                                            plData.setProperty("descuento", valueJsonString(ox2.getJSONObject(jj), "Descuento"));
                                            plData.setProperty("codlineaarticulodppa", valueJsonString(ox2.getJSONObject(jj), "CodLineaArticuloDppa"));
                                            plData.setProperty("codlineaarticulodpa", valueJsonString(ox2.getJSONObject(jj), "CodLineaArticuloDPA"));
                                            plData.setProperty("num_linea_dpa", valueJsonString(ox2.getJSONObject(jj), "NumLineaDPA"));
                                        }
                                    }
                                }
                            }
                        }
                        //
                        plData.setProperty("visible", "N");
                        ox = jox.getString("TipoServicioCma");
                        if (ox.equals("null") || ox.equals("")) {
                            ox = jox.getString("TipoServicioLmr");
                            if (!ox.equals("null") && !ox.equals("")) {
                                // LMR
                                jol = jox.getJSONObject("TipoServicioLmr");
                                meLogInfo(this.CLASSNAME + "k=" + k + " TipoServicioLmr jol=" + jol.toString());
                                //
                                plData.setProperty("lmr", "Y");
                                plData.setProperty("trazas", valueJsonBoolean(jol, "Trazas"));
                                String strEnvioDeTrazas = valueJsonString(jol, "EnvioDeTrazas");
                                if (null == strEnvioDeTrazas) {
                                    plData.setProperty("enviartrazasmail", "N");
                                    plData.setProperty("trazasadjuntas", "N");
                                } else {
                                    switch (strEnvioDeTrazas) { // trazasadjuntas
                                        case "1":
                                            plData.setProperty("enviartrazasmail", "Y");
                                            plData.setProperty("trazasadjuntas", "Y");
                                            break;
                                        case "2":
                                            plData.setProperty("enviartrazasmail", "Y");
                                            plData.setProperty("trazasadjuntas", "N");
                                            break;
                                        case "3":
                                            plData.setProperty("enviartrazasmail", "N");
                                            plData.setProperty("trazasadjuntas", "N");
                                            break;
                                        default:
                                            plData.setProperty("enviartrazasmail", "N");
                                            plData.setProperty("trazasadjuntas", "N");
                                            break;
                                    }
                                }
                                plData.setProperty("resumenlmr", valueJsonBoolean(jol, "ResumenLMR"));
                                plData.setProperty("cortolmr", valueJsonBoolean(jol, "InformeLMRCorto"));
                                //
                                plData.setProperty("cma", "N");
                                plData.setProperty("avisasuperacma", "N");
                                plData.setProperty("cortocma", "N");
                                plData.setProperty("cma1pag", "N");
                                //
                                meLogInfo(this.CLASSNAME + " plData=" + plData.toJSONString());
                                getActionProcessor().processAction(strActionName, "1", plData);

                                if ("AddSDI".equalsIgnoreCase(strActionName)) {
                                    vOfertaATId = plData.getProperty("newkeyid1");
                                }
                                //
                                joc = jol.getJSONArray("PaisesLMR");
                                int us = 0;
                                for (int jj = 0; jj < joc.length(); jj++) {
                                    Object co = joc.getString(jj);
                                    if (!co.equals("null") && !"".equals(co)) {
                                        String countryData = getCountry((String) co);
                                        if (!"".equals(countryData)) {
                                            String[] cov = StringUtil.split(countryData, "|");
                                            us++;
                                            String lmrid = getLMRId(vOfertaATId, cov[0]);
                                            plData.clear();
                                            plData.setProperty("sdcid", "OfertaAT");
                                            plData.setProperty("linkid", "ofertaat_lmr_link");
                                            plData.setProperty("propsmatch", "Y");
                                            plData.setProperty("applylock", "N");
                                            plData.setProperty("copies", "1");
                                            if ("".equals(lmrid)) {
                                                strActionName = "AddSDIDetail";
                                                plData.setProperty("u_ofertaatid", vOfertaATId);
                                                plData.setProperty("lmrid", cov[0]);
                                            } else {
                                                strActionName = "EditSDIDetail";
                                                plData.setProperty("keyid1", vOfertaATId);
                                                plData.setProperty("keyid2", cov[0]);
                                                plData.setProperty("u_ofertaatid", vOfertaATId);
                                                plData.setProperty("lmrid", cov[0]);
                                            }
                                            plData.setProperty("usersequence", "" + us);
                                            plData.setProperty("codpais", (String) co);
                                            plData.setProperty("descpais", cov[1]);
                                            meLogInfo(this.CLASSNAME + " plData=" + plData.toJSONString());
                                            getActionProcessor().processAction(strActionName, "1", plData);
                                        } else {
                                            this.warning += "En la línea " + valueJsonString(jox, "CodLinea") + " no se encuentra el país con código: " + (String) co + "\n";
                                            //this.error = true;
                                            //this.output = "No se encuentra el país con código: " + (String) co;
                                            //break;
                                        }
                                    }
                                }
                            } else {
                                meLogInfo(this.CLASSNAME + " plData=" + plData.toJSONString());
                                getActionProcessor().processAction(strActionName, "1", plData);
                            }
                            //
                        } else {
                            // CMA
                            jol = jox.getJSONObject("TipoServicioCma");
                            meLogInfo(this.CLASSNAME + "k=" + k + " TipoServicioCma jol=" + jol.toString());
                            //
                            plData.setProperty("cma", "Y");
                            plData.setProperty("avisasuperacma", valueJsonBoolean(jol, "AvisarSuperaCMA"));
                            plData.setProperty("cortocma", valueJsonBoolean(jol, "InformeCMACorto"));
                            plData.setProperty("cma1pag", valueJsonBoolean(jol, "CMA1Pag"));
                            //
                            plData.setProperty("lmr", "N");
                            plData.setProperty("trazas", "N");
                            plData.setProperty("enviartrazasmail", "N");
                            plData.setProperty("trazasadjuntas", "N");
                            plData.setProperty("resumenlmr", "N");
                            plData.setProperty("cortolmr", "N");
                            //
                            meLogInfo(this.CLASSNAME + " plData=" + plData.toJSONString());
                            getActionProcessor().processAction(strActionName, "1", plData);
                            //
                            if ("AddSDI".equalsIgnoreCase(strActionName)) {
                                vOfertaATId = plData.getProperty("newkeyid1");
                            }
                            //

                            DataSet dsLegislacion = getLegislacion(valueJsonString(jol, "IdLegislacion"));
                            //
                            if (dsLegislacion.getRowCount() > 0) {
                                String vLegislacionA = getIdLegislacionActual(vOfertaATId);
                                plData.clear();
                                if ("".equals(vLegislacionA)) {
                                    // Add
                                    strActionName = "AddSDIDetail";
                                    plData.setProperty("u_ofertaatid", vOfertaATId);
                                    plData.setProperty("legislacionid", dsLegislacion.getValue(0, "specid"));
                                } else if (!vLegislacionA.equals(dsLegislacion.getValue(0, "specid"))) {
                                    // Delete and Add
                                    plData.setProperty("sdcid", "OfertaAT");
                                    plData.setProperty("linkid", "ofertaat_legislacion_link");
                                    plData.setProperty("u_ofertaatid", vOfertaATId);
                                    plData.setProperty("legislacionid", vLegislacionA);
                                    getActionProcessor().processAction("DeleteSDIDetail", "1", plData);
                                    //
                                    strActionName = "AddSDIDetail";
                                    plData.clear();
                                    plData.setProperty("u_ofertaatid", vOfertaATId);
                                    plData.setProperty("legislacionid", dsLegislacion.getValue(0, "specid"));
                                } else {
                                    // Edit
                                    strActionName = "EditSDIDetail";
                                    plData.setProperty("keyid1", vOfertaATId);
                                    plData.setProperty("keyid2", dsLegislacion.getValue(0, "specid"));
                                    plData.setProperty("u_ofertaatid", vOfertaATId);
                                    plData.setProperty("legislacionid", dsLegislacion.getValue(0, "specid"));
                                }
                                plData.setProperty("sdcid", "OfertaAT");
                                plData.setProperty("linkid", "ofertaat_legislacion_link");
                                plData.setProperty("propsmatch", "Y");
                                plData.setProperty("applylock", "N");
                                plData.setProperty("copies", "1");
                                plData.setProperty("version", dsLegislacion.getValue(0, "specversionid"));
                                plData.setProperty("codagq", dsLegislacion.getValue(0, "u_codagq"));
                                plData.setProperty("specdesc", dsLegislacion.getValue(0, "specdesc"));
                                plData.setProperty("usersequence", "1");
                                meLogInfo(this.CLASSNAME + " plData=" + plData.toJSONString());
                                getActionProcessor().processAction(strActionName, "1", plData);
                            } else {
                                this.warning += "En la línea " + valueJsonString(jox, "CodLinea") + " no se encuentra la legislación con código: " + valueJsonString(jol, "IdLegislacion") + "\n";
                                //this.error = true;
                                //this.output = "No se encuentra la legislación con código: " + valueJsonBoolean(jol, "IdLegislacion");
                                //break;
                            }
                        }
                    }
                    if (this.error) {
                        break;
                    }
                }
            } catch (JSONException ex) {
                this.warning += "Error al registrar detalle del presupuesto: " + ex.getMessage();
                this.error = true;
                java.util.logging.Logger.getLogger(presupuestomodificacion.class.getName()).log(Level.SEVERE, null, ex);
            }

        if (this.error) {
            props.setProperty("status", "ERROR");
            props.setProperty("code", "400");
            props.setProperty("outputmessage", this.output + ("".equals(this.warning) ? "" : "\n\nWARNING\n" + this.warning));
        } else {
            props.setProperty("status", "OK" + ("".equals(this.warning) ? "" : "\n\nWARNING\n" + this.warning));
            props.setProperty("code", "200");
            props.setProperty("outputmessage", "OK");
        }
    }



    private String formatearIdTipoMuestra(String valor) {
        logger.info("Se formatea el idTipoMuestra: " + valor);
        if (valor == null || valor.trim().isEmpty()) {
            return "";
        }

        valor = valor.trim();

        // Si tiene 6 o más caracteres, se deja tal cual
        if (valor.length() >= 6) {
            return valor;
        }

        // Rellenar con ceros a la izquierda hasta 6
        String idTipoMuestra = String.format("%06d", Integer.parseInt(valor));
        logger.info("IdTipoMuestra: " + idTipoMuestra);
        return idTipoMuestra;
    }



    private String obtenerAddressIdCliente(String customerId) {

        String addressId = "";
        safeSQL.reset();

        String sql =
                "select ad.addressid " +
                        "from address ad " +
                        "where ad.addresstype = 'Customer' " +
                        "and ad.u_customerid = '" + customerId + "'";

        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());

        if (ds != null && ds.getRowCount() > 0) {
            addressId = ds.getValue(0, "addressid", "");
        }

        return addressId;
    }


    private String obtenerProductoPorAnalisisTipo(String idAnalisisTipo) {
        logger.info("Ejecutando método obtenerProductoPorAnalisisTipo... " + idAnalisisTipo);

        String codProducto;
        if (idAnalisisTipo == null || idAnalisisTipo.trim().isEmpty()) {
            return "";
        }

        safeSQL.reset();

        String sql =
                "select sp.s_productid as productid " +
                        "from s_product sp " +
                        "where sp.u_id_analisis_tipo = '" + idAnalisisTipo + "'" ;
        logger.info("SQL= " + sql );
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
        logger.info("Cantidad: " + ds.getRowCount() + " Valor: " + ds.getValue(0, "productid"));
        if (ds.getRowCount() > 0) {
            codProducto = ds.getValue(0, "productid");
            logger.info("Hay datos de productos registrados: " + codProducto);

            return codProducto;
        }
        logger.info("No esta registrado el análisis tipo de código: " + idAnalisisTipo);
        return "";
    }

    private String getPropertyOrEmpty(PropertyList props, String propertyName) {
        logger.info("Ejecutando método getPropertyOrEmpty... " + propertyName);
        if (props == null || propertyName == null) {
            return "";
        }

        String value = props.getProperty(propertyName);

        if (value == null) {
            return "";
        }

        value = value.trim();

        if (value.isEmpty() || "null".equalsIgnoreCase(value)) {
            return "";
        }

        return value;
    }


    private String getCountry(String countryCod) {
        logger.info("Ejecutando método getCountry... " + countryCod);
        String result = "";
        safeSQL.reset();


        String strSQL = "select u_countriesid idpais, countriesdesc country from u_countries where codpais='" + countryCod + "'";
        DataSet dsTemp = this.getQueryProcessor().getPreparedSqlDataSet(strSQL, safeSQL.getValues());
        if (dsTemp.getRowCount() > 0) {
            String country = dsTemp.getValue(0, "country");
            logger.info("Se ha ejecutado la consulta de ciudad: " + country);
            result = dsTemp.getValue(0, "idpais") + "|" + country;
        }
        return result;
    }

    private String getOfertaId(String pCodPresupuesto) {
        logger.info("Ejecutando método getOfertaId... " + pCodPresupuesto);
        String result = "";
        String strSQL = "select u_ofertaid from u_oferta where cod_presupuesto='" + pCodPresupuesto + "'";
        DataSet dsTemp = getQueryProcessor().getSqlDataSet(strSQL);
        if (dsTemp.getRowCount() > 0) {
            result = dsTemp.getString(0, "u_ofertaid", "");
        }
        return result;
    }

    private String getOfertaATId(String ofertaid, String pCodLinea) {
        String result = "";
        String strSQL = "select u_ofertaatid from u_ofertaat where ofertaid='" + ofertaid + "' and codlinea='" + pCodLinea + "'";
        DataSet dsTemp = getQueryProcessor().getSqlDataSet(strSQL);
        if (dsTemp.getRowCount() > 0) {
            result = dsTemp.getString(0, "u_ofertaatid", "");
        }
        return result;
    }

    private DataSet getLegislacion(String pIdLegislacion) {
        logger.info("Ejecutando método getLegislacion... " + pIdLegislacion);
        DataSet dsTemp;
        String strSQL = "select specid, u_codagq, specversionid, specdesc from spec where u_codagq='" + pIdLegislacion + "'";
        dsTemp = getQueryProcessor().getSqlDataSet(strSQL);
        if (dsTemp.getRowCount() > 0) {
            logger.info("Se ha ejecutado la consulta de legislación: " + dsTemp.getRowCount());
            dsTemp = dsTemp.getRows(0, 1);
        } else {
            dsTemp = new DataSet();
        }
        return dsTemp;
    }

    private String getEstado(String estado) {
        String result = "";
        String strSQL = "select rv.refvalueid refvalueid from refvalue rv where rv.reftypeid = 'EstadoOfertas' and rv.u_abreviacion = '" + estado + "'";
        DataSet dsTemp = getQueryProcessor().getSqlDataSet(strSQL);
        if (dsTemp.getRowCount() > 0) {
            result = dsTemp.getString(0, "refvalueid", ESTADO_VACIO);
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
            this.status = connAnswer.getResponseCode();
            meLogInfo("STATUS: " + this.status);
            this.output = (this.error == true ? "ERROR! STATUS:" + this.status : "Message processed successfully.");

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

    private String getLMRId(String ofertaatid, String lmrid) {
        String result = "";
        String strSQL = "select lmrid from u_ofertaat_lmr where lmrid='" + lmrid + "' and u_ofertaatid='" + ofertaatid + "'";
        DataSet dsTemp = getQueryProcessor().getSqlDataSet(strSQL);
        if (dsTemp.getRowCount() > 0) {
            result = dsTemp.getString(0, "lmrid", "");
        }
        return result;
    }

    private String getIdLegislacionActual(String pIdOfertaAT) {
        String Temp = "";
        String strSQL = "select legislacionid from u_ofertaat_legislacion uol where u_ofertaatid='" + pIdOfertaAT + "'";
        DataSet dsTemp = getQueryProcessor().getSqlDataSet(strSQL);
        if (dsTemp.getRowCount() > 0) {
            Temp = dsTemp.getValue(0, "legislacionid");
        }
        return Temp;
    }

}
