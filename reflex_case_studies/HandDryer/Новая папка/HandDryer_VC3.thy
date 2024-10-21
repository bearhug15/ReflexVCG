theory HandDryer_VC3
imports HandDryerTheory
begin

lemma
 assumes base_inv:"(inv st0)"
 and st1:"(st1=(setVarBool st0 ''inp_1'' inp_1))"
 and st1_Dryer_state:"(getPstate st1 ''Dryer'')=''Work''"
 and st1_if2:"(getVarBool st1 ''inp_1'')=True"
 and st2:"(st2=(reset st1 ''Dryer''))"
 and st2_Work_timeout:"2000>(ltime st2 ''Dryer'')"
 and st3:"(st3=(toEnv st2))"
 and st_final:"(st_final=st3)"
shows "(inv st_final)"

end
