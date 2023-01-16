define([], function() {
  'use strict';

  var PageModule = function PageModule() {};
  
  //function for calculating Age column from the date of Submission to Current date
  PageModule.prototype.calculateAge = function(submissionDate) {
         /* var today = new Date();
        var date1 = today.getTime();
        var subDate = new Date(submissionDate);
        var date2 = subDate.getTime();
        var age = (date1 - date2)/(1000 * 60 * 60 * 24);
        var hours = 0;
       /* var days = Math.round(age);
        if(days == 1 || days == 0) 
        return days + ' day';
        else
        return days +' days';*/

        var one_day = 24*60*60*1000;              // total milliseconds in one day
         var today = new Date();
         
         /*today.setHours(today.getHours()+5);*/
        var date1 = today.getTime();
        //var date1 = new Date(today.getTime() - (9*60*60*1000) - (30*60*100));
        var subDate = new Date(submissionDate);
        var date2 = subDate.getTime();                 

        var time_diff = Math.abs(date1 - date2);  //time diff in ms  
        var days = Math.floor(time_diff / one_day);            // no of days

        var remaining_time = time_diff - (days*one_day);      // remaining ms  

        var hours = Math.floor(remaining_time/(60*60*1000));
        if(days == 1 || days == 0) 
        return days + ' day ' + hours +' hours';
        else
        return days +' days ' + hours +' hours';


    };

PageModule.prototype.waitForNextCall = function(){
          var start = new Date().getTime();
          var end=0;
          while( (end-start) < 2000){
            end = new Date().getTime();
          }
        console.log("refreshing");

  }; 

  PageModule.prototype.testFunc = function() {
// Start changes by Prajakta        

const timezone = Intl.DateTimeFormat().resolvedOptions().timeZone;
console.log('timezone'+timezone);

 var date = new Date();
const utc = date.toISOString().slice(0,19)+'Z';
console.log('ISO format '+utc);

//End changes by Prajakta
  };

  //function to enable the hyperlink of Request Id for specific request status
  PageModule.prototype.enableHyperlink = function(status)
  {
    if( status == "Request for Approval" || status == "Approved" || status == "Approved - Sync Errors" || status == "Rejected" || status == "Cancelled" || status == "More Information Requested")
    return "true";
    else
    return "false";
  };

  //dialog open - Processing please wait
  PageModule.prototype.openDialogFunc = function(){
    console.log('Inside open popup');
    document.querySelector('#dialog1').open();
  };

  //dialog close
  PageModule.prototype.closeDialogFunc = function(){
    console.log('Inside close popup');
    document.querySelector('#dialog1').close();
  }; 

  return PageModule;
});