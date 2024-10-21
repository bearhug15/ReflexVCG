theory Fridge_VC11
imports FridgeTheory
begin

lemma
 assumes base_inv:"(inv st0)"
 and st1:"(st1=(setVarBool st0 ''inp_3'' inp_3))"
 and st2:"(st2=(setVarBool st1 ''inp_2'' inp_2))"
 and st3:"(st3=(setVarBool st2 ''inp_1'' inp_1))"
 and st4:"(st4=(setVarBool st3 ''inp_0'' inp_0))"
 and st5:"(st5=(setVarBool st4 ''inp_4'' inp_4))"
 and st5_Init_state:"(getPstate st5 ''Init'')=''begin''"
 and st6:"(st6=(setPstate st5 ''FridgrDoorController'' ''closed''))"
 and st7:"(st7=(setPstate st6 ''FridgeCompressorController'' ''checkTemp''))"
 and st8:"(st8=(setPstate st7 ''FreezerCompressorController'' ''checkTemp''))"
 and st9:"(st9=(setPstate st8 ''Init'' ''''stop''''))"
 and st9_FridgrDoorController_state:"(getPstate st9 ''FridgrDoorController'')=''closed''"
 and st9_if1:"(getVarBool st9 ''inp_4'')=False"
 and st9_FridgeCompressorController_state:"(getPstate st9 ''FridgeCompressorController'')=''checkTemp''"
 and st9_if2:"(getVarBool st9 ''inp_1'')=True"
 and st10:"(st10=(setVarBool st9 ''out_0'' True))"
 and st10_FreezerCompressorController_state:"(getPstate st10 ''FreezerCompressorController'')=''checkTemp''"
 and st10_if1:"(getVarBool st10 ''inp_3'')=False"
 and st10_if2:"(\<not>(getVarBool st10 ''inp_2''))=True"
 and st11:"(st11=(setVarBool st10 ''out_1'' False))"
 and st12:"(st12=(toEnv st11))"
 and st_final:"(st_final=st12)"
shows "(inv st_final)"

end
