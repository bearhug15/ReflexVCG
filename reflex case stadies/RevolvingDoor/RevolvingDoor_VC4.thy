theory RevolvingDoor_VC4
imports RevolvingDoorTheory
begin

lemma
 assumes base_inv:"(inv st0)"
 and st1:"(st1=(setVarBool st0 ''inp_2'' inp_2))"
 and st2:"(st2=(setVarBool st1 ''inp_1'' inp_1))"
 and st2_Controller_state:"(getPstate st2 ''Controller'')=''rotating''"
 and st2_if4:"(getVarBool st2 ''inp_2'')=True"
 and st3:"(st3=(setVarBool st2 ''out_1'' False))"
 and st4:"(st4=(setVarBool st3 ''out_2'' True))"
 and st5:"(st5=(setPstate st4 ''Controller'' ''suspended''))"
 and st6:"(st6=(toEnv st5))"
shows "(inv st6)"

end
