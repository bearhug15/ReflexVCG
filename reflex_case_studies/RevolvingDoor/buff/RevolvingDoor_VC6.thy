theory RevolvingDoor_VC6
imports RevolvingDoorTheory
begin

lemma
 assumes base_inv:"(inv st0)"
 and st1:"(st1=(setVarBool st0 ''pressure'' pressure))"
 and st2:"(st2=(setVarBool st1 ''user'' user))"
 and st2_Controller_state:"(getPstate st2 ''Controller'')=''rotating''"
 and st2_if5:"(getVarBool st2 ''pressure'')=False"
 and st2_if7:"(getVarBool st2 ''user'')=False"
 and st2_rotating_timeout:"1000\<le>(ltime st2 ''Controller'')"
 and st3:"(st3=(setVarBool st2 ''rotation'' False))"
 and st4:"(st4=(setPstate st3 ''Controller'' ''motionless''))"
 and st5:"(st5=(toEnv st4))"
 and st_final:"(st_final=st5)"
shows "(inv st_final)"

end
