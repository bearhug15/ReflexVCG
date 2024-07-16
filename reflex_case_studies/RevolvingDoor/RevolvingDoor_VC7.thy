theory RevolvingDoor_VC7
imports RevolvingDoorTheory
begin

lemma extra:
 assumes base_inv:"(extraInv st0)"
 and st1:"(st1=(setVarBool st0 ''pressure'' pressure))"
 and st2:"(st2=(setVarBool st1 ''user'' user))"
 and st2_Controller_state:"(getPstate st2 ''Controller'')=''rotating''"
 and st2_if5:"(getVarBool st2 ''pressure'')=False"
 and st2_if7:"(getVarBool st2 ''user'')=False"
 and st2_rotating_timeout:"1000>(ltime st2 ''Controller'')"
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
  proof -
    have "extraMotionlessOut st0" using extraInv_def base_inv by blast
    then show ?thesis using assms extraMotionlessOut_def by auto
  qed
next
  show "extraRotatingOut st_final" 
  proof (simp only:extraRotatingOut_def; rule allI; rule impI)
    fix s1
    assume prems:"toEnvP s1 \<and>
          substate s1 st_final \<and>
          getPstate s1 ''Controller'' = ''rotating''"
    then show "getVarBool s1 ''rotation'' \<and>
          \<not> getVarBool s1 ''brake'' \<and>
          ltime s1 ''Controller'' \<le> 1000"
    proof (cases)
      assume 1:"s1=st_final"
      have "\<not> getVarBool st0 ''brake''" using extraRotatingOut_def substate_refl assms extraInv_def by auto
      then have 2:"\<not> getVarBool st_final ''brake''" using assms by auto
      have 3: "getVarBool st_final ''rotation''" using extraRotatingOut_def substate_refl assms extraInv_def by auto
      have "1000=(10*100::nat) \<and> 900=(10*100::nat) -100" by auto
      then have "(ltime st2 ''Controller'')\<le>900" using assms ltime_mod by presburger
      then have 4: "ltime st_final ''Controller'' \<le> 1000" using assms ltime_div_less  by auto
      show ?thesis using 1 2 3 4 by auto
    next
      assume 1:"s1\<noteq>st_final"
      have "predEnv st_final = st0" using assms extraInv_def by auto
      then have 2:"substate s1 st0" using 1 assms prems substate_eq_or_predEnv by blast
      have "extraRotatingOut st0" using base_inv extraInv_def by auto
      thus ?thesis using 1 2 prems assms extraInv_def extraRotatingOut_def by auto
    qed
  qed
next
  show "extraSuspendedOut st_final"
  proof -
    have "extraSuspendedOut st0" using extraInv_def base_inv by blast
    then show ?thesis using assms extraSuspendedOut_def by auto
  qed
next
  show "extra1 st_final"
  proof -
    have "extra1 st0" using base_inv extraInv_def by auto
    thus ?thesis using assms extra1_def by auto
  qed
