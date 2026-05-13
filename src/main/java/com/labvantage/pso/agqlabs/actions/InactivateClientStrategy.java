package com.labvantage.pso.agqlabs.actions;

import org.json.JSONObject;
import sapphire.SapphireException;


/**
 * Estrategia para inactivar un cliente.
 */
public class InactivateClientStrategy implements ActionStrategy {

    @Override
    public void execute(AGQX3ClientsFactory21 processor, JSONObject joRequest, String addressId) throws SapphireException {
        processor.inactiveClient(addressId, AGQX3ClientsFactory21.CUSTOMER);
    }
}
