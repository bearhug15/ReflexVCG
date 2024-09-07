theory trafficLightsTheory
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

lemma
"substate s1 s2 \<and> timeContinuous s1 s2 p \<Longrightarrow> (ltime s2 p - ltime s1 p) div 100 = toEnvNum s1 s2"
  by

lemma 
"\<forall>s1 s. toEnvP s \<and> toEnvP s1 \<and> substate s1 s \<and>toEnvNum s1 s < ltime s x div 100 \<longrightarrow> getPstate s x = getPstate s1 x"
proof (intro allI;rule impI)
  fix s1 s
  assume prem:"toEnvP s \<and> toEnvP s1 \<and> substate s1 s \<and> toEnvNum s1 s < ltime s x div 100"
  then obtain n where
    o1:"toEnvNum s1 s = n" by auto
  then have 1:"n < ltime s x div 100" using prem by auto
  have 2:"s1 = shiftEnv s n" using o1 prem shift_toEnvNum by force
  show "getPstate s x = getPstate s1 x"
  proof -
    fix s2
    assume "substate s1 s2 \<and> substate s2 s \<and> s2 = setPstate s3 x v \<and> getPstate s x = v"
    then have 3:"ltime s2 x = 0" by auto
    have 4:" "
  
    
    
    

qed
(*
abbreviation T1:: nat where
 "T1 \<equiv> 150"
abbreviation T2:: nat where "T2 \<equiv> 30"
abbreviation T3:: nat where "T3 \<equiv> 100"
*)
(*definition extra0 where
"extra0 s \<equiv>
  (\<forall> s1. toEnvP s1 \<and> substate s1 s \<longrightarrow> 
    getPstate s1 ''Controller'' \<in> {''minimalRed'', ''redAfterMinimalRed'', ''redToGreen'', ''green''})"

definition extra1 where
"extra1 s \<equiv> 
  (\<forall> s1. toEnvP s1 \<and> substate s1 s \<and> getPstate s1 ''Controller'' = ''minimalRed'' \<longrightarrow> 
     ltime s1 ''Controller'' \<le> 10000)"

definition extra2 where
"extra2 s \<equiv> 
  (\<forall> s1. toEnvP s1 \<and> substate s1 s \<and> getPstate s1 ''Controller'' = ''redToGreen'' \<longrightarrow> 
    ltime s1 ''Controller'' \<le> 5000)"

definition extra3 where
"extra3 s \<equiv> 
  (\<forall> s1. toEnvP s1 \<and> substate s1 s \<and> getPstate s1 ''Controller'' = ''green'' \<longrightarrow> 
    getVarBool s1 ''light'' = True \<and> getVarBool s1 ''_bpressed'' = False \<and> ltime s1 ''Controller'' \<le> 3000)"

definition extra4 where
"extra4 s \<equiv>
  (\<forall> s1. toEnvP s1 \<and> substate s1 s \<and> getPstate s1 ''Controller'' \<noteq> ''green'' \<longrightarrow> 
    getVarBool s1 ''light'' = False)"

definition extra5 where
"extra5 s \<equiv>
(\<forall> s5. toEnvP s5 \<and> substate s5 s \<and> 
  getPstate s5 ''Controller'' \<noteq> ''green'' \<longrightarrow>
    (\<forall> s1 s2. toEnvP s1 \<and> toEnvP s2 \<and> 
      substate s1 s2 \<and> substate s2 s5 \<and> 
      toEnvNum s1 s2 = 1 \<and>
      getVarBool s1 ''light'' \<noteq> True \<and> getVarBool s2 ''light'' = True \<longrightarrow>
        (\<exists> s4. toEnvP s4 \<and> 
          substate s2 s4 \<and> substate s4 s5 \<and> 
          toEnvNum s2 s4 \<le> T2+T3 \<and> getVarBool s4 ''light'' = False \<and>
          (\<forall> s3. toEnvP s3 \<and> 
            substate s2 s3 \<and> substate s3 s4 \<and> s3 \<noteq> s4 \<longrightarrow> 
              getVarBool s3 ''light'' = True))))"

definition extra6 where
"extra6 s \<equiv>
(\<forall> s5. toEnvP s5 \<and> substate s5 s \<and> 
  getPstate s5 ''Controller'' = ''green'' \<longrightarrow>
    (\<forall> s1 s2. toEnvP s1 \<and> toEnvP s2 \<and> 
      substate s1 s2 \<and> substate s2 s5 \<and> 
      toEnvNum s1 s2 = 1 \<and>
      getVarBool s1 ''light'' \<noteq> True \<and> getVarBool s2 ''light'' = True \<longrightarrow>
        (\<exists> s4. toEnvP s4 \<and> 
          substate s2 s4 \<and> substate s4 s5 \<and> 
          toEnvNum s2 s4 \<le> T2+T3 \<and> getVarBool s4 ''light'' = False \<and>
            (\<forall> s3. toEnvP s3 \<and> 
              substate s2 s3 \<and> substate s3 s4 \<and> s3 \<noteq> s4 \<longrightarrow> 
              getVarBool s3 ''light'' = True)) \<or>
        toEnvNum s2 s5 < T3 + ltime s5 ''Controller'' div 100 \<and>
        (\<forall> s3. toEnvP s3 \<and> 
          substate s2 s3 \<and> substate s3 s5 \<longrightarrow> 
            getVarBool s3 ''light'' = True) ))"

definition extra7 where
"extra7 s \<equiv>
(\<forall> s5. toEnvP s5 \<and> substate s5 s \<and> 
  getPstate s5 ''Controller'' = ''green'' \<longrightarrow>
    (\<forall> s1 s2. toEnvP s1 \<and> toEnvP s2 \<and> 
      substate s1 s2 \<and> substate s2 s5 \<and> 
      toEnvNum s1 s2 = 1 \<and> getVarBool s1 ''light'' = False \<and> 
      getVarBool s1 ''button'' = False \<and> getVarBool s2 ''button'' = True \<longrightarrow>
        (\<exists> s4. toEnvP s4 \<and> 
          substate s2 s4 \<and> substate s4 s5 \<and> 
          toEnvNum s2 s4 \<le> T1 \<and> getVarBool s4 ''light'' = True \<and>
          (\<forall> s3. toEnvP s3 \<and> 
            substate s2 s3 \<and> substate s3 s4 \<and> s3 \<noteq> s4 \<longrightarrow> 
              getVarBool s3 ''light'' = False))))"

definition extra8 where
"extra8 s \<equiv> 
(\<forall> s5. toEnvP s5 \<and> substate s5 s \<and> 
  getPstate s5 ''Controller'' = ''redToGreen'' \<longrightarrow>
    (\<forall> s1 s2. toEnvP s1 \<and> toEnvP s2 \<and> 
      substate s1 s2 \<and> substate s2 s5 \<and> toEnvNum s1 s2 = 1 \<and>
      getVarBool s1 ''light'' = False \<and> getVarBool s1 ''button'' = False \<and> 
      getVarBool s2 ''button'' = True \<longrightarrow>
        (\<exists> s4. toEnvP s4 \<and> 
          substate s2 s4 \<and> substate s4 s5 \<and> 
          toEnvNum s2 s4 \<le> T1 \<and> getVarBool s4 ''light'' = True \<and>
          (\<forall> s3. toEnvP s3 \<and> 
            substate s2 s3 \<and> substate s3 s4 \<and> s3 \<noteq> s4 \<longrightarrow> 
              getVarBool s3 ''light'' = False)) \<or>
        toEnvNum s2 s5 < 100  + ltime s5 ''Controller''div 100 \<and>
        (\<forall> s3. toEnvP s3 \<and> 
          substate s2 s3 \<and> substate s3 s5 \<longrightarrow> 
            getVarBool s3 ''light'' = False)))"

definition extra9 where
"extra9 s \<equiv> 
(\<forall> s5. toEnvP s5 \<and> substate s5 s \<and> 
  getPstate s5 ''Controller'' = ''redAfterMinimalRed'' \<and> 
  getVarBool s5 ''_bpressed'' = True \<longrightarrow>
    (\<forall> s1 s2. toEnvP s1 \<and> toEnvP s2 \<and> 
      substate s1 s2 \<and> substate s2 s5 \<and> 
      toEnvNum s1 s2 = 1 \<and> getVarBool s1 ''light'' = False \<and> 
      getVarBool s1 ''button'' = False \<and> getVarBool s2 ''button'' = True \<longrightarrow>
        (\<exists> s4. toEnvP s4 \<and> 
          substate s2 s4 \<and> substate s4 s5 \<and> 
          toEnvNum s2 s4 \<le> T1 \<and> getVarBool s4 ''light'' = True \<and>
          (\<forall> s3. toEnvP s3 \<and> 
            substate s2 s3 \<and> substate s3 s4 \<and> s3 \<noteq> s4 \<longrightarrow> 
              getVarBool s3 ''light'' = False)) \<or>
        toEnvNum s2 s5 < 100 \<and>
        (\<forall> s3. toEnvP s3 \<and> 
          substate s2 s3 \<and> substate s3 s5 \<longrightarrow> 
            getVarBool s3 ''light'' = False) ))"

definition extra10 where
"extra10 s \<equiv> 
(\<forall> s5. toEnvP s5 \<and> 
  substate s5 s \<and> getPstate s5 ''Controller'' = ''minimalRed'' \<and> 
  getVarBool s5 ''_bpressed'' = True \<longrightarrow>
    (\<forall> s1 s2. toEnvP s1 \<and> toEnvP s2 \<and> 
      substate s1 s2 \<and> substate s2 s5 \<and> 
      toEnvNum s1 s2 = 1 \<and> getVarBool s1 ''light'' = False \<and> 
      getVarBool s1 ''button'' = False \<and> 
      getVarBool s2 ''button'' = True \<longrightarrow>
        (\<exists> s4. toEnvP s4 \<and> 
          substate s2 s4 \<and> substate s4 s5 \<and> 
          toEnvNum s2 s4 \<le> T1 \<and> getVarBool s4 ''light'' = True \<and>
          (\<forall> s3. toEnvP s3 \<and> 
            substate s2 s3 \<and> substate s3 s4 \<and> s3 \<noteq> s4 \<longrightarrow> 
              getVarBool s3 ''light'' = False)) \<or>
        toEnvNum s2 s5 < ltime s5 ''Controller'' div 100 - 1 \<and>
        (\<forall> s3. toEnvP s3 \<and> 
          substate s2 s3 \<and> substate s3 s5 \<longrightarrow> 
          getVarBool s3 ''light'' = False)))"

definition extra11 where
"extra11 s \<equiv> 
(\<forall> s5. toEnvP s5 \<and> 
  substate s5 s \<and> getPstate s5 ''Controller'' = ''redAfterMinimalRed'' \<and> 
  getVarBool s5 ''_bpressed'' = False \<longrightarrow>
    (\<forall> s1 s2. toEnvP s1 \<and> toEnvP s2 \<and> 
      substate s1 s2 \<and> substate s2 s5 \<and> toEnvNum s1 s2 = 1 \<and>
      getVarBool s1 ''light'' = False \<and> getVarBool s1 ''button'' = False \<and> 
      getVarBool s2 ''button'' = True \<longrightarrow>
        (\<exists> s4. toEnvP s4 \<and> 
          substate s2 s4 \<and> substate s4 s5 \<and> 
          toEnvNum s2 s4 \<le> T1 \<and> getVarBool s4 ''light'' = True \<and>
          (\<forall> s3. toEnvP s3 \<and> 
            substate s2 s3 \<and> substate s3 s4 \<and> s3 \<noteq> s4 \<longrightarrow> 
              getVarBool s3 ''light'' = False))))"

definition extra12 where
"extra12 s \<equiv> 
(\<forall> s5. toEnvP s5 \<and> 
  substate s5 s \<and> getPstate s5 ''Controller'' = ''minimalRed'' \<and> 
  getVarBool s5 ''_bpressed'' = False \<longrightarrow>
    (\<forall> s1 s2. toEnvP s1 \<and> toEnvP s2 \<and> 
      substate s1 s2 \<and> substate s2 s5 \<and> toEnvNum s1 s2 = 1 \<and>
      getVarBool s1 ''light'' = False \<and> getVarBool s1 ''button'' = False \<and> 
      getVarBool s2 ''button'' = True \<longrightarrow>
        (\<exists> s4. toEnvP s4 \<and> 
          substate s2 s4 \<and> substate s4 s5 \<and> 
          toEnvNum s2 s4 \<le> T1 \<and> getVarBool s4 ''light'' = True \<and>
          (\<forall> s3. toEnvP s3 \<and> 
            substate s2 s3 \<and> substate s3 s4 \<and> s3 \<noteq> s4 \<longrightarrow> 
              getVarBool s3 ''light'' = False))  ))"

definition extra13 where
"extra13 s \<equiv> 
(\<forall> s3. toEnvP s3 \<and> substate s3 s \<and> 
  getPstate s3 ''Controller'' = ''minimalRed'' \<and> 
  getVarBool s3 ''_bpressed'' \<longrightarrow>
  (\<forall> s1 s2. toEnvP s1 \<and> toEnvP s2 \<and> 
    substate s1 s2 \<and> substate s2 s3 \<and> 
    toEnvNum s1 s2 = 1 \<and> getVarBool s1 ''light'' \<noteq> False \<and> 
    getVarBool s2 ''light'' = False \<longrightarrow>
      (\<exists> s4. toEnvP s4 \<and> substate s2 s4 \<and> substate s4 s3 \<and> 
        getVarBool s4 ''button'')))"

definition extra14 where
"extra14 s \<equiv> 
(\<forall> s3. toEnvP s3 \<and> 
  substate s3 s \<and> getPstate s3 ''Controller'' = ''redAfterMinimalRed'' \<and> 
  getVarBool s3 ''_bpressed'' \<longrightarrow>
    (\<forall> s1 s2. toEnvP s1 \<and> toEnvP s2 \<and> 
      substate s1 s2 \<and> substate s2 s3 \<and> 
      toEnvNum s1 s2 = 1 \<and> getVarBool s1 ''light'' \<noteq> False \<and> 
      getVarBool s2 ''light'' = False \<longrightarrow>
        (\<exists> s4. toEnvP s4 \<and> substate s2 s4 \<and> substate s4 s3 \<and> 
          getVarBool s4 ''button'')))"

definition extra15 where
"extra15 s \<equiv> 
(\<forall> s3. toEnvP s3 \<and> 
  substate s3 s \<and> getPstate s3 ''Controller'' = ''redToGreen'' \<longrightarrow>
    (\<forall> s1 s2. toEnvP s1 \<and> toEnvP s2 \<and> 
      substate s1 s2 \<and> substate s2 s3 \<and> 
      toEnvNum s1 s2 = 1 \<and> getVarBool s1 ''light'' \<noteq> False \<and> 
      getVarBool s2 ''light'' = False \<longrightarrow>
        (\<exists> s4. toEnvP s4 \<and> substate s2 s4 \<and> substate s4 s3 \<and> 
          getVarBool s4 ''button'')))"

definition extraInv where "extraInv s \<equiv> toEnvP s \<and>
extra0 s \<and>
extra1 s \<and>
extra2 s \<and>
extra3 s \<and>
extra4 s \<and>
extra5 s \<and>
extra6 s \<and>
extra7 s \<and>
extra8 s \<and>
extra9 s \<and>
extra10 s\<and>
extra11 s\<and>
extra12 s\<and>
extra13 s\<and>
extra14 s\<and>
extra15 s"
*)

