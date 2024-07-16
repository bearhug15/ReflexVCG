theory HandDryerTheory
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

lemma toEnvNum_getPstate:
"toEnvNum s s' < ltime s' p div 100 \<Longrightarrow> getPstate s p = getPstate s' p"
  apply (induction s' arbitrary:s)
  apply auto
  apply (metis Suc_eq_plus1 getPstate.simps(2) not_less_eq)
  using getPstate.simps apply presburger+
done

lemma inter_toEnvNum_getPstate:
"toEnvNum s s' < ltime s' p div 100 \<and> substate s s'' \<and> substate s'' s'\<Longrightarrow> toEnvNum s'' s' < ltime s' p div 100"
  using toEnvNum3 by fastforce

(*
lemma
"toEnvP s \<and> toEnvP s' \<and> timeContinuous s s' p \<and> s1\<noteq>s \<and> s1\<noteq>s' \<and> substate s s1 \<and> substate s1 s'\<Longrightarrow>
   \<not>(\<exists>s2. s1 = (reset s2 p) \<or> s1 = (setPstate s2 p name))"
  apply  
*)

(*
lemma 
"toEnvP s \<and> toEnvP s' \<and> timeContinuous s s' p \<Longrightarrow>
  toEnvNum s s' = ltime s' p div 100 - ltime s p div 100"
  apply (induction s' arbitrary:s)
         apply auto
  apply try
*)



lemma ltime_after_toEnvNum:
"toEnvP s \<and> toEnvP s' \<and> ltime s p div 100 = x \<and> substate s s' \<and>toEnvNum s s' = y \<Longrightarrow> ltime s' p div 100 \<le> x + y"
  by sorry



definition extra1 where
"extra1 s = 
(\<forall>s2. toEnvP s2 \<and> substate s2 s \<and> 
  getPstate s2 ''Dryer'' = ''Work'' \<longrightarrow>
    (0 < ltime s2 ''Dryer'' div 100 \<and> ltime s2 ''Dryer'' div 100 \<le> 20 \<and>
    (\<forall> s1. toEnvP s1 \<and> substate s1 s2 \<and>
     (toEnvNum s1 s2+1) = ltime s2 ''Dryer'' div 100 \<longrightarrow>
        getVarBool s1 ''inp_1'' = True \<and> 
        getVarBool s1 ''out_1'' = True) \<and>
    (\<forall> s1. toEnvP s1 \<and> substate s1 s2 \<and>
     (toEnvNum s1 s2+1) < ltime s2 ''Dryer'' div 100\<longrightarrow>
        getVarBool s1 ''inp_1'' = False \<and>
        getVarBool s1 ''out_1'' = True)))"

definition extra2 where
"extra2 s =
(\<forall> s1. toEnvP s1 \<and> substate s1 s \<and>
  getPstate s1 ''Dryer'' = ''Wait'' \<longrightarrow>
  ((\<forall> s2. toEnvP s2 \<and> substate s2 s1 \<longrightarrow> 
      getPstate s2 ''Dryer'' = ''Wait'') \<or>
  (\<exists> s2. toEnvP s2 \<and> substate s2 s1 \<and>
    getPstate s2 ''Dryer'' = ''Work'' \<and> ltime s2 ''Dryer'' div 100 \<ge>20 \<and>
    (\<forall> s3. toEnvP s3 \<and> substate s2 s3 \<and> substate s3 s1 \<and>
      s3\<noteq>s2 \<longrightarrow> getPstate s3 ''Dryer'' = ''Wait''))))"

definition extra3 where
"extra3 s = 
(\<forall>s1. toEnvP s1 \<and> substate s1 s \<and> 
  getPstate s1 ''Dryer'' = ''Work'' \<longrightarrow>
    (\<exists>s2 s3. toEnvP s2 \<and> toEnvP s3 \<and> 
      toEnvNum s2 s3 = 1 \<and>substate s3 s1 \<and>
      getPstate s2 ''Dryer'' = ''Wait'' \<and> 
      getPstate s3 ''Dryer'' = ''Work'' \<and>
      (\<forall>s4. toEnvP s4 \<and> substate s3 s4 \<and> substate s4 s1 \<longrightarrow> 
        getPstate s4 ''Dryer'' = ''Work'')))"

definition extra4 where
"extra4 s =
(\<forall>s1 s2. toEnvP s1 \<and> toEnvP s2 \<and> 
  toEnvNum s1 s2 = 1 \<and> substate s2 s \<and>
  getPstate s1 ''Dryer'' = ''Work'' \<and> getVarBool s1 ''inp_1'' = True \<longrightarrow>
  getPstate s2 ''Dryer'' = ''Work'' \<and> ltime s2 ''Dryer'' div 100 = 1)"

definition extra5 where
"extra5 s = 
(\<forall> s1 s2. toEnvP s2 \<and> toEnvP s1 \<and> 
  substate s1 s2 \<and> substate s2 s \<and> 
  getPstate s2 ''Dryer'' = ''Work'' \<and> getPstate s1 ''Dryer'' = ''Work'' \<and>
  (toEnvNum s1 s2+1) > ltime s2 ''Dryer'' div 100 \<longrightarrow>
    ((\<exists> s3. toEnvP s3 \<and> substate s1 s3 \<and> substate s3 s2 \<and>
      getVarBool s3 ''inp_1'' = True \<and>
      (\<forall> s4. toEnvP s4 \<and> substate s1 s4 \<and> substate s4 s3 \<and> s4 \<noteq> s3 \<longrightarrow>
        getVarBool s3 ''inp_1'' = False \<and> getVarBool s3 ''out_1'' = True)) \<or>
    (\<exists> s3 s4. toEnvP s3 \<and> toEnvP s4 \<and> 
      substate s1 s3 \<and> substate s3 s4 \<and> substate s4 s2 \<and>
      toEnvNum s3 s4 = 1 \<and> getPstate s3 ''Dryer'' = ''Work'' \<and>
      getPstate s4 ''Dryer'' = ''Wait'')))"

definition extraInv where
"extraInv s \<equiv>
toEnvP s \<and>
extra1 s \<and>
extra2 s \<and>
extra3 s \<and>
extra4 s \<and>
extra5 s \<and>
(\<forall> s1. toEnvP s1 \<and> substate s1 s \<and> 
getPstate s1 ''Dryer'' = ''Wait'' \<longrightarrow>
 getVarBool s1 ''out_1'' = False) \<and>

(\<forall> s1. toEnvP s1 \<and> substate s1 s \<and> 
getPstate s1 ''Dryer'' = ''Work'' \<longrightarrow>
 getVarBool s1 ''out_1'' = True)\<and>

(\<forall> s1. toEnvP s1 \<and> substate s1 s \<longrightarrow>
getPstate s1 ''Dryer'' = ''Wait'' \<or>
getPstate s1 ''Dryer'' = ''Work'')"


(*
P8   1) Тепловентилятор должен включиться за время, приемлемое с точки зрения пользователя (не позже 0.2 секунды), после того, как под ним появляются руки. 
P2_1 2) Тепловентилятор никогда не включается произвольно (Если нет рук и тепловентилятор не включен, то он не включится до тех пор, пока не появятся руки). 
P8   3) Если руки убрали, то не более чем через 1 с тепловентилятор выключится, если руки за это время не появились вновь. 
P2_1 4) Если руки имеются и тепловентилятор включен, он не выключится. 
P2 (Но вообще паттерна нет) 5) Время непрерывной работы тепловентилятора не более часа
*)

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

