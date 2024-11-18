theory RevolvingDoorTheory
imports Reflex ReflexPatterns
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

section "Extra"

definition extraControllerStates where
"extraControllerStates s \<equiv>
(\<forall> s1. 
  toEnvP s1 \<and> 
  substate s1 s \<longrightarrow>
    getPstate s1 ''Controller'' = ''motionless'' \<or>
    getPstate s1 ''Controller'' = ''rotating'' \<or>
    getPstate s1 ''Controller'' = ''suspended'')"

definition extraMotionlessOut where
"extraMotionlessOut s \<equiv>
(\<forall> s1.
  toEnvP s1 \<and> 
  substate s1 s \<and>
  getPstate s1 ''Controller'' = ''motionless'' \<longrightarrow>
  \<not>getVarBool s1 ''rotation'' \<and> 
  \<not>getVarBool s1 ''brake'')"

definition extraRotatingOut where
"extraRotatingOut s \<equiv>
(\<forall> s1. 
  toEnvP s1 \<and> 
  substate s1 s \<and> 
  getPstate s1 ''Controller'' = ''rotating'' \<longrightarrow>
    getVarBool s1 ''rotation'' \<and> 
    \<not>getVarBool s1 ''brake'' \<and>
    ltime s1 ''Controller'' \<le> 1000)"

definition extraSuspendedOut where
"extraSuspendedOut s \<equiv>
(\<forall> s1. 
  toEnvP s1 \<and> 
  substate s1 s \<and> 
  getPstate s1 ''Controller'' = ''suspended'' \<longrightarrow>
    \<not>getVarBool s1 ''rotation'' \<and> 
    getVarBool s1 ''brake'' \<and>
    ltime s1 ''Controller'' \<le> 1000)"

definition extra1 where
"extra1 s \<equiv>
(\<forall> s1.
  toEnvP s1 \<and>
  substate s1 s \<and>
  getPstate s1 ''Controller'' = ''motionless'' \<longrightarrow>
    (\<forall> s2.
      toEnvP s2 \<and>
      substate s2 s1 \<longrightarrow>
      getPstate s2 ''Controller'' = ''motionless'')\<or>
    (\<exists> s2 s3.
      toEnvP s2 \<and>
      toEnvP s3 \<and>
      substate s2 s3 \<and>
      substate s3 s1 \<and>
      toEnvNum s2 s3 = 1 \<and>
      getPstate s2 ''Controller'' = ''rotating'' \<and>
      ltime s2 ''Controller'' \<ge> 1000 \<and>
      \<not> getVarBool s3 ''user'' \<and>
      \<not> getVarBool s3 ''pressure'' \<and>
      (\<forall> s4 s5.
        toEnvP s4 \<and> 
        toEnvP s5 \<and> 
        substate s3 s4 \<and> 
        substate s4 s5 \<and> 
        substate s5 s1 \<and>
        toEnvNum s4 s5 = 1 \<longrightarrow>
          getPstate s4 ''Controller'' = ''motionless'' \<and>
          \<not> getVarBool s5 ''user'')))"


definition extra1s where
"extra1s s \<equiv>
(\<forall> s1.
  toEnvP s1 \<and>
  substate s1 s \<and>
  getPstate s1 ''Controller'' = ''motionless'' \<longrightarrow>
    (\<forall> s2.
      toEnvP s2 \<and>
      substate s2 s1 \<longrightarrow>
      getPstate s2 ''Controller'' = ''motionless'')\<or>
    (\<exists> s2 s3.
      toEnvP s2 \<and>
      toEnvP s3 \<and>
      substate s2 s3 \<and>
      substate s3 s1 \<and>
      toEnvNum s2 s3 = 1 \<and>
      getPstate s2 ''Controller'' = ''rotating'' \<and>
      ltime s2 ''Controller'' \<ge> 1000 \<and>
      \<not> getVarBool s3 ''user'' \<and>
      \<not> getVarBool s3 ''pressure'' \<and>
      (\<forall> s4 s5.
        toEnvP s4 \<and> 
        toEnvP s5 \<and> 
        substate s3 s4 \<and> 
        substate s4 s5 \<and> 
        substate s5 s1 \<and>
        toEnvNum s4 s5 = 1 \<longrightarrow>
          getPstate s4 ''Controller'' = ''motionless'' \<and>
          \<not> getVarBool s5 ''user'')))"

