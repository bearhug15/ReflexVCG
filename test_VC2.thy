theory test_VC2
imports testTheory
begin

lemma
 assumes base_inv:"(inv st0)"
 and st0_Dryer_state:"(getPstate st0 ''Dryer'')=''Wait''"
 and st0_if:"(getVarBool st0 ''_inp_1'')=False"
 and st1:"(st1=(toEnv st0))"
shows "(inv st1)"

end
