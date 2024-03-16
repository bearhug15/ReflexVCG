theory Fridge_VC587
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
 and st5_FridgrDoorController_state:"(getPstate st5 ''FridgrDoorController'')=''open''"
 and st5_if:"((getVarBool st5 ''inp_5'') = False)=False"
 and st5_open_timeout:"30000\<le>(ltime st5 ''FridgrDoorController'')"
 and st6:"(st6=(setVarBool st5 ''out_4'' True))"
 and st7:"(st7=(setPstate st6 ''FridgrDoorController'' ''longOpen''))"
 and st7_FridgeCompressorController_state:"(getPstate st7 ''FridgeCompressorController'')=''checkTemp''"
 and st7_if:"(getVarBool st7 ''inp_2'')=True"
 and st8:"(st8=(setVarBool st7 ''out_1'' True))"
 and st8_FreezerCompressorController_state:"(getPstate st8 ''FreezerCompressorController'')=''checkTemp''"
 and st8_if:"(getVarBool st8 ''inp_4'')=True"
 and st9:"(st9=(setVarBool st8 ''out_2'' True))"
 and st9_FridgeCompressorController_state:"(getPstate st9 ''FridgeCompressorController'')=''checkTemp''"
 and st9_if:"(getVarBool st9 ''inp_2'')=False"
 and st9_if:"(\<not>(getVarBool st9 ''inp_1''))=False"
 and st9_FreezerCompressorController_state:"(getPstate st9 ''FreezerCompressorController'')=''stop''"
 and st10:"(st10=(toEnv st9))"
shows "(inv st10)"

end
