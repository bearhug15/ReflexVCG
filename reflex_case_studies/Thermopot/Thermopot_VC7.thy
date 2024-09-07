theory Thermopot_VC7
imports ThermopotTheory
begin

lemma
 assumes base_inv:"(inv st0)"
 and st1:"(st1=(setVarBool st0 ''buttons_1'' buttons_1))"
 and st2:"(st2=(setVarInt st1 ''temperature_0'' temperature_0))"
 and st3:"(st3=(setVarBool st2 ''buttons_2'' buttons_2))"
 and st4:"(st4=(setVarBool st3 ''buttons_3'' buttons_3))"
 and st5:"(st5=(setVarBool st4 ''boilingButton_0'' boilingButton_0))"
 and st5_Init_state:"(getPstate st5 ''Init'')=''begin''"
 and st6:"(st6=(setPstate st5 ''TempSelection'' ''tempSelection''))"
 and st7:"(st7=(setPstate st6 ''HeaterController'' ''begin''))"
 and st8:"(st8=(setPstate st7 ''Init'' ''''stop''''))"
 and st8_TempSelection_state:"(getPstate st8 ''TempSelection'')=''tempSelection''"
 and st8_if0:"((getVarBool st8 ''buttons_1'') = True)=True"
 and st9:"(st9=(setVarInt st8 ''selectedTemp_0'' 98))"
 and st9_HeaterController_state:"(getPstate st9 ''HeaterController'')=''maintaining''"
 and st9_if3:"((getVarBool st9 ''boilingButton_0'') = True)=False"
 and st9_if5:"((getVarInt st9 ''temperature_0'') \<ge> (getVarInt st9 ''selectedTemp_0''))=False"
 and st9_if6:"((getVarInt st9 ''temperature_0'') < ((getVarInt st9 ''selectedTemp_0'') - 5))=True"
 and st10:"(st10=(setVarBool st9 ''heater_0'' True))"
 and st11:"(st11=(toEnv st10))"
 and st_final:"(st_final=st11)"
shows "(inv st_final)"

end