definition R1_full where
"R1_full s \<equiv>
toEnvP s \<and>
(\<forall> s1 s2.
  substate s1 s2 \<and>
  substate s2 s \<and> 
  toEnvP s1 \<and> 
  toEnvP s2 \<and>
  toEnvNum s1 s2 = 1 \<and>
  getVarBool s1 ''inp_1'' = False \<and>
  getVarBool s1 ''out_1'' = False \<and>
  getVarBool s2 ''inp_1'' = True \<longrightarrow>
    (\<exists>s4. 
      toEnvP s4 \<and> 
      substate s2 s4 \<and> 
      substate s4 s \<and>
      toEnvNum s2 s4 \<le> 1 \<and> 
      getVarBool s4 ''out_1'' = True \<and>
      (\<forall> s3. 
        toEnvP s3 \<and> 
        substate s2 s3 \<and>
        substate s3 s4 \<and> 
        s3 \<noteq> s4 \<longrightarrow>
          getVarBool s3 ''inp_1'' = True)))"

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

definition inv1 where "inv1 s \<equiv> R1_full s \<and> extraInv s"

definition R1_A where
"R1_A s s2 s1 \<equiv>
  getVarBool s1 ''inp_1'' = False \<and>
  getVarBool s1 ''out_1'' = False \<and>
  getVarBool s2 ''inp_1'' = True"