definition extra1_Q1::"state\<Rightarrow>bool" where
"extra1_Q1 s1 \<equiv>
  getPstate s1 ''Controller'' = ''motionless'' \<longrightarrow>
    (\<forall> s2.
      toEnvP s2 \<and>
      substate s2 s1 \<longrightarrow>
      getPstate s2 ''Controller'' = ''motionless'')\<or>
    (\<exists> s2 s3.
      toEnvP s2 \<and>
      toEnvP s3 \<and>
      substate s2 s3 \<and>
      substate s3 s1 \<and>
      toEnvNum s2 s3 = 1 \<and>
      getPstate s2 ''Controller'' = ''rotating'' \<and>
      ltime s2 ''Controller'' \<ge> 1000 \<and>
      \<not> getVarBool s3 ''user'' \<and>
      \<not> getVarBool s3 ''pressure'' \<and>
      (\<forall> s4 s5.
        toEnvP s4 \<and> 
        toEnvP s5 \<and> 
        substate s3 s4 \<and> 
        substate s4 s5 \<and> 
        substate s5 s1 \<and>
        toEnvNum s4 s5 = 1 \<longrightarrow>
          getPstate s4 ''Controller'' = ''motionless'' \<and>
          \<not> getVarBool s5 ''user''))"

lemma P1_extra1_Q1_st0:
"extra1 st0\<Longrightarrow>P1 extra1_Q1 st0"
  using extra1_def  apply auto
  by (smt (verit) One_nat_def P1_def extra1_Q1_def)
(*
lemma P2_inv1_Q2_eq_R1:
"toEnvP st_final \<and> P2 inv1_Q2 st_final \<Longrightarrow>R1 st_final"
  using P2_def inv1_Q2_def including R1_defs apply auto
  done
*)
definition extra2 where
"extra2 s \<equiv>
(\<forall> s1. toEnvP s1 \<and> substate s1 s \<and>
  getPstate s1 ''Controller'' = ''rotating'' \<longrightarrow>
  (\<exists> s2 s3. toEnvP s2 \<and> toEnvP s3 \<and> 
    substate s2 s3 \<and> substate s3 s1 \<and> 
    toEnvNum s2 s3 = 1 \<and>
    toEnvNum s2 s1 = ltime s1 ''Controller'' div 100 \<and>
    ((getPstate s2 ''Controller'' = ''motionless'' \<or> 
        getPstate s2 ''Controller'' = ''rotating'') \<and>
      getVarBool s3 ''user'' \<or> 
      getPstate s2 ''Controller'' = ''suspended'') \<and> 
    \<not>getVarBool s3 ''pressure''))"

definition extra2_A where
"extra2_A s s1 \<equiv> getPstate s1 ''Controller'' = ''rotating''"

definition extra2_B where
"extra2_B s1 s3 s2 \<equiv> 
  toEnvNum s2 s1 = ltime s1 ''Controller'' div 100 \<and>
    ((getPstate s2 ''Controller'' = ''motionless'' \<or> 
        getPstate s2 ''Controller'' = ''rotating'') \<and>
      getVarBool s3 ''user'' \<or> 
      getPstate s2 ''Controller'' = ''suspended'') \<and> 
    \<not>getVarBool s3 ''pressure''"

lemma extra2:"extra2 s = P5_1_cons extra2_A extra2_B s" 
  by (simp only: extra2_def P5_1_cons_def extra2_A_def extra2_B_def)
(*
lemma "(toEnvP s2 \<and> s2 = predEnv s3 \<and> extra2_B s1 s3 s2) = p_2_3_conpred_notEmpty extra2_B s1 s3" 
proof 
  assume "(toEnvP s2 \<and> s2 = predEnv s3 \<and> extra2_B s1 s3 s2)"
  thus "p_2_3_conpred_notEmpty extra2_B s1 s3" by force
next
  assume prem:"p_2_3_conpred_notEmpty extra2_B s1 s3"
  thus "(toEnvP s2 \<and> s2 = predEnv s3 \<and> extra2_B s1 s3 s2)"
  proof -
    obtain s4 where o1:"s4 = predEnv s3" by auto
    then have 1:"s4  \<noteq> emptyState\<and>
              toEnvNum s4 s1 = ltime s1 ''Controller'' div 100 \<and>
              ((getPstate s4 ''Controller'' = ''motionless'' \<or>
                getPstate s4 ''Controller'' = ''rotating'') \<and>
               getVarBool s3 ''user'' \<or>
               getPstate s4 ''Controller'' = ''suspended'') \<and>
              \<not> getVarBool s3 ''pressure''" using prem p_2_3_conpred_notEmpty extra2_B_def o1  by auto
    have 2: "toEnvP s4" using o1 1 using predEnvP_or_emptyState by blast
    thus ?thesis using extra2_B_def 1 2 predEnv_det o1 by sledgehammer
(*    apply (simp only:p_2_3_conpred_notEmpty extra2_B_def)*) 
*)

