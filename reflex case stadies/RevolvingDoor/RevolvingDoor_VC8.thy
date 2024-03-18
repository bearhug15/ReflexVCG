theory RevolvingDoor_VC8
imports RevolvingDoorTheory
begin

lemma
 assumes base_inv:"(inv st0)"
 and st1:"(st1=(setVarBool st0 ''inp_2'' inp_2))"
 and st2:"(st2=(setVarBool st1 ''inp_1'' inp_1))"
 and st2_Controller_state:"(getPstate st2 ''Controller'')=''suspended''"
 and st2_if8:"(getVarBool st2 ''inp_2'')=True"
 and st3:"(st3=(reset st2 ''Controller''))"
 and st4:"(st4=(toEnv st3))"
shows "(inv st4)"

end