next
  have prems: "extra2 st0" using assms extraInv_def by auto
  show "extra2 st_final"
  proof -
    have 0:"toEnvP st0 \<and> toEnvP st_final \<and> st0 = predEnv st_final" using assms extraInv_def by auto
    moreover have "P5_1_cons extra2_A extra2_B st0" using extra2 prems by auto
    moreover have "(AB_comb_before extra2_A (p_2_3_conpred_notEmpty extra2_B)) st_final st_final"
    proof -
      have 1:"toEnvP st0 \<and> substate st0 st0 \<and> getPstate st0 ''Controller'' = ''rotating''" 
        using assms extraInv_def substate_refl by auto
      then obtain s2 s3 where
        o1:"toEnvP s2 \<and> 
            toEnvP s3 \<and> 
            substate s2 s3 \<and> 
            substate s3 st0 \<and> 
            toEnvNum s2 s3 = 1 \<and>
            toEnvNum s2 st0 = ltime st0 ''Controller'' div 100 \<and>
            ((getPstate s2 ''Controller'' = ''motionless'' \<or> 
                getPstate s2 ''Controller'' = ''rotating'') \<and>
              getVarBool s3 ''user'' \<or> 
              getPstate s2 ''Controller'' = ''suspended'') \<and> 
            \<not>getVarBool s3 ''pressure''" using prems extra2_def by blast
      have "substate st0 st_final" using 0 predEnv_substate by auto
      then have 2:"substate s3 st_final" using  o1 substate_trans by blast
      have 3:"toEnvNum s2 st_final = ltime st_final ''Controller'' div 100"
      proof -
        have "toEnvNum st0 st_final = 1" using 0
          using predEnv_substate predEnv_toEnvNum by auto
        moreover have "substate s2 st0 \<and> substate st0 st_final" using o1 0 substate_trans predEnv_substate by blast
        ultimately  have sub1:"toEnvNum s2 st_final = toEnvNum s2 st0 +1" using toEnvNum3 by auto
        have "ltime st_final ''Controller'' = ltime st0 ''Controller'' + 100"
          by (simp add: st1 st2 st3 st_final)
        then have sub2:"ltime st_final ''Controller'' div 100 = ltime st0 ''Controller'' div 100 +1" by auto
        have "toEnvNum s2 st0 = ltime st0 ''Controller'' div 100" using o1 by auto
        thus ?thesis using sub1 sub2 by auto
      qed
      have "toEnvP s2 \<and> toEnvP s3 \<and> substate s2 s3 \<and> toEnvNum s2 s3 = 1" using o1 by auto
      then have "s2 = predEnv s3" using toEnvNum_predEnv by auto
      then have "(predEnv s3) \<noteq> emptyState \<and> 
                toEnvP s3 \<and> 
                substate s3 st_final \<and> 
                toEnvNum (predEnv s3) st_final = ltime st_final ''Controller'' div 100 \<and>
                ((getPstate (predEnv s3) ''Controller'' = ''motionless'' \<or> 
                    getPstate (predEnv s3) ''Controller'' = ''rotating'') \<and>
                  getVarBool s3 ''user'' \<or> 
                  getPstate (predEnv s3) ''Controller'' = ''suspended'') \<and> 
                \<not>getVarBool s3 ''pressure''" using o1 2 3 by auto
      thus ?thesis apply (simp only: AB_comb_before_def extra2_A_def extra2_B_def p_2_3_conpred_notEmpty)
        using \<open>s2 = predEnv s3\<close> \<open>toEnvP s2 \<and> toEnvP s3 \<and> substate s2 s3 \<and> toEnvNum s2 s3 = 1\<close> by blast
    qed
    moreover have "(\<forall>x. toEnvP x \<and> substate x st_final \<and> extra2_A st_final x \<and> \<not> extra2_A st0 x \<longrightarrow> (\<exists>y. toEnvP y \<and> substate y x \<and> substate y st_final \<and> (p_2_3_conpred_notEmpty extra2_B) x y))"
      apply (simp only: extra2_A_def extra2_B_def p_2_3_conpred_notEmpty)
      by auto
    ultimately have "P5_1_cons extra2_A extra2_B st_final" using P5_1_lemma P5_1_bridge by blast
    thus ?thesis using extra2 by auto
  qed
next
  have prems:"extra3 st0" using assms extraInv_def by simp
  show "extra3 st_final"
  proof -
    have 0:"toEnvP st0 \<and> toEnvP st_final \<and> st0 = predEnv st_final" using assms extraInv_def by auto
    moreover have "P6_cons extra3_A extra3_B extra3_C st0" using assms extraInv_def extra3 P6_bridge by auto
    moreover have "(AB_comb_before extra3_A (BC_to_BP4 extra3_B extra3_C)) st_final st_final"
    proof -
      have 1:"toEnvP st0 \<and> substate st0 st0 \<and> getPstate st0 ''Controller'' = ''rotating''" 
        using assms extraInv_def substate_refl by auto
      then obtain s2 s3 where
       o1:"toEnvP s2 \<and> 
        toEnvP s3 \<and> 
        substate s2 s3 \<and> 
        substate s3 st0 \<and> 
        toEnvNum s2 s3 = 1 \<and>
        (getPstate s2 ''Controller'' = ''motionless'' \<and>
        getVarBool s3 ''user'' \<or>
        getPstate s2 ''Controller'' = ''suspended'') \<and>
        \<not> getVarBool s3 ''pressure'' \<and>
        (\<forall>s4. toEnvP s4 \<and> substate s3 s4 \<and> substate s4 st0 \<longrightarrow>
                getPstate s4 ''Controller'' = ''rotating'')" using extra3_def prems by blast
      have 2:"substate st0 st_final" using calculation(1) predEnv_substate by auto
      then have 3:"substate s3 st_final" using o1 substate_trans by blast
      define subA where "subA \<equiv> (\<lambda>x. substate s3 x \<longrightarrow> getPstate x ''Controller'' = ''rotating'')"
      then have "P1 subA st0" using P1_def o1 SMT.verit_bool_simplify(4) by auto
      moreover have "subA st_final" using assms subA_def by auto
      ultimately have "P1 subA st_final" using 0 P1_lemma by auto
      then have 4:"(\<forall>s4. toEnvP s4 \<and> substate s3 s4 \<and> substate s4 st_final \<longrightarrow>
                getPstate s4 ''Controller'' = ''rotating'')" using P1_def subA_def by auto
      have 5:"s2 = predEnv s3"using o1 toEnvNum_predEnv by auto
      have 6:"s2\<noteq>emptyState" using o1 by auto
      have "(\<exists>s2.
                  toEnvP s2 \<and> 
                  substate s2 st_final \<and> 
                  ((getPstate (predEnv s2) ''Controller'' = ''motionless'' \<and>
                  getVarBool s2 ''user'' \<or>
                  getPstate (predEnv s2) ''Controller'' = ''suspended'') \<and>
                  \<not> getVarBool s2 ''pressure'') \<and>
                  (predEnv s2) \<noteq>emptyState \<and>
                  (\<forall>s3. toEnvP s3 \<and> substate s2 s3 \<and> substate s3 st_final \<longrightarrow>
                          getPstate s3 ''Controller'' = ''rotating''))" using o1 3 4 5 6 by blast
      then show ?thesis
        apply (simp only:AB_comb_before_def BC_to_BP4_def extra3_A_def extra3_B_def extra3_C_def) 
        using predEnvP_or_emptyState by blast 
      qed
      moreover have "(\<forall>x. toEnvP x \<and> substate x st_final \<and> extra3_A st_final x \<and> \<not> extra3_A st0 x \<longrightarrow> 
          (\<exists>y. toEnvP y \<and> substate y x \<and> substate y st_final \<and> (BC_to_BP4 extra3_B extra3_C) x y))" using assms extraInv_def extra3_def BC_to_BP4_def extra3_A_def extra3_B_def extra3_C_def
        by (smt (verit, best)) 
      ultimately have "P6_cons extra3_A extra3_B extra3_C st_final" using P6_lemma P6_bridge by auto
      thus ?thesis using P6_bridge extra3 by auto
  qed
