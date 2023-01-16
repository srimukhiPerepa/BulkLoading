/*
  Copyright (c) 2018, Oracle and/or its affiliates.
  The Universal Permissive License (UPL), Version 1.0
*/
define([], function(ArrayDataProvider) {
  'use strict';

  var PageModule = function PageModule() {};

// Function to create Supplier Type search criteria for REST API
  PageModule.prototype.createSupplierTypeSearchCriteria = function(supplierTypesArray, userEnteredValue){

    console.log("LOG5:supplierTypesArray ",supplierTypesArray);
    console.log("LOG5:userEnteredValue ",userEnteredValue);

    var flag = 0;
    var supplierTypeSearchCriteria;

    //Check user entered supplier type value against supplier typesarray
    for(var i=0;i<supplierTypesArray.length;i++){
      if(supplierTypesArray[i].LookupCode == userEnteredValue){
        flag = 1;
        break;
      };
    };

    console.log("LOG5:flag ",flag);

    //User has selected supplier type from LOV
    if(flag==1){
      supplierTypeSearchCriteria = "AND (SupplierType='"+userEnteredValue+"')";
      return supplierTypeSearchCriteria;
    }
    //User did not select any supplier type from LOV
    else if(userEnteredValue == '' || userEnteredValue == 'undefined' || !(userEnteredValue)){
      for(i=0;i<supplierTypesArray.length;i++){
        if(i==0){
          supplierTypeSearchCriteria = "AND (SupplierType='"+supplierTypesArray[i].LookupCode+"'";
        }
        else{
          supplierTypeSearchCriteria = supplierTypeSearchCriteria+" OR SupplierType='"+supplierTypesArray[i].LookupCode+"'";
        };
      };
      supplierTypeSearchCriteria=supplierTypeSearchCriteria+")";
      return supplierTypeSearchCriteria;
    }
    // user enters supplier type other than from LOV
    else if(flag==0){
      return "AND (SupplierType='undefined')";
    };
  };

  PageModule.prototype.validateSearchStrings = function(arrayOfSearchStrings) {

    for (const key in arrayOfSearchStrings) {
      console.log("LOG1: Key:Value: ",`${key} : ${arrayOfSearchStrings[key]}`);

      if(`${key}` !== 'supplierType' && `${arrayOfSearchStrings[key]}` && `${arrayOfSearchStrings[key]}` != 'undefined'){

        var str = `${arrayOfSearchStrings[key]}`;

        if(`${key}`=='supplierName' || `${key}`=='alternateName' || `${key}`=='parentSupplier' || `${key}`=='taxRegNumber' || `${key}`=='taxpayerID' || `${key}`=='supplierNumber'){
          var flag=0;
          for(var i=0;i<str.length;i++){
            var code = str.charCodeAt(i);
            if(code!=37 && code!=42){  //ASCII value of asterisk and percent sign
              flag=flag+1;            
              if(flag==3){
                console.log('LOG5: flag ',flag);
                break;
              };
            }
            else{
              flag=0;
            };
          };
          if(flag<3){
            return false;
          };            
       }
/*        else if(`${key}`=='supplierNumber'){              
          if(`${arrayOfSearchStrings[key]}`){
            if(`${arrayOfSearchStrings[key]}`.indexOf('*') > -1 || `${arrayOfSearchStrings[key]}`.indexOf('%') > -1){
              return false;
            };
          }; 
        };*/
      };  
    };    
    console.log("Log1: Return true");
    return true; 
  };

PageModule.prototype.waitForNextCall = function(){
          var start = new Date().getTime();
          var end=0;
          while( (end-start) < 2000){
            end = new Date().getTime();
          }
        console.log("refreshing");

  }; 

  PageModule.prototype.openDialogFunc = function(){
    console.log('Inside open popup');
    document.querySelector('#dialog').open();
  };

  PageModule.prototype.closeDialogFunc = function(){
    console.log('Inside close popup');
    document.querySelector('#dialog').close();
  };    

  return PageModule;
});