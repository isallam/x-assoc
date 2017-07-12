/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objy.se;

import com.objy.data.ClassBuilder;
import com.objy.data.DataSpecification;
import com.objy.data.LogicalType;
import com.objy.data.Reference;
import com.objy.data.Variable;
import com.objy.data.dataSpecificationBuilder.ListSpecificationBuilder;
import com.objy.data.dataSpecificationBuilder.MapSpecificationBuilder;
import com.objy.data.dataSpecificationBuilder.ReferenceSpecificationBuilder;
import com.objy.data.dataSpecificationBuilder.StringSpecificationBuilder;
import com.objy.data.schemaProvider.SchemaProvider;
import com.objy.db.ObjectivityException;
import com.objy.db.TransactionMode;
import com.objy.db.TransactionScope;
import java.util.Iterator;


/**
 *
 * @author ibrahim
 */
class ClassCache {

  String name;
  com.objy.data.Class classRef;
  com.objy.data.Variable value = new com.objy.data.Variable();
  com.objy.data.Variable stringValue = new com.objy.data.Variable();
};

class AssocMapClass extends ClassCache {

  com.objy.data.Attribute filterAttr;
  com.objy.data.Attribute collectionAttr;
};

class PersonAssocMapClass extends ClassCache {

  com.objy.data.Attribute idAttr;
  com.objy.data.Attribute nameAttr;
  com.objy.data.Attribute callsAttr;
};

class PersonAssocSegClass extends ClassCache {

  com.objy.data.Attribute idAttr;
  com.objy.data.Attribute nameAttr;
  com.objy.data.Attribute callsAttr;
};

class CallClass extends ClassCache {

  com.objy.data.Attribute idAttr;
  com.objy.data.Attribute phoneNumberAttr;
  com.objy.data.Attribute callerAttr;
};


public class ObjyAccess {

  // class names
  final String AssocMapClassName = "AssocMap";
  final String PersonAssocMapClassName = "PersonAssocMap";
  final String PersonAssocSegClassName = "PersonAssocSeg";
  final String CallClassName = "Call";

  final String AssocMapFilterAttr = "filter";
  final String AssocMapCollectionAttr = "collection";

  final String PersonIdAttr = "id";
  final String PersonNameAttr = "name";
  final String PersonCallsAttr = "calls";

  final String CallIdAttr = "id";
  final String CallPhoneNumberAttr = "phone_number";
  final String CallCallerAttr = "caller";

  // more caching 
  AssocMapClass assocMapClass             = new AssocMapClass();
  PersonAssocMapClass personAssocMapClass = new PersonAssocMapClass();
  PersonAssocSegClass personAssocSegClass = new PersonAssocSegClass();
  CallClass callClass                     = new CallClass();

  /**
   * createSchema
   *
   * @return
   */
  boolean createSchema() {
    // -----------------------------------------------------------
    // Some needed specs for various references and collections...
    // -----------------------------------------------------------

    try (TransactionScope tx = new TransactionScope(TransactionMode.READ_UPDATE)) {
      // transaction Reference 
      DataSpecification elementRefSpec
              = new ReferenceSpecificationBuilder()
                      .setReferencedClass("ooObj")
                      .build();

      // AssocMap Map                 
      DataSpecification mapSpec
              = new MapSpecificationBuilder()
                      .setElementSpecification(elementRefSpec)
                      .setKeySpecification(new StringSpecificationBuilder().build())
                      .setCollectionName("ooMap")
                      .build();

      // AssocMap reference
      DataSpecification assocMapRefSpec
              = new ReferenceSpecificationBuilder()
                      .setReferencedClass(AssocMapClassName)
                      .build();

//      // PersonAssocMap reference
//      DataSpecification personAssocMapRefSpec
//              = new ReferenceSpecificationBuilder()
//                      .setReferencedClass(PersonAssocMapClassName)
//                      .build();
//
      // Embedded string spec (currently not used)
//    DataSpecification stringSpec
//            = new StringSpecifictaionBuilder()
//                    .setEncoding(com.objy.data.StringEncoding.Utf8)
//                    .setStorage(com.objy.data.StringStorage.Fixed)
//                    .setFixedLength(66)
//                    .build();
// -------------------
      // AssocMap Class
      // -------------------
      com.objy.data.Class assocMapClassRep = new ClassBuilder(AssocMapClassName)
              .setSuperclass("ooObj")
              .addAttribute(LogicalType.INTEGER, AssocMapFilterAttr)
              .addAttribute(AssocMapCollectionAttr, mapSpec)
              .build();

      // AssocSeg spec
      DataSpecification assocSegSpec
              = new ListSpecificationBuilder()
                      .setElementSpecification(elementRefSpec)
                      .setCollectionName("SegmentedArray")
                      .build();

      // -------------------
      // PersonAssocMap Class
      // -------------------
      com.objy.data.Class personAssocMapClassRep = new ClassBuilder(PersonAssocMapClassName)
              .setSuperclass("ooObj")
              .addAttribute(LogicalType.INTEGER, PersonIdAttr)
              .addAttribute(LogicalType.STRING, PersonNameAttr)
              .addAttribute(PersonCallsAttr, assocMapRefSpec)
              .build();

      // -------------------
      // PersonAssocSeg Class
      // -------------------
      com.objy.data.Class personAssocSegClassRep = new ClassBuilder(PersonAssocSegClassName)
              .setSuperclass("ooObj")
              .addAttribute(LogicalType.INTEGER, PersonIdAttr)
              .addAttribute(LogicalType.STRING, PersonNameAttr)
              .addAttribute(PersonCallsAttr, assocSegSpec)
              .build();

      // -------------------
      // Call Class
      // -------------------
      com.objy.data.Class callClassRep = new ClassBuilder(CallClassName)
              .setSuperclass("ooObj")
              .addAttribute(LogicalType.INTEGER, CallIdAttr)
              .addAttribute(LogicalType.STRING, CallPhoneNumberAttr)
              .addAttribute(CallCallerAttr, elementRefSpec)
              .build();

      SchemaProvider provider = SchemaProvider.getDefaultPersistentProvider();
      provider.represent(assocMapClassRep);
      provider.represent(personAssocMapClassRep);
      provider.represent(personAssocSegClassRep);
      provider.represent(callClassRep);

      tx.complete();

    } catch (ObjectivityException e) {
      e.printStackTrace();
    }
    return true;
  }