definition extra0 where
"extra0 s \<equiv>
  (\<forall> s1. toEnvP s1 \<and> substate s1 s \<longrightarrow> 
    getPstate s1 ''Controller'' \<in> {''minimalRed'', ''redAfterMinimalRed'', ''redToGreen'', ''green''})"

definition extra1 where
"extra1 s \<equiv> 
  (\<forall> s1. toEnvP s1 \<and> substate s1 s \<and> getPstate s1 ''Controller'' = ''minimalRed'' \<longrightarrow> 
     100\<le>ltime s1 ''Controller'' \<and> ltime s1 ''Controller'' \<le> 10000)"

definition extra2 where
"extra2 s \<equiv> 
  (\<forall> s1. toEnvP s1 \<and> substate s1 s \<and> getPstate s1 ''Controller'' = ''redToGreen'' \<longrightarrow> 
    100\<le> ltime s1 ''Controller'' \<and> ltime s1 ''Controller'' \<le> 5000)"
definition extra2_2 where
"extra2_2 s \<equiv> 
  (\<forall> s1. toEnvP s1 \<and> substate s1 s \<and> getPstate s1 ''Controller'' = ''redAfterMinimalRed'' \<longrightarrow> 
    100\<le> ltime s1 ''Controller'')"

definition extra3 where
"extra3 s \<equiv> 
  (\<forall> s1. toEnvP s1 \<and> substate s1 s \<and> getPstate s1 ''Controller'' = ''green'' \<longrightarrow> 
    100 \<le> ltime s1 ''Controller'' \<and> getVarBool s1 ''light'' = True \<and> 
    getVarBool s1 ''_bpressed'' = False \<and> ltime s1 ''Controller'' \<le> 30000)"

