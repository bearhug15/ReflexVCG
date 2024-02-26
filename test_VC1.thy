theory test_VC1
imports testTheory
begin

lemma
 assumes base_inv:"(inv st0)"
 and st0_Dryer_state:"(getPstate st0 ''Dryer'')=''Wait''"
 and st0_if:"(getVarBool st0 ''_inp_1'')=True"
 and st1:"(st1=(setVarBool st0 ''_out_1'' true))"
 and st2:"(st2=(setPstate st1 ''Dryer'' ''Work''))"
 and st3:"(st3=(toEnv st2))"
shows "(inv st3)"

end
