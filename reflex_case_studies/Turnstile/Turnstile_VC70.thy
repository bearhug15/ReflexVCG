theory Turnstile_VC70
imports TurnstileTheory
begin

lemma
 assumes base_inv:"(inv st0)"
 and st1:"(st1=(setVarBool st0 ''inp_2'' inp_2))"
 and st2:"(st2=(setVarBool st1 ''inp_1'' inp_1))"
 and st3:"(st3=(setVarBool st2 ''inp_0'' inp_0))"
 and st3_Init_state:"(getPstate st3 ''Init'')=''stop''"
 and st3_Controller_state:"(getPstate st3 ''Controller'')=''minimalOpened''"
 and st3_if7:"(getVarBool st3 ''inp_0'')=False"
 and st3_minimalOpened_timeout:"0t1s\<le>(ltime st3 ''Controller'')"
 and st3_if8:"(getVarBool st3 ''_passed'')=True"
 and st4:"(st4=(setVarBool st3 ''out_0'' False))"
 and st5:"(st5=(setPstate st4 ''Controller'' ''isClosed''))"
 and st5_EntranceController_state:"(getPstate st5 ''EntranceController'')=''isClosed''"
 and st5_if4:"(getVarBool st5 ''inp_2'')=True"
 and st6:"(st6=(setVarBool st5 ''out_2'' True))"
 and st7:"(st7=(setPstate st6 ''EntranceController'' ''isOpened''))"
 and st7_Unlocker_state:"(getPstate st7 ''Unlocker'')=''stop''"
 and st8:"(st8=(toEnv st7))"
 and st_final:"(st_final=st8)"
shows "(inv st_final)"

end
