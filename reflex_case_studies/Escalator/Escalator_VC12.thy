theory Escalator_VC12
imports EscalatorTheory
begin

lemma
 assumes base_inv:"(inv st0)"
 and st1:"(st1=(setVarBool st0 ''inp_3'' inp_3))"
 and st2:"(st2=(setVarBool st1 ''inp_2'' inp_2))"
 and st3:"(st3=(setVarBool st2 ''inp_1'' inp_1))"
 and st4:"(st4=(setVarBool st3 ''inp_5'' inp_5))"
 and st5:"(st5=(setVarBool st4 ''inp_4'' inp_4))"
 and st5_Ctrl_state:"(getPstate st5 ''Ctrl'')=''goDown''"
 and st5_if15:"(getVarBool st5 ''inp_4'')=False"
 and st5_if17:"(getVarBool st5 ''inp_5'')=False"
 and st5_if18:"((getVarBool st5 ''inp_1'') \<or> (getVarBool st5 ''inp_2''))=True"
 and st6:"(st6=(reset st5 ''Ctrl''))"
 and st6_goDown_timeout:"120000>(ltime st6 ''Ctrl'')"
 and st7:"(st7=(toEnv st6))"
 and st_final:"(st_final=st7)"
shows "(inv st_final)"

end
