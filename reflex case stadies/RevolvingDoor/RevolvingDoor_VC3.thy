theory RevolvingDoor_VC3
imports RevolvingDoorTheory
begin


definition extra1_1_Q1 :: "state\<Rightarrow>bool" where 
"extra1_1_Q1 x \<equiv> True \<longrightarrow> getPstate x ''Controller'' =''motionless''"

lemma extra1_1_Q1_to_extra1_Q1:
  "getPstate s ''Controller'' = ''motionless'' \<and> P1 extra1_1_Q1 s \<Longrightarrow> extra1_Q1 s"
  using extra1_1_Q1_def extra1_Q1_def P1_def apply auto
  done

definition extra1_2_Q2 ::"state\<Rightarrow>state\<Rightarrow>state\<Rightarrow>bool" where
"extra1_2_Q2 s s3 s2 \<equiv>
      toEnvNum s2 s3 = 1 \<and>
       getPstate s2 ''Controller'' = ''rotating'' \<and>
       1000 \<le> ltime s2 ''Controller'' \<and>
       \<not> getVarBool s3 ''user'' \<and>
       \<not> getVarBool s3 ''pressure'' \<and>
       (\<forall>s4 s5.
           toEnvP s4 \<and>
           toEnvP s5 \<and>
           substate s3 s4 \<and>
           substate s4 s5 \<and>
           substate s5 s \<and> toEnvNum s4 s5 = 1 \<longrightarrow>
           getPstate s4 ''Controller'' = ''motionless'' \<and>
           \<not> getVarBool s5 ''user'')"
(*
lemma extra1_2_Q2_to_extra1_Q1:
"getPstate s ''Controller'' = ''motionless'' \<and> PE2 extra1_2_Q2 s \<Longrightarrow> extra1_Q1 s" 
  using extra1_2_Q2_def extra1_Q1_def PE2_def apply simp
  apply (rule uncurry)
    apply auto
  apply 
  done*)
find_theorems "(_\<longrightarrow>_\<longrightarrow>_) \<Longrightarrow> (_\<and>_\<longrightarrow>_)"
lemma extra:
 assumes base_inv:"(extraInv st0)"
 and st1:"(st1=(setVarBool st0 ''pressure'' pressure))"
 and st2:"(st2=(setVarBool st1 ''user'' user))"
 and st2_Controller_state:"(getPstate st2 ''Controller'')=''motionless''"
 and st2_if3:"(getVarBool st2 ''user'')=False"
 and st3:"(st3=(toEnv st2))"
 and st_final:"(st_final=st3)"
shows "(extraInv st_final)" 
proof (simp only: extraInv_def;intro conjI)
  show "toEnvP st_final" using assms by auto
next
  show "extraControllerStates st_final"
  proof -
    have "extraControllerStates st0" using extraInv_def base_inv by blast
    then show ?thesis using assms extraControllerStates_def by auto
  qed
next
  show "extraMotionlessOut st_final" 
  proof (simp only: extraMotionlessOut_def; rule allI; rule impI)
    fix s1
    assume prems:"toEnvP s1 \<and>
          substate s1 st_final \<and>
          getPstate s1 ''Controller'' = ''motionless''"
    then show "\<not> getVarBool s1 ''rotation'' \<and>
                \<not> getVarBool s1 ''brake''"
    proof (cases)
      assume 1:"s1=st_final"
      have "getPstate st0 ''Controller'' = ''motionless''" using assms by simp
      moreover have "extraMotionlessOut st0" using base_inv extraInv_def by blast
      ultimately have buff:"\<not> getVarBool st0 ''rotation'' \<and> \<not> getVarBool st0 ''brake''" using base_inv extraInv_def extraMotionlessOut_def substate_refl by blast
      then have 2:"\<not> getVarBool st_final ''rotation''" using assms by auto
      have 3:"\<not> getVarBool st_final ''brake''" using assms buff by auto
      show  ?thesis using 1 2 3  by auto
    next
      assume 1:"s1\<noteq>st_final"
      have "predEnv st_final = st0" using assms extraInv_def by auto
      then have 2:"substate s1 st0" using 1 assms prems substate_eq_or_predEnv by blast 
      have "extraMotionlessOut st0" using base_inv extraInv_def by auto
      thus ?thesis using 1 2 prems assms extraInv_def extraMotionlessOut_def[of st0] substate_refl by blast
    qed
  qed
