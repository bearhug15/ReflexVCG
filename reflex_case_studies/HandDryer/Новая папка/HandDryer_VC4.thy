theory HandDryer_VC4
imports HandDryerTheory
begin

lemma
 assumes base_inv:"(inv st0)"
 and st1:"(st1=(setVarBool st0 ''inp_1'' inp_1))"
 and st1_Dryer_state:"(getPstate st1 ''Dryer'')=''Work''"
 and st1_if3:"(getVarBool st1 ''inp_1'')=False"
 and st1_Work_timeout:"2000\<le>(ltime st1 ''Dryer'')"
 and st2:"(st2=(setVarBool st1 ''out_1'' False))"
 and st3:"(st3=(setPstate st2 ''Dryer'' ''Wait''))"
 and st4:"(st4=(toEnv st3))"
 and st_final:"(st_final=st4)"
shows "(inv st_final)"

end
