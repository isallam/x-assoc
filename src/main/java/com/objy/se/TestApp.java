/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objy.se;

import com.objy.data.Instance;
import com.objy.data.LogicalType;
import com.objy.data.Reference;
import com.objy.data.Variable;
import com.objy.db.Transaction;
import com.objy.db.TransactionMode;
import com.objy.expression.language.LanguageRegistry;
import com.objy.policy.Policies;
import com.objy.statement.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import org.apache.commons.text.RandomStringGenerator;

/**
 *
 * @author ibrahim
 */
public class TestApp {

  private ObjyAccess objyAccess;
  private RandomStringGenerator nameGen;
  private RandomStringGenerator numberGen;
  
  TestApp(ObjyAccess objyAccess, RandomStringGenerator nameGen, RandomStringGenerator numberGen) {
    this.objyAccess = objyAccess;
    this.nameGen = nameGen;
    this.numberGen = numberGen;
  }

  /**
   * 
   * @param tx
   * @param numPerson
   * @return 
   */
  ArrayList<Reference> getOrCreatePersonAssocMapObjects(Transaction tx, int numPerson) {
    return getOrCreatePersonObjects(tx, numPerson, true /* assocMap */);
  }

  /**
   * 
   * @param tx
   * @param numPerson
   * @return 
   */
  ArrayList<Reference> getOrCreatePersonAssocSegObjects(Transaction tx, int numPerson) {
    return getOrCreatePersonObjects(tx, numPerson, false /* !assocMap */);
  }
  
  /**
   * 
   * @param tx
   * @param numPerson
   * @param assocMapType
   * @return 
   */
  private ArrayList<Reference> getOrCreatePersonObjects(
          Transaction tx, int numPerson, boolean assocMapType) {
    
    ArrayList<Reference> personList = new ArrayList<Reference>();
    
    long timeStart = System.currentTimeMillis();

    tx.start(TransactionMode.READ_UPDATE);

    // find data first and create extra if needed.
    personList = findPesonObjects(assocMapType);
    
    if (personList.size() < numPerson)
    {
      int numToCreate = numPerson - personList.size();

      Instance caller = null;

      for (int personI = 0; personI < numToCreate; personI++) {
        String name = nameGen.generate(20);

        if (assocMapType)
          caller = objyAccess.createPersonAssocMap(personI, name);
        else
          caller = objyAccess.createPersonAssocSeg(personI, name);

        personList.add(new Reference(caller));
      }
    }
    tx.commit();
    double diff = (System.currentTimeMillis() - timeStart) / 1000.0;
    if (assocMapType)
      System.out.println("... processTime for getOrCreate PersonAssocMap: " + diff);
    else
    System.out.println("... processTime for getOrCreate PersonAssocSeg: " + diff);
      
    return personList;
  }

  /**
   * 
   * @param tx
   * @param personList
   * @param numCalls 
   */
  void addNewCallsToPersonAssocMap(Transaction tx, ArrayList<Reference> personList, int numCalls) {
    addNewCallsToObjects(tx, personList, numCalls, true /* assocMapType */);
  }

  /**
   * 
   * @param tx
   * @param personList
   * @param numCalls 
   */
  void addNewCallsToPersonAssocSeg(Transaction tx, ArrayList<Reference> personList, int numCalls) {
   addNewCallsToObjects(tx, personList, numCalls, false /* !assocMapType */);
  }

