theory trafficLights_VC9
imports trafficLightsTheory
begin

lemma
 assumes base_inv:"(inv st0)"
 and st1:"(st1=(setVarBool st0 ''button'' button))"
 and st1_Controller_state:"(getPstate st1 ''Controller'')=''green''"
 and st1_green_timeout:"30000\<le>(ltime st1 ''Controller'')"
 and st2:"(st2=(setVarBool st1 ''light'' False))"
 and st3:"(st3=(setPstate st2 ''Controller'' ''minimalRed''))"
 and st4:"(st4=(toEnv st3))"
 and st_final:"(st_final=st4)"
shows "(inv st_final)"

end
