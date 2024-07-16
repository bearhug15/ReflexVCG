theory RevolvingDoor_VC3
imports RevolvingDoorTheory
begin

lemma
 assumes base_inv:"(inv st0)"
 and st1:"(st1=(setVarBool st0 ''pressure'' pressure))"
 and st2:"(st2=(setVarBool st1 ''user'' user))"
 and st2_Controller_state:"(getPstate st2 ''Controller'')=''motionless''"
 and st2_if3:"(getVarBool st2 ''user'')=False"
 and st3:"(st3=(toEnv st2))"
 and st_final:"(st_final=st3)"
shows "(inv st_final)"

end
