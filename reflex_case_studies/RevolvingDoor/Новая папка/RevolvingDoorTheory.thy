theory RevolvingDoorTheory
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
assumes "(ltime s0 p div 100)≤ a"
shows "(ltime s0 p -100) div 100 < a ∨ ltime s0 p = 0"
proof -
have"(ltime s0 p) mod 100 = 0" by (induction s0) (auto)
thus ?thesis using assms by (induction a) (auto)
qed

lemma ltime_le_toEnvNum: 
"ltime s p div 100 ≤ toEnvNum emptyState s"
  apply(induction s)
         apply(auto)
  done
lemma toEnvNum_getPstate:
"toEnvNum s s' < ltime s' p div 100 \<Rightarrow> getPstate s p = getPstate s' p"
  apply (induction s' arbitrary:s)
  apply auto
  apply (metis Suc_eq_plus1 getPstate.simps(2) not_less_eq)
  using getPstate.simps apply presburger+
done

lemma inter_toEnvNum_getPstate:
"toEnvNum s s' < ltime s' p div 100 ∧ substate s s'' ∧ substate s'' s'\<Rightarrow> toEnvNum s'' s' < ltime s' p div 100"
  using toEnvNum3 by fastforce
end
