theory HandDryerTheory
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

lemma ltime_mult:
"ltime s p mod 100 = 0"
  by (induction s) (auto)

lemma ltime_mod:
assumes "ltime s0 p < a*100"
shows "ltime s0 p \<le> (a*100-100)"
proof -
have"(ltime s0 p) mod 100 = 0" by (induction s0) (auto)
thus ?thesis using assms by (induction a) (auto)
qed

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

find_theorems "(_ + _) * _"

subsection "Requirement 1"

definition R1_sub4_prems where
"R1_sub4_prems s2 s3 s4 \<equiv>
  toEnvP s3 \<and> 
  substate s2 s3 \<and>
  substate s3 s4 \<and> 
  s3 \<noteq> s4"

definition R1_sub3 where
"R1_sub3 s2 s4 \<equiv>
(\<forall> s3. 
  R1_sub4_prems s2 s3 s4 \<longrightarrow>
  getVarBool s3 ''inp_1'' = True)"

definition R1_sub3_prems where
"R1_sub3_prems s s2 s4 \<equiv>
  toEnvP s4 \<and> 
  substate s2 s4 \<and> 
  substate s4 s \<and>
  toEnvNum s2 s4 \<le> 1 \<and> 
  getVarBool s4 ''out_1'' = True"

definition R1_sub2 where
"R1_sub2 s s2 \<equiv>
(\<exists>s4. 
  R1_sub3_prems s s2 s4 \<and>
  R1_sub3 s2 s4)"

definition R1_sub2_prems where 
"R1_sub2_prems s s1 s2 \<equiv>
  substate s1 s2 \<and>
  substate s2 s \<and> 
  toEnvP s1 \<and> 
  toEnvP s2 \<and>
  toEnvNum s1 s2 = 1 \<and>
  getVarBool s1 ''inp_1'' = False \<and>
  getVarBool s1 ''out_1'' = False \<and>
  getVarBool s2 ''inp_1'' = True"

definition R1_sub1 where
"R1_sub1 s \<equiv> 
(\<forall> s1 s2.
  R1_sub2_prems s s1 s2 \<longrightarrow>
  R1_sub2 s s2)"

definition R1 where
"R1 s \<equiv>
toEnvP s \<and>
R1_sub1 s"

bundle R1_defs
begin
declare R1_def [simp]
declare R1_sub1_def [simp]
declare R1_sub2_prems_def [simp]
declare R1_sub2_def [simp]
declare R1_sub3_prems_def [simp]
declare R1_sub3_def [simp]
declare R1_sub4_prems_def [simp]
end

subsection "Requirement 2"

definition R2_sub2_prems where
"R2_sub2_prems s s1 s2 \<equiv>
  substate s1 s2 \<and>
  substate s2 s \<and> 
  toEnvP s1 \<and> 
  toEnvP s2 \<and>
  toEnvNum s1 s2 = 1 \<and> 
  getVarBool s1 ''out_1'' = False \<and>
  getVarBool s2 ''inp_1'' = False"

definition R2_sub1 where
"R2_sub1 s \<equiv> 
(\<forall>s1 s2.  R2_sub2_prems s s1 s2 \<longrightarrow>
 getVarBool s2 ''out_1'' = False)"

definition R2 where
"R2 s \<equiv>
  toEnvP s \<and> 
  R2_sub1 s"

bundle R2_defs
begin
declare R2_def [simp]
declare R2_sub1_def [simp]
declare R2_sub2_prems_def [simp]
end

subsection "Requirement 3"

definition R3_sub4_prems where
"R3_sub4_prems s2 s3 s4 \<equiv>
  toEnvP s3 \<and> 
  substate s2 s3 \<and>
  substate s3 s4 \<and> 
  s3 \<noteq> s4"

definition R3_sub3 where
"R3_sub3 s2 s4 \<equiv>
(\<forall> s3. R3_sub4_prems s2 s3 s4 \<longrightarrow>
getVarBool s3 ''out_1'' = True \<and>
getVarBool s3 ''inp_1'' = False)"

definition R3_sub3_prems where
"R3_sub3_prems s s2 s4 \<equiv>
  toEnvP s4 \<and> 
  substate s2 s4 \<and> 
  substate s4 s\<and>
  toEnvNum s2 s4 \<le> 20 \<and> 
  (getVarBool s4 ''out_1'' = False \<or> 
  getVarBool s4 ''inp_1'' = True)"

definition R3_sub2 where
"R3_sub2 s s2 \<equiv>
(\<exists> s4. R3_sub3_prems s s2 s4 \<and>
  R3_sub3 s2 s4)" 

definition R3_sub2_prems where
"R3_sub2_prems s s1 s2 \<equiv>
  substate s1 s2 \<and>
  substate s2 s \<and> 
  toEnvP s1 \<and> 
  toEnvP s2 \<and>
  toEnvNum s1 s2 = 1 \<and> 
  toEnvNum s2 s \<ge> 20 \<and>
  getVarBool s1 ''inp_1'' = True \<and>
  getVarBool s1 ''out_1'' = True \<and>
  getVarBool s2 ''inp_1'' = False"

definition R3_sub1 where 
"R3_sub1 s \<equiv>
(\<forall> s1 s2.  R3_sub2_prems s s1 s2 \<longrightarrow>
  R3_sub2 s s2)"

definition R3 where
"R3 s \<equiv>
toEnvP s \<and>
R3_sub1 s"

bundle R3_defs
begin
declare R3_def [simp]
declare R3_sub1_def [simp]
declare R3_sub2_prems_def [simp]
declare R3_sub2_def [simp]
declare R3_sub3_prems_def [simp]
declare R3_sub3_def [simp]
declare R3_sub4_prems_def [simp]
end

