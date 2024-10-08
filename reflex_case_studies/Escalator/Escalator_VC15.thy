theory Escalator_VC15
imports EscalatorTheory
begin

lemma
 assumes base_inv:"(inv st0)"
 and st1:"(st1=(setVarBool st0 ''inp_3'' inp_3))"
 and st2:"(st2=(setVarBool st1 ''inp_2'' inp_2))"
 and st3:"(st3=(setVarBool st2 ''inp_1'' inp_1))"
 and st4:"(st4=(setVarBool st3 ''inp_5'' inp_5))"
 and st5:"(st5=(setVarBool st4 ''inp_4'' inp_4))"
 and st5_Ctrl_state:"(getPstate st5 ''Ctrl'')=''stuckState''"
 and st6:"(st6=(setVarBool st5 ''out_1'' False))"
 and st7:"(st7=(setVarBool st6 ''out_2'' False))"
 and st7_if1:"(getVarBool st7 ''inp_4'')=False"
 and st7_if2:"(getVarBool st7 ''inp_5'')=True"
 and st8:"(st8=(reset st7 ''Ctrl''))"
 and st8_stuckState_timeout:"1000>(ltime st8 ''Ctrl'')"
 and st9:"(st9=(toEnv st8))"
 and st_final:"(st_final=st9)"
shows "(inv st_final)"

end
