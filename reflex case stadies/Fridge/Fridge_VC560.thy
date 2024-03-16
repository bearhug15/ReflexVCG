theory Fridge_VC560
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
 and st5_if:"((getVarBool st5 ''inp_5'') = False)=True"
 and st6:"(st6=(setVarBool st5 ''out_3'' False))"
 and st7:"(st7=(setPstate st6 ''FridgrDoorController'' ''closed''))"
 and st7_FridgeCompressorController_state:"(getPstate st7 ''FridgeCompressorController'')=''checkTemp''"
 and st7_if:"(getVarBool st7 ''inp_2'')=False"
 and st7_if:"(\<not>(getVarBool st7 ''inp_1''))=False"
 and st7_FreezerCompressorController_state:"(getPstate st7 ''FreezerCompressorController'')=''checkTemp''"
 and st7_if:"(getVarBool st7 ''inp_4'')=False"
 and st7_if:"(\<not>(getVarBool st7 ''inp_3''))=True"
 and st8:"(st8=(setVarBool st7 ''out_2'' False))"
 and st9:"(st9=(toEnv st8))"
shows "(inv st9)"

end
