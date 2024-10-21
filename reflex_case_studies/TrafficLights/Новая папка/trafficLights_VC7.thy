theory trafficLights_VC7
imports trafficLightsTheory
begin

lemma
 assumes base_inv:"(inv st0)"
 and st1:"(st1=(setVarBool st0 ''button'' button))"
 and st1_Controller_state:"(getPstate st1 ''Controller'')=''redToGreen''"
 and st1_redToGreen_timeout:"5000\<le>(ltime st1 ''Controller'')"
 and st2:"(st2=(setVarBool st1 ''light'' True))"
 and st3:"(st3=(setVarBool st2 ''_bpressed'' False))"
 and st4:"(st4=(setPstate st3 ''Controller'' ''green''))"
 and st5:"(st5=(toEnv st4))"
 and st_final:"(st_final=st5)"
shows "(inv st_final)"

end
