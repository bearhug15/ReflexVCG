theory trafficLights_VC9
imports trafficLightsTheory
begin

lemma extra:
 assumes base_inv:"(extraInv st0)"
 and st1:"(st1=(setVarBool st0 ''button'' button))"
 and st1_Controller_state:"(getPstate st1 ''Controller'')=''green''"
 and st1_green_timeout:"30000\<le>(ltime st1 ''Controller'')"
 and st2:"(st2=(setVarBool st1 ''light'' False))"
 and st3:"(st3=(setPstate st2 ''Controller'' ''minimalRed''))"
 and st4:"(st4=(toEnv st3))"
 and st_final:"(st_final=st4)"
shows "(extraInv st_final)"
  by sorry

lemma
 assumes base_inv:"(inv2 st0)"
 and st1:"(st1=(setVarBool st0 ''button'' button))"
 and st1_Controller_state:"(getPstate st1 ''Controller'')=''green''"
 and st1_green_timeout:"30000\<le>(ltime st1 ''Controller'')"
 and st2:"(st2=(setVarBool st1 ''light'' False))"
 and st3:"(st3=(setPstate st2 ''Controller'' ''minimalRed''))"
 and st4:"(st4=(toEnv st3))"
 and st_final:"(st_final=st4)"
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
      then have 3:"toEnvNum x st0 <299" using 0 toEnvNum3 predEnv_toEnvNum predEnv_substate by force
      then have 4:"toEnvNum (predEnv x) st0 <300" 
      proof -
        have "substate (predEnv x) x \<and> substate x st0" using prem predEnv_substate by auto
        then have "toEnvNum (predEnv x) st0 = 1 + toEnvNum x st0" using toEnvNum3 predEnv_toEnvNum prem by auto
        thus ?thesis using 3 by auto
      qed
      have 5:"ltime st0 ''Controller'' div 100 \<ge> 300" using assms by auto
      have 6:"getPstate st0 ''Controller'' = ''green''" using assms by auto
      then have "(\<forall> s1. toEnvP s1 \<and> toEnvP st0 \<and> 
                  substate s1 st0 \<and> substate st0 st0 \<and> 
                  getPstate st0 ''Controller'' = ''green'' \<and>
                  toEnvNum s1 st0 < ltime st0 ''Controller'' div 100 \<longrightarrow> 
                    getPstate s1 ''Controller'' = ''green'')" 
        using extra extraInv_def extra13_def substate_refl by blast
      then have "(\<forall>y. toEnvP y \<and> substate y st0 \<and> toEnvNum y st0 <300 \<longrightarrow> 
                  getPstate y ''Controller'' = ''green'')" 
        using 5 0 6 substate_refl by force 
      then have 7:"getPstate (predEnv x) ''Controller'' = ''green''" using 4
        by (smt (verit) "5" ltime_le_toEnvNum order_less_le_trans predEnvP_or_emptyState predEnv_substate prem substate_trans verit_comp_simplify1(3))
      then have "(predEnv x) \<noteq> emptyState" by auto
      then have "getVarBool (predEnv x) ''light'' = True"
        using substate_trans prem predEnv_substate extra extraInv_def extra3_def 7 predEnvP_or_emptyState by blast
      thus "getVarBool st_final ''light'' = True" using prem by auto
    qed
    ultimately have "P3_cons R2_A st_final" using P3_lemma by auto
    thus ?thesis using P3_cons_to_R2 assms by auto
  qed
qed

lemma
 assumes base_inv:"(inv5 st0)"
 and st1:"(st1=(setVarBool st0 ''button'' button))"
 and st1_Controller_state:"(getPstate st1 ''Controller'')=''green''"
 and st1_green_timeout:"30000\<le>(ltime st1 ''Controller'')"
 and st2:"(st2=(setVarBool st1 ''light'' False))"
 and st3:"(st3=(setPstate st2 ''Controller'' ''minimalRed''))"
 and st4:"(st4=(toEnv st3))"
 and st_final:"(st_final=st4)"
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
      have "getPstate st_final ''Controller'' = ''minimalRed''" using assms by auto
      thus "getVarBool st_final ''light'' = False" using extra extraInv_def extra4_def substate_refl by auto
    qed
    ultimately have "P3_cons R5_A st_final" using P3_lemma by auto
    thus ?thesis using P3_cons_to_R5 assms by auto
  qed
qed

end
