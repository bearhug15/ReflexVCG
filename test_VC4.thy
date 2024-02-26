theory test_VC4
imports testTheory
begin

lemma
 assumes base_inv:"(inv st0)"
 and st0_Dryer_state:"(getPstate st0 ''Dryer'')=''Work''"
 and st0_if:"(getVarBool st0 ''_inp_1'')=True"
 and st1:"(st1=(reset st0 ''Dryer''))"
 and st1_Work_timeout:"(getVarNat st1 ''TIMEOUT'')>(ltime st1 ''Dryer'')"
 and st2:"(st2=(toEnv st1))"
shows "(inv st2)"

end
