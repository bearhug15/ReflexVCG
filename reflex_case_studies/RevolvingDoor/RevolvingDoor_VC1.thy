theory RevolvingDoor_VC1
imports RevolvingDoorTheory
begin

lemma extra:
 assumes base_inv:"(extraInv st0)"
 and st1:"(st1=(setVarBool st0 ''pressure'' pressure))"
 and st2:"(st2=(setVarBool st1 ''user'' user))"
 and st2_Controller_state:"(getPstate st2 ''Controller'')=''motionless''"
 and st2_if0:"(getVarBool st2 ''user'')=True"
 and st2_if1:"(getVarBool st2 ''pressure'')=True"
 and st3:"(st3=(setVarBool st2 ''brake'' True))"
 and st4:"(st4=(setPstate st3 ''Controller'' ''suspended''))"
 and st5:"(st5=(toEnv st4))"
 and st_final:"(st_final=st5)"
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
  proof -
    have "extraMotionlessOut st0" using extraInv_def base_inv by blast
    then show ?thesis using assms extraMotionlessOut_def by auto
  qed
next
  show "extraRotatingOut st_final" 
  proof -
    have "extraRotatingOut st0" using extraInv_def base_inv by blast
    then show ?thesis using assms extraRotatingOut_def by auto
  qed
next
  show "extraSuspendedOut st_final"
  proof (simp only:extraSuspendedOut_def;rule allI; rule impI)
    fix s1
    assume prems: "toEnvP s1 \<and>
          substate s1 st_final \<and>
          getPstate s1 ''Controller'' =
          ''suspended''"
    then show "\<not> getVarBool s1 ''rotation'' \<and>
          getVarBool s1 ''brake'' \<and>
          ltime s1 ''Controller'' \<le> 1000"
    proof (cases)
      assume 1:"s1=st_final"
      have "getPstate st0 ''Controller'' = ''motionless''" using assms by simp
      moreover have "extraMotionlessOut st0" using base_inv extraInv_def by blast
      ultimately have "\<not> getVarBool st0 ''rotation''" using base_inv extraInv_def extraMotionlessOut_def substate_refl by blast
      then have 2:"\<not> getVarBool st_final ''rotation''" using assms by auto
      have 3:"getVarBool st_final ''brake''" using assms by auto
      have 4:"ltime st_final ''Controller'' \<le> 1000" using assms by auto
      show  ?thesis using 1 2 3 4 by auto
    next
      assume 1: "s1\<noteq>st_final"
      have "predEnv st_final = st0" using assms extraInv_def by auto
      then have 2:"substate s1 st0" using 1 assms prems substate_eq_or_predEnv by blast 
      have "extraSuspendedOut st0" using base_inv extraInv_def by auto
      thus ?thesis using 1 2 prems assms extraInv_def extraSuspendedOut_def[of st0] substate_refl by blast
    qed
  qed
next
  show "extra1 st_final"
  proof -
    have "extra1 st0" using base_inv extraInv_def by auto
    thus ?thesis using assms extra1_def by auto
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
  proof(simp only: extra4_def;rule allI;rule impI)
    fix s1
    assume prems:"toEnvP s1 \<and>
                  substate s1 st_final \<and>
                  getPstate s1 ''Controller'' = ''suspended''"
    then show 
      "(\<exists> s2 s3. 
      toEnvP s2 \<and> 
      toEnvP s3 \<and> 
      substate s2 s3 \<and> 
      substate s3 s1 \<and> 
      toEnvNum s2 s3 = 1 \<and>
      toEnvNum s2 s1 = ltime s1 ''Controller'' div 100 \<and>
      (getPstate s2 ''Controller'' \<noteq> ''motionless'' \<or> 
        getVarBool s3 ''user'') \<and> 
      getVarBool s3 ''pressure'')"
    proof (cases)
      assume 1:"s1=st_final"
      have "toEnvP st0 \<and> 
            toEnvP st_final" using  assms extraInv_def by auto
      moreover have "substate st0 st_final" using assms substate_refl by auto 
      moreover have "substate st_final s1" using 1 substate_refl by auto
      moreover have "toEnvNum st0 st_final =1" using assms toEnvNum_id by auto
      moreover have "toEnvNum st0 s1 = ltime s1 ''Controller'' div 100" using 1 assms toEnvNum_id by auto
      moreover have "getVarBool st_final ''user''" using assms by auto
      moreover have "getVarBool st_final ''pressure''" using assms by auto
      ultimately show ?thesis by auto
    next
      assume 1:"s1\<noteq>st_final"
      have "predEnv st_final = st0" using assms extraInv_def by auto
      then have 2:"substate s1 st0" using 1 assms prems substate_eq_or_predEnv by blast
      have "extra4 st0" using assms extraInv_def by auto
      then show ?thesis using 1 2 prems assms extraInv_def extra4_def by auto
    qed
  qed