definition R1_B where
"R1_B s2 s1 s4 \<equiv>
  toEnvNum s2 s4 \<le> 1 \<and> 
  getVarBool s4 ''out_1'' = True"

definition R1_C where
"R1_C s2 s4 s3 \<equiv>
  s3 \<noteq> s4 \<longrightarrow>
  getVarBool s3 ''inp_1'' = True"

lemma R1_to_P8:"R1_full s\<Longrightarrow> P8_cons R1_A R1_B R1_C s"
  apply (simp only:R1_full_def P8_cons_def R1_A_def R1_B_def R1_C_def SMT.verit_bool_simplify(4))
  by auto

lemma P8_to_R1:"toEnvP s \<and> P8_cons R1_A R1_B R1_C s \<Longrightarrow> R1_full s"
  apply (simp only:R1_full_def P8_cons_def R1_A_def R1_B_def R1_C_def SMT.verit_bool_simplify(4))
  by auto

subsection "Requirement 2"

definition R2_sub2_prems where
"R2_sub2_prems s s1 s2 \<equiv>
  substate s1 s2 \<and>
  substate s2 s \<and> 
  toEnvP s1 \<and> 
  toEnvP s2 \<and>
  toEnvNum s1 s2 = 1 \<and> 
  \<not>getVarBool s1 ''out_1''\<and>
  \<not>getVarBool s2 ''inp_1''"

definition R2_sub1 where
"R2_sub1 s \<equiv> 
(\<forall>s1 s2.  R2_sub2_prems s s1 s2 \<longrightarrow>
 \<not>getVarBool s2 ''out_1'')"

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

definition R2_full where
"R2_full s \<equiv> 
toEnvP s \<and>
(\<forall>s1 s2.  
  substate s1 s2 \<and>
  substate s2 s \<and> 
  toEnvP s1 \<and> 
  toEnvP s2 \<and>
  toEnvNum s1 s2 = 1 \<and> 
  getVarBool s1 ''out_1'' = False \<and>
  getVarBool s2 ''inp_1'' = False \<longrightarrow>
    getVarBool s2 ''out_1'' = False)"

definition inv2 where "inv2 s \<equiv> R2_full s \<and> extraInv s"

definition R2_A where
"R2_A s2 s1 \<equiv>
  getVarBool s1 ''out_1'' = False \<and>
  getVarBool s2 ''inp_1'' = False \<longrightarrow>
  getVarBool s2 ''out_1'' = False"

lemma R2_to_P2_1:
"R2_full s \<Longrightarrow> P2_1_cons R2_A s"
  using R2_full_def P2_1_cons_def R2_A_def by auto

lemma P2_1_to_R2:
"toEnvP s \<and> P2_1_cons R2_A s \<Longrightarrow> R2_full s"
  using R2_full_def P2_1_cons_def R2_A_def by auto

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