definition extra3 where
"extra3 s \<equiv>
(\<forall> s1. 
  toEnvP s1 \<and> 
  substate s1 s \<and> 
  getPstate s1 ''Controller'' = ''rotating'' \<longrightarrow>
    (\<exists> s2 s3. 
      toEnvP s2 \<and> 
      toEnvP s3 \<and> 
      substate s2 s3 \<and> 
      substate s3 s1 \<and> 
      toEnvNum s2 s3 = 1 \<and> 
      (getPstate s2 ''Controller'' = ''motionless'' \<and> 
        getVarBool s3 ''user'' \<or>
        getPstate s2 ''Controller'' = ''suspended'') \<and> 
      \<not> getVarBool s3 ''pressure'' \<and>
      (\<forall> s4. 
        toEnvP s4 \<and> 
        substate s3 s4 \<and> 
        substate s4 s1 \<longrightarrow> 
          getPstate s4 ''Controller'' = ''rotating'')))"

definition extra3_A where
"extra3_A s s1 \<equiv> getPstate s1 ''Controller'' = ''rotating''"

definition extra3_B where 
"extra3_B s1 s3 s2 \<equiv>
(getPstate s2 ''Controller'' = ''motionless'' \<and> 
getVarBool s3 ''user'' \<or>
getPstate s2 ''Controller'' = ''suspended'') \<and> 
\<not> getVarBool s3 ''pressure'' "

definition extra3_C where
"extra3_C s4 \<equiv>
  getPstate s4  ''Controller'' = ''rotating''"

lemma extra3:
"extra3 s = P6_cons extra3_A extra3_B extra3_C s" 
  apply (simp only:extra3_def P6_cons_def extra3_A_def extra3_B_def extra3_C_def)
  by presburger



definition extra4 where
"extra4 s \<equiv>
(\<forall> s1.
  toEnvP s1 \<and>
  substate s1 s \<and>
  getPstate s1 ''Controller'' = ''suspended'' \<longrightarrow>
  (\<exists> s2 s3. 
    toEnvP s2 \<and> 
    toEnvP s3 \<and> 
    substate s2 s3 \<and> 
    substate s3 s1 \<and> 
    toEnvNum s2 s3 = 1 \<and>
    toEnvNum s2 s1 = ltime s1 ''Controller'' div 100 \<and>
    (getPstate s2 ''Controller'' \<noteq> ''motionless'' \<or> 
      getVarBool s3 ''user'') \<and> 
    getVarBool s3 ''pressure''))"

definition extra5 where
"extra5 s \<equiv>
(\<forall> s1. 
  toEnvP s1 \<and> 
  substate s1 s \<and> 
  getPstate s1 ''Controller'' = ''suspended'' \<longrightarrow>
    (\<exists> s2 s3. 
      toEnvP s2 \<and> 
      toEnvP s3 \<and> 
      substate s2 s3 \<and> 
      substate s3 s1 \<and> 
      toEnvNum s2 s3 = 1 \<and> 
      (getPstate s2 ''Controller'' = ''motionless'' \<and> 
        getVarBool s3 ''user'' \<or>
        getPstate s2 ''Controller'' = ''rotating'') \<and> 
      getVarBool s3 ''pressure'' \<and>
      (\<forall> s4. 
        toEnvP s4 \<and> 
        substate s3 s4 \<and> 
        substate s4 s1 \<longrightarrow> 
          getPstate s4 ''Controller'' = ''suspended'')))"

definition extra5_A where
"extra5_A s s1 \<equiv> getPstate s1 ''Controller'' = ''suspended''"

definition extra5_B where 
"extra5_B s1 s3 s2 \<equiv>
(getPstate s2 ''Controller'' = ''motionless'' \<and> 
  getVarBool s3 ''user'' \<or>
  getPstate s2 ''Controller'' = ''rotating'') \<and> 
getVarBool s3 ''pressure'' "

definition extra5_C where
"extra5_C s4 \<equiv>
  getPstate s4 ''Controller'' = ''suspended''"

lemma extra5:
"extra5 s \<equiv> P6_cons extra5_A extra5_B extra5_C s" 
  apply (simp only:extra5_def P6_cons_def extra5_A_def extra5_B_def extra5_C_def)
  by presburger


