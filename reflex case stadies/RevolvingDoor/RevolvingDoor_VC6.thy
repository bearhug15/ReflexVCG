theory RevolvingDoor_VC6
imports RevolvingDoorTheory
begin

lemma
 assumes base_inv:"(inv st0)"
 and st1:"(st1=(setVarBool st0 ''inp_2'' inp_2))"
 and st2:"(st2=(setVarBool st1 ''inp_1'' inp_1))"
 and st2_Controller_state:"(getPstate st2 ''Controller'')=''rotating''"
 and st2_if5:"(getVarBool st2 ''inp_2'')=False"
 and st2_if7:"(getVarBool st2 ''inp_1'')=False"
 and st2_rotating_timeout:"1000\<le>(ltime st2 ''Controller'')"
 and st3:"(st3=(setVarBool st2 ''out_1'' False))"
 and st4:"(st4=(setPstate st3 ''Controller'' ''motionless''))"
 and st5:"(st5=(toEnv st4))"
shows "(inv st5)"

end
