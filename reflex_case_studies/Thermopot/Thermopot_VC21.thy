theory Thermopot_VC21
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
 and st5_if2:"((getVarBool st5 ''buttons_2'') = True)=True"
 and st6:"(st6=(setVarInt st5 ''selectedTemp_0'' 85))"
 and st6_HeaterController_state:"(getPstate st6 ''HeaterController'')=''maintaining''"
 and st6_if10:"((getVarBool st6 ''boilingButton_0'') = True)=True"
 and st7:"(st7=(setVarBool st6 ''mods_1'' False))"
 and st8:"(st8=(setVarBool st7 ''mods_0'' True))"
 and st9:"(st9=(setPstate st8 ''HeaterController'' ''heating''))"
 and st10:"(st10=(toEnv st9))"
 and st_final:"(st_final=st10)"
shows "(inv st_final)"

end
