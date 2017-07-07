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
import com.objy.data.Storage;
import com.objy.data.Variable;
import com.objy.data.dataSpecificationBuilder.IntegerSpecificationBuilder;
import com.objy.data.dataSpecificationBuilder.ListSpecificationBuilder;
import com.objy.data.dataSpecificationBuilder.MapSpecificationBuilder;
import com.objy.data.dataSpecificationBuilder.ReferenceSpecificationBuilder;
import com.objy.data.dataSpecificationBuilder.StringSpecificationBuilder;
import com.objy.data.schemaProvider.SchemaProvider;
import com.objy.db.ObjectId;
import com.objy.db.ObjectivityException;
import com.objy.db.TransactionMode;
import com.objy.db.TransactionScope;
import java.util.Date;

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

class PersonClass extends ClassCache {

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
  final String PersonClassName = "Person";
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
  AssocMapClass assocMapClass  = new AssocMapClass();
  PersonClass personClass      = new PersonClass();
  CallClass callClass          = new CallClass();

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
      DataSpecification mapElementRefSpec
              = new ReferenceSpecificationBuilder()
                      .setReferencedClass("ooObj")
                      .build();

      // AssocMap Map                 
      DataSpecification mapSpec
              = new MapSpecificationBuilder()
                      .setElementSpecification(mapElementRefSpec)
                      .setKeySpecification(new StringSpecificationBuilder().build())
                      .setCollectionName("ooMap")
                      .build();

      // AssocMap reference
      DataSpecification assocMapRefSpec
              = new ReferenceSpecificationBuilder()
                      .setReferencedClass(AssocMapClassName)
                      .build();

      // Person reference
      DataSpecification personRefSpec
              = new ReferenceSpecificationBuilder()
                      .setReferencedClass(PersonClassName)
                      .build();

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
      // -------------------
      // Person Class
      // -------------------
      com.objy.data.Class personClassRep = new ClassBuilder(PersonClassName)
              .setSuperclass("ooObj")
              .addAttribute(LogicalType.INTEGER, PersonIdAttr)
              //.addAttribute(com.objy.data.LogicalType.DateTime, "time")
              .addAttribute(LogicalType.STRING, PersonNameAttr)
              .addAttribute(PersonCallsAttr, assocMapRefSpec)
              .build();

      // -------------------
      // Call Class
      // -------------------
      com.objy.data.Class callClassRep = new ClassBuilder(CallClassName)
              .setSuperclass("ooObj")
              .addAttribute(LogicalType.INTEGER, CallIdAttr)
              .addAttribute(LogicalType.STRING, CallPhoneNumberAttr)
              .addAttribute(CallCallerAttr, personRefSpec)
              .build();

      SchemaProvider provider = SchemaProvider.getDefaultPersistentProvider();
      provider.represent(assocMapClassRep);
      provider.represent(personClassRep);
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
  boolean setupCache() {
    // cache classes.
    assocMapClass.classRef = com.objy.data.Class.lookupClass(AssocMapClassName);
    assocMapClass.filterAttr = assocMapClass.classRef.lookupAttribute(AssocMapFilterAttr);
    assocMapClass.collectionAttr = assocMapClass.classRef.lookupAttribute(AssocMapCollectionAttr);

    personClass.classRef = com.objy.data.Class.lookupClass(PersonClassName);
    personClass.idAttr = personClass.classRef.lookupAttribute(PersonIdAttr);
    personClass.nameAttr = personClass.classRef.lookupAttribute(PersonNameAttr);
    personClass.callsAttr = personClass.classRef.lookupAttribute(PersonCallsAttr);

    callClass.classRef = com.objy.data.Class.lookupClass(CallClassName);
    callClass.idAttr = callClass.classRef.lookupAttribute(CallIdAttr);
    callClass.phoneNumberAttr = callClass.classRef.lookupAttribute(CallPhoneNumberAttr);
    callClass.callerAttr = callClass.classRef.lookupAttribute(CallCallerAttr);

    return true;
  }

  /**
   *
   * @param id
   * @param version
   * @param prevBlockHash
   * @param blockMerkleRoot
   * @param blkTime
   * @param hash
   * @param prevBlock
   * @return
   */
  com.objy.data.Instance createPerson(
          int id, String name) {
    
    com.objy.data.Instance instance = 
            com.objy.data.Instance.createPersistent(personClass.classRef);

    instance.getAttributeValue(personClass.idAttr, personClass.value);
    personClass.value.set(id);

    //blockClass.stringValue.set(hash);
    instance.getAttributeValue(personClass.nameAttr, personClass.value);
    personClass.value.set(name);

    com.objy.data.Instance assocMapInstance = 
            com.objy.data.Instance.createPersistent(assocMapClass.classRef);
    
    instance.getAttributeValue(personClass.callsAttr, personClass.value);
    personClass.value.set(new Reference(assocMapInstance));
  
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


  boolean addCallToPerson(com.objy.data.Reference call, 
          com.objy.data.Reference person) {

    person.getReferencedObject().getAttributeValue(
            personClass.callsAttr, personClass.value);
    
    Reference assocMap = personClass.value.referenceValue();
    assocMap.getReferencedObject().getAttributeValue(
            assocMapClass.collectionAttr, assocMapClass.value);

    // set the key and value in the map from the call object.... 
    // TBD...
    //assocMapClass.value.mapValue().put(key, value);
    
    return true;
  }

}
