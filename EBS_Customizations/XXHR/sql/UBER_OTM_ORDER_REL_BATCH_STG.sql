WHENEVER SQLERROR CONTINUE;
REM +===================================================================================+
REM +                      Uber Technologies Inc.                                       +
REM +===================================================================================+
REM |                                                                                   |
REM |Author: Satya Padala                                                               |
REM |Initial Build Date:                                                                |
REM |Source File Name: UBER_OTM_ORDER_REL_BATCH_STG.sql .                               |
REM |                                                                                   |
REM |Object Name:                                                                       |
REM |Description: This is the batch table that shall have data from the allocations     |
REM |              screen.                                                              |
REM |                                                                                   |
REM |Dependencies:                                                                      |
REM |                                                                                   |
REM |Usage:                                                                             |
REM |                                                                                   |
REM |Parameters   :  <Required Parameters>                                              |
REM |         <Optional Parameters>                                                     |
REM |         <Return Codes – if any>                                                   |
REM |                                                                                   |
REM |                                                                                   |
REM |Modification History:                                                              |
REM |===============                                                                    |
REM |Version       Date          Author                  Remarks                        |
REM |=========   =============  =========               =============================   |
REM |1.0         20-01-2020    Satya Padala(satyap@)      Initial draft version         |
REM +===================================================================================+

CREATE TABLE XXUBER.UBER_OTM_ORDER_REL_BATCH_STG (
    SOURCE             VARCHAR2(150), 
    BATCH_ID           NUMBER, 
    RECORD_STATUS      VARCHAR2(100), 
    ERROR_MESSAGE      VARCHAR2(2000), 
    ATTRIBUTE1         VARCHAR2(150), 
    ATTRIBUTE2         VARCHAR2(150), 
    ATTRIBUTE3         VARCHAR2(150), 
    ATTRIBUTE4         VARCHAR2(150), 
    ATTRIBUTE5         VARCHAR2(150), 
    ATTRIBUTE6         VARCHAR2(150), 
    ATTRIBUTE7         VARCHAR2(150), 
    ATTRIBUTE8         VARCHAR2(150), 
    ATTRIBUTE9         VARCHAR2(150), 
    ATTRIBUTE10        VARCHAR2(150), 
    ATTRIBUTE11        VARCHAR2(150), 
    ATTRIBUTE12        VARCHAR2(150), 
    ATTRIBUTE13        VARCHAR2(150), 
    ATTRIBUTE14        VARCHAR2(150), 
    ATTRIBUTE15        VARCHAR2(150), 
    ATTRIBUTE16        VARCHAR2(150), 
    ATTRIBUTE17        VARCHAR2(150), 
    ATTRIBUTE18        VARCHAR2(150), 
    ATTRIBUTE19        VARCHAR2(150), 
    ATTRIBUTE20        VARCHAR2(150), 
    CREATION_DATE      DATE, 
    CREATED_BY         NUMBER, 
    LAST_UPDATE_DATE   DATE, 
    LAST_UPDATED_BY    NUMBER, 
    LAST_UPDATE_LOGIN  NUMBER, 
    REQUEST_ID         NUMBER
);

DROP SYNONYM APPS.UBER_OTM_ORDER_REL_BATCH_STG;

CREATE SYNONYM APPS.UBER_OTM_ORDER_REL_BATCH_STG FOR XXUBER.UBER_OTM_ORDER_REL_BATCH_STG;