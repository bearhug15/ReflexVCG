theory Fridge_VC107
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
 and st9_FridgrDoorController_state:"(getPstate st9 ''FridgrDoorController'')=''longOpen''"
 and st9_if20:"((getVarBool st9 ''inp_5'') = False)=True"
 and st10:"(st10=(setVarBool st9 ''out_3'' False))"
 and st11:"(st11=(setVarBool st10 ''out_4'' False))"
 and st12:"(st12=(setPstate st11 ''FridgrDoorController'' ''closed''))"
 and st12_FridgeCompressorController_state:"(getPstate st12 ''FridgeCompressorController'')=''checkTemp''"
 and st12_if19:"(getVarBool st12 ''inp_2'')=False"
 and st12_FreezerCompressorController_state:"(getPstate st12 ''FreezerCompressorController'')=''stop''"
 and st13:"(st13=(toEnv st12))"
 and st_final:"(st_final=st13)"
shows "(inv st_final)"

end