definition extra6 where
"extra6 s \<equiv>
(\<forall> s1 s2 s3. 
  toEnvP s1 \<and> 
  toEnvP s2 \<and> 
  toEnvP s3 \<and> 
  substate s1 s2 \<and> 
  substate s2 s3 \<and> 
  substate s3 s \<and>
  getPstate s3 ''Controller'' = ''rotating'' \<and> 
  toEnvNum s1 s2 = 1 \<and> 
  toEnvNum s1 s3 < ltime s3 ''Controller'' div 100 \<longrightarrow>
    getPstate s1 ''Controller'' = ''rotating'' \<and> 
    \<not> getVarBool s2 ''user'')"

definition ex6_A3 where
"ex6_A3 s3 s2 s1 \<equiv>
  getPstate s3 ''Controller'' = ''rotating'' \<and> 
  toEnvNum s1 s3 < ltime s3 ''Controller'' div 100 \<longrightarrow>
    getPstate s1 ''Controller'' = ''rotating'' \<and> 
    \<not> getVarBool s2 ''user''"

lemma ex6_A3_sub:
"extra6 s \<Longrightarrow> P3_cons ex6_A3 s"
  using extra6_def P3_cons_def ex6_A3_def by auto

lemma ex6_A3:
"extra6 s \<Longrightarrow> P3_predEnv ex6_A3 s"
  using ex6_A3_sub P3_bridge by auto

lemma A3_ex6_sub:
"P3_cons ex6_A3 s \<Longrightarrow> extra6 s"
  using extra6_def P3_cons_def ex6_A3_def by auto

lemma "P3_predEnv ex6_A3 s \<Longrightarrow> extra6 s"
  using A3_ex6_sub P3_bridge by auto

definition extra7 where
"extra7 s \<equiv>
(\<forall> s1 s2 s3. 
  toEnvP s1 \<and> 
  toEnvP s2 \<and> 
  toEnvP s3 \<and> 
  substate s1 s2 \<and> 
  substate s2 s3 \<and> 
  substate s3 s \<and>
  toEnvNum s1 s2 = 1 \<and> 
  toEnvNum s1 s3 < ltime s3 ''Controller'' div 100 \<and> 
  getPstate s3 ''Controller'' = ''suspended'' \<longrightarrow>
    getPstate s1 ''Controller'' = ''suspended'' \<and> 
    \<not> getVarBool s2 ''pressure'')"

definition extraInv where
"extraInv s \<equiv>
toEnvP s \<and>
extraControllerStates s \<and>
extraMotionlessOut s \<and>
extraRotatingOut s \<and>
extraSuspendedOut s \<and>

extra1 s \<and>
extra2 s \<and>
extra3 s \<and>
extra4 s \<and>
extra5 s \<and>
extra6 s \<and>
extra7 s"

(*
lemma extraInv_cont:
"extraInv s \<and> substate s' s \<and> toEnvP s' \<Longrightarrow> extraInv s'"
  by sorry
*)
(*
Первая большая может быть такой:
  (\<forall> s1.
  toEnvP s1 \<and>
  substate s1 s \<and>
  getPstate s1 ''Controller'' = ''motionless'' \<longrightarrow>
    (\<forall> s2.
      toEnvP s2 \<and>
      substate s2 s1 \<longrightarrow>
      getPstate s2 ''Controller'' = ''motionless'')\<or>
    (\<exists> s2.
      toEnvP s2 \<and>
      toEnvP s3 \<and>
      substate s2 s1 \<and>
      getPstate s2 ''Controller'' = ''rotating'' \<and>
      ltime s2 ''Controller'' \<ge> 1000 \<and>
      (\<forall> s3.
        substate s2 s3 \<and>
        substate s3 s1 \<and>
        s2 \<noteq> s3 \<and>
        getPstate s3 ''Controller'' = ''motionless'')))
*)

bundle extraInv_defs 
begin
declare extraInv_def [simp]
declare extraControllerStates_def [simp]
declare extraMotionlessOut_def [simp]
declare extraRotatingOut_def [simp]
declare extraSuspendedOut_def [simp]
declare extra1_def [simp]
declare extra2_def [simp]
declare extra3_def [simp]
declare extra4_def [simp]
declare extra5_def [simp]
declare extra6_def [simp]
declare extra7_def [simp]
end

