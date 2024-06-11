theory trafficLights_VC5
imports trafficLightsTheory
begin

lemma extra:
 assumes base_inv:"(extraInv st0)"
 and st1:"(st1=(setVarBool st0 ''button'' button))"
 and st1_Controller_state:"(getPstate st1 ''Controller'')=''redAfterMinimalRed''"
 and st1_if2:"((getVarBool st1 ''_bpressed'') \<or> (getVarBool st1 ''button''))=True"
 and st2:"(st2=(setPstate st1 ''Controller'' ''redToGreen''))"
 and st3:"(st3=(toEnv st2))"
 and st_final:"(st_final=st3)"
shows "(extraInv st_final)"
  by sorry

lemma
 assumes base_inv:"(inv2 st0)"
 and st1:"(st1=(setVarBool st0 ''button'' button))"
 and st1_Controller_state:"(getPstate st1 ''Controller'')=''redAfterMinimalRed''"
 and st1_if2:"((getVarBool st1 ''_bpressed'') \<or> (getVarBool st1 ''button''))=True"
 and st2:"(st2=(setPstate st1 ''Controller'' ''redToGreen''))"
 and st3:"(st3=(toEnv st2))"
 and st_final:"(st_final=st3)"
shows "(inv2 st_final)"
proof(simp only:inv2_def;rule conjI)
  show "extraInv st_final" using assms extra inv2_def by auto
next
  have extra:"extraInv st_final \<and> extraInv st0 \<and> R2 st0" using assms extra inv2_def by auto
  show "R2 st_final"
  proof -
    have 0:"toEnvP st0 \<and> toEnvP st_final \<and> st0 = predEnv st_final" using assms inv2_def extraInv_def by auto
    moreover have "P3_cons R2_A st0" using assms inv2_def R2_to_P3_cons by auto
    moreover have "(p_2_3_conpred R2_A st_final) st_final"
      apply (simp only: p_2_3_conpred_def R2_A_def)
      by blast
    moreover have "P1 (p_2_3_conpred R2_A st_final) st0"
    proof (simp only: P1_def p_2_3_conpred_def R2_A_def SMT.verit_bool_simplify(4);rule allI;rule impI)
      fix x
      assume prem:"(toEnvP x \<and> substate x st0) \<and>
               toEnvNum x st_final < 300 \<and>
               getVarBool (predEnv x) ''light'' \<noteq> True \<and>
               getVarBool x ''light'' = True"
      have 4:"toEnvNum x st0 <300" using prem 0 toEnvNum3 predEnv_toEnvNum predEnv_substate by force
      have 5:"toEnvP x \<and> toEnvP st0" using extra extraInv_def prem by auto
      have 6:"substate (predEnv x) x \<and> substate x st0 \<and> substate st0 st0" using predEnv_substate substate_refl prem by auto
      have 7:"toEnvNum (predEnv x) x = 1" using predEnv_toEnvNum predEnvP_or_emptyState 
        by (metis "5" add.commute add.right_neutral gtime_predE predEnv_substate toEnvNum.simps(1))
      consider  (a) "toEnvP (predEnv x)" |
                (b) "(predEnv x) = emptyState" using 4 le_neq_implies_less predEnvP_or_emptyState by auto
      then show "getVarBool st_final ''light'' = True"
      proof (cases)
        case a
        then have 8:"getVarBool st0 ''light'' = True" using extra R2_def 4 5 6 7 prem by blast
        have "getPstate st0 ''Controller'' = ''redAfterMinimalRed''" using assms by auto
        then have "getVarBool st0 ''light'' = False" using extra extraInv_def extra4_def substate_refl by auto
        then show ?thesis using 8 by auto
      next
        case b
        have 8:"getPstate x ''Controller'' = ''green''" using prem extra extraInv_def extra0_def extra4_def by auto
        then obtain s2 where o1:"( toEnvP s2 \<and> substate s2 x \<and> 
                    toEnvNum s2 x = ltime x ''Controller'' div 100 \<and>
                    getPstate s2 ''Controller'' = ''redToGreen'' \<and> 
                    ltime s2 ''Controller'' \<ge> 5000)" using extra extraInv_def extra12_def prem by auto
        then have "s2 \<noteq> x" using 8 by auto
        moreover have "substate s2 x \<and> toEnvP x \<and> (predEnv x) = emptyState" using b prem o1 by auto
        ultimately have "\<not>toEnvP s2" using substate_end by auto
        then show ?thesis using o1 by auto  
      qed
    qed
    ultimately have "P3_cons R2_A st_final" using P3_lemma by auto
    thus ?thesis using P3_cons_to_R2 assms by auto
  qed
qed

