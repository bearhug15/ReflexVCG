theory RevolvingDoor_VC3
imports RevolvingDoorTheory
begin

lemma
 assumes base_inv:"(inv st0)"
 and st1:"(st1=(setVarBool st0 ''inp_2'' inp_2))"
 and st2:"(st2=(setVarBool st1 ''inp_1'' inp_1))"
 and st2_Controller_state:"(getPstate st2 ''Controller'')=''motionless''"
 and st2_if3:"(getVarBool st2 ''inp_1'')=False"
 and st3:"(st3=(toEnv st2))"
shows "(inv st3)"

end
