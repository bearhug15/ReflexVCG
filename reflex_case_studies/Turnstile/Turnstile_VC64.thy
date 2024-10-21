theory Turnstile_VC64
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
 and st4_minimalOpened_timeout:"0t1s>(ltime st4 ''Controller'')"
 and st4_EntranceController_state:"(getPstate st4 ''EntranceController'')=''isOpened''"
 and st4_if4:"(\<not>(getVarBool st4 ''inp_2''))=True"
 and st5:"(st5=(setVarBool st4 ''out_2'' False))"
 and st6:"(st6=(setVarBool st5 ''out_1'' True))"
 and st7:"(st7=(setPstate st6 ''Unlocker'' ''unlock''))"
 and st8:"(st8=(setPstate st7 ''EntranceController'' ''isClosed''))"
 and st8_Unlocker_state:"(getPstate st8 ''Unlocker'')=''unlock''"
 and st8_unlock_timeout:"0t1s>(ltime st8 ''Unlocker'')"
 and st9:"(st9=(toEnv st8))"
 and st_final:"(st_final=st9)"
shows "(inv st_final)"

end