definition extra4 where
"extra4 s \<equiv>
  (\<forall> s1. toEnvP s1 \<and> substate s1 s \<and> getPstate s1 ''Controller'' \<noteq> ''green'' \<longrightarrow> 
    getVarBool s1 ''light'' = False)"

definition extra5 where
"extra5 s \<equiv>
(\<forall> s1. toEnvP s1 \<and> substate s1 s \<and> 
  getPstate s1 ''Controller'' = ''minimalRed'' \<longrightarrow>
   ((\<exists> s2. toEnvP s2 \<and> substate s2 s1 \<and> 
      toEnvNum s2 s1 = ltime s1 ''Controller'' div 100 \<and>
      getPstate s2 ''Controller'' = ''green'' \<and> ltime s2 ''Controller'' = 30000) \<or>
    (\<forall> s2. toEnvP s2 \<and> substate s2 s1 \<longrightarrow> 
      getPstate s2 ''Controller'' = ''minimalRed'')))"

definition extra6 where
"extra6 s \<equiv>
(\<forall> s1 s2. toEnvP s1 \<and> toEnvP s2 \<and> 
  substate s1 s2 \<and> substate s2 s \<and> 
  getPstate s2 ''Controller'' = ''minimalRed'' \<and>
  toEnvNum s1 s2 < ltime s2 ''Controller'' div 100 \<longrightarrow> 
    getPstate s1 ''Controller'' = ''minimalRed'')"