(*
P2_1 1. При входе пользователя дверь начинает вращаться, если на перегородки
    не оказывается давление.
P2_1 2. Вращение продолжается, пока пользователь находится внутри пространства 
   вращения, если на перегородки не оказывается давление.
P8   3. Если пользователь покинул пространство вращения, то не более, чем через 
  DELAY секунд вращение остановится, если за это время пользователи 
  не появятся вновь.
P3   4. Если на секционные перегородки оказывается давление, то вращение 
   приостанавливается не менее, чем на SUSPENSION_TIME секунд.
P8   5. Если на секционные перегородки перестали оказывать давление, то не более, 
   чем через SUSPENSION_TIME секунд вращение возобновится.
P1   6. Запрещена одновременная подача сигналов rotation и brake.
*)

section "Requirement 1" 

definition R1_sub2_prems where
"R1_sub2_prems s s1 s2 \<equiv> 
 substate s1 s2 \<and> 
 substate s2 s \<and>
 toEnvP s1 \<and> 
 toEnvP s2 \<and> 
 toEnvNum s1 s2 = 1 \<and>
 \<not> getVarBool s1 ''rotation'' \<and> 
 getVarBool s2 ''user'' \<and> 
 \<not> getVarBool s2 ''pressure''"

definition R1_sub1 where
"R1_sub1 s  \<equiv> 
(\<forall> s1 s2. 
 R1_sub2_prems s s1 s2 \<longrightarrow>
 getVarBool s2 ''rotation'')"

definition R1 where "R1 s \<equiv> 
toEnvP s \<and> 
R1_sub1 s"

bundle R1_defs begin
declare R1_def [simp]
declare R1_sub1_def [simp]
declare R1_sub2_prems_def [simp]
end

definition R1_full where
"R1_full s \<equiv>
toEnvP s \<and> 
(\<forall> s1 s2. 
  substate s1 s2 \<and> 
  substate s2 s \<and>
  toEnvP s1 \<and> 
  toEnvP s2 \<and> 
  toEnvNum s1 s2 = 1 \<and>
  \<not> getVarBool s1 ''rotation'' \<and> 
  getVarBool s2 ''user'' \<and> 
  \<not> getVarBool s2 ''pressure'' \<longrightarrow>
    getVarBool s2 ''rotation'')"

definition R1_A2:: "state\<Rightarrow>state\<Rightarrow>bool"where
"R1_A2 s2 s1 \<equiv> 
  \<not> getVarBool s1 ''rotation'' \<and> 
  getVarBool s2 ''user'' \<and> 
  \<not> getVarBool s2 ''pressure'' \<longrightarrow>
    getVarBool s2 ''rotation''"

lemma R1_A2:
"R1 s\<Longrightarrow>P2_1_cons R1_A2 s"
  using P2_1_cons_def R1_A2_def including R1_defs by auto


lemma A2_R1:
"toEnvP s \<and> P2_1_cons R1_A2 s \<Longrightarrow>R1 s" 
  using P2_1_cons_def R1_A2_def R1_full_def including R1_defs by auto


section "Requirement 2" 

definition R2_sub2_prems where
"R2_sub2_prems s s1 s2 \<equiv> 
 substate s1 s2 \<and> 
 substate s2 s \<and>
 toEnvP s1 \<and> 
 toEnvP s2 \<and> 
 toEnvNum s1 s2 = 1 \<and>
 getVarBool s1 ''rotation'' \<and> 
 getVarBool s2 ''user'' \<and> 
 \<not> getVarBool s2 ''pressure''"

definition R2_sub1 where
"R2_sub1 s  \<equiv> 
(\<forall> s1 s2. 
 R2_sub2_prems s s1 s2 \<longrightarrow>
 getVarBool s2 ''rotation'')"

definition R2 where "R2 s \<equiv> 
toEnvP s \<and> 
R2_sub1 s"

bundle R2_defs begin
declare R2_def [simp]
declare R2_sub1_def [simp]
declare R2_sub2_prems_def [simp]
end

definition R2_full:
"R2_full s \<equiv>
toEnvP s \<and>
    (\<forall>s1 s2.
        substate s1 s2 \<and>
        substate s2 s \<and>
        toEnvP s1 \<and>
        toEnvP s2 \<and>
        toEnvNum s1 s2 = 1 \<and>
        getVarBool s1 ''rotation'' \<and>
        getVarBool s2 ''user'' \<and>
        \<not> getVarBool s2 ''pressure'' \<longrightarrow>
        getVarBool s2 ''rotation'')"

definition R2_A2:: "state\<Rightarrow>state\<Rightarrow>bool"where
"R2_A2 s2 s1 \<equiv> 
  getVarBool s1 ''rotation'' \<and> 
  getVarBool s2 ''user'' \<and> 
  \<not> getVarBool s2 ''pressure'' \<longrightarrow>
    getVarBool s2 ''rotation''"