next
  show "extra5 st_final" 
  proof(simp only: extra5_def;rule allI;rule impI)
    fix s1
    assume prems:"toEnvP s1 \<and>
          substate s1 st_final \<and>
          getPstate s1 ''Controller''=''suspended''"
    then show "\<exists>s2 s3.
             toEnvP s2 \<and>
             toEnvP s3 \<and>
             substate s2 s3 \<and>
             substate s3 s1 \<and>
             toEnvNum s2 s3 = 1 \<and>
             (getPstate s2 ''Controller'' = ''motionless'' \<and> getVarBool s3 ''user'' \<or>
              getPstate s2 ''Controller'' = ''rotating'') \<and>
             getVarBool s3 ''pressure'' \<and>
             (\<forall>s4. toEnvP s4 \<and> substate s3 s4 \<and> substate s4 s1 \<longrightarrow>
                   getPstate s4 ''Controller'' = ''suspended'')"
    proof (cases)
      assume 1:"s1=st_final"
      have "toEnvP st0 \<and> 
            toEnvP st_final" using  assms extraInv_def by auto
      moreover have "substate st0 st_final" using assms substate_refl by auto 
      moreover have "substate st_final s1" using 1 substate_refl by auto
      moreover have "toEnvNum st0 st_final =1" using assms toEnvNum_id by auto
      moreover have "getPstate st0 ''Controller'' = ''motionless'' \<and> getVarBool st_final ''user''" using assms by auto
      moreover have "getVarBool st_final ''pressure''" using assms by auto
      moreover have "(\<forall>s4. toEnvP s4 \<and> substate st_final s4 \<and> substate s4 s1 \<longrightarrow>getPstate s4 ''Controller'' = ''suspended'')" using assms 1 substate_asym prems by blast
      ultimately show ?thesis by auto
    next
      assume 1:"s1\<noteq>st_final"
      have "predEnv st_final = st0" using assms extraInv_def by auto
      then have 2:"substate s1 st0" using 1 assms prems substate_eq_or_predEnv by blast
      have "extra5 st0" using assms extraInv_def by auto
      then show ?thesis using 1 2 prems assms extraInv_def extra5_def by auto
    qed
  qed
next
  show "extra7 st_final"
  proof (simp only: extra7_def; intro allI; rule impI)
    fix s1 s2 s3
    assume prems:
      "toEnvP s1 \<and> 
  toEnvP s2 \<and> 
  toEnvP s3 \<and> 
  substate s1 s2 \<and> 
  substate s2 s3 \<and> 
  substate s3 st_final \<and>
  toEnvNum s1 s2 = 1 \<and> 
  toEnvNum s1 s3 < ltime s3 ''Controller'' div 100 \<and> 
  getPstate s3 ''Controller'' = ''suspended''"
    then show "getPstate s1 ''Controller'' = ''suspended'' \<and> 
              \<not> getVarBool s2 ''pressure''"
    proof (cases)
      assume 1:"s3=st_final"
      then have "ltime s3 ''Controller'' div 100 = 1" using assms by auto
      then have "toEnvNum s1 s3 =0" using prems by auto
      moreover have "toEnvNum s1 s3 \<ge>1" using prems toEnvNum3 by auto
      ultimately show ?thesis by auto
    next
      assume 1:"s3\<noteq>st_final"
      have "predEnv st_final = st0" using assms extraInv_def by auto
      then have 2:"substate s3 st0" using 1 prems assms extraInv_def substate_eq_or_predEnv by blast
      then show ?thesis using assms prems extraInv_def extra7_def by auto
    qed
  qed
