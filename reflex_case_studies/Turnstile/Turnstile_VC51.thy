theory Turnstile_VC51
imports TurnstileTheory
begin

lemma
 assumes base_inv:"(inv st0)"
 and st1:"(st1=(setVarBool st0 ''inp_2'' inp_2))"
 and st2:"(st2=(setVarBool st1 ''inp_1'' inp_1))"
 and st3:"(st3=(setVarBool st2 ''inp_0'' inp_0))"
 and st3_Init_state:"(getPstate st3 ''Init'')=''stop''"
 and st3_Controller_state:"(getPstate st3 ''Controller'')=''minimalOpened''"
 and st3_if6:"(getVarBool st3 ''inp_0'')=True"
 and st4:"(st4=(setVarBool st3 ''_passed'' True))"
 and st4_minimalOpened_timeout:"0t1s\<le>(ltime st4 ''Controller'')"
 and st4_if1:"(getVarBool st4 ''_passed'')=False"
 and st5:"(st5=(setPstate st4 ''Controller'' ''isOpened''))"
 and st5_minimalOpened_timeout:"0t1s>(ltime st5 ''Controller'')"
 and st5_EntranceController_state:"(getPstate st5 ''EntranceController'')=''isClosed''"
 and st5_if1:"(getVarBool st5 ''inp_2'')=False"
 and st5_Unlocker_state:"(getPstate st5 ''Unlocker'')=''stop''"
 and st6:"(st6=(toEnv st5))"
 and st_final:"(st_final=st6)"
shows "(inv st_final)"

end