  /**
   *
   * @return
   */
  boolean initAccessMode() {
    // cache classes.
    assocMapClass.classRef = com.objy.data.Class.lookupClass(AssocMapClassName);
    if (assocMapClass.classRef == null)
      return false; 
    assocMapClass.filterAttr = assocMapClass.classRef.lookupAttribute(AssocMapFilterAttr);
    assocMapClass.collectionAttr = assocMapClass.classRef.lookupAttribute(AssocMapCollectionAttr);

    personAssocMapClass.classRef = com.objy.data.Class.lookupClass(PersonAssocMapClassName);
    if (personAssocMapClass.classRef == null)
      return false;
    personAssocMapClass.idAttr = personAssocMapClass.classRef.lookupAttribute(PersonIdAttr);
    personAssocMapClass.nameAttr = personAssocMapClass.classRef.lookupAttribute(PersonNameAttr);
    personAssocMapClass.callsAttr = personAssocMapClass.classRef.lookupAttribute(PersonCallsAttr);

    personAssocSegClass.classRef = com.objy.data.Class.lookupClass(PersonAssocSegClassName);
    if (personAssocSegClass.classRef == null)
      return false;
    personAssocSegClass.idAttr = personAssocSegClass.classRef.lookupAttribute(PersonIdAttr);
    personAssocSegClass.nameAttr = personAssocSegClass.classRef.lookupAttribute(PersonNameAttr);
    personAssocSegClass.callsAttr = personAssocSegClass.classRef.lookupAttribute(PersonCallsAttr);

    callClass.classRef = com.objy.data.Class.lookupClass(CallClassName);
    if (callClass.classRef == null)
      return false;
    callClass.idAttr = callClass.classRef.lookupAttribute(CallIdAttr);
    callClass.phoneNumberAttr = callClass.classRef.lookupAttribute(CallPhoneNumberAttr);
    callClass.callerAttr = callClass.classRef.lookupAttribute(CallCallerAttr);

    return true;
  }

  /**
   * 
   * @param id
   * @param name
   * @return 
   */
  com.objy.data.Instance createPersonAssocMap(
          int id, String name) {
    
    com.objy.data.Instance instance = 
            com.objy.data.Instance.createPersistent(personAssocMapClass.classRef);

    instance.getAttributeValue(personAssocMapClass.idAttr, personAssocMapClass.value);
    personAssocMapClass.value.set(id);

    //blockClass.stringValue.set(hash);
    instance.getAttributeValue(personAssocMapClass.nameAttr, personAssocMapClass.value);
    personAssocMapClass.value.set(name);

    com.objy.data.Instance assocMapInstance = 
            com.objy.data.Instance.createPersistent(assocMapClass.classRef);
    
    instance.getAttributeValue(personAssocMapClass.callsAttr, personAssocMapClass.value);
    personAssocMapClass.value.set(new Reference(assocMapInstance));
  
    return instance;
  }

