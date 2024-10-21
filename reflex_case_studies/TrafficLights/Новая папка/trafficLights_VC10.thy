theory trafficLights_VC10
imports trafficLightsTheory
begin

lemma
 assumes base_inv:"(inv st0)"
 and st1:"(st1=(setVarBool st0 ''button'' button))"
 and st1_Controller_state:"(getPstate st1 ''Controller'')=''green''"
 and st1_green_timeout:"30000>(ltime st1 ''Controller'')"
 and st2:"(st2=(toEnv st1))"
 and st_final:"(st_final=st2)"
shows "(inv st_final)"

end
