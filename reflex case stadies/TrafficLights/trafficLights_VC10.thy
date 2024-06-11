theory trafficLights_VC10
imports trafficLightsTheory
begin

lemma extra:
 assumes base_inv:"(extraInv st0)"
 and st1:"(st1=(setVarBool st0 ''button'' button))"
 and st1_Controller_state:"(getPstate st1 ''Controller'')=''green''"
 and st1_green_timeout:"30000>(ltime st1 ''Controller'')"
 and st2:"(st2=(toEnv st1))"
 and st_final:"(st_final=st2)"
shows "(extraInv st_final)"
  by sorry


lemma
 assumes base_inv:"(inv2 st0)"
 and st1:"(st1=(setVarBool st0 ''button'' button))"
 and st1_Controller_state:"(getPstate st1 ''Controller'')=''green''"
 and st1_green_timeout:"30000>(ltime st1 ''Controller'')"
 and st2:"(st2=(toEnv st1))"
 and st_final:"(st_final=st2)"
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
      show "getVarBool st_final ''light'' = True" using assms extra extraInv_def extra3_def substate_refl by auto
    qed
    ultimately have "P3_cons R2_A st_final" using P3_lemma by auto
    thus ?thesis using P3_cons_to_R2 assms by auto
  qed
qed

lemma
 assumes base_inv:"(inv5 st0)"
 and st1:"(st1=(setVarBool st0 ''button'' button))"
 and st1_Controller_state:"(getPstate st1 ''Controller'')=''green''"
 and st1_green_timeout:"30000>(ltime st1 ''Controller'')"
 and st2:"(st2=(toEnv st1))"
 and st_final:"(st_final=st2)"
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
      have a4:"getPstate x ''Controller'' \<noteq> ''green''" using prem extra1 extraInv_def extra0_def extra3_def by auto
      have 5:"(ltime st0 ''Controller'') div 100 <300" using assms by auto
      have 6:"toEnvNum (predEnv x) st0 <150" using 1 predEnv_substate predEnv_toEnvNum prem toEnvNum3 by force
      consider (a)"toEnvNum x st0 < (ltime st0 ''Controller'') div 100" | (b)"toEnvNum x st0 \<ge> (ltime st0 ''Controller'') div 100" by force
      thus "getVarBool st_final ''light'' = False"
      proof(cases)
        case a
        moreover have "toEnvP x \<and> toEnvP st0 \<and> substate x st0 \<and> substate st0 st0" using 0 prem substate_refl by auto
        moreover have "getPstate st0 ''Controller'' = ''green''" using assms by auto
        ultimately have "getPstate x ''Controller'' = ''green'' " using extra1 extraInv_def extra13_def by blast
        then have "getVarBool x ''light''" using prem extra1 extraInv_def extra3_def by auto
        then show ?thesis using prem by auto
      next
        case b
        then have 7:"toEnvNum (predEnv x) st0 > (ltime st0 ''Controller'') div 100" using toEnvNum3 predEnv_substate predEnv_toEnvNum prem by force
        have "getPstate st0 ''Controller''= ''green''" using assms by auto
        then obtain y where o1:"toEnvP y \<and> substate y st0 \<and>
                                toEnvNum y st0 = ltime st0 ''Controller'' div 100\<and>
                                getPstate y ''Controller'' = ''redToGreen'' \<and>
                                ltime y ''Controller'' \<ge> 5000" 
          using extra1 extraInv_def extra12_def 0 substate_refl by blast
        then have "substate (predEnv x) y \<or> substate y (predEnv x)" using substate_total 3 by auto
        then consider (c) "substate (predEnv x) y" | (d) "substate y (predEnv x) \<and> (predEnv x)\<noteq>y" by auto
        then show "getVarBool st_final ''light'' = False"
        proof (cases)
          case c
          then have "toEnvNum (predEnv x) st0 = toEnvNum (predEnv x) y + toEnvNum y st0" using o1 toEnvNum3 by auto
          then have 8:"toEnvNum (predEnv x) y < 150" using 6 by auto
          have "ltime y ''Controller'' div 100 \<ge> 50" using o1 by auto
          then have "100 + 1 + (ltime y ''Controller'' div 100) \<ge> 150" by auto
          then have "toEnvP (predEnv x) \<and> toEnvP y \<and> 
                      substate (predEnv x) y \<and> substate y st0 \<and>
                      getPstate y ''Controller'' = ''redToGreen'' \<and>
                      toEnvNum (predEnv x) y \<le> 100 + 1 + (ltime y ''Controller'' div 100)" using 2 o1 c 3 8 by auto
          then have "getVarBool (predEnv x) ''light'' = False" using extra1 extraInv_def extra18_def by auto
          then show ?thesis using prem by auto
        next
          case d
          then have "toEnvNum y st0 = toEnvNum y (predEnv x) + toEnvNum (predEnv x) st0" using 3 toEnvNum3 by auto
          then have "ltime st0 ''Controller'' div 100 = toEnvNum y (predEnv x) + toEnvNum (predEnv x) st0" using o1 by auto
          then show ?thesis using 7 by auto 
        qed
      qed
    qed
    ultimately have "P3_cons R5_A st_final" using P3_lemma by auto
    thus ?thesis using P3_cons_to_R5 assms by auto
  qed
