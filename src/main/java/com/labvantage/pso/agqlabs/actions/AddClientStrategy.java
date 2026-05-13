package com.labvantage.pso.agqlabs.actions;


import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

/**
 * Estrategia para agregar un nuevo cliente.
 */
public class AddClientStrategy implements ActionStrategy{
    @Override
    public void execute(AGQX3ClientsFactory21 processor, JSONObject joRequest, String addressId) throws SapphireException {
        PropertyList addClient = processor.buildClientPropertyList(joRequest);
        processor.addClient(addClient, joRequest);
    }
}