lemma R2_A2:
"R2 s\<Longrightarrow>P2_1_cons R2_A2 s"
  using P2_1_cons_def R2_A2_def including R2_defs by auto

lemma A2_R2:
"toEnvP s \<and> P2_1_cons R2_A2 s \<Longrightarrow>R2 s" 
  using P2_1_cons_def R2_A2_def  including R2_defs by auto



section "Requirement 3" 

definition R3_sub4_prems where
"R3_sub4_prems s2 s3 s4 \<equiv>
  toEnvP s3 \<and> 
  substate s2 s3 \<and> 
  substate s3 s4 \<and> 
  s3 \<noteq> s4"

definition R3_sub4 where
"R3_sub4 s3 \<equiv>
  getVarBool s3 ''rotation''\<and> 
  \<not> getVarBool s3 ''user''"

definition R3_sub2_prems where
"R3_sub2_prems s s1 s2 \<equiv>
  toEnvP s1 \<and> 
  toEnvP s2 \<and>
  substate s1 s2 \<and> 
  substate s2 s \<and>
  toEnvNum s1 s2 = 1 \<and> 
  toEnvNum s2 s \<ge> 10 \<and>
  getVarBool s1 ''rotation''\<and> 
  \<not> getVarBool s2 ''user''"

definition R3_sub3 where
"R3_sub3 s2 s4 \<equiv>
(\<forall> s3. 
    R3_sub4_prems s2 s3 s4 \<longrightarrow>  
    R3_sub4 s3)"

definition R3_sub2 where
"R3_sub2 s s1 s2 \<equiv>
 (\<exists> s4. 
  toEnvP s4 \<and> 
  substate s2 s4 \<and> 
  substate s4 s \<and> 
  toEnvNum s2 s4 \<le> 10 \<and>
  (getVarBool s4 ''rotation'' = False \<or> getVarBool s4 ''user'') \<and>
  R3_sub3 s2 s4)"

definition R3_sub1 where 
"R3_sub1 s \<equiv>
(\<forall> s1 s2. 
  R3_sub2_prems s s1 s2 \<longrightarrow>
  R3_sub2 s s1 s2)"

definition R3 where "R3 s \<equiv> 
toEnvP s \<and> 
R3_sub1 s"

bundle R3_defs
begin
declare R3_def [simp]
declare R3_sub1_def [simp]
declare R3_sub2_prems_def [simp]
declare R3_sub2_def [simp]
declare R3_sub3_def [simp]
declare R3_sub4_prems_def [simp]
declare R3_sub4_def [simp]
end

definition R3_full where
"R3_full s \<equiv>
toEnvP s \<and> 
(\<forall> s1 s2. 
  toEnvP s1 \<and> 
  toEnvP s2 \<and>
  substate s1 s2 \<and> 
  substate s2 s \<and>
  toEnvNum s1 s2 = 1 \<and> 
  toEnvNum s2 s \<ge> 10 \<and>
  getVarBool s1 ''rotation''\<and> 
  \<not> getVarBool s2 ''user'' \<longrightarrow>
    (\<exists> s4. 
    toEnvP s4 \<and> 
    substate s2 s4 \<and> 
    substate s4 s \<and> 
    toEnvNum s2 s4 \<le> 10 \<and>
    (getVarBool s4 ''rotation'' = False \<or> getVarBool s4 ''user'') \<and>
      (\<forall> s3. 
        toEnvP s3 \<and> 
        substate s2 s3 \<and> 
        substate s3 s4 \<and> 
        s3 \<noteq> s4 \<longrightarrow>  
          getVarBool s3 ''rotation''\<and> 
          \<not> getVarBool s3 ''user'')))"

definition R3_A where
"R3_A s s2 s1 \<equiv>
  toEnvNum s2 s \<ge> 10 \<and>
  getVarBool s1 ''rotation''\<and> 
  \<not> getVarBool s2 ''user''"

definition R3_B where
"R3_B s2 s1 s4 \<equiv>
  toEnvNum s2 s4 \<le> 10 \<and>
    (getVarBool s4 ''rotation'' = False \<or> getVarBool s4 ''user'')"

definition R3_C where
"R3_C s2 s4 s3 \<equiv>
  s3 \<noteq> s4 \<longrightarrow>  
   getVarBool s3 ''rotation''\<and> 
   \<not> getVarBool s3 ''user''"

lemma [simp]:"R3 s = R3_full s" including R3_defs using R3_full_def by auto