  /**
   * 
   * @param tx
   * @param personList
   * @param numCalls
   * @param assocMapType 
   */
  private void addNewCallsToObjects(
          Transaction tx, ArrayList<Reference> personList, int numCalls, boolean assocMapType) {
      long timeStart = System.currentTimeMillis();
      
      tx.start(TransactionMode.READ_UPDATE);
      
      int personCount = 0;
      
      for (Reference callerRef : personList) 
      {
        personCount++;
        for (int phoneI = 0; phoneI < numCalls; phoneI++)
        {
          String phoneNumber = numberGen.generate(10);
          Instance call = objyAccess.createCall(phoneI, phoneNumber, callerRef);
          //System.out.println("Name: " + name + " >> phoneCall: " + phoneNumber);
          if (assocMapType) {
            objyAccess.addCallToPersonAssocMap(new Reference(call), callerRef);
          }
          else {
            objyAccess.addCallToPersonAssocSeg(new Reference(call), callerRef);
          }
        }
        if ((personCount%10)==0)
        {
          tx.commit();
          System.out.println("... processed " + personCount + " callers.");
          tx.start(TransactionMode.READ_UPDATE);
        }
      }

      tx.commit();
      double diff = (System.currentTimeMillis() - timeStart)/1000.0;
      
      if (assocMapType)
        System.out.println("... processTime for adding calls AssocMap: " + diff);
      else
        System.out.println("... processTime for adding calls AssocSeg: " + diff);
  }

  /**
   * 
   * @param assocMapType
   * @return 
   */
  private ArrayList<Reference> findPesonObjects(boolean assocMapType) {
    
    ArrayList<Reference> personList = new ArrayList<Reference>();
    
    String className = objyAccess.PersonAssocMapClassName;
    
    if (!assocMapType)
      className = objyAccess.PersonAssocSegClassName;
    
    String doString = "from " + className + " return *;";
    
    Statement doStatement = new Statement(LanguageRegistry.lookupLanguage("DO"),
					doString);

    // Add the identifier to the results projection.
    Policies policies = new Policies();
    policies.add("AddIdentifier.enable", new Variable(true));

    Variable results = doStatement.execute(policies);

    //System.out.println(results.getSpecification().getLogicalType().toString());
    if (results.getSpecification().getLogicalType() == LogicalType.SEQUENCE) {

      Iterator<Variable> resultItr = results.sequenceValue().iterator();
      Variable resultVar = null;
      LogicalType resultVarType = null;
      
      while (resultItr.hasNext()) {
		resultVar = resultItr.next();
        resultVarType = resultVar.getSpecification().getLogicalType();
        if (resultVarType.equals(LogicalType.INSTANCE)) {
          Instance instance = resultVar.instanceValue();
          personList.add(new Reference(instance));
        }
      }
    }
    
    System.out.print("found: " + personList.size());
    if (assocMapType)
      System.out.println(" person objects with assocMap.");
    else
      System.out.println(" person objects with assocSeg.");
    
    return personList;
  }

  /**
   * 
   * @param tx
   * @param personAssocMapList 
   */
  void readCallsFromPersonAssocMap(Transaction tx, ArrayList<Reference> personAssocMapList) {
    readCallsFromPersonObjects(tx, personAssocMapList, true /* assocMapType */);
  }

  /**
   * 
   * @param tx
   * @param personAssocSegList 
   */
  void readCallsFromPersonAssocSeg(Transaction tx, ArrayList<Reference> personAssocSegList) {
    readCallsFromPersonObjects(tx, personAssocSegList, false /* !assocMapType */);
  }

  /**
   * 
   * @param tx
   * @param personList
   * @param assocMapType 
   */
  private void readCallsFromPersonObjects(
          Transaction tx, ArrayList<Reference> personList, boolean assocMapType) {
      long timeStart = System.currentTimeMillis();
      
      tx.start(TransactionMode.READ_ONLY);
      
      int personCount = 0;
      int callCount = 0;
      for (Reference callerRef : personList) 
      {
        personCount++;
        if (assocMapType) {
          callCount += objyAccess.readAllPersonAssocMapCalls(callerRef);
        }
        else {
          callCount += objyAccess.readAllPersonAssocSegCalls(callerRef);
        }
      }

      tx.commit();
      double diff = (System.currentTimeMillis() - timeStart)/1000.0;
      
      if (assocMapType)
        System.out.println("... processTime for reading " + callCount + " calls AssocMap: " + diff);
      else
        System.out.println("... processTime for reading " + callCount + " calls AssocSeg: " + diff);
  }

}