qed

lemma
 assumes base_inv:"(inv4 st0)"
 and st1:"(st1=(setVarBool st0 ''button'' button))"
 and st1_Controller_state:"(getPstate st1 ''Controller'')=''green''"
 and st1_green_timeout:"30000>(ltime st1 ''Controller'')"
 and st2:"(st2=(toEnv st1))"
 and st_final:"(st_final=st2)"
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
    moreover have "(\<forall>x. toEnvP x \<and>substate x st0 \<and> toEnvP (predEnv x) \<and> B_wrap R4_B st_final x \<and>
                    (\<not>R4_A st0 x (predEnv x) \<or> R4_C st0) \<longrightarrow>
                      (\<not>R4_A st_final x (predEnv x) \<or> R4_C st_final))"
    proof(simp only:R4_A_def R4_B_def R4_C_def; rule allI; rule impI)
      fix x
      assume prem:"toEnvP x \<and>substate x st0 \<and>toEnvP (predEnv x) \<and>B_wrap R4_B st_final x \<and>
         (\<not> (getVarBool (predEnv x) ''light'' \<noteq> False \<and>getVarBool x ''light'' = False) \<or>
          getVarBool st0 ''light'' = False)"
      thus "\<not> (getVarBool (predEnv x) ''light'' \<noteq> False \<and> getVarBool x ''light'' = False) \<or>
          getVarBool st_final ''light'' = False"
      proof -
        have "getPstate st0 ''Controller'' = ''green''" using assms by auto
        then have "getVarBool st0 ''light'' = True" using extra1 extraInv_def extra3_def substate_refl by blast
        then have "\<not> (getVarBool (predEnv x) ''light'' \<noteq> False \<and>getVarBool x ''light'' = False)" using prem by auto
        thus ?thesis by auto
      qed
    qed
    moreover have "(\<forall>x y. toEnvP x \<and> toEnvP y \<and> substate x y \<and> substate y st0 \<and> toEnvP (predEnv x) \<and> 
                    \<not> R4_C y \<and> B_wrap R4_B st_final x \<and> \<not>R4_A st0 x (predEnv x) \<longrightarrow>
                       \<not>R4_A st_final x (predEnv x))"
    proof(simp only: R4_A_def R4_B_def R4_C_def; intro allI; rule impI)
      fix x y
      assume prem:"toEnvP x \<and> toEnvP y \<and> substate x y \<and> substate y st0 \<and>
                  toEnvP (predEnv x) \<and> getVarBool y ''light'' \<noteq> False \<and> B_wrap R4_B st_final x \<and>
                  \<not>(getVarBool (predEnv x) ''light'' \<noteq> False \<and> getVarBool x ''light'' = False)"
      thus "\<not> (getVarBool (predEnv x) ''light'' \<noteq> False \<and> getVarBool x ''light'' = False)" by auto
    qed
    ultimately show ?thesis using P9_lemma P9_cons_to_R4 by auto
  qed
qed
end
