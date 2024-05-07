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
      have 2:"extra2 st0" using assms extraInv_def by auto
      have 3:"substate st0 st_final" using assms substate_refl by auto
      show ?thesis using 1 2 3 assms extraInv_def extra2_def by sorry
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
    proof - 
      have "extra3 st0" using assms extraInv_def by auto
      then obtain 1:"(\<forall> s1. 
                toEnvP s1 \<and> 
                substate s1 st0 \<and> 
                getPstate s1 ''Controller'' = ''rotating'' \<longrightarrow>
                  (\<exists> s2 s3. 
                    toEnvP s2 \<and> 
                    toEnvP s3 \<and> 
                    substate s2 s3 \<and> 
                    substate s3 s1 \<and> 
                    toEnvNum s2 s3 = 1 \<and> 
                    (getPstate s2 ''Controller'' = ''motionless'' \<and> 
                      getVarBool s3 ''user'' \<or>
                      getPstate s2 ''Controller'' = ''suspended'') \<and> 
                    \<not> getVarBool s3 ''pressure'' \<and>
                    (\<forall> s4. 
                      toEnvP s4 \<and> 
                      substate s3 s4 \<and> 
                      substate s4 s1 \<longrightarrow> 
                        getPstate s4 ''Controller'' = ''rotating'')))" using extra3_def by auto
      have 2:"substate st0 st_final" using assms substate_refl by auto
      then show ?thesis using 1 assms prems extraInv_def extra3_def substate_refl substate_trans by sorry
    qed
  qed
next
  show "extra6 st_final"
  proof -
    define ex6_A1 where "ex6_A1 = P3_to_predEnv ex6_A3 st_final"
    moreover have "toEnvP st0 \<and> toEnvP st_final \<and> st0 = predEnv st_final" using assms extraInv_def by auto
    moreover have "P3_predEnv ex6_A3 st0" using assms extraInv_def ex6_A3 by auto
    moreover have "ex6_A1 st_final" 
    proof -
      have "getPstate st0 ''Controller'' = ''rotating''" using assms by auto
      moreover have "\<not>getVarBool st_final ''user''" using assms by auto
      ultimately show  "ex6_A1 st_final" using ex6_A1_def P3_to_predEnv_def ex6_A3_def assms extraInv_def by auto
    qed
    moreover have "P1 ex6_A1 st0"  by sorry
    ultimately show ?thesis using A3_ex6_sub P3_cons_from_predEnv P3_predEnv by blast 
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
    moreover have "P2_predEnv R1_A2 st0" using R1_A2 assms inv1_def by auto
    moreover have "R1_A2 st_final st0" using assms R1_A2_def by auto
    ultimately show ?thesis using two_pos_cond_cons_exp A2_R1 by auto
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
    moreover have "P2_predEnv R2_A2 st0" using R2_A2 assms inv2_def by auto
    moreover have "R2_A2 st_final st0" using assms R2_A2_def by auto
    ultimately show ?thesis using two_pos_cond_cons_exp A2_R2 by auto
  qed
qed

lemma
 assumes base_inv:"(inv st0)"
 and st1:"(st1=(setVarBool st0 ''pressure'' pressure))"
 and st2:"(st2=(setVarBool st1 ''user'' user))"
 and st2_Controller_state:"(getPstate st2 ''Controller'')=''rotating''"
 and st2_if5:"(getVarBool st2 ''pressure'')=False"
 and st2_if7:"(getVarBool st2 ''user'')=False"
 and st2_rotating_timeout:"1000>(ltime st2 ''Controller'')"
 and st3:"(st3=(toEnv st2))"
 and st_final:"(st_final=st3)"
shows "(inv st_final)"

end
