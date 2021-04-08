-- ---------------------------------------------------------------------------
-- Script Name : DF_INC2365797_CA_GCSS_Datafix.sql
-- Date        : 10-Aug-2020
-- Ticket #    : INC2365797
-- Description : Create new Subsidy records in the table CATOKL_SUBSIDY_LINES 
--               for contracts in error.
--               Following contracts will be adjusted:
--               104-50007541    - Create OR record for 27,759.47 CAD
--
-- ---------------------------------------------------------------------------
-- change 11
DECLARE
   -- ----------------------------------
   --  Local Variables
   -- ----------------------------------
   p_debug_yn                  VARCHAR2 (1) := 'N';
   l_status                    VARCHAR2(30);
   l_error_msg                 VARCHAR2(2000);
   l_msg_count                 NUMBER;
   l_org_id                    NUMBER;
   l_subsidy_id                NUMBER;
   l_last_line_record          apps.CATOKL_SUBSIDY_LINES%ROWTYPE;

BEGIN

   cat_api.apps_initialize( p_user_name     => 'CATNJS',
                            p_resp_name     => 'CA ALL: OLFM Customer Service',
                            x_return_status => l_status,
                            x_msg_count     => l_msg_count,
                            x_msg_data      => l_error_msg );
   IF ( l_status <> fnd_api.g_ret_sts_success ) THEN
      raise_application_error( -20001, fnd_msg_pub.get( fnd_msg_pub.g_first, 'F' ));
   END IF;

   SELECT organization_id
     INTO l_org_id
     FROM hr_all_organization_units
    WHERE name = 'CA_CFSL_OU';

   mo_global.init('OKL');
   mo_global.set_policy_context ('S', l_org_id);

   l_subsidy_id := 17520;

   SELECT *
     INTO l_last_line_record
     FROM apps.catokl_subsidy_lines
    WHERE subsidy_id = l_subsidy_id
      AND line_id = (SELECT MAX (line_id)
                       FROM apps.catokl_subsidy_lines
                      WHERE subsidy_id = l_subsidy_id);

   l_last_line_record.line_id := l_last_line_record.line_id + 1;
   l_last_line_record.trxn_date := SYSDATE;
   l_last_line_record.trxn_code := 'OR';
   l_last_line_record.trxn_date_processed := NULL;
   l_last_line_record.trxn_message := 'DF Adjust Claim - INC2365797';
   l_last_line_record.object_version_number := 1;
   l_last_line_record.created_by := fnd_global.user_id;
   l_last_line_record.creation_date := SYSDATE;
   l_last_line_record.last_updated_by := fnd_global.user_id;
   l_last_line_record.last_update_date := SYSDATE;
   l_last_line_record.last_update_login := fnd_global.login_id;
   l_last_line_record.record_src := 'USER';
   l_last_line_record.subsidy_vat := 0;
   l_last_line_record.trxn_amount_vat := 0;
   l_last_line_record.manufacturer_subsidy := 30872.47;
   l_last_line_record.trxn_amount := 27759.47;
   l_last_line_record.msg_code := NULL;

   INSERT INTO apps.catokl_subsidy_lines
      VALUES l_last_line_record;

   UPDATE apps.catokl_subsidy_header_all
      SET trxn_process_flag = 'N'
    WHERE subsidy_id = 17520;

   UPDATE apps.catokl_subsidy_lines
      SET manufacturer_subsidy = 0,
	      trxn_amount = 0
    WHERE subsidy_id = 17520
	  AND line_id = 1;

   COMMIT;
END;
/

EXIT
/