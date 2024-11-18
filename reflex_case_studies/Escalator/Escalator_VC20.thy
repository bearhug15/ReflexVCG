theory Escalator_VC20
imports EscalatorTheory Requirements extra
begin

lemma
 assumes base_inv:"(extraInv st0)"
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
 and st7_if7:"(getVarBool st7 ''_moving'')=False"
 and st8:"(st8=(setPstate st7 ''Ctrl'' ''motionless''))"
 and st9:"(st9=(toEnv st8))"
 and st_final:"(st_final=st9)"
shows "(extraInv st_final)" by sorry

end
