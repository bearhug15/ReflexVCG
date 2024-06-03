theory trafficLights_VC7
imports trafficLightsTheory
begin

lemma extra:
 assumes base_inv:"(extraInv st0)"
 and st1:"(st1=(setVarBool st0 ''button'' button))"
 and st1_Controller_state:"(getPstate st1 ''Controller'')=''redToGreen''"
 and st1_redToGreen_timeout:"5000\<le>(ltime st1 ''Controller'')"
 and st2:"(st2=(setVarBool st1 ''light'' True))"
 and st3:"(st3=(setVarBool st2 ''_bpressed'' False))"
 and st4:"(st4=(setPstate st3 ''Controller'' ''green''))"
 and st5:"(st5=(toEnv st4))"
 and st_final:"(st_final=st5)"
shows "(extraInv st_final)"
  by sorry

lemma
 assumes base_inv:"(inv2 st0)"
 and st1:"(st1=(setVarBool st0 ''button'' button))"
 and st1_Controller_state:"(getPstate st1 ''Controller'')=''redToGreen''"
 and st1_redToGreen_timeout:"5000\<le>(ltime st1 ''Controller'')"
 and st2:"(st2=(setVarBool st1 ''light'' True))"
 and st3:"(st3=(setVarBool st2 ''_bpressed'' False))"
 and st4:"(st4=(setPstate st3 ''Controller'' ''green''))"
 and st5:"(st5=(toEnv st4))"
 and st_final:"(st_final=st5)"
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
      show "getVarBool st_final ''light'' = True" using assms by auto
    qed
    ultimately have "P3_cons R2_A st_final" using P3_lemma by auto
    thus ?thesis using P3_cons_to_R2 assms by auto
  qed
qed

lemma
 assumes base_inv:"(inv5 st0)"
 and st1:"(st1=(setVarBool st0 ''button'' button))"
 and st1_Controller_state:"(getPstate st1 ''Controller'')=''redToGreen''"
 and st1_redToGreen_timeout:"5000\<le>(ltime st1 ''Controller'')"
 and st2:"(st2=(setVarBool st1 ''light'' True))"
 and st3:"(st3=(setVarBool st2 ''_bpressed'' False))"
 and st4:"(st4=(setPstate st3 ''Controller'' ''green''))"
 and st5:"(st5=(toEnv st4))"
 and st_final:"(st_final=st5)"
shows "(inv5 st_final)"
proof(simp only:inv5_def;rule conjI)
  show "extraInv st_final" using assms extra inv5_def by auto
next
  have extra1:"extraInv st0 \<and> R5 st0" using assms inv5_def by auto
  have extra2:"extraInv st_final" using assms extra inv5_def by auto
  show "R5 st_final"
  proof -
    have 0:"toEnvP st0 \<and> toEnvP st_final \<and> st0 = predEnv st_final" using assms inv5_def extraInv_def by auto
    moreover have "P3_cons R5_A st0" using assms inv5_def R5_to_P3_cons by auto
    moreover have "(p_2_3_conpred R5_A st_final) st_final"
      apply (simp only: p_2_3_conpred_def R5_A_def)
      by blast
    moreover have "P1 (p_2_3_conpred R5_A st_final) st0"
    proof (simp only: P1_def p_2_3_conpred_def R5_A_def SMT.verit_bool_simplify(4);rule allI;rule impI)
      fix x
      assume prem:"(toEnvP x \<and> substate x st0) \<and>
               toEnvNum x st_final < 150 \<and>
               getVarBool (predEnv x) ''light'' \<noteq> False \<and>
               getVarBool x ''light'' = False"
      have 1:"toEnvNum x st0 < 149" using 0 prem predEnv_substate predEnv_toEnvNum toEnvNum3 by force
      have 2:"toEnvP (predEnv x)" using prem predEnvP_or_emptyState by force
      have 3:"substate (predEnv x) st0" using predEnv_substate prem substate_trans by blast
      have 4:"getPstate (predEnv x) ''Controller'' = ''green''" using prem 2 3  extra1 extraInv_def extra0_def extra4_def by auto
      have 5:"50\<le>(ltime st0 ''Controller'') div 100" using assms by auto
      have 6:"toEnvNum (predEnv x) st0 <150" using 1 predEnv_substate predEnv_toEnvNum prem toEnvNum3 by force
      have "toEnvP (predEnv x) \<and> toEnvP st0 \<and>
            substate (predEnv x) st0 \<and> substate st0 st0 \<and>
            getPstate st0 ''Controller'' = ''redToGreen''" using 2 0 3 substate_refl assms by auto
      moreover have "toEnvNum (predEnv x) st0 \<le> 100 + 1 + (ltime st0 ''Controller'' div 100)" using 6 5 by auto
      ultimately have "getVarBool (predEnv x) ''light''=False" using extra1 extraInv_def extra18_def by auto
      thus "getVarBool st_final ''light'' = False" using prem by auto
    qed
    ultimately have "P3_cons R5_A st_final" using P3_lemma by auto
    thus ?thesis using P3_cons_to_R5 assms by auto
  qed
qed

end
