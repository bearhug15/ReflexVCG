theory Fridge_VC55
imports FridgeTheory
begin

lemma
 assumes base_inv:"(inv st0)"
 and st1:"(st1=(setVarBool st0 ''inp_3'' inp_3))"
 and st2:"(st2=(setVarBool st1 ''inp_2'' inp_2))"
 and st3:"(st3=(setVarBool st2 ''inp_1'' inp_1))"
 and st4:"(st4=(setVarBool st3 ''inp_5'' inp_5))"
 and st5:"(st5=(setVarBool st4 ''inp_4'' inp_4))"
 and st5_Init_state:"(getPstate st5 ''Init'')=''begin''"
 and st6:"(st6=(setPstate st5 ''FridgrDoorController'' ''closed''))"
 and st7:"(st7=(setPstate st6 ''FridgeCompressorController'' ''checkTemp''))"
 and st8:"(st8=(setPstate st7 ''FreezerCompressorController'' ''checkTemp''))"
 and st9:"(st9=(setPstate st8 ''Init'' ''''stop''''))"
 and st9_FridgrDoorController_state:"(getPstate st9 ''FridgrDoorController'')=''open''"
 and st9_open_timeout:"30000\<le>(ltime st9 ''FridgrDoorController'')"
 and st10:"(st10=(setVarBool st9 ''out_4'' True))"
 and st11:"(st11=(setPstate st10 ''FridgrDoorController'' ''longOpen''))"
 and st11_FridgeCompressorController_state:"(getPstate st11 ''FridgeCompressorController'')=''checkTemp''"
 and st11_if18:"(getVarBool st11 ''inp_2'')=True"
 and st12:"(st12=(setVarBool st11 ''out_1'' True))"
 and st12_FreezerCompressorController_state:"(getPstate st12 ''FreezerCompressorController'')=''checkTemp''"
 and st12_if12:"(getVarBool st12 ''inp_4'')=True"
 and st13:"(st13=(setVarBool st12 ''out_2'' True))"
 and st13_FridgeCompressorController_state:"(getPstate st13 ''FridgeCompressorController'')=''checkTemp''"
 and st13_if1:"(getVarBool st13 ''inp_2'')=False"
 and st13_if2:"(\<not>(getVarBool st13 ''inp_1''))=True"
 and st14:"(st14=(setVarBool st13 ''out_1'' False))"
 and st14_FreezerCompressorController_state:"(getPstate st14 ''FreezerCompressorController'')=''checkTemp''"
 and st14_if4:"(getVarBool st14 ''inp_4'')=False"
 and st15:"(st15=(toEnv st14))"
 and st_final:"(st_final=st15)"
shows "(inv st_final)"

end
