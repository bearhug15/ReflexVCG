theory Thermopot_VC22
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
 and st8_if1:"((getVarBool st8 ''buttons_1'') = True)=False"
 and st8_if3:"((getVarBool st8 ''buttons_2'') = True)=False"
 and st8_if4:"((getVarBool st8 ''buttons_3'') = True)=True"
 and st9:"(st9=(setVarInt st8 ''selectedTemp_0'' 60))"
 and st9_HeaterController_state:"(getPstate st9 ''HeaterController'')=''heating''"
 and st10:"(st10=(setVarBool st9 ''heater_0'' True))"
 and st11:"(st11=(setVarBool st10 ''lid_0'' True))"
 and st11_if5:"((getVarInt st11 ''temperature_0'') \<ge> 100)=False"
 and st12:"(st12=(toEnv st11))"
shows "(inv st12)"

end
