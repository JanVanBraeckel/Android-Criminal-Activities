package com.example;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

public class CrimeDaoGenerator {
    public static void main(String[] args) throws Exception{
        Schema schema = new Schema(1, "com.example.gebruiker.inspectorgadget.database");
        schema.enableKeepSectionsByDefault();

        Entity crime = schema.addEntity("Crime");
        crime.addIdProperty().autoincrement();
        crime.addStringProperty("title");
        crime.addDateProperty("date");
        crime.addBooleanProperty("solved");
        crime.addStringProperty("suspect");
        crime.addStringProperty("picturePath");

        new DaoGenerator().generateAll(schema, "../app/src/main/java");
    }
}
