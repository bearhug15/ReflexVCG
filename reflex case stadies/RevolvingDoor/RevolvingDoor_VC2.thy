theory RevolvingDoor_VC2
imports RevolvingDoorTheory
begin

lemma extra:
 assumes base_inv:"(extraInv st0)"
 and st1:"(st1=(setVarBool st0 ''pressure'' pressure))"
 and st2:"(st2=(setVarBool st1 ''user'' user))"
 and st2_Controller_state:"(getPstate st2 ''Controller'')=''motionless''"
 and st2_if0:"(getVarBool st2 ''user'')=True"
 and st2_if2:"(getVarBool st2 ''pressure'')=False"
 and st3:"(st3=(setVarBool st2 ''rotation'' True))"
 and st4:"(st4=(setPstate st3 ''Controller'' ''rotating''))"
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
      have "getPstate st0 ''Controller'' = ''motionless''" using assms by auto
      moreover have "extraMotionlessOut st0" using assms extraInv_def by auto
      ultimately have "\<not> getVarBool st0 ''brake''" using extraMotionlessOut_def substate_refl assms extraInv_def by auto
      then have 2:"\<not> getVarBool st_final ''brake''" using assms by auto
      have 3: "getVarBool st_final ''rotation''" using assms by auto
      have 4: "ltime st_final ''Controller'' \<le> 1000" using assms by auto
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
  show "extra2 st_final"
  proof (simp only: extra2_def; rule allI; rule impI)
    fix s1
    assume prems:"toEnvP s1 \<and>
                  substate s1 st_final \<and>
                  getPstate s1 ''Controller'' = ''rotating''"
    thus "\<exists>s2 s3.
             toEnvP s2 \<and>
             toEnvP s3 \<and>
             substate s2 s3 \<and>
             substate s3 s1 \<and>
             toEnvNum s2 s3 = 1 \<and>
             toEnvNum s2 s1 = ltime s1 ''Controller'' div 100 \<and>
             ((getPstate s2 ''Controller'' = ''motionless'' \<or>
               getPstate s2 ''Controller'' = ''rotating'') \<and>
              getVarBool s3 ''user'' \<or>
              getPstate s2 ''Controller'' = ''suspended'') \<and>
             \<not> getVarBool s3 ''pressure''"
    proof (cases)
      assume 1:"s1=st_final"
      have "toEnvP st0 \<and> 
            toEnvP st_final" using  assms extraInv_def by auto
      moreover have "substate st0 st_final" using assms substate_refl by auto 
      moreover have "substate st_final s1" using 1 substate_refl by auto
      moreover have "toEnvNum st0 st_final =1" using assms toEnvNum_id by auto
      moreover have "toEnvNum st0 s1 = ltime s1 ''Controller'' div 100" using 1 assms toEnvNum_id by auto
      moreover have "getPstate st0 ''Controller'' = ''motionless'' \<and> getVarBool st_final ''user''" using assms by auto
      moreover have "\<not> getVarBool st_final ''pressure''" using assms by auto
      ultimately show ?thesis by auto
    next
      assume 1:"s1\<noteq>st_final"
      have "predEnv st_final = st0" using assms extraInv_def by auto
      then have 2:"substate s1 st0" using 1 assms prems substate_eq_or_predEnv by blast
      have "extra2 st0" using assms extraInv_def by auto
      then show ?thesis using 1 2 prems assms extraInv_def extra2_def by auto
    qed
  qed
