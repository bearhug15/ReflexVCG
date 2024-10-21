theory Turnstile_VC84
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
 and st3_if9:"(getVarBool st3 ''_passed'')=False"
 and st4:"(st4=(setPstate st3 ''Controller'' ''isOpened''))"
 and st4_minimalOpened_timeout:"0t1s>(ltime st4 ''Controller'')"
 and st4_EntranceController_state:"(getPstate st4 ''EntranceController'')=''isClosed''"
 and st4_if7:"(getVarBool st4 ''inp_2'')=False"
 and st4_Unlocker_state:"(getPstate st4 ''Unlocker'')=''stop''"
 and st5:"(st5=(toEnv st4))"
 and st_final:"(st_final=st5)"
shows "(inv st_final)"

end
