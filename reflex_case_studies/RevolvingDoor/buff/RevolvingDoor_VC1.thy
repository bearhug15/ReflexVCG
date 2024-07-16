theory RevolvingDoor_VC1
imports RevolvingDoorTheory
begin

lemma
 assumes base_inv:"(inv st0)"
 and st1:"(st1=(setVarBool st0 ''pressure'' pressure))"
 and st2:"(st2=(setVarBool st1 ''user'' user))"
 and st2_Controller_state:"(getPstate st2 ''Controller'')=''motionless''"
 and st2_if0:"(getVarBool st2 ''user'')=True"
 and st2_if1:"(getVarBool st2 ''pressure'')=True"
 and st3:"(st3=(setVarBool st2 ''brake'' True))"
 and st4:"(st4=(setPstate st3 ''Controller'' ''suspended''))"
 and st5:"(st5=(toEnv st4))"
 and st_final:"(st_final=st5)"
shows "(inv st_final)"

end
