theory Fridge_VC20
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
 and st5_FridgrDoorController_state:"(getPstate st5 ''FridgrDoorController'')=''closed''"
 and st5_if0:"(getVarBool st5 ''inp_4'')=True"
 and st6:"(st6=(setVarBool st5 ''out_2'' True))"
 and st7:"(st7=(setPstate st6 ''FridgrDoorController'' ''open''))"
 and st7_FridgeCompressorController_state:"(getPstate st7 ''FridgeCompressorController'')=''checkTemp''"
 and st7_if0:"(getVarBool st7 ''inp_1'')=True"
 and st8:"(st8=(setVarBool st7 ''out_0'' True))"
 and st8_FreezerCompressorController_state:"(getPstate st8 ''FreezerCompressorController'')=''checkTemp''"
 and st8_if1:"(getVarBool st8 ''inp_3'')=False"
 and st8_if2:"(\<not>(getVarBool st8 ''inp_2''))=True"
 and st9:"(st9=(setVarBool st8 ''out_1'' False))"
 and st10:"(st10=(toEnv st9))"
 and st_final:"(st_final=st10)"
shows "(inv st_final)"

end
