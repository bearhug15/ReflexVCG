theory RevolvingDoor_VC10
imports RevolvingDoorTheory
begin

lemma
 assumes base_inv:"(inv st0)"
 and st1:"(st1=(setVarBool st0 ''pressure'' pressure))"
 and st2:"(st2=(setVarBool st1 ''user'' user))"
 and st2_Controller_state:"(getPstate st2 ''Controller'')=''suspended''"
 and st2_if9:"(getVarBool st2 ''pressure'')=False"
 and st2_suspended_timeout:"1000>(ltime st2 ''Controller'')"
 and st3:"(st3=(toEnv st2))"
 and st_final:"(st_final=st3)"
shows "(inv st_final)"

end
