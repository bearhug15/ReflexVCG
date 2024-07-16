theory RevolvingDoor_VC6
imports RevolvingDoorTheory
begin


lemma extra:
 assumes base_inv:"(extraInv st0)"
 and st1:"(st1=(setVarBool st0 ''pressure'' pressure))"
 and st2:"(st2=(setVarBool st1 ''user'' user))"
 and st2_Controller_state:"(getPstate st2 ''Controller'')=''rotating''"
 and st2_if5:"(getVarBool st2 ''pressure'')=False"
 and st2_if7:"(getVarBool st2 ''user'')=False"
 and st2_rotating_timeout:"1000\<le>(ltime st2 ''Controller'')"
 and st3:"(st3=(setVarBool st2 ''rotation'' False))"
 and st4:"(st4=(setPstate st3 ''Controller'' ''motionless''))"
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
  proof (simp only: extraMotionlessOut_def; rule allI; rule impI)
    fix s1
    assume prems:"toEnvP s1 \<and>
          substate s1 st_final \<and>
          getPstate s1 ''Controller'' = ''motionless''"
    then show "\<not> getVarBool s1 ''rotation'' \<and>
                \<not> getVarBool s1 ''brake''"
    proof (cases)
      assume 1:"s1=st_final"
      have 2:"\<not> getVarBool st_final ''rotation''" using assms by auto
      have "\<not> getVarBool st0 ''brake''" using assms extraInv_def extraRotatingOut_def substate_refl by auto
      then have 3:"\<not> getVarBool st_final ''brake''" using assms by auto
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
  proof (simp only: extra1_def; rule allI; rule impI)
    fix s1
    assume prems:"toEnvP s1 \<and>substate s1 st_final \<and>
          getPstate s1 ''Controller'' = ''motionless''"
    then show "(\<forall>s2. toEnvP s2 \<and> substate s2 s1 \<longrightarrow> getPstate s2 ''Controller'' = ''motionless'') \<or>
          (\<exists>s2 s3.
              toEnvP s2 \<and>
              toEnvP s3 \<and>
              substate s2 s3 \<and>
              substate s3 s1 \<and>
              toEnvNum s2 s3 = 1 \<and>
              getPstate s2 ''Controller'' = ''rotating'' \<and>
              1000 \<le> ltime s2 ''Controller'' \<and>
              \<not> getVarBool s3 ''user'' \<and>
              \<not> getVarBool s3 ''pressure'' \<and>
              (\<forall>s4 s5.
                  toEnvP s4 \<and>
                  toEnvP s5 \<and> substate s3 s4 \<and> substate s4 s5 \<and> substate s5 s1 \<and> toEnvNum s4 s5 = 1 \<longrightarrow>
                  getPstate s4 ''Controller'' = ''motionless'' \<and> \<not> getVarBool s5 ''user''))"
    proof (cases)
      assume 1:"s1=st_final"
      have "toEnvP st0 \<and> 
            toEnvP st_final" using  assms extraInv_def by auto
      moreover have "substate st0 st_final" using assms substate_refl by auto 
      moreover have "substate st_final s1" using 1 substate_refl by auto
      moreover have "toEnvNum st0 st_final =1" using assms toEnvNum_id by auto
      moreover have "getPstate st0 ''Controller'' = ''rotating''" using assms by auto
      moreover have "1000 \<le> ltime st0 ''Controller''" using assms by auto
      moreover have "\<not> getVarBool st_final ''user'' \<and>\<not> getVarBool st_final ''pressure''" using assms by auto
      moreover have "(\<forall>s4 s5.
                  toEnvP s4 \<and>
                  toEnvP s5 \<and> substate st_final s4 \<and> substate s4 s5 \<and> substate s5 s1 \<and> toEnvNum s4 s5 = 1 \<longrightarrow>
                  getPstate s4 ''Controller'' = ''motionless'' \<and> \<not> getVarBool s5 ''user'')" using prems 1 substate_trans substate_asym
        using calculation(7) by blast
      ultimately show ?thesis by auto
    next
      assume 1:"s1\<noteq>st_final"
      have "predEnv st_final = st0" using assms extraInv_def by auto
      then have 2:"substate s1 st0" using 1 assms prems substate_eq_or_predEnv by blast
      have "extra1 st0" using assms extraInv_def by auto
      then show ?thesis using 1 2 prems assms extraInv_def extra1_def by auto
    qed
  qed
qed

