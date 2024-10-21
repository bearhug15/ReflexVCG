theory RevolvingDoor_VC4
imports RevolvingDoorTheory
begin

lemma
 assumes base_inv:"(inv st0)"
 and st1:"(st1=(setVarBool st0 ''pressure'' pressure))"
 and st2:"(st2=(setVarBool st1 ''user'' user))"
 and st2_Controller_state:"(getPstate st2 ''Controller'')=''rotating''"
 and st2_if4:"(getVarBool st2 ''pressure'')=True"
 and st3:"(st3=(setVarBool st2 ''rotation'' False))"
 and st4:"(st4=(setVarBool st3 ''brake'' True))"
 and st5:"(st5=(setPstate st4 ''Controller'' ''suspended''))"
 and st5_rotating_timeout:"1000>(ltime st5 ''Controller'')"
 and st6:"(st6=(toEnv st5))"
 and st_final:"(st_final=st6)"
shows "(inv st_final)"

end
