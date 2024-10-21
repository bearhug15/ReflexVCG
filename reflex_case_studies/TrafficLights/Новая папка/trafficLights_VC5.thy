theory trafficLights_VC5
imports trafficLightsTheory
begin

lemma
 assumes base_inv:"(inv st0)"
 and st1:"(st1=(setVarBool st0 ''button'' button))"
 and st1_Controller_state:"(getPstate st1 ''Controller'')=''redAfterMinimalRed''"
 and st1_if2:"((getVarBool st1 ''_bpressed'') \<or> (getVarBool st1 ''button''))=True"
 and st2:"(st2=(setPstate st1 ''Controller'' ''redToGreen''))"
 and st3:"(st3=(toEnv st2))"
 and st_final:"(st_final=st3)"
shows "(inv st_final)"

end
