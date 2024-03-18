theory RevolvingDoor_VC2
imports RevolvingDoorTheory
begin

lemma
 assumes base_inv:"(inv st0)"
 and st1:"(st1=(setVarBool st0 ''inp_2'' inp_2))"
 and st2:"(st2=(setVarBool st1 ''inp_1'' inp_1))"
 and st2_Controller_state:"(getPstate st2 ''Controller'')=''motionless''"
 and st2_if0:"(getVarBool st2 ''inp_1'')=True"
 and st2_if2:"(getVarBool st2 ''inp_2'')=False"
 and st3:"(st3=(setVarBool st2 ''out_1'' True))"
 and st4:"(st4=(setPstate st3 ''Controller'' ''rotating''))"
 and st5:"(st5=(toEnv st4))"
shows "(inv st5)"

end
