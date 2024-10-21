theory Turnstile_VC12
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
 and st6_if3:"(getVarBool st6 ''inp_2'')=False"
 and st6_Unlocker_state:"(getPstate st6 ''Unlocker'')=''stop''"
 and st7:"(st7=(toEnv st6))"
 and st_final:"(st_final=st7)"
shows "(inv st_final)"

end
