package com.labvantage.pso.agqlabs.actions;

import org.json.JSONObject;
import sapphire.SapphireException;

public interface ActionStrategy {

    void execute(AGQX3ClientsFactory21 processor, JSONObject joRequest, String addressId) throws SapphireException;

}
