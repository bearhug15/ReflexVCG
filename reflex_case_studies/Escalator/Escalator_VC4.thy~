theory Escalator_VC4
imports EscalatorTheory
begin

lemma
 assumes base_inv:"(inv st0)"
 and st1:"(st1=(setVarBool st0 ''inp_3'' inp_3))"
 and st2:"(st2=(setVarBool st1 ''inp_2'' inp_2))"
 and st3:"(st3=(setVarBool st2 ''inp_1'' inp_1))"
 and st4:"(st4=(setVarBool st3 ''inp_0'' inp_0))"
 and st5:"(st5=(setVarBool st4 ''inp_4'' inp_4))"
 and st5_Ctrl_state:"(getPstate st5 ''Ctrl'')=''motionless''"
 and st5_if1:"(getVarBool st5 ''inp_3'')=False"
 and st5_if3:"(getVarBool st5 ''inp_4'')=False"
 and st5_if4:"((getVarBool st5 ''inp_0'') \<or> (getVarBool st5 ''inp_1''))=True"
 and st5_if6:"((getVarBool st5 ''inp_2'') = True)=False"
 and st6:"(st6=(setVarBool st5 ''out_1'' True))"
 and st7:"(st7=(setVarBool st6 ''_moving'' True))"
 and st8:"(st8=(setVarBool st7 ''_direction'' False))"
 and st9:"(st9=(setPstate st8 ''Ctrl'' ''goDown''))"
 and st10:"(st10=(toEnv st9))"
 and st_final:"(st_final=st10)"
shows "(inv st_final)"

end