qed
    
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

lemma
 assumes base_inv:"(inv1 st0)"
 and st1:"(st1=(setVarBool st0 ''pressure'' pressure))"
 and st2:"(st2=(setVarBool st1 ''user'' user))"
 and st2_Controller_state:"(getPstate st2 ''Controller'')=''motionless''"
 and st2_if0:"(getVarBool st2 ''user'')=True"
 and st2_if1:"(getVarBool st2 ''pressure'')=True"
 and st3:"(st3=(setVarBool st2 ''brake'' True))"
 and st4:"(st4=(setPstate st3 ''Controller'' ''suspended''))"
 and st5:"(st5=(toEnv st4))"
 and st_final:"(st_final=st5)"
shows "(inv1 st_final)"
proof(simp only: inv1_def; rule conjI)
  show "extraInv st_final" using assms extra inv1_def by auto
next
  show "R1 st_final"
  proof -
    have "toEnvP st0 \<and> toEnvP st_final \<and> st0 = predEnv st_final" using assms inv1_def extraInv_def by auto
    moreover have "P2_1_cons R1_A2 st0" using R1_A2 assms inv1_def by auto
    moreover have "R1_A2 st_final st0" using assms R1_A2_def by auto
    ultimately show ?thesis using P2_1_lemma A2_R1 by auto
  qed
qed



lemma
 assumes base_inv:"(inv2 st0)"
 and st1:"(st1=(setVarBool st0 ''pressure'' pressure))"
 and st2:"(st2=(setVarBool st1 ''user'' user))"
 and st2_Controller_state:"(getPstate st2 ''Controller'')=''motionless''"
 and st2_if0:"(getVarBool st2 ''user'')=True"
 and st2_if1:"(getVarBool st2 ''pressure'')=True"
 and st3:"(st3=(setVarBool st2 ''brake'' True))"
 and st4:"(st4=(setPstate st3 ''Controller'' ''suspended''))"
 and st5:"(st5=(toEnv st4))"
 and st_final:"(st_final=st5)"
shows "(inv2 st_final)"
proof(simp only: inv2_def; rule conjI)
  show "extraInv st_final" using assms extra inv2_def by auto
next
  show "R2 st_final"
  proof -
    have "toEnvP st0 \<and> toEnvP st_final \<and> st0 = predEnv st_final" using assms inv2_def extraInv_def by auto
    moreover have "P2_1_cons R2_A2 st0" using R2_A2 assms inv2_def by auto
    moreover have "R2_A2 st_final st0" using assms R2_A2_def by auto
    ultimately show ?thesis using P2_1_lemma A2_R2 by auto
  qed
qed



lemma
 assumes base_inv:"(inv4 st0)"
 and st1:"(st1=(setVarBool st0 ''pressure'' pressure))"
 and st2:"(st2=(setVarBool st1 ''user'' user))"
 and st2_Controller_state:"(getPstate st2 ''Controller'')=''motionless''"
 and st2_if0:"(getVarBool st2 ''user'')=True"
 and st2_if1:"(getVarBool st2 ''pressure'')=True"
 and st3:"(st3=(setVarBool st2 ''brake'' True))"
 and st4:"(st4=(setPstate st3 ''Controller'' ''suspended''))"
 and st5:"(st5=(toEnv st4))"
 and st_final:"(st_final=st5)"
shows "(inv4 st_final)"
proof(simp only: inv4_def; rule conjI)
  show "extraInv st_final" using assms extra inv4_def by auto