next
  show "extra6 st_final"
  proof -
    define ex6_A1 where "ex6_A1 \<equiv> (\<lambda>x. p_2_3_conpred ex6_A3 st_final x)"
    moreover have 0:"toEnvP st0 \<and> toEnvP st_final \<and> st0 = predEnv st_final" using assms extraInv_def by auto
    moreover have "P3_predEnv ex6_A3 st0" using assms extraInv_def ex6_A3 by auto
    moreover have "ex6_A1 st_final" 
    proof (simp only: ex6_A1_def p_2_3_conpred_def ex6_A3_def; rule impI)
      have "getPstate st0 ''Controller'' = ''rotating''" using assms by auto
      moreover have "\<not>getVarBool st_final ''user''" using assms by auto
      ultimately show  "getPstate (predEnv st_final) ''Controller'' =''rotating'' \<and>
                        \<not> getVarBool st_final ''user''" 
        using 0 by auto
    qed
    moreover have "P1 ex6_A1 st0"
      apply (simp only:P1_def ex6_A1_def p_2_3_conpred_def ex6_A3_def SMT.verit_bool_simplify(4))
      using P1_def ex6_A1_def p_2_3_conpred_def ex6_A3_def by sorry
    ultimately show ?thesis using A3_ex6_sub P3_bridge P3_lemma by blast 
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
qed



lemma
 assumes base_inv:"(inv1 st0)"
 and st1:"(st1=(setVarBool st0 ''pressure'' pressure))"
 and st2:"(st2=(setVarBool st1 ''user'' user))"
 and st2_Controller_state:"(getPstate st2 ''Controller'')=''rotating''"
 and st2_if5:"(getVarBool st2 ''pressure'')=False"
 and st2_if7:"(getVarBool st2 ''user'')=False"
 and st2_rotating_timeout:"1000>(ltime st2 ''Controller'')"
 and st3:"(st3=(toEnv st2))"
 and st_final:"(st_final=st3)"
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
 and st2_Controller_state:"(getPstate st2 ''Controller'')=''rotating''"
 and st2_if5:"(getVarBool st2 ''pressure'')=False"
 and st2_if7:"(getVarBool st2 ''user'')=False"
 and st2_rotating_timeout:"1000>(ltime st2 ''Controller'')"
 and st3:"(st3=(toEnv st2))"
 and st_final:"(st_final=st3)"
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
 assumes base_inv:"(inv6 st0)"
 and st1:"(st1=(setVarBool st0 ''pressure'' pressure))"
 and st2:"(st2=(setVarBool st1 ''user'' user))"
 and st2_Controller_state:"(getPstate st2 ''Controller'')=''rotating''"
 and st2_if5:"(getVarBool st2 ''pressure'')=False"
 and st2_if7:"(getVarBool st2 ''user'')=False"
 and st2_rotating_timeout:"1000>(ltime st2 ''Controller'')"
 and st3:"(st3=(toEnv st2))"
 and st_final:"(st_final=st3)"
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

