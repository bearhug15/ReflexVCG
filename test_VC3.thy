theory test_VC3
imports testTheory
begin

lemma
 assumes base_inv:"(inv st0)"
 and st0_Dryer_state:"(getPstate st0 ''Dryer'')=''Work''"
 and st0_if:"(getVarBool st0 ''_inp_1'')=True"
 and st1:"(st1=(reset st0 ''Dryer''))"
 and st1_Work_timeout:"(getVarNat st1 ''TIMEOUT'')\<le>(ltime st1 ''Dryer'')"
 and st2:"(st2=(setVarBool st1 ''_out_1'' false))"
 and st3:"(st3=(setPstate st2 ''Dryer'' ''Wait''))"
 and st4:"(st4=(toEnv st3))"
shows "(inv st4)"

end
