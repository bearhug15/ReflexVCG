theory HandDryer_VC6
imports HandDryerTheory
begin

lemma
 assumes base_inv:"(inv st0)"
 and st1:"(st1=(setVarBool st0 ''inp_1'' inp_1))"
 and st1_Dryer_state:"(getPstate st1 ''Dryer'')=''stop''"
 and st2:"(st2=(toEnv st1))"
shows "(inv st2)"

end