lemma
 assumes base_inv:"(inv4 st0)"
 and st1:"(st1=(setVarBool st0 ''pressure'' pressure))"
 and st2:"(st2=(setVarBool st1 ''user'' user))"
 and st2_Controller_state:"(getPstate st2 ''Controller'')=''rotating''"
 and st2_if5:"(getVarBool st2 ''pressure'')=False"
 and st2_if7:"(getVarBool st2 ''user'')=False"
 and st2_rotating_timeout:"1000>(ltime st2 ''Controller'')"
 and st3:"(st3=(toEnv st2))"
 and st_final:"(st_final=st3)"
shows "(inv4 st_final)"
proof(simp only: inv4_def; rule conjI)
  show "extraInv st_final" using assms extra inv4_def by auto
next
  have extra:"extraInv st_final" using assms extra inv4_def by auto
  show "R4_full st_final"
  proof -
    define inv4_A1 where "inv4_A1 = p_2_3_conpred R4_A3 st_final"
    moreover have 0:"toEnvP st0 \<and> toEnvP st_final \<and> st0 = predEnv st_final" using assms R4_full_def inv4_def by auto
    moreover have "P3_cons R4_A3 st0" using assms inv4_def R4_A3 R4_def by auto
    moreover have "P1 inv4_A1 st0"
    proof (simp only: P1_def inv4_A1_def p_2_3_conpred_def R4_A3_def SMT.verit_bool_simplify(4);rule allI; rule impI)
      fix x
      assume prem:"(toEnvP x \<and> substate x st0) \<and>
               toEnvNum x st_final < 10 \<and>
               getVarBool (predEnv x) ''rotation'' \<and>
               getVarBool x ''pressure''"
      then have 1:"substate x st0 \<and> substate st0 st_final \<and> substate (predEnv x) st0 \<and> substate (predEnv x) x" using 0 predEnv_substate prem substate_trans by blast 
      then have 2: "toEnvNum st0 st_final = 1" using assms calculation(2) predEnv_toEnvNum by blast
      then have 3:"toEnvNum x st0 < 9" using prem assms toEnvNum3
        by (metis "1" add_less_imp_less_right numeral_plus_one semiring_norm(5) semiring_norm(8))
      have 4:"toEnvP (predEnv x) \<and> toEnvNum (predEnv x) x =1" using predEnv_substate predEnv_toEnvNum predEnvP_or_emptyState prem by force
      then have 5:"toEnvNum (predEnv x) st0 <10" using 1 2 predEnv_substate toEnvNum3 predEnv_toEnvNum prem predEnvP_or_emptyState by force 
      have 6:"extra6 st0" using assms inv4_def extraInv_def by auto
      have 7:"getPstate st0 ''Controller'' = ''rotating''" using assms by auto
      have 8:"getPstate (predEnv x) ''Controller'' = ''rotating''"
        using 1 4 prem base_inv inv4_def extraInv_def extraControllerStates_def extraMotionlessOut_def extraRotatingOut_def extraSuspendedOut_def
        by meson
      have 9:"R4_full st0" using assms inv4_def by auto
      consider (a) "getVarBool x ''pressure''" | (b) "\<not>getVarBool x ''pressure''" by auto
      then have "\<not>getVarBool x ''pressure''"
      proof (cases)
        case a
        have "toEnvP (predEnv x) \<and> toEnvP x \<and> toEnvP st0 \<and> 
                substate (predEnv x) x \<and> substate x st0 \<and> substate st0 st0 \<and>
                toEnvNum (predEnv x) x = 1 \<and> toEnvNum x st0 <10 \<and>
                getVarBool (predEnv x) ''rotation'' \<and> getVarBool x ''pressure''" 
          using 1 4 3 prem base_inv inv4_def extraInv_def substate_refl predEnv_toEnvNum 2 9 
          by simp
        then have "getVarBool st0 ''brake''" using 9 R4_full_def a  by auto
        then have "getPstate st0 ''Controller'' = ''suspended''" 
          using base_inv inv4_def extraInv_def extra extraControllerStates_def extraMotionlessOut_def extraRotatingOut_def extraSuspendedOut_def substate_refl by blast
        then show ?thesis using 7 by auto
      next
        case b
        then show ?thesis by auto
      qed
      thus "getVarBool st_final ''brake''" using prem by auto
    qed
    moreover have "inv4_A1 st_final"
    proof(simp only:inv4_A1_def p_2_3_conpred_def R4_A3_def; rule impI)
      assume "toEnvNum st_final st_final < 10 \<and>
              getVarBool (predEnv st_final) ''rotation'' \<and>
              getVarBool st_final ''pressure''"
      thus "getVarBool st_final ''brake''" using assms by auto
    qed
    ultimately show ?thesis using P3_lemma A3_R4 by auto
  qed
qed

end