lemma
 assumes base_inv:"(inv5 st0)"
 and st1:"(st1=(setVarBool st0 ''button'' button))"
 and st1_Controller_state:"(getPstate st1 ''Controller'')=''redAfterMinimalRed''"
 and st1_if2:"((getVarBool st1 ''_bpressed'') \<or> (getVarBool st1 ''button''))=True"
 and st2:"(st2=(setPstate st1 ''Controller'' ''redToGreen''))"
 and st3:"(st3=(toEnv st2))"
 and st_final:"(st_final=st3)"
shows "(inv5 st_final)"
proof(simp only:inv5_def;rule conjI)
  show "extraInv st_final" using assms extra inv5_def by auto
next
  have extra:"extraInv st_final \<and> extraInv st0 \<and> R5 st0" using assms extra inv5_def by auto
  show "R5 st_final"
  proof -
    have 0:"toEnvP st0 \<and> toEnvP st_final \<and> st0 = predEnv st_final" using assms inv5_def extraInv_def by auto
    moreover have "P3_cons R5_A st0" using assms inv5_def R5_to_P3_cons by auto
    moreover have "(p_2_3_conpred R5_A st_final) st_final"
      apply (simp only: p_2_3_conpred_def R5_A_def)
      by blast
    moreover have "P1 (p_2_3_conpred R5_A st_final) st0"
    proof (simp only: P1_def p_2_3_conpred_def R5_A_def SMT.verit_bool_simplify(4);rule allI;rule impI)
      have "getPstate st_final ''Controller'' = ''redToGreen''" using assms by auto
      thus "getVarBool st_final ''light'' = False" using extra extraInv_def extra4_def substate_refl by auto
    qed
    ultimately have "P3_cons R5_A st_final" using P3_lemma by auto
    thus ?thesis using P3_cons_to_R5 assms by auto
  qed
qed

lemma
 assumes base_inv:"(inv4 st0)"
 and st1:"(st1=(setVarBool st0 ''button'' button))"
 and st1_Controller_state:"(getPstate st1 ''Controller'')=''redAfterMinimalRed''"
 and st1_if2:"((getVarBool st1 ''_bpressed'') \<or> (getVarBool st1 ''button''))=True"
 and st2:"(st2=(setPstate st1 ''Controller'' ''redToGreen''))"
 and st3:"(st3=(toEnv st2))"
 and st_final:"(st_final=st3)"
shows "(inv4 st_final)"
proof(simp only:inv4_def;rule conjI)
  show "extraInv st_final" using assms extra inv4_def by auto
next
  have extra1:"extraInv st0 \<and> R4 st0" using assms extra inv4_def by auto
  have extra2:"extraInv st_final" using assms extra inv4_def by auto
  show "R4 st_final"
  proof -
    have 0:"toEnvP st0 \<and> toEnvP st_final \<and> st0 = predEnv st_final" using assms inv4_def extraInv_def by auto
    moreover have "P9_cons R4_A R4_B R4_C st0" using extra1 R4_to_P9_cons by auto
    moreover have "(P9_ABC_comb R4_A R4_B R4_C) st_final st_final st_final" 
      apply (simp only:P9_ABC_comb_def R4_A_def R4_B_def R4_C_def)
      using assms by blast
    moreover have "(\<forall>x. toEnvP x \<and> substate x st0 \<and> toEnvP (predEnv x) \<and>
                    (\<not>R4_A st0 x (predEnv x) \<or> R4_C st0) \<longrightarrow> 
                      (\<not>R4_A st_final x (predEnv x) \<or> R4_C st_final))"
    proof -
      have "getPstate st_final ''Controller'' = ''redToGreen''" using assms by auto
      then have "getVarBool st_final ''light'' = False" using extraInv_def extra2 extra4_def substate_refl by force 
      then have "R4_C st_final" using R4_C_def by blast
      thus ?thesis by auto
    qed
    moreover have "(\<forall>x y. toEnvP x \<and> toEnvP y \<and> substate x y \<and> substate y st0 \<and> toEnvP (predEnv x) \<and> \<not> R4_C y \<and>
                    (\<not>R4_A st0 x (predEnv x)) \<longrightarrow>
                      (\<not>R4_A st_final x (predEnv x)))"
    proof(simp only: R4_A_def R4_B_def R4_C_def; intro allI; rule impI)
      fix x y
      assume prem:"toEnvP x \<and> toEnvP y \<and> substate x y \<and> substate y st0 \<and>
              toEnvP (predEnv x) \<and> getVarBool y ''light'' \<noteq> False \<and>
              \<not> (getVarBool (predEnv x) ''light'' \<noteq> False \<and> getVarBool x ''light'' = False)"
      thus "\<not> (getVarBool (predEnv x) ''light'' \<noteq> False \<and> getVarBool x ''light'' = False)" by auto
    qed
    ultimately show ?thesis using P9_lemma_simped P9_cons_to_R4 by auto
  qed
qed

end