next
  have extra:"extraInv st_final" using assms extra inv4_def by auto
  show "R4_full st_final"
  proof -
    define inv4_A1 where "inv4_A1 \<equiv> (p_2_3_conpred R4_A3) st_final "
    moreover have "toEnvP st0 \<and> toEnvP st_final \<and> st0 = predEnv st_final" using assms R4_full_def inv4_def by auto
    moreover have "P3_cons R4_A3 st0" using assms inv4_def R4_A3 R4_full_def by auto
    moreover have "P1 inv4_A1 st0"
      apply (simp only:P1_def p_2_3_conpred_def R4_A3_def inv4_A1_def SMT.verit_bool_simplify(4))
      using assms by auto
    moreover have "inv4_A1 st_final" 
      apply (simp only: p_2_3_conpred_def R4_A3_def inv4_A1_def SMT.verit_bool_simplify(4))
      using assms by auto
    ultimately show ?thesis using P3_lemma A3_R4 by auto
  qed
qed

lemma
 assumes base_inv:"(inv6 st0)"
 and st1:"(st1=(setVarBool st0 ''pressure'' pressure))"
 and st2:"(st2=(setVarBool st1 ''user'' user))"
 and st2_Controller_state:"(getPstate st2 ''Controller'')=''motionless''"
 and st2_if0:"(getVarBool st2 ''user'')=True"
 and st2_if1:"(getVarBool st2 ''pressure'')=True"
 and st3:"(st3=(setVarBool st2 ''brake'' True))"
 and st4:"(st4=(setPstate st3 ''Controller'' ''suspended''))"
 and st5:"(st5=(toEnv st4))"
 and st_final:"(st_final=st5)"
shows "(inv6 st_final)"
proof(simp only: inv6_def; rule conjI)
  show "extraInv st_final" using assms extra inv6_def by auto
next
  have extra:"extraInv st_final" using assms extra inv6_def by auto
  show "R6 st_final"
  proof -
    have 0:"toEnvP st0 \<and> toEnvP st_final \<and> st0 = predEnv st_final" using assms R6_def inv6_def by auto
    moreover have "P1 R6_A st0" using assms inv6_def R6_A by auto
    moreover have "R6_A st_final" 
      using R6_A_def assms inv6_def extraInv_def extraSuspendedOut_def extraMotionlessOut_def extraControllerStates_def extraRotatingOut_def local.extra substate_refl
      by (smt (verit)) 
    ultimately have "P1 R6_A st_final" using P1_lemma by blast
    thus ?thesis using A_R6 0 by auto
  qed
qed

find_theorems "(_ \<and> _)"
find_theorems "(_ \<and> (_ \<and> _)) = (_ \<and> _ \<and> _)"
find_theorems "(_ \<longrightarrow> _ \<longrightarrow> _) \<Longrightarrow>(_ \<and> _ \<longrightarrow> _) "

lemma
 assumes base_inv:"(inv3 st0)"
 and st1:"(st1=(setVarBool st0 ''pressure'' pressure))"
 and st2:"(st2=(setVarBool st1 ''user'' user))"
 and st2_Controller_state:"(getPstate st2 ''Controller'')=''motionless''"
 and st2_if0:"(getVarBool st2 ''user'')=True"
 and st2_if1:"(getVarBool st2 ''pressure'')=True"
 and st3:"(st3=(setVarBool st2 ''brake'' True))"
 and st4:"(st4=(setPstate st3 ''Controller'' ''suspended''))"
 and st5:"(st5=(toEnv st4))"
 and st_final:"(st_final=st5)"
shows "(inv3 st_final)"
proof(simp only: inv3_def; rule conjI)
  show "extraInv st_final" using assms extra inv3_def by auto