lemma
 assumes base_inv:"(inv1 st0)"
 and st1:"(st1=(setVarBool st0 ''pressure'' pressure))"
 and st2:"(st2=(setVarBool st1 ''user'' user))"
 and st2_Controller_state:"(getPstate st2 ''Controller'')=''rotating''"
 and st2_if5:"(getVarBool st2 ''pressure'')=False"
 and st2_if7:"(getVarBool st2 ''user'')=False"
 and st2_rotating_timeout:"1000\<le>(ltime st2 ''Controller'')"
 and st3:"(st3=(setVarBool st2 ''rotation'' False))"
 and st4:"(st4=(setPstate st3 ''Controller'' ''motionless''))"
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
 and st2_Controller_state:"(getPstate st2 ''Controller'')=''rotating''"
 and st2_if5:"(getVarBool st2 ''pressure'')=False"
 and st2_if7:"(getVarBool st2 ''user'')=False"
 and st2_rotating_timeout:"1000\<le>(ltime st2 ''Controller'')"
 and st3:"(st3=(setVarBool st2 ''rotation'' False))"
 and st4:"(st4=(setPstate st3 ''Controller'' ''motionless''))"
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
 assumes base_inv:"(inv6 st0)"
 and st1:"(st1=(setVarBool st0 ''pressure'' pressure))"
 and st2:"(st2=(setVarBool st1 ''user'' user))"
 and st2_Controller_state:"(getPstate st2 ''Controller'')=''rotating''"
 and st2_if5:"(getVarBool st2 ''pressure'')=False"
 and st2_if7:"(getVarBool st2 ''user'')=False"
 and st2_rotating_timeout:"1000\<le>(ltime st2 ''Controller'')"
 and st3:"(st3=(setVarBool st2 ''rotation'' False))"
 and st4:"(st4=(setPstate st3 ''Controller'' ''motionless''))"
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

lemma
 assumes base_inv:"(inv4 st0)"
 and st1:"(st1=(setVarBool st0 ''pressure'' pressure))"
 and st2:"(st2=(setVarBool st1 ''user'' user))"
 and st2_Controller_state:"(getPstate st2 ''Controller'')=''rotating''"
 and st2_if5:"(getVarBool st2 ''pressure'')=False"
 and st2_if7:"(getVarBool st2 ''user'')=False"
 and st2_rotating_timeout:"1000\<le>(ltime st2 ''Controller'')"
 and st3:"(st3=(setVarBool st2 ''rotation'' False))"
 and st4:"(st4=(setPstate st3 ''Controller'' ''motionless''))"
 and st5:"(st5=(toEnv st4))"
 and st_final:"(st_final=st5)"
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
    proof (simp only: P1_def inv4_A1_def p_2_3_conpred_def R4_A3_def SMT.verit_bool_simplify(4); rule allI; rule impI)
      fix x
      assume prem:"(toEnvP x \<and> substate x st0) \<and>
               toEnvNum x st_final < 10 \<and>
               getVarBool (predEnv x) ''rotation'' \<and>
               getVarBool x ''pressure''"
      then have prem_add1:"substate (predEnv x) st0" using predEnv_substate substate_trans by blast
      have 1:"substate st0 st_final \<and> toEnvNum st0 st_final =1" using 0 predEnv_substate predEnv_toEnvNum by auto
      then have 2:"toEnvNum x st0 < 9" using toEnvNum3 prem by force
      have 3:"toEnvP (predEnv x) \<and> substate (predEnv x) x \<and> toEnvNum (predEnv x) x =1" using predEnv_substate predEnv_toEnvNum predEnvP_or_emptyState prem by force
      then have 4:"toEnvNum (predEnv x) st0 <10" using 2 predEnv_substate toEnvNum3 predEnv_toEnvNum prem predEnvP_or_emptyState by force 
      have 5:"extra6 st0" using assms inv4_def extraInv_def by auto
      have "ltime st0 ''Controller'' div 100 \<ge> 10" using assms by force
      then have 6:"toEnvNum (predEnv x) st0 < ltime st0 ''Controller'' div 100" using 4 by auto
      have 7:"getPstate st0 ''Controller'' = ''rotating''" using assms by auto
      have 8:"getPstate (predEnv x) ''Controller'' = ''rotating'' \<and> \<not> getVarBool x ''user''" using prem 0 3 5 6 7 extra6_def substate_refl by blast
      then have 9:"getVarBool (predEnv x) ''rotation''" using prem_add1 base_inv inv4_def extraInv_def extraRotatingOut_def prem by blast
      have 10:"R4_full st0" using assms inv4_def by auto
      consider (a) "getVarBool x ''pressure''" | (b) "\<not>getVarBool x ''pressure''" by auto
      then have "\<not>getVarBool x ''pressure''"
      proof (cases)
        case a
        have "toEnvP (predEnv x) \<and> toEnvP x \<and> toEnvP st0 \<and> 
                substate (predEnv x) x \<and> substate x st0 \<and> substate st0 st0 \<and>
                toEnvNum (predEnv x) x = 1 \<and> toEnvNum x st0 <10 \<and>
                getVarBool (predEnv x) ''rotation'' \<and> getVarBool x ''pressure''" using 3 prem base_inv inv4_def extraInv_def substate_refl predEnv_toEnvNum 2 9 by auto
        then have "getVarBool st0 ''brake''" using 10 R4_full_def a  by auto
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
      apply (simp only: inv4_A1_def p_2_3_conpred_def R4_A3_def)
      using 0 assms inv4_def extraInv_def extraMotionlessOut_def by auto
    ultimately show ?thesis using P3_lemma A3_R4 by auto
  qed
qed

find_theorems "_ \<and> _ \<longrightarrow> _"
end
