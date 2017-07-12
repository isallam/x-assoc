package com.objy.se;

import com.objy.data.Instance;
import com.objy.data.Reference;
import com.objy.db.Connection;
import com.objy.db.Objy;
import com.objy.db.Transaction;
import com.objy.db.TransactionMode;
import java.util.ArrayList;

import org.apache.commons.text.RandomStringGenerator;

public class TestNewAssoc {
    /**
     * @param args the command line arguments
     */
  
    public static int numPerson = 10;
    public static int numCalls = 100;
  
    public static void main(String[] args)
    {

      if (args.length < 1) {
        System.out.println("Params missing: <boot_file_path");
        return;
      }
      String bootFile = args[0];
      
      System.out.println("Objectivity federation: " + bootFile);
      
      // Objy initialization
      Objy.enableConfiguration();
      Connection connection = new Connection(bootFile);
      
      ObjyAccess objyAccess = new ObjyAccess();
 
      Transaction tx = new Transaction(TransactionMode.READ_ONLY, "spark_write");
      boolean schemaInitOK = objyAccess.initAccessMode();
      tx.commit();
      
      if (!schemaInitOK)
      {
        System.err.println("ERROR: Schema is not initialized correctly.");
        System.err.println("ERROR: You need to run task 'objySetup' before running this one.");
        return;
      }
       
      RandomStringGenerator nameGen = new RandomStringGenerator.Builder()
                                                .withinRange('a', 'z').build();

      RandomStringGenerator numberGen = new RandomStringGenerator.Builder()
                                                .withinRange('1', '9').build();

      TestApp testApp = new TestApp(objyAccess, nameGen, numberGen);
      
      ArrayList<Reference> personAssocMapList = 
              testApp.getOrCreatePersonAssocMapObjects(tx, numPerson);
      
      ArrayList<Reference> personAssocSegList = 
              testApp.getOrCreatePersonAssocSegObjects(tx, numPerson);
     
      System.out.println(">> Test adding call objects to associations");
      
      for (int i = 0; i < 5; i++)
      {
        testApp.addNewCallsToPersonAssocMap(tx, personAssocMapList, numCalls);

        testApp.addNewCallsToPersonAssocSeg(tx, personAssocSegList, numCalls);
      }

      System.out.println(">> Test Reading call objects from associations");
      for (int i = 0; i < 5; i++)
      {
        testApp.readCallsFromPersonAssocMap(tx, personAssocMapList);
        testApp.readCallsFromPersonAssocSeg(tx, personAssocSegList);
      }
      Transaction.getCurrent().close();
    }
}
