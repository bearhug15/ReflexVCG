theory RevolvingDoor_VC8
imports RevolvingDoorTheory
begin

lemma
 assumes base_inv:"(inv st0)"
 and st1:"(st1=(setVarBool st0 ''pressure'' pressure))"
 and st2:"(st2=(setVarBool st1 ''user'' user))"
 and st2_Controller_state:"(getPstate st2 ''Controller'')=''suspended''"
 and st2_if8:"(getVarBool st2 ''pressure'')=True"
 and st3:"(st3=(reset st2 ''Controller''))"
 and st3_suspended_timeout:"1000>(ltime st3 ''Controller'')"
 and st4:"(st4=(toEnv st3))"
 and st_final:"(st_final=st4)"
shows "(inv st_final)"

end