  /**
   * 
   * @param id
   * @param name
   * @return 
   */
  com.objy.data.Instance createPersonAssocSeg(
          int id, String name) {
    
    com.objy.data.Instance instance = 
            com.objy.data.Instance.createPersistent(personAssocSegClass.classRef);

    instance.getAttributeValue(personAssocSegClass.idAttr, personAssocSegClass.value);
    personAssocSegClass.value.set(id);

    //blockClass.stringValue.set(hash);
    instance.getAttributeValue(personAssocSegClass.nameAttr, personAssocSegClass.value);
    personAssocSegClass.value.set(name);
  
    return instance;
  }

  /**
   *
   * @param id
   * @param hash
   * @return
   */
  com.objy.data.Instance createCall(
          int id, String phoneNumber, Reference caller) {
    
    com.objy.data.Instance instance = 
            com.objy.data.Instance.createPersistent(callClass.classRef);

    instance.getAttributeValue(callClass.idAttr, callClass.value);
    callClass.value.set(id);

    //transactionClass.stringValue.set(hash);
    //instance.getAttributeValue("hash").set<objydata::Utf8String>(value);
    instance.getAttributeValue(callClass.phoneNumberAttr, callClass.value);
    callClass.value.set(phoneNumber);

    instance.getAttributeValue(callClass.callerAttr, callClass.value);
    callClass.value.set(caller);
  
    return instance;
  }

  /**
   * 
   * @param call
   * @param person
   * @return 
   */
  boolean addCallToPersonAssocMap(com.objy.data.Reference call, 
          com.objy.data.Reference person) {

    person.getReferencedObject().getAttributeValue(
            personAssocMapClass.callsAttr, personAssocMapClass.value);
    
    Reference assocMap = personAssocMapClass.value.referenceValue();
    assocMap.getReferencedObject().getAttributeValue(
            assocMapClass.collectionAttr, assocMapClass.value);

    // set the key and value in the map from the call object.... 
    Variable key = new Variable(call.getObjectId().toString());
    //System.out.println("call.getObjectId(): " + call.getObjectId().toString());
    if (!assocMapClass.value.mapValue().containsKey(key))
    {
      Variable value = new Variable(call);
      assocMapClass.value.mapValue().put(key, value);
    }
    return true;
  }

  /**
   * 
   * @param call
   * @param person
   * @return 
   */
  boolean addCallToPersonAssocSeg(com.objy.data.Reference call, 
          com.objy.data.Reference person) {

    person.getReferencedObject().getAttributeValue(
            personAssocSegClass.callsAttr, personAssocSegClass.value);

    // check to see if we have such OID in the list...
    com.objy.data.List list = personAssocSegClass.value.listValue();
     
    if (!doesListContainReference(list, call))
    {
      list.add(new Variable(call));
    }
    return true;
  }
  
  /**
   * 
   * @param list
   * @param value
   * @return 
   */
  private boolean doesListContainReference(com.objy.data.List list, Reference value) {
    Variable var = new Variable();
    long valueOid = value.getObjectId().asLong();
    for (int i = 0; i < list.size(); i++)
    {
      list.get(i, var);
      long refOid = var.referenceValue().getObjectId().asLong();
      if (refOid == valueOid)
      {
        return true;
      }
    }
    return false;
  }

  /**
   * 
   * @param caller
   * @return count of calls
   */
  public int readAllPersonAssocMapCalls(Reference caller) {
    caller.getReferencedObject().getAttributeValue(
            personAssocMapClass.callsAttr, personAssocMapClass.value);
    
    Reference assocMap = personAssocMapClass.value.referenceValue();
    assocMap.getReferencedObject().getAttributeValue(
            assocMapClass.collectionAttr, assocMapClass.value);

    Iterator<Variable> elemItr = assocMapClass.value.mapValue().elements().iterator();
    Variable callVar = new Variable();
    int callCount = 0;
    while (elemItr.hasNext())
    {
      callVar = elemItr.next();
      callVar.referenceValue();
      String str = callVar.referenceValue().getObjectId().toString();
      callCount++;
    }
    
    return callCount;
  }

  /**
   * 
   * @param caller
   * @return count of calls
   */
  public int readAllPersonAssocSegCalls(Reference caller) {
    caller.getReferencedObject().getAttributeValue(
            personAssocSegClass.callsAttr, personAssocSegClass.value);

    // check to see if we have such OID in the list...
    com.objy.data.List list = personAssocSegClass.value.listValue();
    
    Variable callVar = new Variable();
    int callCount = 0;
    for (int i = 0; i < list.size(); i++)
    {
      list.get(i, callVar);
      callVar.referenceValue();
      String str = callVar.referenceValue().getObjectId().toString();
      callCount++;
     }
    
    return callCount;
  }
}
