package com.objy.se;

import com.objy.data.Instance;
import com.objy.data.Reference;
import com.objy.db.Connection;
import com.objy.db.Objy;
import com.objy.db.Transaction;
import com.objy.db.TransactionMode;

import org.apache.commons.text.RandomStringGenerator;

public class TestNewAssoc {
    /**
     * @param args the command line arguments
     */
  
    public static int numPerson = 100;
    public static int numCalls = 500;
  
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
      objyAccess.initAccessMode();
      tx.commit();
      
      RandomStringGenerator nameGen = new RandomStringGenerator.Builder()
                                                .withinRange('a', 'z').build();

      RandomStringGenerator numberGen = new RandomStringGenerator.Builder()
                                                .withinRange('1', '9').build();

      createPersonAssocSegObjects(tx, objyAccess, nameGen, numberGen);

      createPersonAssocMapObjects(tx, objyAccess, nameGen, numberGen);
      
      createPersonAssocSegObjects(tx, objyAccess, nameGen, numberGen);

      createPersonAssocMapObjects(tx, objyAccess, nameGen, numberGen);
      
      Transaction.getCurrent().close();
    }
    
    private static void createPersonAssocMapObjects(Transaction tx,
            ObjyAccess objyAccess, RandomStringGenerator nameGen, 
            RandomStringGenerator numberGen)
    {
      long timeStart = System.currentTimeMillis();
      
      tx.start(TransactionMode.READ_UPDATE);
      
      for (int personI = 0; personI < numPerson; personI++)
      {
        String name = nameGen.generate(20);
        Instance caller = objyAccess.createPersonAssocMap(personI, name);
        Reference callerRef = new Reference(caller);
        for (int phoneI = 0; phoneI < numCalls; phoneI++)
        {
          String phoneNumber = numberGen.generate(10);
          Instance call = objyAccess.createCall(phoneI, phoneNumber, callerRef);
          //System.out.println("Name: " + name + " >> phoneCall: " + phoneNumber);
          objyAccess.addCallToPersonAssocMap(new Reference(call), callerRef);
        }
        if (((personI+1)%10)==0)
        {
          tx.commit();
          System.out.println("... processed " + (personI+1) + " callers.");
          tx.start(TransactionMode.READ_UPDATE);
        }
      }

      Transaction.getCurrent().commit();
      double diff = (System.currentTimeMillis() - timeStart)/1000.0;
      System.out.println("... processTime for AssocMap: " + diff);
      
    }
    
    private static void createPersonAssocSegObjects(Transaction tx,
            ObjyAccess objyAccess, RandomStringGenerator nameGen, 
            RandomStringGenerator numberGen)
    {
      long timeStart = System.currentTimeMillis();
      tx.start(TransactionMode.READ_UPDATE);
      
      for (int personI = 0; personI < numPerson; personI++)
      {
        String name = nameGen.generate(20);
        Instance caller = objyAccess.createPersonAssocSeg(personI, name);
        Reference callerRef = new Reference(caller);
        for (int phoneI = 0; phoneI < numCalls; phoneI++)
        {
          String phoneNumber = numberGen.generate(10);
          Instance call = objyAccess.createCall(phoneI, phoneNumber, callerRef);
          //System.out.println("Name: " + name + " >> phoneCall: " + phoneNumber);
          objyAccess.addCallToPersonAssocSeg(new Reference(call), callerRef);
        }
        if (((personI+1)%10)==0)
        {
          tx.commit();
          System.out.println("... processed " + (personI+1) + " callers.");
          tx.start(TransactionMode.READ_UPDATE);
        }
      }

      Transaction.getCurrent().commit();
      double diff = (System.currentTimeMillis() - timeStart)/1000.0;
      System.out.println("... processTime for AssocSeg: " + diff);
      
    }
}
