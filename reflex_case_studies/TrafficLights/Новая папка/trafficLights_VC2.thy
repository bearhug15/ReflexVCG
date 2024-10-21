theory trafficLights_VC2
imports trafficLightsTheory
begin

lemma
 assumes base_inv:"(inv st0)"
 and st1:"(st1=(setVarBool st0 ''button'' button))"
 and st1_Controller_state:"(getPstate st1 ''Controller'')=''minimalRed''"
 and st1_if0:"(getVarBool st1 ''button'')=True"
 and st2:"(st2=(setVarBool st1 ''_bpressed'' True))"
 and st2_minimalRed_timeout:"10000>(ltime st2 ''Controller'')"
 and st3:"(st3=(toEnv st2))"
 and st_final:"(st_final=st3)"
shows "(inv st_final)"

end