lemma R3_to_P8_cons:
"R3_full s \<Longrightarrow> P8_cons R3_A R3_B R3_C s"
  apply (simp only:R3_full_def P8_cons_def R3_A_def R3_B_def R3_C_def)
  using SMT.verit_bool_simplify(4) by force

lemma P8_cons_to_R3:
"toEnvP s \<and> P8_cons R3_A R3_B R3_C s \<Longrightarrow> R3_full s"
  apply (simp only:R3_full_def P8_cons_def R3_A_def R3_B_def R3_C_def)
  using SMT.verit_bool_simplify(4) by force


section "Requirement 4"

definition R4_sub2_prems where
"R4_sub2_prems s s1 s2 s3 \<equiv>
  toEnvP s1 \<and> 
  toEnvP s2 \<and> 
  toEnvP s3 \<and>  
  substate s1 s2 \<and> 
  substate s2 s3 \<and> 
  substate s3 s \<and> 
  toEnvNum s1 s2 = 1 \<and> 
  toEnvNum s2 s3 < 1000 div 100  \<and>
  getVarBool s1 ''rotation'' \<and> 
  getVarBool s2 ''pressure''"

definition R4_sub1 where
"R4_sub1 s \<equiv>
(\<forall> s1 s2 s3.
  R4_sub2_prems s s1 s2 s3 \<longrightarrow> 
    getVarBool s3 ''brake'')"

definition R4 where "R4 s \<equiv> 
toEnvP s \<and> 
R4_sub1 s"

bundle R4_defs
begin
declare R4_def [simp]
declare R4_sub1_def [simp]
declare R4_sub2_prems_def [simp]
end
(*
4. Если на секционные перегородки оказывается давление, то вращение 
   приостанавливается не менее, чем на SUSPENSION_TIME секунд.
*)
definition R4_full where
"R4_full s \<equiv> 
toEnvP s \<and>
(\<forall> s1 s2 s3.
  toEnvP s1 \<and> 
  toEnvP s2 \<and> 
  toEnvP s3 \<and>  
  substate s1 s2 \<and> 
  substate s2 s3 \<and> 
  substate s3 s \<and> 
  toEnvNum s1 s2 = 1 \<and> 
  toEnvNum s2 s3 < 10  \<and>
  getVarBool s1 ''rotation'' \<and> 
  getVarBool s2 ''pressure'' \<longrightarrow> 
    getVarBool s3 ''brake'')"

definition R4_A3 where
"R4_A3 s3 s2 s1 \<equiv>
  toEnvNum s2 s3 < 10 \<and>
  getVarBool s1 ''rotation'' \<and> 
  getVarBool s2 ''pressure'' \<longrightarrow> 
    getVarBool s3 ''brake''"

lemma R4_A3:
"R4_full s \<Longrightarrow>P3_cons R4_A3 s"
  using P3_cons_def R4_A3_def R4_full_def by auto

lemma A3_R4:
"toEnvP s \<and> P3_cons R4_A3 s \<Longrightarrow> R4_full s"
  using P3_cons_def R4_A3_def R4_full_def by auto


section "Requirement 5" 
definition R5_sub4_prems where
"R5_sub4_prems s2 s3 s4 \<equiv>
  toEnvP s3 \<and> 
  substate s2 s3 \<and> 
  substate s3 s4 \<and> 
  s3 \<noteq> s4"

definition R5_sub4 where
"R5_sub4 s3 \<equiv>
  \<not> getVarBool s3 ''rotation''\<and> 
  \<not> getVarBool s3 ''pressure''"

definition R5_sub2_prems where
"R5_sub2_prems s s1 s2 \<equiv>
  toEnvP s1 \<and> 
  toEnvP s2 \<and>
  substate s1 s2 \<and> 
  substate s2 s \<and>
  toEnvNum s1 s2 = 1 \<and> 
  toEnvNum s2 s \<ge> 10 \<and>
  \<not> getVarBool s1 ''rotation''\<and> 
  getVarBool s2 ''pressure''"

definition R5_sub3 where
"R5_sub3 s2 s4 \<equiv>
(\<forall> s3. 
    R5_sub4_prems s2 s3 s4 \<longrightarrow>  
    R5_sub4 s3)"

definition R5_sub2 where
"R5_sub2 s s1 s2 \<equiv>
 (\<exists> s4. 
  toEnvP s4 \<and> 
  substate s2 s4 \<and> 
  substate s4 s \<and> 
  toEnvNum s2 s4 \<le> 10 \<and>
  (getVarBool s4 ''brake''\<or> \<not>getVarBool s4 ''pressure'') \<and>
  R5_sub3 s2 s4)"

