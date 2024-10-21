theory Fridge_VC81
imports FridgeTheory
begin

lemma
 assumes base_inv:"(inv st0)"
 and st1:"(st1=(setVarBool st0 ''inp_3'' inp_3))"
 and st2:"(st2=(setVarBool st1 ''inp_2'' inp_2))"
 and st3:"(st3=(setVarBool st2 ''inp_1'' inp_1))"
 and st4:"(st4=(setVarBool st3 ''inp_0'' inp_0))"
 and st5:"(st5=(setVarBool st4 ''inp_4'' inp_4))"
 and st5_Init_state:"(getPstate st5 ''Init'')=''stop''"
 and st5_FridgrDoorController_state:"(getPstate st5 ''FridgrDoorController'')=''longOpen''"
 and st5_if21:"((getVarBool st5 ''inp_4'') = False)=False"
 and st5_FridgeCompressorController_state:"(getPstate st5 ''FridgeCompressorController'')=''checkTemp''"
 and st5_if23:"(getVarBool st5 ''inp_1'')=False"
 and st5_if25:"(\<not>(getVarBool st5 ''inp_0''))=False"
 and st5_FreezerCompressorController_state:"(getPstate st5 ''FreezerCompressorController'')=''checkTemp''"
 and st5_if27:"(getVarBool st5 ''inp_3'')=False"
 and st5_if29:"(\<not>(getVarBool st5 ''inp_2''))=False"
 and st6:"(st6=(toEnv st5))"
 and st_final:"(st_final=st6)"
shows "(inv st_final)"

end
