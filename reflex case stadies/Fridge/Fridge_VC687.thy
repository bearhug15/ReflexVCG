theory Fridge_VC687
imports FridgeTheory
begin

lemma
 assumes base_inv:"(inv st0)"
 and st1:"(st1=(setVarBool st0 ''inp_3'' inp_3))"
 and st2:"(st2=(setVarBool st1 ''inp_2'' inp_2))"
 and st3:"(st3=(setVarBool st2 ''inp_1'' inp_1))"
 and st4:"(st4=(setVarBool st3 ''inp_5'' inp_5))"
 and st5:"(st5=(setVarBool st4 ''inp_4'' inp_4))"
 and st5_Init_state:"(getPstate st5 ''Init'')=''error''"
 and st5_FridgrDoorController_state:"(getPstate st5 ''FridgrDoorController'')=''longOpen''"
 and st5_if:"((getVarBool st5 ''inp_5'') = False)=False"
 and st5_FridgeCompressorController_state:"(getPstate st5 ''FridgeCompressorController'')=''checkTemp''"
 and st5_if:"(getVarBool st5 ''inp_2'')=False"
 and st5_if:"(\<not>(getVarBool st5 ''inp_1''))=False"
 and st5_FreezerCompressorController_state:"(getPstate st5 ''FreezerCompressorController'')=''error''"
 and st6:"(st6=(toEnv st5))"
shows "(inv st6)"

end
