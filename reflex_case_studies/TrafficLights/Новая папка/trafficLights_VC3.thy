theory trafficLights_VC3
imports trafficLightsTheory
begin

lemma
 assumes base_inv:"(inv st0)"
 and st1:"(st1=(setVarBool st0 ''button'' button))"
 and st1_Controller_state:"(getPstate st1 ''Controller'')=''minimalRed''"
 and st1_if1:"(getVarBool st1 ''button'')=False"
 and st1_minimalRed_timeout:"10000\<le>(ltime st1 ''Controller'')"
 and st2:"(st2=(setPstate st1 ''Controller'' ''redAfterMinimalRed''))"
 and st3:"(st3=(toEnv st2))"
 and st_final:"(st_final=st3)"
shows "(inv st_final)"

end
