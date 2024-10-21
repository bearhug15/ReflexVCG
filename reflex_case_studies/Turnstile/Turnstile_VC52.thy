theory Turnstile_VC52
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
 and st5_EntranceController_state:"(getPstate st5 ''EntranceController'')=''isOpened''"
 and st5_if2:"(\<not>(getVarBool st5 ''inp_2''))=True"
 and st6:"(st6=(setVarBool st5 ''out_2'' False))"
 and st7:"(st7=(setVarBool st6 ''out_1'' True))"
 and st8:"(st8=(setPstate st7 ''Unlocker'' ''unlock''))"
 and st9:"(st9=(setPstate st8 ''EntranceController'' ''isClosed''))"
 and st9_Unlocker_state:"(getPstate st9 ''Unlocker'')=''unlock''"
 and st9_unlock_timeout:"0t1s\<le>(ltime st9 ''Unlocker'')"
 and st10:"(st10=(setVarBool st9 ''out_1'' False))"
 and st11:"(st11=(setPstate st10 ''Unlocker'' ''''stop''''))"
 and st12:"(st12=(toEnv st11))"
 and st_final:"(st_final=st12)"
shows "(inv st_final)"

end