subsection "Requirement 4"

definition R4_sub2_prems where
"R4_sub2_prems s s1 s2 \<equiv>
  substate s1 s2 \<and>
  substate s2 s \<and> 
  toEnvP s1 \<and> 
  toEnvP s2 \<and>
  toEnvNum s1 s2 = 1 \<and> 
  getVarBool s1 ''out_1'' = True \<and>
  getVarBool s2 ''inp_1'' = True"

definition R4_sub1 where
"R4_sub1 s \<equiv>
(\<forall> s1 s2.  
  R4_sub2_prems s s1 s2 \<longrightarrow>
  getVarBool s2 ''out_1'' = True)"

definition R4 where
"R4 s \<equiv>
toEnvP s \<and>
R4_sub1 s"

bundle R4_defs
begin
declare R4_def [simp]
declare R4_sub1_def [simp]
declare R4_sub2_prems_def [simp]
end

subsection "Requirement 5"

definition R5_sub2 where
"R5_sub2 s s1 s3 \<equiv>
(\<exists> s2. substate s1 s2 \<and> substate s2 s3 \<and>
 getVarBool s2 ''out_1'' = False)"

definition R5_sub2_prems where
"R5_sub2_prems s s1 s3 \<equiv>
  substate s1 s3 \<and> 
  substate s3 s \<and>
  toEnvNum s1 s3*100 > 3600000"

definition R5_sub1 where
"R5_sub1 s \<equiv>
(\<forall> s1 s3. R5_sub2_prems s s1 s3 \<longrightarrow>
R5_sub2 s s1 s3)"

definition R5 where
"R5 s \<equiv>
toEnvP s \<and>
R5_sub1 s"


definition extraInv where
"extraInv s \<equiv>
toEnvP s \<and>
(getPstate s ''Dryer'' = ''Work'' \<longrightarrow>
0 < ltime s ''Dryer'' \<and> ltime s ''Dryer'' \<le> 2000 \<and>
(\<forall> s1. toEnvP s1 \<and> substate s1 s \<and>
 (toEnvNum s1 s*100 +100) = ltime s ''Dryer'' \<longrightarrow>
getVarBool s1 ''inp_1'' = True \<and>
 getVarBool s1 ''out_1'' = True) \<and>
(\<forall> s1. toEnvP s1 \<and> substate s1 s \<and>
 (toEnvNum s1 s*100 +100) < ltime s ''Dryer'' \<longrightarrow>
getVarBool s1 ''inp_1'' = False \<and>
 getVarBool s1 ''out_1'' = True)) \<and>
(getPstate s ''Dryer'' = ''Wait'' \<longrightarrow>
 getVarBool s ''out_1'' = False) \<and>
(\<forall> s1. toEnvP s1 \<and> substate s1 s \<longrightarrow>
getPstate s1 ''Dryer'' = ''Wait'' \<or>
getPstate s1 ''Dryer'' = ''Work'')"


definition inv1 where "inv1 s \<equiv> R1 s \<and> extraInv s"


definition inv2 where "inv2 s \<equiv> R2 s \<and> extraInv s"


definition inv3 where "inv3 s \<equiv> R3 s \<and> extraInv s"


definition inv4 where "inv4 s \<equiv> R4 s \<and> extraInv s"


definition inv5 where "inv5 s \<equiv> R5 s \<and> extraInv s"
(*
definition pred3_sub1_prems where 
"pred3_sub1_prems s2 s3 s5 \<equiv>
  toEnvP s3 \<and>
  substate s2 s3 \<and>
  substate s3 s5 \<and> s3 \<noteq> s5"
*)

definition pred3_sub1 where
"pred3_sub1 s2 s5 \<equiv>
  (\<forall>s3. 
  toEnvP s3 \<and>
  substate s2 s3 \<and>
  substate s3 s5 \<and> s3 \<noteq> s5 \<longrightarrow>
  getVarBool s3 ''out_1'' = True \<and>
  getVarBool s3 ''inp_1'' = False)"

definition pred3_sub2_prems where
"pred3_sub2_prems s s2 s4 s5 \<equiv>
  toEnvP s4 \<and>
  substate s5 s4 \<and>
  substate s4 s \<and>
  toEnvNum s2 s4 \<le> 20 \<and>
  (getVarBool s4 ''out_1'' = False \<or>
  getVarBool s4 ''inp_1'' = True)"

definition pred3_sub3 where
"pred3_sub3 s4 s5 \<equiv>
(\<forall>s3. toEnvP s3 \<and>
      substate s5 s3 \<and>
      substate s3 s4 \<and> 
      s3 \<noteq> s4 \<longrightarrow>
      getVarBool s3 ''out_1'' = True \<and>
      getVarBool s3 ''inp_1'' = False)"

definition pred3_sub2 where
"pred3_sub2 s s2 s5 \<equiv>
(\<exists>s4. pred3_sub2_prems s s2 s4 s5 \<and>
      pred3_sub3 s4 s5)"

(*Если для двух состояний s2 s5, для любого состояния s3 между ними сушилка включен, а рук нет, 
то между s5 и s существует состояние s4 такое что находжится не далее чем в 20 циклах и в нем сушилка выключенна, а руки есть,
и для любого s3 между s5 и s4 сушилка работает и рук нет.*)
definition pred3 where
"pred3 s s2 s5 \<equiv>
pred3_sub1 s2 s5 \<longrightarrow>
pred3_sub2 s s2 s5"

bundle pred3_defs begin 
declare pred3_def [simp]
declare pred3_sub1_def [simp]
declare pred3_sub2_def [simp]
declare pred3_sub2_prems_def [simp]
declare pred3_sub3_def [simp]
end

end