next
  have extra:"extraInv st_final" using assms extra inv3_def by auto
  show "R3_full st_final"
  proof -
    have 0:"toEnvP st0 \<and> toEnvP st_final \<and> st0=predEnv st_final" using assms inv3_def extraInv_def by auto
    moreover have "P8_cons R3_A R3_B R3_C st0" using R3_to_P8_cons assms inv3_def by auto
    moreover have "(P8_ABC_comb R3_A R3_B R3_C) st_final st_final" 
      apply (simp only:P8_ABC_comb R3_A_def R3_B_def R3_C_def AB_comb_after_def P8_BC_comb p_2_3_conpred_def)
      by (simp add: toEnvNum_id)
    moreover have "(\<forall>x. toEnvP x \<and> substate x st_final \<and> (p_2_3_conpred R3_A) st_final x \<and> \<not> (p_2_3_conpred R3_A) st0 x \<longrightarrow> 
          (\<exists>y. toEnvP y \<and> substate x y \<and> substate y st_final \<and> (P8_BC_comb R3_B R3_C) x y))" 
    proof (simp only: p_2_3_conpred_def R3_A_def de_Morgan_conj conj_disj_distribL P8_BC_comb R3_B_def R3_C_def p_1_2_conpred_def SMT.verit_bool_simplify(4); rule allI; rule impI; elim disjE)
      fix x
      assume prem:"toEnvP x \<and>
         substate x st_final \<and>
         (10 \<le> toEnvNum x st_final \<and>
          getVarBool (predEnv x) ''rotation'' \<and>
          \<not> getVarBool x ''user'') \<and>
         \<not> 10 \<le> toEnvNum x st0"
      thus "\<exists>y. toEnvP y \<and> substate x y \<and> substate y st_final \<and> 
              (toEnvNum x y \<le> 10 \<and> getVarBool y ''rotation'' = False \<or> toEnvNum x y \<le> 10 \<and> getVarBool y ''user'') \<and>
             (\<forall>s3. (toEnvP s3 \<and> substate x s3 \<and> substate s3 y) \<and>s3 \<noteq> y \<longrightarrow>
                 getVarBool s3 ''rotation'' \<and> \<not> getVarBool s3 ''user'')"
      proof (cases)
        assume "x = st_final"
        thus ?thesis using prem toEnvNum_id by auto
      next
        assume asm:"\<not> x = st_final"
        then have 1:"substate x st0" using 0 prem substate_eq_or_predEnv by blast
        moreover have "10 >toEnvNum x st0" using prem by auto
        moreover have "toEnvNum st0 st_final = 1" using 0 predEnv_toEnvNum predEnv_substate by auto
        moreover have "substate st0 st_final" using 0 predEnv_substate by auto
        ultimately have "11 >toEnvNum x st_final " using toEnvNum3 by force
        then have buff1:"toEnvNum x st_final = 10" using prem by force
        then have 2:"toEnvNum x st0 = 9" using toEnvNum3 0 predEnv_substate
          using \<open>substate x st0\<close> \<open>toEnvNum st0 st_final = 1\<close> by fastforce
        have 3:"extra1 st0" using assms inv3_def extraInv_def by blast
        have 4:"toEnvP (predEnv x)" using prem predEnvP_or_emptyState by fastforce
        then have 5:"getPstate (predEnv x) ''Controller'' = ''rotating''" using extraInv_def 
          by (meson extraControllerStates_def extraMotionlessOut_def extraSuspendedOut_def local.extra predEnv_substate prem substate_trans)
        have 6:"substate (predEnv x) st0" using 1 predEnv_substate substate_trans by blast
        have predxEnv:"toEnvNum (predEnv x) st0 \<ge>1" using 1
        by (metis "0" "4" "6" less_one linorder_le_less_linear predEnv_substate predEnv_toEnvNum substate_asym substate_toEnvNum_id zero_neq_one)
        have 7:"(getPstate st0 ''Controller'')=''motionless''" using assms by auto
        have "(\<forall> s2.
                    toEnvP s2 \<and>
                    substate s2 st0 \<longrightarrow>
                    getPstate s2 ''Controller'' = ''motionless'') \<or>
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
                        \<not> getVarBool s5 ''user''))" using 0 1 3 7 extra1_def  substate_refl by blast
        then obtain s2 s3 where o1: "(
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
                        \<not> getVarBool s5 ''user''))" 
          using 0 1 4 5 6 by auto
        have 8:"substate (predEnv x) s3 \<or> substate s3 (predEnv x)" using o1 6 substate_total by auto
        have "toEnvP (predEnv x) \<and> toEnvP x \<and> substate s3 (predEnv x) \<and> substate (predEnv x) x \<and> substate x st0 \<longrightarrow> 
                getPstate (predEnv x)''Controller'' = ''motionless''" 
          using o1 predEnv_toEnvNum by blast
        then have "\<not>substate s3 (predEnv x)" using o1 5 4 6 substate_refl 1 8 predEnv_substate prem by force
        then have "substate (predEnv x) s3 \<and> (predEnv x)\<noteq>s3" using 8 by auto
        then have 8:"substate x s3" using predEnv_substate_imp_eq_or_substate 1 o1 prem by auto
        have 9:"R3_full st0" using base_inv inv3_def by auto
        have 10:"(\<forall>y. toEnvP y \<and> substate x y \<and> substate y st_final \<longrightarrow> toEnvNum x y \<le>10)" using 2 0 predEnv_toEnvNum toEnvNum3 buff1 by auto  
        (*obtain y where "toEnvP y \<and> substate x y \<and> substate y st0 \<and> y\<noteq>st0"
          using "1" prem substate_total by blast 
        then have "getVarBool y ''user'' \<or> \<not>getVarBool y ''rotation''"
        proof
          assume "\<forall>y. toEnvP y \<and> substate x y \<and> substate y st0 \<longrightarrow> \<not>getVarBool y ''user'' \<and> getVarBool y ''rotation''"
        qed*)
        thus ?thesis using prem using assms extra 0 1 2 3 4 5 6 prem extraInv_def R3_full_def by 
      qed
    next
      fix x
      assume "toEnvP x \<and>
         substate x st_final \<and>
         (10 \<le> toEnvNum x st_final \<and>
          getVarBool (predEnv x) ''rotation'' \<and>
          \<not> getVarBool x ''user'') \<and>
         \<not> getVarBool (predEnv x) ''rotation''"
      thus "\<exists>y. toEnvP y \<and> substate x y \<and> substate y st_final \<and> 
              (toEnvNum x y \<le> 10 \<and> getVarBool y ''rotation'' = False \<or> toEnvNum x y \<le> 10 \<and> getVarBool y ''user'') \<and>
             (\<forall>s3. (toEnvP s3 \<and> substate x s3 \<and> substate s3 y) \<and>s3 \<noteq> y \<longrightarrow>
                 getVarBool s3 ''rotation'' \<and> \<not> getVarBool s3 ''user'')" by auto
    next
      fix x
      assume "toEnvP x \<and>
         substate x st_final \<and>
         (10 \<le> toEnvNum x st_final \<and>
          getVarBool (predEnv x) ''rotation'' \<and> \<not> getVarBool x ''user'') \<and>
         \<not> \<not> getVarBool x ''user''"
      thus "\<exists>y. toEnvP y \<and> substate x y \<and> substate y st_final \<and> 
              (toEnvNum x y \<le> 10 \<and> getVarBool y ''rotation'' = False \<or> toEnvNum x y \<le> 10 \<and> getVarBool y ''user'') \<and>
             (\<forall>s3. (toEnvP s3 \<and> substate x s3 \<and> substate s3 y) \<and>s3 \<noteq> y \<longrightarrow>
                 getVarBool s3 ''rotation'' \<and> \<not> getVarBool s3 ''user'')" by auto
    qed
    ultimately have  "P8_cons R3_A R3_B R3_C st_final" using P8_lemma by blast
    thus ?thesis using "0" P8_cons_to_R3 by auto
  qed
qed
*)
lemma
 assumes base_inv:"(inv5 st0)"
 and st1:"(st1=(setVarBool st0 ''pressure'' pressure))"
 and st2:"(st2=(setVarBool st1 ''user'' user))"
 and st2_Controller_state:"(getPstate st2 ''Controller'')=''motionless''"
 and st2_if0:"(getVarBool st2 ''user'')=True"
 and st2_if1:"(getVarBool st2 ''pressure'')=True"
 and st3:"(st3=(setVarBool st2 ''brake'' True))"
 and st4:"(st4=(setPstate st3 ''Controller'' ''suspended''))"
 and st5:"(st5=(toEnv st4))"
 and st_final:"(st_final=st5)"
shows "(inv5 st_final)"


end