definition extra7 where
"extra7 s \<equiv>
(\<forall> s1 s2. toEnvP s1 \<and> toEnvP s2 \<and> 
  substate s1 s2 \<and> substate s2 s \<and>
  (\<forall> s3. toEnvP s3 \<and> substate s1 s3 \<and> substate s3 s2 \<longrightarrow> 
    getPstate s3 ''Controller'' = ''minimalRed'' ) \<longrightarrow>
    toEnvNum s1 s2 = (ltime s2 ''Controller'' - ltime s1 ''Controller'') div 100)"

definition extra8 where
"extra8 s \<equiv>
(\<forall> s1 . toEnvP s1 \<and> substate s1 s \<and> 
  getPstate s1 ''Controller'' = ''redAfterMinimalRed'' \<and>
  getVarBool s1 ''_bpressed'' = False \<longrightarrow>
    (\<exists> s2. toEnvP s2 \<and> substate s2 s1 \<and> 
      getPstate s2 ''Controller'' = ''minimalRed'' \<and> 
      ltime s2 ''Controller'' = 10000 \<and> getVarBool s2 ''_bpressed'' = False \<and>
      (\<forall> s3. toEnvP s3 \<and> substate s2 s3 \<and> substate s3 s1 \<and> s2 \<noteq> s3 \<longrightarrow>
        getPstate s3 ''Controller'' = ''redAfterMinimalRed'' \<and> 
        getVarBool s3 ''_bpressed'' = False \<and> getVarBool s3  ''button'' = False)))"

