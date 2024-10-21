theory Fridge_VC72
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
 and st5_if20:"((getVarBool st5 ''inp_4'') = False)=True"
 and st6:"(st6=(setVarBool st5 ''out_2'' False))"
 and st7:"(st7=(setVarBool st6 ''out_3'' False))"
 and st8:"(st8=(setPstate st7 ''FridgrDoorController'' ''closed''))"
 and st8_FridgeCompressorController_state:"(getPstate st8 ''FridgeCompressorController'')=''checkTemp''"
 and st8_if25:"(getVarBool st8 ''inp_1'')=False"
 and st8_if27:"(\<not>(getVarBool st8 ''inp_0''))=False"
 and st8_FreezerCompressorController_state:"(getPstate st8 ''FreezerCompressorController'')=''checkTemp''"
 and st8_if29:"(getVarBool st8 ''inp_3'')=False"
 and st8_if31:"(\<not>(getVarBool st8 ''inp_2''))=False"
 and st9:"(st9=(toEnv st8))"
 and st_final:"(st_final=st9)"
shows "(inv st_final)"

end
