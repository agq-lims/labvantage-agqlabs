package com.labvantage.pso.agqlabs.actions;

import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public interface FieldProcessor {
    void process(String value, JSONObject itemJson, PropertyList pl) throws SapphireException;
}

