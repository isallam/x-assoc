package com.objy.se;

import com.objy.db.Connection;
import com.objy.db.Objy;

import org.apache.commons.text.RandomStringGenerator;

public class TestNewAssoc {
    /**
     * @param args the command line arguments
     */
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
      RandomStringGenerator nameGen = new RandomStringGenerator.Builder()
                                                .withinRange('a', 'z').build();

      RandomStringGenerator numberGen = new RandomStringGenerator.Builder()
                                                .withinRange('1', '9').build();

      long timeStart = System.currentTimeMillis();
      for (int personI = 0; personI < 10; personI++)
      {
        String name = nameGen.generate(20);
        //caller = objyAccess.createPerson(personI, name);
        for (int phoneI = 0; phoneI < 10; phoneI++)
        {
          String phoneNumber = numberGen.generate(10);
          //call = objyAccess.createCall(phoneI, phoneNumber, caller);
          System.out.println("Name: " + name + " >> phoneCall: " + phoneNumber);
        }
      }

      double diff = (System.currentTimeMillis() - timeStart)/1000.0;
      System.out.println("... processTime: " + diff);
      // import placement.
      // ... TBD.
    }
}
