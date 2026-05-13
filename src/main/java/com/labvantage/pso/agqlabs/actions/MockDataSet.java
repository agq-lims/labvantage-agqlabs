package com.labvantage.pso.agqlabs.actions;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simula el comportamiento basico de DataSet de LabVantage
 * SOLO para pruebas sin BD
 */
public class MockDataSet {

    private final List<Map<String, String>> rows = new ArrayList<>();
    private int cursor = -1;

    /**
     * Agrega una fila simulada
     */
    public void addRow(String valueString, String valueName) {
        Map<String, String> row = new HashMap<>();
        row.put("value_string", valueString);
        row.put("value_name", valueName);
        rows.add(row);
    }


    /**
     * Obtiene un valor por índice (1-based como DataSet)
     */
    public String getString(int rowIndex, String columnName) {
        // LabVantage usa indices desde 1
        int index = rowIndex - 1;

        if (index < 0 || index >= rows.size()) {
            return null;
        }

        return rows.get(index).get(columnName);
    }

    /**
     * Retorna el numero total de filas
     */
    public int getRowCount() {
        return rows.size();
    }
}


