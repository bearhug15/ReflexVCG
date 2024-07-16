theory RevolvingDoor_VC9
imports RevolvingDoorTheory
begin

lemma
 assumes base_inv:"(inv st0)"
 and st1:"(st1=(setVarBool st0 ''pressure'' pressure))"
 and st2:"(st2=(setVarBool st1 ''user'' user))"
 and st2_Controller_state:"(getPstate st2 ''Controller'')=''suspended''"
 and st2_if9:"(getVarBool st2 ''pressure'')=False"
 and st2_suspended_timeout:"1000\<le>(ltime st2 ''Controller'')"
 and st3:"(st3=(setVarBool st2 ''brake'' False))"
 and st4:"(st4=(setVarBool st3 ''rotation'' True))"
 and st5:"(st5=(setPstate st4 ''Controller'' ''rotating''))"
 and st6:"(st6=(toEnv st5))"
 and st_final:"(st_final=st6)"
shows "(inv st_final)"

end
