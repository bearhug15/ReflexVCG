theory Thermopot_VC35
imports ThermopotTheory
begin

lemma
 assumes base_inv:"(inv st0)"
 and st1:"(st1=(setVarBool st0 ''buttons_1'' buttons_1))"
 and st2:"(st2=(setVarInt st1 ''temperature_0'' temperature_0))"
 and st3:"(st3=(setVarBool st2 ''buttons_2'' buttons_2))"
 and st4:"(st4=(setVarBool st3 ''buttons_3'' buttons_3))"
 and st5:"(st5=(setVarBool st4 ''boilingButton_0'' boilingButton_0))"
 and st5_Init_state:"(getPstate st5 ''Init'')=''stop''"
 and st5_TempSelection_state:"(getPstate st5 ''TempSelection'')=''tempSelection''"
 and st5_if1:"((getVarBool st5 ''buttons_1'') = True)=False"
 and st5_if3:"((getVarBool st5 ''buttons_2'') = True)=False"
 and st5_if5:"((getVarBool st5 ''buttons_3'') = True)=False"
 and st5_HeaterController_state:"(getPstate st5 ''HeaterController'')=''heating''"
 and st6:"(st6=(setVarBool st5 ''heater_0'' True))"
 and st7:"(st7=(setVarBool st6 ''lid_0'' True))"
 and st7_if0:"((getVarInt st7 ''temperature_0'') \<ge> 100)=True"
 and st8:"(st8=(setVarBool st7 ''heater_0'' False))"
 and st9:"(st9=(setVarBool st8 ''lid_0'' False))"
 and st10:"(st10=(setVarBool st9 ''mods_0'' False))"
 and st11:"(st11=(setVarBool st10 ''mods_1'' True))"
 and st12:"(st12=(setPstate st11 ''HeaterController'' ''maintaining''))"
 and st13:"(st13=(toEnv st12))"
 and st_final:"(st_final=st13)"
shows "(inv st_final)"

end
