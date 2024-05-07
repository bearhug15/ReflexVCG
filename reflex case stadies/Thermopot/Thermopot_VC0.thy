theory Thermopot_VC0
imports ThermopotTheory
begin

lemma
 assumes st0:"(st0=emptyState)"
 and st1:"(st1=(setPstate st0 ''Init'' ''begin''))"
 and st2:"(st2=(toEnv st1))"
shows "(inv st2)"

end