definition extra9 where
"extra9 s \<equiv>
(\<forall> s1. toEnvP s1 \<and> substate s1 s \<and> 
  getPstate s1 ''Controller'' = ''redToGreen'' \<longrightarrow>
    (\<exists> s2. toEnvP s2  \<and> substate s2 s1 \<and>
      toEnvNum s2 s1 = ltime s1 ''Controller'' div 100\<and> 
      getPstate s2 ''Controller'' = ''redAfterMinimalRed'' \<and> 
      (getVarBool s2 ''_bpressed'' = True \<or> getVarBool s2 ''button'' = True)))"

definition extra10 where
"extra10 s \<equiv>
(\<forall> s1 s2. toEnvP s1 \<and> toEnvP s2 \<and> 
  substate s1 s2 \<and> substate s2 s \<and> 
  toEnvNum s1 s2 < ltime s2 ''Controller'' div 100 \<and>
  getPstate s2 ''Controller'' = ''redToGreen'' \<longrightarrow> 
    getPstate s1 ''Controller'' = ''redToGreen'') "

definition extra11 where
"extra11 s \<equiv>
(\<forall> s1 s2. toEnvP s1 \<and> toEnvP s2 \<and> 
  substate s1 s2 \<and> substate s2 s \<and>
  (\<forall> s3. toEnvP s3 \<and> substate s1 s3 \<and> substate s3 s2 \<longrightarrow> 
    getPstate s3 ''Controller'' = ''redToGreen'') \<longrightarrow>
    toEnvNum s1 s2 = (ltime s2 ''Controller'' - ltime s1 ''Controller'') div 100)"

definition extra12 where
"extra12 s \<equiv>
(\<forall> s1. toEnvP s1 \<and> substate s1 s \<and> 
  getPstate s1 ''Controller'' = ''green'' \<longrightarrow>
    (\<exists> s2. toEnvP s2 \<and> substate s2 s1 \<and> 
      toEnvNum s2 s1 = ltime s1 ''Controller'' div 100 \<and>
      getPstate s2 ''Controller'' = ''redToGreen'' \<and> 
      ltime s2 ''Controller'' \<ge> 5000)) "

definition extra13 where
"extra13 s \<equiv>
(\<forall> s1 s2. toEnvP s1 \<and> toEnvP s2 \<and> 
  substate s1 s2 \<and> substate s2 s \<and> 
  getPstate s2 ''Controller'' = ''green'' \<and>
  toEnvNum s1 s2 < ltime s2 ''Controller'' div 100 \<longrightarrow> 
    getPstate s1 ''Controller'' = ''green'')"

definition extra14 where
"extra14 s \<equiv>
(\<forall> s1 s2. toEnvP s1 \<and> toEnvP s2 \<and> 
  substate s1 s2 \<and> substate s2 s \<and> 
  (\<forall> s3. toEnvP s3 \<and> substate s1 s3 \<and> substate s3 s2 \<longrightarrow> 
    getPstate s3 ''Controller'' = ''green'')\<longrightarrow>
    toEnvNum s1 s2 = (ltime s2 ''Controller'' - ltime s1 ''Controller'')div 100)"