next
  show "extraRotatingOut st_final" 
  proof -
    have "extraRotatingOut st0" using extraInv_def base_inv by blast
    then show ?thesis using assms extraRotatingOut_def by auto
  qed
next
  show "extraSuspendedOut st_final"
  proof -
    have "extraSuspendedOut st0" using extraInv_def base_inv by blast
    then show ?thesis using assms extraSuspendedOut_def by auto
  qed     
next
  show "extra2 st_final"
  proof -
    have "extra2 st0" using base_inv extraInv_def by auto
    thus ?thesis using assms extra2_def by auto
  qed
next
  show "extra3 st_final"
  proof -
    have "extra3 st0" using base_inv extraInv_def by auto
    thus ?thesis using assms extra3_def by auto
  qed
next
  show "extra6 st_final"
  proof -
    have "extra6 st0" using base_inv extraInv_def by auto
    thus ?thesis using assms extra6_def by auto
  qed
next
  show "extra4 st_final"
  proof -
    have "extra4 st0" using base_inv extraInv_def by auto
    thus ?thesis using assms extra4_def by auto
  qed
next
  show "extra5 st_final" 
  proof -
    have "extra5 st0" using base_inv extraInv_def by auto
    thus ?thesis using assms extra5_def by auto
  qed
next
  show "extra7 st_final"
  proof -
    have "extra7 st0" using base_inv extraInv_def by auto
    thus ?thesis using assms extra7_def by auto
  qed
next
  show "extra1 st_final"
  proof -
    have "toEnvP st0 \<and> toEnvP st_final \<and> st0 = predEnv st_final" using assms extraInv_def by auto
    moreover have "P1 extra1_Q1 st0" using P1_extra1_Q1_st0 base_inv extraInv_def by auto
    moreover have "extra1_Q1 st_final" 
    proof -
      have "extra1_Q1 st0" using assms extraInv_def using P1_def calculation(2) substate_refl by blast
      then have "(\<forall> s2.
      toEnvP s2 \<and>
      substate s2 st0 \<longrightarrow>
      getPstate s2 ''Controller'' = ''motionless'')\<or>
    (\<exists> s2 s3.
      toEnvP s2 \<and>
      toEnvP s3 \<and>
      substate s2 s3 \<and>
      substate s3 st0 \<and>
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
        substate s5 st0 \<and>
        toEnvNum s4 s5 = 1 \<longrightarrow>
          getPstate s4 ''Controller'' = ''motionless'' \<and>
          \<not> getVarBool s5 ''user''))" using extra1_Q1_def assms by auto
      then show ?thesis 
      proof (rule disjE)
        assume "\<forall>s2. 
                  toEnvP s2 \<and>
                  substate s2 st0 \<longrightarrow>
                 getPstate s2 ''Controller'' =''motionless''"
        then have "P1 extra1_1_Q1 st0" using P1_def extra1_1_Q1_def by auto
        moreover have "toEnvP st0 \<and> toEnvP st_final \<and> st0 = predEnv st_final" using assms extraInv_def by auto
        moreover have "extra1_1_Q1 st_final" by (simp add: extra1_1_Q1_def st2_Controller_state st3 st_final)
        ultimately have "P1 extra1_1_Q1 st_final" using one_pos_cond by auto
        then show ?thesis using P1_def extra1_1_Q1_def 
          by (simp add: extra1_1_Q1_to_extra1_Q1 st2_Controller_state st3 st_final)
      next
        assume "\<exists>s2 s3.
       toEnvP s2 \<and>
       toEnvP s3 \<and>
       substate s2 s3 \<and>
       substate s3 st0 \<and>
       toEnvNum s2 s3 = 1 \<and>
       getPstate s2 ''Controller'' = ''rotating'' \<and>
       1000 \<le> ltime s2 ''Controller'' \<and>
       \<not> getVarBool s3 ''user'' \<and>
       \<not> getVarBool s3 ''pressure'' \<and>
       (\<forall>s4 s5.
           toEnvP s4 \<and>
           toEnvP s5 \<and>
           substate s3 s4 \<and>
           substate s4 s5 \<and>
           substate s5 st0 \<and> toEnvNum s4 s5 = 1 \<longrightarrow>
           getPstate s4 ''Controller'' = ''motionless'' \<and>
           \<not> getVarBool s5 ''user'')"
        then show ?thesis by sorry
    qed
  qed
  thus ?thesis by sorry
  qed