next
  show "extra3 st_final"
  proof (simp only:extra3_def; rule allI; rule impI)
    fix s1
    assume prems:"toEnvP s1 \<and>
            substate s1 st_final \<and>
            getPstate s1 ''Controller'' = ''rotating''"
    then show "\<exists>s2 s3.
             toEnvP s2 \<and>
             toEnvP s3 \<and>
             substate s2 s3 \<and>
             substate s3 s1 \<and>
             toEnvNum s2 s3 = 1 \<and>
             (getPstate s2 ''Controller'' = ''motionless'' \<and>
              getVarBool s3 ''user'' \<or>
              getPstate s2 ''Controller'' = ''suspended'') \<and>
             \<not> getVarBool s3 ''pressure'' \<and>
             (\<forall>s4. toEnvP s4 \<and> substate s3 s4 \<and> substate s4 s1 \<longrightarrow>
                   getPstate s4 ''Controller'' = ''rotating'')"
    proof (cases)
      assume 1:"s1=st_final"
      have "toEnvP st0 \<and> 
            toEnvP st_final" using  assms extraInv_def by auto
      moreover have "substate st0 st_final" using assms substate_refl by auto 
      moreover have "substate st_final s1" using 1 substate_refl by auto
      moreover have "toEnvNum st0 st_final =1" using assms toEnvNum_id by auto
      moreover have "getPstate st0 ''Controller'' = ''motionless'' \<and> getVarBool st_final ''user''" using assms by auto
      moreover have "\<not> getVarBool st_final ''pressure''" using assms by auto
      moreover have "(\<forall>s4. toEnvP s4 \<and> substate st_final s4 \<and> substate s4 s1 \<longrightarrow>getPstate s4 ''Controller'' = ''rotating'')" using assms 1 substate_asym prems by blast
      ultimately show ?thesis by auto
    next
      assume 1:"s1\<noteq>st_final"
      have "predEnv st_final = st0" using assms extraInv_def by auto
      then have 2:"substate s1 st0" using 1 assms prems substate_eq_or_predEnv by blast
      have "extra3 st0" using assms extraInv_def by auto
      then show ?thesis using 1 2 prems assms extraInv_def extra3_def by auto
    qed
  qed
next
  show "extra6 st_final"
  proof (simp only: extra6_def; intro allI; rule impI)
    fix s1 s2 s3
    assume prems:"toEnvP s1 \<and>
       toEnvP s2 \<and>
       toEnvP s3 \<and>
       substate s1 s2 \<and>
       substate s2 s3 \<and>
       substate s3 st_final \<and>
       getPstate s3 ''Controller'' = ''rotating'' \<and>
       toEnvNum s1 s2 = 1 \<and>
       toEnvNum s1 s3 < ltime s3 ''Controller'' div 100"
    then show "getPstate s1 ''Controller'' = ''rotating'' \<and>
       \<not> getVarBool s2 ''user''" using assms extraInv_def extra6_def
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
      then show ?thesis using assms prems extraInv_def extra6_def by auto
    qed
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
 and st2_if2:"(getVarBool st2 ''pressure'')=False"
 and st3:"(st3=(setVarBool st2 ''rotation'' True))"
 and st4:"(st4=(setPstate st3 ''Controller'' ''rotating''))"
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
 and st2_if2:"(getVarBool st2 ''pressure'')=False"
 and st3:"(st3=(setVarBool st2 ''rotation'' True))"
 and st4:"(st4=(setPstate st3 ''Controller'' ''rotating''))"
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
 and st2_Controller_state:"(getPstate st2 ''Controller'')=''motionless''"
 and st2_if0:"(getVarBool st2 ''user'')=True"
 and st2_if2:"(getVarBool st2 ''pressure'')=False"
 and st3:"(st3=(setVarBool st2 ''rotation'' True))"
 and st4:"(st4=(setPstate st3 ''Controller'' ''rotating''))"
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
 and st2_Controller_state:"(getPstate st2 ''Controller'')=''motionless''"
 and st2_if0:"(getVarBool st2 ''user'')=True"
 and st2_if2:"(getVarBool st2 ''pressure'')=False"
 and st3:"(st3=(setVarBool st2 ''rotation'' True))"
 and st4:"(st4=(setPstate st3 ''Controller'' ''rotating''))"
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
      have 7:"getPstate st0 ''Controller'' = ''motionless''" using assms by auto
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

find_theorems "(_ \<longrightarrow>_ \<longrightarrow> _)"

(*
\<forall>x. toEnvP x \<and> substate x st0 \<longrightarrow>
        toEnvNum x st_final < 10 \<and>
        getVarBool (predEnv x) ''rotation'' \<and>
        getVarBool x ''pressure'' \<longrightarrow>
        getVarBool st_final ''brake''
*)
end