definition R3_full where
"R3_full s \<equiv>
toEnvP s \<and>
(\<forall>s1 s2. toEnvP s1 \<and> toEnvP s2 \<and>
   substate s1 s2 \<and>substate s2 s \<and>
   toEnvNum s1 s2 = 1 \<and> 20 \<le> toEnvNum s2 s \<and>
   getVarBool s1 ''inp_1'' = True \<and> getVarBool s1 ''out_1'' = True \<and> 
   getVarBool s2 ''inp_1'' = False \<longrightarrow>
     (\<exists>s4. toEnvP s4 \<and>
        substate s2 s4 \<and> substate s4 s \<and>
        toEnvNum s2 s4 \<le> 20 \<and>
        (getVarBool s4 ''out_1'' = False \<or> getVarBool s4 ''inp_1'' = True) \<and>
        (\<forall>s3. toEnvP s3 \<and> substate s2 s3 \<and> substate s3 s4 \<and> s3 \<noteq> s4 \<longrightarrow>
           getVarBool s3 ''out_1'' = True \<and> getVarBool s3 ''inp_1'' = False)))"

definition inv3 where "inv3 s \<equiv> R3_full s \<and> extraInv s"

definition R3_A where
"R3_A s s2 s1 \<equiv>
  20 \<le> toEnvNum s2 s \<and>
  getVarBool s1 ''inp_1'' = True \<and> 
  getVarBool s1 ''out_1'' = True \<and> 
  getVarBool s2 ''inp_1'' = False"

definition R3_B where
"R3_B s2 s1 s4 \<equiv>
  toEnvNum s2 s4 \<le> 20 \<and>
  (getVarBool s4 ''out_1'' = False \<or> 
    getVarBool s4 ''inp_1'' = True)"

definition R3_C where
"R3_C s2 s4 s3 \<equiv>
  s3 \<noteq> s4 \<longrightarrow>
    getVarBool s3 ''out_1'' = True \<and> 
    getVarBool s3 ''inp_1'' = False"

lemma R3_to_P8:
"R3_full s \<Longrightarrow> P8_cons R3_A R3_B R3_C s"
  apply(simp only: R3_full_def P8_cons_def R3_A_def R3_B_def R3_C_def)
  apply blast
  done

lemma P8_to_R3:
"toEnvP s \<and> P8_cons R3_A R3_B R3_C s \<Longrightarrow> R3_full s"
  apply(simp only: R3_full_def P8_cons_def R3_A_def R3_B_def R3_C_def)
  apply blast
  done

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

definition R4_full where
"R4_full s \<equiv> toEnvP s \<and>
    (\<forall>s1 s2.
        substate s1 s2 \<and>
        substate s2 s \<and>
        toEnvP s1 \<and>
        toEnvP s2 \<and>
        toEnvNum s1 s2 = 1 \<and>
        getVarBool s1 ''out_1'' = True \<and>
        getVarBool s2 ''inp_1'' = True \<longrightarrow>
        getVarBool s2 ''out_1'' = True)"

definition inv4 where "inv4 s \<equiv> R4_full s \<and> extraInv s"

definition R4_A where
"R4_A s2 s1 \<equiv>
getVarBool s1 ''out_1'' = True \<and> getVarBool s2 ''inp_1'' = True \<longrightarrow>
  getVarBool s2 ''out_1'' = True"

lemma R4_to_P2_1:
"R4_full s \<Longrightarrow> P2_1_cons R4_A s"
  using R4_full_def P2_1_cons_def R4_A_def by auto

lemma P2_1_to_R4:
"toEnvP s \<and> P2_1_cons R4_A s \<Longrightarrow> R4_full s"
  using R4_full_def P2_1_cons_def R4_A_def by auto

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

definition R5_full where
"R5_full s \<equiv>
toEnvP s \<and>  
(\<forall> s1 s3. 
  substate s1 s3 \<and> 
  substate s3 s \<and>
  toEnvNum s1 s3*100 > 3600000 \<longrightarrow>
    (\<exists> s2. substate s1 s2 \<and> substate s2 s3 \<and>
      getVarBool s2 ''out_1'' = False))"

definition inv5 where "inv5 s \<equiv> R5_full s \<and> extraInv s"


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
то между s5 и s существует состояние s4 такое что нахожится не далее чем в 20 циклах и в нем сушилка выключенна, а руки есть,
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
