/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.labvantage.pso.agqlabs.actions;

import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.ActionProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

/**
 *
 * @author Usuario
 */
public class DesvalidarAction extends BaseAction {

    private static String QUERY_DEPARMENT="with muestraPadre AS (select u_modificar, conditionlabel from s_sample ss where s_sampleid = '$2') SELECT distinct s.keyid1, s.paramlistid , s.paramlistversionid ,s.variantid ,s.dataset, mp.u_modificar, mp.conditionlabel from s_sample ss inner join sdidata s on s.keyid1 =ss.s_sampleid and s.s_datasetstatus='Completed' and s.u_aprobado ='Y' and s.testingdepartmentid ='$1' and s.s_qcbatchid is not null inner join sdidataitem s2 on s2.keyid1 =ss.s_sampleid and s2.releasedflag ='Y' inner join sdiworkitem s3 on s3.keyid1 =ss.s_sampleid cross join muestraPadre mp where ss.u_mode is not NULL and ss.s_sampleid in(select ss.destsampleid from s_samplemap ss where ss.sourcesampleid ='$2')";
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
      //  super.processAction(properties); //To change body of generated methods, choose Tools | Templates.
      
     
      String departamento=properties.getProperty("departamento");
      String keyid1=properties.getProperty("keyid1");
    
      this.logger.info("Start DesvalidarAction "+" "+departamento+" "+keyid1);
    
        this.unvalidation(departamento, keyid1);
      
      this.logger.info("END DesvalidarAction");
      
    }
    
    
    
    public void unvalidation(String departamento,String id) throws ActionException{
      this.logger.info("Start DesvalidarAction "+" "+departamento+" "+id);
        PropertyList props=new PropertyList();
        
        String query=QUERY_DEPARMENT;
        query=query.replace("$1", departamento);
        query=query.replace("$2", id);
        
       DataSet data= this.getQueryProcessor().getSqlDataSet(query);
       this.logger.info("contador "+data.getRowCount());
        if(data.getRowCount()<=0){//query return no data
            throw new ActionException("No hay datos a validar");
        }else{//return data
            for(int i=0;i<data.getRowCount();i++){//inside iteration
                String dataset=data.getValue(i, "dataset");
                String keyid1=data.getValue(i, "keyid1");
                String paramlistid=data.getValue(i, "paramlistid");
                String paramlistversionid=data.getValue(i, "paramlistversionid");
                String variantid=data.getValue(i, "variantid");
                props.setProperty("sdcid", "Sample");
                props.setProperty("keyid1", keyid1);
                props.setProperty("paramlistid", paramlistid);
                props.setProperty("paramlistversionid", paramlistversionid);
                props.setProperty("variantid", variantid);
                props.setProperty("dataset", dataset);
                // new properties
                props.setProperty("u_aprobado", "N");
                props.setProperty("u_aprobadodt", "(null)");
                props.setProperty("u_aprobadoby", "(null)");
                this.excecutionAction("EditDataSet", props);
            }

            String estadoActual=data.getValue(0,"conditionlabel");
            int modificacion= Integer.parseInt(data.getValue(0,"u_modificar"));
            int modifica = this.modificar(estadoActual, modificacion);
            this.editData(id, modifica);
        }
        
        
    }
    
    private int modificar(String estadoActual, int modificacion){
        this.logger.info("Estado y modificacion de la muestra antes de desvalidar: "
                + estadoActual + " " + modificacion);
       return "ValidadaEnviada".equalsIgnoreCase(estadoActual)
           ? modificacion + 1
           : modificacion;

    }
    
    
     private void excecutionAction(String action, PropertyList props) throws ActionException {
        this.logger.info("start execution " + action + " " + props.toJSONString());
        ActionProcessor ap = this.getActionProcessor();
        ap.processAction(action, "1", props);

    }
     
     private void editData(String keyid1,int modifica) throws ActionException{
        PropertyList props=new PropertyList();
         props.setProperty("sdcid", "Sample");
         props.setProperty("keyid1", keyid1);
         props.setProperty("u_modificar", String.valueOf(modifica));
         props.setProperty("conditionlabel", "Finalizada");
         this.excecutionAction("EditSDI", props);
     }

    private String returnValue(String sql, String param1, String param2, String value) {
        this.logger.info("returnValue Execute Query Start"); // parametro id a recibir
        SafeSQL safeSQL = new SafeSQL();
        String row = "";
        sql = sql.replace("$1", param1);
        sql = sql.replace("$2", param2);
        //safeSQL.addVar(param1);
        //safeSQL.addVar(param2);
        safeSQL.reset();
        DataSet data = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
        if (data.getValue(0, value).length() > 0) {
            this.logger.info("La query Extrajo datos " + sql);
            row = data.getValue(0, value);
            this.logger.info("value return " + row);
        }
        return row;
    }
}