definition extra15 where
"extra15 s \<equiv>
(\<forall> s1 s2. toEnvP s1 \<and> toEnvP s2 \<and> 
  substate s1 s2 \<and> substate s2 s \<and>
  toEnvNum s1 s2 < ltime s2 ''Controller'' div 100 - 1 \<and>  
  getPstate s2 ''Controller'' = ''minimalRed'' \<and>
  getVarBool s2 ''_bpressed'' = False \<longrightarrow> 
    getVarBool s1 ''button'' = False)"

definition extra16 where
"extra16 s \<equiv>
(\<forall> s2. toEnvP s2  \<and> substate s2 s  \<and>
 getPstate s2 ''Controller'' = ''minimalRed'' \<and> 
  getVarBool s2 ''_bpressed'' = True \<longrightarrow>
    (\<exists> s1. toEnvP s1 \<and> substate s1 s2 \<and> 
      toEnvNum s1 s2 < ltime s2 ''Controller'' div 100 - 1 \<and> 
      getVarBool s1 ''button'' = True))"

definition extra17 where
"extra17 s \<equiv>
(\<forall> s1. toEnvP s1 \<and> substate s1 s \<and> 
  getPstate s1 ''Controller'' = ''redAfterMinimalRed'' \<and> 
  getVarBool s1 ''_bpressed'' \<longrightarrow>
    (\<exists> s2. toEnvP s2 \<and> substate s2 s1 \<and> 
      toEnvNum s2 s1 = 1 \<and> getPstate s2 ''Controller'' = ''minimalRed'' \<and>
      ltime s2 ''Controller'' = 10000 \<and>
      getVarBool s2 ''_bpressed''))"

definition extra18 where
"extra18 s \<equiv>
(\<forall> s1 s2. toEnvP s1 \<and> toEnvP s2 \<and> 
  substate s1 s2 \<and> substate s2 s \<and>
  getPstate s2 ''Controller'' = ''redToGreen'' \<and>
  toEnvNum s1 s2 \<le> 100 + 1 + (ltime s2 ''Controller'' div 100) \<longrightarrow>
  getVarBool s1 ''light''=False)"

definition extra19 where
"extra19 s \<equiv>
(\<forall> s1 s2. toEnvP s1 \<and> toEnvP s2 \<and>
  substate s1 s2 \<and> substate s2 s \<and>
  toEnvNum s1 s2 = 1 \<and> 
  getPstate s1 ''Controller''= ''green'' \<longrightarrow>
    getPstate s2 ''Controller''= ''green'' \<or> (getPstate s2 ''Controller''= ''minimalRed'' \<and> ltime s2 ''Controller'' div 100 =1))"

definition extra20 where
"extra20 s \<equiv>
(\<forall> s1 s2. toEnvP s1 \<and> toEnvP s2 \<and>
  substate s1 s2 \<and> substate s2 s \<and>
  toEnvNum s1 s2 = 1 \<and> 
  getPstate s2 ''Controller''= ''redToGreen'' \<longrightarrow>
    getPstate s1 ''Controller''= ''redToGreen'' \<or> getPstate s1 ''Controller''= ''redAfterMinimalRed'')"

definition extra21 where
"extra21 s \<equiv>
(\<forall>s2. toEnvP s2 \<and> substate s2 s \<and> getPstate s2 ''Controller'' = ''redToGreen''\<longrightarrow>
  (\<exists>s1. toEnvP s1 \<and> substate s1 s2 \<and> getVarBool s1 ''button'' \<and> 
    (\<forall>s3. toEnvP s3 \<and> substate s1 s3 \<and> substate s3 s2 \<and> getPstate s3 ''Controller'' \<noteq>''green'')))"

definition extra22 where
"extra22 s \<equiv>
(\<forall> s1 s2. toEnvP s1 \<and> toEnvP s2 \<and> 
  substate s1 s2 \<and> substate s2 s \<and>
  getPstate s2 ''Controller'' = ''redAfterMinimalRed'' \<and>
  toEnvNum s1 s2 \<le> 100 +(ltime s2 ''Controller'' div 100) \<longrightarrow>
  getVarBool s1 ''light''=False)"

definition extraInv where "extraInv s \<equiv> toEnvP s \<and>
extra0 s \<and>
extra1 s \<and>
extra2 s \<and>
extra2_2 s\<and>
extra3 s \<and>
extra4 s \<and>
extra5 s \<and>
extra6 s \<and>
extra7 s \<and>
extra8 s \<and>
extra9 s \<and>
extra10 s\<and>
extra11 s\<and>
extra12 s\<and>
extra13 s\<and>
extra14 s\<and>
extra15 s\<and>
extra16 s\<and>
extra17 s\<and>
extra18 s\<and>
extra19 s\<and>
extra20 s\<and>
extra21 s\<and>
extra22 s"

