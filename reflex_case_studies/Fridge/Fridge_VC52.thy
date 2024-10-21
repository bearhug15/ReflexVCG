theory Fridge_VC52
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
 and st5_FridgrDoorController_state:"(getPstate st5 ''FridgrDoorController'')=''open''"
 and st5_if11:"((getVarBool st5 ''inp_4'') = False)=False"
 and st5_open_timeout:"30000\<le>(ltime st5 ''FridgrDoorController'')"
 and st6:"(st6=(setVarBool st5 ''out_3'' True))"
 and st7:"(st7=(setPstate st6 ''FridgrDoorController'' ''longOpen''))"
 and st7_FridgeCompressorController_state:"(getPstate st7 ''FridgeCompressorController'')=''checkTemp''"
 and st7_if17:"(getVarBool st7 ''inp_1'')=False"
 and st7_if19:"(\<not>(getVarBool st7 ''inp_0''))=False"
 and st7_FreezerCompressorController_state:"(getPstate st7 ''FreezerCompressorController'')=''checkTemp''"
 and st7_if20:"(getVarBool st7 ''inp_3'')=True"
 and st8:"(st8=(setVarBool st7 ''out_1'' True))"
 and st9:"(st9=(toEnv st8))"
 and st_final:"(st_final=st9)"
shows "(inv st_final)"

end