definition R5_sub1 where 
"R5_sub1 s \<equiv>
(\<forall> s1 s2. 
  R5_sub2_prems s s1 s2 \<longrightarrow>
  R5_sub2 s s1 s2)"



definition R5 where "R5 s \<equiv> 
toEnvP s \<and> 
R5_sub1 s"

bundle R5_defs
begin
declare R5_def [simp]
declare R5_sub1_def [simp]
declare R5_sub2_prems_def [simp]
declare R5_sub2_def [simp]
declare R5_sub3_def [simp]
declare R5_sub4_prems_def [simp]
declare R5_sub4_def [simp]
end

definition R5_full:
"R5_full s \<equiv>
toEnvP s \<and>
    (\<forall>s1 s2.
        toEnvP s1 \<and>
        toEnvP s2 \<and>
        substate s1 s2 \<and>
        substate s2 s \<and>
        toEnvNum s1 s2 = 1 \<and>
        10 \<le> toEnvNum s2 s \<and>
        \<not> getVarBool s1 ''rotation'' \<and>
        getVarBool s2 ''pressure'' \<longrightarrow>
        (\<exists>s4. toEnvP s4 \<and>
              substate s2 s4 \<and>
              substate s4 s \<and>
              toEnvNum s2 s4 \<le> 10 \<and>
              (getVarBool s4 ''brake'' \<or>
               \<not> getVarBool s4 ''pressure'') \<and>
              (\<forall>s3. toEnvP s3 \<and> substate s2 s3 \<and> 
              substate s3 s4 \<and> s3 \<noteq> s4 \<longrightarrow>
              \<not> getVarBool s3 ''rotation''\<and> 
              \<not> getVarBool s3 ''pressure'')))"

section "Requirement 6" 

definition R6_sub1 where
"R6_sub1 s \<equiv>
 (\<forall> s1. 
  substate s1 s \<and> 
  toEnvP s1 \<longrightarrow>
  (getVarBool s1 ''brake'' \<longrightarrow>
    \<not> getVarBool s1 ''rotation'') \<and>
  (getVarBool s1 ''rotation''  \<longrightarrow>
  \<not> getVarBool s1 ''brake''))"

definition R6_A where
"R6_A s1 == 
(getVarBool s1 ''brake'' \<longrightarrow>
    \<not> getVarBool s1 ''rotation'') \<and>
  (getVarBool s1 ''rotation''  \<longrightarrow>
  \<not> getVarBool s1 ''brake'')"

definition R6 where "R6 s \<equiv> 
toEnvP s \<and>
R6_sub1 s"

lemma R6_A:
"R6 s \<Longrightarrow> P1 R6_A s"
  using R6_def P1_def R6_A_def  R6_sub1_def by blast

lemma A_R6:
"toEnvP s \<and> P1 R6_A s \<Longrightarrow> R6 s"
  using R6_def P1_def R6_A_def  R6_sub1_def SMT.verit_bool_simplify(4) by auto

bundle R6_defs
begin
declare R6_def [simp]
declare R6_sub1_def [simp]

end


definition inv1 where "inv1 s \<equiv> R1 s \<and> extraInv s"
definition inv2 where "inv2 s \<equiv> R2 s \<and> extraInv s"
definition inv3 where "inv3 s \<equiv> R3_full s \<and> extraInv s"
definition inv4 where "inv4 s \<equiv> R4_full s \<and> extraInv s"
definition inv5 where "inv5 s \<equiv> R5 s \<and> extraInv s"
definition inv6 where "inv6 s \<equiv> R6 s \<and> extraInv s"

(*
definition inv1_Q2:: "state\<Rightarrow>state\<Rightarrow>bool"where
"inv1_Q2 s2 s1 \<equiv> 
  toEnvNum s1 s2 = 1 \<and>
  \<not> getVarBool s1 ''rotation'' \<and> 
  getVarBool s2 ''user'' \<and> 
  \<not> getVarBool s2 ''pressure'' \<longrightarrow>
    getVarBool s2 ''rotation''"

lemma P2_inv1_Q2_st0:
"inv1 st0\<Longrightarrow>P2 inv1_Q2 st0"
  using inv1_def including R1_defs apply auto
  by (smt (verit) One_nat_def P2_def inv1_Q2_def)

lemma P2_inv1_Q2_eq_R1:
"toEnvP st_final \<and> P2 inv1_Q2 st_final \<Longrightarrow>R1 st_final"
  using P2_def inv1_Q2_def including R1_defs apply auto
  done
*)







end
