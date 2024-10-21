theory Turnstile_VC125
imports TurnstileTheory
begin

lemma
 assumes base_inv:"(inv st0)"
 and st1:"(st1=(setVarBool st0 ''inp_2'' inp_2))"
 and st2:"(st2=(setVarBool st1 ''inp_1'' inp_1))"
 and st3:"(st3=(setVarBool st2 ''inp_0'' inp_0))"
 and st3_Init_state:"(getPstate st3 ''Init'')=''stop''"
 and st3_Controller_state:"(getPstate st3 ''Controller'')=''isOpened''"
 and st3_if15:"(getVarBool st3 ''inp_0'')=False"
 and st3_isOpened_timeout:"0t9s>(ltime st3 ''Controller'')"
 and st3_EntranceController_state:"(getPstate st3 ''EntranceController'')=''isClosed''"
 and st3_if16:"(getVarBool st3 ''inp_2'')=True"
 and st4:"(st4=(setVarBool st3 ''out_2'' True))"
 and st5:"(st5=(setPstate st4 ''EntranceController'' ''isOpened''))"
 and st5_Unlocker_state:"(getPstate st5 ''Unlocker'')=''stop''"
 and st6:"(st6=(toEnv st5))"
 and st_final:"(st_final=st6)"
shows "(inv st_final)"

end
