theory Thermopot_VC40
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
 and st8_TempSelection_state:"(getPstate st8 ''TempSelection'')=''stop''"
 and st8_HeaterController_state:"(getPstate st8 ''HeaterController'')=''heating''"
 and st9:"(st9=(setVarBool st8 ''heater_0'' True))"
 and st10:"(st10=(setVarBool st9 ''lid_0'' True))"
 and st10_if3:"((getVarInt st10 ''temperature_0'') \<ge> 100)=False"
 and st11:"(st11=(toEnv st10))"
shows "(inv st11)"

end
