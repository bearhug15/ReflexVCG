theory Turnstile_VC2
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
 and st6_if0:"(getVarBool st6 ''inp_1'')=True"
 and st7:"(st7=(setVarBool st6 ''out_0'' True))"
 and st8:"(st8=(setVarBool st7 ''_passed'' False))"
 and st9:"(st9=(setPstate st8 ''Controller'' ''minimalOpened''))"
 and st9_EntranceController_state:"(getPstate st9 ''EntranceController'')=''isClosed''"
 and st9_if0:"(getVarBool st9 ''inp_2'')=True"
 and st10:"(st10=(setVarBool st9 ''out_2'' True))"
 and st11:"(st11=(setPstate st10 ''EntranceController'' ''isOpened''))"
 and st11_Unlocker_state:"(getPstate st11 ''Unlocker'')=''unlock''"
 and st11_unlock_timeout:"0t1s>(ltime st11 ''Unlocker'')"
 and st12:"(st12=(toEnv st11))"
 and st_final:"(st_final=st12)"
shows "(inv st_final)"

end