(*
<<<<<<< HEAD
P8 1)  Если горел красный свет и кнопку нажали, то не более, чем через Tr загорится зеленый свет
P3 2)  Если только что загорелся зеленый, то зеленый будет гореть не менее Tg секунд.
P8 3)  Если только что загорелся зеленый, то за время не более Tg+Т3 он переключится на красный
P9 4)  Если только что загорелся красный, то он горит постоянно, пока не нажмут на кнопку
P3 5)  Если загорелся красный, то красный будет гореть не менее Tr секунд
=======
1)  Если горел красный свет и кнопку нажали, то не более, чем через Tr загорится зеленый свет
2)  Если только что загорелся зеленый, то зеленый будет гореть не менее Tg секунд.
3)  Если только что загорелся зеленый, то за время не более Tg+Т3 он переключится на красный
4)  Если только что загорелся красный, то он горит постоянно, пока не нажмут на кнопку
5)  Если загорелся красный, то красный будет гореть не менее Tr секунд
>>>>>>> c7da1483a357cd17d86ed81087ede1ab76c50480
*)

definition R1 where 
"R1 s \<equiv> toEnvP s \<and> 
(\<forall> s1 s2. toEnvP s1 \<and> toEnvP s2 \<and>
  substate s1 s2 \<and> substate s2 s \<and> 
  toEnvNum s1 s2 = 1 \<and> toEnvNum s2 s \<ge> 150 \<and>
  getVarBool s1 ''light'' = False \<and> getVarBool s1 ''button'' = False \<and>
  getVarBool s2 ''button'' = True \<longrightarrow>
    (\<exists> s4. toEnvP s4 \<and> 
      substate s2 s4 \<and> substate s4 s\<and>
      toEnvNum s2 s4 \<le> 150 \<and> getVarBool s4 ''light'' = True \<and>
      (\<forall> s3. toEnvP s3 \<and> substate s2 s3 \<and> substate s3 s4\<and> s3 \<noteq> s4 \<longrightarrow>
        getVarBool s3 ''light'' = False)))"

definition R1_A where
"R1_A s s2 s1 \<equiv>
  toEnvNum s2 s \<ge> 150 \<and>
  getVarBool s1 ''light'' = False \<and> 
  getVarBool s1 ''button'' = False \<and>
  getVarBool s2 ''button'' = True"

definition R1_B where
"R1_B s2 s1 s4 \<equiv> 
  toEnvNum s2 s4 \<le> 150 \<and> 
  getVarBool s4 ''light'' = True"

definition R1_C where
"R1_C s2 s4 s3 \<equiv>
  s3 \<noteq> s4 \<longrightarrow> getVarBool s3 ''light'' = False"


lemma R1_to_P8_cons:
"R1 s \<Longrightarrow> P8_cons R1_A R1_B R1_C s"
  apply(simp add: P8_cons_def R1_A_def R1_B_def R1_C_def SMT.verit_bool_simplify(4) R1_def)
  by blast

lemma P8_cons_to_R1:
"toEnvP s \<and> P8_cons R1_A R1_B R1_C s \<Longrightarrow> R1 s"
  apply(simp add: P8_cons_def R1_A_def R1_B_def R1_C_def SMT.verit_bool_simplify(4) R1_def)
  by blast

(*P8_cons*)

definition R2 where 
"R2 s \<equiv> toEnvP s \<and>
(\<forall> s1 s2 s3. toEnvP s1 \<and> toEnvP s2 \<and> toEnvP s3 \<and> 
  substate s1 s2 \<and> substate s2 s3 \<and> substate s3 s \<and> 
  toEnvNum s1 s2 = 1 \<and> toEnvNum s2 s3 < 300 \<and>
  getVarBool s1 ''light'' \<noteq> True \<and>
  getVarBool s2 ''light'' = True \<longrightarrow>
    getVarBool s3 ''light'' = True)"

definition R2_A where
"R2_A s3 s2 s1 \<equiv>
  toEnvNum s2 s3 < 300 \<and>
  getVarBool s1 ''light'' \<noteq> True \<and>
  getVarBool s2 ''light'' = True \<longrightarrow>
    getVarBool s3 ''light'' = True"


lemma R2_to_P3_cons:
"R2 s \<Longrightarrow> P3_cons R2_A s"
  apply (simp only: P3_cons_def R2_A_def SMT.verit_bool_simplify(4))
  using R2_def by blast

