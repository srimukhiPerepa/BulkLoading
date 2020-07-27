REM Testing SQL File Errors
REM test
WHENEVER SQLERROR CONTINUE;

CREATE TABLE apps.uber_test_tbl(
    id number,
    dat varchar2(2000)
);

CREATE OR REPLACE SYNONYM apps.uber_test_tbl_syn FOR apps.uber_test_tbl;