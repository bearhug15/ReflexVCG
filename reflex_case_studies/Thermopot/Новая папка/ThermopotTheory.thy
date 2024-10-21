theory ThermopotTheory
imports ReflexPatterns
begin

fun ltime:: "state \<Rightarrow> process \<Rightarrow> nat" where 
"ltime emptyState _ = 0" 
| "ltime (toEnv s) p = (ltime s p) + 100" 
| "ltime (setVarBool s _ _) p = ltime s p" 
| "ltime (setVarInt s _ _) p = ltime s p"
| "ltime (setVarNat s _ _) p = ltime s p"
| "ltime (setVarReal s _ _) p = ltime s p"
| "ltime (setPstate s p1 _) p =
  (if p=p1 then 0 else ltime s p)" 
| "ltime (reset s p1) p =
  (if p=p1 then 0 else ltime s p)"

lemma ltime_mod:
assumes "ltime s0 p < a*100"
shows "ltime s0 p \<le> (a*100-100)"
proof -
have"(ltime s0 p) mod 100 = 0" by (induction s0) (auto)
thus ?thesis using assms by (induction a) (auto)
qed
lemma ltime_mult:
"ltime s p mod 100 = 0"
  by (induction s) (auto)

lemma ltime_div_less:
assumes "(ltime s0 p div 100)\<le> a"
shows "(ltime s0 p -100) div 100 < a \<or> ltime s0 p = 0"
proof -
have"(ltime s0 p) mod 100 = 0" by (induction s0) (auto)
thus ?thesis using assms by (induction a) (auto)
qed

lemma ltime_le_toEnvNum: 
"ltime s p div 100 \<le> toEnvNum emptyState s"
  apply(induction s)
         apply(auto)
  done

(*
definition R1 where "R1 s \<equiv> toEnvP s \<and>
(\<forall> s1 s2. substate s1 s2 \<and> substate s2 s \<and> toEnvP s1 \<and> toEnvP s2 \<and> toEnvNum s1 s2 = 1 \<and>
getVarBool s1 ''boilingMode'' \<and> getVarInt s2 ''temperature'' < 100 \<longrightarrow>
getVarBool s2 ''lid'' = False)"

definition R2 where "R2 s \<equiv> toEnvP s \<and>
(\<forall> s1 s2. substate s1 s2 \<and> substate s2 s \<and> toEnvP s1 \<and> toEnvP s2 \<and> toEnvNum s1 s2 = 1 \<and>
getVarBool s1 maintainingMode' \<and> \<not> (getVarBool s2 boilingButton' = PRESSED') \<and> getVarInt s2 temperature' \<ge> getVarInt s2 selectedTemp' \<longrightarrow> getVarBool s2 heater' = False)"

definition R3 where "R3 s \<equiv> toEnvP s \<and>
(\<forall> s1 s2. substate s1 s2 \<and> substate s2 s \<and> toEnvP s1 \<and> toEnvP s2 \<and> toEnvNum s1 s2 = 1 \<and>
getVarBool s1 maintainingMode' \<and> \<not> getVarBool s2 boilingButton' = PRESSED' \<and> getVarInt s2 temperature' < getVarInt s2 selectedTemp' - 5 \<longrightarrow>
getVarBool s2 heater' = True)"

definition R4 where "R4 s \<equiv> toEnvP s \<and>
(\<forall> s1. substate s1 s \<and> toEnvP s1 \<and> getVarBool s1 button1' \<longrightarrow> getVarInt s1 selectedTemp' = TEMP1')"

definition R5 where "R5 s \<equiv> toEnvP s \<and>
(\<forall> s1 s2. substate s1 s2 \<and> substate s2 s \<and> toEnvP s1 \<and> toEnvP s2 \<and> toEnvNum s1 s2 = 1 \<and> 
\<not> getVarBool s1 ''boilingMode'' \<and> \<not> getVarBool s1 maintainingMode' \<longrightarrow> getVarBool s2 heater' = False)"
*)

(*
P2_1 1. Пока требуемая температура не достигнута, крышка заблокирована.
P2_1 2. При достижении заданной температуры ТЭН отключается.
P2_1 3. ТЭН включается при понижении температуру воды более, чем на 5 градусов   ниже заданной.
P1   4. При нажатии одной из кнопок выбора температурного режима выбирается соответствующая требуемая температура.
P2_1 5. Блокировка автоматического кипячения воды привключении термолота  в сеть. (Если кнопка кипячения не нажата, то нагрев не включится.)
*)


end