lemma P3_cons_to_R2:
"toEnvP s \<and> P3_cons R2_A s \<Longrightarrow> R2 s"
  apply (simp only: P3_cons_def R2_A_def SMT.verit_bool_simplify(4))
  using R2_def by blast


definition R3 where 
"R3 s \<equiv> toEnvP s \<and> 
(\<forall> s1 s2. toEnvP s1 \<and> toEnvP s2 \<and> 
  substate s1 s2 \<and> substate s2 s \<and> 
  toEnvNum s1 s2 = 1 \<and> toEnvNum s2 s \<ge> 30+100 \<and>
  getVarBool s1 ''light'' \<noteq> True \<and> getVarBool s2 ''light'' = True \<longrightarrow>
    (\<exists> s4. toEnvP s4 \<and> 
      substate s2 s4 \<and> substate s4 s\<and>
      toEnvNum s2 s4 \<le> 30+100 \<and> getVarBool s4 ''light'' = False \<and>
      (\<forall> s3. toEnvP s3 \<and> substate s2 s3 \<and> substate s3 s4 \<and> s3 \<noteq> s4 \<longrightarrow>
        getVarBool s3 ''light'' = True)))"

(*
4)  Если только что загорелся красный, то он горит постоянно, пока не нажмут на кнопку
*)


definition R4 where 
"R4 s \<equiv> toEnvP s \<and>
(\<forall> s1 s2 s3. toEnvP s1 \<and> toEnvP s2 \<and> toEnvP s3 \<and> 
  substate s1 s2 \<and> substate s2 s3 \<and> substate s3 s \<and> 
  toEnvNum s1 s2 = 1 \<and> 
  getVarBool s1 ''light'' \<noteq> False \<and> getVarBool s2 ''light'' = False \<and>
  (\<forall> s4. toEnvP s4 \<and> substate s2 s4 \<and> substate s4 s\<longrightarrow> 
    getVarBool s4 ''button'' = False) \<longrightarrow>
      getVarBool s3 ''light'' = False)"

definition R4_A where
"R4_A s s2 s1 \<equiv> getVarBool s1 ''light'' \<noteq> False \<and> getVarBool s2 ''light'' = False"
definition R4_B where
"R4_B s4 \<equiv> getVarBool s4 ''button'' = False"
definition R4_C where
"R4_C s3 \<equiv> getVarBool s3 ''light'' = False"

lemma R4_to_P9_cons:
"R4 s \<Longrightarrow> P9_cons R4_A R4_B R4_C s"
  apply (simp only: P9_cons_def R4_A_def R4_B_def R4_C_def)
  using R4_def by blast


lemma P9_cons_to_R4:
"toEnvP s \<and> P9_cons R4_A R4_B R4_C s \<Longrightarrow> R4 s"
  apply (simp only: P9_cons_def R4_A_def R4_B_def R4_C_def)
  using R4_def by blast

definition R5 where 
"R5 s \<equiv> toEnvP s \<and>
(\<forall> s1 s2 s3. toEnvP s1 \<and> toEnvP s2 \<and> toEnvP s3 \<and> 
  substate s1 s2 \<and> substate s2 s3 \<and> substate s3 s \<and> 
  toEnvNum s1 s2 = 1 \<and> toEnvNum s2 s3 < 150 \<and>
  getVarBool s1 ''light'' \<noteq> False \<and> getVarBool s2 ''light'' = False \<longrightarrow>
    getVarBool s3 ''light'' = False)"

definition R5_A where
"R5_A s3 s2 s1 \<equiv>
  toEnvNum s2 s3 < 150 \<and>
  getVarBool s1 ''light'' \<noteq> False \<and> 
  getVarBool s2 ''light'' = False \<longrightarrow>
    getVarBool s3 ''light'' = False"

lemma R5_to_P3_cons:
"R5 s \<Longrightarrow> P3_cons R5_A s"
  apply (simp only: P3_cons_def R5_A_def SMT.verit_bool_simplify(4))
  using R5_def by blast

lemma P3_cons_to_R5:
"toEnvP s \<and> P3_cons R5_A s \<Longrightarrow> R5 s"
  apply (simp only: P3_cons_def R5_A_def SMT.verit_bool_simplify(4))
  using R5_def by blast

definition inv1 where "inv1 s \<equiv> R1 s \<and> extraInv s"
definition inv2 where "inv2 s \<equiv> R2 s \<and> extraInv s"
definition inv3 where "inv3 s \<equiv> R3 s \<and> extraInv s"
definition inv4 where "inv4 s \<equiv> R4 s \<and> extraInv s"
definition inv5 where "inv5 s \<equiv> R5 s \<and> extraInv s"

end
