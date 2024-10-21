theory trafficLights_VC6
imports trafficLightsTheory
begin

lemma
 assumes base_inv:"(inv st0)"
 and st1:"(st1=(setVarBool st0 ''button'' button))"
 and st1_Controller_state:"(getPstate st1 ''Controller'')=''redAfterMinimalRed''"
 and st1_if3:"((getVarBool st1 ''_bpressed'') \<or> (getVarBool st1 ''button''))=False"
 and st2:"(st2=(toEnv st1))"
 and st_final:"(st_final=st2)"
shows "(inv st_final)"

end
