theory test_VC6
imports testTheory
begin

lemma
 assumes base_inv:"(inv st0)"
 and st0_Dryer_state:"(getPstate st0 ''Dryer'')=''stop''"
 and st1:"(st1=(toEnv st0))"
shows "(inv st1)"

end
