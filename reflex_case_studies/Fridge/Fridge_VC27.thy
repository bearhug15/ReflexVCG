theory Fridge_VC27
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
 and st7_if1:"(getVarBool st7 ''inp_1'')=False"
 and st7_if3:"(\<not>(getVarBool st7 ''inp_0''))=False"
 and st7_FreezerCompressorController_state:"(getPstate st7 ''FreezerCompressorController'')=''checkTemp''"
 and st7_if5:"(getVarBool st7 ''inp_3'')=False"
 and st7_if7:"(\<not>(getVarBool st7 ''inp_2''))=False"
 and st8:"(st8=(toEnv st7))"
 and st_final:"(st_final=st8)"
shows "(inv st_final)"

end
