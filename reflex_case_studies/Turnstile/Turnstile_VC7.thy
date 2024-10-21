theory Turnstile_VC7
imports TurnstileTheory
begin

lemma
 assumes base_inv:"(inv st0)"
 and st1:"(st1=(setVarBool st0 ''inp_2'' inp_2))"
 and st2:"(st2=(setVarBool st1 ''inp_1'' inp_1))"
 and st3:"(st3=(setVarBool st2 ''inp_0'' inp_0))"
 and st3_Init_state:"(getPstate st3 ''Init'')=''init''"
 and st4:"(st4=(setPstate st3 ''Controller'' ''isClosed''))"
 and st5:"(st5=(setPstate st4 ''EntranceController'' ''isClosed''))"
 and st6:"(st6=(setPstate st5 ''Init'' ''''stop''''))"
 and st6_Controller_state:"(getPstate st6 ''Controller'')=''isClosed''"
 and st6_if1:"(getVarBool st6 ''inp_1'')=False"
 and st6_EntranceController_state:"(getPstate st6 ''EntranceController'')=''isClosed''"
 and st6_if2:"(getVarBool st6 ''inp_2'')=True"
 and st7:"(st7=(setVarBool st6 ''out_2'' True))"
 and st8:"(st8=(setPstate st7 ''EntranceController'' ''isOpened''))"
 and st8_Unlocker_state:"(getPstate st8 ''Unlocker'')=''unlock''"
 and st8_unlock_timeout:"0t1s\<le>(ltime st8 ''Unlocker'')"
 and st9:"(st9=(setVarBool st8 ''out_1'' False))"
 and st10:"(st10=(setPstate st9 ''Unlocker'' ''''stop''''))"
 and st11:"(st11=(toEnv st10))"
 and st_final:"(st_final=st11)"
shows "(inv st_final)"

end