qed
   


lemma
 assumes base_inv:"(inv1 st0)"
 and st1:"(st1=(setVarBool st0 ''pressure'' pressure))"
 and st2:"(st2=(setVarBool st1 ''user'' user))"
 and st2_Controller_state:"(getPstate st2 ''Controller'')=''motionless''"
 and st2_if3:"(getVarBool st2 ''user'')=False"
 and st3:"(st3=(toEnv st2))"
 and st_final:"(st_final=st3)"
shows "(inv1 st_final)"
proof(simp only: inv1_def; rule conjI)
  show "extraInv st_final" using assms extra inv1_def by auto
next
  show "R1 st_final"
  proof -
    have "toEnvP st0 \<and> toEnvP st_final \<and> st0 = predEnv st_final" using assms inv1_def extraInv_def by auto
    moreover have "P2_predEnv R1_A2 st0" using R1_A2 assms inv1_def by auto
    moreover have "R1_A2 st_final st0" using assms R1_A2_def by auto
    ultimately show ?thesis using two_pos_cond_cons_exp A2_R1 by auto
  qed
qed


lemma
 assumes base_inv:"(inv2 st0)"
 and st1:"(st1=(setVarBool st0 ''pressure'' pressure))"
 and st2:"(st2=(setVarBool st1 ''user'' user))"
 and st2_Controller_state:"(getPstate st2 ''Controller'')=''motionless''"
 and st2_if3:"(getVarBool st2 ''user'')=False"
 and st3:"(st3=(toEnv st2))"
 and st_final:"(st_final=st3)"
shows "(inv2 st_final)"
proof(simp only: inv2_def; rule conjI)
  show "extraInv st_final" using assms extra inv2_def by auto
next
  show "R2 st_final"
  proof -
    have "toEnvP st0 \<and> toEnvP st_final \<and> st0 = predEnv st_final" using assms inv2_def extraInv_def by auto
    moreover have "P2_predEnv R2_A2 st0" using R2_A2 assms inv2_def by auto
    moreover have "R2_A2 st_final st0" using assms R2_A2_def by auto
    ultimately show ?thesis using two_pos_cond_cons_exp A2_R2 by auto
  qed
qed

lemma
 assumes base_inv:"(inv4 st0)"
 and st1:"(st1=(setVarBool st0 ''pressure'' pressure))"
 and st2:"(st2=(setVarBool st1 ''user'' user))"
 and st2_Controller_state:"(getPstate st2 ''Controller'')=''motionless''"
 and st2_if3:"(getVarBool st2 ''user'')=False"
 and st3:"(st3=(toEnv st2))"
 and st_final:"(st_final=st3)"
shows "(inv4 st_final)"
proof(simp only: inv4_def; rule conjI)
  show "extraInv st_final" using assms extra inv4_def by auto
next
  show "R4 st_final"
  proof -
    define inv4_A1 where "inv4_A1 = P3_to_predEnv inv4_A3 st_final"
    moreover have "toEnvP st0 \<and> toEnvP st_final \<and> st0 = predEnv st_final" using assms R4_def inv4_def by auto
    moreover have "P3_predEnv inv4_A3 st0" using assms inv4_def inv4_A3 R4_def by auto
    moreover have "P1 inv4_A1 st0" using assms P1_def inv4_A1_def P3_to_predEnv_def inv4_A3_def by auto
    moreover have "inv4_A1 st_final" using assms inv4_A1_def P3_to_predEnv_def inv4_A3_def by auto
    ultimately show ?thesis using P3_predEnv A3_inv4 by auto
  qed
qed

end
