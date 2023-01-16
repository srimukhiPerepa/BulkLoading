define([], function() {
  'use strict';

  var PageModule = function PageModule() {};

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
        var date1 = today.getTime();
        //var date1 = new Date(today.getTime()- (9*60*60*1000)- (30*60*100));
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

  PageModule.prototype.getMonthYear = function(){
    var today = new Date();
    var year = today.getFullYear();
    var mon = today.getMonth()+1;
    var month = null;
    if (mon == 1) month = 'January';
    else if (mon == 2) month = 'Febraury';
    else if (mon == 3) month = 'March';
    else if (mon == 4) month = 'April';
    else if (mon == 5) month = 'May';
    else if (mon == 6) month = 'June';
    else if (mon == 7) month = 'July';
    else if (mon == 8) month = 'August';
    else if (mon == 9) month = 'September';
    else if (mon == 10) month = 'October';
    else if (mon == 11) month = 'November';
    else if (mon == 12) month = 'December';

    return month + ' '+ year;
  };

  PageModule.prototype.waitForNextCall = function(){
          var start = new Date().getTime();
          var end=0;
          while( (end-start) < 2000){
            end = new Date().getTime();
          }
        console.log("refreshing");

  }; 

    PageModule.prototype.getMonthYearForErroredRequests = function(){
    var today = new Date();
    var year = today.getFullYear();
    var mon = today.getMonth()+1;
    var month = null;
    var prevMonth = null;
    if (mon == 1) {prevMonth = 'December'; month = 'January';}
    else if (mon == 2) { prevMonth = 'January'; month = 'Febraury';}
    else if (mon == 3) {prevMonth = 'Febraury'; month = 'March';}
    else if (mon == 4) {prevMonth = 'March'; month = 'April';}
    else if (mon == 5) {prevMonth = 'April'; month = 'May';}
    else if (mon == 6) {prevMonth = 'May'; month = 'June';}
    else if (mon == 7) {prevMonth = 'June'; month = 'July';}
    else if (mon == 8) {prevMonth = 'July'; month = 'August';}
    else if (mon == 9) {prevMonth = 'August'; month = 'September';}
    else if (mon == 10) {prevMonth = 'September'; month = 'October';}
    else if (mon == 11) {prevMonth = 'October'; month = 'November';}
    else if (mon == 12) {prevMonth = 'November'; month = 'December';}

    if(prevMonth == 'December' && month == 'January'){ return 'December '+(year-1) +' - '+'January '+year;}
    else {return prevMonth + ' - ' + month + ' '+ year;}
  };

  PageModule.prototype.openDialogFunc = function(openDialogId){
    document.querySelector(openDialogId).open();
  };

  PageModule.prototype.erroredRequestHyperlinkClicked = function(hyperlinkClickedVar){
    if (hyperlinkClickedVar == true) {return "Requests ended in Sync Error: ";}
    else return "Requests pending for action: ";
  };


  return PageModule;
});