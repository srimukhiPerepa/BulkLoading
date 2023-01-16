define([], function() {
  'use strict';

  var PageModule = function PageModule() {};

  //the function checks if input value is a number
  PageModule.prototype.numValidator = function() {
    return [{
      type: 'regExp',
      options: {
        pattern: "[0-9]*",
        hint: "enter a valid number",
        messageDetail: "Not a valid number format"
      }
    }];
  }; 

PageModule.prototype.waitForNextCall = function(){
          var start = new Date().getTime();
          var end=0;
          while( (end-start) < 2000){
            end = new Date().getTime();
          }
        console.log("refreshing");

  }; 
 


  PageModule.prototype.eatNonNumbers = function() {
    // Only allow ".0123456789" (and non-display characters)
    let char = event.key;
    console.log("LOG100: ",char);
    let replacedValue = char.replace(/[^0-9\.]/g, "");
    if (char !== replacedValue) {
      console.log(" LOG100: Returning true");
       return true;
    }
    else {
      console.log(" LOG100: Returning false");
      return false;
    };
  }; 

  PageModule.prototype.checkNumberFormat = function(inputValue)
  {
    console.log('log 300: '+ inputValue);
    console.log('log 300: '+ isNaN);
    if (isNaN(inputValue)) return false;
    else return true;
  };

  //the function is used to rename the button "Submit for Approval" incase the source page is View Details and conditional request status
  PageModule.prototype.renameButton = function(sourcePage, requestStatus)
  {
    if (sourcePage == "ViewDetails" && (requestStatus == "More Information Requested")) return "Re-Submit for Approval";
    else return "Submit for Approval";
  };

   PageModule.prototype.setTitle = function(sourcePage, requestStatus)
  {
    if (sourcePage == "ViewDetails" && (requestStatus == "More Information Requested")) return "Resubmit request for approval";
    else return "Submit request for Approval";
  };

  PageModule.prototype.enableSubmitButton = function(sourcePage, requestStatus)
  {
    if ((sourcePage == "ViewDetails" && requestStatus == "More Information Requested")||(sourcePage == "" && requestStatus == ""))
      return true;
    else 
      return false;
  };

  PageModule.prototype.openDialogFunc = function(openDialogId){
    document.querySelector(openDialogId).open();
  };

  PageModule.prototype.closeDialogFunc = function(closeDialogId){
    document.querySelector(closeDialogId).close();
  };    

  PageModule.prototype.deleteBusinessClassification = function(key, action, businessClassificationArray){
    console.log('Log: businessClassificationArray '+businessClassificationArray);
    console.log('Log: key '+key);
    console.log('Log: action '+action);
    for(var i=0;i<businessClassificationArray.length();i++){
      if(key==businessClassificationArray[i].Classification && action==businessClassificationArray[i].ActionRequested){
        for(var j=i;j<businessClassificationArray.length()-1;j++){
          businessClassificationArray[j] = businessClassificationArray[j+1];
        };
        break;
      };
    };
    return businessClassificationArray;
  };     

PageModule.prototype.returnCurrentDate = function() {
        var today = new Date();
        var date = today.toISOString().slice(0,19)+'Z'
       /* var dd = String(today.getDate()).padStart(2, '0');
        var mm = String(today.getMonth() + 1).padStart(2, '0'); //January is 0!
        var yyyy = today.getFullYear();
        var hr = String(today.getHours()).padStart(2, '0');
        var min = String(today.getMinutes()).padStart(2, '0');
        var sec = String(today.getSeconds()).padStart(2, '0');

        var date = String(yyyy + '-' + mm + '-' + dd + 'T' + hr + ':' + min + ':' + sec + 'Z');
 
*/
        return date;
    };

PageModule.prototype.returnSupplierCreationDate = function(supplierCreationDate) {
  
        console.log("supplierCreationDate"+supplierCreationDate);
        supplierCreationDate=supplierCreationDate.substring(0,supplierCreationDate.length-1).replace("T"," ");
        supplierCreationDate=supplierCreationDate.substring(0,supplierCreationDate.length-5);
        console.log("supplierCreationDate"+supplierCreationDate);

        return supplierCreationDate;
    }; 

PageModule.prototype.validateExpirationDate = function(expirationDate) {
        if(!expirationDate){
          return true;
        };  
        var today = new Date();
        var expDate = new Date(expirationDate);
        today.setHours(0, 0, 0, 0);
        expDate.setHours(0, 0, 0, 0);        
        if(expDate > today){
          return true;
        }
        else{
          return false;
        };
    };

PageModule.prototype.validateInactiveDate = function(inactiveDate) {
        if(!inactiveDate){
          return true;
        };
        var today = new Date();
        var inactvDate = new Date(inactiveDate);
        today.setHours(0, 0, 0, 0);
        inactvDate.setHours(0, 0, 0, 0);
        console.log('inactiveDate: '+inactvDate);
        console.log('today: '+today);
        if(inactvDate >= today){
          return true;
        }
        else{
          return false;
        };
    };  

PageModule.prototype.validateContactInactiveDate = function(inactiveDate) {
        if(!inactiveDate){
          return true;
        };
        var today = new Date();
        var inactvDate = new Date(inactiveDate);
        today.setHours(0, 0, 0, 0);
        inactvDate.setHours(0, 0, 0, 0);
        console.log('inactiveDate: '+inactvDate);
        console.log('today: '+today);
        if(inactvDate <= today){
          return true;
        }
        else{
          return false;
        };
    };     

PageModule.prototype.validateEmail = function(emailId) { 
        if (/^\w+([\.-]?\w+)*@\w+([\.-]?\w+)*(\.\w{2,3})+$/.test(emailId)){
          return true;
        }
        else if (!emailId){
          return true;
        }
        else{
          return false;
        };
    };

PageModule.prototype.validateTaxRegistrationNumber = function(taxRegistrationNumber,countryCode) { 
        console.log("taxRegistrationNumber.substring(1,2)"+taxRegistrationNumber.substring(0,2));
        console.log("countryCode: "+countryCode);
        if((taxRegistrationNumber.substring(0,2)).toUpperCase()==countryCode){
          return true;
        }
        else{
          return false;
        };
    };   

PageModule.prototype.isUndefined = function(value) { 
  if(value){
    console.log("returning true");
    return true;
  }
  else
  {
    console.log("returning false");
    return false;
  };
    
};  

PageModule.prototype.consolelogger = function(inputV){
  console.log("Vault Secret "+ inputV);
};

PageModule.prototype.getUniqueBanks = function(banksArray){
    var newBanksArray = [...new Map(banksArray.map(item => [item['BankName'], item])).values()];
    return newBanksArray;
};

PageModule.prototype.getUniqueBankBranches = function(bankBranchesArray){
    var newBankBranchesArray = [...new Map(bankBranchesArray.map(item => [item['BankBranchName'], item])).values()];
    return newBankBranchesArray;
};

PageModule.prototype.enableOkButton = function(requestStatus){
    if (requestStatus == "Approved" || requestStatus == "Request for Approval" || requestStatus == "Rejected" || requestStatus == "Approved - Sync Errors")
    return false;
    else return true;
};


PageModule.prototype.disableTip = function(requestStatus,sourcePage){
    if (requestStatus == "Approved" || requestStatus == "More Information Requested" || requestStatus == "Rejected" || requestStatus == "Approved - Sync Errors" || sourcePage == "Admin" || sourcePage =="ViewDetails")
    return false;
    else return true;
};


PageModule.prototype.isAddressFormatValid = function(fieldsToBeValidated,validationsArray){
  console.log("fieldsToBeValidated: "+fieldsToBeValidated);
  console.log("validationsArray: "+validationsArray.COUNTRY);
  var fieldName;
  const myArray = fieldsToBeValidated.split("-");
  for(var i=0;i<myArray.length;i++){
    fieldName = myArray[i];
    if(fieldName=="COUNTRY"){
      validationsArray.COUNTRY = "Mandatory";
    }
    else if(fieldName=="ADDRESS1"){
      validationsArray.ADDRESS1 = "Mandatory";
    }
    else if(fieldName=="POSTAL_CODE"){
      validationsArray.POSTAL_CODE = "Mandatory";
    }
    else if(fieldName=="CITY"){
      validationsArray.CITY = "Mandatory";
    }
    else if(fieldName=="STATE"){
      validationsArray.STATE = "Mandatory";
    }
    else if(fieldName=="COUNTY"){
      validationsArray.COUNTY = "Mandatory";
    }
    else if(fieldName=="PROVINCE"){
      validationsArray.PROVINCE = "Mandatory";
    };
    console.log("Log100: "+validationsArray);
  };
  return validationsArray;
};

  return PageModule;
});