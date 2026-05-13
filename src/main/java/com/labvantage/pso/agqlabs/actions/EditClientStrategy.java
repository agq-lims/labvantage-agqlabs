package com.labvantage.pso.agqlabs.actions;

import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public class EditClientStrategy implements ActionStrategy {
    @Override
    public void execute(AGQX3ClientsFactory21 processor, JSONObject joRequest, String addressId) throws SapphireException {
        PropertyList editClient = processor.buildClientPropertyList(joRequest);
        processor.editClient(editClient, addressId, joRequest);
    }
}
