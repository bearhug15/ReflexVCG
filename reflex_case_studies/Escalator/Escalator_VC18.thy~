theory Escalator_VC18
imports EscalatorTheory
begin

lemma
 assumes base_inv:"(inv st0)"
 and st1:"(st1=(setVarBool st0 ''inp_3'' inp_3))"
 and st2:"(st2=(setVarBool st1 ''inp_2'' inp_2))"
 and st3:"(st3=(setVarBool st2 ''inp_1'' inp_1))"
 and st4:"(st4=(setVarBool st3 ''inp_0'' inp_0))"
 and st5:"(st5=(setVarBool st4 ''inp_4'' inp_4))"
 and st5_Ctrl_state:"(getPstate st5 ''Ctrl'')=''stuckState''"
 and st6:"(st6=(setVarBool st5 ''out_0'' False))"
 and st7:"(st7=(setVarBool st6 ''out_1'' False))"
 and st7_if1:"(getVarBool st7 ''inp_3'')=False"
 and st7_if3:"(getVarBool st7 ''inp_4'')=False"
 and st7_stuckState_timeout:"1000\<le>(ltime st7 ''Ctrl'')"
 and st7_if4:"(getVarBool st7 ''_moving'')=True"
 and st7_if5:"((getVarBool st7 ''_direction'') = True)=True"
 and st8:"(st8=(setVarBool st7 ''out_0'' True))"
 and st9:"(st9=(setPstate st8 ''Ctrl'' ''goUp''))"
 and st10:"(st10=(toEnv st9))"
 and st_final:"(st_final=st10)"
shows "(inv st_final)"

end
