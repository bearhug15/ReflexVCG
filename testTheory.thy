theory testTheory
imports Reflex
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

end
