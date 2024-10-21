theory Turnstile_VC109
imports TurnstileTheory
begin

lemma
 assumes base_inv:"(inv st0)"
 and st1:"(st1=(setVarBool st0 ''inp_2'' inp_2))"
 and st2:"(st2=(setVarBool st1 ''inp_1'' inp_1))"
 and st3:"(st3=(setVarBool st2 ''inp_0'' inp_0))"
 and st3_Init_state:"(getPstate st3 ''Init'')=''stop''"
 and st3_Controller_state:"(getPstate st3 ''Controller'')=''isOpened''"
 and st3_if14:"(getVarBool st3 ''inp_0'')=True"
 and st4:"(st4=(setVarBool st3 ''out_0'' False))"
 and st5:"(st5=(setPstate st4 ''Controller'' ''isClosed''))"
 and st5_isOpened_timeout:"0t9s>(ltime st5 ''Controller'')"
 and st5_EntranceController_state:"(getPstate st5 ''EntranceController'')=''isOpened''"
 and st5_if11:"(\<not>(getVarBool st5 ''inp_2''))=False"
 and st5_Unlocker_state:"(getPstate st5 ''Unlocker'')=''unlock''"
 and st5_unlock_timeout:"0t1s\<le>(ltime st5 ''Unlocker'')"
 and st6:"(st6=(setVarBool st5 ''out_1'' False))"
 and st7:"(st7=(setPstate st6 ''Unlocker'' ''''stop''''))"
 and st8:"(st8=(toEnv st7))"
 and st_final:"(st_final=st8)"
shows "(inv st_final)"

end
